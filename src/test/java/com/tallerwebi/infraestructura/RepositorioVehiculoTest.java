package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RepositorioVehiculoTest {



    private RepositorioVehiculoImpl repositorioVehiculo;

    @BeforeEach
    public void setUp() {
        repositorioVehiculo = new RepositorioVehiculoImpl();
    }

    @Test
    public void deberiaEncontrarVehiculoPorPatenteExistente() {
        Vehiculo vehiculo = repositorioVehiculo.encontrarVehiculoConPatente("ABC123");
        assertNotNull(vehiculo);
        assertEquals("ABC123", vehiculo.getPatente());
    }

    @Test
    public void deberiaRetornarNullSiPatenteNoExiste() {
        Vehiculo vehiculo = repositorioVehiculo.encontrarVehiculoConPatente("ZZZ999");
        assertNull(vehiculo);
    }

    @Test
    public void deberiaGuardarVehiculoCorrectamente() {
        // Arrange
        Conductor conductor = new Conductor(4L, new ArrayList<>(), "Nuevo Conductor", "nuevo@example.com", "password123", LocalDate.now());
        Vehiculo nuevoVehiculo = new Vehiculo(null, conductor, "Toyota Hilux", "2022", "RRLL", 5, EstadoVerificacion.PENDIENTE);

        // Act
        Vehiculo vehiculoGuardado = repositorioVehiculo.guardarVehiculo(nuevoVehiculo);

        // Assert
        assertNotNull(vehiculoGuardado.getId(), "El vehículo guardado debe tener un ID asignado");
        assertEquals("RRLL", vehiculoGuardado.getPatente(), "La patente del vehículo guardado debe coincidir");
        assertEquals(EstadoVerificacion.PENDIENTE, vehiculoGuardado.getEstadoVerificacion(), "El estado de verificación debe ser PENDIENTE");
        assertSame(conductor, vehiculoGuardado.getConductor(), "El conductor debe ser el mismo");

        // Verificar que el vehículo realmente se guardó en el repositorio
        Vehiculo vehiculoEncontrado = repositorioVehiculo.encontrarVehiculoConPatente("RRLL");
        assertNotNull(vehiculoEncontrado, "El vehículo debe encontrarse en el repositorio");
        assertEquals(vehiculoGuardado.getId(), vehiculoEncontrado.getId(), "El ID del vehículo encontrado debe coincidir");

    }

    @Test
    public void deberiaRetornarListaVaciaSiConductorNoTieneVehiculos() {
        List<Vehiculo> lista = repositorioVehiculo.obtenerVehiculosParaConductor(999L);
        assertTrue(lista.isEmpty());
    }

    @Test
    public void deberiaRetornarNullSiIdNoExiste() {
        Vehiculo vehiculo = repositorioVehiculo.findById(999L);
        assertNull(vehiculo);
    }
}
