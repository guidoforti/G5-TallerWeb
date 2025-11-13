-- ========================================
-- POBLADO INICIAL DE DATOS DEL SISTEMA
-- (Alineado con Atributos de Usuario Base)
-- ========================================

-- 0️⃣ USUARIOS BASE (CONDUCTORES y VIAJEROS)
-- Nota: La edad es ahora calculada desde fecha_nacimiento. Usamos fechas para simular edades.
-- Edad de Conductores: ~35, ~40, ~28 años.
-- Edad de Viajeros: ~25, ~30, ~22 años.

-- CONDUCTORES (IDs 1 a 3)
INSERT INTO usuario (id, nombre, email, contrasenia, rol, activo, fecha_nacimiento, fumador, discapacitado)
VALUES (1, 'Carlos Perez', 'carlos@correo.com', '1234', 'CONDUCTOR', TRUE, '1990-05-15', FALSE, NULL),
       (2, 'Maria Lopez', 'maria@correo.com', 'abcd', 'CONDUCTOR', TRUE, '1985-11-20', FALSE, NULL),
       (3, 'Juan Garcia', 'juan@correo.com', 'pass', 'CONDUCTOR', TRUE, '1997-03-15', TRUE, 'Auditiva'); -- Juan es fumador

-- VIAJEROS (IDs 4 a 6)
INSERT INTO usuario (id, nombre, email, contrasenia, rol, activo, fecha_nacimiento, fumador, discapacitado)
VALUES (4, 'Sofia Torres', 'sofia@correo.com', 'pass1', 'VIAJERO', TRUE, '2000-08-20', FALSE, NULL),
       (5, 'Martin Diaz', 'martin@correo.com', 'pass2', 'VIAJERO', TRUE, '1995-10-10', TRUE, 'Movilidad reducida'), -- Martin es fumador
       (6, 'Lucia Fernandez', 'lucia@correo.com', 'pass3', 'VIAJERO', TRUE, '2003-01-01', FALSE, NULL);


-- 1️⃣ CIUDADES
INSERT INTO ciudad (id, nombre, latitud, longitud) VALUES (1, 'Buenos Aires', -34.6095579, -58.3887904);
INSERT INTO ciudad (id, nombre, latitud, longitud) VALUES (2, 'Córdoba', -31.4166867, -64.1834193);
INSERT INTO ciudad (id, nombre, latitud, longitud) VALUES (3, 'Rosario', -32.9593609, -60.6617024);

-- 2️⃣ TABLAS DE ROL ESPECÍFICAS
-- CONDUCTORES ESPECÍFICOS
INSERT INTO conductor (usuario_id, fecha_de_vencimiento_licencia)
VALUES (1, '2027-05-10'), 
       (2, '2026-11-20'), 
       (3, '2028-03-15');

-- VIAJEROS ESPECÍFICOS (La columna 'edad' ha sido eliminada. Insertamos solo la FK)
INSERT INTO viajero (usuario_id)
VALUES (4), 
       (5), 
       (6);


-- 3️⃣ VEHÍCULOS
INSERT INTO vehiculo (id, modelo, anio, asientos_totales, estado_verificacion, patente, conductor_id)
VALUES (1, 'Toyota Corolla', '2014', 5, 1, 'AB123CD', 1),
       (2, 'Volkswagen Gol', '2012', 4, 1, 'AC456EF', 2),
       (3, 'Peugeot 208', '2015', 5, 1, 'AD789GH', 3);

-- 4️⃣ VIAJES (Estado: 0=DISPONIBLE, 2=FINALIZADO, 4=EN_CURSO)
-- Viaje 1: DISPONIBLE (Futuro)
INSERT INTO viaje (id, asientos_disponibles, estado, fecha_de_creacion, fecha_hora_de_salida, precio, conductor_id, origen_id,
                   destino_id, vehiculo_id)
VALUES (1, 3, 0, CURRENT_TIMESTAMP, DATEADD('DAY', 5, CURRENT_TIMESTAMP), 15000, 1, 1, 2, 1);

-- Viaje 2: EN CURSO (Iniciado en el pasado reciente)
INSERT INTO viaje (id, asientos_disponibles, estado, fecha_de_creacion, fecha_hora_de_salida, precio, conductor_id, origen_id,
                   destino_id, vehiculo_id, fecha_hora_inicio_real)
VALUES (2, 0, 4, CURRENT_TIMESTAMP, DATEADD('HOUR', -1, CURRENT_TIMESTAMP), 12000, 2, 2, 3, 2, DATEADD('HOUR', -1, CURRENT_TIMESTAMP));

-- Viaje 3: FINALIZADO (Pasado)
INSERT INTO viaje (id, asientos_disponibles, estado, fecha_de_creacion, fecha_hora_de_salida, precio, conductor_id, origen_id,
                   destino_id, vehiculo_id, fecha_hora_fin_real)
VALUES (3, 4, 2, CURRENT_TIMESTAMP, DATEADD('DAY', -7, CURRENT_TIMESTAMP), 18000, 3, 3, 1, 3, CURRENT_TIMESTAMP);


-- 5️⃣ PARADAS (Mantenemos la lógica de paradas por viaje)
INSERT INTO parada (id, orden, ciudad_id, viaje_id)
VALUES (1, 1, 1, 1),
       (2, 2, 2, 1),
       (3, 1, 2, 2),
       (4, 2, 3, 2),
       (5, 1, 3, 3),
       (6, 2, 1, 3);

-- 6️⃣ RESERVAS
-- Dejamos la tabla vacía para que los tests de repositorio puedan crear sus propios datos de prueba
-- Si necesitas alguna reserva, añádela aquí, asegurando que los IDs referenciados existan.
-- Ejemplo de Reserva Confirmada para Viaje 3 (Finalizado):
INSERT INTO reserva (id, viaje_id, viajero_id, fecha_solicitud, estado, estado_pago, asistencia)
VALUES (1, 3, 4, CURRENT_TIMESTAMP, 1, 'PAGADO', 'PRESENTE');