# AppBelmonte - Backend

Monorepo con un microservicio Spring Boot independiente por entidad del modelo
de datos. Cada servicio tiene su propio proyecto Maven, su puerto y su esquema
Flyway dentro de una base MySQL compartida. La comunicacion interna se hace con
Feign y Eureka; el cliente Android entra por el gateway en el puerto `8080`.

No hay autenticacion ni autorizacion: el login solo valida credenciales.

## Documentacion

- `plan_backend_appbelmonte.md`: especificacion original del backend.
- `docs/plan-construccion-appbelmonte.md`: patrones, fichas y planes por lote.
- `docs/Sumario_Final.md`: arquitectura, dependencias, Flyway, llamadas API,
  flujo completo y estado de entrega.
- `docs/Comparativa_XAMPP_vs_MySQL_Docker.md`: guia de XAMPP, fricciones,
  comparacion con MySQL 8.4 en Docker y plan de migracion.

## Arquitectura

- `eureka-server` en `8761`: registro y descubrimiento de servicios.
- `gateway` en `8080`: unica entrada para la app Android; enruta `/api/...`
  hacia los servicios registrados con `lb://`.
- 15 servicios de negocio en `8081-8095`.
- `plantilla-servicio` en `8099`: base reutilizable; no se levanta en Compose.
- XAMPP/MariaDB en `localhost:3307`, usuario `appbelmonte`.
- La migracion a MySQL 8.4 en Docker esta planificada; ver
  `docs/Comparativa_XAMPP_vs_MySQL_Docker.md`.
- Base compartida `appbelmonte`; `plantilla-servicio` usa
  `appbelmonte_plantilla`.
- Flyway crea las tablas (`V1`) y carga los datos de prueba (`V2`).
- Hibernate usa `spring.jpa.hibernate.ddl-auto=validate`; no crea ni modifica
  tablas.
- Los servicios con clientes usan `spring-cloud-starter-openfeign` y
  `spring-cloud-starter-loadbalancer`.
- El gateway usa WebFlux y no incluye OpenFeign.

## Servicios y puertos

| N.º | Servicio | Puerto | Tabla principal | Datos iniciales |
|---|---|---|---|---|
| 1 | rol-servicio | 8081 | `roles` | 3 roles |
| 2 | usuario-servicio | 8082 | `usuarios` | 6 usuarios |
| 3 | marca-servicio | 8083 | `marcas` | 3 marcas |
| 4 | modelo-vehiculo-servicio | 8084 | `modelos_vehiculo` | 3 modelos |
| 5 | vehiculo-servicio | 8085 | `vehiculos` | 3 vehiculos |
| 6 | conductor-servicio | 8086 | `conductores` | 3 conductores |
| 7 | region-servicio | 8087 | `regiones` | 2 regiones |
| 8 | comuna-servicio | 8088 | `comunas` | 6 comunas |
| 9 | direccion-servicio | 8089 | `direcciones` | 6 direcciones |
| 10 | pasajero-servicio | 8090 | `pasajeros` | 3 pasajeros |
| 11 | estado-solicitud-servicio | 8091 | `estados_solicitud` | 3 estados |
| 12 | solicitud-servicio | 8092 | `solicitudes_traslado` | 3 solicitudes |
| 13 | estado-viaje-servicio | 8093 | `estados_viaje` | 3 estados |
| 14 | viaje-servicio | 8094 | `viajes` | Sin datos |
| 15 | historial-servicio | 8095 | `historial_estados` | Sin datos |

`eureka-server` usa el puerto `8761` y `gateway` el `8080`.
`plantilla-servicio` es la base reutilizable y arranca en `8099`. Ver
`plantilla-servicio/README.md`.

## Base de datos y Flyway

Cada servicio usa la misma base `appbelmonte`, pero tiene su propia tabla de
historial de Flyway:

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

Las migraciones estan en `src/main/resources/db/migration`:

- `V1__crear_tabla_<tabla>.sql`: crea la tabla.
- `V2__datos_prueba_<tabla>.sql`: carga datos ficticios con ids explicitos.

La configuracion usa `baseline-on-migrate=true` y `baseline-version=0`. La
version `0` es obligatoria: con `1`, Flyway omitiria `V1` en una base que ya
tiene tablas y el esquema quedaria incompleto.

`baseline-on-migrate` no adopta tablas existentes ni corrige diferencias de
esquema. Para la primera ejecucion se recomienda una base limpia. Si ya existen
tablas sin historial Flyway, recrear la base o hacer un baseline manual.

Si el datadir de XAMPP viene de una version anterior, ejecutar una vez
`/opt/lampp/bin/mysql_upgrade` antes de arrancar los servicios. Un datadir
antiguo puede fallar con `Column count of mysql.proc is wrong`. En este equipo
el datadir ya fue actualizado y las bases `appbelmonte` quedaron creadas con
sus seeds.

Preparar las bases:

```bash
/opt/lampp/bin/mysql -u root < scripts/init-mysql.sql
```

El script crea `appbelmonte` y `appbelmonte_plantilla` si no existen. La
aplicacion usa el usuario `appbelmonte`; no se recomienda conectar como `root`.

## Orden de arranque local

1. MySQL de XAMPP.
2. `eureka-server` (`8761`).
3. Servicios de negocio, en el orden de dependencias.
4. `gateway` (`8080`).

Orden recomendado de negocio:

1. rol-servicio
2. usuario-servicio
3. region-servicio y comuna-servicio
4. direccion-servicio y pasajero-servicio
5. marca-servicio, modelo-vehiculo-servicio y vehiculo-servicio
6. conductor-servicio
7. estado-solicitud-servicio y estado-viaje-servicio
8. solicitud-servicio
9. historial-servicio
10. viaje-servicio

## Arrancar un servicio

```bash
cd eureka-server
./mvnw spring-boot:run

cd ../rol-servicio
./mvnw spring-boot:run
```

Cada servicio queda disponible en `http://localhost:<puerto>`. La app Android
debe usar una sola URL base:

```text
http://localhost:8080
```

El gateway enruta `/api/auth/**` y `/api/usuarios/**` a `usuario-servicio`, y el
resto de `/api/...` al servicio correspondiente.

## Docker Compose

`docker-compose.yml` levanta Eureka, gateway y los 15 servicios. En el estado
actual MySQL no se incluye en Compose: se usa XAMPP/MariaDB en el host.

```bash
docker compose up --build
```

En Linux, cada contenedor recibe `host.docker.internal:host-gateway` y usa:

```text
jdbc:mysql://host.docker.internal:3307/appbelmonte
```

La migracion a MySQL 8.4 dentro de Compose, con credenciales en `.env`, esta
descrita en `docs/Comparativa_XAMPP_vs_MySQL_Docker.md`.

Para detener:

```bash
docker compose down
```

## Compilar y probar

```bash
cd viaje-servicio
./mvnw -q -DskipTests package
./mvnw test
```

Pruebas incluidas, solo unitarias con Mockito:

- `solicitud-servicio`: `SolicitudServiceTest` (7 pruebas).
- `viaje-servicio`: `ViajeServiceTest` (12 pruebas).
- `usuario-servicio`: `UsuarioServiceTest` (4 pruebas).

Las pruebas de contexto `@SpringBootTest` fueron eliminadas porque dependian de
H2. Los 18 proyectos Maven compilan y las pruebas unitarias pasan sin levantar
MySQL.

## Usuarios de prueba

Todos usan la contrasena `clave123`.

| Id | Nombre | Correo | Rol |
|---|---|---|---|
| 1 | Admin Belmonte | `admin@appbelmonte.cl` | ADMINISTRADOR |
| 2 | Carla Soto | `carla.soto@ficticio.cl` | SOLICITANTE |
| 3 | Diego Rojas | `diego.rojas@ficticio.cl` | SOLICITANTE |
| 4 | Pedro Vera | `pedro.vera@ficticio.cl` | CONDUCTOR |
| 5 | Maria Lagos | `maria.lagos@ficticio.cl` | CONDUCTOR |
| 6 | Luis Fuentes | `luis.fuentes@ficticio.cl` | CONDUCTOR |

## Autenticacion y alcance de seguridad

- `POST /api/auth/login` valida correo y contrasena, y devuelve `id`, `rolId`,
  `rolNombre` y `nombre`. No emite token.
- No hay Spring Security, JWT, filtros, headers de identidad ni `@PreAuthorize`.
- Todos los endpoints son publicos. La identidad se envia en el body o en la ruta
  segun el endpoint (por ejemplo, `solicitanteId`, `conductorId`, `asignadoPorId`).
- Las contrasenas de los datos de prueba estan en texto plano (`clave123`).
- Limitacion: al no existir autenticacion, cualquier cliente puede invocar
  cualquier endpoint. No hay validacion de rol ni de propiedad del recurso.

## Rutas de la API

Prefijo comun: `/api`. Todas las rutas se pueden invocar por el gateway en
`http://localhost:8080`.

### rol-servicio

| Metodo y ruta | Acceso |
|---|---|
| GET `/api/roles` | Publico |
| GET `/api/roles/{id}` | Publico |
| GET `/api/roles/nombre/{nombre}` | Publico |
| POST `/api/roles` | Publico |
| PUT `/api/roles/{id}` | Publico |

### usuario-servicio

| Metodo y ruta | Acceso |
|---|---|
| POST `/api/auth/registro` | Publico |
| POST `/api/auth/login` | Publico |
| GET `/api/usuarios/{id}` | Publico |

### marca-servicio

| Metodo y ruta | Acceso |
|---|---|
| GET `/api/marcas` | Publico |
| GET `/api/marcas/{id}` | Publico |
| POST `/api/marcas` | Publico |
| PUT `/api/marcas/{id}` | Publico |

### modelo-vehiculo-servicio

| Metodo y ruta | Acceso |
|---|---|
| GET `/api/modelos` | Publico |
| GET `/api/modelos?marcaId=` | Publico |
| GET `/api/modelos/{id}` | Publico |
| POST `/api/modelos` | Publico |
| PUT `/api/modelos/{id}` | Publico |

### vehiculo-servicio

| Metodo y ruta | Acceso |
|---|---|
| GET `/api/vehiculos` | Publico |
| GET `/api/vehiculos/{id}` | Publico |
| POST `/api/vehiculos` | Publico |
| PUT `/api/vehiculos/{id}` | Publico |

### conductor-servicio

| Metodo y ruta | Acceso |
|---|---|
| POST `/api/conductores/{usuarioId}` | Publico |
| GET `/api/conductores/{id}` | Publico |
| PUT `/api/conductores/{id}/disponibilidad` | Publico |
| GET `/api/conductores/disponibles` | Publico |

### region-servicio

| Metodo y ruta | Acceso |
|---|---|
| GET `/api/regiones` | Publico |
| GET `/api/regiones/{id}` | Publico |
| POST `/api/regiones` | Publico |
| PUT `/api/regiones/{id}` | Publico |

### comuna-servicio

| Metodo y ruta | Acceso |
|---|---|
| GET `/api/comunas` | Publico |
| GET `/api/comunas?regionId=` | Publico |
| GET `/api/comunas/{id}` | Publico |
| POST `/api/comunas` | Publico |
| PUT `/api/comunas/{id}` | Publico |

### direccion-servicio

| Metodo y ruta | Acceso |
|---|---|
| POST `/api/direcciones` | Publico |
| GET `/api/direcciones/{id}` | Publico |

### pasajero-servicio

| Metodo y ruta | Acceso |
|---|---|
| POST `/api/pasajeros` | Publico |
| GET `/api/pasajeros/{id}` | Publico |

### estado-solicitud-servicio

| Metodo y ruta | Acceso |
|---|---|
| GET `/api/estados-solicitud` | Publico |
| GET `/api/estados-solicitud/{id}` | Publico |
| GET `/api/estados-solicitud/nombre/{nombre}` | Publico |
| POST `/api/estados-solicitud` | Publico |
| PUT `/api/estados-solicitud/{id}` | Publico |

### solicitud-servicio

| Metodo y ruta | Acceso |
|---|---|
| POST `/api/solicitudes` | Publico |
| GET `/api/solicitudes/mias/{solicitanteId}` | Publico |
| PUT `/api/solicitudes/{id}/cancelar` | Publico |
| GET `/api/solicitudes?estado=&fecha=` | Publico |
| GET `/api/solicitudes?ids=1,2,3&fecha=` | Publico |
| GET `/api/solicitudes/{id}` | Publico |
| PUT `/api/solicitudes/{id}/asignar` | Publico |

### estado-viaje-servicio

| Metodo y ruta | Acceso |
|---|---|
| GET `/api/estados-viaje` | Publico |
| GET `/api/estados-viaje/{id}` | Publico |
| GET `/api/estados-viaje/nombre/{nombre}` | Publico |
| POST `/api/estados-viaje` | Publico |
| PUT `/api/estados-viaje/{id}` | Publico |

### viaje-servicio

| Metodo y ruta | Acceso |
|---|---|
| POST `/api/viajes` | Publico |
| PUT `/api/viajes/{id}/reasignar` | Publico |
| GET `/api/viajes?fecha=` | Publico |
| GET `/api/viajes/mis-viajes/{conductorId}` | Publico |
| GET `/api/viajes/mis-viajes/{conductorId}/hoy` | Publico |
| GET `/api/viajes/{id}` | Publico |
| PUT `/api/viajes/{id}/iniciar` | Publico |
| PUT `/api/viajes/{id}/finalizar` | Publico |

### historial-servicio

| Metodo y ruta | Acceso |
|---|---|
| POST `/api/historial` | Publico |
| GET `/api/historial/viaje/{viajeId}` | Publico |

## Codigos de error

Formato de respuesta:

```json
{
  "estado": 400,
  "mensaje": "Descripcion del error en espanol",
  "fecha": "2026-11-09T18:00:00"
}
```

| Situacion | Codigo |
|---|---|
| Datos invalidos o id relacionado que no existe | 400 |
| Credenciales invalidas en login | 401 |
| Recurso no existe | 404 |
| Regla de negocio incumplida (estado incorrecto, dato repetido) | 409 |
| Otro servicio no disponible | 503 |

## Flujo completo validado

1. Solicitante inicia sesion (solo validacion de credenciales), consulta
   regiones/comunas y crea una solicitud.
2. Administrador lista pendientes y conductores disponibles.
3. Administrador asigna conductor: solicitud `ASIGNADA` y viaje `ASIGNADO`.
4. Conductor ve el viaje en `mis-viajes/{conductorId}/hoy`, con coordenadas.
5. Conductor inicia el viaje: `EN_CURSO`.
6. Conductor finaliza el viaje: `FINALIZADO`.
7. Administrador consulta el historial con nombres de estado.

El flujo se valido con Eureka, gateway y los 15 servicios encendidos sobre
MySQL 8.4 y MariaDB 10.4.32 de XAMPP: registro de los 16 modulos, rutas del
gateway, login, creacion de solicitud con Feign, asignacion de viaje, inicio,
fin e historial. Las 23 pruebas unitarias y las compilaciones de los 18
proyectos pasan. El gateway puede responder `503` durante los primeros segundos
mientras actualiza su cache de Eureka.

## Alcance de esta etapa

- Backend completo: 15 servicios implementados y compilados.
- Fase 1 sin autenticacion ni autorizacion; solo el login valida credenciales.
- Fase 2 con MySQL, Flyway, Eureka, gateway, Feign y Docker Compose.
- Coleccion Postman: se realizara por separado.
- La app Android debe usar `http://localhost:8080` como URL base.
