package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.IRepository.RepositorioCiudad;
import com.tallerwebi.dominio.IServicio.ServicioCiudad;
import com.tallerwebi.dominio.ServiceImpl.ServicioCiudadImpl;
import com.tallerwebi.infraestructura.Datos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServicioCiudadTest {

  @Test
    public void queSeListanTodasLasUbicaciones() {

    RepositorioCiudad repositorioMock = mock(RepositorioCiudad.class);
    ServicioCiudad servicio = new ServicioCiudadImpl(repositorioMock);

    List<Ciudad> ubicacionesDePrueba = Datos.obtenerCiudades();
    when(repositorioMock.findAll()).thenReturn(ubicacionesDePrueba);

    List<Ciudad> ubicaciones = servicio.listarTodas();

    assertThat(ubicaciones.size(), is(ubicacionesDePrueba.size()));
        
    }


@Test
public void queLasUbicacionesNoSeanNulas() {
   

    RepositorioCiudad repositorioMock = mock(RepositorioCiudad.class);
    ServicioCiudad servicio = new ServicioCiudadImpl(repositorioMock);

    when(repositorioMock.findAll()).thenReturn(Datos.obtenerCiudades());

    List<Ciudad> ubicaciones = servicio.listarTodas();

    assertThat(ubicaciones, everyItem(notNullValue()));

    }



@Test
public void queCadaUbicacionTengaDireccionValida() {

   RepositorioCiudad repositorioMock = mock(RepositorioCiudad.class);
    ServicioCiudad servicio = new ServicioCiudadImpl(repositorioMock);

    List<Ciudad> ubicacionesDePrueba = Datos.obtenerCiudades();

    when(repositorioMock.findAll()).thenReturn(ubicacionesDePrueba);

    List<Ciudad> ubicaciones = servicio.listarTodas();

    for (Ciudad ciudad : ubicaciones) {
        assertThat(ciudad.getNombre(), allOf(notNullValue(), not(isEmptyString())));
    }


}

@Test
public void queGuardeNuevaCiudadSiNoExiste() {
    // Arrange
    RepositorioCiudad repositorioMock = mock(RepositorioCiudad.class);
    ServicioCiudad servicio = new ServicioCiudadImpl(repositorioMock);

    Ciudad nuevaCiudad = new Ciudad();
    nuevaCiudad.setNombre("Mendoza");
    nuevaCiudad.setLatitud(-32.8895f);
    nuevaCiudad.setLongitud(-68.8458f);

    // Mock CORREGIDO: ahora devuelve Optional.empty() para simular que NO existe
    when(repositorioMock.buscarPorCoordenadas(-32.8895f, -68.8458f)).thenReturn(Optional.empty());
    when(repositorioMock.guardarCiudad(nuevaCiudad)).thenReturn(nuevaCiudad);

    // Act
    Ciudad resultado = servicio.guardarCiudad(nuevaCiudad);

    // Assert
    assertEquals("Mendoza", resultado.getNombre());
    verify(repositorioMock).buscarPorCoordenadas(-32.8895f, -68.8458f);
    verify(repositorioMock).guardarCiudad(nuevaCiudad);
}

@Test
public void queRetorneCiudadExistenteSiYaExisteConMismasCoordenadas() {
    // Arrange
    RepositorioCiudad repositorioMock = mock(RepositorioCiudad.class);
    ServicioCiudad servicio = new ServicioCiudadImpl(repositorioMock);

    Ciudad ciudadExistente = new Ciudad();
    ciudadExistente.setId(1L);
    ciudadExistente.setNombre("Buenos Aires");
    ciudadExistente.setLatitud(-34.6037f);
    ciudadExistente.setLongitud(-58.3816f);

    Ciudad nuevaCiudad = new Ciudad();
    nuevaCiudad.setNombre("CABA");
    nuevaCiudad.setLatitud(-34.6037f);
    nuevaCiudad.setLongitud(-58.3816f);

    // Mock CORREGIDO: ahora devuelve Optional.of(ciudadExistente) para simular que SÍ existe
    when(repositorioMock.buscarPorCoordenadas(-34.6037f, -58.3816f)).thenReturn(Optional.of(ciudadExistente));

    // Act
    Ciudad resultado = servicio.guardarCiudad(nuevaCiudad);

    // Assert
    assertEquals(ciudadExistente.getId(), resultado.getId());
    assertEquals("Buenos Aires", resultado.getNombre());
    verify(repositorioMock).buscarPorCoordenadas(-34.6037f, -58.3816f);
    verify(repositorioMock, never()).guardarCiudad(any());
}

@Test
public void queNoGuardeDuplicadosCuandoSeIntentaGuardarMismaCiudadDosVeces() {
    // Arrange
    RepositorioCiudad repositorioMock = mock(RepositorioCiudad.class);
    ServicioCiudad servicio = new ServicioCiudadImpl(repositorioMock);

    Ciudad ciudad1 = new Ciudad();
    ciudad1.setNombre("Rosario");
    ciudad1.setLatitud(-32.9442f);
    ciudad1.setLongitud(-60.6505f);
    ciudad1.setId(10L);

    Ciudad ciudad2 = new Ciudad();
    ciudad2.setNombre("Rosario Centro");  // Nombre diferente
    ciudad2.setLatitud(-32.9442f);  // Mismas coordenadas
    ciudad2.setLongitud(-60.6505f);

    when(repositorioMock.buscarPorCoordenadas(-32.9442f, -60.6505f))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(ciudad1));

    when(repositorioMock.guardarCiudad(ciudad1)).thenReturn(ciudad1);

    // Act
    Ciudad resultado1 = servicio.guardarCiudad(ciudad1);
    Ciudad resultado2 = servicio.guardarCiudad(ciudad2);

    // Assert
    assertEquals(10L, resultado1.getId());
    assertEquals(10L, resultado2.getId());
    assertEquals("Rosario", resultado2.getNombre());
    verify(repositorioMock, times(1)).guardarCiudad(any());  // Solo se guarda una vez
}

    @Test
    public void queElimineCiudadSiExiste() throws NotFoundException {
        // Arrange
        RepositorioCiudad repositorioMock = mock(RepositorioCiudad.class);
        ServicioCiudad servicio = new ServicioCiudadImpl(repositorioMock);
        Long idExistente = 1L;

        // Mock: la ciudad existe
        when(repositorioMock.buscarPorId(idExistente)).thenReturn(Optional.of(new Ciudad()));

        // Act
        servicio.eliminarCiudad(idExistente);

        // Assert
        verify(repositorioMock).buscarPorId(idExistente);
        verify(repositorioMock).eliminarCiudad(idExistente);
    }

    @Test
    public void queLanceExcepcionAlIntentarEliminarCiudadInexistente() {
        // Arrange
        RepositorioCiudad repositorioMock = mock(RepositorioCiudad.class);
        ServicioCiudad servicio = new ServicioCiudadImpl(repositorioMock);
        Long idInexistente = 999L;

        when(repositorioMock.buscarPorId(idInexistente)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> servicio.eliminarCiudad(idInexistente));
        verify(repositorioMock).buscarPorId(idInexistente);
        verify(repositorioMock, never()).eliminarCiudad(anyLong());
    }

    @Test
    public void queActualiceCiudadSiExiste() throws NotFoundException {
        // Arrange
        RepositorioCiudad repositorioMock = mock(RepositorioCiudad.class);
        ServicioCiudad servicio = new ServicioCiudadImpl(repositorioMock);
        Ciudad ciudadAModificar = new Ciudad();
        ciudadAModificar.setId(1L);

        when(repositorioMock.buscarPorId(1L)).thenReturn(Optional.of(ciudadAModificar));
        when(repositorioMock.actualizarCiudad(ciudadAModificar)).thenReturn(ciudadAModificar);

        Ciudad resultado = servicio.actualizarCiudad(ciudadAModificar);

        // Assert
        assertThat(resultado, is(ciudadAModificar));
        verify(repositorioMock).buscarPorId(1L);
        verify(repositorioMock).actualizarCiudad(ciudadAModificar);
    }

    @Test
    public void queLanceExcepcionAlIntentarActualizarCiudadInexistente() {
        // Arrange
        RepositorioCiudad repositorioMock = mock(RepositorioCiudad.class);
        ServicioCiudad servicio = new ServicioCiudadImpl(repositorioMock);
        Ciudad ciudadInexistente = new Ciudad();
        ciudadInexistente.setId(999L);

        when(repositorioMock.buscarPorId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> servicio.actualizarCiudad(ciudadInexistente));
        verify(repositorioMock).buscarPorId(999L);
        verify(repositorioMock, never()).actualizarCiudad(any());
    }

    @Test
    public void queLanceExcepcionSiBuscarPorIdNoEncuentraCiudad() {
        RepositorioCiudad repositorioMock = mock(RepositorioCiudad.class);
        ServicioCiudad servicio = new ServicioCiudadImpl(repositorioMock);
        Long idInexistente = 999L;
        when(repositorioMock.buscarPorId(idInexistente)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> servicio.buscarPorId(idInexistente));
        verify(repositorioMock).buscarPorId(idInexistente);
    }

}
