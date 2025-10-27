package com.tallerwebi.presentacion.DTO.InputsDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para recibir los datos al rechazar una reserva
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RechazoReservaInputDTO {

    private Long reservaId;
    private String motivo;
}
