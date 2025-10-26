package com.tallerwebi.presentacion.DTO.OutputsDTO;

import com.tallerwebi.dominio.Entity.Reserva;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViajeroConfirmadoDTO {

    private Long reservaId;
    private String viajeroNombre;
    private String viajeroEmail;
    private String estadoPago;
    private String asistencia;

    public ViajeroConfirmadoDTO(Reserva reserva) {
        this.reservaId = reserva.getId();
        this.viajeroNombre = reserva.getViajero() != null && reserva.getViajero().getNombre() != null
            ? reserva.getViajero().getNombre()
            : "Desconocido";
        this.viajeroEmail = reserva.getViajero() != null && reserva.getViajero().getEmail() != null
            ? reserva.getViajero().getEmail()
            : "No disponible";
        this.estadoPago = reserva.getEstadoPago() != null
            ? reserva.getEstadoPago().name()
            : "NO_PAGADO";
        this.asistencia = reserva.getAsistencia() != null
            ? reserva.getAsistencia().name()
            : "NO_MARCADO";
    }
}
