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

