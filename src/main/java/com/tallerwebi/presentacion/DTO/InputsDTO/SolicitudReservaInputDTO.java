package com.tallerwebi.presentacion.DTO.InputsDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para recibir una solicitud de reserva desde el frontend
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudReservaInputDTO {

    /**
     * ID del viaje para el cual se solicita la reserva
     */
    private Long viajeId;

    /**
     * ID del viajero que solicita la reserva
     * (típicamente se obtiene de la sesión)
     */
    private Long viajeroId;
}
