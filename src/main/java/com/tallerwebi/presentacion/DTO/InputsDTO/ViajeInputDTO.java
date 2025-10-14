package com.tallerwebi.presentacion.DTO.InputsDTO;

import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.Entity.Parada;
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
@AllArgsConstructor
@NoArgsConstructor
public class ViajeInputDTO {

    private Long conductorId;

    // Nombres completos de ciudades elegidos desde autocomplete
    private String nombreCiudadOrigen;
    private String nombreCiudadDestino;
    private List<String> nombresParadas;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaHoraDeSalida;

    private Double precio;
    private Integer asientosDisponibles;
    private Long idVehiculo;

    /**
     * Convierte el DTO a entidad Viaje.
     * Recibe las ciudades ya resueltas como parámetros (responsabilidad del controlador).
     *
     * @param origen Ciudad de origen resuelta por el controlador
     * @param destino Ciudad de destino resuelta por el controlador
     * @param paradas Lista de paradas resueltas por el controlador
     * @return Viaje entity con todos los campos seteados
     */
    public Viaje toEntity(Ciudad origen, Ciudad destino, List<Parada> paradas) {
        Viaje viaje = new Viaje();
        viaje.setFechaHoraDeSalida(this.fechaHoraDeSalida);
        viaje.setPrecio(this.precio);
        viaje.setAsientosDisponibles(this.asientosDisponibles);
        viaje.setFechaDeCreacion(LocalDateTime.now());
        viaje.setEstado(EstadoDeViaje.DISPONIBLE);
        viaje.setViajeros(new ArrayList<>());

        // Setear ciudades resueltas por el controlador
        viaje.setOrigen(origen);
        viaje.setDestino(destino);
        viaje.setParadas(paradas != null ? paradas : new ArrayList<>());

        // Conductor y Vehiculo se setean en el servicio
        return viaje;
    }
}
