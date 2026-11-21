# Plan de ejecución: Backend AppBelmonte (15 microservicios, modelo en 3FN)

Documento guía para un agente de programación. Describe qué construir, en qué orden y cómo comprobar cada paso.

## 1. Contexto

Belmonte SpA es una empresa de transporte privado corporativo. Hoy gestiona los traslados por llamadas, correos y WhatsApp, lo que genera información dispersa, lentitud y errores.

El proyecto es un MVP educativo (app Android + backend) que centraliza el ciclo completo: el cliente pide un traslado, el administrador asigna un conductor y el conductor lo realiza y lo finaliza.

Este plan cubre solo el **backend**. Usa arquitectura de **microservicios**: cada entidad del modelo de datos (normalizado en tercera forma normal) es un servicio Spring Boot independiente, 15 en total. La app Android (Kotlin, Jetpack Compose, MVVM, Retrofit, Room) se hará después y consumirá estos servicios.

> **Nota histórica (2026-11-14):** las secciones sobre JWT, Spring Security, roles, headers de identidad y `@PreAuthorize` quedaron superadas. La Fase 1 actual no usa autenticación ni autorización: solo el login valida credenciales y la identidad se envía en el body o en la ruta. Ver `docs/Sumario_Final.md`.
>
> **Nota de actualización (2026-11-14):** la Fase 2 también quedó aplicada. MySQL, Flyway, Eureka, gateway, Feign y Docker Compose reemplazan H2, `RestClient` y las URLs `servicios.*.url`. Cada servicio mantiene su propia tabla de historial Flyway dentro de la base compartida `appbelmonte`. Ver `docs/Sumario_Final.md`.

## 2. Reglas para el agente

1. Mantener todo simple. No agregar capas, patrones, entidades ni librerías que este plan no pida.
2. Trabajar solo con datos ficticios. No usar nombres, RUT, correos ni teléfonos reales.
3. Respetar los nombres de servicios, entidades, campos, estados y rutas de este documento. La app Android usará los mismos.
4. Cada servicio es independiente: tiene su propio proyecto Maven, su propio puerto y su propia base de datos. No comparten código ni tablas.
5. Entre servicios no hay relaciones JPA ni restricciones de clave foránea. Una entidad referencia a otra solo guardando su id.
6. Los servicios se comunican solo por API REST (sección 6).
7. No exponer entidades en la API: usar siempre DTO.
8. Seguir el orden de las fases. No avanzar a la siguiente sin cumplir los criterios de la actual.
9. Código en Java con nombres claros. Los comentarios y mensajes de error van en español.
10. Si algo no está definido aquí, elegir la opción más simple y dejarla anotada en un comentario.

## 3. Entorno

| Elemento | Valor |
|---|---|
| Repositorio | Uno solo (monorepo) con una carpeta por servicio |
| Spring Boot | 4.1.2 |
| Java | 21 |
| Gestor | Maven, un proyecto independiente por servicio (no multimódulo) |
| Base de datos | MySQL XAMPP, base compartida `appbelmonte` con historial Flyway por servicio |

### Dependencias de cada servicio (`pom.xml`)

- `spring-boot-starter-webmvc`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `spring-boot-starter-flyway` y `flyway-mysql`
- `mysql-connector-j` (runtime)
- `spring-cloud-starter-netflix-eureka-client`
- `spring-cloud-starter-openfeign` y `spring-cloud-starter-loadbalancer` solo en servicios con clientes
- `lombok` (opcional)
- Dependencias de prueba `*-test`
- Sin `spring-boot-devtools`, sin H2 y sin Spring Security/JJWT

El proyecto `backend` ya creado se usa como base de la plantilla (fase 0).

## 4. Arquitectura

### Servicios

| N.º | Servicio | Puerto | Entidad |
|---|---|---|---|
| 1 | `rol-servicio` | 8081 | Rol |
| 2 | `usuario-servicio` | 8082 | Usuario |
| 3 | `marca-servicio` | 8083 | Marca |
| 4 | `modelo-vehiculo-servicio` | 8084 | ModeloVehiculo |
| 5 | `vehiculo-servicio` | 8085 | Vehiculo |
| 6 | `conductor-servicio` | 8086 | Conductor |
| 7 | `region-servicio` | 8087 | Region |
| 8 | `comuna-servicio` | 8088 | Comuna |
| 9 | `direccion-servicio` | 8089 | Direccion |
| 10 | `pasajero-servicio` | 8090 | Pasajero |
| 11 | `estado-solicitud-servicio` | 8091 | EstadoSolicitud |
| 12 | `solicitud-servicio` | 8092 | SolicitudTraslado |
| 13 | `estado-viaje-servicio` | 8093 | EstadoViaje |
| 14 | `viaje-servicio` | 8094 | Viaje |
| 15 | `historial-servicio` | 8095 | HistorialEstado |

### Estructura del repositorio

```
appbelmonte-backend/
├── plantilla-servicio/        (base que se copia para cada servicio)
├── rol-servicio/
├── usuario-servicio/
├── ...                        (un directorio por servicio de la tabla)
├── historial-servicio/
├── eureka-server/             (fase 2)
├── gateway/                   (aplicado, fase 12)
├── README.md
└── coleccion-postman.json
```

### Paquetes dentro de cada servicio

Cada servicio tiene una sola entidad. Paquete base: `cl.duoc.appbelmonte.<servicio>`, por ejemplo `cl.duoc.appbelmonte.viaje`.

```
cl.duoc.appbelmonte.<servicio>
├── <Servicio>Application.java
├── controller      endpoints REST
├── service         reglas del negocio
├── repository      acceso a datos
├── model           entidad
├── dto             objetos de entrada y salida
├── client          llamadas a otros servicios (solo si el servicio llama a otros)
├── config          seguridad, JWT y datos de prueba
└── exception       excepciones y manejador global
```

Flujo de una petición: `controller → service → repository → base de datos`. El controller no usa el repository, y el service devuelve DTO, no entidades.

### Dependencias entre servicios

Un servicio llama a otro para (a) comprobar que un id existe al guardar y (b) traer nombres o datos relacionados para armar su respuesta.

| Servicio | Llama a |
|---|---|
| `usuario-servicio` | `rol-servicio` |
| `modelo-vehiculo-servicio` | `marca-servicio` |
| `vehiculo-servicio` | `modelo-vehiculo-servicio` |
| `conductor-servicio` | `vehiculo-servicio` |
| `comuna-servicio` | `region-servicio` |
| `direccion-servicio` | `comuna-servicio` |
| `solicitud-servicio` | `pasajero-servicio`, `direccion-servicio`, `estado-solicitud-servicio` |
| `viaje-servicio` | `solicitud-servicio`, `conductor-servicio`, `estado-viaje-servicio`, `historial-servicio` |
| `historial-servicio` | `estado-viaje-servicio` |
| `rol-servicio`, `marca-servicio`, `region-servicio`, `pasajero-servicio`, `estado-solicitud-servicio`, `estado-viaje-servicio` | Ninguno |

No se permiten llamadas circulares.

### Un solo id por conductor

El id del conductor es el mismo id de su usuario. Así el token (que trae el id del usuario) identifica al conductor en todos los servicios sin consultas extra.

### Ids fijos de los catálogos

Los catálogos de estados y roles se cargan con estos ids, y el código los usa como constantes al escribir datos (así se evita una llamada por cada operación). Los nombres se consultan al servicio solo para armar respuestas.

| Catálogo | Ids |
|---|---|
| Rol | 1 ADMINISTRADOR, 2 SOLICITANTE, 3 CONDUCTOR |
| EstadoSolicitud | 1 PENDIENTE, 2 ASIGNADA, 3 CANCELADA |
| EstadoViaje | 1 ASIGNADO, 2 EN_CURSO, 3 FINALIZADO |

## 5. Modelo de datos (tercera forma normal)

PK = clave primaria. FK = id de otra entidad (sin restricción en base de datos cuando pertenece a otro servicio).

Las entidades cumplen la 1FN (valores atómicos), la 2FN (claves de una sola columna) y la 3FN (ningún atributo depende de otro que no sea la clave). Por eso los datos de otro tema se separan en su propia entidad.

| Entidad | Campos |
|---|---|
| **Rol** | id (PK), nombre (único) |
| **Usuario** | id (PK), nombre, correo (único), contrasena (BCrypt), telefono, activo (por defecto true), rolId (FK Rol) |
| **Marca** | id (PK), nombre (único) |
| **ModeloVehiculo** | id (PK), nombre, marcaId (FK Marca) |
| **Vehiculo** | id (PK), patente (única, ficticia), anio (opcional), modeloId (FK ModeloVehiculo) |
| **Conductor** | id (PK, igual al id del usuario), numeroLicencia (único, ficticio), disponible (por defecto true), vehiculoId (FK Vehiculo) |
| **Region** | id (PK), nombre (único) |
| **Comuna** | id (PK), nombre, regionId (FK Region) |
| **Direccion** | id (PK), calle (calle y número), latitud, longitud (coordenadas de prueba), comunaId (FK Comuna) |
| **Pasajero** | id (PK), nombre, telefono (opcional, ficticio) |
| **EstadoSolicitud** | id (PK), nombre (único) |
| **SolicitudTraslado** | id (PK), fecha, hora, numeroVuelo (opcional), observaciones (opcional), fechaCreacion, solicitanteId (FK Usuario), pasajeroId (FK Pasajero), origenId (FK Direccion), destinoId (FK Direccion), estadoId (FK EstadoSolicitud) |
| **EstadoViaje** | id (PK), nombre (único) |
| **Viaje** | id (PK), fechaAsignacion, horaInicioReal, horaFinReal, solicitudId (FK única SolicitudTraslado), conductorId (FK Conductor), asignadoPorId (FK Usuario), estadoId (FK EstadoViaje) |
| **HistorialEstado** | id (PK), fechaHora, detalle (opcional), viajeId (FK Viaje), estadoAnteriorId (FK EstadoViaje, nulo en el primer registro), estadoNuevoId (FK EstadoViaje), cambiadoPorId (FK Usuario) |

### Relaciones

```
Rol 1─N Usuario
Usuario 1─1 Conductor
Marca 1─N ModeloVehiculo 1─N Vehiculo 1─N Conductor
Region 1─N Comuna 1─N Direccion 1─N SolicitudTraslado (como origen y como destino)
Usuario 1─N SolicitudTraslado (solicitante)
Pasajero 1─N SolicitudTraslado
EstadoSolicitud 1─N SolicitudTraslado
SolicitudTraslado 1─1 Viaje
Conductor 1─N Viaje
Usuario 1─N Viaje (administrador que asigna)
EstadoViaje 1─N Viaje
Viaje 1─N HistorialEstado
```

### Estado visible del traslado

| Momento | Dónde está el estado | Valor |
|---|---|---|
| Solicitud creada | Solicitud | PENDIENTE |
| Administrador asigna conductor | Solicitud y Viaje | ASIGNADA / ASIGNADO |
| Conductor inicia | Viaje | EN_CURSO |
| Conductor finaliza | Viaje | FINALIZADO |
| Cliente anula antes de asignar | Solicitud | CANCELADA |

## 6. Seguridad y comunicación entre servicios

### JWT

- `usuario-servicio` genera el token al iniciar sesión. Contiene: id del usuario (subject), nombre del rol y nombre.
- Todos los servicios usan la **misma clave secreta** (propiedad `jwt.secret`) para validar el token. Ninguno consulta a `usuario-servicio` para validarlo.
- Cada servicio tiene su propia configuración de seguridad y filtro de token, copiados desde la plantilla.
- Sesión sin estado. Sin token o con token inválido: 401. Rol sin permiso: 403.

### Permisos de los catálogos

- Las consultas (GET) de catálogos requieren cualquier usuario autenticado. Excepción: `GET` de `rol-servicio` es público, porque lo usa `usuario-servicio` al registrar e iniciar sesión.
- Crear y modificar catálogos (POST y PUT) solo lo hace el ADMINISTRADOR.

### Llamadas entre servicios

- Se usa Feign con descubrimiento de Eureka y `spring-cloud-starter-loadbalancer`.
- Los clientes declaran `@FeignClient(name = "<servicio>")` y las rutas `/api/...`.
- No se reenvía `Authorization` porque no existe token.
- Los servicios se registran en Eureka (`8761`); el gateway (`8080`) enruta con `lb://`.
- Si el servicio llamado no responde, se devuelve error 503 con mensaje claro.
- Si una consulta a otro servicio indica que el id no existe, se devuelve 400 según el caso (sección 8).

### Crear una solicitud (operación que cruza servicios)

La app envía todo en una sola llamada. `SolicitudRequest` trae los datos del pasajero, del origen y del destino, además de fecha, hora, vuelo y observaciones. `SolicitudService`:

1. Valida los datos recibidos.
2. Crea el pasajero en `pasajero-servicio`.
3. Crea el origen y el destino en `direccion-servicio` (que valida la comuna).
4. Guarda la solicitud con los ids obtenidos, estado PENDIENTE (id 1) y el id del solicitante tomado del token.

Si falla un paso, se devuelve el error y no se guarda la solicitud. Si hubieran quedado registros ya creados, se aceptan como sobrantes en este MVP.

### Asignar conductor (operación que cruza servicios)

`ViajeService.asignarConductor`:

1. Consulta la solicitud en `solicitud-servicio` y verifica que esté PENDIENTE.
2. Consulta el conductor en `conductor-servicio` y verifica que esté disponible.
3. Guarda el viaje con estado ASIGNADO (id 1), dentro de una transacción.
4. Llama a `PUT /api/solicitudes/{id}/asignar` en `solicitud-servicio`. Si falla, la transacción se revierte y el viaje no queda guardado.
5. Llama a `historial-servicio` para registrar el cambio. Si falla, solo se deja un aviso en el registro de la aplicación y la asignación se mantiene.

### Datos de un viaje

El viaje guarda solo sus propios datos. Para responder una consulta, `viaje-servicio` pide los datos de la solicitud (pasajero, direcciones, coordenadas, vuelo, fecha y hora) a `solicitud-servicio`, y el nombre del estado a `estado-viaje-servicio`. Para listas, `solicitud-servicio` ofrece la consulta por varios ids (`GET /api/solicitudes?ids=1,2,3`) y así se evita una llamada por viaje.

## 7. Reglas de negocio

1. Una solicitud siempre se crea en PENDIENTE; el cliente no elige el estado.
2. Una solicitud solo se asigna si está PENDIENTE y el conductor está disponible.
3. Al asignar se aplica el flujo de la sección 6.
4. Una solicitud solo se cancela si está PENDIENTE y solo la puede cancelar quien la creó.
5. El viaje avanza en orden: ASIGNADO → EN_CURSO → FINALIZADO. Cualquier otro salto se rechaza.
6. Solo el conductor del viaje puede iniciarlo o finalizarlo.
7. La reasignación solo se permite mientras el viaje esté ASIGNADO y el nuevo conductor esté disponible; se registra en el historial.
8. Cada cambio de estado del viaje deja un registro en `historial-servicio`.
9. El correo no puede repetirse. La patente no puede repetirse. El número de licencia no puede repetirse.
10. El registro público solo permite los roles SOLICITANTE (id 2) y CONDUCTOR (id 3). El ADMINISTRADOR se crea con los datos de prueba.
11. Un usuario con rol CONDUCTOR crea su perfil en `conductor-servicio` después de registrarse, indicando un vehículo existente; el id del perfil es el id de su usuario, tomado del token.
12. Una dirección debe pertenecer a una comuna existente, y una comuna a una región existente.

## 8. Contrato de la API

Prefijo común: `/api`. Todas las rutas, excepto `/auth/**` y `GET /roles/**`, requieren `Authorization: Bearer <token>`.

### Patrón de catálogo

Lo siguen los servicios de Rol, Region, Comuna, Marca, ModeloVehiculo, EstadoSolicitud y EstadoViaje.

| Método y ruta | Rol | Qué hace |
|---|---|---|
| GET `/api/<recurso>` | Cualquier usuario autenticado | Lista (con filtro cuando se indica) |
| GET `/api/<recurso>/{id}` | Cualquier usuario autenticado | Detalle |
| POST `/api/<recurso>` | ADMINISTRADOR | Crea |
| PUT `/api/<recurso>/{id}` | ADMINISTRADOR | Modifica |

| Servicio | Recurso | Filtro en la lista | Ruta extra |
|---|---|---|---|
| `rol-servicio` | `roles` | — | `GET /api/roles/nombre/{nombre}` (público) |
| `region-servicio` | `regiones` | — | — |
| `comuna-servicio` | `comunas` | `?regionId=` | — |
| `marca-servicio` | `marcas` | — | — |
| `modelo-vehiculo-servicio` | `modelos` | `?marcaId=` | — |
| `estado-solicitud-servicio` | `estados-solicitud` | — | `GET /api/estados-solicitud/nombre/{nombre}` |
| `estado-viaje-servicio` | `estados-viaje` | — | `GET /api/estados-viaje/nombre/{nombre}` |

Las respuestas de `comunas` incluyen el nombre de su región; las de `modelos`, el nombre de su marca.

### usuario-servicio (8082)
| Método y ruta | Rol | Qué hace |
|---|---|---|
| POST `/api/auth/registro` | Público | Crea un usuario (recibe rolId 2 o 3) |
| POST `/api/auth/login` | Público | Devuelve token, id, rol y nombre |
| GET `/api/usuarios/yo` | Cualquier usuario autenticado | Datos del usuario, con el nombre de su rol |

### vehiculo-servicio (8085)
| Método y ruta | Rol | Qué hace |
|---|---|---|
| POST `/api/vehiculos` | ADMINISTRADOR | Crea un vehículo (valida el modelo) |
| PUT `/api/vehiculos/{id}` | ADMINISTRADOR | Modifica |
| GET `/api/vehiculos` | ADMINISTRADOR | Lista |
| GET `/api/vehiculos/{id}` | ADMINISTRADOR, CONDUCTOR | Detalle, con modelo y marca |

### conductor-servicio (8086)
| Método y ruta | Rol | Qué hace |
|---|---|---|
| POST `/api/conductores` | CONDUCTOR | Crea su perfil (recibe licencia y vehiculoId) |
| GET `/api/conductores/yo` | CONDUCTOR | Sus datos, con su vehículo |
| PUT `/api/conductores/yo/disponibilidad` | CONDUCTOR | Cambia su disponibilidad |
| GET `/api/conductores/disponibles` | ADMINISTRADOR | Lista los disponibles |
| GET `/api/conductores/{id}` | ADMINISTRADOR | Detalle de un conductor |

### direccion-servicio (8089)
| Método y ruta | Rol | Qué hace |
|---|---|---|
| POST `/api/direcciones` | SOLICITANTE, ADMINISTRADOR | Crea una dirección (valida la comuna) |
| GET `/api/direcciones/{id}` | Cualquier usuario autenticado | Detalle, con comuna y región |

### pasajero-servicio (8090)
| Método y ruta | Rol | Qué hace |
|---|---|---|
| POST `/api/pasajeros` | SOLICITANTE, ADMINISTRADOR | Crea un pasajero |
| GET `/api/pasajeros/{id}` | Cualquier usuario autenticado | Detalle |

### solicitud-servicio (8092)
| Método y ruta | Rol | Qué hace |
|---|---|---|
| POST `/api/solicitudes` | SOLICITANTE | Crea una solicitud (flujo de la sección 6) |
| GET `/api/solicitudes/mias` | SOLICITANTE | Lista sus solicitudes |
| PUT `/api/solicitudes/{id}/cancelar` | SOLICITANTE | Cancela una solicitud pendiente |
| GET `/api/solicitudes?estado=&fecha=` | ADMINISTRADOR | Lista con filtros opcionales |
| GET `/api/solicitudes?ids=1,2,3&fecha=` | ADMINISTRADOR, CONDUCTOR | Consulta varias por id (uso del viaje) |
| GET `/api/solicitudes/{id}` | ADMINISTRADOR, CONDUCTOR, SOLICITANTE (solo las suyas) | Detalle |
| PUT `/api/solicitudes/{id}/asignar` | ADMINISTRADOR | Pasa de PENDIENTE a ASIGNADA (uso entre servicios) |

### viaje-servicio (8094)
| Método y ruta | Rol | Qué hace |
|---|---|---|
| POST `/api/viajes` | ADMINISTRADOR | Asigna conductor (recibe solicitudId y conductorId) |
| PUT `/api/viajes/{id}/reasignar` | ADMINISTRADOR | Cambia el conductor |
| GET `/api/viajes?fecha=` | ADMINISTRADOR | Lista viajes, con filtro opcional |
| GET `/api/viajes/mis-viajes` | CONDUCTOR | Sus viajes asignados |
| GET `/api/viajes/mis-viajes/hoy` | CONDUCTOR | Sus viajes del día |
| GET `/api/viajes/{id}` | Los tres roles | Detalle (el solicitante y el conductor solo ven los suyos) |
| PUT `/api/viajes/{id}/iniciar` | CONDUCTOR | Pasa a EN_CURSO |
| PUT `/api/viajes/{id}/finalizar` | CONDUCTOR | Pasa a FINALIZADO |

### historial-servicio (8095)
| Método y ruta | Rol | Qué hace |
|---|---|---|
| POST `/api/historial` | ADMINISTRADOR, CONDUCTOR | Registra un cambio (uso entre servicios) |
| GET `/api/historial/viaje/{viajeId}` | ADMINISTRADOR | Lista los cambios de un viaje, con nombres de estado |

### DTO (cada uno en el paquete `dto` de su servicio)

- Catálogos: `<Entidad>Request` y `<Entidad>Response`
- `usuario-servicio`: `RegistroRequest`, `LoginRequest`, `LoginResponse`, `UsuarioResponse`
- `vehiculo-servicio`: `VehiculoRequest`, `VehiculoResponse`
- `conductor-servicio`: `ConductorRequest`, `DisponibilidadRequest`, `ConductorResponse`
- `direccion-servicio`: `DireccionRequest`, `DireccionResponse`
- `pasajero-servicio`: `PasajeroRequest`, `PasajeroResponse`
- `solicitud-servicio`: `SolicitudRequest` (con los datos del pasajero y de las dos direcciones), `SolicitudResponse`
- `viaje-servicio`: `AsignacionRequest`, `ReasignacionRequest`, `ViajeResponse`
- `historial-servicio`: `HistorialRequest`, `HistorialResponse`
- Todos: `ErrorResponse`

`SolicitudResponse` y `ViajeResponse` deben mostrar nombres legibles (estado, calle, comuna, región, pasajero) y las coordenadas de origen y destino, porque la app las usa para abrir Google Maps o Waze.

### Formato de error (igual en todos los servicios)

```json
{
  "estado": 400,
  "mensaje": "Descripción del error en español",
  "fecha": "2026-11-09T18:00:00"
}
```

| Situación | Código HTTP |
|---|---|
| Datos inválidos o id relacionado que no existe | 400 |
| Sin token o token inválido | 401 |
| Rol sin permiso | 403 |
| Recurso no existe | 404 |
| Regla de negocio incumplida (estado incorrecto, dato repetido) | 409 |
| Otro servicio no disponible | 503 |

## 9. Fases de ejecución

Se construye en orden de dependencia: primero los servicios que no llaman a nadie.

### Fase 0. Repositorio y plantilla
- Verificar la estructura de carpetas de la sección 4, si no esta creada, desarrolla la estructura.
- Analizar el microservicio **usuario-servicio** y tomarlo como una `plantilla-servicio` con: `pom.xml` según la sección 3, `ErrorResponse`, manejador global de errores, configuración de MySQL/Flyway, Eureka y `application.properties` con datasource y tabla de historial Flyway.
- La plantilla tiene la construcción completa del microservicio, la usaras de ejemplo para poder realizar la construcción de los demás. 
- Para cada servicio: copiar la plantilla, renombrar el directorio, el paquete, la clase principal y el puerto.
- **Listo cuando:** la plantilla arranca sin errores y se puede copiar a un servicio nuevo en pocos pasos.

### Fase 1. rol-servicio
- Patrón de catálogo, con `GET` público y las rutas extra de la sección 8.
- Cargar los 3 roles con los ids fijos.
- **Listo cuando:** `GET /api/roles` responde sin token con los 3 roles.

### Fase 2. usuario-servicio
- Entidad, registro y login sin token (con el nombre del rol obtenido de `rol-servicio`).
- Persistencia en MySQL y seeds mediante Flyway.
- Cargar los usuarios de prueba (sección 10).
- **Listo cuando:** el login con cada usuario de prueba devuelve id, rolId, rolNombre y nombre.

### Fase 3. region-servicio y comuna-servicio
- Patrón de catálogo; `comuna-servicio` valida la región y la incluye en sus respuestas.
- **Listo cuando:** `GET /api/comunas?regionId=1` devuelve las comunas con el nombre de su región.

### Fase 4. direccion-servicio y pasajero-servicio
- Rutas de la sección 8. `direccion-servicio` valida la comuna.
- **Listo cuando:** se crea una dirección con `comunaId` y se consulta con su comuna y región.

### Fase 5. marca-servicio, modelo-vehiculo-servicio y vehiculo-servicio
- Patrón de catálogo para marca y modelo; `vehiculo-servicio` con sus propias rutas.
- **Listo cuando:** un vehículo se consulta con su modelo y su marca.

### Fase 6. conductor-servicio
- Rutas de la sección 8; valida el vehículo en `vehiculo-servicio`.
- **Listo cuando:** un conductor crea y consulta su perfil con los datos de su vehículo, y el administrador lista los disponibles.

### Fase 7. estado-solicitud-servicio y estado-viaje-servicio
- Patrón de catálogo con la ruta extra por nombre y los ids fijos.
- **Listo cuando:** ambos devuelven sus 3 estados con los ids de la sección 4.

### Fase 8. solicitud-servicio
- Crear solicitud (flujo de la sección 6), cancelar, consultas por estado, fecha, ids y las propias, y la ruta `asignar`.
- **Listo cuando:** el solicitante crea y cancela solicitudes y el administrador las filtra.

### Fase 9. historial-servicio
- Dos rutas de la sección 8; completa los nombres de estado consultando `estado-viaje-servicio`.
- **Listo cuando:** se registra y consulta el historial de un viaje.

### Fase 10. viaje-servicio
- Paquete `client` con las llamadas a solicitud, conductor, estado-viaje e historial.
- Asignación (sección 6), reasignación, `iniciar`, `finalizar` y las consultas del conductor.
- **Listo cuando:** se completa el flujo de la sección 11 con todos los servicios encendidos.

### Fase 11. Errores, pruebas y documentación
- Comprobar los casos de error de la sección 11, incluido un servicio apagado (503).
- Pruebas unitarias de las reglas de `SolicitudService` y `ViajeService` (con los clientes simulados) y una prueba de login.
- Colección de Postman con el flujo completo.
- `README.md` en la raíz con: servicios y puertos, orden para iniciarlos (el de las fases), usuarios de prueba (ficticios) y la lista de rutas.
- **Listo cuando:** las pruebas pasan y el flujo completo funciona desde Postman.

### Fase 12. Puerta de entrada única (aplicada)
- El módulo `gateway` escucha en el puerto 8080 y dirige cada ruta `/api/...` al servicio correspondiente con `lb://` y Eureka.
- Usa `spring-cloud-starter-gateway-server-webflux`, compatible con Spring Boot 4.1.2.
- La app Android usa una sola dirección base: `http://localhost:8080`.
- **Listo:** todas las rutas de la sección 8 responden por el puerto 8080.

## 10. Datos de prueba ficticios

Cada servicio carga sus datos con Flyway (`V2__datos_prueba_<tabla>.sql`), con ids explícitos. Todos los nombres, correos, teléfonos y patentes son inventados. Las contraseñas de prueba se guardan en texto plano (`clave123`) porque no hay autenticación real.

| Servicio | Datos |
|---|---|
| `rol-servicio` | Los 3 roles (ids 1 a 3) |
| `usuario-servicio` | Id 1 administrador; ids 2 y 3 solicitantes; ids 4, 5 y 6 conductores |
| `region-servicio` | Región Metropolitana (id 1) y Región de Valparaíso (id 2) |
| `comuna-servicio` | Santiago, Providencia, Las Condes, Maipú y Pudahuel (región 1); Viña del Mar (región 2) |
| `marca-servicio` | Toyota, Hyundai, Kia |
| `modelo-vehiculo-servicio` | Un modelo por marca |
| `vehiculo-servicio` | 3 vehículos con patentes inventadas |
| `conductor-servicio` | Perfiles con ids 4, 5 y 6, cada uno con un vehículo |
| `estado-solicitud-servicio` | Los 3 estados (ids 1 a 3) |
| `estado-viaje-servicio` | Los 3 estados (ids 1 a 3) |
| `pasajero-servicio` | 3 pasajeros inventados |
| `direccion-servicio` | 6 direcciones con coordenadas de prueba de Santiago (incluye una de aeropuerto en Pudahuel) |
| `solicitud-servicio` | 3 solicitudes de los solicitantes 2 y 3, en estado PENDIENTE, que usan los pasajeros y direcciones anteriores |
| `viaje-servicio` | Sin datos iniciales |
| `historial-servicio` | Sin datos iniciales |

## 11. Flujo completo para validar el backend

Con todos los servicios encendidos:

1. Solicitante inicia sesión, consulta regiones y comunas, y crea una solicitud → PENDIENTE.
2. Administrador inicia sesión, lista las pendientes y los conductores disponibles.
3. Administrador asigna un conductor → solicitud ASIGNADA, viaje ASIGNADO.
4. Conductor inicia sesión y ve el viaje en `mis-viajes/hoy`, con sus coordenadas.
5. Conductor inicia el viaje → EN_CURSO.
6. Conductor finaliza el viaje → FINALIZADO.
7. Administrador consulta el historial y ve los cambios de estado con sus nombres.

Casos de error que deben fallar:
- Iniciar un viaje por un conductor que no es el suyo (403).
- Finalizar un viaje que no está EN_CURSO (409).
- Cancelar una solicitud ya asignada (409).
- Asignar una solicitud que no está PENDIENTE (409).
- Crear una dirección con una comuna que no existe (400).
- Crear un vehículo con una patente repetida (409).
- Asignar con `solicitud-servicio` apagado (503, sin viaje guardado).
- Cualquier ruta sin token (401) o con el rol equivocado (403).

## 12. Entregable de esta etapa

- 15 servicios funcionando de forma independiente y comunicados entre sí.
- Colección de Postman.
- `README.md` con cómo ejecutar y los usuarios de prueba.

## 13. Fuera de alcance por ahora

- App Android (se hará después de validar estos servicios).
- Tiempo real con WebSockets: bastan las consultas REST al abrir o refrescar la pantalla.
- Integración con mapas en el backend: la navegación se resuelve en la app con Intents.
- Mensajería y Config Server. Eureka, Docker y MySQL ya fueron incorporados en la Fase 2.
