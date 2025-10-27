package com.tallerwebi.dominio.Entity;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.tallerwebi.dominio.Enums.EstadoReserva;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "historial_reserva")
public class HistorialReserva {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Reserva reserva;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Viaje viaje;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Viajero viajero;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Usuario conductor;

    private LocalDateTime fechaEvento;

    @Enumerated(EnumType.STRING)
    private EstadoReserva estadoFinal;
}
