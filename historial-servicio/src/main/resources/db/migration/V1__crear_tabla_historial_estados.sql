CREATE TABLE historial_estados (
	id BIGINT NOT NULL AUTO_INCREMENT,
	fecha_hora DATETIME(6) NOT NULL,
	detalle VARCHAR(255),
	viaje_id BIGINT NOT NULL,
	estado_anterior_id BIGINT,
	estado_nuevo_id BIGINT NOT NULL,
	cambiado_por_id BIGINT NOT NULL,
	PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
