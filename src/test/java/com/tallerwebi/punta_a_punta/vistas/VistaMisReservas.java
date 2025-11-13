package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaMisReservas extends VistaWeb {

    public VistaMisReservas(Page page) {
        super(page);
    }

    public void darClickEnVolverHome() {
        darClickEnElElemento("#btn-volver-home");
    }

    public void darClickEnAceptarReserva() {
        darClickEnElElemento("#btn-aceptar-reserva");
    }

    public void darClickEnRechazarReserva() {
        darClickEnElElemento("#btn-rechazar-reserva");
    }

    public void darClickEnGestionViaje() {
        darClickEnElElemento("#btn-gestion-viaje");
    }

    public void darClickEnConfirmarAceptar() {
        darClickEnElElemento("#btn-confirmar-aceptar");
    }

    public String obtenerMensajeExito() {
        return obtenerTextoDelElemento("#mensaje-exito");
    }
}
