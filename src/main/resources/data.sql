-- =================================================================================================
-- 1. ENTIDADES BASE (USUARIO, CONDUCTOR, VIAJERO) - REQUERIDAS POR VIAJE
-- =================================================================================================
-- 1.1 ADMINISTRADOR (ID 1)
INSERT INTO usuario (id, email, contrasenia, nombre, rol, activo, fecha_nacimiento, fumador, discapacitado)
VALUES (1, 'test@unlam.edu.ar', 'test', 'Admin Test', 'ADMIN', TRUE, '1990-01-01', FALSE, NULL);

-- 1.2 CONDUCTOR JUAN PÉREZ (ID 2)
INSERT INTO usuario (id, email, contrasenia, nombre, rol, activo, fecha_nacimiento, fumador, discapacitado)
VALUES (2, 'juan.perez@unlam.edu.ar', 'password123', 'Juan Pérez', 'CONDUCTOR', TRUE, '1995-05-15', FALSE, NULL);
INSERT INTO conductor (usuario_id, fecha_de_vencimiento_licencia)
VALUES (2, '2026-12-31');

-- 1.3 VIAJERO MARÍA GÓMEZ (ID 3)
INSERT INTO usuario (id, email, contrasenia, nombre, rol, activo, fecha_nacimiento, fumador, discapacitado)
VALUES (3, 'maria.gomez@unlam.edu.ar', 'viajero123', 'María Gómez', 'VIAJERO', TRUE, '1997-08-20', TRUE, 'Movilidad reducida');
INSERT INTO viajero (usuario_id)
VALUES (3);

-- 2. ENTIDADES INTERMEDIAS (CIUDADES, VEHÍCULOS) - REQUERIDAS POR VIAJE
-- 2.1 CIUDADES
INSERT INTO ciudad (id, nombre, latitud, longitud) VALUES (1, 'Buenos Aires', -34.6095579, -58.3887904);
INSERT INTO ciudad (id, nombre, latitud, longitud) VALUES (2, 'Córdoba', -31.4166867, -64.1834193);
INSERT INTO ciudad (id, nombre, latitud, longitud) VALUES (3, 'Rosario', -32.9593609, -60.6617024);
INSERT INTO ciudad (id, nombre, latitud, longitud) VALUES (4, 'Ciudad de Mendoza', -32.8894155, -68.8446177);

-- 2.2 VEHÍCULOS (Referencian a CONDUCTOR ID 2)
INSERT INTO vehiculo (id, patente, modelo, anio, asientos_totales, estado_verificacion, conductor_id) VALUES (1, 'ABC123', 'Toyota Corolla', '2020', 5, 1, 2);
INSERT INTO vehiculo (id, patente, modelo, anio, asientos_totales, estado_verificacion, conductor_id) VALUES (2, 'DEF456', 'Honda Civic', '2019', 5, 1, 2);
INSERT INTO vehiculo (id, patente, modelo, anio, asientos_totales, estado_verificacion, conductor_id) VALUES (3, 'GHI789', 'Volkswagen Vento', '2021', 5, 1, 2);
INSERT INTO vehiculo (id, patente, modelo, anio, asientos_totales, estado_verificacion, conductor_id) VALUES (4, 'JKL012', 'Ford Focus', '2018', 5, 0, 2);
INSERT INTO vehiculo (id, patente, modelo, anio, asientos_totales, estado_verificacion, conductor_id) VALUES (5, 'MNO345', 'Chevrolet Cruze', '2022', 5, 1, 2);


-- 3. ENTIDAD VIAJE (REQUERIDA POR RESERVA)
-- Estados: 0=DISPONIBLE, 1=COMPLETO, 2=FINALIZADO, 3=CANCELADO, 4=EN_CURSO

-- Viaje 1: Buenos Aires → Córdoba (DISPONIBLE, futuro)
INSERT INTO viaje (id, origen_id, destino_id, conductor_id, vehiculo_id, fecha_hora_de_salida, asientos_disponibles, precio, estado, fecha_de_creacion, duracion_estimada_minutos, version)
VALUES (1, 1, 2, 2, 1, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 5 DAY), 3, 5000.00, 0, CURRENT_TIMESTAMP, 838, 0);

-- Viaje 2: Buenos Aires → Rosario (DISPONIBLE, futuro)
INSERT INTO viaje (id, origen_id, destino_id, conductor_id, vehiculo_id, fecha_hora_de_salida, asientos_disponibles, precio, estado, fecha_de_creacion, duracion_estimada_minutos, version)
VALUES (2, 1, 3, 2, 2, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY), 4, 3500.00, 0, CURRENT_TIMESTAMP, 386, 0);

-- Viaje 3: Córdoba → Buenos Aires (COMPLETO, futuro)
INSERT INTO viaje (id, origen_id, destino_id, conductor_id, vehiculo_id, fecha_hora_de_salida, asientos_disponibles, precio, estado, fecha_de_creacion, duracion_estimada_minutos, version)
VALUES (3, 2, 1, 2, 3, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 10 DAY), 0, 4800.00, 1, CURRENT_TIMESTAMP, 838, 0);

-- Viaje 4: Buenos Aires → Mendoza (DISPONIBLE, futuro)
INSERT INTO viaje (id, origen_id, destino_id, conductor_id, vehiculo_id, fecha_hora_de_salida, asientos_disponibles, precio, estado, fecha_de_creacion, duracion_estimada_minutos, version)
VALUES (4, 1, 4, 2, 5, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 15 DAY), 2, 12000.00, 0, CURRENT_TIMESTAMP, 1349, 0);

-- Viaje 5: Buenos Aires → Córdoba (FINALIZADO, pasado)
INSERT INTO viaje (id, origen_id, destino_id, conductor_id, vehiculo_id, fecha_hora_de_salida, asientos_disponibles, precio, estado, fecha_de_creacion, duracion_estimada_minutos, version, fecha_hora_fin_real)
VALUES (5, 1, 2, 2, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY), 0, 4500.00, 2, CURRENT_TIMESTAMP, 838, 0, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY));


-- 4. ENTIDAD RESERVA (Depende de VIAJE y VIAJERO)
-- Estados: 0=PENDIENTE, 1=CONFIRMADA, 2=RECHAZADA

-- Reserva 1 (Viaje 1 - DISPONIBLE, futuro) - Pendiente
INSERT INTO reserva (id, viaje_id, viajero_id, fecha_solicitud, estado, motivo_rechazo, estado_pago, asistencia)
VALUES (1, 1, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 0, NULL, 'NO_PAGADO', 'NO_MARCADO');

-- Reserva 2 (Viaje 5 - FINALIZADO, pasado) - Confirmada y completada
INSERT INTO reserva (id, viaje_id, viajero_id, fecha_solicitud, estado, motivo_rechazo, estado_pago, asistencia)
VALUES (2, 5, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 DAY), 1, NULL, 'PAGADO', 'PRESENTE');

-- Reserva 3 (Viaje 2 - DISPONIBLE, futuro) - Pendiente
INSERT INTO reserva (id, viaje_id, viajero_id, fecha_solicitud, estado, motivo_rechazo, estado_pago, asistencia)
VALUES (3, 2, 3, CURRENT_TIMESTAMP, 0, NULL, 'NO_PAGADO', 'NO_MARCADO');