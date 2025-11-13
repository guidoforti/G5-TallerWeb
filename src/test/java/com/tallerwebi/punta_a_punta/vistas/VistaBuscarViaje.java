package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaBuscarViaje extends VistaWeb {

    public VistaBuscarViaje(Page page) {
        super(page);
    }

    public void seleccionarOrigen(String nombreCiudad) {
        seleccionarEnTomSelect("nombreCiudadOrigen", nombreCiudad);
    }

    public void seleccionarDestino(String nombreCiudad) {
        seleccionarEnTomSelect("nombreCiudadDestino", nombreCiudad);
    }

    public void escribirFechaSalida(String fecha) {
        escribirEnElElemento("#fechaSalida", fecha);
    }

    public void escribirPrecioMin(String precio) {
        escribirEnElElemento("#precioMin", precio);
    }

    public void escribirPrecioMax(String precio) {
        escribirEnElElemento("#precioMax", precio);
    }

    public void darClickEnBuscar() {
        darClickEnElElemento("#btn-buscar-viajes");
    }

    public void darClickEnVolverHome() {
        darClickEnElElemento("#btn-volver-home");
    }

    public void darClickEnVerDetalle() {
        darClickEnElElemento("#btn-ver-detalle");
    }
}
