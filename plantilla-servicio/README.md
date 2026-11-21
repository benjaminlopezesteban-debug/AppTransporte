# plantilla-servicio

Microservicio base de AppBelmonte. Se usa como referencia para crear los demás
servicios copiando esta carpeta y renombrando paquete, clase principal, puerto y
tabla.

Arranca en el puerto `8099` y usa MySQL con la base `appbelmonte_plantilla`.
Las migraciones Flyway crean la tabla `usuarios` y cargan seis usuarios de
ejemplo.

## Contenido reutilizable (infraestructura)

- `dto/ErrorResponse.java`: formato de error común.
- `exception/`: excepciones de negocio y manejador global (400, 401, 404, 409,
  500 y 503).
- No hay configuración de seguridad, filtros JWT ni propagación de token.
- `pom.xml`: MySQL, Flyway, Eureka y Spring Cloud.
- `application.properties`: datasource, Flyway, Eureka y validación de esquema.
- `db/migration/V1__crear_tabla_usuarios.sql` y
  `V2__datos_prueba_usuarios.sql`: ejemplo de migraciones.
- Si el servicio usa clientes internos, agregar `@EnableFeignClients` y una
  interfaz `@FeignClient`; no crear `RestClientConfig`.

## Contenido de ejemplo (solo referencia)

El ejemplo implementa el registro y login de Usuario para mostrar la
construcción completa. No forma parte de la infraestructura reutilizable:

- `model/Usuario.java`, `model/Rol.java`
- `repository/UsuarioRepository.java`
- `service/UsuarioService.java`
- `controller/AuthController.java`
- `dto/LoginRequest.java`, `dto/LoginResponse.java`, `dto/RegistroRequest.java`
- `db/migration/V1__crear_tabla_usuarios.sql`
- `db/migration/V2__datos_prueba_usuarios.sql`

El login solo valida correo y contraseña. No genera token.

## Cómo crear un servicio nuevo

1. Borrar artefactos de compilación antes de copiar:

   ```bash
   rm -rf plantilla-servicio/target
   ```

2. Copiar la carpeta:

   ```bash
   cp -r plantilla-servicio <nombre>-servicio
   ```

3. En `<nombre>-servicio/pom.xml`, cambiar `artifactId`, `name` y
   `description`. Conservar las dependencias de MySQL, Flyway, Eureka y Spring
   Cloud.

4. Renombrar el paquete:

   ```bash
   mv <nombre>-servicio/src/main/java/cl/duoc/appbelmonte/plantilla \
      <nombre>-servicio/src/main/java/cl/duoc/appbelmonte/<paquete>
   mv <nombre>-servicio/src/test/java/cl/duoc/appbelmonte/plantilla \
      <nombre>-servicio/src/test/java/cl/duoc/appbelmonte/<paquete>
   ```

   Reemplazar `cl.duoc.appbelmonte.plantilla` por
   `cl.duoc.appbelmonte.<paquete>` en todos los archivos Java.

5. Renombrar `PlantillaServicioApplication.java` con el nombre del servicio y
   actualizar sus referencias. Si usa Feign, agregar `@EnableFeignClients`.

6. En `src/main/resources/application.properties`, ajustar:

   - `spring.application.name`.
   - `server.port`.
   - `spring.flyway.table` con un nombre único por servicio.
   - `DB_URL` si el servicio usa otra base; por defecto usa `appbelmonte`.

7. Eliminar el ejemplo de Usuario si el servicio no lo necesita (ver lista
   anterior). Conservar siempre `dto/ErrorResponse`, `exception/` y la
   configuración de Flyway.

8. Crear las migraciones en `src/main/resources/db/migration`:

   - `V1__crear_tabla_<tabla>.sql`.
   - `V2__datos_prueba_<tabla>.sql` si hay seeds.

   No usar `ddl-auto=update`. El esquema lo controla Flyway y Hibernate solo
   valida.

9. Crear las clases de negocio en `model`, `repository`, `service`, `controller`
   y `dto`, y los clientes Feign en `client`.

10. Si se usan clientes internos:

    - Agregar `spring-cloud-starter-openfeign` y
      `spring-cloud-starter-loadbalancer` al `pom.xml`.
    - Agregar `@EnableFeignClients` en la clase principal.
    - Declarar interfaces `@FeignClient(name = "<servicio>")`.
    - Traducir `FeignException` y `RetryableException` a las excepciones de
      negocio.

11. Compilar y probar:

    ```bash
    ./mvnw -q -DskipTests package
    ./mvnw test
    ```

## Notas

- No hay autenticación ni autorización. Todos los endpoints son públicos.
- La identidad se envía en el body o en la ruta, no en headers.
- No se usan relaciones JPA ni claves foráneas entre servicios; solo se guarda el
  id de la entidad referenciada.
- Las llamadas entre servicios usan Feign con Eureka, sin token.
- Si el servicio llamado no responde, lanzar `ServicioNoDisponibleException`
  para devolver 503.
- `spring.flyway.baseline-version=0` es obligatorio para no omitir `V1`.
- La primera ejecución debe hacerse sobre una base limpia. `baseline-on-migrate`
  no adopta tablas existentes ni corrige el esquema.
