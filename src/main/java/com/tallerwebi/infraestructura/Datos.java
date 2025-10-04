package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.*;
import com.tallerwebi.dominio.Enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Datos {



    // ==== Conductores ====
    private static final Conductor c1 = new Conductor(1L, "Juan Pérez", "juan.perez@example.com", "passJuan123", LocalDate.of(2027, 5, 20), new ArrayList<>(), new ArrayList<>());
    private static final Conductor c2 = new Conductor(2L, "María Gómez", "maria.gomez@example.com", "passMaria456", LocalDate.of(2026, 8, 10), new ArrayList<>(), new ArrayList<>());
    private static final Conductor c3 = new Conductor(3L, "Carlos Díaz", "carlos.diaz@example.com", "passCarlos789", LocalDate.of(2028, 1, 15) , new ArrayList<>(), new ArrayList<>());

    // ==== Vehículos ====
    private static final Vehiculo v1 = new Vehiculo(1L, "Toyota Corolla", "2015", "ABC123", 4, EstadoVerificacion.VERIFICADO, c1);
    private static final Vehiculo v2 = new Vehiculo(2L, "Volkswagen Gol", "2018", "XYZ789", 4, EstadoVerificacion.VERIFICADO, c2);
    private static final Vehiculo v3 = new Vehiculo(3L, "Chevrolet Spin", "2020", "LMN456", 6, EstadoVerificacion.VERIFICADO, c3);

    // ==== Ubicaciones ====
    private static final Ciudad origen1 = new Ciudad(1L, "Av. Siempre Viva 123, CABA", -34.6037f, -58.3816f);
    private static final Ciudad destino1 = new Ciudad(2L, "La Plata, Buenos Aires", -34.9214f, -57.9544f);

    private static final Ciudad origen2 = new Ciudad(3L, "Rosario, Santa Fe", -32.9587f, -60.6939f);
    private static final Ciudad destino2 = new Ciudad(4L, "Córdoba Capital", -31.4201f, -64.1888f);

    private static final Ciudad origen3 = new Ciudad(5L, "Mendoza Capital", -32.8895f, -68.8458f);
    private static final Ciudad destino3 = new Ciudad(6L, "San Juan Capital", -31.5375f, -68.5364f);


    // ==== Viajeros ====
    private static final Viajero viajero1 = new Viajero(1L, "Juan Pérez", 25, new ArrayList<>());
    private static final Viajero viajero2 = new Viajero(2L, "María García", 30, new ArrayList<>());
    private static final Viajero viajero3 = new Viajero(3L, "Carlos López", 28, new ArrayList<>());




    private static final List<Ciudad> ciudades = Arrays.asList(
            origen1, destino1, origen2, destino2, origen3, destino3
    );

    private static final List<Conductor> conductores = Arrays.asList(c1, c2, c3);
    private static final List<Vehiculo> vehiculos = Arrays.asList(v1, v2, v3);



    public static List<Ciudad> obtenerCiudades() {
        return new ArrayList<>(ciudades);
    }

    public static List<Conductor> obtenerConductores() {
        return new ArrayList<>(conductores);
    }

    public static List<Vehiculo> obtenerVehiculos() {
        return new ArrayList<>(vehiculos);
    }
}
