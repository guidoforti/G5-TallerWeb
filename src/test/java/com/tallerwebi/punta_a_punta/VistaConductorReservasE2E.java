package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.*;
import org.junit.jupiter.api.*;

import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;

public class VistaConductorReservasE2E {

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
    void conductorDeberiaGestionarReservasPendientes() throws MalformedURLException {
        // GIVEN - Inicio de sesión como conductor
        dadoQueElConductorInicioSesion("conductor@test.com", "test123");
        cuandoElConductorTocaElBotonDeLogin();
        entoncesDeberiaSerRedirigidoAlHomeConductor();

        // WHEN - Navega a ver mis reservas
        VistaHomeConductor vistaHome = new VistaHomeConductor(page);
        cuandoElConductorNavegaAVerMisReservas(vistaHome);

        // THEN - Debería ver la lista de reservas
        VistaMisReservas vistaMisReservas = new VistaMisReservas(page);
        entoncesDeberiaEstarEnMisReservas();

        // WHEN - Acepta la reserva pendiente
        cuandoElConductorAceptaLaReserva(vistaMisReservas);
        cuandoElConductorConfirmaLaAceptacion(vistaMisReservas);

        // THEN - Debería ver mensaje de éxito
        entoncesDeberiaVerMensajeDeConfirmacion(vistaMisReservas);
    }

    // ============ MÉTODOS PRIVADOS (Given-When-Then) ============

    private void dadoQueElConductorInicioSesion(String email, String clave) {
        vistaLogin.escribirEMAIL(email);
        vistaLogin.escribirClave(clave);
    }

    private void cuandoElConductorTocaElBotonDeLogin() {
        vistaLogin.darClickEnIniciarSesion();
    }

    private void entoncesDeberiaSerRedirigidoAlHomeConductor() throws MalformedURLException {
        URL url = vistaLogin.obtenerURLActual();
        // Coincide con o sin parámetro jsessionid
        assertThat(url.getPath(), matchesPattern("^/spring/conductor/home(;jsessionid=.+)?$"));
    }

    private void cuandoElConductorNavegaAVerMisReservas(VistaHomeConductor vistaHome) {
        vistaHome.darClickEnVerReservas();
    }

    private void entoncesDeberiaEstarEnMisReservas() throws MalformedURLException {
        URL url = new URL(page.url());
        assertThat(url.getPath(), containsString("/spring/reserva/misReservas"));
    }

    private void cuandoElConductorAceptaLaReserva(VistaMisReservas vistaMisReservas) {
        vistaMisReservas.darClickEnAceptarReserva();
        // Espera a que el modal esté visible
        page.waitForSelector("#btn-confirmar-aceptar", new Page.WaitForSelectorOptions().setTimeout(5000));
    }

    private void cuandoElConductorConfirmaLaAceptacion(VistaMisReservas vistaMisReservas) {
        vistaMisReservas.darClickEnConfirmarAceptar();
        // Espera a que cargue la página después del envío del formulario
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }

    private void entoncesDeberiaVerMensajeDeConfirmacion(VistaMisReservas vistaMisReservas) {
        // Espera a que aparezca el mensaje de éxito
        page.waitForSelector("#mensaje-exito", new Page.WaitForSelectorOptions().setTimeout(5000));
        String mensaje = vistaMisReservas.obtenerMensajeExito();
        assertThat(mensaje, containsString("Reserva confirmada exitosamente"));
    }
}
