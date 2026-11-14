# Sumario Final - Backend AppBelmonte

Fecha: 2026-11-10

Estado: backend terminado y validado. Los 15 microservicios compilan, las
pruebas unitarias pasan y el flujo completo se ejecuto con todos los servicios
encendidos. La coleccion Postman y el gateway quedan fuera de esta etapa.

## 1. Arquitectura del sistema

AppBelmonte usa una arquitectura de microservicios sincrona:

- Un proyecto Maven independiente por entidad del modelo.
- Cada servicio tiene su propio puerto, su propia base H2 en memoria y su
  propio `application.properties`.
- No se usa Eureka, Feign, broker de mensajes ni Config Server.
- La comunicacion entre servicios se hace con `RestClient` de Spring.
- Cada llamada interna reenvia el encabezado `Authorization` recibido.
- Cada servicio valida el JWT localmente con la misma clave secreta.

Capas dentro de cada servicio:

```text
Controller -> Service -> Repository -> H2
                  |
                  +-> Client (RestClient) -> otro microservicio
```

El controller no usa repositorios directamente y el service devuelve DTO, no
entidades JPA.

## 2. Servicios y responsabilidades

| Servicio | Puerto | Entidad | Depende de | Datos iniciales |
|---|---|---|---|---|
| `rol-servicio` | 8081 | `Rol` | Ninguno | 3 roles |
| `usuario-servicio` | 8082 | `Usuario` | `rol-servicio` | 6 usuarios |
| `marca-servicio` | 8083 | `Marca` | Ninguno | Toyota, Hyundai, Kia |
| `modelo-vehiculo-servicio` | 8084 | `ModeloVehiculo` | `marca-servicio` | 1 modelo por marca |
| `vehiculo-servicio` | 8085 | `Vehiculo` | `modelo-vehiculo-servicio` | 3 vehiculos |
| `conductor-servicio` | 8086 | `Conductor` | `vehiculo-servicio` | Perfiles 4, 5 y 6 |
| `region-servicio` | 8087 | `Region` | Ninguno | 2 regiones |
| `comuna-servicio` | 8088 | `Comuna` | `region-servicio` | 6 comunas |
| `direccion-servicio` | 8089 | `Direccion` | `comuna-servicio` | 6 direcciones |
| `pasajero-servicio` | 8090 | `Pasajero` | Ninguno | 3 pasajeros |
| `estado-solicitud-servicio` | 8091 | `EstadoSolicitud` | Ninguno | 3 estados |
| `solicitud-servicio` | 8092 | `SolicitudTraslado` | pasajero, direccion, estado-solicitud | 3 solicitudes |
| `estado-viaje-servicio` | 8093 | `EstadoViaje` | Ninguno | 3 estados |
| `historial-servicio` | 8095 | `HistorialEstado` | `estado-viaje-servicio` | Sin datos |
| `viaje-servicio` | 8094 | `Viaje` | solicitud, conductor, estado-viaje, historial | Sin datos |

`plantilla-servicio` (8099) es la base reutilizable con el ejemplo completo de
usuario, seguridad JWT, manejo de errores y configuracion de `RestClient`.

## 3. Dependencias tecnicas

- Java 21.
- Spring Boot 4.1.2.
- Maven Wrapper (`./mvnw`).
- `spring-boot-starter-webmvc`.
- `spring-boot-starter-data-jpa`.
- `spring-boot-starter-validation`.
- `spring-boot-starter-security`.
- `spring-boot-h2console`.
- H2 en memoria.
- Lombok.
- JJWT 0.12.6 (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`).
- Jackson 3, incluido por Spring Boot 4.
- `spring-boot-starter-test` y `spring-boot-starter-webmvc-test` para pruebas.

## 4. Relaciones entre funciones y servicios

### Crear una solicitud

`SolicitudService.crear`:

1. Consulta el estado PENDIENTE en `estado-solicitud-servicio`.
2. Crea el pasajero en `pasajero-servicio`.
3. Crea la direccion de origen en `direccion-servicio`.
4. Crea la direccion de destino en `direccion-servicio`.
5. Guarda la solicitud con los ids obtenidos, estado PENDIENTE y el id del
   solicitante tomado del token.

Si falla un paso, no se guarda la solicitud. Si ya se crearon pasajero o
direcciones, quedan como registros sobrantes; es una decision aceptada para el
MVP.

### Asignar conductor

`ViajeService.asignar`:

1. Consulta la solicitud y verifica que este PENDIENTE.
2. Verifica que no exista ya un viaje para esa solicitud.
3. Consulta el conductor y verifica que este disponible.
4. Guarda el viaje en estado ASIGNADO.
5. Llama a `PUT /api/solicitudes/{id}/asignar`.
6. Registra el cambio en `historial-servicio`.

Si falla la asignacion remota, la transaccion local revierte y el viaje no queda
guardado. Si falla el historial, solo se escribe un aviso en el log y la
asignacion se mantiene.

### Iniciar y finalizar

- `iniciar`: solo el conductor del viaje, solo desde ASIGNADO. Guarda
  `horaInicioReal` y pasa a EN_CURSO.
- `finalizar`: solo el conductor del viaje, solo desde EN_CURSO. Guarda
  `horaFinReal` y pasa a FINALIZADO.
- Cada cambio registra un evento en `historial-servicio`.

### Consultas enriquecidas

- `comuna-servicio` pide el nombre de la region.
- `modelo-vehiculo-servicio` pide el nombre de la marca.
- `vehiculo-servicio` pide modelo y marca.
- `conductor-servicio` pide los datos del vehiculo.
- `direccion-servicio` pide comuna y region.
- `solicitud-servicio` pide pasajero, direcciones y estado.
- `viaje-servicio` pide la solicitud y el nombre del estado de viaje.
- `historial-servicio` pide los nombres de los estados anterior y nuevo.

Para listas de viajes, `viaje-servicio` usa
`GET /api/solicitudes?ids=1,2,3` y evita una llamada a solicitud por cada viaje.
El nombre del estado se cachea por peticion.

## 5. Seguridad

- La contrasena se guarda con BCrypt.
- `usuario-servicio` emite el JWT en `POST /api/auth/login`.
- El token contiene:
  - `sub`: id del usuario.
  - `rol`: nombre del rol (`ADMINISTRADOR`, `SOLICITANTE`, `CONDUCTOR`).
  - `nombre`: nombre visible del usuario.
- Todos los servicios comparten `jwt.secret` y validan la firma localmente.
- La sesion es sin estado (`SessionCreationPolicy.STATELESS`).
- Cada servicio tiene su `JwtAuthenticationFilter`, que deja como principal el
  id del usuario (`Long`) y como autoridad `ROLE_<rol>`.
- Los permisos se declaran con `@EnableMethodSecurity` y `@PreAuthorize`.
- Rutas publicas:
  - `POST /api/auth/registro`
  - `POST /api/auth/login`
  - `GET /api/roles/**`
- El resto requiere token.
- Sin token o token invalido: `401`.
- Rol sin permiso: `403`.
- El registro publico solo permite los roles SOLICITANTE (id 2) y CONDUCTOR
  (id 3).

## 6. Tokenizacion y propagacion

Flujo del token:

```text
Android -> usuario-servicio : POST /api/auth/login
usuario-servicio -> Android : JWT
Android -> servicio X       : Authorization: Bearer <token>
servicio X -> servicio Y    : Authorization: Bearer <token>
```

Detalles:

- `RestClientConfig` declara el `RestClient.Builder` con `@Scope("prototype")`.
- Un interceptor obtiene la peticion actual con `RequestContextHolder` y copia
  el encabezado `Authorization` a la llamada saliente.
- El token no se regenera ni se cambia entre servicios: se propaga el token del
  usuario que origino la peticion.
- Por eso los permisos del servicio llamado se evaluan con el mismo actor.
- La expiracion es de 24 horas (`jwt.expiracion-ms=86400000`).
- No hay refresh token.

## 7. Llamadas API entre servicios

- Se usa `RestClient` con la URL base de cada servicio en
  `application.properties` (`servicios.<nombre>.url`).
- Todos los servicios tienen las 15 URLs configuradas.
- Los clientes traducen errores:
  - `404` de un id relacionado: `DatosInvalidosException` (`400`).
  - `403` al consultar una solicitud ajena: `AccessDeniedException` (`403`).
  - Error de conexion, timeout o `5xx`: `ServicioNoDisponibleException` (`503`).
- No se usan reintentos, circuit breaker ni timeouts explicitos; se usan los
  valores por defecto de `RestClient`.
- La asignacion de viaje es la operacion con mas llamadas remotas:
  solicitud, conductor, solicitud (asignar) e historial.
- La unicidad de `solicitudId` en `Viaje` y la validacion de estado evitan
  asignaciones duplicadas.

## 8. Modelo de datos y convenciones

- Una entidad JPA por servicio.
- Las claves foraneas son ids logicos; no hay relaciones JPA entre servicios.
- H2 en memoria con `spring.jpa.hibernate.ddl-auto=update`.
- Los datos iniciales se cargan con `CommandLineRunner` e ids explicitos.
- Restricciones de unicidad:
  - `Usuario.correo`.
  - `Vehiculo.patente`.
  - `Conductor.numeroLicencia`.
  - `Viaje.solicitudId`.
- Fechas:
  - `LocalDate` para la fecha del traslado.
  - `LocalTime` para la hora del traslado.
  - `LocalDateTime` para creacion, asignacion, inicio, fin e historial.
- Las bases son independientes; reiniciar un servicio borra solo su base y
  vuelve a cargar sus seeds.

## 9. Flujo completo validado

Con los 15 servicios encendidos:

1. Solicitante inicia sesion, consulta regiones y comunas, y crea una solicitud
   -> PENDIENTE.
2. Administrador inicia sesion, lista pendientes y conductores disponibles.
3. Administrador asigna conductor -> solicitud ASIGNADA y viaje ASIGNADO.
4. Conductor inicia sesion y ve el viaje en `mis-viajes/hoy` con coordenadas.
5. Conductor inicia el viaje -> EN_CURSO.
6. Conductor finaliza el viaje -> FINALIZADO.
7. Administrador consulta el historial y ve los cambios con sus nombres.

Casos de error comprobados:

| Caso | Resultado |
|---|---|
| Iniciar viaje de otro conductor | 403 |
| Finalizar viaje ya finalizado | 409 |
| Cancelar solicitud asignada | 409 |
| Asignar solicitud no pendiente | 409 |
| Direccion con comuna inexistente | 400 |
| Vehiculo con patente repetida | 409 |
| Ruta sin token | 401 |
| Rol equivocado | 403 |
| Asignar con `solicitud-servicio` apagado | 503 y sin viaje guardado |

## 10. Pruebas

Pruebas unitarias con clientes simulados (Mockito):

- `SolicitudServiceTest`: 9 pruebas.
  - Creacion con estado PENDIENTE y ids remotos.
  - Cancelacion solo del dueno y solo si esta PENDIENTE.
  - Asignacion solo si esta PENDIENTE.
  - Consulta ajena como solicitante.
  - Filtros por fecha y listado propio.
- `ViajeServiceTest`: 15 pruebas.
  - Asignacion valida, solicitud no pendiente, conductor no disponible,
    solicitud duplicada y fallo remoto.
  - Reasignacion solo en ASIGNADO y con conductor disponible.
  - Inicio y fin solo por el conductor del viaje y en el estado correcto.
  - Historial fallido sin romper el cambio de estado.
  - Consulta ajena como conductor o solicitante.
  - Listado de viajes del conductor.
- `UsuarioServiceTest`: 4 pruebas.
  - Login valido con token, rol y nombre.
  - Contrasena invalida, correo inexistente y usuario inactivo.

Todas las pruebas pasan, junto con las pruebas de contexto de Spring de los tres
servicios.

## 11. Estado de entrega y alcance

Terminado:

- 15 servicios implementados y compilados.
- Seguridad JWT, roles y permisos.
- Llamadas entre servicios con propagacion de token.
- Seeds de datos ficticios.
- Flujo completo y casos de error validados.
- Pruebas unitarias de las reglas principales.
- README con rutas, usuarios, errores y arranque.

Fuera de esta etapa:

- Coleccion Postman: la realizara el equipo por separado.
- Gateway en el puerto 8080: se aplicara en otra etapa. Por ahora la app debe
  usar una URL base por servicio.
- Commit de git: no se ha creado ningun commit.

## 12. Decisiones y riesgos

- `GET /api/viajes?fecha=` y `GET /api/viajes/mis-viajes/hoy` filtran por
  `fechaAsignacion`, no por la fecha de la solicitud. Es la unica fecha propia
  del viaje.
- Las bases H2 son en memoria: al reiniciar un servicio se pierden sus datos y
  se recargan los seeds. Si solo se reinicia un servicio, las referencias de
  otros servicios pueden quedar apuntando a ids que ya no existen.
- Las llamadas remotas ocurren dentro de transacciones locales. Si la
  asignacion remota de la solicitud tiene exito pero el commit local falla,
  puede quedar una solicitud ASIGNADA sin viaje. Es un riesgo aceptado para el
  MVP.
- La creacion de solicitud puede dejar pasajero o direcciones sobrantes si un
  paso posterior falla. Es un riesgo aceptado para el MVP.
- No hay timeouts explicitos ni reintentos en `RestClient`.
- No hay gateway; cada cliente debe conocer los puertos de los servicios.

## 13. Archivos clave

- `plan_backend_appbelmonte.md`: especificacion original.
- `docs/plan-construccion-appbelmonte.md`: patrones y fichas por lote.
- `README.md`: servicios, puertos, usuarios, rutas, errores y arranque.
- `plantilla-servicio/`: base reutilizable.
- `solicitud-servicio/.../SolicitudService.java`: creacion, cancelacion,
  listados y asignacion.
- `viaje-servicio/.../ViajeService.java`: asignacion, reasignacion, inicio,
  fin, consultas e historial.
- `historial-servicio/.../HistorialService.java`: registro y consulta de
  cambios de estado.
- `*/config/RestClientConfig.java`: propagacion del token.
- `*/config/JwtAuthenticationFilter.java`: validacion del token y autoridades.
