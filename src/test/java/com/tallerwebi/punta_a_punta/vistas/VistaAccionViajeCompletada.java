package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaAccionViajeCompletada extends VistaWeb {

    public VistaAccionViajeCompletada(Page page) {
        super(page);
    }

    public void darClickEnValorarViajeros() {
        darClickEnElElemento("#btn-valorar-viajeros");
    }

    public void darClickEnVerDetalleViaje() {
        darClickEnElElemento("#btn-ver-detalle-viaje");
    }
}
