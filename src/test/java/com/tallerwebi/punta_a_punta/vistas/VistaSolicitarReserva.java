package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaSolicitarReserva extends VistaWeb {

    public VistaSolicitarReserva(Page page) {
        super(page);
    }

    public void darClickEnConfirmarSolicitud() {
        darClickEnElElemento("#btn-confirmar-solicitud");
    }

    public void darClickEnCancelarSolicitud() {
        darClickEnElElemento("#btn-cancelar-solicitud");
    }
}
