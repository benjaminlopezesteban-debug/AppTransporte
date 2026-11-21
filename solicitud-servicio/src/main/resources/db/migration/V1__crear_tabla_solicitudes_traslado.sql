CREATE TABLE solicitudes_traslado (
	id BIGINT NOT NULL AUTO_INCREMENT,
	fecha DATE NOT NULL,
	hora TIME(6) NOT NULL,
	numero_vuelo VARCHAR(255),
	observaciones VARCHAR(255),
	fecha_creacion DATETIME(6) NOT NULL,
	solicitante_id BIGINT NOT NULL,
	pasajero_id BIGINT NOT NULL,
	origen_id BIGINT NOT NULL,
	destino_id BIGINT NOT NULL,
	estado_id BIGINT NOT NULL,
	PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
