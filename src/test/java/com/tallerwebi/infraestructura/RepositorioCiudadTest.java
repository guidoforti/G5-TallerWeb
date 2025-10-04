package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.IRepository.RepositorioCiudad;
import com.tallerwebi.integracion.config.DataBaseTestInitilizationConfig;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
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
@ContextConfiguration(classes = {HibernateTestConfig.class , DataBaseTestInitilizationConfig.class,  SpringWebTestConfig.class})
@WebAppConfiguration
@Transactional
public class RepositorioCiudadTest {

    @Autowired
    private  RepositorioCiudad repositorioCiudad;


    @Test
    public void testBuscarCiudadPorId() {
        // Dado que dataTest.sql inserta ciudades con IDs 1, 2, 3
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
        assertEquals("Córdoba", ciudades.get(1).getNombre());
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


}