-- ========================================
-- POBLADO INICIAL DE DATOS DEL SISTEMA
-- ========================================

-- 1️⃣ CIUDADES
INSERT INTO ciudad (id, nombre, latitud, longitud)
VALUES (1, 'Buenos Aires', -34.6037, -58.3816),
       (2, 'Cordoba', -31.4201, -64.1888),
       (3, 'Rosario', -32.9587, -60.6930);

-- 2️⃣ CONDUCTORES
INSERT INTO conductor (id, nombre, email, contrasenia, fechaDeVencimientoLicencia)
VALUES (1, 'Carlos Perez', 'carlos@correo.com', '1234', '2027-05-10'),
       (2, 'Maria Lopez', 'maria@correo.com', 'abcd', '2026-11-20'),
       (3, 'Juan Garcia', 'juan@correo.com', 'pass', '2028-03-15');

-- 3️⃣ VEHÍCULOS
INSERT INTO vehiculo (id, Modelo, anio, asientosTotales, estadoVerificacion, patente, conductor_id)
VALUES (1, 'Toyota Corolla', '2014', 5, 1, 'AB123CD', 1),
       (2, 'Volkswagen Gol', '2012', 4, 1, 'AC456EF', 2),
       (3, 'Peugeot 208', '2015', 5, 1, 'AD789GH', 3);

-- 4️⃣ VIAJEROS
INSERT INTO viajero (id, nombre, edad)
VALUES (1, 'Sofia Torres', 25),
       (2, 'Martin Diaz', 30),
       (3, 'Lucia Fernandez', 22);

-- 5️⃣ VIAJES
INSERT INTO Viaje (id, asientosDisponibles, estado, fechaDeCreacion, fechaHoraDeSalida, precio, conductor_id, origen_id,
                   destino_id, vehiculo_id)
VALUES (1, 3, 0, '2025-10-01 10:00:00', '2025-10-05 08:00:00', 15000, 1, 1, 2, 1),
       (2, 2, 0, '2025-09-20 14:00:00', '2025-09-25 07:30:00', 12000, 2, 2, 3, 2),
       (3, 4, 0, '2025-08-10 09:30:00', '2025-08-15 06:45:00', 18000, 3, 3, 1, 3);

-- 6️⃣ PARADAS
INSERT INTO parada (id, orden, ciudad_id, viaje_id)
VALUES (1, 1, 1, 1),
       (2, 2, 2, 1),
       (3, 1, 2, 2),
       (4, 2, 3, 2),
       (5, 1, 3, 3),
       (6, 2, 1, 3);

-- 7️⃣ VIAJE_VIAJERO
INSERT INTO viaje_viajero (viaje_id, viajero_id)
VALUES (1, 1),
       (1, 2),
       (2, 2),
       (2, 3),
       (3, 1),
       (3, 3);


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