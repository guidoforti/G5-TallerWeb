package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.integracion.config.DataBaseTestInitilizationConfig;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestConfig.class , DataBaseTestInitilizationConfig.class})
@Transactional
public class RepositorioConductorTest {

    @Autowired
    SessionFactory sessionFactory;

    private RepositorioConductor repositorio;
    private Conductor conductorBase;

    @BeforeEach
    void setUp() {
        this.repositorio = new RepositorioConductorImpl(this.sessionFactory);

        conductorBase = new Conductor(2L, "Maria Lopez", "maria@correo.com", "abcd",
                LocalDate.of(2026, 11, 20), new ArrayList<>(), new ArrayList<>());
    }

    @Test
    void deberiaGuardarConductorConductorNuevo() {
        // Arrange
        Conductor nuevo = new Conductor();
        nuevo.setNombre("ConductorAGuardar");
        nuevo.setEmail("pedro123@correo.com");
        nuevo.setContrasenia("clave1234");

        // Act
        Conductor guardado = repositorio.guardarConductor(nuevo);

        // Assert
        assertNotNull(nuevo.getId(), "El conductor debería tener un ID asignado después de guardarse");

        Optional<Conductor>  recuperado = repositorio.buscarPorId(guardado.getId());
        assertTrue(recuperado.isPresent(), "Debería poder recuperarse el conductor guardado");
        assertEquals("ConductorAGuardar", recuperado.get().getNombre());
        assertEquals("pedro123@correo.com", recuperado.get().getEmail());
    }


    @Test
    void deberiaBuscarPorEmailYContraseniaSiExiste() {
        // Act
        Optional<Conductor> conductor = repositorio.buscarPorEmailYContrasenia(conductorBase.getEmail(), conductorBase.getContrasenia());

        // Assert
        assertTrue(conductor.isPresent(), "El conductor debería encontrarse con credenciales correctas");
        assertEquals(conductorBase.getId(), conductor.get().getId());
        assertEquals(conductorBase.getEmail(), conductor.get().getEmail());
    }

    @Test
    void noDeberiaEncontrarSiContraseniaIncorrecta() {
        // Act
        Optional<Conductor> conductor = repositorio.buscarPorEmailYContrasenia("maria@correo.com", "claveMala");

        // Assert
        assertTrue(conductor.isEmpty(), "No debería devolver un conductor con contraseña incorrecta");
    }

    @Test
    void deberiaBuscarPorIdSiExiste() {
        Optional<Conductor> conductor = repositorio.buscarPorId(conductorBase.getId()); // Usamos 2L

        // Assert
        assertTrue(conductor.isPresent(), "Debería encontrar el conductor precargado por su ID");
        assertEquals(conductorBase.getEmail(), conductor.get().getEmail());
    }

    @Test
    void deberiaBuscarPorEmailSiExiste() {
        // Act
        Optional<Conductor> conductor = repositorio.buscarPorEmail(conductorBase.getEmail());

        // Assert
        assertTrue(conductor.isPresent(), "El conductor debería encontrarse con el email correcto");
        assertEquals(conductorBase.getNombre(), conductor.get().getNombre());
    }
}
