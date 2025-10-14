package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.IRepository.RepositorioCiudad;
import com.tallerwebi.integracion.config.DataBaseTestInitilizationConfig;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestConfig.class, DataBaseTestInitilizationConfig.class})
@Transactional
public class RepositorioCiudadTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioCiudad repositorioCiudad;


    @BeforeEach
    void setUp() {
       this.repositorioCiudad = new RepositorioCiudadImpl(this.sessionFactory);

    }

    @Test
    public void testBuscarCiudadPorId() {
        Ciudad ciudad = repositorioCiudad.buscarPorId(1L);

        // Verificaciones
        assertNotNull(ciudad, "La ciudad no debería ser null");
        assertEquals("Buenos Aires", ciudad.getNombre());
        assertEquals(-34.6037, ciudad.getLatitud(), 0.001);
        assertEquals(-58.3816, ciudad.getLongitud(), 0.001);
    }

    @Test
    public void testListarTodasLasCiudades() {
        List<Ciudad> ciudades = repositorioCiudad.findAll();
        assertNotNull(ciudades);
        assertEquals(3, ciudades.size(), "Debería haber 3 ciudades de dataTest.sql");

        // Verificar que están ordenadas (asumiendo orden por ID)
        assertEquals("Buenos Aires", ciudades.get(0).getNombre());
        assertEquals("Cordoba", ciudades.get(1).getNombre());
        assertEquals("Rosario", ciudades.get(2).getNombre());
    }

    @Test
    public void testGuardarNuevaCiudad() {
        // Crear nueva ciudad
        Ciudad nuevaCiudad = new Ciudad();
        nuevaCiudad.setNombre("Mendoza");
        nuevaCiudad.setLatitud(-32.8895f);
        nuevaCiudad.setLongitud(-68.8458f);

        // Guardar en BD
        Ciudad ciudadGuardada = repositorioCiudad.guardarCiudad(nuevaCiudad);

        // Verificaciones
        assertNotNull(ciudadGuardada.getId(), "Debería tener ID asignado");
        assertEquals("Mendoza", ciudadGuardada.getNombre());

        // Verificar que se puede recuperar
        Ciudad ciudadRecuperada = repositorioCiudad.buscarPorId(ciudadGuardada.getId());
        assertEquals("Mendoza", ciudadRecuperada.getNombre());
    }

    @Test
    public void testBuscarCiudadPorCoordenadasExistentes() {
        // Primero guardar una ciudad para poder buscarla
        Ciudad nuevaCiudad = new Ciudad();
        nuevaCiudad.setNombre("La Plata");
        nuevaCiudad.setLatitud(-34.9205f);
        nuevaCiudad.setLongitud(-57.9536f);

        Ciudad ciudadGuardada = repositorioCiudad.guardarCiudad(nuevaCiudad);

        // Buscar ciudad por coordenadas
        Ciudad ciudad = repositorioCiudad.buscarPorCoordenadas(-34.9205f, -57.9536f);

        // Verificaciones
        assertNotNull(ciudad, "Debería encontrar la ciudad");
        assertEquals("La Plata", ciudad.getNombre());
        assertEquals(ciudadGuardada.getId(), ciudad.getId());
    }

    @Test
    public void testBuscarCiudadPorCoordenadasNoExistentes() {
        // Buscar con coordenadas que no existen
        Ciudad ciudad = repositorioCiudad.buscarPorCoordenadas(-99.9999f, -99.9999f);

        // Verificaciones
        assertThat(ciudad, is(nullValue()));
    }

    @Test
    public void testBuscarCiudadPorCoordenadasDespuesDeGuardar() {
        // Guardar nueva ciudad
        Ciudad nuevaCiudad = new Ciudad();
        nuevaCiudad.setNombre("Mendoza");
        nuevaCiudad.setLatitud(-32.8895f);
        nuevaCiudad.setLongitud(-68.8458f);

        repositorioCiudad.guardarCiudad(nuevaCiudad);

        // Buscar por coordenadas
        Ciudad ciudadEncontrada = repositorioCiudad.buscarPorCoordenadas(-32.8895f, -68.8458f);

        // Verificaciones
        assertNotNull(ciudadEncontrada);
        assertEquals("Mendoza", ciudadEncontrada.getNombre());
        assertEquals(-32.8895f, ciudadEncontrada.getLatitud(), 0.0001);
        assertEquals(-68.8458f, ciudadEncontrada.getLongitud(), 0.0001);
    }


}