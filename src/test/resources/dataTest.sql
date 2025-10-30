 -- ========================================
-- POBLADO INICIAL DE DATOS DEL SISTEMA (JOINED INHERITANCE)
-- ========================================

-- 0️⃣ USUARIOS BASE (CONDUCTORES y VIAJEROS)
-- NOTA: Asignamos IDs grandes para evitar colisiones con futuras inserciones manuales si usas ID autoincrementales.
-- La columna 'nombre' se ha movido a la tabla USUARIO.

-- CONDUCTORES (IDs 1 a 3)
INSERT INTO usuario (id, nombre, email, contrasenia, rol, activo)
VALUES (1, 'Carlos Perez', 'carlos@correo.com', '1234', 'CONDUCTOR', TRUE),
       (2, 'Maria Lopez', 'maria@correo.com', 'abcd', 'CONDUCTOR', TRUE),
       (3, 'Juan Garcia', 'juan@correo.com', 'pass', 'CONDUCTOR', TRUE);

-- VIAJEROS (IDs 4 a 6)
INSERT INTO usuario (id, nombre, email, contrasenia, rol, activo)
VALUES (4, 'Sofia Torres', 'sofia@correo.com', 'pass1', 'VIAJERO', TRUE),
       (5, 'Martin Diaz', 'martin@correo.com', 'pass2', 'VIAJERO', TRUE),
       (6, 'Lucia Fernandez', 'lucia@correo.com', 'pass3', 'VIAJERO', TRUE);


-- 1️⃣ CIUDADES
INSERT INTO ciudad (id, nombre, latitud, longitud)
VALUES (1, 'Buenos Aires', -34.6037, -58.3816),
       (2, 'Cordoba', -31.4201, -64.1888),
       (3, 'Rosario', -32.9587, -60.6930);

-- 2️⃣ TABLAS DE ROL ESPECÍFICAS
-- La columna ID aquí debe llamarse 'usuario_id' y ser el mismo ID de la tabla usuario.

-- CONDUCTORES ESPECÍFICOS (IDs 1 a 3)
INSERT INTO conductor (usuario_id, fecha_de_vencimiento_licencia)
VALUES (1, '2027-05-10'), -- Corresponde a Carlos Perez (id=1)
       (2, '2026-11-20'), -- Corresponde a Maria Lopez (id=2)
       (3, '2028-03-15'); -- Corresponde a Juan Garcia (id=3)

-- VIAJEROS ESPECÍFICOS (IDs 4 a 6)
INSERT INTO viajero (usuario_id, edad)
VALUES (4, 25), -- Corresponde a Sofia Torres (id=4)
       (5, 30), -- Corresponde a Martin Diaz (id=5)
       (6, 22); -- Corresponde a Lucia Fernandez (id=6)


-- 3️⃣ VEHÍCULOS (El conductor_id se mantiene igual)
INSERT INTO vehiculo (id, modelo, anio, asientos_totales, estado_verificacion, patente, conductor_id)
VALUES (1, 'Toyota Corolla', '2014', 5, 1, 'AB123CD', 1),
       (2, 'Volkswagen Gol', '2012', 4, 1, 'AC456EF', 2),
       (3, 'Peugeot 208', '2015', 5, 1, 'AD789GH', 3);

-- 4️⃣ VIAJES
-- Los IDs de conductor siguen siendo los mismos de la tabla usuario (1, 2, 3)
INSERT INTO viaje (id, asientos_disponibles, estado, fecha_de_creacion, fecha_hora_de_salida, precio, conductor_id, origen_id,
                   destino_id, vehiculo_id)
VALUES (1, 3, 0, '2025-10-01 10:00:00', '2025-10-05 08:00:00', 15000, 1, 1, 2, 1),
       (2, 2, 0, '2025-09-20 14:00:00', '2025-09-25 07:30:00', 12000, 2, 2, 3, 2),
       (3, 4, 0, '2025-08-10 09:30:00', '2025-08-15 06:45:00', 18000, 3, 3, 1, 3);

-- 5️⃣ PARADAS
INSERT INTO parada (id, orden, ciudad_id, viaje_id)
VALUES (1, 1, 1, 1),
       (2, 2, 2, 1),
       (3, 1, 2, 2),
       (4, 2, 3, 2),
       (5, 1, 3, 3),
       (6, 2, 1, 3);

-- 6️⃣ RESERVAS
-- Dejamos la tabla vacía para que los tests de repositorio puedan crear sus propios datos de prueba
-- Los tests necesitan un estado limpio para verificar correctamente las operaciones CRUD