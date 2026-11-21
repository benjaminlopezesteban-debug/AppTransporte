CREATE TABLE vehiculos (
	id BIGINT NOT NULL AUTO_INCREMENT,
	patente VARCHAR(255) NOT NULL,
	anio INTEGER,
	modelo_id BIGINT NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT uk_vehiculos_patente UNIQUE (patente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
