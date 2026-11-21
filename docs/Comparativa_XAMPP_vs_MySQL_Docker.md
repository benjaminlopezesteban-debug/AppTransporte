# Comparativa XAMPP/MariaDB vs MySQL 8.4 en Docker

Guia tecnica para AppBelmonte: operacion con XAMPP, fricciones encontradas,
comparacion con MySQL en Docker y plan de migracion.

- Fecha: 2026-11-18
- Estado: documento de decision y planificacion; migracion aun no ejecutada.
- Alcance: base de datos del backend AppBelmonte.
- Decision objetivo: MySQL 8.4 LTS en Docker Compose, con credenciales en `.env`.

## 1. Resumen ejecutivo

AppBelmonte se desarrollo y valido inicialmente contra XAMPP/MariaDB porque era
el entorno disponible para el curso. XAMPP permitio avanzar rapido, pero introdujo
dependencia del host, conflictos de puertos, diferencias de version y problemas de
credenciales que no forman parte del dominio del proyecto.

La validacion tambien se realizo contra MySQL 8.4 en un contenedor temporal. El
esquema, Flyway y las rutas REST funcionaron en ambas bases. Por portabilidad,
reproducibilidad y cercania a un entorno de integracion, se decide migrar a MySQL
8.4 LTS dentro de Docker Compose.

Este documento no reemplaza la migracion. Primero se deben probar todos los
metodos REST en Postman y corroborar las pruebas automatizadas. Despues se aplica
el cambio de infraestructura descrito en la seccion 9.

## 2. Contexto del proyecto

- Java 21.
- Spring Boot 4.1.2.
- Spring Cloud 2025.1.4.
- 18 proyectos Maven independientes; no existe `pom.xml` raiz.
- 15 servicios de negocio, `eureka-server` y `gateway`.
- Base compartida `appbelmonte`.
- `plantilla-servicio` usa `appbelmonte_plantilla` y no se levanta en Compose.
- Flyway crea tablas (`V1`) y carga datos de prueba (`V2`).
- Hibernate usa `spring.jpa.hibernate.ddl-auto=validate`.
- El gateway es la entrada HTTP en `8080`; Eureka opera en `8761`.

### Estado actual

- XAMPP 8.2.12-0 con MariaDB 10.4.32.
- MariaDB escucha en `0.0.0.0:3307`.
- `docker-compose.yml` usa `host.docker.internal:3307`.
- Las credenciales de base de datos estan escritas directamente en Compose.
- La aplicacion funciona, pero el arranque depende de XAMPP y de la configuracion
  del host.

### Estado objetivo

- MySQL 8.4 LTS como servicio `mysql` de Docker Compose.
- Volumen persistente para `/var/lib/mysql`.
- Script de inicializacion para crear las bases.
- `healthcheck` antes de arrancar los servicios.
- `DB_URL` interno: `jdbc:mysql://mysql:3306/appbelmonte`.
- Credenciales en `.env`; `.env.example` documentado y `.env` ignorado por Git.
- Un clon nuevo del repositorio debe levantar con pocos comandos.

## 3. Glosario

| Termino | Significado |
|---|---|
| XAMPP | Paquete que integra Apache, MariaDB, PHP y herramientas auxiliares. |
| MariaDB | Fork de MySQL, compatible en protocolo y SQL comun, pero no identico. |
| MySQL 8.4 LTS | Version de soporte extendido de MySQL. |
| Flyway | Herramienta de migraciones versionadas. |
| `ddl-auto=validate` | Hibernate valida el esquema, pero no lo modifica. |
| `host.docker.internal` | Nombre usado por un contenedor para llegar al host. |
| `.env` | Archivo local de variables de entorno; no debe versionarse. |

## 4. Guia de trabajo con XAMPP/MariaDB

### 4.1 Que se usa realmente

Aunque XAMPP incluye Apache y PHP, el backend solo necesita MySQL/MariaDB. No se
debe iniciar Apache, porque intenta ocupar el puerto `8080`, el mismo del gateway.

### 4.2 Arranque

```bash
sudo /opt/lampp/lampp startmysql
```

Verificar que el proceso exista y que el puerto este abierto:

```bash
ps aux | grep -E '[m]ysqld|[m]ariadbd'
ss -ltnp | grep 3307
```

No confiar solamente en `lampp status`. En este equipo ese comando reporto
`MySQL is not running` porque el usuario sin privilegios no podia leer el archivo
PID, aunque MariaDB si estaba escuchando.

### 4.3 Puerto y bind-address

Si el MySQL del sistema ocupa `3306`, usar `3307` para XAMPP. La configuracion
relevante esta en `/opt/lampp/etc/my.cnf`:

```ini
[client]
port=3307
socket=/opt/lampp/var/mysql/mysql.sock

[mysqld]
user=mysql
port=3307
socket=/opt/lampp/var/mysql/mysql.sock
bind-address=0.0.0.0
```

`bind-address=0.0.0.0` es necesario si los contenedores Docker van a conectarse
al host. Si solo se usa un cliente local, `127.0.0.1` es suficiente.

Despues de editar:

```bash
sudo /opt/lampp/lampp restartmysql
ss -ltnp | grep 3307
```

### 4.4 Crear las bases

```bash
/opt/lampp/bin/mysql -u root -p < scripts/init-mysql.sql
```

El script crea:

- `appbelmonte`
- `appbelmonte_plantilla`

Ambas con `utf8mb4` y `utf8mb4_unicode_ci`.

### 4.5 Crear el usuario de la aplicacion

No usar `root` para los servicios. Crear un usuario dedicado:

```sql
CREATE USER IF NOT EXISTS 'appbelmonte'@'%' IDENTIFIED BY '<password>';
GRANT ALL PRIVILEGES ON appbelmonte.* TO 'appbelmonte'@'%';
GRANT ALL PRIVILEGES ON appbelmonte_plantilla.* TO 'appbelmonte'@'%';
FLUSH PRIVILEGES;
```

Para pruebas desde el host puede ser necesario crear tambien:

```sql
CREATE USER IF NOT EXISTS 'appbelmonte'@'localhost' IDENTIFIED BY '<password>';
GRANT ALL PRIVILEGES ON appbelmonte.* TO 'appbelmonte'@'localhost';
GRANT ALL PRIVILEGES ON appbelmonte_plantilla.* TO 'appbelmonte'@'localhost';
FLUSH PRIVILEGES;
```

El usuario JDBC debe ser `appbelmonte`. El sufijo `@'%'` pertenece a la cuenta de
MySQL, no al valor de `DB_USERNAME`.

### 4.6 Verificar desde el host y desde Docker

Desde el host:

```bash
/opt/lampp/bin/mysql --protocol=TCP -h127.0.0.1 -P3307 \
  -uappbelmonte -p -e "SELECT CURRENT_USER(), VERSION();"
```

Desde la red Docker:

```bash
docker run --rm --network appbelmonte-net \
  --add-host host.docker.internal:host-gateway \
  mysql:8.4 mysql --protocol=TCP \
  -h host.docker.internal -P3307 \
  -uappbelmonte -p -e "SELECT CURRENT_USER(), VERSION();"
```

La conexion desde un contenedor debe mostrar `appbelmonte@%`.

### 4.7 Datadir antiguo

Un datadir creado con MariaDB 10.1.8 puede fallar con:

```text
Column count of mysql.proc is wrong
```

Antes de actualizar, respaldar:

```bash
sudo /opt/lampp/lampp stopmysql
sudo tar -czf /tmp/xampp-mysql-backup-$(date +%F).tar.gz \
  -C /opt/lampp/var mysql
sudo /opt/lampp/lampp startmysql
```

Ejecutar una vez:

```bash
sudo /opt/lampp/bin/mysql_upgrade -u root -p
```

No copiar un datadir entre versiones mayores sin ejecutar el upgrade correspondiente.

## 5. Fricciones encontradas

| # | Sintoma | Causa | Solucion | Leccion |
|---|---|---|---|---|
| 1 | `Column count of mysql.proc is wrong` | Datadir creado con MariaDB 10.1.8 y servidor 10.4.32 | Ejecutar `mysql_upgrade` con respaldo previo | Un datadir antiguo no se debe asumir compatible |
| 2 | XAMPP no arrancaba en `3306` | MySQL del sistema ocupaba ese puerto | Mover XAMPP a `3307` | Los puertos del host son un recurso compartido |
| 3 | Contenedores con `Connection refused` | MariaDB no estaba corriendo o escuchaba solo en loopback | Iniciar MySQL y usar `bind-address=0.0.0.0` | `localhost` del host no es el mismo que el del contenedor |
| 4 | `Access denied` desde Docker | No existia `appbelmonte@'%'` o faltaban grants | Crear usuario `%` y otorgar permisos | El host de la cuenta importa |
| 5 | `DB_USERNAME: appbelmonte@` | Se confundio el usuario JDBC con la cuenta SQL | Usar `appbelmonte` | El `@'host'` solo se usa en `CREATE USER` |
| 6 | `createDatabaseIfNotExist` fallaba | El usuario no tenia privilegio global de creacion | Crear las bases antes y quitar el parametro | La aplicacion no debe necesitar permisos globales |
| 7 | Gateway respondia `503` | `region-servicio` no arrancaba y no se registraba en Eureka | Corregir base de datos y esperar registro | El `503` es sintoma; revisar el servicio de destino |
| 8 | `No servers available for service` | La instancia no estaba en Eureka | Revisar logs del servicio y Eureka | El gateway no reemplaza el diagnostico del servicio |
| 9 | `lampp status` decia que MySQL no corria | El PID no era legible por el usuario | Verificar con `ps` y `ss` | No confiar en un unico comando de estado |
| 10 | Apache no iniciaba en `8080` | El gateway ya usaba `8080` | Usar solo `startmysql` | XAMPP no necesita Apache para este backend |
| 11 | Flyway no adoptaba tablas existentes | `baseline-on-migrate` no corrige esquemas | Usar base limpia o baseline manual | La primera ejecucion debe ser controlada |
| 12 | Diferencias de collation | MySQL 8 y MariaDB 10.4 tienen defaults distintos | El script fija `utf8mb4_unicode_ci` | No depender del default del motor |
| 13 | Credenciales visibles en Compose | Configuracion rapida para el curso | Mover a `.env` y documentar `.env.example` | Los secretos no deben versionarse |
| 14 | Arranque lento y `503` inicial | Servicios y cache de Eureka tardan en estabilizar | Esperar y verificar registro | El arranque no es instantaneo |

## 6. Ventajas y desventajas de XAMPP/MariaDB

### Ventajas

- Instalacion conocida y disponible en el entorno del curso.
- Incluye phpMyAdmin para inspeccionar tablas.
- Bajo consumo adicional si solo se usa MySQL.
- Permite avanzar sin depender de Docker.
- MariaDB es compatible con el SQL usado por el proyecto.

### Desventajas

- Dependencia de la configuracion y del sistema operativo del host.
- Version antigua y fuera de soporte en el caso de MariaDB 10.4.
- Conflictos de puertos con otros MySQL.
- Necesidad de crear usuarios y permisos manualmente.
- `bind-address` y datadir se convierten en fuentes de error.
- Poca reproducibilidad entre equipos.
- Secretos y credenciales terminan en archivos de Compose.
- No representa un entorno de CI/CD.

## 7. MySQL 8.4 en Docker

### 7.1 Arquitectura objetivo

- Servicio `mysql` con imagen `mysql:8.4`.
- Volumen `mysql-data` para `/var/lib/mysql`.
- Script `scripts/init-mysql.sql` montado en
  `/docker-entrypoint-initdb.d/01-init.sql`.
- `healthcheck` con `mysqladmin ping`.
- Red `appbelmonte-net`.
- `DB_URL` interno: `jdbc:mysql://mysql:3306/appbelmonte`.
- Mapeo opcional `3307:3306` solo para clientes del host.
- Credenciales en `.env`; `.env.example` sin valores reales.

### 7.2 Ejemplo conceptual de Compose

```yaml
services:
  mysql:
    image: mysql:8.4
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    ports:
      - "3307:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./scripts/init-mysql.sql:/docker-entrypoint-initdb.d/01-init.sql:ro
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h localhost -p$$MYSQL_ROOT_PASSWORD"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 30s

volumes:
  mysql-data:
```

### 7.3 Variables de entorno

```dotenv
MYSQL_ROOT_PASSWORD=<password-root>
MYSQL_DATABASE=appbelmonte
MYSQL_USER=appbelmonte
MYSQL_PASSWORD=<password-app>
DB_USERNAME=appbelmonte
DB_PASSWORD=<password-app>
DB_URL=jdbc:mysql://mysql:3306/appbelmonte?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
EUREKA_URL=http://eureka-server:8761/eureka/
```

`.env` debe agregarse a `.gitignore`. `.env.example` debe contener solo
placeholders.

### 7.4 Ventajas

- Version de MySQL fijada y reproducible.
- Aislamiento respecto de MySQL/MariaDB del host.
- Inicializacion automatica de bases y usuarios.
- Volumen persistente y facil de respaldar.
- Healthcheck antes de arrancar servicios.
- Funciona igual en Linux, macOS y Windows con Docker.
- Facilita CI/CD y limpieza con `docker compose down -v`.
- Menos pasos manuales para un nuevo integrante.

### 7.5 Desventajas

- Requiere Docker y consume mas recursos que XAMPP.
- Hay que gestionar volumen, respaldos y permisos.
- Los secretos deben salir de Compose y pasar a `.env`.
- La primera migracion requiere probar una base limpia.
- El mapeo de puertos puede chocar con MySQL local.
- MySQL 8 usa `caching_sha2_password`; para clientes locales sin TLS se necesita
  `allowPublicKeyRetrieval=true`.
- En Windows/WSL pueden aparecer diferencias de rutas y permisos.

## 8. Comparacion directa

| Criterio | XAMPP/MariaDB | MySQL 8.4 Docker | Mejor opcion |
|---|---|---|---|
| Instalacion | Manual en el host | `docker compose up` | Docker |
| Reproducibilidad | Baja | Alta | Docker |
| Version del motor | Ligada al host | Fijada en la imagen | Docker |
| Aislamiento | Bajo | Alto | Docker |
| Conflictos de puertos | Frecuentes | Configurables | Docker |
| Persistencia | Datadir del host | Volumen Docker | Empate |
| Inicializacion de bases | Manual | Script automatico | Docker |
| phpMyAdmin | Incluido | Se puede agregar | XAMPP |
| Consumo de recursos | Menor | Mayor | XAMPP |
| Curva de aprendizaje | Baja | Media | XAMPP |
| CI/CD | Limitado | Natural | Docker |
| Dependencia del sistema operativo | Alta | Baja | Docker |
| Adecuado para el curso | Si | Si, si se permite Docker | Empate |

## 9. Plan de migracion

La migracion no se ejecuta hasta completar la validacion REST y las pruebas.

### Fase 0: linea base

- Probar todos los metodos REST en Postman.
- Ejecutar `mvn test` en los modulos con pruebas.
- Registrar resultados y errores.
- Confirmar que el flujo actual con XAMPP funciona.

Criterio de salida: flujo REST validado y pruebas en verde.

### Fase 1: respaldo

- Exportar datos si se quieren conservar:
  ```bash
  mysqldump -h127.0.0.1 -P3307 -uappbelmonte -p \
    --databases appbelmonte appbelmonte_plantilla > backup.sql
  ```
- Guardar el respaldo fuera del repositorio.
- Si solo se usan datos de prueba de Flyway, se puede partir de una base limpia.

### Fase 2: variables de entorno

- Crear `.env.example` con placeholders.
- Crear `.env` local.
- Agregar `.env` a `.gitignore`.
- Parametrizar `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `EUREKA_URL`.
- Evitar credenciales literales en `docker-compose.yml`.

### Fase 3: servicio MySQL

- Agregar `mysql:8.4` a `docker-compose.yml`.
- Agregar volumen `mysql-data`.
- Montar `scripts/init-mysql.sql` como inicializacion.
- Agregar `healthcheck`.
- Mapear `3307:3306` solo si se necesita acceso desde el host.

### Fase 4: cambiar los servicios

- Cambiar `DB_URL` a `jdbc:mysql://mysql:3306/appbelmonte`.
- Agregar `depends_on` con `condition: service_healthy`.
- Eliminar `host.docker.internal` si ya no se usa XAMPP.
- Mantener `restart: unless-stopped`.
- Verificar que `plantilla-servicio` siga fuera de Compose.

### Fase 5: documentacion

- Actualizar `README.md`.
- Actualizar `docs/Sumario_Final.md`.
- Enlazar este documento.
- Corregir referencias a `3306`, `root` y XAMPP.

### Fase 6: validacion desde cero

```bash
docker compose down -v
docker compose up -d --build
docker compose ps
```

Validar:

- MySQL saludable.
- 16 aplicaciones registradas en Eureka.
- `GET http://localhost:8080/api/regiones` responde `200`.
- Flujo completo: login, solicitud, asignacion de viaje, inicio, fin e historial.
- `mvn test` en los modulos con pruebas.
- Persistencia tras `docker compose down` y `up` sin `-v`.

### Fase 7: cierre

- Documentar version de MySQL y decisiones.
- Confirmar que un clon nuevo levanta sin pasos manuales de base de datos.
- Confirmar que no hay secretos versionados.

## 10. Criterios de aceptacion

- [ ] `docker compose config` no muestra errores.
- [ ] `docker compose up -d --build` levanta MySQL y los 17 contenedores de
      aplicacion (18 contenedores en total).
- [ ] MySQL 8.4 responde y pasa el `healthcheck`.
- [ ] Las bases se crean desde el script de inicializacion.
- [ ] Los 16 modulos aparecen en Eureka.
- [ ] `GET /api/regiones` responde `200`.
- [ ] Todos los metodos REST se prueban en Postman.
- [ ] Las 23 pruebas unitarias pasan.
- [ ] Los datos persisten tras reiniciar sin borrar el volumen.
- [ ] `.env` no esta versionado.
- [ ] README y Sumario reflejan MySQL Docker.

## 11. Runbook operativo

### Levantar

```bash
docker compose up -d
```

### Detener sin borrar datos

```bash
docker compose down
```

### Detener y borrar datos

```bash
docker compose down -v
```

Usar `-v` solo en pruebas de base limpia.

### Ver logs

```bash
docker compose logs -f mysql
docker compose logs -f region-servicio
docker compose logs -f gateway
```

### Verificar Eureka

```bash
curl -s http://localhost:8761/eureka/apps
```

### Probar el gateway

```bash
curl http://localhost:8080/api/regiones
```

### Respaldo del volumen

```bash
docker compose exec mysql mysqldump -uroot -p \
  --databases appbelmonte appbelmonte_plantilla > backup.sql
```

## 12. Diagnostico rapido

| Error | Revision |
|---|---|
| `Connection refused` | MySQL no corre, puerto incorrecto o bind en loopback |
| `Access denied` | Usuario, host o password incorrectos |
| `Unknown database` | No se ejecuto el script de inicializacion |
| `Schema validation failed` | Migraciones Flyway no coinciden con entidades |
| `No servers available` | Servicio no registrado en Eureka |
| `503` persistente | Revisar logs del servicio de destino, no solo el gateway |
| `Port already in use` | Conflicto en `3306`, `3307`, `8080` u `8761` |
| MySQL no saludable | Revisar volumen, permisos y logs del contenedor |

## 13. Riesgos y mitigaciones

| Riesgo | Mitigacion |
|---|---|
| Perder datos al migrar | Respaldo con `mysqldump` y prueba previa |
| Conflicto con MySQL local | Mapear `3307:3306` o no publicar el puerto |
| Volumen con permisos incorrectos | Revisar UID/GID y logs de MySQL |
| Secretos en Git | `.env` ignorado y `.env.example` con placeholders |
| Diferencias MySQL/MariaDB | Validar Flyway y consultas en MySQL 8.4 |
| Base con tablas sin historial | Base limpia o baseline manual |
| Arranque lento | Healthcheck y espera de Eureka |
| Dependencia de Docker | Documentar alternativa XAMPP como respaldo |

## 14. Anexos

### A. Comandos XAMPP

```bash
sudo /opt/lampp/lampp startmysql
sudo /opt/lampp/lampp restartmysql
ss -ltnp | grep 3307
/opt/lampp/bin/mysql -u root -p
/opt/lampp/bin/mysql_upgrade -u root -p
```

### B. Comandos MySQL Docker

```bash
docker compose up -d
docker compose ps
docker compose logs -f mysql
docker compose down
docker compose down -v
```

### C. Versiones observadas

| Componente | Version |
|---|---|
| XAMPP | 8.2.12-0 |
| MariaDB XAMPP | 10.4.32 |
| MySQL validado | 8.4 |
| Java | 21 |
| Spring Boot | 4.1.2 |
| Spring Cloud | 2025.1.4 |
| Flyway | 12.4.0 |
| MySQL Connector/J | 9.7.0 |

### D. Archivos relacionados

- `README.md`: operacion general.
- `docs/Sumario_Final.md`: arquitectura y estado de entrega.
- `docker-compose.yml`: orquestacion actual y futura.
- `scripts/init-mysql.sql`: creacion de bases.
- `Dockerfile`: construccion de imagenes por servicio.
- `*/src/main/resources/application.properties`: datasource, Flyway y Eureka.

## 15. Decision

Se adopta MySQL 8.4 LTS en Docker Compose como entorno oficial del backend por
portabilidad, versionado y reproducibilidad. XAMPP/MariaDB queda documentado como
alternativa local y como referencia de las fricciones que motivaron el cambio.

La migracion se ejecutara despues de validar todos los metodos REST en Postman y
confirmar las pruebas automatizadas.
