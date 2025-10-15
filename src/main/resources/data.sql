INSERT INTO Usuario(id, email, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true);

-- Insertar un conductor de prueba
-- Campos: id, nombre, email, contrasenia, fechaDeVencimientoLicencia
INSERT INTO conductor (id, nombre, email, contrasenia, fechaDeVencimientoLicencia)
VALUES (1, 'Juan Pérez', 'juan.perez@unlam.edu.ar', 'password123', '2026-12-31');

-- Insertar varios vehículos para el conductor
-- Campos: id, patente, Modelo, anio, asientosTotales, estadoVerificacion (0=PENDIENTE, 1=VERIFICADO, 2=NO_CARGADO), conductor_id
INSERT INTO vehiculo (id, patente, Modelo, anio, asientosTotales, estadoVerificacion, conductor_id)
VALUES (1, 'ABC123', 'Toyota Corolla', '2020', 5, 1, 1);

INSERT INTO vehiculo (id, patente, Modelo, anio, asientosTotales, estadoVerificacion, conductor_id)
VALUES (2, 'DEF456', 'Honda Civic', '2019', 5, 1, 1);

INSERT INTO vehiculo (id, patente, Modelo, anio, asientosTotales, estadoVerificacion, conductor_id)
VALUES (3, 'GHI789', 'Volkswagen Vento', '2021', 5, 1, 1);

INSERT INTO vehiculo (id, patente, Modelo, anio, asientosTotales, estadoVerificacion, conductor_id)
VALUES (4, 'JKL012', 'Ford Focus', '2018', 5, 0, 1);

INSERT INTO vehiculo (id, patente, Modelo, anio, asientosTotales, estadoVerificacion, conductor_id)
VALUES (5, 'MNO345', 'Chevrolet Cruze', '2022', 5, 1, 1);



-- 1. Insertar Ciudades
INSERT INTO ciudad (nombre, latitud, longitud) VALUES
                                                   ('Buenos Aires', -34.6037, -58.3816),
                                                   ('La Plata', -34.9205, -57.9536),
                                                   ('Quilmes', -34.7242, -58.2526),
                                                   ('Berazategui', -34.7629, -58.2138);

-- 2. Insertar un Usuario (Conductor)
INSERT INTO usuario (nombre, email, password, rol, activo, fecha_de_creacion)
VALUES ('Juan Perez', 'juan.perez@example.com', 'hashed_password', 'CONDUCTOR', TRUE, CURRENT_TIMESTAMP);

-- 3. Obtener el ID del conductor recién insertado
-- (En un script real, este ID se obtendría dinámicamente)
-- SET @conductor_id = LAST_INSERT_ID();

-- 4. Insertar un Vehículo
INSERT INTO vehiculo (marca, modelo, anio, patente, color, asientos_disponibles, conductor_id)
VALUES ('Toyota', 'Corolla', 2020, 'ABC123', 'Blanco', 4, 1); -- Ajusta el conductor_id según corresponda

-- 5. Insertar un Viaje
INSERT INTO viaje (conductor_id, vehiculo_id, origen_id, destino_id, fecha_hora_de_salida, precio, asientos_disponibles, fecha_de_creacion, estado)
VALUES (
           1, -- ID del conductor
           1, -- ID del vehículo
           1, -- ID de la ciudad de origen (Buenos Aires)
           2, -- ID de la ciudad de destino (La Plata)
           '2025-10-15 15:00:00', -- Fecha y hora de salida
           500.00, -- Precio
           4, -- Asientos disponibles
           CURRENT_TIMESTAMP, -- Fecha de creación
           'PENDIENTE' -- Estado del viaje
       );

-- 6. Insertar Viajeros (opcional, si ya existen)
INSERT INTO usuario (nombre, email, password, rol, activo, fecha_de_creacion)
VALUES
    ('Maria Gonzalez', 'maria@example.com', 'hashed_password', 'VIAJERO', TRUE, CURRENT_TIMESTAMP),
    ('Carlos Lopez', 'carlos@example.com', 'hashed_password', 'VIAJERO', TRUE, CURRENT_TIMESTAMP);

-- 7. Insertar relación entre Viaje y Viajeros (tabla intermedia)
INSERT INTO viaje_viajero (viaje_id, viajero_id)
VALUES
    (1, 2), -- Asumiendo que el viaje es ID 1 y el viajero 1 es ID 2
    (1, 3); -- Asumiendo que el viajero 2 es ID 3

-- 8. Insertar Paradas (opcional)
-- Primero necesitamos el ID del viaje recién creado
-- SET @viaje_id = LAST_INSERT_ID();

-- Insertar paradas
INSERT INTO parada (ciudad_id, viaje_id, orden)
VALUES
    (3, 1, 1), -- Quilmes como primera parada
    (4, 1, 2); -- Berazategui como segunda parada