package com.tallerwebi.punta_a_punta;

import java.io.IOException;

public class ReiniciarDB {
    public static void limpiarBaseDeDatos() {
        try {
            String dbHost = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
            String dbPort = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "3306";
            String dbName = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "tallerwebi";
            String dbUser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "user";
            String dbPassword = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "user";

            String sqlCommands = "SET FOREIGN_KEY_CHECKS=0;\n" +
                               "DELETE FROM valoracion;\n" +
                               "DELETE FROM reserva;\n" +
                               "DELETE FROM parada;\n" +
                               "DELETE FROM viaje;\n" +
                               "DELETE FROM vehiculo;\n" +
                               "DELETE FROM conductor;\n" +
                               "DELETE FROM viajero;\n" +
                               "DELETE FROM ciudad;\n" +
                               "DELETE FROM usuario;\n" +
                               "ALTER TABLE usuario AUTO_INCREMENT = 1;\n" +
                               "ALTER TABLE vehiculo AUTO_INCREMENT = 1;\n" +
                               "ALTER TABLE viaje AUTO_INCREMENT = 1;\n" +
                               "ALTER TABLE reserva AUTO_INCREMENT = 1;\n" +
                               "ALTER TABLE ciudad AUTO_INCREMENT = 1;\n" +
                               "ALTER TABLE valoracion AUTO_INCREMENT = 1;\n" +
                               "SET FOREIGN_KEY_CHECKS=1;\n" +
                               // Insert Conductor user
                               "INSERT INTO usuario(id, email, contrasenia, nombre, rol, activo, fecha_nacimiento, fumador, discapacitado) VALUES(1, 'conductor@test.com', 'test123', 'Test Conductor', 'CONDUCTOR', true, '1990-01-01', false, NULL);\n" +
                               "INSERT INTO conductor(usuario_id, fecha_de_vencimiento_licencia) VALUES(1, '2027-12-31');\n" +
                               // Insert Viajero user
                               "INSERT INTO usuario(id, email, contrasenia, nombre, rol, activo, fecha_nacimiento, fumador, discapacitado) VALUES(2, 'viajero@test.com', 'test123', 'Test Viajero', 'VIAJERO', true, '1995-05-15', false, NULL);\n" +
                               "INSERT INTO viajero(usuario_id) VALUES(2);\n" +
                               // Insert Cities (all from data.sql)
                               "INSERT INTO ciudad(id, nombre, latitud, longitud) VALUES(1, 'Buenos Aires', -34.6095579, -58.3887904);\n" +
                               "INSERT INTO ciudad(id, nombre, latitud, longitud) VALUES(2, 'Córdoba', -31.4166867, -64.1834193);\n" +
                               "INSERT INTO ciudad(id, nombre, latitud, longitud) VALUES(3, 'Rosario', -32.9593609, -60.6617024);\n" +
                               "INSERT INTO ciudad(id, nombre, latitud, longitud) VALUES(4, 'Ciudad de Mendoza', -32.8894155, -68.8446177);\n" +
                               // Insert Vehicle for conductor
                               "INSERT INTO vehiculo(id, conductor_id, patente, modelo, anio, asientos_totales, estado_verificacion) VALUES(1, 1, 'ABC123', 'Toyota Corolla', '2020', 4, 1);\n" +
                               // Insert available Viaje (3 days from now, 3 seats available) - For reservation tests
                               // Estado: 0=DISPONIBLE, 1=COMPLETO, 2=FINALIZADO, 3=CANCELADO, 4=EN_CURSO
                               "INSERT INTO viaje(id, conductor_id, vehiculo_id, origen_id, destino_id, fecha_hora_de_salida, precio, asientos_disponibles, duracion_estimada_minutos, estado, fecha_de_creacion, version) " +
                               "VALUES(1, 1, 1, 1, 2, DATE_ADD(NOW(), INTERVAL 3 DAY), 1500.00, 3, 838, 0, NOW(), 0);\n" +
                               // Insert pending reservation from viajero for this trip
                               // Estado: 0=PENDIENTE, 1=CONFIRMADA, 2=RECHAZADA, 3=CANCELADA_POR_VIAJERO
                               "INSERT INTO reserva(id, viaje_id, viajero_id, fecha_solicitud, estado, motivo_rechazo, estado_pago, asistencia) " +
                               "VALUES(1, 1, 2, NOW(), 0, NULL, 'NO_PAGADO', 'NO_MARCADO');\n" +
                               // Insert second Viaje (starting NOW) - For trip lifecycle tests
                               // This trip can be started immediately since departure time is now
                               "INSERT INTO viaje(id, conductor_id, vehiculo_id, origen_id, destino_id, fecha_hora_de_salida, precio, asientos_disponibles, duracion_estimada_minutos, estado, fecha_de_creacion, version) " +
                               "VALUES(2, 1, 1, 2, 1, NOW(), 2000.00, 4, 838, 0, NOW(), 0);\n" +
                               // Insert third Viaje (2 days from now, NO reservations) - For search/reserve tests (Test #2)
                               // Route: Buenos Aires → Rosario (UNIQUE route, no conflicts with other trips)
                               // This trip is available for viajero to search and reserve without conflicts
                               "INSERT INTO viaje(id, conductor_id, vehiculo_id, origen_id, destino_id, fecha_hora_de_salida, precio, asientos_disponibles, duracion_estimada_minutos, estado, fecha_de_creacion, version) " +
                               "VALUES(3, 1, 1, 1, 3, DATE_ADD(NOW(), INTERVAL 2 DAY), 1800.00, 4, 386, 0, NOW(), 0);";

            String comando = String.format(
                "docker exec tallerwebi-mysql mysql -h %s -P %s -u %s -p%s %s -e \"%s\"",
                dbHost, dbPort, dbUser, dbPassword, dbName, sqlCommands
            );

            Process process = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", comando});
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Base de datos limpiada exitosamente");
            } else {
                System.err.println("Error al limpiar la base de datos. Exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error ejecutando script de limpieza: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
