package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RepositorioVehiculoTest {


    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioVehiculoImpl repositorioVehiculo;

    @BeforeEach
    public void setUp() {
        // Asumiendo que la inyección de SessionFactory se maneja por Spring Test Context.
        repositorioVehiculo = new RepositorioVehiculoImpl(this.sessionFactory);
    }


    @Test
    public void deberiaEncontrarVehiculoPorPatenteExistente() {
        // Arrange: Aquí deberías persistir un vehículo de prueba primero, por ejemplo:
        // Vehiculo vehiculoExistente = crearYGuardarVehiculo("XYZ123", ...);
        // La implementación completa del test 'deberiaGuardarVehiculoCorrectamente' sirve de ejemplo.

        // --- Simulando la persistencia inicial ---
        Conductor conductor = new Conductor(10L, "Test Conductor", "test@mail.com", "pass", LocalDate.now(), new ArrayList<>(), new ArrayList<>());
        Vehiculo vehiculoExistente = new Vehiculo(null, "XYZ123", "Ford Fiesta", "2015", 5, EstadoVerificacion.VERIFICADO, conductor);
        repositorioVehiculo.guardarVehiculo(vehiculoExistente);
        // -----------------------------------------

        // Act
        Optional<Vehiculo> optionalVehiculo = repositorioVehiculo.encontrarVehiculoConPatente("XYZ123");

        // Assert
        assertTrue(optionalVehiculo.isPresent(), "El Optional debe contener un vehículo.");
        assertEquals("XYZ123", optionalVehiculo.get().getPatente(), "La patente del vehículo encontrado debe coincidir.");
    }

    // ---
    @Test
    public void deberiaRetornarOptionalVacioSiPatenteNoExiste() {
        // Act
        Optional<Vehiculo> optionalVehiculo = repositorioVehiculo.encontrarVehiculoConPatente("ZZZ999");

        // Assert: Usamos isEmpty() que es el chequeo moderno en Optional para la ausencia.
        assertTrue(optionalVehiculo.isEmpty(), "El Optional debe estar vacío si la patente no existe.");
    }

    // ---
    @Test
    public void deberiaGuardarVehiculoCorrectamente() {
        // Arrange
        Conductor conductor = new Conductor(4L, "Nuevo Conductor", "nuevo@example.com", "password123", LocalDate.now(), new ArrayList<>(), new ArrayList<>());
        Vehiculo nuevoVehiculo = new Vehiculo(null, "RRLL", "Toyota Hilux", "2022", 5, EstadoVerificacion.PENDIENTE, conductor);

        // Act
        Vehiculo vehiculoGuardado = repositorioVehiculo.guardarVehiculo(nuevoVehiculo); // Asumimos que guardarVehiculo sigue devolviendo Vehiculo

        // Assert (Validaciones iniciales se mantienen)
        assertNotNull(vehiculoGuardado.getId(), "El vehículo guardado debe tener un ID asignado");
        assertEquals("RRLL", vehiculoGuardado.getPatente(), "La patente del vehículo guardado debe coincidir");
        assertEquals(EstadoVerificacion.PENDIENTE, vehiculoGuardado.getEstadoVerificacion(), "El estado de verificación debe ser PENDIENTE");
        assertSame(conductor, vehiculoGuardado.getConductor(), "El conductor debe ser el mismo");

        // Verificar que el vehículo realmente se guardó en el repositorio usando el nuevo método con Optional
        Optional<Vehiculo> optionalVehiculoEncontrado = repositorioVehiculo.encontrarVehiculoConPatente("RRLL");

        assertTrue(optionalVehiculoEncontrado.isPresent(), "El vehículo debe encontrarse en el repositorio (Optional no vacío)");

        // Usar .get() es seguro aquí porque acabamos de chequear con isPresent()
        Vehiculo vehiculoEncontrado = optionalVehiculoEncontrado.get();
        assertEquals(vehiculoGuardado.getId(), vehiculoEncontrado.getId(), "El ID del vehículo encontrado debe coincidir");
    }

    // ---
    // Este método sigue siendo válido, ya que obtenerVehiculosParaConductor devuelve una List (nunca null, solo vacía)
    @Test
    public void deberiaRetornarListaVaciaSiConductorNoTieneVehiculos() {
        List<Vehiculo> lista = repositorioVehiculo.obtenerVehiculosParaConductor(999L);
        assertTrue(lista.isEmpty(), "La lista debe estar vacía si el conductor no tiene vehículos.");
    }

    // ---
    @Test
    public void deberiaRetornarOptionalVacioSiIdNoExiste() {
        // Act
        Optional<Vehiculo> optionalVehiculo = repositorioVehiculo.findById(999L);

        // Assert
        assertTrue(optionalVehiculo.isEmpty(), "El Optional debe estar vacío si el ID no existe.");
    }
}