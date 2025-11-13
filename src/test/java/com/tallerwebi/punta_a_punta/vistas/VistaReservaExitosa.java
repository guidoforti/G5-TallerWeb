package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaReservaExitosa extends VistaWeb {

    public VistaReservaExitosa(Page page) {
        super(page);
    }

    public String obtenerMensajeExito() {
        return obtenerTextoDelElemento("#mensaje-exito");
    }

    public void darClickEnVerMisReservas() {
        darClickEnElElemento("#btn-ver-mis-reservas");
    }

    public void darClickEnBuscarMasViajes() {
        darClickEnElElemento("#btn-buscar-mas-viajes");
    }
}
