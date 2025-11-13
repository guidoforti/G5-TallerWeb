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
        darClickEnUltimoElemento("#btn-aceptar-reserva");
    }

    public void darClickEnRechazarReserva() {
        darClickEnElElemento("#btn-rechazar-reserva");
    }

    public void darClickEnGestionViaje() {
        darClickEnElElemento("#btn-gestion-viaje");
    }

    public void darClickEnConfirmarAceptar() {
        // Click the confirm button inside the currently visible modal
        darClickEnElElemento(".modal.show #btn-confirmar-aceptar");
    }

    public String obtenerMensajeExito() {
        return obtenerTextoDelElemento("#mensaje-exito");
    }
}
