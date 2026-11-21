CREATE TABLE estados_viaje (
	id BIGINT NOT NULL AUTO_INCREMENT,
	nombre VARCHAR(255) NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT uk_estados_viaje_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
