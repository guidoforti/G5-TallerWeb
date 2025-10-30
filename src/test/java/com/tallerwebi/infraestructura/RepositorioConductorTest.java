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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestConfig.class , DataBaseTestInitilizationConfig.class})
@Transactional
public class RepositorioConductorTest {

    @Autowired
    SessionFactory sessionFactory;

    private RepositorioConductor repositorioConductor;
    private RepositorioUsuarioImpl repositorioUsuario;

    private Conductor conductorBase;

    @BeforeEach
    void setUp() {
        this.repositorioConductor = new RepositorioConductorImpl(this.sessionFactory);
        this.repositorioUsuario = new RepositorioUsuarioImpl(this.sessionFactory);

        conductorBase = new Conductor();
        conductorBase.setNombre("Maria Lopez");
        conductorBase.setEmail("maria@correo.com");
        conductorBase.setContrasenia("abcd");
        conductorBase.setRol("CONDUCTOR");
        conductorBase.setActivo(true);
        conductorBase.setFechaDeVencimientoLicencia(LocalDate.of(2026, 11, 20));
        conductorBase.setViajes(new ArrayList<>());
        conductorBase.setVehiculos(new ArrayList<>());

        repositorioUsuario.guardar(conductorBase);
    }
    @Test
    void deberiaBuscarPorIdSiExiste() {
        // Act
        Optional<Conductor> conductor = repositorioConductor.buscarPorId(conductorBase.getId());

        // Assert
        assertTrue(conductor.isPresent(), "Debería encontrar el conductor precargado por su ID");
        assertEquals(conductorBase.getEmail(), conductor.get().getEmail());
    }
}