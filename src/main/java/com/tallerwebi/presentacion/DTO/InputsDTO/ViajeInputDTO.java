package com.tallerwebi.presentacion.DTO.InputsDTO;

import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.presentacion.DTO.CiudadDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ViajeInputDTO {

    private Long conductorId;
    private CiudadDTO ciudadOrigen;
    private CiudadDTO ciudadDestino;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaHoraDeSalida;

    private Double precio;
    private Integer asientosDisponibles;
    private Long idVehiculo;

    public Viaje toEntity() {
        Viaje viaje = new Viaje();
        viaje.setFechaHoraDeSalida(this.fechaHoraDeSalida);
        viaje.setPrecio(this.precio);
        viaje.setAsientosDisponibles(this.asientosDisponibles);
        viaje.setFechaDeCreacion(LocalDateTime.now());
        viaje.setEstado(EstadoDeViaje.DISPONIBLE);
        viaje.setViajeros(new ArrayList<>());
        viaje.setParadas(new ArrayList<>());
        // Origen y destino en null por ahora (hasta que se integre Nominatim)
        viaje.setOrigen(null);
        viaje.setDestino(null);
        // Conductor y Vehiculo se setean en el servicio
        return viaje;
    }
}
