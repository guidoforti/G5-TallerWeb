package com.tallerwebi.dominio;

import com.tallerwebi.dominio.util.CalculadoraDistancia;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CalculadoraDistanciaTest {

    @Test
    public void deberiaCalcularDistanciaEntreBuenosAiresYRosario() {
        // given - Buenos Aires y Rosario coordinates
        double latBuenosAires = -34.6037;
        double lonBuenosAires = -58.3816;
        double latRosario = -32.9468;
        double lonRosario = -60.6393;

        // when
        double distancia = CalculadoraDistancia.calcularDistanciaHaversine(
            latBuenosAires, lonBuenosAires,
            latRosario, lonRosario
        );

        // then - real distance is ~278 km, allowing 5% margin
        assertThat(distancia, is(closeTo(278.0, 14.0)));
    }

    @Test
    public void deberiaCalcularDistanciaEntreBuenosAiresYCordoba() {
        // given
        double latBuenosAires = -34.6037;
        double lonBuenosAires = -58.3816;
        double latCordoba = -31.4201;
        double lonCordoba = -64.1888;

        // when
        double distancia = CalculadoraDistancia.calcularDistanciaHaversine(
            latBuenosAires, lonBuenosAires,
            latCordoba, lonCordoba
        );

        // then - real distance is ~645 km
        assertThat(distancia, is(closeTo(645.0, 32.0)));
    }

    @Test
    public void deberiaRetornarCeroCuandoLasCoordenadasSonIguales() {
        // given
        double lat = -34.6037;
        double lon = -58.3816;

        // when
        double distancia = CalculadoraDistancia.calcularDistanciaHaversine(
            lat, lon, lat, lon
        );

        // then
        assertThat(distancia, is(closeTo(0.0, 0.1)));
    }

    @Test
    public void deberiaCalcularDuracionEstimadaCorrecta() {
        // given - 300 km distance
        double distanciaKm = 300.0;

        // when
        int duracionMinutos = CalculadoraDistancia.calcularDuracionEstimadaMinutos(distanciaKm);

        // then - 300km * 1.3 (buffer) / 60 km/h = 6.5 hours = 390 minutes
        assertThat(duracionMinutos, is(390));
    }

    @Test
    public void deberiaRedondearHaciaArribaDuracionEstimada() {
        // given - distance that results in fractional minutes
        double distanciaKm = 100.0;

        // when
        int duracionMinutos = CalculadoraDistancia.calcularDuracionEstimadaMinutos(distanciaKm);

        // then - 100 * 1.3 / 60 = 2.166 hours = 130 minutes (rounded up)
        assertThat(duracionMinutos, is(130));
    }

    @Test
    public void deberiaManejarDistanciasCortas() {
        // given - 50 km distance
        double distanciaKm = 50.0;

        // when
        int duracionMinutos = CalculadoraDistancia.calcularDuracionEstimadaMinutos(distanciaKm);

        // then - 50 * 1.3 / 60 = 1.08 hours = 65 minutes
        assertThat(duracionMinutos, is(65));
    }

    @Test
    public void deberiaManejarDistanciasLargas() {
        // given - 1000 km distance
        double distanciaKm = 1000.0;

        // when
        int duracionMinutos = CalculadoraDistancia.calcularDuracionEstimadaMinutos(distanciaKm);

        // then - 1000 * 1.3 / 60 = 21.66 hours = 1300 minutes
        assertThat(duracionMinutos, is(1300));
    }
}
