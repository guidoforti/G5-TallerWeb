package com.tallerwebi.dominio;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;

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

    // Mock: no existe ciudad con esas coordenadas
    when(repositorioMock.buscarPorCoordenadas(-32.8895f, -68.8458f)).thenReturn(null);
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

    // Mock: existe ciudad con esas coordenadas
    when(repositorioMock.buscarPorCoordenadas(-34.6037f, -58.3816f)).thenReturn(ciudadExistente);

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

    Ciudad ciudad2 = new Ciudad();
    ciudad2.setNombre("Rosario Centro");  // Nombre diferente
    ciudad2.setLatitud(-32.9442f);  // Mismas coordenadas
    ciudad2.setLongitud(-60.6505f);

    // Primera vez: no existe
    when(repositorioMock.buscarPorCoordenadas(-32.9442f, -60.6505f))
        .thenReturn(null)
        .thenReturn(ciudad1);  // Segunda vez: ya existe

    ciudad1.setId(10L);
    when(repositorioMock.guardarCiudad(ciudad1)).thenReturn(ciudad1);

    // Act
    Ciudad resultado1 = servicio.guardarCiudad(ciudad1);
    Ciudad resultado2 = servicio.guardarCiudad(ciudad2);

    // Assert
    assertEquals(10L, resultado1.getId());
    assertEquals(10L, resultado2.getId());
    assertEquals("Rosario", resultado2.getNombre());  // Retorna la primera, no la segunda
    verify(repositorioMock, times(1)).guardarCiudad(any());  // Solo se guarda una vez
}



}
