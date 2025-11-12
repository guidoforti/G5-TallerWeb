package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Valoracion;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IRepository.RepositorioValoracion;
import com.tallerwebi.dominio.IRepository.RepositorioViajero;
import com.tallerwebi.dominio.IServicio.ServicioLogin;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.ServiceImpl.ServicioViajeroImpl;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.EdadInvalidaException;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
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
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ServicioViajeroTest {

    private RepositorioViajero repositorioMock;
    private ServicioViajero servicio;
    private ServicioLogin servicioLoginMock;
    private RepositorioValoracion repositorioValoracionMock;

    @BeforeEach
    void setUp() {
        repositorioMock = mock(RepositorioViajero.class);
        servicioLoginMock = Mockito.mock(ServicioLogin.class);
        repositorioValoracionMock = mock(RepositorioValoracion.class);
        servicio = new ServicioViajeroImpl(repositorioMock, servicioLoginMock, repositorioValoracionMock);
    }

    // --- HELPERS ---

    private Viaje crearViajeDummy(Long id) {
        Viaje viaje = new Viaje();
        viaje.setId(id);
        return viaje;
    }

    private LocalDate calcularFechaNacimiento(int edad) {
        return LocalDate.now().minusYears(edad).minusDays(1);
    }

    private Viajero crearViajeroBase(String nombre, LocalDate fechaNacimiento) {
        Viajero v = new Viajero();
        v.setNombre(nombre);
        v.setFechaNacimiento(fechaNacimiento);
        v.setEmail("test@mail.com");
        v.setContrasenia("123");
        v.setReservas(new ArrayList<>());
        return v;
    }

    // --- TESTS PARA REGISTRO Y VALIDACIÓN ---

    @Test
    void deberiaRegistrarViajeroSiNoExiste() throws UsuarioExistente, EdadInvalidaException, DatoObligatorioException {
        // Arrange
        Viajero v = crearViajeroBase("Ana", calcularFechaNacimiento(30));

        doNothing().when(servicioLoginMock).registrar(any(Viajero.class));

        // Act
        servicio.registrar(v);

        // Assert
        verify(servicioLoginMock, times(1)).registrar(v);
        assertThat(v.getRol(), equalTo("VIAJERO"));
        assertThat(v.getActivo(), equalTo(true));
    }

    @Test
    void noDeberiaRegistrarSiUsuarioYaExiste() throws UsuarioExistente {
        // Arrange
        Viajero nuevo = crearViajeroBase("Ana", calcularFechaNacimiento(30));
        nuevo.setEmail("ana@mail.com");

        doThrow(new UsuarioExistente("Ya existe un usuario con ese email")).when(servicioLoginMock).registrar(any(Viajero.class));

        // Act & Assert
        assertThrows(UsuarioExistente.class, () -> servicio.registrar(nuevo));
        verify(repositorioMock, never()).buscarPorId(anyLong());
    }

    @Test
    void noDeberiaRegistrarSiNombreEsNulo() throws UsuarioExistente{
        Viajero sinNombre = crearViajeroBase("Ana", calcularFechaNacimiento(25));
        sinNombre.setNombre(null);

        assertThrows(DatoObligatorioException.class, () -> servicio.registrar(sinNombre));
        verify(servicioLoginMock, never()).registrar(any());
    }

    @Test
    void noDeberiaRegistrarSiNombreEsVacio() throws UsuarioExistente{
        Viajero sinNombre = crearViajeroBase("Ana", calcularFechaNacimiento(25));
        sinNombre.setNombre(" ");

        assertThrows(DatoObligatorioException.class, () -> servicio.registrar(sinNombre));
        verify(servicioLoginMock, never()).registrar(any());
    }

    @Test
    void noDeberiaRegistrarSiEdadEsNula() throws UsuarioExistente{
        Viajero sinFecha = crearViajeroBase("Ana", null); // Fecha de nacimiento nula

        assertThrows(EdadInvalidaException.class, () -> servicio.registrar(sinFecha));
        verify(servicioLoginMock, never()).registrar(any());
    }

    @Test
    void noDeberiaRegistrarSiEdadEsMenorA18() throws UsuarioExistente{
        // Fecha que resulta en una edad de 17
        Viajero menor = crearViajeroBase("Ana", calcularFechaNacimiento(17));

        assertThrows(EdadInvalidaException.class, () -> servicio.registrar(menor));
        verify(servicioLoginMock, never()).registrar(any());
    }

    @Test
    void noDeberiaRegistrarSiEdadEsMayorA120() throws UsuarioExistente{
        // Fecha que resulta en una edad de 121
        Viajero anciano = crearViajeroBase("Ana", calcularFechaNacimiento(121));

        assertThrows(EdadInvalidaException.class, () -> servicio.registrar(anciano));
        verify(servicioLoginMock, never()).registrar(any());
    }

    // --- TESTS PARA OBTENER VIAJERO (Buscar por ID) ---

    @Test
    void obtenerViajero_existente_deberiaRetornarViajero() throws UsuarioInexistente {
        Long id = 1L;
        Viajero esperado = new Viajero();
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

    // --- TESTS PARA PERFIL Y VALORACIONES ---

    @Test
    void deberiaObtenerPerfilViajeroConPromedioYValoraciones() throws UsuarioInexistente {
        // Arrange
        Long ID_VIAJERO = 1L;
        Long ID_VIAJE = 100L;
        Viaje viajeDummy = crearViajeDummy(ID_VIAJE);

        Viajero viajero = new Viajero();
        viajero.setId(ID_VIAJERO);
        viajero.setNombre("Nacho");
        // Establecer Fecha de nacimiento para que getEdad() devuelva 25
        viajero.setFechaNacimiento(calcularFechaNacimiento(25));

        Usuario emisor1 = new Conductor(); emisor1.setNombre("Carlos");
        Usuario emisor2 = new Conductor(); emisor2.setNombre("Laura");

        // Valoraciones (CONSTRUCTOR: emisor, receptor, puntuacion, comentario, viaje)
        Valoracion valoracion1 = new Valoracion(emisor1, viajero, 5, "Excelente experiencia", viajeDummy);
        valoracion1.setFecha(LocalDate.now());

        Valoracion valoracion2 = new Valoracion(emisor2, viajero, 3, "Normal", viajeDummy);
        valoracion2.setFecha(LocalDate.now());

        // GIVEN
        when(repositorioMock.buscarPorId(ID_VIAJERO)).thenReturn(Optional.of(viajero));
        when(repositorioValoracionMock.findByReceptorId(ID_VIAJERO)).thenReturn(List.of(valoracion1, valoracion2));

        // WHEN
        ViajeroPerfilOutPutDTO resultado = servicio.obtenerPerfilViajero(ID_VIAJERO);

        // THEN
        assertThat(resultado, is(notNullValue()));
        assertThat(resultado.getNombre(), equalTo("Nacho"));
        // La edad calculada debe ser 25 (o 26 si el helper no es perfecto, pero verificamos si está cerca)
        assertThat(resultado.getEdad(), is(25));
        assertThat(resultado.getValoraciones(), hasSize(2));
        assertThat(resultado.getPromedioValoraciones(), closeTo(4.0, 0.01));

        List<ValoracionOutputDTO> valoracionesDTO = resultado.getValoraciones();

        assertThat(valoracionesDTO.get(0).getNombreReceptor(), equalTo("Nacho"));
        assertThat(valoracionesDTO.get(0).getNombreEmisor(), anyOf(equalTo("Carlos"), equalTo("Laura")));
        assertThat(valoracionesDTO.get(0).getPuntuacion(), anyOf(equalTo(5), equalTo(3)));

        verify(repositorioMock).buscarPorId(ID_VIAJERO);
        verify(repositorioValoracionMock).findByReceptorId(ID_VIAJERO);
    }

    @Test
    void obtenerPerfilViajeroConViajeroInexistenteDeberiaLanzarUsuarioInexistente() {
        // GIVEN
        Long viajeroInexistenteId = 99L;
        when(repositorioMock.buscarPorId(viajeroInexistenteId)).thenReturn(Optional.empty());

        // WHEN & THEN
        UsuarioInexistente excepcion = assertThrows(
                UsuarioInexistente.class,
                () -> servicio.obtenerPerfilViajero(viajeroInexistenteId)
        );

        assertThat(excepcion.getMessage(), containsString("No se encontró el viajero con id 99"));
        verify(repositorioMock).buscarPorId(viajeroInexistenteId);
    }
}