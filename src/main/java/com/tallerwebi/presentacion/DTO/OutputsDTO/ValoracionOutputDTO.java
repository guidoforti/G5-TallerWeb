package com.tallerwebi.presentacion.DTO.OutputsDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Valoracion;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValoracionOutputDTO {
    private Long id;
    private String nombreEmisor;
    private String nombreReceptor;
    private Integer puntuacion;
    private String comentario;
    private LocalDate fecha;

    public ValoracionOutputDTO(Valoracion valoracion) {
        this.id = valoracion.getId();
        this.nombreEmisor = valoracion.getEmisor().getNombre();
        this.nombreReceptor = valoracion.getReceptor().getNombre();
        this.puntuacion = valoracion.getPuntuacion();
        this.comentario = valoracion.getComentario();
        this.fecha = valoracion.getFecha();
    }
}
