package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaEditarViaje extends VistaWeb {

    public VistaEditarViaje(Page page) {
        super(page);
    }

    public void escribirFechaHoraSalida(String fechaHora) {
        escribirEnElElemento("input[name='fechaHoraDeSalida']", fechaHora);
    }

    public void escribirPrecio(String precio) {
        escribirEnElElemento("input[name='precio']", precio);
    }

    public void escribirAsientosDisponibles(String asientos) {
        escribirEnElElemento("input[name='asientosDisponibles']", asientos);
    }

    public void darClickEnGuardarCambios() {
        darClickEnElElemento("#btn-guardar-cambios");
    }
}
