package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Valoracion;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IRepository.RepositorioValoracion;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioLogin;
import com.tallerwebi.dominio.ServiceImpl.ServicioConductorImpl;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ConductorPerfilOutPutDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

class ServicioConductorTest {

    private RepositorioConductor repositorioMock;
    private ServicioConductor servicio;
    private ServicioLogin servicioLoginMock;
    private RepositorioValoracion repositorioValoracionMock;

    private final LocalDate FECHA_VENCIMIENTO_VALIDA = LocalDate.now().plusDays(10);
    private final LocalDate FECHA_NACIMIENTO_VALIDA = LocalDate.now().minusYears(30);
    private final LocalDate FECHA_NACIMIENTO_MENOR = LocalDate.now().minusYears(17);

    private Viaje crearViajeDummy(Long id) {
        Viaje viaje = new Viaje();
        viaje.setId(id);
        return viaje;
    }

    @BeforeEach
    void setUp() {
        repositorioMock = mock(RepositorioConductor.class);
        servicioLoginMock = mock(ServicioLogin.class);
        repositorioValoracionMock = mock(RepositorioValoracion.class);
        servicio = new ServicioConductorImpl(repositorioMock, servicioLoginMock, repositorioValoracionMock);
    }


    @Test
    void deberiaRegistrarConductorSiNoExiste() throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida, EdadInvalidaException, DatoObligatorioException {
        // Arrange
        Conductor nuevo = new Conductor();
        nuevo.setNombre("Ana");
        nuevo.setEmail("ana@mail.com");
        nuevo.setContrasenia("123");
        // 🔥 CAMBIO CLAVE: Usar Fecha de Nacimiento (equivalente a 30 años)
        nuevo.setFechaNacimiento(FECHA_NACIMIENTO_VALIDA);
        nuevo.setFechaDeVencimientoLicencia(FECHA_VENCIMIENTO_VALIDA);

        doNothing().when(servicioLoginMock).registrar(any(Conductor.class));

        // Act
        servicio.registrar(nuevo);

        // Assert
        verify(servicioLoginMock, times(1)).registrar(nuevo);
        assertThat(nuevo.getRol(), equalTo("CONDUCTOR"));
        assertThat(nuevo.getActivo(), equalTo(true));
    }

    @Test
    void noDeberiaRegistrarSiUsuarioYaExiste() throws UsuarioExistente, DatoObligatorioException {
        // Arrange
        Conductor nuevo = new Conductor();
        nuevo.setNombre("Ana");
        nuevo.setEmail("ana@mail.com");
        nuevo.setContrasenia("123");
        nuevo.setFechaNacimiento(FECHA_NACIMIENTO_VALIDA);
        nuevo.setFechaDeVencimientoLicencia(FECHA_VENCIMIENTO_VALIDA);

        doThrow(new UsuarioExistente("Ya existe un usuario con ese email"))
                .when(servicioLoginMock).registrar(any(Conductor.class));

        // Act & Assert
        assertThrows(UsuarioExistente.class,
                () -> servicio.registrar(nuevo));

        verify(repositorioMock, never()).buscarPorId(anyLong());
    }

    @Test
    void noDeberiaRegistrarConductorSiLicenciaEstaVencida() throws UsuarioExistente, DatoObligatorioException {
        // Arrange
        Conductor vencido = new Conductor();
        vencido.setNombre("Carlos");
        vencido.setEmail("carlos@mail.com");
        vencido.setContrasenia("1234");
        vencido.setFechaNacimiento(FECHA_NACIMIENTO_VALIDA);
        // Licencia pasada
        vencido.setFechaDeVencimientoLicencia(LocalDate.now().minusDays(1));

        // Act & Assert
        FechaDeVencimientoDeLicenciaInvalida exception = assertThrows(
                FechaDeVencimientoDeLicenciaInvalida.class,
                () -> servicio.registrar(vencido)
        );

        assertThat(exception.getMessage(), equalTo("La fecha de vencimiento de la licencia debe ser mayor a la actual"));
        verify(servicioLoginMock, never()).registrar(any());
    }

    @Test
    void noDeberiaRegistrarSiEdadEsMenorA18() throws UsuarioExistente {
        // Arrange
        Conductor menor = new Conductor();
        menor.setNombre("Ana");
        // Fecha que resulta en una edad de 17
        menor.setFechaNacimiento(FECHA_NACIMIENTO_MENOR);
        menor.setFechaDeVencimientoLicencia(FECHA_VENCIMIENTO_VALIDA);

        // Act & Assert
        EdadInvalidaException exception = assertThrows(
                EdadInvalidaException.class,
                () -> servicio.registrar(menor)
        );

        assertThat(exception.getMessage(), containsString("El usuario debe ser mayor de 18 años"));
        verify(servicioLoginMock, never()).registrar(any());
    }


    // 4. Cobertura: obtenerConductor() - Éxito
    @Test
    void obtenerConductor_existente_deberiaRetornarConductor() throws UsuarioInexistente {
        // Arrange
        Long id = 1L;
        Conductor esperado = new Conductor();
        esperado.setId(id);

        when(repositorioMock.buscarPorId(id)).thenReturn(Optional.of(esperado));

        // Act
        Conductor resultado = servicio.obtenerConductor(id);

        // Assert
        assertThat(resultado, equalTo(esperado));
        verify(repositorioMock).buscarPorId(id);
    }

    // 5. Cobertura: obtenerConductor() - Conductor Inexistente
    @Test
    void obtenerConductor_noExistente_deberiaLanzarExcepcion() {
        // Arrange
        Long id = 1L;
        when(repositorioMock.buscarPorId(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsuarioInexistente.class, () -> servicio.obtenerConductor(id));
        verify(repositorioMock).buscarPorId(id);
    }

    @Test
    public void deberiaObtenerPerfilDeConductorCorrectamente() throws UsuarioInexistente {
        // given
        Long conductorId = 1L;

        Conductor conductor = new Conductor();
        conductor.setId(conductorId);
        conductor.setNombre("Carlos");
        conductor.setFechaNacimiento(FECHA_NACIMIENTO_VALIDA);

        Viajero viajero = new Viajero();
        viajero.setNombre("Juan"); // emisor de la valoración

        Viaje viajeDummy = crearViajeDummy(10L);

        Valoracion valoracion1 = new Valoracion(viajero, conductor, 5, "Excelente viaje", viajeDummy);
        Valoracion valoracion2 = new Valoracion(viajero, conductor, 3, "Podría mejorar", viajeDummy);

        when(repositorioMock.buscarPorId(conductorId)).thenReturn(Optional.of(conductor));
        when(repositorioValoracionMock.findByReceptorId(conductorId))
                .thenReturn(Arrays.asList(valoracion1, valoracion2));

        // when
        ConductorPerfilOutPutDTO resultado = servicio.obtenerPerfilDeConductor(conductorId);

        // then
        assertThat(resultado, is(notNullValue()));
        assertThat(resultado.getPromedioValoraciones(), equalTo(4.0));
        assertThat(resultado.getValoraciones().size(), equalTo(2));
        assertThat(resultado.getEdad(), is(30));
    }
}