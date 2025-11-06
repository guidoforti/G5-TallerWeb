package com.tallerwebi.dominio.ServiceImpl;

import java.util.List;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Valoracion;
import com.tallerwebi.dominio.IRepository.RepositorioUsuario;
import com.tallerwebi.dominio.IRepository.RepositorioValoracion;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioValoracion;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.DTO.InputsDTO.ValoracionNuevaInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ValoracionOutputDTO;

@Service("servicioValoracion")
@Transactional
public class ServicioValoracionImpl implements ServicioValoracion {

    private final RepositorioValoracion repositorioValoracion;
    private final RepositorioUsuario repositorioUsuario;
    private final ViajeRepository viajeRepository; 

    @Autowired
    public ServicioValoracionImpl(RepositorioValoracion repositorioValoracion,
                                  RepositorioUsuario repositorioUsuario,
                                  ViajeRepository viajeRepository) {
        this.repositorioValoracion = repositorioValoracion;
        this.repositorioUsuario = repositorioUsuario;
        this.viajeRepository = viajeRepository;
    }

    @Override
    public void valorarUsuario(Usuario emisor, ValoracionNuevaInputDTO dto)
            throws UsuarioInexistente, DatoObligatorioException {

        if (emisor.getId().equals(dto.getReceptorId())) {
            throw new DatoObligatorioException("Error. No podes valorarte a vos mismo");
        }

        if (dto.getPuntuacion() == null || dto.getPuntuacion() < 1 || dto.getPuntuacion() > 5) {
            throw new DatoObligatorioException("La valoracion debe estar entre 1 y 5");
        }

        if (dto.getComentario() == null || dto.getComentario().trim().isEmpty()) {
            throw new DatoObligatorioException("El comentario es obligatorio");
        }

        if (!viajeRepository.existeViajeFinalizadoYNoValorado(emisor.getId(), dto.getReceptorId())) {
            throw new DatoObligatorioException(
                "No hay un viaje concluido y pendiente de valoración entre usted y el usuario receptor."
            );
        }

        Usuario receptor = repositorioUsuario.buscarPorId(dto.getReceptorId())
                .orElseThrow(() -> new UsuarioInexistente("No se encontró el usuario receptor"));

        Valoracion valoracion = new Valoracion(emisor, receptor, dto.getPuntuacion(), dto.getComentario());
        repositorioValoracion.save(valoracion);
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
}
