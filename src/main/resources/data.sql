INSERT INTO Usuario(id, email, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true);

-- Insertar un conductor de prueba
-- Campos: id, nombre, email, contrasenia, fechaDeVencimientoLicencia
INSERT INTO conductor (id, nombre, email, contrasenia, fecha_de_vencimiento_licencia)
VALUES (1, 'Juan Pérez', 'juan.perez@unlam.edu.ar', 'password123', '2026-12-31');

-- Insertar varios vehículos para el conductor
-- Campos: id, patente, Modelo, anio, asientosTotales, estadoVerificacion (0=PENDIENTE, 1=VERIFICADO, 2=NO_CARGADO), conductor_id
INSERT INTO vehiculo (id, patente, Modelo, anio, asientos_totales, estado_verificacion, conductor_id)
VALUES (1, 'ABC123', 'Toyota Corolla', '2020', 5, 1, 1);

INSERT INTO vehiculo (id, patente, Modelo, anio, asientos_totales, estado_verificacion, conductor_id)
VALUES (2, 'DEF456', 'Honda Civic', '2019', 5, 1, 1);

INSERT INTO vehiculo (id, patente, Modelo, anio, asientos_totales, estado_verificacion, conductor_id)
VALUES (3, 'GHI789', 'Volkswagen Vento', '2021', 5, 1, 1);

INSERT INTO vehiculo (id, patente, Modelo, anio, asientos_totales, estado_verificacion, conductor_id)
VALUES (4, 'JKL012', 'Ford Focus', '2018', 5, 0, 1);

INSERT INTO vehiculo (id, patente, Modelo, anio, asientos_totales, estado_verificacion, conductor_id)
VALUES (5, 'MNO345', 'Chevrolet Cruze', '2022', 5, 1, 1);

-- Inserts para tu archivo data.sql (versión sin acentos)

-- 1. CIUDADES
INSERT INTO ciudad (nombre, latitud, longitud) VALUES ('Ramos Mejia', -34.65, -58.56);
INSERT INTO ciudad (nombre, latitud, longitud) VALUES ('Lujan', -34.57, -59.10);
INSERT INTO ciudad (nombre, latitud, longitud) VALUES ('Moron', -34.653, -58.619);


-- 2. CONDUCTOR
INSERT INTO conductor (nombre, email, contrasenia, fecha_de_vencimiento_licencia) VALUES ('Carlos Rodriguez', 'carlos.conductor@example.com', 'pass123', '2026-12-31');


-- 3. VEHICULO
-- NOTA: El estado 'estadoVerificacion' es un Enum. JPA por defecto lo guarda como un numero (ordinal).
-- Suponiendo que el primer valor del enum EstadoVerificacion es 'APROBADO' (indice 0).
INSERT INTO vehiculo (patente, modelo, anio, asientos_totales, estado_verificacion) VALUES ('AE789BC', 'Toyota Corolla', '2021', 4, 0);


-- 4. VIAJEROS
INSERT INTO viajero (nombre, edad, email, contrasenia) VALUES ('Ana Fuentes', 28, 'ana.viajera@example.com', 'pass456');
INSERT INTO viajero (nombre, edad, email, contrasenia) VALUES ('Luis Torres', 35, 'luis.viajero@example.com', 'pass789');


-- 5. VIAJE
-- El estado 'DISPONIBLE' es un Enum. Por defecto JPA lo guarda como un String.
INSERT INTO viaje (conductor_id, vehiculo_id, origen_id, destino_id, fecha_hora_de_salida, precio, asientos_disponibles, fecha_de_creacion, estado) VALUES (1, 1, 1, 2, '2025-11-10 08:30:00', 2500.00, 1, CURRENT_TIMESTAMP, 1);


-- 6. PARADAS
INSERT INTO parada (viaje_id, ciudad_id, orden) VALUES (1, 3, 1);


-- 7. TABLA INTERMEDIA (viaje_viajero)
INSERT INTO viaje_viajero (viaje_id, viajero_id) VALUES (1, 1);
INSERT INTO viaje_viajero (viaje_id, viajero_id) VALUES (1, 2);