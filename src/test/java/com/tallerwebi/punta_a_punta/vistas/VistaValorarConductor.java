package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaValorarConductor extends VistaWeb {

    public VistaValorarConductor(Page page) {
        super(page);
    }

    public void escribirPuntuacion(String puntuacion) {
        escribirEnElElemento("#puntuacion", puntuacion);
    }

    public void escribirComentario(String comentario) {
        escribirEnElElemento("#comentario", comentario);
    }

    public void darClickEnEnviarValoracion() {
        darClickEnElElemento("#btn-enviar-valoracion");
    }
}
