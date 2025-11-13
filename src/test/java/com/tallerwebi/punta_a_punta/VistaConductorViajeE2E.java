package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.*;
import org.junit.jupiter.api.*;

import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;

public class VistaConductorViajeE2E {

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;
    VistaLogin vistaLogin;

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500));
    }

    @AfterAll
    static void cerrarNavegador() {
        playwright.close();
    }

    @BeforeEach
    void crearContextoYPagina() {
        ReiniciarDB.limpiarBaseDeDatos();

        context = browser.newContext();
        page = context.newPage();
        vistaLogin = new VistaLogin(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    @Test
    void conductorDeberiaIniciarYFinalizarViaje() throws MalformedURLException {
        // GIVEN - Inicio de sesión como conductor
        dadoQueElConductorInicioSesion("conductor@test.com", "test123");
        cuandoElConductorTocaElBotonDeLogin();
        entoncesDeberiaSerRedirigidoAlHomeConductor();

        // WHEN - Navega a mis viajes
        VistaHomeConductor vistaHome = new VistaHomeConductor(page);
        cuandoElConductorNavegaAMisViajes(vistaHome);

        // AND - Ve el detalle del viaje
        VistaMisViajes vistaMisViajes = new VistaMisViajes(page);
        cuandoElConductorVeElDetalleDelViaje(vistaMisViajes);

        // AND - Inicia el viaje
        VistaDetalleViaje vistaDetalle = new VistaDetalleViaje(page);
        cuandoElConductorIniciaElViaje(vistaDetalle);

        // THEN - Debería ver el botón de finalizar viaje
        entoncesDeberiaVerElBotonDeFinalizarViaje();

        // WHEN - Finaliza el viaje
        cuandoElConductorFinalizaElViaje(vistaDetalle);

        // THEN - Debería ver mensaje de éxito
        entoncesDeberiaVerMensajeDeViajeFinalizadoExitosamente();
    }

    // ============ MÉTODOS PRIVADOS (Given-When-Then) ============

    private void dadoQueElConductorInicioSesion(String email, String clave) {
        vistaLogin.escribirEMAIL(email);
        vistaLogin.escribirClave(clave);
    }

    private void cuandoElConductorTocaElBotonDeLogin() {
        vistaLogin.darClickEnIniciarSesion();
        // Wait for redirect to complete after form submission
        page.waitForURL("**/conductor/home**");
    }

    private void entoncesDeberiaSerRedirigidoAlHomeConductor() throws MalformedURLException {
        URL url = vistaLogin.obtenerURLActual();
        // Coincide con o sin parámetro jsessionid
        assertThat(url.getPath(), matchesPattern("^/spring/conductor/home(;jsessionid=.+)?$"));
    }

    private void cuandoElConductorNavegaAMisViajes(VistaHomeConductor vistaHome) {
        vistaHome.darClickEnMisViajes();
        // Espera a que la página de listado de viajes cargue
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
        // Espera a que aparezca al menos un botón de detalle
        page.waitForSelector(".btn-ver-detalle-viaje", new Page.WaitForSelectorOptions().setTimeout(5000));
    }

    private void cuandoElConductorVeElDetalleDelViaje(VistaMisViajes vistaMisViajes) {
        // Click on trip id=2 (the one that can be started immediately)
        vistaMisViajes.darClickEnVerDetalleViajeConId(2L);
        // Espera a que la página de detalle cargue
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }

    private void cuandoElConductorIniciaElViaje(VistaDetalleViaje vistaDetalle) {
        // Click button to open modal
        vistaDetalle.darClickEnIniciarViaje();

        // Wait for modal to appear
        page.waitForSelector("#confirmStartModal", new Page.WaitForSelectorOptions().setTimeout(2000));

        // Click confirmation button in modal
        vistaDetalle.darClickEnConfirmarIniciarViaje();

        // Wait for navigation/reload to complete
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);

        // Navigate back to trip detail page to verify state changed
        page.navigate("http://localhost:8080/spring/viaje/detalle?id=2");
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }

    private void entoncesDeberiaVerElBotonDeFinalizarViaje() {
        // Verifica que el botón de finalizar viaje esté visible
        page.waitForSelector("#btn-finalizar-viaje", new Page.WaitForSelectorOptions().setTimeout(5000));
    }

    private void cuandoElConductorFinalizaElViaje(VistaDetalleViaje vistaDetalle) {
        // Click button to open modal
        vistaDetalle.darClickEnFinalizarViaje();

        // Wait for modal to appear
        page.waitForSelector("#confirmFinishModal", new Page.WaitForSelectorOptions().setTimeout(2000));

        // Click confirmation button in modal
        vistaDetalle.darClickEnConfirmarFinalizarViaje();

        // Wait for navigation/reload to complete
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);

        // Navigate back to trip detail page to verify state changed
        page.navigate("http://localhost:8080/spring/viaje/detalle?id=2");
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }

    private void entoncesDeberiaVerMensajeDeViajeFinalizadoExitosamente() {
        // Verifica que aparezca un mensaje de éxito o que el estado sea FINALIZADO
        if (page.locator(".alert-success").count() > 0) {
            String mensaje = page.locator(".alert-success").textContent();
            assertThat(mensaje, containsString("finalizado"));
        } else {
            // Check trip state badge using semantic ID
            String estado = page.locator("#badge-estado-viaje").textContent();
            assertThat(estado.toLowerCase(), containsString("finalizado"));
        }
    }
}
