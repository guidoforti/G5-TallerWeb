package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaListarVehiculos extends VistaWeb {

    public VistaListarVehiculos(Page page) {
        super(page);
    }

    public void darClickEnRegistrarVehiculo() {
        darClickEnElElemento("#btn-registrar-vehiculo");
    }
}
