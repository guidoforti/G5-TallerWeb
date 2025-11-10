package com.tallerwebi.dominio.ServiceImpl;

import com.tallerwebi.dominio.Entity.Notificacion;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Enums.TipoNotificacion;
import com.tallerwebi.dominio.IRepository.RepositorioNotificacion;
import com.tallerwebi.dominio.IRepository.RepositorioUsuario;
import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.presentacion.DTO.OutputsDTO.NotificacionOutputDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("servicioNotificacion")
@Transactional
public class ServicioNotificacionImpl implements ServicioNotificacion {

    private final RepositorioNotificacion repositorioNotificacion;
    private final RepositorioUsuario repositorioUsuario;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ServicioNotificacionImpl(
            RepositorioNotificacion repositorioNotificacion,
            RepositorioUsuario repositorioUsuario,
            SimpMessagingTemplate messagingTemplate // Inyección de WebSocket
    ) {
        this.repositorioNotificacion = repositorioNotificacion;
        this.repositorioUsuario = repositorioUsuario;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void crearYEnviar(Usuario destinatario, TipoNotificacion tipo, String mensaje, String urlDestino) {
        // 1. Persistencia: Crear y guardar la Notificación
        Notificacion notificacion = new Notificacion();
        notificacion.setDestinatario(destinatario);
        notificacion.setTipo(tipo);
        notificacion.setMensaje(mensaje);
        notificacion.setUrlDestino(urlDestino);
        notificacion.setLeida(false);

        repositorioNotificacion.guardar(notificacion);

        // 2. Tiempo Real: Enviar por WebSocket
        Long idDestinatario = destinatario.getId();
        String destino = "/topic/notificaciones/" + idDestinatario;

        // Crear el DTO que viaja por el socket (solo la info necesaria)
        NotificacionOutputDTO output = new NotificacionOutputDTO(
                notificacion.getMensaje(),
                notificacion.getUrlDestino(),
                notificacion.getId()
        );

        messagingTemplate.convertAndSend(destino, output);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarNoLeidas(Long idUsuario) throws NotFoundException {
        Usuario usuario = repositorioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado para notificaciones."));

        return repositorioNotificacion.contarNoLeidasPorUsuario(usuario);
    }

    @Override
    @Transactional
    public void marcarComoLeida(Long idNotificacion) throws NotFoundException {
        Notificacion notificacion = repositorioNotificacion.buscarPorId(idNotificacion)
                .orElseThrow(() -> new NotFoundException("Notificación no encontrada."));

        if (!notificacion.getLeida()) {
            notificacion.setLeida(true);
            repositorioNotificacion.actualizar(notificacion);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> buscarUltimas(Long idUsuario) throws NotFoundException {
        Usuario usuario = repositorioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado para notificaciones."));

        return repositorioNotificacion.buscarPorUsuario(usuario, 10);
    }

    @Override
    @Transactional
    public List<Notificacion> obtenerYMarcarComoLeidas(Long idUsuario) throws NotFoundException {
        Usuario usuario = repositorioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado para obtener notificaciones."));

        List<Notificacion> notificaciones = repositorioNotificacion.buscarPorUsuario(usuario, 20);

        notificaciones.stream()
                .filter(n -> !n.getLeida())
                .forEach(n -> {
                    n.setLeida(true);
                    repositorioNotificacion.actualizar(n);
                });

        return notificaciones;
    }
}