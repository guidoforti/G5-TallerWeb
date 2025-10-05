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

    @BeforeEach
    void setUp() {
    this.repositorio = new RepositorioConductorImpl(this.sessionFactory);
    }

    @Test
    void deberiaGuardarConductorNuevo() {
        // Arrange
        Conductor nuevo = new Conductor();
        nuevo.setNombre("Pedro Ramírez");
        nuevo.setEmail("pedro@correo.com");
        nuevo.setContrasenia("clave123");

        // Act
        repositorio.guardar(nuevo);

        // Assert
        assertNotNull(nuevo.getId(), "El conductor debería tener un ID asignado después de guardarse");

        Optional<Conductor>  recuperado = repositorio.buscarPorId(nuevo.getId());
        assertNotNull(recuperado, "Debería poder recuperarse el conductor guardado");
        assertEquals("Pedro Ramírez", recuperado.get().getNombre());
        assertEquals("pedro@correo.com", recuperado.get().getEmail());
    }


    @Test
    void deberiaBuscarPorEmailYContrasenia() throws Exception {
        // Act
        Optional<Conductor> conductor = repositorio.buscarPorEmailYContrasenia("maria@correo.com", "abcd");

        // Assert
        assertNotNull(conductor, "El conductor debería encontrarse");
        assertEquals("María López", conductor.get().getNombre());
        assertEquals("maria@correo.com", conductor.get().getEmail());
    }

    @Test
    void noDeberiaEncontrarSiContraseniaIncorrecta() throws Exception {
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
        assertEquals("Carlos Pérez", conductor.get().getNombre());
        assertEquals("carlos@correo.com", conductor.get().getEmail());
    }
}
