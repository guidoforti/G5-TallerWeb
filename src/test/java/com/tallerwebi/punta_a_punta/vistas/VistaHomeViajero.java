package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaHomeViajero extends VistaWeb {

    public VistaHomeViajero(Page page) {
        super(page);
        this.page.navigate("localhost:8080/spring/viajero/home");
    }

    public void darClickEnBuscarViajes() {
        darClickEnElElemento("#btn-buscar-viajes");
    }

    public void darClickEnMisReservasActivas() {
        darClickEnElElemento("#btn-mis-reservas-activas");
    }

    public void darClickEnMisViajes() {
        darClickEnElElemento("#btn-mis-viajes");
    }

    public void darClickEnMiPerfil() {
        darClickEnElElemento("#btn-mi-perfil");
    }

    public void darClickEnCerrarSesion() {
        darClickEnElElemento("#btn-cerrar-sesion");
    }
}
