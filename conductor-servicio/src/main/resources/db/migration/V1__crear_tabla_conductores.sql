CREATE TABLE conductores (
	id BIGINT NOT NULL,
	numero_licencia VARCHAR(255) NOT NULL,
	disponible BIT(1) NOT NULL DEFAULT b'1',
	vehiculo_id BIGINT NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT uk_conductores_numero_licencia UNIQUE (numero_licencia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
