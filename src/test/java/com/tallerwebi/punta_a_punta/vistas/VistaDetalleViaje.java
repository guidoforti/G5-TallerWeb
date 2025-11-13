package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaDetalleViaje extends VistaWeb {

    public VistaDetalleViaje(Page page) {
        super(page);
    }

    // VIAJERO actions
    public void darClickEnSolicitarReserva() {
        darClickEnElElemento("#btn-solicitar-reserva");
    }

    public void darClickEnVolverHomeViajero() {
        darClickEnElElemento("#btn-volver-home-viajero");
    }

    // CONDUCTOR actions
    public void darClickEnIniciarViaje() {
        darClickEnElElemento("#btn-iniciar-viaje");
    }

    public void darClickEnConfirmarIniciarViaje() {
        darClickEnElElemento("#btn-confirm-iniciar-viaje");
    }

    public void darClickEnFinalizarViaje() {
        darClickEnElElemento("#btn-finalizar-viaje");
    }

    public void darClickEnConfirmarFinalizarViaje() {
        darClickEnElElemento("#btn-confirm-finalizar-viaje");
    }

    public void darClickEnVerViajerosConfirmados() {
        darClickEnElElemento("#btn-ver-viajeros-confirmados");
    }

    public void darClickEnVerHistorial() {
        darClickEnElElemento("#btn-ver-historial");
    }

    public void darClickEnVolverHomeConductor() {
        darClickEnElElemento("#btn-volver-home-conductor");
    }
}
