package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaListarViajesConductor extends VistaWeb {

    public VistaListarViajesConductor(Page page) {
        super(page);
    }

    public void darClickEnVerDetalleViaje(Long viajeId) {
        darClickEnElElemento("#btn-ver-detalle-viaje-" + viajeId);
    }

    public void darClickEnEditarViaje(Long viajeId) {
        darClickEnElElemento("#btn-editar-viaje-" + viajeId);
    }
}
