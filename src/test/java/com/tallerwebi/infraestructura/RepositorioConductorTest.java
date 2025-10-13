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

        // --- 1. PERSISTIR DATOS NECESARIOS (EN LUGAR DE ASUMIR QUE EXISTEN) ---

        // Conductor para pruebas de éxito (ID y credenciales)
        conductorBase = new Conductor(null, "Maria Lopez", "maria@correo.com", "abcd",
                LocalDate.now(), new ArrayList<>(), new ArrayList<>());

        repositorio.guardarConductor(conductorBase);

        // Limpiar para asegurar que las búsquedas siguientes lean de la DB
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
    }

    @Test
    void deberiaGuardarConductorConductorNuevo() {
        // Arrange
        Conductor nuevo = new Conductor();
        nuevo.setNombre("Pedro Ramirez");
        nuevo.setEmail("pedro@correo.com");
        nuevo.setContrasenia("clave123");

        // Act
        Conductor guardado = repositorio.guardarConductor(nuevo);

        // Assert
        assertNotNull(nuevo.getId(), "El conductor debería tener un ID asignado después de guardarse");

        Optional<Conductor>  recuperado = repositorio.buscarPorId(guardado.getId());
        assertTrue(recuperado.isPresent(), "Debería poder recuperarse el conductor guardado");
        assertEquals("Pedro Ramirez", recuperado.get().getNombre());
        assertEquals("pedro@correo.com", recuperado.get().getEmail());
    }


    @Test
    void deberiaBuscarPorEmailYContrasenia() {
        // Act
        Optional<Conductor> conductor = repositorio.buscarPorEmailYContrasenia("maria@correo.com", "abcd");

        // Assert
        assertTrue(conductor.isPresent(), "El conductor debería encontrarse con credenciales correctas");
        assertEquals(conductorBase.getNombre(), conductor.get().getNombre());
        assertEquals("maria@correo.com", conductor.get().getEmail());
    }

    @Test
    void noDeberiaEncontrarSiContraseniaIncorrecta() {
        // Act
        Optional<Conductor> conductor = repositorio.buscarPorEmailYContrasenia("maria@correo.com", "claveMala");

        // Assert
        assertTrue(conductor.isEmpty(), "No debería devolver un conductor con contraseña incorrecta");
    }

    @Test
    void deberiaBuscarPorId() {
        // Act
        Optional<Conductor> conductor = repositorio.buscarPorId(1L);

        // Assert
        assertNotNull(conductor);
        assertEquals("Carlos Perez", conductor.get().getNombre());
        assertEquals("carlos@correo.com", conductor.get().getEmail());
    }
}
