package com.tallerwebi.presentacion.DTO.OutputsDTO;

import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.presentacion.DTO.CiudadDTO;
import com.tallerwebi.presentacion.DTO.ParadaDTO;
import com.tallerwebi.presentacion.DTO.ViajeroDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetalleViajeOutputDTO {
    private CiudadDTO origen;
    private CiudadDTO destino;
    private VehiculoOutputDTO vehiculo;
    private List<ViajeroDTO> viajeros;
    private List<ParadaDTO> paradas;
    private LocalDateTime fechaHoraDeSalida;
    private Long conductorId;
    private String conductorNombre;
    private Integer asientosDisponibles;
    private EstadoDeViaje estado;
    private Integer duracionEstimadaMinutos;
    private String fechaSalidaFormato;
    private String horaLlegadaEstimada;
    private String duracionEstimadaFormato;

    public DetalleViajeOutputDTO (Viaje viaje, List<ViajeroDTO> viajeros) {
        this.origen = new CiudadDTO(viaje.getOrigen());
        this.destino = new CiudadDTO(viaje.getDestino());
        this.vehiculo = new VehiculoOutputDTO(viaje.getVehiculo());
        this.viajeros = viajeros;
        this.paradas = viaje.getParadas().stream()
                .map(p -> new ParadaDTO(p))
                .collect(Collectors.toList());
        this.fechaHoraDeSalida = viaje.getFechaHoraDeSalida();
        this.asientosDisponibles = viaje.getAsientosDisponibles();
        this.estado = viaje.getEstado();
        this.duracionEstimadaMinutos = viaje.getDuracionEstimadaMinutos();

        if (viaje.getConductor() != null) {
            this.conductorId = viaje.getConductor().getId();
            this.conductorNombre = viaje.getConductor().getNombre();
        } else {
            this.conductorNombre = "Conductor no asignado";
        }
        // Format duration as "X.X horas (Y minutos)"
        if (duracionEstimadaMinutos != null) {
            double horas = duracionEstimadaMinutos / 60.0;
            this.duracionEstimadaFormato = String.format("%.1f horas (%d minutos)", horas, duracionEstimadaMinutos);
        }

        // Format departure time and calculate ETA
        if (viaje.getFechaHoraDeSalida() != null) {
            this.fechaSalidaFormato = viaje.getFechaHoraDeSalida()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

            // Calculate and format estimated arrival time
            if (duracionEstimadaMinutos != null) {
                LocalDateTime llegadaEstimada = viaje.getFechaHoraDeSalida()
                    .plusMinutes(duracionEstimadaMinutos);
                this.horaLlegadaEstimada = llegadaEstimada
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
            }
        }
    }
}
