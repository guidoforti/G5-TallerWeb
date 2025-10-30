package com.tallerwebi.presentacion.DTO.InputsDTO;

import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViajeEdicionDTO {

    private Long id;
    private String nombreCiudadOrigen;
    private String nombreCiudadDestino;
    private Long vehiculoId;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaHoraDeSalida;
    private Double precio;
    private Integer asientosDisponibles;
    private List<String> nombreParadas = new ArrayList<>();
    private EstadoDeViaje estado = EstadoDeViaje.DISPONIBLE;

    public Viaje toEntity (Ciudad origen, Ciudad destino , Vehiculo vehiculo) {
        Viaje viaje = new Viaje();
        viaje.setId(this.id);
        viaje.setOrigen(origen);
        viaje.setDestino(destino);
        viaje.setVehiculo(vehiculo);
        viaje.setFechaHoraDeSalida(this.fechaHoraDeSalida);
        viaje.setPrecio(this.precio);
        viaje.setAsientosDisponibles(this.asientosDisponibles);
        viaje.setEstado(this.estado);
        return viaje;
    }
}
