# plantilla-servicio

Microservicio base de AppBelmonte. Se usa como referencia para crear los demás
servicios copiando esta carpeta y renombrando paquete, clase principal, puerto y
base de datos.

Arranca en el puerto `8099` y usa la base H2 en memoria `plantilla`.

## Contenido reutilizable (infraestructura)

- `config/SecurityConfig.java`: seguridad sin estado, `/api/auth/**` público y
  `@EnableMethodSecurity`.
- `config/JwtAuthenticationFilter.java`: valida el token y arma la autenticación
  con el id del usuario y el rol.
- `config/JwtService.java`: solo valida y parsea el token. Todos los servicios lo
  usan.
- `config/RestAuthenticationEntryPoint.java`: respuesta 401 en JSON.
- `config/RestAccessDeniedHandler.java`: respuesta 403 en JSON.
- `config/RestClientConfig.java`: `RestClient.Builder` que reenvía el encabezado
  `Authorization` de la petición actual.
- `dto/ErrorResponse.java`: formato de error común.
- `exception/`: excepciones de negocio y manejador global (400, 401, 403, 404,
  409, 500 y 503).

## Contenido de ejemplo (solo referencia)

El ejemplo implementa el registro y login de Usuario para mostrar la
construcción completa. No forma parte de la infraestructura reutilizable:

- `model/Usuario.java`, `model/Rol.java`
- `repository/UsuarioRepository.java`
- `service/UsuarioService.java`
- `controller/AuthController.java`
- `dto/LoginRequest.java`, `dto/LoginResponse.java`, `dto/RegistroRequest.java`
- `config/DatosPruebaConfig.java`
- `config/JwtEmisor.java` (genera el token; solo lo usa el servicio de usuario)

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
   `description`.

4. Renombrar el paquete:

   ```bash
   mv <nombre>-servicio/src/main/java/cl/duoc/appbelmonte/plantilla \
      <nombre>-servicio/src/main/java/cl/duoc/appbelmonte/<paquete>
   mv <nombre>-servicio/src/test/java/cl/duoc/appbelmonte/plantilla \
      <nombre>-servicio/src/test/java/cl/duoc/appbelmonte/<paquete>
   ```

   Reemplazar `cl.duoc.appbelmonte.plantilla` por
   `cl.duoc.appbelmonte.<paquete>` en todos los archivos Java.

5. Renombrar `PlantillaServicioApplication.java` y
   `PlantillaServicioApplicationTests.java` con el nombre del servicio y
   actualizar sus referencias.

6. En `src/main/resources/application.properties`, ajustar
   `spring.application.name`, `server.port` y el nombre de la base H2.

7. Eliminar el ejemplo de Usuario si el servicio no lo necesita (ver lista
   anterior). Conservar siempre `config/` de seguridad y JWT, `dto/ErrorResponse`
   y `exception/`.

8. Dejar solo las propiedades `servicios.*.url` que el servicio realmente use.

9. Crear las clases de negocio en `model`, `repository`, `service`, `controller`
   y `dto`, y los clientes REST en `client`.

10. Compilar:

    ```bash
    ./mvnw -q -DskipTests package
    ```

## Notas

- La clave `jwt.secret` debe ser idéntica en todos los servicios.
- No se usan relaciones JPA ni claves foráneas entre servicios; solo se guarda el
  id de la entidad referenciada.
- Las llamadas entre servicios usan `RestClient` y reenvían el token.
- Si el servicio llamado no responde, lanzar `ServicioNoDisponibleException`
  para devolver 503.
