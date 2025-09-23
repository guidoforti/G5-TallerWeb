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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ServicioUbicacionTest {

  @Test
    public void queSeListanTodasLasUbicaciones() {

        RepositorioUbicacion repositorioMock = mock(RepositorioUbicacion.class);

        List<Ubicacion> ubicacionesDePrueba = Arrays.asList(
            new Ubicacion(), new Ubicacion(), new Ubicacion()
        );

        when(repositorioMock.findAll()).thenReturn(ubicacionesDePrueba);
        ServicioUbicacion servicio = new ServicioUbicacionImpl(repositorioMock);


        List<Ubicacion> ubicaciones = servicio.listarTodas();

        assertThat(ubicaciones.size(), is(3));
        
    }


@Test
public void queLasUbicacionesNoSeanNulas() {
    // Instanciar repositorio
    // Verificar que ninguna ubicación en el listado es null    

       RepositorioUbicacion repositorioMock = mock(RepositorioUbicacion.class);

        
        List<Ubicacion> ubicacionesDePrueba = Arrays.asList(
            new Ubicacion(), new Ubicacion(), new Ubicacion()
        );

        when(repositorioMock.findAll()).thenReturn(ubicacionesDePrueba);

        ServicioUbicacion servicio = new ServicioUbicacionImpl(repositorioMock);


        List<Ubicacion> ubicaciones = servicio.listarTodas();

        assertThat(ubicaciones, everyItem(notNullValue()));
    }


/* 
@Test
public void queCadaUbicacionTengaDireccionValida() {
    // Listar ubicaciones
    // Verificar que todas tienen un campo direccion no vacío

      

}

*/

}
