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
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;


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
        Optional<Ciudad> ciudadOptional = repositorioCiudad.buscarPorId(1L);

        // Verificaciones
        assertTrue(ciudadOptional.isPresent(), "El Optional debería contener la ciudad");
        Ciudad ciudad = ciudadOptional.get();
        assertEquals("Buenos Aires", ciudad.getNombre());
        assertEquals(-34.6095579, ciudad.getLatitud(), 0.001);
        assertEquals(-58.3887904, ciudad.getLongitud(), 0.001);
    }

    @Test
    public void testBuscarCiudadPorIdNoExistente() {
        // Act
        Optional<Ciudad> ciudadOptional = repositorioCiudad.buscarPorId(999L);
        // Assert
        assertTrue(ciudadOptional.isEmpty(), "El Optional debería estar vacío para un ID inexistente");
    }

    @Test
    public void testListarTodasLasCiudades() {
        List<Ciudad> ciudades = repositorioCiudad.findAll();
        assertNotNull(ciudades);

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
        Optional<Ciudad> ciudadOptional = repositorioCiudad.buscarPorId(ciudadGuardada.getId());
        assertTrue(ciudadOptional.isPresent(), "La ciudad guardada debe ser recuperable");
        assertEquals("Mendoza", ciudadOptional.get().getNombre());
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
        Optional<Ciudad> ciudadOptional = repositorioCiudad.buscarPorCoordenadas(-34.9205f, -57.9536f);

        // Verificaciones
        assertTrue(ciudadOptional.isPresent(), "El Optional debería contener la ciudad");
        Ciudad ciudad = ciudadOptional.get();
        assertEquals("La Plata", ciudad.getNombre());
        assertEquals(ciudadGuardada.getId(), ciudad.getId());
    }

    @Test
    public void testBuscarCiudadPorCoordenadasNoExistentes() {
        // Buscar con coordenadas que no existen
        Optional<Ciudad> ciudadOptional = repositorioCiudad.buscarPorCoordenadas(-99.9999f, -99.9999f);

        // Verificaciones
        assertTrue(ciudadOptional.isEmpty(), "El optional deberia estar vacio");
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
        Optional<Ciudad> ciudadOptional = repositorioCiudad.buscarPorCoordenadas(-32.8895f, -68.8458f);

        // Verificaciones
        assertTrue(ciudadOptional.isPresent());
        Ciudad ciudadEncontrada = ciudadOptional.get();
        assertEquals("Mendoza", ciudadEncontrada.getNombre());
        assertEquals(-32.8895f, ciudadEncontrada.getLatitud(), 0.0001);
        assertEquals(-68.8458f, ciudadEncontrada.getLongitud(), 0.0001);
    }

    @Test
    public void testDeberiaEliminarCiudadExistente() {
        // creamos ciudad nueva para que no tenga dependencias
        Ciudad ciudadNueva = new Ciudad();
        ciudadNueva.setNombre("Ciudad Temporal");
        ciudadNueva.setLatitud(-1f);
        ciudadNueva.setLongitud(-1f);
        repositorioCiudad.guardarCiudad(ciudadNueva);

        Long ciudadId = ciudadNueva.getId();

        assertTrue(repositorioCiudad.buscarPorId(ciudadId).isPresent(), "La ciudad debería existir antes de la eliminación");

        repositorioCiudad.eliminarCiudad(ciudadId);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        assertTrue(repositorioCiudad.buscarPorId(ciudadId).isEmpty(), "La ciudad no debería ser encontrada después de la eliminación");
    }
    @Test
    public void testEliminarCiudadInexistenteNoDeberiaLanzarExcepcion() {
        // Arrange
        Long idInexistente = 999L;
        assertDoesNotThrow(() -> {
            repositorioCiudad.eliminarCiudad(idInexistente);
            sessionFactory.getCurrentSession().flush();
        }, "Eliminar una ciudad inexistente no debería lanzar excepción con DELETE HQL directo");
    }

    @Test
    public void testDeberiaActualizarCiudadExistente() {
        Long ciudadId = 2L;
        Optional<Ciudad> ciudadOptional = repositorioCiudad.buscarPorId(ciudadId);
        assertTrue(ciudadOptional.isPresent());
        Ciudad ciudadAModificar = ciudadOptional.get();

        String nuevoNombre = "Córdoba Capital";
        float nuevaLatitud = -31.4167f;

        ciudadAModificar.setNombre(nuevoNombre);
        ciudadAModificar.setLatitud(nuevaLatitud);

        // Act
        repositorioCiudad.actualizarCiudad(ciudadAModificar);
        sessionFactory.getCurrentSession().flush();

        Optional<Ciudad> ciudadActualizadaOptional = repositorioCiudad.buscarPorId(ciudadId);
        assertTrue(ciudadActualizadaOptional.isPresent());
        Ciudad ciudadActualizada = ciudadActualizadaOptional.get();

        assertEquals(ciudadId, ciudadActualizada.getId());
        assertEquals(nuevoNombre, ciudadActualizada.getNombre());
        assertEquals(nuevaLatitud, ciudadActualizada.getLatitud(), 0.0001); // Comparación de float con tolerancia
    }

}