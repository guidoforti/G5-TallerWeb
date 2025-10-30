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



    private static Conductor c1 = new Conductor();
    private static Conductor c2 = new Conductor();
    private static Conductor c3 = new Conductor();

    private static Viajero viajero1 = new Viajero();
    private static Viajero viajero2 = new Viajero();
    private static Viajero viajero3 = new Viajero();

    static {
        // ==== Conductores ====
        c1.setId(1L);
        c1.setNombre("Juan Perez");
        c1.setEmail("juan.perez@example.com");
        c1.setContrasenia("passJuan123");
        c1.setRol("CONDUCTOR");
        c1.setActivo(true);
        c1.setFechaDeVencimientoLicencia(LocalDate.of(2027, 5, 20));
        c1.setViajes(new ArrayList<>());
        c1.setVehiculos(new ArrayList<>());

        c2.setId(2L);
        c2.setNombre("Maria Gomez");
        c2.setEmail("maria.gomez@example.com");
        c2.setContrasenia("passMaria456");
        c2.setRol("CONDUCTOR");
        c2.setActivo(true);
        c2.setFechaDeVencimientoLicencia(LocalDate.of(2026, 8, 10));
        c2.setViajes(new ArrayList<>());
        c2.setVehiculos(new ArrayList<>());

        c3.setId(3L);
        c3.setNombre("Carlos Diaz");
        c3.setEmail("carlos.diaz@example.com");
        c3.setContrasenia("passCarlos789");
        c3.setRol("CONDUCTOR");
        c3.setActivo(true);
        c3.setFechaDeVencimientoLicencia(LocalDate.of(2028, 1, 15));
        c3.setViajes(new ArrayList<>());
        c3.setVehiculos(new ArrayList<>());

        // ==== Viajeros ====
        viajero1.setId(4L); // IDs deben ser unicos
        viajero1.setNombre("Ana Rodriguez");
        viajero1.setEmail("ana@gmail.com");
        viajero1.setContrasenia("passAna123");
        viajero1.setRol("VIAJERO");
        viajero1.setActivo(true);
        viajero1.setEdad(25);
        viajero1.setReservas(new ArrayList<>());

        viajero2.setId(5L);
        viajero2.setNombre("Luis Garcia");
        viajero2.setEmail("luisg@gmail.com");
        viajero2.setContrasenia("passLuisg123");
        viajero2.setRol("VIAJERO");
        viajero2.setActivo(true);
        viajero2.setEdad(30);
        viajero2.setReservas(new ArrayList<>());

        viajero3.setId(6L);
        viajero3.setNombre("Sofia Lopez");
        viajero3.setEmail("sofial@gmail.com");
        viajero3.setContrasenia("sofialL123");
        viajero3.setRol("VIAJERO");
        viajero3.setActivo(true);
        viajero3.setEdad(28);
        viajero3.setReservas(new ArrayList<>());
    }

    // ==== Vehículos ====
    private static final Vehiculo v1 = new Vehiculo(1L, "Toyota Corolla", "2015", "ABC123", 4, EstadoVerificacion.VERIFICADO, c1);
    private static final Vehiculo v2 = new Vehiculo(2L, "Volkswagen Gol", "2018", "XYZ789", 4, EstadoVerificacion.VERIFICADO, c2);
    private static final Vehiculo v3 = new Vehiculo(3L, "Chevrolet Spin", "2020", "LMN456", 6, EstadoVerificacion.VERIFICADO, c3);

    // ==== Ubicaciones ====
    private static final Ciudad origen1 = new Ciudad(1L, "Av. Siempre Viva 123, CABA", -34.6037f, -58.3816f);
    private static final Ciudad destino1 = new Ciudad(2L, "La Plata, Buenos Aires", -34.9214f, -57.9544f);

    private static final Ciudad origen2 = new Ciudad(3L, "Rosario, Santa Fe", -32.9587f, -60.6939f);
    private static final Ciudad destino2 = new Ciudad(4L, "Cordoba Capital", -31.4201f, -64.1888f);

    private static final Ciudad origen3 = new Ciudad(5L, "Mendoza Capital", -32.8895f, -68.8458f);
    private static final Ciudad destino3 = new Ciudad(6L, "San Juan Capital", -31.5375f, -68.5364f);

    private static final List<Ciudad> ciudades = Arrays.asList(
            origen1, destino1, origen2, destino2, origen3, destino3
    );

    private static final List<Conductor> conductores = Arrays.asList(c1, c2, c3);
    private static final List<Vehiculo> vehiculos = Arrays.asList(v1, v2, v3);
    private static final List<Viajero> viajeros = Arrays.asList(viajero1, viajero2, viajero3);

    public static List<Ciudad> obtenerCiudades() {
        return new ArrayList<>(ciudades);
    }

    public static List<Conductor> obtenerConductores() {
        return new ArrayList<>(conductores);
    }

    public static List<Vehiculo> obtenerVehiculos() {
        return new ArrayList<>(vehiculos);
    }

    public static List<Viajero> obtenerViajeros() {
        return new ArrayList<>(viajeros);
    }

}
