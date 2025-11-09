package com.tallerwebi.dominio.ServiceImpl;

import java.util.ArrayList;
import java.util.List;

import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.presentacion.DTO.InputsDTO.ValoracionIndividualInputDTO;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Entity.Valoracion;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.IRepository.RepositorioUsuario;
import com.tallerwebi.dominio.IRepository.RepositorioValoracion;
import com.tallerwebi.dominio.IRepository.RepositorioViajero;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioValoracion;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;

@Service("servicioValoracion")
@Transactional
public class ServicioValoracionImpl implements ServicioValoracion {

    private final RepositorioValoracion repositorioValoracion;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioViajero repositorioViajero;
    private final ViajeRepository viajeRepository;

    @Autowired
    public ServicioValoracionImpl(RepositorioValoracion repositorioValoracion,
                                  RepositorioUsuario repositorioUsuario,
                                  RepositorioViajero repositorioViajero,
                                  ViajeRepository viajeRepository) {
        this.repositorioValoracion = repositorioValoracion;
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioViajero = repositorioViajero;
        this.viajeRepository = viajeRepository;
    }

    @Override
    public void valorarUsuario(Usuario emisor, ValoracionIndividualInputDTO dto, Long viajeId)
            throws UsuarioInexistente, DatoObligatorioException {
        if (emisor.getId().equals(dto.getReceptorId())) {
            throw new DatoObligatorioException("Error. No podes valorarte a vos mismo");
        }
        if (dto.getPuntuacion() == null || dto.getPuntuacion() < 1 || dto.getPuntuacion() > 5) {
            throw new DatoObligatorioException("La valoracion debe estar entre 1 y 5");
        }

        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new DatoObligatorioException("El Viaje no existe para registrar la valoración."));

        Usuario receptor = repositorioUsuario.buscarPorId(dto.getReceptorId())
                .orElseThrow(() -> new UsuarioInexistente("No se encontró el usuario receptor"));


        if (viaje.getEstado() != EstadoDeViaje.FINALIZADO) {
            throw new DatoObligatorioException("Solo puedes valorar viajes finalizados.");
        }

        if (repositorioValoracion.yaExisteValoracionParaViaje(emisor.getId(), receptor.getId(), viajeId)) {
            throw new DatoObligatorioException("Ya has valorado a este usuario para este viaje.");
        }
        Valoracion valoracion = new Valoracion(emisor, receptor, dto.getPuntuacion(), dto.getComentario(), viaje);
        repositorioValoracion.save(valoracion);
    }

    @Override
    @Transactional(readOnly = true)
    public Viajero obtenerViajero(Long viajeroId) throws UsuarioInexistente {
        return repositorioViajero.buscarPorId(viajeroId)
                .orElseThrow(() -> new UsuarioInexistente("El viajero no existe."));
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerUsuario(Long usuarioId) throws UsuarioInexistente {
        return repositorioUsuario.buscarPorId(usuarioId)
                .orElseThrow(() -> new UsuarioInexistente("El usuario no existe."));
    }

    @Override
        @Transactional(readOnly = true)
        public List<Valoracion> obtenerValoracionesDeUsuario(Long usuarioId) {
            List<Valoracion> valoraciones = repositorioValoracion.findByReceptorId(usuarioId);

        valoraciones.forEach(v -> {
            Hibernate.initialize(v.getEmisor());
            Hibernate.initialize(v.getReceptor());
        });


            return valoraciones;
        }


    @Override
    @Transactional(readOnly = true)
    public Double calcularPromedioValoraciones(Long usuarioId) {
        List<Valoracion> valoraciones = repositorioValoracion.findByReceptorId(usuarioId);

        valoraciones.forEach(v -> {
            Hibernate.initialize(v.getEmisor());
            Hibernate.initialize(v.getReceptor());
        });

        return valoraciones.isEmpty()
                ? 0.0
                : valoraciones.stream().mapToDouble(Valoracion::getPuntuacion).average().orElse(0.0); 
    }




    @Override
    @Transactional(readOnly = true)
    public List<Viajero> obtenerViajeros(Long viajeId) throws ViajeNoEncontradoException {
        // 1. Obtener el Viaje
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new ViajeNoEncontradoException("El viaje con ID " + viajeId + " no fue encontrado."));

        // 2. Obtener la lista de Reservas (relación lazy-loaded)
        List<Reserva> reservas = viaje.getReservas();

        if (reservas.isEmpty()) {
            return new ArrayList<>();
        }
        List<Viajero> viajeros = reservas.stream()
                .map(Reserva::getViajero)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());

        // Esto previene la LazyInitializationException en la vista (donde se accede a .nombre/.email)
        for (Viajero viajero : viajeros) {
            viajero.getNombre();
            viajero.getEmail();
        }

        return viajeros;
    }

   
    
}
