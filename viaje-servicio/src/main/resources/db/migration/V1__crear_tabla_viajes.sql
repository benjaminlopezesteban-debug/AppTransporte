CREATE TABLE viajes (
	id BIGINT NOT NULL AUTO_INCREMENT,
	fecha_asignacion DATETIME(6) NOT NULL,
	hora_inicio_real DATETIME(6),
	hora_fin_real DATETIME(6),
	solicitud_id BIGINT NOT NULL,
	conductor_id BIGINT NOT NULL,
	asignado_por_id BIGINT NOT NULL,
	estado_id BIGINT NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT uk_viajes_solicitud UNIQUE (solicitud_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
