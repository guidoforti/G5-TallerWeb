package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Enums.EstadoVerificacion;
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
import java.util.Optional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateTestConfig.class, DataBaseTestInitilizationConfig.class })
@Transactional
public class RepositorioVehiculoTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioVehiculoImpl repositorioVehiculo;

    @BeforeEach
    public void setUp() {
        repositorioVehiculo = new RepositorioVehiculoImpl(this.sessionFactory);
    }


    @Test
    public void deberiaEncontrarVehiculoPorIdExistente() {
        // Arrange: ID del vehículo precargado: 1L (Toyota Corolla)
        Long idExistente = 1L;

        Optional<Vehiculo> vehiculoOptional = repositorioVehiculo.findById(idExistente);

        assertTrue(vehiculoOptional.isPresent(), "Deberia encontrar el vehiculo por ID=1");
        assertEquals(idExistente, vehiculoOptional.get().getId());
    }

    @Test
    public void deberiaRetornarOptionalVacioSiIdNoExiste() {
        Optional<Vehiculo> optionalVehiculo = repositorioVehiculo.findById(999L);

        // Assert
        assertTrue(optionalVehiculo.isEmpty(), "El Optional debe estar vacío si el ID no existe.");
    }

    @Test
    public void deberiaEncontrarVehiculoPorPatenteExistente() {
        // Arrange: Patente del vehículo precargado: 'AB123CD' (ID=1)
        String patenteExistente = "AB123CD";

        Optional<Vehiculo> optionalVehiculo = repositorioVehiculo.encontrarVehiculoConPatente(patenteExistente);

        assertTrue(optionalVehiculo.isPresent(), "El Optional debe contener un vehículo con la patente.");
        assertEquals(patenteExistente, optionalVehiculo.get().getPatente(), "La patente debe coincidir.");
    }

    @Test
    public void deberiaRetornarOptionalVacioSiPatenteNoExiste() {
        Optional<Vehiculo> optionalVehiculo = repositorioVehiculo.encontrarVehiculoConPatente("ZZZ999");

        assertTrue(optionalVehiculo.isEmpty(), "El Optional debe estar vacío si la patente no existe.");
    }

    @Test
    public void deberiaGuardarVehiculoCorrectamente() {
        // Arrange
        // Buscamos un conductor precargado para la FK. Usamos el ID 3 (Juan Garcia).
        Conductor conductor = sessionFactory.getCurrentSession().get(Conductor.class, 3L);

        Vehiculo nuevoVehiculo = new Vehiculo(null, "NUEVO777", "Test Model", "2024", 5, EstadoVerificacion.PENDIENTE,
                conductor);

        Vehiculo vehiculoGuardado = repositorioVehiculo.guardarVehiculo(nuevoVehiculo);

        assertNotNull(vehiculoGuardado.getId(), "El vehículo guardado debe tener un ID asignado");
        Optional<Vehiculo> optionalVehiculoEncontrado = repositorioVehiculo.encontrarVehiculoConPatente("NUEVO777");
        assertTrue(optionalVehiculoEncontrado.isPresent());
        assertEquals("NUEVO777", optionalVehiculoEncontrado.get().getPatente());
    }


    @Test
    public void deberiaRetornarListaDeVehiculosParaConductor() {
        // Arrange: Conductor precargado ID=2 (Maria Lopez) tiene 1 vehículo precargado (ID=2)
        final Long ID_CONDUCTOR_CON_VEHICULOS = 2L;
        Conductor conductorLista = sessionFactory.getCurrentSession().get(Conductor.class, ID_CONDUCTOR_CON_VEHICULOS);

        // Insertamos un segundo vehículo para este conductor para probar la lista > 1
        repositorioVehiculo.guardarVehiculo(
                new Vehiculo(null, "AUX999", "AutoExtra", "2023", 4, EstadoVerificacion.PENDIENTE, conductorLista));

        //sessionFactory.getCurrentSession().flush();
        //sessionFactory.getCurrentSession().clear();

        List<Vehiculo> lista = repositorioVehiculo.obtenerVehiculosParaConductor(ID_CONDUCTOR_CON_VEHICULOS);

        assertNotNull(lista, "La lista no debe ser null.");
        assertEquals(2, lista.size(), "Debería retornar 2 vehículos.");
    }

    @Test
    public void deberiaRetornarListaVaciaSiConductorNoTieneVehiculos() {
        // Arrange: Creamos un conductor que NO tiene vehículos
        Conductor conductor = new Conductor();
        conductor.setId(1L);
        conductor.setNombre("Juan");
        conductor.setEmail("juan@test.com");
        conductor.setContrasenia("pass");
        conductor.setFechaDeVencimientoLicencia(LocalDate.now().plusDays(30)); // O la fecha que corresponda
        conductor.setViajes(new ArrayList<>());
        conductor.setVehiculos(null);
        conductor.setRol("CONDUCTOR");
        conductor.setActivo(true);
        sessionFactory.getCurrentSession().save(conductor);
        Long idSinVehiculos = conductor.getId();

        List<Vehiculo> lista = repositorioVehiculo.obtenerVehiculosParaConductor(idSinVehiculos);

        assertNotNull(lista, "La lista no debe ser null, debe ser vacía.");
        assertTrue(lista.isEmpty(), "La lista debe estar vacía si el conductor no tiene vehículos.");
    }
}