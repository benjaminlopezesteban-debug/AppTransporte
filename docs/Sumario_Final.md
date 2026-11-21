# Sumario Final - Backend AppBelmonte

Fecha: 2026-11-14

Estado: Fase 1 y Fase 2 aplicadas. Los 18 proyectos Maven compilan y las 23
pruebas unitarias pasan. MySQL, Flyway, Eureka, gateway, Feign y Docker Compose
quedaron incorporados. No hay autenticacion ni autorizacion: solo
`POST /api/auth/login` valida credenciales. La identidad se envia en el body o
en la ruta.

## 1. Arquitectura del sistema

AppBelmonte usa una arquitectura de microservicios sincrona:

- Un proyecto Maven independiente por entidad del modelo.
- 15 servicios de negocio, mas `eureka-server`, `gateway` y
  `plantilla-servicio`.
- MySQL XAMPP con base compartida `appbelmonte`.
- `plantilla-servicio` usa la base separada `appbelmonte_plantilla`.
- Flyway crea el esquema y carga los datos de prueba.
- Hibernate solo valida el esquema (`ddl-auto=validate`).
- Eureka registra los servicios en el puerto `8761`.
- El gateway WebFlux escucha en `8080` y enruta `/api/...` con `lb://`.
- La comunicacion interna usa Feign con `spring-cloud-starter-loadbalancer`.
- No se reenvia `Authorization`: no existe token ni sesion.
- No hay filtros de seguridad ni validacion de identidad en las llamadas
  internas.

Capas dentro de cada servicio:

```text
Controller -> Service -> Repository -> MySQL
                  |
                  +-> Feign Client -> otro microservicio
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
| `viaje-servicio` | 8094 | `Viaje` | solicitud, conductor, estado-viaje, historial | Sin datos |
| `historial-servicio` | 8095 | `HistorialEstado` | `estado-viaje-servicio` | Sin datos |

Infraestructura:

- `eureka-server` (`8761`): registro de servicios.
- `gateway` (`8080`): entrada unica de la app Android.
- `plantilla-servicio` (`8099`): base reutilizable, fuera de Docker Compose.

## 3. Dependencias tecnicas

- Java 21.
- Spring Boot 4.1.2.
- Spring Cloud `2025.1.4`.
- Maven Wrapper (`./mvnw`) en cada proyecto.
- `spring-boot-starter-webmvc`.
- `spring-boot-starter-data-jpa`.
- `spring-boot-starter-validation`.
- `spring-boot-starter-flyway`.
- `org.flywaydb:flyway-mysql`.
- `com.mysql:mysql-connector-j`.
- `spring-cloud-starter-netflix-eureka-client`.
- `spring-cloud-starter-openfeign` en los 9 servicios con clientes.
- `spring-cloud-starter-loadbalancer` en esos mismos servicios.
- `spring-cloud-starter-netflix-eureka-server` en `eureka-server`.
- `spring-cloud-starter-gateway-server-webflux` en `gateway`.
- Lombok.
- Jackson 3, incluido por Spring Boot 4.
- `spring-boot-starter-test` y `spring-boot-starter-webmvc-test` para pruebas.

Ya no se usan H2, `spring-boot-h2console`, `spring-boot-starter-security`, JJWT
ni `PasswordEncoder`.

## 4. Relaciones entre funciones y servicios

### Crear una solicitud

`SolicitudService.crear`:

1. Consulta el estado PENDIENTE en `estado-solicitud-servicio`.
2. Crea el pasajero en `pasajero-servicio`.
3. Crea la direccion de origen en `direccion-servicio`.
4. Crea la direccion de destino en `direccion-servicio`.
5. Guarda la solicitud con los ids obtenidos, estado PENDIENTE y el
   `solicitanteId` recibido en el body.

Si falla un paso, no se guarda la solicitud. Si ya se crearon pasajero o
direcciones, quedan como registros sobrantes; es una decision aceptada para el
MVP.

### Asignar conductor

`ViajeService.asignar`:

1. Consulta la solicitud y verifica que este PENDIENTE.
2. Verifica que no exista ya un viaje para esa solicitud.
3. Consulta el conductor y verifica que este disponible.
4. Guarda el viaje en estado ASIGNADO con `asignadoPorId` recibido en el body.
5. Llama a `PUT /api/solicitudes/{id}/asignar`.
6. Registra el cambio en `historial-servicio` con `cambiadoPorId`.

Si falla la asignacion remota, la transaccion local revierte y el viaje no queda
guardado. Si falla el historial, solo se escribe un aviso en el log y la
asignacion se mantiene.

### Iniciar y finalizar

- `iniciar`: solo valida que el viaje este ASIGNADO. Guarda `horaInicioReal` y
  pasa a EN_CURSO.
- `finalizar`: solo valida que el viaje este EN_CURSO. Guarda `horaFinReal` y
  pasa a FINALIZADO.
- Cada cambio registra un evento en `historial-servicio` usando el conductor del
  viaje como `cambiadoPorId`.
- No se valida que quien invoca sea el conductor del viaje.

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

## 5. Autenticacion y alcance de seguridad

- La contrasena se guarda en texto plano.
- `POST /api/auth/login` compara correo y contrasena, y devuelve `id`, `rolId`,
  `rolNombre` y `nombre`. No emite token.
- No hay Spring Security, JWT, filtros, `@PreAuthorize`, headers de identidad ni
  validacion de propiedad del recurso.
- Todos los endpoints son publicos.
- La identidad se envia en el body o en la ruta:
  - `SolicitudRequest.solicitanteId`.
  - `AsignacionRequest.asignadoPorId` y `ReasignacionRequest.asignadoPorId`.
  - `HistorialRequest.cambiadoPorId`.
  - `GET /api/usuarios/{id}`.
  - `POST /api/conductores/{usuarioId}`.
  - `GET /api/solicitudes/mias/{solicitanteId}`.
  - `GET /api/viajes/mis-viajes/{conductorId}`.
- Limitacion: cualquier cliente puede invocar cualquier endpoint y suplantar
  cualquier id. No hay autenticacion real ni autorizacion.
- El registro publico sigue limitado a los roles SOLICITANTE (id 2) y CONDUCTOR
  (id 3) por validacion de negocio, no por seguridad.

## 6. Flujo sin token

```text
Android -> gateway:8080 -> usuario-servicio : POST /api/auth/login
usuario-servicio -> Android : id, rolId, rolNombre, nombre
Android -> gateway:8080 -> servicio X : ids en body o ruta
servicio X -> Eureka/Feign -> servicio Y : sin Authorization
```

- No hay propagacion de identidad entre servicios.
- Las operaciones que necesitan saber quien actua reciben el id explicitamente.
- No hay expiracion, refresh token ni sesion.

## 7. Llamadas API entre servicios

- Se usa Feign con descubrimiento de Eureka. Los clientes declaran
  `@FeignClient(name = "<servicio>")` y rutas `/api/...`.
- Los 14 clientes conservan sus metodos publicos como `default methods` y
  traducen errores.
- Los modulos con clientes tienen `@EnableFeignClients`:
  comuna, conductor, direccion, historial, modelo-vehiculo, solicitud, usuario,
  vehiculo y viaje.
- Ya no existen `RestClientConfig` ni propiedades `servicios.*.url`.
- Timeouts Feign: conexion `2000 ms` y lectura `5000 ms`.
- Los clientes traducen errores:
  - `404` de un id relacionado: `DatosInvalidosException` (`400`).
  - `409` de una solicitud ya asignada: `ReglaNegocioException` (`409`).
  - Error de conexion, timeout o `5xx`: `ServicioNoDisponibleException` (`503`).
- `SolicitudClient.listarPorIds` convierte la lista de ids a CSV antes de
  llamar a `GET /api/solicitudes?ids=`.
- La asignacion de viaje es la operacion con mas llamadas remotas:
  solicitud, conductor, solicitud (asignar) e historial.
- La unicidad de `solicitudId` en `Viaje` y la validacion de estado evitan
  asignaciones duplicadas.

## 8. Base de datos, Flyway y convenciones

- Una entidad JPA por servicio.
- Las claves foraneas son ids logicos; no hay relaciones JPA entre servicios.
- XAMPP/MariaDB: usuario `appbelmonte`, puerto `3307`.
- La migracion a MySQL 8.4 en Docker esta planificada; ver
  `docs/Comparativa_XAMPP_vs_MySQL_Docker.md`.
- Base compartida `appbelmonte`; `plantilla-servicio` usa
  `appbelmonte_plantilla`.
- `spring.jpa.hibernate.ddl-auto=validate` en todos los servicios.
- Flyway crea las tablas y carga los seeds.
- Ruta de migraciones: `src/main/resources/db/migration`.
- Nomenclatura:
  - `V1__crear_tabla_<tabla>.sql`.
  - `V2__datos_prueba_<tabla>.sql`.
  - Columnas en `snake_case`; Hibernate convierte los campos Java `camelCase`
    automaticamente.
- `viaje-servicio` e `historial-servicio` solo tienen `V1`.
- Los datos iniciales usan ids explicitos.
- `solicitudes_traslado.fecha_creacion` usa una fecha fija:
  `2026-11-01 09:00:00`.
- Restricciones de unicidad:
  - `Usuario.correo`.
  - `Vehiculo.patente`.
  - `Conductor.numeroLicencia`.
  - `Viaje.solicitudId`.
  - `Rol.nombre`, `Marca.nombre`, `Region.nombre`, `EstadoSolicitud.nombre` y
    `EstadoViaje.nombre`.
- Fechas:
  - `DATE` para la fecha del traslado.
  - `TIME(6)` para la hora del traslado.
  - `DATETIME(6)` para creacion, asignacion, inicio, fin e historial.
- Tipos DDL alineados con Hibernate: `BIGINT`, `INTEGER`, `VARCHAR(255)`,
  `DOUBLE`, `BIT(1)`, `DATE`, `TIME(6)` y `DATETIME(6)`.
- `Conductor.id` no usa `AUTO_INCREMENT`; el resto de ids si.

### Flyway y control de migraciones

Cada servicio tiene su propia tabla de historial para compartir la misma base:

| Servicio | Tabla de historial |
|---|---|
| rol-servicio | `flyway_schema_history_rol` |
| usuario-servicio | `flyway_schema_history_usuario` |
| marca-servicio | `flyway_schema_history_marca` |
| modelo-vehiculo-servicio | `flyway_schema_history_modelo_vehiculo` |
| vehiculo-servicio | `flyway_schema_history_vehiculo` |
| conductor-servicio | `flyway_schema_history_conductor` |
| region-servicio | `flyway_schema_history_region` |
| comuna-servicio | `flyway_schema_history_comuna` |
| direccion-servicio | `flyway_schema_history_direccion` |
| pasajero-servicio | `flyway_schema_history_pasajero` |
| estado-solicitud-servicio | `flyway_schema_history_estado_solicitud` |
| solicitud-servicio | `flyway_schema_history_solicitud` |
| estado-viaje-servicio | `flyway_schema_history_estado_viaje` |
| viaje-servicio | `flyway_schema_history_viaje` |
| historial-servicio | `flyway_schema_history_historial` |
| plantilla-servicio | `flyway_schema_history_plantilla` |

Configuracion obligatoria:

```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
spring.flyway.table=flyway_schema_history_<servicio>
spring.flyway.locations=classpath:db/migration
```

Riesgos y reglas de control:

- `baseline-version=0` es obligatorio. Con `baseline-version=1`, Flyway omite
  `V1` cuando hace baseline sobre una base con tablas y el esquema queda
  incompleto.
- `baseline-on-migrate` no adopta tablas existentes, no corrige diferencias de
  esquema y no vuelve idempotentes los seeds.
- La primera ejecucion debe hacerse sobre una base limpia. Si ya hay tablas sin
  historial Flyway, recrear la base o hacer un baseline manual.
- MySQL hace commit implicito en cada DDL. Las migraciones deben ser pequenas y
  revisadas antes de ejecutar.
- No usar `spring.jpa.hibernate.ddl-auto=update` en ningun perfil.

## 9. Gateway, Eureka y Docker

- `eureka-server` en `8761`, sin registrarse ni buscar registro.
- Los clientes usan
  `eureka.client.service-url.defaultZone=${EUREKA_URL:http://localhost:8761/eureka/}`.
- `gateway` en `8080` con WebFlux.
- Starter del gateway: `spring-cloud-starter-gateway-server-webflux`.
- Namespace de rutas: `spring.cloud.gateway.server.webflux.routes`.
- Rutas por recurso con `lb://<servicio>`, sin reescritura de path.
- El gateway no usa OpenFeign ni `spring-boot-starter-webmvc`.
- Docker Compose levanta Eureka, gateway y los 15 servicios; no levanta MySQL.
- Cada servicio usa `DB_URL` con
  `jdbc:mysql://host.docker.internal:3307/appbelmonte` mientras se usa XAMPP.
- En Linux se agrega `host.docker.internal:host-gateway`.
- `Dockerfile` multi-stage con `ARG SERVICE`.
- `scripts/init-mysql.sql` crea `appbelmonte` y `appbelmonte_plantilla` como
  respaldo.

## 10. Flujo completo validado

Con los 15 servicios encendidos:

1. Solicitante inicia sesion (solo validacion de credenciales), consulta
   regiones y comunas, y crea una solicitud -> PENDIENTE.
2. Administrador lista pendientes y conductores disponibles.
3. Administrador asigna conductor -> solicitud ASIGNADA y viaje ASIGNADO.
4. Conductor ve el viaje en `mis-viajes/{conductorId}/hoy` con coordenadas.
5. Conductor inicia el viaje -> EN_CURSO.
6. Conductor finaliza el viaje -> FINALIZADO.
7. Administrador consulta el historial y ve los cambios con sus nombres.

Casos de error comprobados:

| Caso | Resultado |
|---|---|
| Finalizar viaje ya finalizado | 409 |
| Cancelar solicitud asignada | 409 |
| Asignar solicitud no pendiente | 409 |
| Direccion con comuna inexistente | 400 |
| Vehiculo con patente repetida | 409 |
| Credenciales invalidas en login | 401 |
| Asignar con `solicitud-servicio` apagado | 503 y sin viaje guardado |

Los casos de acceso denegado por rol o por pertenencia ya no existen.

## 11. Pruebas

Pruebas unitarias con clientes simulados (Mockito):

- `SolicitudServiceTest`: 7 pruebas.
  - Creacion con estado PENDIENTE y ids remotos.
  - Cancelacion solo si esta PENDIENTE.
  - Asignacion solo si esta PENDIENTE.
  - Filtros por fecha y listado propio.
- `ViajeServiceTest`: 12 pruebas.
  - Asignacion valida, solicitud no pendiente, conductor no disponible,
    solicitud duplicada y fallo remoto.
  - Reasignacion solo en ASIGNADO y con conductor disponible.
  - Inicio y fin en el estado correcto.
  - Historial fallido sin romper el cambio de estado.
  - Listado de viajes del conductor.
- `UsuarioServiceTest`: 4 pruebas.
  - Login valido con rol y nombre.
  - Contrasena invalida, correo inexistente y usuario inactivo.

Las 23 pruebas unitarias pasan. Se eliminaron las 16 pruebas de contexto
`@SpringBootTest` porque dependian de H2.

Validacion realizada:

- `mvn test` en los 18 proyectos: correcto.
- `mvn -DskipTests package` en los 18 proyectos: correcto.
- Arranque de los 16 modulos de servicio contra MySQL 8.4 temporal en el puerto
  `3307`: correcto. Flyway aplico `V1`/`V2` y Hibernate valido el esquema.
- Arranque de los 16 modulos contra MariaDB 10.4.32 de XAMPP en un datadir
  limpio y puerto `3307`: correcto. Flyway y Hibernate pasaron.
- Se ejecuto `mysql_upgrade` sobre el datadir real de XAMPP; el arranque de los
  16 modulos y el stack completo contra ese datadir tambien paso.
- Stack completo con Eureka, gateway y 15 servicios: 16 aplicaciones registradas.
  El gateway puede devolver `503` durante los primeros segundos hasta que su
  cache de Eureka se actualiza; despues las rutas responden `200`.
- Gateway validado en `8080`:
  - `GET /api/regiones` y `GET /api/comunas?regionId=1`: 200.
  - `POST /api/auth/login`: 200 con rol y nombre.
  - `GET /api/solicitudes/1`: 200 con pasajero, direcciones, comuna y region.
  - `POST /api/solicitudes`: 201 con creacion remota de pasajero y direcciones.
  - `POST /api/viajes`: 201 con solicitud `ASIGNADA` y viaje `ASIGNADO`.
  - `PUT /api/viajes/1/iniciar` y `/finalizar`: 200.
  - `GET /api/historial/viaje/1`: 200 con los tres cambios.
- `docker build --build-arg SERVICE=rol-servicio`: correcto.
- `docker compose config --quiet`: correcto.
- El datadir de XAMPP fue creado con MariaDB 10.1.8. Se ejecuto
  `mysql_upgrade` y quedo actualizado a MariaDB 10.4.32; Flyway ya funciona
  sobre ese datadir. Si se restaura un respaldo antiguo, repetir el upgrade.

## 12. Estado de entrega y alcance

Terminado:

- 15 servicios implementados y compilados.
- Login con validacion de credenciales, sin token.
- Identidad por body/path y endpoints publicos.
- MySQL con Flyway y seeds en `V2`.
- Eureka, gateway y Feign integrados.
- Docker Compose para Eureka, gateway y los 15 servicios.
- Seeds de datos ficticios con contrasena `clave123` en texto plano.
- Pruebas unitarias actualizadas.
- README y documentacion actualizados.

Fuera de esta etapa:

- Coleccion Postman: la realizara el equipo por separado.
- Autenticacion real: se reintroducira en una etapa posterior.
- Commit de git: no se ha creado ningun commit.

## 13. Decisiones y riesgos

- `GET /api/viajes?fecha=` y `GET /api/viajes/mis-viajes/{conductorId}/hoy`
  filtran por `fechaAsignacion`, no por la fecha de la solicitud.
- MySQL es persistente, a diferencia de H2. Reiniciar un servicio ya no borra
  sus datos ni recarga seeds.
- Las migraciones Flyway son parte del codigo y no deben modificarse una vez
  aplicadas en una base compartida.
- `baseline-on-migrate` se usa por la base compartida; cada servicio tiene su
  propia tabla de historial.
- La primera ejecucion requiere una base limpia o un baseline manual.
- Las llamadas remotas ocurren dentro de transacciones locales. Si la
  asignacion remota de la solicitud tiene exito pero el commit local falla,
  puede quedar una solicitud ASIGNADA sin viaje. Es un riesgo aceptado para el
  MVP.
- La creacion de solicitud puede dejar pasajero o direcciones sobrantes si un
  paso posterior falla. Es un riesgo aceptado para el MVP.
- Hay timeouts Feign explicitos, pero no reintentos ni circuit breaker.
- El gateway centraliza el acceso, pero no agrega autenticacion ni autorizacion.
- No hay seguridad real: la fase actual es funcional, no segura.
- El arranque se valido contra MySQL 8.4 y MariaDB 10.4.32 de XAMPP. El datadir
  real de XAMPP ya paso por `mysql_upgrade` y quedo operativo.

## 14. Fase 2 aplicada

- MySQL en XAMPP con base compartida `appbelmonte`.
- `plantilla-servicio` con `appbelmonte_plantilla`.
- Flyway con `V1`/`V2` y tabla de historial por servicio.
- `ddl-auto=validate` en todos los servicios.
- Eliminacion de H2, consola H2 y pruebas de contexto.
- Migracion de 14 clientes `RestClient` a Feign.
- `@EnableFeignClients` en los 9 servicios con clientes.
- Eliminacion de `RestClientConfig` y `servicios.*.url`.
- Creacion de `eureka-server` y `gateway`.
- Gateway WebFlux con 15 rutas y Eureka.
- Docker Compose con 17 contenedores, sin MySQL.
- Dockerfile validado con `rol-servicio`; Compose validado con
  `docker compose config`.
- Script `scripts/init-mysql.sql` para crear las bases.
- Columnas Flyway alineadas a `snake_case` para que `ddl-auto=validate` pase.
- Migracion pendiente a MySQL 8.4 en Docker Compose, con credenciales en
  `.env`; se ejecutara despues de validar Postman y las pruebas.

## 15. Archivos clave

- `plan_backend_appbelmonte.md`: especificacion original.
- `docs/plan-construccion-appbelmonte.md`: patrones y fichas por lote.
- `docs/Comparativa_XAMPP_vs_MySQL_Docker.md`: guia de XAMPP, fricciones,
  comparacion con MySQL 8.4 en Docker y plan de migracion.
- `README.md`: servicios, puertos, rutas, base de datos, Docker y arranque.
- `plantilla-servicio/`: base reutilizable con MySQL, Flyway y Eureka.
- `eureka-server/`: servidor de descubrimiento.
- `gateway/src/main/resources/application.yml`: rutas del gateway.
- `Dockerfile`, `.dockerignore` y `docker-compose.yml`: despliegue.
- `scripts/init-mysql.sql`: creacion de bases.
- `*/src/main/resources/application.properties`: datasource, Flyway y Eureka.
- `*/src/main/resources/db/migration/`: migraciones `V1` y `V2`.
- `*/src/main/java/**/client/*.java`: clientes Feign.
- `solicitud-servicio/.../SolicitudService.java`: creacion, cancelacion,
  listados y asignacion.
- `viaje-servicio/.../ViajeService.java`: asignacion, reasignacion, inicio,
  fin, consultas e historial.
- `historial-servicio/.../HistorialService.java`: registro y consulta de
  cambios de estado.
