package com.tallerwebi.dominio.ServiceImpl;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

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

@Service
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

        // 🚨 1. VALIDACIÓN DE AUTO-VALORACIÓN (Debe ir primero, ya que no depende de ningún dato del DTO)
        if (emisor.getId().equals(dto.getReceptorId())) {
            throw new DatoObligatorioException("Error. No podes valorarte a vos mismo");
        }

        // 🚨 2. VALIDACIÓN DE PUNTUACIÓN (Nulo o Rango)
        if (dto.getPuntuacion() == null || dto.getPuntuacion() < 1 || dto.getPuntuacion() > 5) {
            throw new DatoObligatorioException("La valoracion debe estar entre 1 y 5");
        }
        
        // 🚨 3. VALIDACIÓN DE COMENTARIO (Nulo o Vacío/Espacios)
        if (dto.getComentario() == null || dto.getComentario().trim().isEmpty()) {
            throw new DatoObligatorioException("El comentario es obligatorio");
        }

        // 4. VALIDACIÓN DE VIAJE CONCLUIDO Y NO VALORADO (Depende de los IDs de los usuarios)
        if (!viajeRepository.existeViajeFinalizadoYNoValorado(emisor.getId(), dto.getReceptorId())) {
            throw new DatoObligatorioException(
                "No hay un viaje concluido y pendiente de valoración entre usted y el usuario receptor."
            );
        }

        // 5. BÚSQUEDA DEL RECEPTOR
        Usuario receptor = repositorioUsuario.buscarPorId(dto.getReceptorId())
                .orElseThrow(() -> new UsuarioInexistente("No se encontró el usuario receptor"));

        // 6. GUARDAR
        Valoracion valoracion = new Valoracion(emisor, receptor, dto.getPuntuacion(), dto.getComentario());
        repositorioValoracion.save(valoracion);
    }

    @Override
    public List<ValoracionOutputDTO> obtenerValoracionesDeUsuario(Long usuarioId) {
        // repositorioValoracion devuelve la lista ordenada
        return repositorioValoracion.findByReceptorId(usuarioId)
                .stream()
                .map(ValoracionOutputDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public Double calcularPromedioValoraciones(Long usuarioId) {
        List<Valoracion> valoraciones = repositorioValoracion.findByReceptorId(usuarioId);
        return valoraciones.isEmpty()
        //devuelve 0.0 en vez de null
                ? 0.0
                : valoraciones.stream().mapToInt(Valoracion::getPuntuacion).average().orElse(0.0); 
    }
}
