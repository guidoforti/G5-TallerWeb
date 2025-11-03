package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Valoracion;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IRepository.RepositorioValoracion;
import com.tallerwebi.dominio.IRepository.RepositorioViajero;
import com.tallerwebi.dominio.IServicio.ServicioLogin; // Usado para el registro centralizado
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.ServiceImpl.ServicioViajeroImpl;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.EdadInvalidaException;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ValoracionOutputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeroPerfilOutPutDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ServicioViajeroTest {

    private RepositorioViajero repositorioMock;
    private ServicioViajero servicio;
    private ServicioLogin servicioLoginMock; // Renombrado a servicioLoginMock para claridad
    private RepositorioValoracion repositorioValoracionMock;

    @BeforeEach
    void setUp() {
        repositorioMock = mock(RepositorioViajero.class);
        servicioLoginMock = Mockito.mock(ServicioLogin.class); // Mock para el servicio centralizado
        repositorioValoracionMock = mock(RepositorioValoracion.class);
        servicio = new ServicioViajeroImpl(repositorioMock, servicioLoginMock, repositorioValoracionMock);
    }


    @Test
    void deberiaRegistrarViajeroSiNoExiste() throws UsuarioExistente, EdadInvalidaException, DatoObligatorioException {
        // Arrange
        Viajero v = new Viajero();
        v.setId(null);
        v.setNombre("Ana");
        v.setEdad(30);
        v.setEmail("ana@mail.com");
        v.setContrasenia("123");
        v.setReservas(new ArrayList<>());
        // El rol y activo se setean DENTRO del servicio

        // Simulamos que el ServicioLogin NO lanza UsuarioExistente, lo que implica registro exitoso
        doNothing().when(servicioLoginMock).registrar(any(Viajero.class));

        // Act
        servicio.registrar(v);

        // Assert
        // Verificamos que se llama al método centralizado de registro
        verify(servicioLoginMock, times(1)).registrar(v);
        // Verificamos que se asignaron los campos de Usuario dentro del servicio
        assertThat(v.getRol(), equalTo("VIAJERO"));
        assertThat(v.getActivo(), equalTo(true));
    }

    @Test
    void noDeberiaRegistrarSiUsuarioYaExiste() throws UsuarioExistente {
        // Arrange
        Viajero nuevo = new Viajero();
        nuevo.setId(null);
        nuevo.setNombre("Ana");
        nuevo.setEdad(30);
        nuevo.setEmail("ana@mail.com");
        nuevo.setContrasenia("123");
        nuevo.setReservas(new ArrayList<>());

        // El ServicioLogin debe lanzar la excepción para simular que el email ya existe
        doThrow(new UsuarioExistente("Ya existe un usuario con ese email"))
                .when(servicioLoginMock).registrar(any(Viajero.class));

        // Act & Assert
        assertThrows(UsuarioExistente.class,
                () -> servicio.registrar(nuevo));

        // Verificamos que el repositorio de rol NO fue llamado
        verify(repositorioMock, never()).buscarPorId(anyLong());
    }

    // --- Los tests de validación de negocio (Nombre, Edad) siguen siendo válidos ---
    // Ya que la validación ocurre ANTES de llamar a servicioLoginMock.registrar()

    @Test
    void noDeberiaRegistrarSiNombreEsNulo() throws UsuarioExistente {
        // ... (el código del test no cambia)
        Viajero sinNombre = new Viajero();
        sinNombre.setNombre(null);
        sinNombre.setEdad(25);
        // ... (otros campos)

        assertThrows(DatoObligatorioException.class,
                () -> servicio.registrar(sinNombre));

        // Verificamos que NUNCA se llama al servicio de login
        verify(servicioLoginMock, never()).registrar(any());
    }

    @Test
    void noDeberiaRegistrarSiNombreEsVacio() throws UsuarioExistente {
        // ... (el código del test no cambia)
        Viajero sinNombre = new Viajero();
        sinNombre.setNombre(" ");
        sinNombre.setEdad(25);
        // ... (otros campos)

        assertThrows(DatoObligatorioException.class,
                () -> servicio.registrar(sinNombre));

        verify(servicioLoginMock, never()).registrar(any());
    }

    @Test
    void noDeberiaRegistrarSiEdadEsNula() throws UsuarioExistente {
        // ... (el código del test no cambia)
        Viajero sinEdad = new Viajero();
        sinEdad.setNombre("Ana");
        sinEdad.setEdad(null);

        assertThrows(EdadInvalidaException.class,
                () -> servicio.registrar(sinEdad));

        verify(servicioLoginMock, never()).registrar(any());
    }

    @Test
    void noDeberiaRegistrarSiEdadEsMenorA18() throws UsuarioExistente {
        // ... (el código del test no cambia)
        Viajero menor = new Viajero();
        menor.setNombre("Ana");
        menor.setEdad(17);

        assertThrows(EdadInvalidaException.class,
                () -> servicio.registrar(menor));

        verify(servicioLoginMock, never()).registrar(any());
    }

    @Test
    void noDeberiaRegistrarSiEdadEsMayorA120() throws UsuarioExistente {
        // ... (el código del test no cambia)
        Viajero anciano = new Viajero();
        anciano.setNombre("Ana");
        anciano.setEdad(121);

        assertThrows(EdadInvalidaException.class,
                () -> servicio.registrar(anciano));

        verify(servicioLoginMock, never()).registrar(any());
    }

    // --- Los tests de obtenerViajero (Buscar por ID) siguen siendo válidos ---
    // Ya que RepositorioViajero SÍ mantiene el método buscarPorId(Long id)

    @Test
    void obtenerViajero_existente_deberiaRetornarViajero() throws UsuarioInexistente {
        Long id = 1L;
        Viajero esperado = new Viajero();
        // ... (setup de esperado)
        when(repositorioMock.buscarPorId(id)).thenReturn(Optional.of(esperado));

        Viajero resultado = servicio.obtenerViajero(id);

        assertThat(resultado, equalTo(esperado));
        verify(repositorioMock).buscarPorId(id);
    }

    @Test
    void obtenerViajero_noExistente_deberiaLanzarExcepcion() {
        Long id = 1L;
        when(repositorioMock.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(UsuarioInexistente.class, () -> servicio.obtenerViajero(id));
        verify(repositorioMock).buscarPorId(id);
    }

    @Test
    void deberiaObtenerPerfilViajeroConPromedioYValoraciones() throws UsuarioInexistente {
          // Datos del viajero
        Viajero viajero = new Viajero();
        viajero.setId(1L);
        viajero.setNombre("Nacho");
        viajero.setEdad(25);
        viajero.setFotoPerfilUrl("foto.jpg");

        // Datos de los usuarios emisores
        Usuario emisor1 = new Conductor();
        emisor1.setNombre("Carlos");

        Usuario emisor2 = new Conductor();
        emisor2.setNombre("Laura");

        // Valoraciones
        Valoracion valoracion1 = new Valoracion(emisor1, viajero, 5, "Excelente experiencia");
        valoracion1.setFecha(LocalDate.now());

        Valoracion valoracion2 = new Valoracion(emisor2, viajero, 3, "Normal");
        valoracion2.setFecha(LocalDate.now());

        // GIVEN
        when(repositorioMock.buscarPorId(1L)).thenReturn(Optional.of(viajero));
        when(repositorioValoracionMock.findByReceptorId(1L)).thenReturn(List.of(valoracion1, valoracion2));

        // WHEN
        ViajeroPerfilOutPutDTO resultado = servicio.obtenerPerfilViajero(1L);

        // THEN
        assertThat(resultado, is(notNullValue()));
        assertThat(resultado.getNombre(), equalTo("Nacho"));
        assertThat(resultado.getEdad(), equalTo(25));
        assertThat(resultado.getValoraciones(), hasSize(2));
        assertThat(resultado.getPromedioValoraciones(), closeTo(4.0, 0.01));

        List<ValoracionOutputDTO> valoracionesDTO = resultado.getValoraciones();

        assertThat(valoracionesDTO.get(0).getNombreReceptor(), equalTo("Nacho"));
        assertThat(valoracionesDTO.get(0).getNombreEmisor(), anyOf(equalTo("Carlos"), equalTo("Laura")));
        assertThat(valoracionesDTO.get(0).getPuntuacion(), anyOf(equalTo(5), equalTo(3)));

        verify(repositorioMock).buscarPorId(1L);
        verify(repositorioValoracionMock).findByReceptorId(1L);
    }

    @Test
    void obtenerPerfilViajeroConViajeroInexistenteDeberiaLanzarViajeroNoEncontradoException() {
    // GIVEN
    Long viajeroInexistenteId = 99L;
    when(repositorioMock.buscarPorId(viajeroInexistenteId)).thenReturn(Optional.empty());

    // WHEN & THEN
    UsuarioInexistente excepcion = assertThrows(
        UsuarioInexistente.class,
        () -> servicio.obtenerPerfilViajero(viajeroInexistenteId)
    );

    assertThat(excepcion.getMessage(), containsString("No se encontró el viajero con id 99"));
}
}