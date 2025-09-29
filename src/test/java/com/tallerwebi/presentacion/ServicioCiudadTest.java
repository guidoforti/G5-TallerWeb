package com.tallerwebi.presentacion;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.IRepository.RepositorioCiudad;
import com.tallerwebi.dominio.IServicio.ServicioCiudad;
import com.tallerwebi.dominio.ServiceImpl.ServicioCiudadImpl;
import com.tallerwebi.infraestructura.Datos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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



}
