package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaPublicarViaje extends VistaWeb {

    public VistaPublicarViaje(Page page) {
        super(page);
        this.page.navigate("localhost:8080/spring/viaje/publicar");
    }

    public void seleccionarOrigen(String nombreCiudad) {
        seleccionarEnTomSelect("nombreCiudadOrigen", nombreCiudad);
    }

    public void seleccionarDestino(String nombreCiudad) {
        seleccionarEnTomSelect("nombreCiudadDestino", nombreCiudad);
    }

    public void escribirFechaHoraSalida(String fechaHora) {
        escribirEnElElemento("#fechaHoraDeSalida", fechaHora);
    }

    public void seleccionarVehiculo(String valorVehiculo) {
        page.locator("#idVehiculo").selectOption(valorVehiculo);
    }

    public void escribirAsientosDisponibles(String asientos) {
        escribirEnElElemento("#asientosDisponibles", asientos);
    }

    public void escribirPrecio(String precio) {
        escribirEnElElemento("#precio", precio);
    }

    public void darClickEnAgregarParada() {
        darClickEnElElemento("#btnAgregarParada");
    }

    public void darClickEnSubmit() {
        darClickEnElElemento("#btn-submit-viaje");
    }

    public void darClickEnCancelar() {
        darClickEnElElemento("#btn-cancelar-viaje");
    }

    public String obtenerMensajeError() {
        return obtenerTextoDelElemento(".alert.alert-danger");
    }
}
