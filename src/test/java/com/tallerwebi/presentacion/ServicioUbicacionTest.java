package com.tallerwebi.presentacion;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.tallerwebi.dominio.Entity.Ubicacion;
import com.tallerwebi.dominio.IRepository.RepositorioUbicacion;
import com.tallerwebi.dominio.IServicio.ServicioUbicacion;
import com.tallerwebi.dominio.ServiceImpl.ServicioUbicacionImpl;
import com.tallerwebi.infraestructura.Datos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ServicioUbicacionTest {

  @Test
    public void queSeListanTodasLasUbicaciones() {

    RepositorioUbicacion repositorioMock = mock(RepositorioUbicacion.class);
    ServicioUbicacion servicio = new ServicioUbicacionImpl(repositorioMock);

    List<Ubicacion> ubicacionesDePrueba = Datos.obtenerUbicaciones();
    when(repositorioMock.findAll()).thenReturn(ubicacionesDePrueba);

    List<Ubicacion> ubicaciones = servicio.listarTodas();

    assertThat(ubicaciones.size(), is(ubicacionesDePrueba.size()));
        
    }


@Test
public void queLasUbicacionesNoSeanNulas() {
   

    RepositorioUbicacion repositorioMock = mock(RepositorioUbicacion.class);
    ServicioUbicacion servicio = new ServicioUbicacionImpl(repositorioMock);

    when(repositorioMock.findAll()).thenReturn(Datos.obtenerUbicaciones());

    List<Ubicacion> ubicaciones = servicio.listarTodas();

    assertThat(ubicaciones, everyItem(notNullValue()));

    }



@Test
public void queCadaUbicacionTengaDireccionValida() {
    
   RepositorioUbicacion repositorioMock = mock(RepositorioUbicacion.class);
    ServicioUbicacion servicio = new ServicioUbicacionImpl(repositorioMock);
    
    List<Ubicacion> ubicacionesDePrueba = Datos.obtenerUbicaciones();

    when(repositorioMock.findAll()).thenReturn(ubicacionesDePrueba);

    List<Ubicacion> ubicaciones = servicio.listarTodas();

    for (Ubicacion ubicacion : ubicaciones) {
        assertThat(ubicacion.getDireccion(), allOf(notNullValue(), not(isEmptyString())));
    }


}



}
