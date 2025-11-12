package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Notificacion;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Enums.TipoNotificacion;
import com.tallerwebi.dominio.IRepository.RepositorioNotificacion;
import com.tallerwebi.dominio.IRepository.RepositorioUsuario;
import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import com.tallerwebi.dominio.ServiceImpl.ServicioNotificacionImpl;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.presentacion.DTO.OutputsDTO.NotificacionOutputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ServicioNotificacionTest {

    private RepositorioNotificacion repositorioNotificacionMock;
    private RepositorioUsuario repositorioUsuarioMock;
    private SimpMessagingTemplate messagingTemplateMock;
    private ServicioNotificacion servicioNotificacion;

    private final Long USER_ID = 1L;
    private final Long NOTIFICACION_ID = 5L;
    private Usuario usuarioMock;
    private Notificacion notificacionMock;

    @BeforeEach
    void setUp() {
        repositorioNotificacionMock = mock(RepositorioNotificacion.class);
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        messagingTemplateMock = mock(SimpMessagingTemplate.class);

        servicioNotificacion = new ServicioNotificacionImpl(
                repositorioNotificacionMock,
                repositorioUsuarioMock,
                messagingTemplateMock
        );

        usuarioMock = mock(Usuario.class);
        when(usuarioMock.getId()).thenReturn(USER_ID);
        when(repositorioUsuarioMock.buscarPorId(USER_ID)).thenReturn(Optional.of(usuarioMock));

        notificacionMock = mock(Notificacion.class);
        when(notificacionMock.getId()).thenReturn(NOTIFICACION_ID);
    }

    @Test
    void deberiaGuardarNotificacionYEnviarPorWebSocket() {
        // Arrange
        String mensaje = "Nueva solicitud";
        String url = "/reserva/1";

        ArgumentCaptor<Notificacion> notificacionCaptor = ArgumentCaptor.forClass(Notificacion.class);

        doAnswer(invocation -> {
            Notificacion n = invocation.getArgument(0);
            n.setId(NOTIFICACION_ID);
            return null;
        }).when(repositorioNotificacionMock).guardar(notificacionCaptor.capture());

        // Act
        servicioNotificacion.crearYEnviar(usuarioMock, TipoNotificacion.RESERVA_SOLICITADA, mensaje, url);

        // Assert (Persistencia)
        verify(repositorioNotificacionMock, times(1)).guardar(Mockito.any(Notificacion.class));

        // Assert (WebSocket)
        String destinoEsperado = "/topic/notificaciones/1";
        verify(messagingTemplateMock, times(1)).convertAndSend(
                Mockito.eq(destinoEsperado),
                Mockito.any(NotificacionOutputDTO.class)
        );

        Notificacion guardada = notificacionCaptor.getValue();
        assertThat(guardada.getMensaje(), is(mensaje));
        assertThat(guardada.getLeida(), is(false));
    }

    @Test
    void deberiaContarNotificacionesNoLeidasParaUsuarioExistente() throws NotFoundException {
        // Arrange
        Long contadorEsperado = 3L;
        when(repositorioNotificacionMock.contarNoLeidasPorUsuario(usuarioMock)).thenReturn(contadorEsperado);

        // Act
        Long resultado = servicioNotificacion.contarNoLeidas(USER_ID);

        // Assert
        assertThat(resultado, is(contadorEsperado));
        verify(repositorioNotificacionMock, times(1)).contarNoLeidasPorUsuario(usuarioMock);
    }

    @Test
    void deberiaLanzarNotFoundExceptionSiUsuarioEsInexistenteAlContar() {
        // Arrange
        when(repositorioUsuarioMock.buscarPorId(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            servicioNotificacion.contarNoLeidas(USER_ID);
        });
        verify(repositorioNotificacionMock, never()).contarNoLeidasPorUsuario(Mockito.any());
    }


    @Test
    void deberiaMarcarNotificacionComoLeidaSiNoLoEstaba() throws NotFoundException {
        // Arrange
        when(repositorioNotificacionMock.buscarPorId(NOTIFICACION_ID)).thenReturn(Optional.of(notificacionMock));
        when(notificacionMock.getLeida()).thenReturn(false); // Estado actual: NO LEÍDA

        // Act
        servicioNotificacion.marcarComoLeida(NOTIFICACION_ID);

        // Assert
        verify(notificacionMock, times(1)).setLeida(true);
        verify(repositorioNotificacionMock, times(1)).actualizar(notificacionMock);
    }

    @Test
    void noDeberiaActualizarNotificacionSiYaEstabaLeida() throws NotFoundException {
        // Arrange
        when(repositorioNotificacionMock.buscarPorId(NOTIFICACION_ID)).thenReturn(Optional.of(notificacionMock));
        when(notificacionMock.getLeida()).thenReturn(true); // Estado actual: YA LEÍDA

        // Act
        servicioNotificacion.marcarComoLeida(NOTIFICACION_ID);

        // Assert
        verify(notificacionMock, never()).setLeida(true);
        verify(repositorioNotificacionMock, never()).actualizar(Mockito.any());
    }

    @Test
    void deberiaLanzarNotFoundExceptionSiNotificacionNoExisteAlMarcar() {
        // Arrange
        when(repositorioNotificacionMock.buscarPorId(NOTIFICACION_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            servicioNotificacion.marcarComoLeida(NOTIFICACION_ID);
        });
        verify(repositorioNotificacionMock, never()).actualizar(Mockito.any());
    }

    @Test
    void deberiaBuscarLasUltimasNotificacionesParaUsuarioExistente() throws NotFoundException {
        // Arrange
        List<Notificacion> listaEsperada = Arrays.asList(notificacionMock, notificacionMock);
        when(repositorioNotificacionMock.buscarPorUsuario(usuarioMock, 10)).thenReturn(listaEsperada);

        // Act
        List<Notificacion> resultado = servicioNotificacion.buscarUltimas(USER_ID);

        // Assert
        assertThat(resultado, is(listaEsperada));
        assertThat(resultado, hasSize(2));
        verify(repositorioNotificacionMock, times(1)).buscarPorUsuario(usuarioMock, 10);
    }

    @Test
    void deberiaLanzarNotFoundExceptionSiUsuarioEsInexistenteAlBuscarUltimas() {
        // Arrange
        when(repositorioUsuarioMock.buscarPorId(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            servicioNotificacion.buscarUltimas(USER_ID);
        });
        verify(repositorioNotificacionMock, never()).buscarPorUsuario(Mockito.any(), Mockito.anyInt());
    }

    @Test
    void deberiaObtenerYMarcarComoLeidasLasNotificaciones() throws NotFoundException {
        // Arrange
        Notificacion n1 = mock(Notificacion.class); // No leída
        when(n1.getLeida()).thenReturn(false);

        Notificacion n2 = mock(Notificacion.class); // Ya leída
        when(n2.getLeida()).thenReturn(true);

        List<Notificacion> listaMixta = Arrays.asList(n1, n2);

        when(repositorioNotificacionMock.buscarPorUsuario(usuarioMock, 20)).thenReturn(listaMixta);

        // Act
        List<Notificacion> resultado = servicioNotificacion.obtenerYMarcarComoLeidas(USER_ID);

        // Assert
        assertThat(resultado, hasSize(2));

        // Verificar que solo la no leída (n1) fue actualizada en el repositorio y marcada como leída
        verify(n1, times(1)).setLeida(true);
        verify(repositorioNotificacionMock, times(1)).actualizar(n1);

        // Verificar que la ya leída (n2) NO fue tocada
        verify(n2, never()).setLeida(true);
        verify(repositorioNotificacionMock, never()).actualizar(n2);
    }

    @Test
    void deberiaLanzarNotFoundExceptionSiUsuarioEsInexistenteAlObtenerYMarcar() {
        // Arrange
        when(repositorioUsuarioMock.buscarPorId(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            servicioNotificacion.obtenerYMarcarComoLeidas(USER_ID);
        });
        verify(repositorioNotificacionMock, never()).buscarPorUsuario(Mockito.any(), Mockito.anyInt());
    }
}