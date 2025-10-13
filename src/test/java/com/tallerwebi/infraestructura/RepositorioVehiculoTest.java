package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Enums.EstadoVerificacion; // Asumo que esta es la ruta de tu Enum
import com.tallerwebi.infraestructura.RepositorioVehiculoImpl; // Necesaria para la declaración de la clase
import com.tallerwebi.integracion.config.DataBaseTestInitilizationConfig;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateTestConfig.class, DataBaseTestInitilizationConfig.class })
@Transactional
public class RepositorioVehiculoTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioVehiculoImpl repositorioVehiculo;
    private Conductor conductorBase;
    private Conductor otroConductor; // Para tests de lista

    @BeforeEach
    public void setUp() {
        repositorioVehiculo = new RepositorioVehiculoImpl(this.sessionFactory);

        // 1. Persistir el Conductor Base
        conductorBase = new Conductor(null, "Conductor Base", "base@mail.com", "pass", LocalDate.now(),
                new ArrayList<>(), new ArrayList<>());
        sessionFactory.getCurrentSession().save(conductorBase);

        // 2. Persistir un Segundo Conductor (para tests de aislamiento/lista)
        otroConductor = new Conductor(null, "Otro Conductor", "otro@mail.com", "pass", LocalDate.now(),
                new ArrayList<>(), new ArrayList<>());
        sessionFactory.getCurrentSession().save(otroConductor);

        // Forzar sincronización con la DB antes de que empiecen los tests
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
    }

    // ---------------------------------------------------------------------
    // Tests: findById
    // ---------------------------------------------------------------------

    @Test
    public void deberiaEncontrarVehiculoPorIdExistente() {
        // Arrange
        Vehiculo vehiculoBase = new Vehiculo(null, "AAA111", "ModeloX", "2020", 4, EstadoVerificacion.PENDIENTE,
                conductorBase);
        repositorioVehiculo.guardarVehiculo(vehiculoBase);
        Long idExistente = vehiculoBase.getId();

        // Act
        Optional<Vehiculo> vehiculoOptional = repositorioVehiculo.findById(idExistente);

        // Assert
        assertTrue(vehiculoOptional.isPresent(), "Deberia encontrar el vehiculo por ID");
        assertEquals(idExistente, vehiculoOptional.get().getId());
    }

    @Test
    public void deberiaRetornarOptionalVacioSiIdNoExiste() {
        // Act
        Optional<Vehiculo> optionalVehiculo = repositorioVehiculo.findById(999L);

        // Assert
        assertTrue(optionalVehiculo.isEmpty(), "El Optional debe estar vacío si el ID no existe.");
    }

    // ---------------------------------------------------------------------
    // Tests: encontrarVehiculoConPatente
    // ---------------------------------------------------------------------

    @Test
    public void deberiaEncontrarVehiculoPorPatenteExistente() {
        // Arrange
        final String patenteEsperada = "XYZ123";
        Vehiculo vehiculoExistente = new Vehiculo(null, patenteEsperada, "Ford Fiesta", "2015", 5,
                EstadoVerificacion.VERIFICADO, conductorBase);
        repositorioVehiculo.guardarVehiculo(vehiculoExistente);

        // Act
        Optional<Vehiculo> optionalVehiculo = repositorioVehiculo.encontrarVehiculoConPatente(patenteEsperada);

        // Assert
        assertTrue(optionalVehiculo.isPresent(), "El Optional debe contener un vehículo.");
        assertEquals(patenteEsperada, optionalVehiculo.get().getPatente(), "La patente debe coincidir.");
    }

    @Test
    public void deberiaRetornarOptionalVacioSiPatenteNoExiste() {
        // Act
        Optional<Vehiculo> optionalVehiculo = repositorioVehiculo.encontrarVehiculoConPatente("ZZZ999");

        // Assert
        assertTrue(optionalVehiculo.isEmpty(), "El Optional debe estar vacío si la patente no existe.");
    }

    // ---------------------------------------------------------------------
    // Tests: guardarVehiculo
    // ---------------------------------------------------------------------

    @Test
    public void deberiaGuardarVehiculoCorrectamente() {
        // Arrange
        // NO se usa 'conductorBase' aquí para probar que la nueva entidad se puede
        // guardar
        // pero la FK sí debe existir. Usamos el 'otroConductor' para mayor seguridad.
        Vehiculo nuevoVehiculo = new Vehiculo(null, "RRLL", "Toyota Hilux", "2022", 5, EstadoVerificacion.PENDIENTE,
                otroConductor);

        // Act
        Vehiculo vehiculoGuardado = repositorioVehiculo.guardarVehiculo(nuevoVehiculo);

        // Assert (Verificar el retorno del objeto con el ID)
        assertNotNull(vehiculoGuardado.getId(), "El vehículo guardado debe tener un ID asignado");

        // Verificar persistencia (buscarlo por patente)
        Optional<Vehiculo> optionalVehiculoEncontrado = repositorioVehiculo.encontrarVehiculoConPatente("RRLL");
        assertTrue(optionalVehiculoEncontrado.isPresent(), "El vehículo debe encontrarse en el repositorio.");
        assertEquals(vehiculoGuardado.getId(), optionalVehiculoEncontrado.get().getId(),
                "El ID del vehículo encontrado debe coincidir");
    }

    // ---------------------------------------------------------------------
    // Tests: obtenerVehiculosParaConductor (100% Cobertura)
    // ---------------------------------------------------------------------

    // Cubre el caso de éxito (lista con resultados)
    @Test
    public void deberiaRetornarListaDeVehiculosParaConductor() {
        // Arrange: Guardar 2 vehículos para conductorBase
        repositorioVehiculo.guardarVehiculo(
                new Vehiculo(null, "VEH1", "Auto1", "2023", 4, EstadoVerificacion.PENDIENTE, conductorBase));
        repositorioVehiculo.guardarVehiculo(
                new Vehiculo(null, "VEH2", "Auto2", "2023", 4, EstadoVerificacion.PENDIENTE, conductorBase));

        // Act
        List<Vehiculo> lista = repositorioVehiculo.obtenerVehiculosParaConductor(conductorBase.getId());

        // Assert
        assertNotNull(lista, "La lista no debe ser null.");
        assertEquals(2, lista.size(), "Debería retornar exactamente 2 vehículos para el conductorBase.");
    }

    // Cubre el caso límite (lista vacía)
    @Test
    public void deberiaRetornarListaVaciaSiConductorNoTieneVehiculos() {
        // Arrange: El 'otroConductor' existe pero no le hemos guardado vehículos en
        // este test.
        Long idSinVehiculos = otroConductor.getId();

        // Act
        List<Vehiculo> lista = repositorioVehiculo.obtenerVehiculosParaConductor(idSinVehiculos);

        // Assert
        assertNotNull(lista, "La lista no debe ser null, debe ser vacía.");
        assertTrue(lista.isEmpty(), "La lista debe estar vacía si el conductor no tiene vehículos.");
    }
}