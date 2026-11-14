# AppBelmonte - Backend

Monorepo con un microservicio Spring Boot independiente por entidad del modelo
de datos. Cada servicio tiene su propio proyecto Maven, su puerto y su base H2
en memoria. La comunicacion entre servicios se hace con `RestClient` y el token
JWT se reenvia en cada llamada.

## Documentacion

- `plan_backend_appbelmonte.md`: especificacion original del backend.
- `docs/plan-construccion-appbelmonte.md`: patrones, fichas y planes por lote.
- `docs/Sumario_Final.md`: arquitectura, dependencias, seguridad, tokenizacion,
  llamadas API, flujo completo y estado de entrega.

## Servicios y puertos

| N.º | Servicio | Puerto | Base H2 |
|---|---|---|---|
| 1 | rol-servicio | 8081 | `jdbc:h2:mem:rol` |
| 2 | usuario-servicio | 8082 | `jdbc:h2:mem:usuario` |
| 3 | marca-servicio | 8083 | `jdbc:h2:mem:marca` |
| 4 | modelo-vehiculo-servicio | 8084 | `jdbc:h2:mem:modelo` |
| 5 | vehiculo-servicio | 8085 | `jdbc:h2:mem:vehiculo` |
| 6 | conductor-servicio | 8086 | `jdbc:h2:mem:conductor` |
| 7 | region-servicio | 8087 | `jdbc:h2:mem:region` |
| 8 | comuna-servicio | 8088 | `jdbc:h2:mem:comuna` |
| 9 | direccion-servicio | 8089 | `jdbc:h2:mem:direccion` |
| 10 | pasajero-servicio | 8090 | `jdbc:h2:mem:pasajero` |
| 11 | estado-solicitud-servicio | 8091 | `jdbc:h2:mem:estadoSolicitud` |
| 12 | solicitud-servicio | 8092 | `jdbc:h2:mem:solicitud` |
| 13 | estado-viaje-servicio | 8093 | `jdbc:h2:mem:estadoViaje` |
| 14 | viaje-servicio | 8094 | `jdbc:h2:mem:viaje` |
| 15 | historial-servicio | 8095 | `jdbc:h2:mem:historial` |

`plantilla-servicio` es la base reutilizable y arranca en el puerto `8099`. Ver
`plantilla-servicio/README.md` para los pasos de copia.

## Orden de arranque

El orden recomendado es el de las fases del plan:

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
cd rol-servicio
./mvnw spring-boot:run
```

Cada servicio queda disponible en `http://localhost:<puerto>`. La consola H2
esta en `http://localhost:<puerto>/h2-console` con usuario `sa` y contrasena
vacia.

## Compilar y probar

```bash
# Compilar un servicio
cd viaje-servicio
./mvnw -q -DskipTests package

# Ejecutar todas las pruebas de un servicio
./mvnw test
```

Pruebas incluidas:

- `solicitud-servicio`: `SolicitudServiceTest` (9 pruebas) y contexto Spring.
- `viaje-servicio`: `ViajeServiceTest` (15 pruebas) y contexto Spring.
- `usuario-servicio`: `UsuarioServiceTest` (4 pruebas) y contexto Spring.

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

## Seguridad y token

- `usuario-servicio` genera el JWT en `POST /api/auth/login`.
- El token incluye `sub` (id del usuario), `rol` (nombre del rol) y `nombre`.
- Todos los servicios comparten `jwt.secret` y validan el token localmente; no
  consultan a `usuario-servicio` en cada peticion.
- La sesion es sin estado. Sin token o con token invalido se responde `401`.
  Un rol sin permiso responde `403`.
- Rutas publicas: `POST /api/auth/**` y `GET /api/roles/**`.
- El resto de rutas requiere `Authorization: Bearer <token>`.
- Los permisos por rol se declaran con `@PreAuthorize`.

## Rutas de la API

Prefijo comun: `/api`.

### rol-servicio (8081)

| Metodo y ruta | Rol |
|---|---|
| GET `/api/roles` | Publico |
| GET `/api/roles/{id}` | Publico |
| GET `/api/roles/nombre/{nombre}` | Publico |
| POST `/api/roles` | ADMINISTRADOR |
| PUT `/api/roles/{id}` | ADMINISTRADOR |

### usuario-servicio (8082)

| Metodo y ruta | Rol |
|---|---|
| POST `/api/auth/registro` | Publico (rolId 2 o 3) |
| POST `/api/auth/login` | Publico |
| GET `/api/usuarios/yo` | Autenticado |

### marca-servicio (8083)

| Metodo y ruta | Rol |
|---|---|
| GET `/api/marcas` | Autenticado |
| GET `/api/marcas/{id}` | Autenticado |
| POST `/api/marcas` | ADMINISTRADOR |
| PUT `/api/marcas/{id}` | ADMINISTRADOR |

### modelo-vehiculo-servicio (8084)

| Metodo y ruta | Rol |
|---|---|
| GET `/api/modelos` | Autenticado |
| GET `/api/modelos?marcaId=` | Autenticado |
| GET `/api/modelos/{id}` | Autenticado |
| POST `/api/modelos` | ADMINISTRADOR |
| PUT `/api/modelos/{id}` | ADMINISTRADOR |

### vehiculo-servicio (8085)

| Metodo y ruta | Rol |
|---|---|
| GET `/api/vehiculos` | ADMINISTRADOR |
| GET `/api/vehiculos/{id}` | ADMINISTRADOR, CONDUCTOR |
| POST `/api/vehiculos` | ADMINISTRADOR |
| PUT `/api/vehiculos/{id}` | ADMINISTRADOR |

### conductor-servicio (8086)

| Metodo y ruta | Rol |
|---|---|
| POST `/api/conductores` | CONDUCTOR |
| GET `/api/conductores/yo` | CONDUCTOR |
| PUT `/api/conductores/yo/disponibilidad` | CONDUCTOR |
| GET `/api/conductores/disponibles` | ADMINISTRADOR |
| GET `/api/conductores/{id}` | ADMINISTRADOR |

### region-servicio (8087)

| Metodo y ruta | Rol |
|---|---|
| GET `/api/regiones` | Autenticado |
| GET `/api/regiones/{id}` | Autenticado |
| POST `/api/regiones` | ADMINISTRADOR |
| PUT `/api/regiones/{id}` | ADMINISTRADOR |

### comuna-servicio (8088)

| Metodo y ruta | Rol |
|---|---|
| GET `/api/comunas` | Autenticado |
| GET `/api/comunas?regionId=` | Autenticado |
| GET `/api/comunas/{id}` | Autenticado |
| POST `/api/comunas` | ADMINISTRADOR |
| PUT `/api/comunas/{id}` | ADMINISTRADOR |

### direccion-servicio (8089)

| Metodo y ruta | Rol |
|---|---|
| POST `/api/direcciones` | SOLICITANTE, ADMINISTRADOR |
| GET `/api/direcciones/{id}` | Autenticado |

### pasajero-servicio (8090)

| Metodo y ruta | Rol |
|---|---|
| POST `/api/pasajeros` | SOLICITANTE, ADMINISTRADOR |
| GET `/api/pasajeros/{id}` | Autenticado |

### estado-solicitud-servicio (8091)

| Metodo y ruta | Rol |
|---|---|
| GET `/api/estados-solicitud` | Autenticado |
| GET `/api/estados-solicitud/{id}` | Autenticado |
| GET `/api/estados-solicitud/nombre/{nombre}` | Autenticado |
| POST `/api/estados-solicitud` | ADMINISTRADOR |
| PUT `/api/estados-solicitud/{id}` | ADMINISTRADOR |

### solicitud-servicio (8092)

| Metodo y ruta | Rol |
|---|---|
| POST `/api/solicitudes` | SOLICITANTE |
| GET `/api/solicitudes/mias` | SOLICITANTE |
| PUT `/api/solicitudes/{id}/cancelar` | SOLICITANTE (dueno) |
| GET `/api/solicitudes?estado=&fecha=` | ADMINISTRADOR |
| GET `/api/solicitudes?ids=1,2,3&fecha=` | ADMINISTRADOR, CONDUCTOR |
| GET `/api/solicitudes/{id}` | ADMINISTRADOR, CONDUCTOR, SOLICITANTE (dueno) |
| PUT `/api/solicitudes/{id}/asignar` | ADMINISTRADOR (uso entre servicios) |

### estado-viaje-servicio (8093)

| Metodo y ruta | Rol |
|---|---|
| GET `/api/estados-viaje` | Autenticado |
| GET `/api/estados-viaje/{id}` | Autenticado |
| GET `/api/estados-viaje/nombre/{nombre}` | Autenticado |
| POST `/api/estados-viaje` | ADMINISTRADOR |
| PUT `/api/estados-viaje/{id}` | ADMINISTRADOR |

### viaje-servicio (8094)

| Metodo y ruta | Rol |
|---|---|
| POST `/api/viajes` | ADMINISTRADOR |
| PUT `/api/viajes/{id}/reasignar` | ADMINISTRADOR |
| GET `/api/viajes?fecha=` | ADMINISTRADOR |
| GET `/api/viajes/mis-viajes` | CONDUCTOR |
| GET `/api/viajes/mis-viajes/hoy` | CONDUCTOR |
| GET `/api/viajes/{id}` | ADMINISTRADOR, CONDUCTOR (dueno), SOLICITANTE (dueno) |
| PUT `/api/viajes/{id}/iniciar` | CONDUCTOR (dueno) |
| PUT `/api/viajes/{id}/finalizar` | CONDUCTOR (dueno) |

### historial-servicio (8095)

| Metodo y ruta | Rol |
|---|---|
| POST `/api/historial` | ADMINISTRADOR, CONDUCTOR (uso entre servicios) |
| GET `/api/historial/viaje/{viajeId}` | ADMINISTRADOR |

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
| Sin token o token invalido | 401 |
| Rol sin permiso | 403 |
| Recurso no existe | 404 |
| Regla de negocio incumplida (estado incorrecto, dato repetido) | 409 |
| Otro servicio no disponible | 503 |

## Flujo completo validado

1. Solicitante inicia sesion, consulta regiones/comunas y crea una solicitud.
2. Administrador lista pendientes y conductores disponibles.
3. Administrador asigna conductor: solicitud `ASIGNADA` y viaje `ASIGNADO`.
4. Conductor ve el viaje en `mis-viajes/hoy`, con coordenadas.
5. Conductor inicia el viaje: `EN_CURSO`.
6. Conductor finaliza el viaje: `FINALIZADO`.
7. Administrador consulta el historial con nombres de estado.

El flujo y los casos de error se validaron con los 15 servicios encendidos,
incluido el caso `503` con `solicitud-servicio` apagado y sin viaje guardado.

## Alcance de esta etapa

- Backend completo: 15 servicios implementados, compilados y probados.
- Coleccion Postman: se realizara por separado.
- Gateway en el puerto 8080: se aplicara en otra etapa. Por ahora la app debe
  usar una URL base por servicio.
