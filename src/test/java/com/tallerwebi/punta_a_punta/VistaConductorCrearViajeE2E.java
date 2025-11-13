package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.*;
import com.tallerwebi.punta_a_punta.vistas.VistaHomeConductor;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import com.tallerwebi.punta_a_punta.vistas.VistaPublicarViaje;
import com.tallerwebi.punta_a_punta.vistas.VistaRegistrarVehiculo;
import org.junit.jupiter.api.*;

import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;

public class VistaConductorCrearViajeE2E {

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
    void conductorDeberiaCrearVehiculoYPublicarViaje() throws MalformedURLException {
        // GIVEN - Login como conductor
        dadoQueElConductorCargaSusDatosDeLogin("conductor@test.com", "test123");
        cuandoElConductorTocaElBotonDeLogin();
        entoncesDeberiaSerRedirigidoAlHomeConductor();

        // WHEN - Navega a registrar vehículo
        VistaHomeConductor vistaHome = new VistaHomeConductor(page);
        cuandoElConductorNavegarARegistrarVehiculo(vistaHome);

        // AND - Registra un vehículo
        VistaRegistrarVehiculo vistaVehiculo = new VistaRegistrarVehiculo(page);
        cuandoElConductorLlenaElFormularioDeVehiculo(vistaVehiculo, "Toyota Corolla", "2020", "DEF456", "4");
        cuandoElConductorEnviaElFormularioDeVehiculo(vistaVehiculo);

        // THEN - Vuelve al home
        vistaHome = new VistaHomeConductor(page);

        // WHEN - Navega a publicar viaje
        cuandoElConductorNavegaAPublicarViaje(vistaHome);

        // AND - Llena el formulario de viaje
        VistaPublicarViaje vistaViaje = new VistaPublicarViaje(page);
        cuandoElConductorSeleccionaElVehiculo(vistaViaje, "2");
        cuandoElConductorSeleccionaOrigen(vistaViaje, "Buenos Aires");
        cuandoElConductorSeleccionaDestino(vistaViaje, "Mendoza");
        cuandoElConductorIngresaFechaSalida(vistaViaje, "2025-12-25T14:00");
        cuandoElConductorIngresaPrecioYAsientos(vistaViaje, "1500", "3");
        cuandoElConductorEnviaElFormularioDeViaje(vistaViaje);

        // THEN - Debe redirigir al home del conductor
        entoncesDeberiaSerRedirigidoAlHomeConductor();
    }

    // ============ MÉTODOS PRIVADOS (Given-When-Then) ============

    private void dadoQueElConductorCargaSusDatosDeLogin(String email, String clave) {
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
        assertThat(url.getPath(), matchesPattern("^/spring/conductor/home(?:;jsessionid=[^/\\s]+)?$"));
    }

    private void cuandoElConductorNavegarARegistrarVehiculo(VistaHomeConductor vistaHome) {
        // Navigate using UI buttons: Home -> Ver Vehículos -> Registrar Vehículo
        vistaHome.darClickEnVerVehiculos();
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);

        VistaListarVehiculos vistaListar = new VistaListarVehiculos(page);
        vistaListar.darClickEnRegistrarVehiculo();
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }

    private void cuandoElConductorLlenaElFormularioDeVehiculo(
            VistaRegistrarVehiculo vistaVehiculo, String modelo, String anio, String patente, String asientos) {
        vistaVehiculo.escribirModelo(modelo);
        vistaVehiculo.escribirAnio(anio);
        vistaVehiculo.escribirPatente(patente);
        vistaVehiculo.seleccionarAsientosTotales(asientos);
    }

    private void cuandoElConductorEnviaElFormularioDeVehiculo(VistaRegistrarVehiculo vistaVehiculo) {
        vistaVehiculo.darClickEnSubmit();
    }

    private void cuandoElConductorNavegaAPublicarViaje(VistaHomeConductor vistaHome) {
        vistaHome.darClickEnPublicarViaje();
    }

    private void cuandoElConductorSeleccionaElVehiculo(VistaPublicarViaje vistaViaje, String idVehiculo) {
        vistaViaje.seleccionarVehiculo(idVehiculo);
    }

    private void cuandoElConductorSeleccionaOrigen(VistaPublicarViaje vistaViaje, String ciudad) {
        vistaViaje.seleccionarOrigen(ciudad);
    }

    private void cuandoElConductorSeleccionaDestino(VistaPublicarViaje vistaViaje, String ciudad) {
        vistaViaje.seleccionarDestino(ciudad);
    }

    private void cuandoElConductorIngresaFechaSalida(VistaPublicarViaje vistaViaje, String fechaHora) {
        vistaViaje.escribirFechaHoraSalida(fechaHora);
    }

    private void cuandoElConductorIngresaPrecioYAsientos(VistaPublicarViaje vistaViaje, String precio, String asientos) {
        vistaViaje.escribirPrecio(precio);
        vistaViaje.escribirAsientosDisponibles(asientos);
    }

    private void cuandoElConductorEnviaElFormularioDeViaje(VistaPublicarViaje vistaViaje) {
        vistaViaje.darClickEnSubmit();
        // Wait for navigation to complete after form submission
        page.waitForURL("**/conductor/home**");
    }
}
