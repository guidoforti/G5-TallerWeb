package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaValorarViajeros extends VistaWeb {

    public VistaValorarViajeros(Page page) {
        super(page);
    }

    public void escribirPuntuacionViajero(int index, String puntuacion) {
        escribirEnElElemento("#puntuacion-viajero-" + index, puntuacion);
    }

    public void escribirComentarioViajero(int index, String comentario) {
        escribirEnElElemento("#comentario-viajero-" + index, comentario);
    }

    public void darClickEnEnviarValoraciones() {
        darClickEnElElemento("#btn-enviar-valoraciones-viajeros");
    }
}
