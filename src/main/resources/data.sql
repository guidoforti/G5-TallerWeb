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
INSERT INTO conductor (usuario_id, fechaDeVencimientoLicencia)
VALUES (2, '2026-12-31');

-- 4️⃣ VEHÍCULOS (Referenciando el conductor_id=2)
-- La clave foránea conductor_id en vehiculo apunta al ID de la tabla USUARIO/CONDUCTOR (ID 2)
INSERT INTO vehiculo (id, patente, Modelo, anio, asientosTotales, estadoVerificacion, conductor_id)
VALUES (1, 'ABC123', 'Toyota Corolla', '2020', 5, 1, 2);

INSERT INTO vehiculo (id, patente, Modelo, anio, asientosTotales, estadoVerificacion, conductor_id)
VALUES (2, 'DEF456', 'Honda Civic', '2019', 5, 1, 2);

INSERT INTO vehiculo (id, patente, Modelo, anio, asientosTotales, estadoVerificacion, conductor_id)
VALUES (3, 'GHI789', 'Volkswagen Vento', '2021', 5, 1, 2);

INSERT INTO vehiculo (id, patente, Modelo, anio, asientosTotales, estadoVerificacion, conductor_id)
VALUES (4, 'JKL012', 'Ford Focus', '2018', 5, 0, 2);

INSERT INTO vehiculo (id, patente, Modelo, anio, asientosTotales, estadoVerificacion, conductor_id)
VALUES (5, 'MNO345', 'Chevrolet Cruze', '2022', 5, 1, 2);