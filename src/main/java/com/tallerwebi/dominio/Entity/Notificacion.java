package com.tallerwebi.dominio.Entity;

import com.tallerwebi.dominio.Enums.TipoNotificacion; // Necesitas crear este Enum
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICACION")
@Getter @Setter
@NoArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación ManyToOne con la superclase Usuario (destinatario puede ser Conductor o Viajero)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @Column(nullable = false)
    private String mensaje;

    // URL para redirigir (Deep Link)
    @Column(name = "url_destino", nullable = false)
    private String urlDestino;

    // Tipo de evento (ej: RESERVA_SOLICITADA, VIAJE_INICIADO)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacion tipo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean leida = false;
}