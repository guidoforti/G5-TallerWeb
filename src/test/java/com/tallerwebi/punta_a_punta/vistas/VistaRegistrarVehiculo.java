package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaRegistrarVehiculo extends VistaWeb {

    public VistaRegistrarVehiculo(Page page) {
        super(page);
        this.page.navigate("localhost:8080/spring/vehiculos/registrar");
    }

    public void escribirModelo(String modelo) {
        escribirEnElElemento("#modelo", modelo);
    }

    public void escribirAnio(String anio) {
        escribirEnElElemento("#anio", anio);
    }

    public void escribirPatente(String patente) {
        escribirEnElElemento("#patente", patente);
    }

    public void seleccionarAsientosTotales(String asientos) {
        page.locator("#asientosTotales").selectOption(asientos);
    }

    public void darClickEnSubmit() {
        darClickEnElElemento("#btn-submit-vehiculo");
    }

    public void darClickEnCancelar() {
        darClickEnElElemento("#btn-cancelar-vehiculo");
    }

    public String obtenerMensajeError() {
        return obtenerTextoDelElemento("p.alert.alert-danger");
    }
}
