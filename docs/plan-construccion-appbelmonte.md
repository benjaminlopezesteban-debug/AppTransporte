# Plan de construcción de AppBelmonte por patrones

Guía de ejecución del backend de AppBelmonte. Complementa a
`plan_backend_appbelmonte.md` y evita repetir un plan extenso por cada fase:
agrupa los 15 servicios en patrones reutilizables y deja planes detallados solo
para las fases con lógica propia.

Los patrones son documentación y copia desde `plantilla-servicio`. No se crea un
módulo Maven compartido ni código común entre servicios: cada servicio mantiene
su propio proyecto, puerto y base de datos.

---

## 1. Convenciones comunes

### Estructura

```
cl.duoc.appbelmonte.<paquete>
├── <Servicio>Application.java
├── controller
├── service
├── repository
├── model
├── dto
├── client        (solo si llama a otros servicios)
├── config        (seguridad, JWT, RestClient y datos de prueba)
└── exception
```

Flujo: `controller → service → repository → base de datos`. El controller no usa
el repositorio y el service devuelve DTO, nunca entidades.

### Entidades

- `id` es `Long` con `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- Las referencias a otras entidades se guardan como `Long` (FK lógica), sin
  relaciones JPA ni restricciones de clave foránea.
- Los nombres de tabla son plurales en minúscula (`roles`, `usuarios`, ...).

### DTO

- Se usan `record`.
- Nombres: `<Entidad>Request` y `<Entidad>Response`.
- Todos los servicios incluyen `ErrorResponse`.
- Las respuestas que lo requieren agregan nombres legibles de entidades
  relacionadas.

### Respuestas y errores

| Situación | Código | Excepción |
|---|---|---|
| Datos inválidos de validación | 400 | `MethodArgumentNotValidException` |
| FK inexistente | 400 | `DatosInvalidosException` |
| Sin token o token inválido | 401 | `RestAuthenticationEntryPoint` |
| Rol sin permiso | 403 | `AccessDeniedException` |
| Recurso no existe | 404 | `RecursoNoEncontradoException` |
| Regla de negocio o duplicado | 409 | `ReglaNegocioException` |
| Otro servicio no disponible | 503 | `ServicioNoDisponibleException` |

Formato:

```json
{
  "estado": 400,
  "mensaje": "Descripción del error en español",
  "fecha": "2026-11-09T18:00:00"
}
```

### Seguridad

- `SecurityConfig` sin estado, con `@EnableMethodSecurity`.
- `anyRequest().authenticated()`; los permisos finos se declaran con
  `@PreAuthorize`.
- El filtro JWT arma la autenticación con el id del usuario como principal y el
  rol como autoridad `ROLE_<ROL>`.
- Todos los servicios comparten `jwt.secret`.
- `usuario-servicio` genera el token; los demás solo lo validan.

### Clientes REST

- Se usa `RestClient.Builder` de `RestClientConfig`, que reenvía el encabezado
  `Authorization` de la petición actual.
- Un 404 del servicio llamado se traduce a `DatosInvalidosException` (400).
- Un fallo de conexión o un 5xx se traduce a `ServicioNoDisponibleException`
  (503).
- Las URL se configuran en `application.properties` con
  `servicios.<nombre>.url`.

### Datos de prueba

- `CommandLineRunner` que solo carga si `repository.count() == 0`.
- Se guardan en orden para obtener los ids fijos esperados.
- Todos los datos son ficticios.
- Contraseñas de prueba cifradas con BCrypt.

### Pruebas y Postman

- Test de contexto en cada servicio.
- Pruebas unitarias del service para reglas y errores principales.
- Una carpeta de Postman por servicio con requests de éxito y error.

---

## 2. Patrones

### P1. Catálogo puro

**Servicios:** `rol`, `region`, `marca`, `estado-solicitud`, `estado-viaje`.

**Entidad**

| Campo | Tipo | Restricción |
|---|---|---|
| id | Long | PK |
| nombre | String | único, obligatorio |

**Archivos**

- `model/<Entidad>.java`
- `repository/<Entidad>Repository.java` con `findByNombre` y `existsByNombre`
- `dto/<Entidad>Request.java` (`nombre` con `@NotBlank`)
- `dto/<Entidad>Response.java` (`id`, `nombre`)
- `service/<Entidad>Service.java`
- `controller/<Entidad>Controller.java`
- `config/DatosPruebaConfig.java`

**Rutas**

| Método y ruta | Rol |
|---|---|
| GET `/api/<recurso>` | Autenticado |
| GET `/api/<recurso>/{id}` | Autenticado |
| POST `/api/<recurso>` | ADMINISTRADOR |
| PUT `/api/<recurso>/{id}` | ADMINISTRADOR |

**Rutas extra**

| Servicio | Ruta extra | Acceso |
|---|---|---|
| rol | GET `/api/roles/nombre/{nombre}` | Público |
| estado-solicitud | GET `/api/estados-solicitud/nombre/{nombre}` | Autenticado |
| estado-viaje | GET `/api/estados-viaje/nombre/{nombre}` | Autenticado |

En `rol-servicio`, todos los `GET /api/roles/**` son públicos. En el resto de
catálogos, todos los GET exigen token.

**Reglas**

- Crear o actualizar con un nombre ya existente → 409.
- Obtener un id inexistente → 404.
- `PUT` mantiene el mismo id; el nombre puede cambiarse si no está duplicado.

**Datos de prueba**

| Servicio | Ids y nombres |
|---|---|
| rol | 1 ADMINISTRADOR, 2 SOLICITANTE, 3 CONDUCTOR |
| region | 1 Región Metropolitana, 2 Región de Valparaíso |
| marca | Toyota, Hyundai, Kia (ids 1, 2, 3) |
| estado-solicitud | 1 PENDIENTE, 2 ASIGNADA, 3 CANCELADA |
| estado-viaje | 1 ASIGNADO, 2 EN_CURSO, 3 FINALIZADO |

**DoD**

- `GET /api/roles` responde sin token con los 3 roles.
- Cada catálogo lista, crea y modifica con el rol correcto.
- Duplicados y no encontrados devuelven 409 y 404.

---

### P2. Catálogo con filtro y nombre relacionado

**Servicios:** `comuna`, `modelo-vehiculo`.

| Servicio | Campos | Filtro | Respuesta enriquecida | Depende de |
|---|---|---|---|---|
| comuna | id, nombre, regionId | `?regionId=` | `regionNombre` | region |
| modelo-vehiculo | id, nombre, marcaId | `?marcaId=` | `marcaNombre` | marca |

**Archivos adicionales**

- `client/RegionClient.java` o `client/MarcaClient.java`
- DTOs de respuesta con el nombre relacionado.

**Rutas**

Igual que P1, más el filtro opcional en la lista. `GET /api/comunas?regionId=1`
y `GET /api/modelos?marcaId=1`.

**Reglas**

- Validar que la FK exista antes de crear o actualizar. Si no existe → 400.
- Si el servicio de la FK no responde → 503.
- La respuesta de la lista y del detalle incluye el nombre de la entidad
  relacionada.

**Datos de prueba**

- Comunas: Santiago, Providencia, Las Condes, Maipú y Pudahuel (región 1); Viña
  del Mar (región 2).
- Modelos: un modelo por marca.

**DoD**

- `GET /api/comunas?regionId=1` devuelve las comunas con el nombre de su región.
- `GET /api/modelos?marcaId=1` devuelve los modelos con el nombre de su marca.

---

### P3. Recurso con validación de FK

**Servicios:** `vehiculo`, `direccion`, `pasajero`.

| Servicio | Campos | Rutas | Depende de |
|---|---|---|---|
| vehiculo | id, patente única, anio opcional, modeloId | POST/PUT/list ADMIN; GET id ADMIN y CONDUCTOR | modelo |
| direccion | id, calle, latitud, longitud, comunaId | POST SOLICITANTE y ADMIN; GET id autenticado | comuna |
| pasajero | id, nombre, telefono opcional | POST SOLICITANTE y ADMIN; GET id autenticado | — |

**Respuestas enriquecidas**

- Vehículo: `modeloNombre` y `marcaNombre`.
- Dirección: `comunaNombre` y `regionNombre`.
- Pasajero: solo sus datos.

**Reglas**

- Patente duplicada → 409.
- FK inexistente → 400.
- Id inexistente → 404.
- `anio` y `telefono` son opcionales.

**Datos de prueba**

- 3 vehículos con patentes inventadas.
- 6 direcciones con coordenadas de prueba de Santiago, incluida una de
  aeropuerto en Pudahuel.
- 3 pasajeros inventados.

**DoD**

- Un vehículo se consulta con su modelo y su marca.
- Una dirección se crea con token de solicitante y se consulta con comuna y
  región.
- Un pasajero se crea y consulta.

---

### P4. Recurso con identidad desde el token

**Servicio:** `conductor`.

**Entidad**

| Campo | Tipo | Restricción |
|---|---|---|
| id | Long | PK, igual al id del usuario |
| numeroLicencia | String | único, obligatorio |
| disponible | Boolean | por defecto true |
| vehiculoId | Long | FK lógica a Vehiculo |

**Rutas**

| Método y ruta | Rol | Qué hace |
|---|---|---|
| POST `/api/conductores` | CONDUCTOR | Crea su perfil con licencia y vehiculoId |
| GET `/api/conductores/yo` | CONDUCTOR | Sus datos con su vehículo |
| PUT `/api/conductores/yo/disponibilidad` | CONDUCTOR | Cambia su disponibilidad |
| GET `/api/conductores/disponibles` | ADMINISTRADOR | Lista los disponibles |
| GET `/api/conductores/{id}` | ADMINISTRADOR | Detalle de un conductor |

**Reglas**

- El id del perfil se toma del token, no del cuerpo.
- Un conductor no puede crear dos perfiles → 409.
- El vehículo debe existir → 400.
- La respuesta incluye los datos del vehículo.

**Datos de prueba**

- Perfiles con ids 4, 5 y 6, cada uno con un vehículo.

**DoD**

- Un conductor crea y consulta su perfil con los datos de su vehículo.
- El administrador lista los disponibles.

---

### P5. Consulta enriquecida con cliente REST

**Servicio:** `historial`.

**Entidad**

| Campo | Tipo | Restricción |
|---|---|---|
| id | Long | PK |
| fechaHora | LocalDateTime | obligatorio |
| detalle | String | opcional |
| viajeId | Long | FK lógica |
| estadoAnteriorId | Long | FK lógica, nulo en el primer registro |
| estadoNuevoId | Long | FK lógica |
| cambiadoPorId | Long | FK lógica a Usuario |

**Rutas**

| Método y ruta | Rol | Qué hace |
|---|---|---|
| POST `/api/historial` | ADMINISTRADOR, CONDUCTOR | Registra un cambio (uso entre servicios) |
| GET `/api/historial/viaje/{viajeId}` | ADMINISTRADOR | Lista los cambios con nombres de estado |

**Reglas**

- `fechaHora` se asigna al registrar si no viene en la petición.
- La consulta agrega `estadoAnteriorNombre` y `estadoNuevoNombre` consultando
  `estado-viaje-servicio`.
- Sin datos iniciales.

**DoD**

- Se registra y consulta el historial de un viaje con los nombres de estado.

---

### P6. Orquestación entre servicios

**Servicios:** `solicitud`, `viaje`.

Se detallan en las secciones 4.2 y 4.3.

---

### P7. Autenticación y JWT

**Servicio:** `usuario`.

Se detalla en la sección 4.1.

---

## 3. Fichas de los servicios

Cada ficha usa este formato:

```
Servicio:
Patrón:
Entidad y campos:
Endpoints:
Dependencias:
Validaciones y reglas:
Respuesta enriquecida:
Datos de prueba:
Casos de error:
DoD:
```

### 3.1 rol-servicio

- **Patrón:** P1.
- **Entidad:** id, nombre.
- **Endpoints:** GET lista, GET detalle, GET por nombre, POST, PUT.
- **Dependencias:** ninguna.
- **Reglas:** nombre único; GET públicos.
- **Datos:** 3 roles con ids fijos.
- **Errores:** 404 id inexistente; 409 nombre duplicado; 401/403 según corresponda.
- **DoD:** GET `/api/roles` sin token devuelve los 3 roles.

### 3.2 usuario-servicio

- **Patrón:** P7.
- **Entidad:** id, nombre, correo único, contrasena BCrypt, telefono, activo, rolId.
- **Endpoints:** POST `/api/auth/registro`, POST `/api/auth/login`, GET `/api/usuarios/yo`.
- **Dependencias:** rol.
- **Reglas:** registro solo con rolId 2 o 3; correo único; login valida activo y contraseña.
- **Datos:** ids 1–6 con roles 1, 2 y 3.
- **Errores:** 401 credenciales inválidas; 409 correo o rol no permitido; 400 rol inexistente.
- **DoD:** login de cada usuario de prueba devuelve token con el rol correcto.

### 3.3 marca-servicio

- **Patrón:** P1.
- **Entidad:** id, nombre.
- **Endpoints:** GET lista, GET detalle, POST, PUT.
- **Datos:** Toyota, Hyundai, Kia.
- **DoD:** catálogo operativo con permisos de P1.

### 3.4 modelo-vehiculo-servicio

- **Patrón:** P2.
- **Entidad:** id, nombre, marcaId.
- **Endpoints:** GET lista con `?marcaId=`, GET detalle, POST, PUT.
- **Dependencias:** marca.
- **Respuesta:** incluye `marcaNombre`.
- **Datos:** un modelo por marca.
- **DoD:** modelos con nombre de marca.

### 3.5 vehiculo-servicio

- **Patrón:** P3.
- **Entidad:** id, patente única, anio opcional, modeloId.
- **Endpoints:** POST/PUT/list ADMIN; GET detalle ADMIN y CONDUCTOR.
- **Dependencias:** modelo.
- **Respuesta:** modelo y marca.
- **Datos:** 3 vehículos.
- **DoD:** vehículo consultado con modelo y marca.

### 3.6 conductor-servicio

- **Patrón:** P4.
- **Entidad:** id, numeroLicencia único, disponible, vehiculoId.
- **Endpoints:** POST, GET yo, PUT yo/disponibilidad, GET disponibles, GET detalle.
- **Dependencias:** vehiculo.
- **Datos:** ids 4, 5 y 6.
- **DoD:** perfil creado y consultado con vehículo; admin lista disponibles.

### 3.7 region-servicio

- **Patrón:** P1.
- **Entidad:** id, nombre.
- **Datos:** Región Metropolitana y Región de Valparaíso.
- **DoD:** catálogo operativo con permisos de P1.

### 3.8 comuna-servicio

- **Patrón:** P2.
- **Entidad:** id, nombre, regionId.
- **Endpoints:** GET lista con `?regionId=`, GET detalle, POST, PUT.
- **Dependencias:** region.
- **Respuesta:** incluye `regionNombre`.
- **Datos:** 5 comunas de la región 1 y 1 de la región 2.
- **DoD:** comunas con nombre de región.

### 3.9 direccion-servicio

- **Patrón:** P3.
- **Entidad:** id, calle, latitud, longitud, comunaId.
- **Endpoints:** POST SOLICITANTE/ADMIN; GET detalle autenticado.
- **Dependencias:** comuna.
- **Respuesta:** comuna y región.
- **Datos:** 6 direcciones con coordenadas de prueba.
- **DoD:** dirección creada y consultada con comuna y región.

### 3.10 pasajero-servicio

- **Patrón:** P3.
- **Entidad:** id, nombre, telefono opcional.
- **Endpoints:** POST SOLICITANTE/ADMIN; GET detalle autenticado.
- **Datos:** 3 pasajeros.
- **DoD:** pasajero creado y consultado.

### 3.11 estado-solicitud-servicio

- **Patrón:** P1.
- **Entidad:** id, nombre.
- **Endpoints:** GET lista, GET detalle, GET por nombre, POST, PUT.
- **Datos:** 1 PENDIENTE, 2 ASIGNADA, 3 CANCELADA.
- **DoD:** 3 estados con ids fijos.

### 3.12 solicitud-servicio

- **Patrón:** P6.
- **Entidad:** id, fecha, hora, numeroVuelo, observaciones, fechaCreacion, solicitanteId, pasajeroId, origenId, destinoId, estadoId.
- **Endpoints:** POST, GET mias, PUT cancelar, GET filtros, GET por ids, GET detalle, PUT asignar.
- **Dependencias:** pasajero, direccion, estado-solicitud.
- **Datos:** 3 solicitudes PENDIENTE.
- **DoD:** solicitante crea y cancela; admin filtra y asigna.

### 3.13 estado-viaje-servicio

- **Patrón:** P1.
- **Entidad:** id, nombre.
- **Endpoints:** GET lista, GET detalle, GET por nombre, POST, PUT.
- **Datos:** 1 ASIGNADO, 2 EN_CURSO, 3 FINALIZADO.
- **DoD:** 3 estados con ids fijos.

### 3.14 viaje-servicio

- **Patrón:** P6.
- **Entidad:** id, fechaAsignacion, horaInicioReal, horaFinReal, solicitudId único, conductorId, asignadoPorId, estadoId.
- **Endpoints:** POST asignar, PUT reasignar, GET lista por fecha, GET mis-viajes, GET mis-viajes/hoy, GET detalle, PUT iniciar, PUT finalizar.
- **Dependencias:** solicitud, conductor, estado-viaje, historial.
- **Datos:** sin datos iniciales.
- **DoD:** flujo completo de la sección 11 del plan general.

### 3.15 historial-servicio

- **Patrón:** P5.
- **Entidad:** id, fechaHora, detalle, viajeId, estadoAnteriorId, estadoNuevoId, cambiadoPorId.
- **Endpoints:** POST, GET por viaje.
- **Dependencias:** estado-viaje.
- **Datos:** sin datos iniciales.
- **DoD:** historial registrado y consultado con nombres de estado.

---

## 4. Planes detallados

### 4.1 Fase 2 — usuario-servicio

**Objetivo:** registro, login y emisión del JWT.

**Cambios respecto de la plantilla**

- Reemplazar el `enum Rol` por `rolId` (`Long`).
- `RegistroRequest`: nombre, correo, contrasena, telefono, rolId.
- `LoginRequest`: correo, contrasena.
- `LoginResponse`: token, id, rolId, rolNombre, nombre.
- `UsuarioResponse`: id, nombre, correo, telefono, activo, rolId, rolNombre.
- `RolClient`: consulta `GET /api/roles/{id}` y `GET /api/roles/nombre/{nombre}`.
- `JwtEmisor`: `subject` = id, `rol` = nombre del rol, `nombre` = nombre.
- `SecurityConfig`: `/api/auth/**` público; `/api/usuarios/**` autenticado.

**Flujo de registro**

1. Validar datos.
2. Rechazar rolId distinto de 2 y 3 → 409.
3. Verificar que el rol exista en `rol-servicio` → 400 si no existe.
4. Verificar correo único → 409.
5. Guardar contraseña con BCrypt y activo en true.

**Flujo de login**

1. Buscar por correo.
2. Validar activo y contraseña → 401 si falla.
3. Obtener el nombre del rol desde `rol-servicio`.
4. Generar token y devolver datos.

**Datos de prueba**

| Id | Nombre | Correo | Rol |
|---|---|---|---|
| 1 | Admin Belmonte | admin@appbelmonte.cl | ADMINISTRADOR |
| 2 | Carla Soto | carla.soto@ficticio.cl | SOLICITANTE |
| 3 | Diego Rojas | diego.rojas@ficticio.cl | SOLICITANTE |
| 4 | Pedro Vera | pedro.vera@ficticio.cl | CONDUCTOR |
| 5 | María Lagos | maria.lagos@ficticio.cl | CONDUCTOR |
| 6 | Luis Fuentes | luis.fuentes@ficticio.cl | CONDUCTOR |

Contraseña de prueba: `clave123`.

**Pruebas**

- Login correcto e incorrecto.
- Registro con rol 2 y 3.
- Registro con rol 1 → 409.
- Correo duplicado → 409.
- Rol inexistente → 400.

**DoD**

- El login de cada usuario devuelve un token con el rol correcto.
- `GET /api/usuarios/yo` devuelve el usuario autenticado con el nombre de su rol.

### 4.2 Fase 8 — solicitud-servicio

**Objetivo:** ciclo de la solicitud desde su creación hasta la asignación.

**Entidad**

`id`, `fecha`, `hora`, `numeroVuelo`, `observaciones`, `fechaCreacion`,
`solicitanteId`, `pasajeroId`, `origenId`, `destinoId`, `estadoId`.

**DTOs**

- `SolicitudRequest`: datos del pasajero, origen, destino, fecha, hora,
  numeroVuelo y observaciones.
- `SolicitudResponse`: id, fecha, hora, numeroVuelo, observaciones,
  fechaCreacion, estadoId, estadoNombre, solicitanteId, pasajero, origen y
  destino con calle, comuna, región y coordenadas.

**Clientes**

- `PasajeroClient`: POST `/api/pasajeros`, GET `/api/pasajeros/{id}`.
- `DireccionClient`: POST `/api/direcciones`, GET `/api/direcciones/{id}`.
- `EstadoSolicitudClient`: GET `/api/estados-solicitud/{id}`.

**Crear solicitud**

1. Validar el cuerpo.
2. Crear el pasajero.
3. Crear el origen.
4. Crear el destino.
5. Guardar la solicitud con `estadoId = 1`, `fechaCreacion = ahora` y
   `solicitanteId` tomado del token.
6. Si falla un paso, devolver el error y no guardar la solicitud. Los registros
   ya creados se aceptan como sobrantes del MVP.

**Cancelar**

- Solo el solicitante dueño.
- Solo si está PENDIENTE.
- Cambia `estadoId` a 3.

**Asignar**

- Solo ADMINISTRADOR.
- Solo si está PENDIENTE.
- Cambia `estadoId` a 2.

**Consultas**

| Ruta | Rol | Filtros |
|---|---|---|
| GET `/api/solicitudes/mias` | SOLICITANTE | — |
| GET `/api/solicitudes?estado=&fecha=` | ADMINISTRADOR | opcionales |
| GET `/api/solicitudes?ids=1,2,3&fecha=` | ADMINISTRADOR, CONDUCTOR | ids obligatorios |
| GET `/api/solicitudes/{id}` | ADMINISTRADOR, CONDUCTOR, SOLICITANTE dueño | — |

**Datos de prueba**

- 3 solicitudes PENDIENTE de los solicitantes 2 y 3, usando los pasajeros y
  direcciones cargados.

**Pruebas**

- Creación con clientes simulados.
- Cancelación permitida y rechazada.
- Asignación de una solicitud no pendiente → 409.
- Consulta de solicitudes de otro solicitante → 403.

**DoD**

- El solicitante crea y cancela solicitudes.
- El administrador filtra y asigna.

### 4.3 Fase 10 — viaje-servicio

**Objetivo:** asignación, ejecución y finalización del viaje.

**Entidad**

`id`, `fechaAsignacion`, `horaInicioReal`, `horaFinReal`, `solicitudId` único,
`conductorId`, `asignadoPorId`, `estadoId`.

**DTOs**

- `AsignacionRequest`: solicitudId, conductorId.
- `ReasignacionRequest`: conductorId.
- `ViajeResponse`: datos del viaje, datos de la solicitud (pasajero,
  direcciones, coordenadas, vuelo, fecha y hora) y nombre del estado.

**Clientes**

- `SolicitudClient`: GET por id, GET por ids, PUT asignar.
- `ConductorClient`: GET por id.
- `EstadoViajeClient`: GET por id.
- `HistorialClient`: POST.

**Asignar conductor**

1. Consultar la solicitud y verificar PENDIENTE.
2. Consultar el conductor y verificar disponible.
3. Guardar el viaje con `estadoId = 1` dentro de una transacción.
4. Llamar a `PUT /api/solicitudes/{id}/asignar`. Si falla, revertir.
5. Llamar a `historial-servicio`. Si falla, solo registrar un aviso y mantener
   la asignación.

**Reasignar**

- Solo si el viaje está ASIGNADO.
- El nuevo conductor debe estar disponible.
- Registrar el cambio en el historial.

**Iniciar**

- Solo el conductor del viaje.
- Solo si está ASIGNADO.
- Cambia a EN_CURSO y registra historial.

**Finalizar**

- Solo el conductor del viaje.
- Solo si está EN_CURSO.
- Cambia a FINALIZADO y registra historial.

**Consultas**

| Ruta | Rol | Filtros |
|---|---|---|
| GET `/api/viajes?fecha=` | ADMINISTRADOR | opcional |
| GET `/api/viajes/mis-viajes` | CONDUCTOR | — |
| GET `/api/viajes/mis-viajes/hoy` | CONDUCTOR | — |
| GET `/api/viajes/{id}` | Los tres roles, con restricción de dueño | — |

**Pruebas**

- Asignación correcta.
- Asignación con solicitud no pendiente → 409.
- Asignación con `solicitud-servicio` apagado → 503 y sin viaje guardado.
- Iniciar un viaje ajeno → 403.
- Finalizar un viaje que no está EN_CURSO → 409.

**DoD**

- Se completa el flujo de la sección 11 del plan general con todos los servicios
  encendidos.

### 4.4 Fase 11 — errores, pruebas y documentación

- Verificar todos los casos de error de la sección 11 del plan general.
- Probar un servicio apagado y confirmar 503.
- Pruebas unitarias de `SolicitudService` y `ViajeService` con clientes
  simulados.
- Prueba de login.
- Colección Postman con el flujo completo.
- README con servicios, puertos, orden de arranque, usuarios de prueba y rutas.

**DoD**

- Las pruebas pasan y el flujo completo funciona desde Postman.

### 4.5 Fase 12 — gateway

- Verificar que la librería de gateway sea compatible con Spring Boot 4.1.2.
- Si es compatible, crear `gateway` en el puerto 8080 con rutas `/api/...`.
- Si no, documentar que la app usará una URL base por servicio.

**DoD**

- Todas las rutas de la sección 8 del plan general responden por el puerto 8080.

---

## 5. Orden de ejecución por lotes

| Lote | Servicios | Patrón | Depende de |
|---|---|---|---|
| A | rol, region, marca, estado-solicitud, estado-viaje | P1 | — |
| B | usuario | P7 | rol |
| C | comuna, modelo-vehiculo | P2 | region, marca |
| D | vehiculo, direccion, pasajero | P3 | modelo, comuna |
| E | conductor | P4 | vehiculo |
| F | historial | P5 | estado-viaje |
| G | solicitud | P6 | pasajero, direccion, estado-solicitud |
| H | viaje | P6 | solicitud, conductor, estado-viaje, historial |
| I | pruebas, Postman, README | Fase 11 | Todos |
| J | gateway | Fase 12 | Todos |

Dentro de un lote, los servicios se pueden construir en paralelo.

---

## 6. DoD común

Un servicio se considera terminado cuando:

1. Compila con `./mvnw -q -DskipTests package`.
2. Arranca en su puerto con H2.
3. Carga sus datos de prueba con los ids esperados.
4. Responde las rutas con el token y rol correctos.
5. Devuelve 400, 401, 403, 404, 409 o 503 según corresponda.
6. Tiene al menos una prueba de sus reglas principales.
7. Está incluido en la colección Postman.

---

## 7. Checklist de validación por lote

- [ ] El servicio compila y arranca.
- [ ] Los datos de prueba tienen los ids esperados.
- [ ] Las rutas públicas y protegidas responden como indica el contrato.
- [ ] Los errores usan el formato `ErrorResponse`.
- [ ] Las llamadas entre servicios reenvían el token.
- [ ] Un servicio apagado produce 503.
- [ ] No hay relaciones JPA ni claves foráneas entre servicios.
- [ ] No se agregaron librerías fuera de las permitidas.

---

## 8. Notas

- La plantilla conserva el ejemplo de Usuario como referencia, pero cada
  servicio se construye con su propia entidad y reglas.
- `usuario-servicio` se reconstruye en la Fase 2 con `rolId` y cliente a
  `rol-servicio`; no se reutiliza el `enum Rol` de la plantilla.
- No se implementa Eureka, mensajería, Docker ni bases de datos externas.
- La app Android se construye después de validar estos servicios.
