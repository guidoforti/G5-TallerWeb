package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.*;
import org.junit.jupiter.api.*;

import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;

public class VistaViajeroE2E {

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
    void viajeroDeberiaBuscarViajeYSolicitarReserva() throws MalformedURLException {
        // GIVEN - Inicio de sesión como viajero
        dadoQueElViajeroInicioSesion("viajero@test.com", "test123");
        cuandoElViajeroTocaElBotonDeLogin();
        entoncesDeberiaSerRedirigidoAlHomeViajero();

        // WHEN - Navega a buscar viajes
        VistaHomeViajero vistaHome = new VistaHomeViajero(page);
        cuandoElViajeroNavegaABuscarViajes(vistaHome);

        // AND - Busca viajes
        VistaBuscarViaje vistaBuscar = new VistaBuscarViaje(page);
        cuandoElViajeroSeleccionaOrigen(vistaBuscar, "Buenos Aires");
        cuandoElViajeroSeleccionaDestino(vistaBuscar, "Cordoba");
        cuandoElViajeroEnviaElFormularioDeBusqueda(vistaBuscar);

        // AND - Ve el detalle del primer viaje
        cuandoElViajeroSeleccionaElPrimerViaje(vistaBuscar);

        // THEN - Debería estar en la página de detalle
        VistaDetalleViaje vistaDetalle = new VistaDetalleViaje(page);
        entoncesDeberiaEstarEnDetalleViaje();

        // WHEN - Solicita reserva
        cuandoElViajeroSolicitaReserva(vistaDetalle);

        // THEN - Debería estar en la página de solicitar reserva
        VistaSolicitarReserva vistaSolicitar = new VistaSolicitarReserva(page);
        entoncesDeberiaEstarEnSolicitarReserva();

        // WHEN - Confirma la solicitud
        cuandoElViajeroConfirmaLaSolicitud(vistaSolicitar);

        // THEN - Debería estar en la página de éxito con mensaje
        VistaReservaExitosa vistaExitosa = new VistaReservaExitosa(page);
        entoncesDeberiaVerMensajeDeExito(vistaExitosa);
    }

    // ============ MÉTODOS PRIVADOS (Given-When-Then) ============

    private void dadoQueElViajeroInicioSesion(String email, String clave) {
        vistaLogin.escribirEMAIL(email);
        vistaLogin.escribirClave(clave);
    }

    private void cuandoElViajeroTocaElBotonDeLogin() {
        vistaLogin.darClickEnIniciarSesion();
    }

    private void entoncesDeberiaSerRedirigidoAlHomeViajero() throws MalformedURLException {
        URL url = vistaLogin.obtenerURLActual();
        // Coincide con o sin parámetro jsessionid
        assertThat(url.getPath(), matchesPattern("^/spring/viajero/home(;jsessionid=.+)?$"));
    }

    private void cuandoElViajeroNavegaABuscarViajes(VistaHomeViajero vistaHome) {
        vistaHome.darClickEnBuscarViajes();
    }

    private void cuandoElViajeroSeleccionaOrigen(VistaBuscarViaje vistaBuscar, String ciudad) {
        vistaBuscar.seleccionarOrigen(ciudad);
    }

    private void cuandoElViajeroSeleccionaDestino(VistaBuscarViaje vistaBuscar, String ciudad) {
        vistaBuscar.seleccionarDestino(ciudad);
    }

    private void cuandoElViajeroEnviaElFormularioDeBusqueda(VistaBuscarViaje vistaBuscar) {
        vistaBuscar.darClickEnBuscar();
        // Espera a que la página recargue después del envío del formulario
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
        // Espera a que aparezcan los resultados de búsqueda
        page.waitForSelector("#btn-ver-detalle", new Page.WaitForSelectorOptions().setTimeout(10000));
    }

    private void cuandoElViajeroSeleccionaElPrimerViaje(VistaBuscarViaje vistaBuscar) {
        vistaBuscar.darClickEnVerDetalle();
    }

    private void entoncesDeberiaEstarEnDetalleViaje() throws MalformedURLException {
        URL url = new URL(page.url());
        assertThat(url.getPath(), containsString("/spring/viaje/detalle"));
    }

    private void cuandoElViajeroSolicitaReserva(VistaDetalleViaje vistaDetalle) {
        vistaDetalle.darClickEnSolicitarReserva();
    }

    private void entoncesDeberiaEstarEnSolicitarReserva() throws MalformedURLException {
        URL url = new URL(page.url());
        assertThat(url.getPath(), containsString("/spring/reserva/solicitar"));
    }

    private void cuandoElViajeroConfirmaLaSolicitud(VistaSolicitarReserva vistaSolicitar) {
        vistaSolicitar.darClickEnConfirmarSolicitud();
        // Espera a que cargue la página después del envío del formulario
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
        // Espera a que aparezca el mensaje de éxito
        page.waitForSelector("#mensaje-exito", new Page.WaitForSelectorOptions().setTimeout(10000));
    }

    private void entoncesDeberiaVerMensajeDeExito(VistaReservaExitosa vistaExitosa) {
        String mensaje = vistaExitosa.obtenerMensajeExito();
        assertThat(mensaje, containsString("Reserva solicitada exitosamente"));
    }
}
