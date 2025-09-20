package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.*;
import com.tallerwebi.dominio.Enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public  class Datos {


    // ==== Conductores ====
    private static final Conductor c1 = new Conductor(1L, new ArrayList<>(), "Juan Pérez", "juan.perez@example.com", "passJuan123", LocalDate.of(2027, 5, 20));
    private static final Conductor c2 = new Conductor(2L, new ArrayList<>(), "María Gómez", "maria.gomez@example.com", "passMaria456", LocalDate.of(2026, 8, 10));
    private static final Conductor c3 = new Conductor(3L, new ArrayList<>(), "Carlos Díaz", "carlos.diaz@example.com", "passCarlos789", LocalDate.of(2028, 1, 15));

    // ==== Vehículos ====
    private static final Vehiculo v1 = new Vehiculo(1L, c1, "Toyota Corolla", "2015", "ABC123", 4, EstadoVerificacion.VERIFICADO);
    private static final Vehiculo v2 = new Vehiculo(2L, c2, "Volkswagen Gol", "2018", "XYZ789", 4, EstadoVerificacion.VERIFICADO);
    private static final Vehiculo v3 = new Vehiculo(3L, c3, "Chevrolet Spin", "2020", "LMN456", 6, EstadoVerificacion.VERIFICADO);

    // ==== Ubicaciones ====
    private static final Ubicacion origen1 = new Ubicacion("Av. Siempre Viva 123, CABA", -34.6037f, -58.3816f);
    private static final Ubicacion destino1 = new Ubicacion("La Plata, Buenos Aires", -34.9214f, -57.9544f);

    private static final Ubicacion origen2 = new Ubicacion("Rosario, Santa Fe", -32.9587f, -60.6939f);
    private static final Ubicacion destino2 = new Ubicacion("Córdoba Capital", -31.4201f, -64.1888f);

    private static final Ubicacion origen3 = new Ubicacion("Mendoza Capital", -32.8895f, -68.8458f);
    private static final Ubicacion destino3 = new Ubicacion("San Juan Capital", -31.5375f, -68.5364f);

    // ==== Viajeros ====
    private static final Viajero viajero1 = new Viajero(1L, new ArrayList<>());
    private static final Viajero viajero2 = new Viajero(2L, new ArrayList<>());
    private static final Viajero viajero3 = new Viajero(3L, new ArrayList<>());

    // ==== Viajes ====
    private static final Viaje viaje1 = new Viaje(
            1L, c1, Arrays.asList(viajero1, viajero2),
            origen1, destino1, Collections.emptyList(),
            LocalDateTime.of(2025, 10, 5, 9, 30),
            1500.0, 4, 2, LocalDateTime.now(), v1
    );

    private static final Viaje viaje2 = new Viaje(
            2L, c2, Collections.singletonList(viajero3),
            origen2, destino2, Collections.singletonList(origen1),
            LocalDateTime.of(2025, 11, 12, 14, 0),
            5000.0, 4, 3, LocalDateTime.now(), v2
    );

    private static final Viaje viaje3 = new Viaje(
            3L, c3, Arrays.asList(viajero1, viajero2, viajero3),
            origen3, destino3, Arrays.asList(destino1, destino2),
            LocalDateTime.of(2025, 12, 1, 7, 0),
            3000.0, 6, 3, LocalDateTime.now(), v3
    );

    private static final List<Viaje> viajes = Arrays.asList(viaje1, viaje2, viaje3);

    // ==== Método público ====
    public static List<Viaje> obtenerViajes() {
        return viajes;
    }


}
