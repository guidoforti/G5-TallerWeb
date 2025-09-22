package com.tallerwebi.presentacion;

import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.tallerwebi.dominio.Entity.Ubicacion;
import com.tallerwebi.infraestructura.RepositorioUbicacionImpl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.*;

public class ServicioUbicacionTest {

    @Test
public void queSeListanTodasLasUbicaciones() {
    // Instanciar repositorio con ubicaciones hardcodeadas
    // Llamar a listarTodas()
    // Verificar que devuelve todas las ubicaciones


        RepositorioUbicacionImpl repositorio = new RepositorioUbicacionImpl();

        List<Ubicacion> ubicaciones = repositorio.findAll();  
        
        assertThat(ubicaciones.size(), is(3));
}

@Test
public void queLasUbicacionesNoSeanNulas() {
    // Instanciar repositorio
    // Verificar que ninguna ubicación en el listado es null    

        RepositorioUbicacionImpl repositorio = new RepositorioUbicacionImpl();

       
        List<Ubicacion> ubicaciones = repositorio.findAll();

        assertThat(ubicaciones, everyItem(notNullValue()));
    }



@Test
public void queCadaUbicacionTengaDireccionValida() {
    // Listar ubicaciones
    // Verificar que todas tienen un campo direccion no vacío

        RepositorioUbicacionImpl repositorio = new RepositorioUbicacionImpl();

        List<Ubicacion> ubicaciones = repositorio.findAll();

        assertThat(ubicaciones, everyItem(hasProperty("direccion", not(emptyOrNullString()))));
}

}
