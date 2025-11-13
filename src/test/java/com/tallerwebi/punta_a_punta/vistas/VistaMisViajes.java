package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaMisViajes extends VistaWeb {

    public VistaMisViajes(Page page) {
        super(page);
    }

    public void darClickEnVerDetalleViaje() {
        // Click on the first detail button (uses class since IDs are unique per trip)
        darClickEnElElemento(".btn-ver-detalle-viaje");
    }

    public void darClickEnVerDetalleViajeConId(Long viajeId) {
        // Click on a specific trip's detail button using unique ID
        darClickEnElElemento("#btn-ver-detalle-viaje-" + viajeId);
    }
}
