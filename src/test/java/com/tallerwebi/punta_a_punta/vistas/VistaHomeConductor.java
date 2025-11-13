package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaHomeConductor extends VistaWeb {

    public VistaHomeConductor(Page page) {
        super(page);
        this.page.navigate("localhost:8080/spring/conductor/home");
    }

    public void darClickEnPublicarViaje() {
        darClickEnElElemento("#btn-publicar-viaje");
    }

    public void darClickEnVerVehiculos() {
        darClickEnElElemento("#btn-ver-vehiculos");
    }

    public void darClickEnMisViajes() {
        darClickEnElElemento("#btn-mis-viajes");
    }

    public void darClickEnVerReservas() {
        darClickEnElElemento("#btn-ver-reservas");
    }

    public void darClickEnMiPerfil() {
        darClickEnElElemento("#btn-mi-perfil");
    }

    public void darClickEnCerrarSesion() {
        darClickEnElElemento("#btn-cerrar-sesion");
    }

    public String obtenerNombreConductor() {
        return obtenerTextoDelElemento("h4 span");
    }
}
