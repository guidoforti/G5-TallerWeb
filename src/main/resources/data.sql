-- POBLADO DE DATOS DE PRUEBA (JOINED INHERITANCE)

-- 1️⃣ USUARIO ADMINISTRADOR (ID 1)
INSERT INTO usuario (id, email, contrasenia, nombre, rol, activo)
VALUES (1, 'test@unlam.edu.ar', 'test', 'Admin Test', 'ADMIN', TRUE);

-- 2️⃣ USUARIO CONDUCTOR (ID 2)
-- La inserción debe ir primero a la tabla base
INSERT INTO usuario (id, email, contrasenia, nombre, rol, activo)
VALUES (2, 'juan.perez@unlam.edu.ar', 'password123', 'Juan Pérez', 'CONDUCTOR', TRUE);

-- 3️⃣ CONDUCTOR ESPECÍFICO (JOINED)
-- Se inserta en la tabla específica usando el ID del usuario conductor (ID 2)
-- CAMBIO: fechaDeVencimientoLicencia → fecha_de_vencimiento_licencia
INSERT INTO conductor (usuario_id, fecha_de_vencimiento_licencia)
VALUES (2, '2026-12-31');

-- 4️⃣ VEHÍCULOS (Referenciando el conductor_id=2)
-- CAMBIO: asientosTotales → asientos_totales
-- CAMBIO: estadoVerificacion → estado_verificacion
-- CAMBIO: Modelo → modelo (para mantener consistencia)
INSERT INTO vehiculo (id, patente, modelo, anio, asientos_totales, estado_verificacion, conductor_id)
VALUES (1, 'ABC123', 'Toyota Corolla', '2020', 5, 1, 2);

INSERT INTO vehiculo (id, patente, modelo, anio, asientos_totales, estado_verificacion, conductor_id)
VALUES (2, 'DEF456', 'Honda Civic', '2019', 5, 1, 2);

INSERT INTO vehiculo (id, patente, modelo, anio, asientos_totales, estado_verificacion, conductor_id)
VALUES (3, 'GHI789', 'Volkswagen Vento', '2021', 5, 1, 2);

INSERT INTO vehiculo (id, patente, modelo, anio, asientos_totales, estado_verificacion, conductor_id)
VALUES (4, 'JKL012', 'Ford Focus', '2018', 5, 0, 2);

INSERT INTO vehiculo (id, patente, modelo, anio, asientos_totales, estado_verificacion, conductor_id)
VALUES (5, 'MNO345', 'Chevrolet Cruze', '2022', 5, 1, 2);

-- 5️⃣ USUARIO VIAJERO (ID 3)
-- La inserción debe ir primero a la tabla base
INSERT INTO usuario (id, email, contrasenia, nombre, rol, activo)
VALUES (3, 'maria.gomez@unlam.edu.ar', 'viajero123', 'María Gómez', 'VIAJERO', TRUE);

-- 6️⃣ VIAJERO ESPECÍFICO (JOINED)
-- Se inserta en la tabla específica usando el ID del usuario viajero (ID 3)
INSERT INTO viajero (usuario_id, edad)
VALUES (3, 28);

-- 7️⃣ CIUDADES (Coordinates from Nominatim API)
INSERT INTO ciudad (id, nombre, latitud, longitud)
VALUES (1, 'Buenos Aires', -34.6095579, -58.3887904);

INSERT INTO ciudad (id, nombre, latitud, longitud)
VALUES (2, 'Córdoba', -31.4166867, -64.1834193);

INSERT INTO ciudad (id, nombre, latitud, longitud)
VALUES (3, 'Rosario', -32.9593609, -60.6617024);

INSERT INTO ciudad (id, nombre, latitud, longitud)
VALUES (4, 'Ciudad de Mendoza', -32.8894155, -68.8446177);

-- 8️⃣ VIAJES DE PRUEBA
-- Estado: 0=DISPONIBLE, 1=COMPLETO, 2=FINALIZADO, 3=CANCELADO
-- Viaje 1: Buenos Aires → Córdoba (DISPONIBLE, futuro)
INSERT INTO viaje (id, origen_id, destino_id, conductor_id, vehiculo_id, fecha_hora_de_salida, asientos_disponibles, precio, estado, fecha_de_creacion)
VALUES (1, 1, 2, 2, 1, '2025-10-30 08:00:00', 3, 5000.00, 0, CURRENT_TIMESTAMP);

-- Viaje 2: Buenos Aires → Rosario (DISPONIBLE, futuro)
INSERT INTO viaje (id, origen_id, destino_id, conductor_id, vehiculo_id, fecha_hora_de_salida, asientos_disponibles, precio, estado, fecha_de_creacion)
VALUES (2, 1, 3, 2, 2, '2025-11-01 10:00:00', 4, 3500.00, 0, CURRENT_TIMESTAMP);

-- Viaje 3: Córdoba → Buenos Aires (COMPLETO, futuro)
INSERT INTO viaje (id, origen_id, destino_id, conductor_id, vehiculo_id, fecha_hora_de_salida, asientos_disponibles, precio, estado, fecha_de_creacion)
VALUES (3, 2, 1, 2, 3, '2025-11-05 14:00:00', 0, 4800.00, 1, CURRENT_TIMESTAMP);

-- Viaje 4: Buenos Aires → Ciudad de Mendoza (DISPONIBLE, futuro, más caro)
INSERT INTO viaje (id, origen_id, destino_id, conductor_id, vehiculo_id, fecha_hora_de_salida, asientos_disponibles, precio, estado, fecha_de_creacion)
VALUES (4, 1, 4, 2, 5, '2025-11-10 06:00:00', 2, 12000.00, 0, CURRENT_TIMESTAMP);

-- Viaje 5: Buenos Aires → Córdoba (FINALIZADO, pasado)
INSERT INTO viaje (id, origen_id, destino_id, conductor_id, vehiculo_id, fecha_hora_de_salida, asientos_disponibles, precio, estado, fecha_de_creacion)
VALUES (5, 1, 2, 2, 1, '2025-10-20 09:00:00', 0, 4500.00, 2, CURRENT_TIMESTAMP);

-- 9️⃣ RESERVAS DE PRUEBA
-- Estados: 0=PENDIENTE, 1=CONFIRMADA, 2=RECHAZADA, 3=CANCELADA_POR_VIAJERO
-- Reserva del viajero María Gómez (ID 3) para algunos viajes
INSERT INTO reserva (id, viaje_id, viajero_id, fecha_solicitud, estado, motivo_rechazo)
VALUES (1, 1, 3, '2025-10-25 10:00:00', 0, NULL);  -- PENDIENTE para viaje a Córdoba

INSERT INTO reserva (id, viaje_id, viajero_id, fecha_solicitud, estado, motivo_rechazo)
VALUES (2, 5, 3, '2025-10-19 15:00:00', 1, NULL);  -- CONFIRMADA para viaje finalizado
