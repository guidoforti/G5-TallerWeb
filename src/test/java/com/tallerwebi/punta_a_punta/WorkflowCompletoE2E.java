package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.*;
import org.junit.jupiter.api.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;

public class WorkflowCompletoE2E {

    static Playwright playwright;
    static Browser browser;
    BrowserContext conductorContext;
    BrowserContext viajeroContext;
    Page conductorPage;
    Page viajeroPage;

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500)
        );
    }

    @AfterAll
    static void cerrarNavegador() {
        playwright.close();
    }

    @BeforeEach
    void crearContextosYPaginas() {
        ReiniciarDB.limpiarBaseDeDatos();

        // Create two separate browser contexts (isolated sessions)
        conductorContext = browser.newContext();
        viajeroContext = browser.newContext();

        conductorPage = conductorContext.newPage();
        viajeroPage = viajeroContext.newPage();
    }

    @AfterEach
    void cerrarContextos() {
        conductorContext.close();
        viajeroContext.close();
    }

    @Test
    void workflowCompletoDeViajeConConductorYViajero() throws MalformedURLException {
        // ==================== FASE 1: CONDUCTOR CREA VIAJE ====================
        // GIVEN - Conductor logs in
        VistaLogin conductorLogin = new VistaLogin(conductorPage);
        dadoQueElConductorInicioSesion(conductorLogin, "conductor@test.com", "test123");

        // WHEN - Conductor creates vehicle
        VistaHomeConductor conductorHome = new VistaHomeConductor(conductorPage);
        cuandoElConductorNavegaARegistrarVehiculo();

        VistaRegistrarVehiculo vistaVehiculo = new VistaRegistrarVehiculo(conductorPage);
        cuandoElConductorRegistraVehiculo(vistaVehiculo, "Toyota Corolla", "2020", "XYZ789", "4");

        // AND - Conductor publishes trip (1 day ahead)
        conductorHome = new VistaHomeConductor(conductorPage);
        cuandoElConductorNavegaAPublicarViaje(conductorHome);

        VistaPublicarViaje vistaPublicar = new VistaPublicarViaje(conductorPage);
        String fechaManana = obtenerFechaMananaFormateada();
        cuandoElConductorPublicaViaje(vistaPublicar, "2", "Buenos Aires", "Mendoza", fechaManana, "2500", "3");

        // ==================== FASE 2: VIAJERO BUSCA Y RESERVA ====================
        // GIVEN - Viajero logs in
        VistaLogin viajeroLogin = new VistaLogin(viajeroPage);
        dadoQueElViajeroInicioSesion(viajeroLogin, "viajero@test.com", "test123");

        // WHEN - Viajero searches for trip
        VistaHomeViajero viajeroHome = new VistaHomeViajero(viajeroPage);
        cuandoElViajeroNavegaABuscarViajes(viajeroHome);

        VistaBuscarViaje vistaBuscar = new VistaBuscarViaje(viajeroPage);
        cuandoElViajeroBuscaViaje(vistaBuscar, "Buenos Aires", "Mendoza");

        // AND - Viajero requests reservation
        cuandoElViajeroSeleccionaPrimerViaje(vistaBuscar);

        VistaDetalleViaje viajeroDetalle = new VistaDetalleViaje(viajeroPage);
        cuandoElViajeroSolicitaReserva(viajeroDetalle);

        VistaSolicitarReserva vistaSolicitar = new VistaSolicitarReserva(viajeroPage);
        cuandoElViajeroConfirmaReserva(vistaSolicitar);

        // ==================== FASE 3: CONDUCTOR CONFIRMA RESERVA ====================
        // WHEN - Conductor navigates to reservations management
        conductorHome = new VistaHomeConductor(conductorPage);
        cuandoElConductorNavegaAGestionarReservas(conductorHome);

        VistaMisReservas vistaReservas = new VistaMisReservas(conductorPage);
        cuandoElConductorAceptaReserva(vistaReservas);

        // ==================== FASE 4: CONDUCTOR EDITA VIAJE A NOW ====================
        // WHEN - Conductor edits trip to change departure time to NOW
        cuandoElConductorNavegaAEditarViaje(4L); // Trip ID 4 (the one we created)

        VistaEditarViaje vistaEditar = new VistaEditarViaje(conductorPage);
        String fechaAhora = obtenerFechaAhoraFormateada();
        cuandoElConductorCambiaFechaDeViaje(vistaEditar, fechaAhora);

        // ==================== FASE 5: CONDUCTOR INICIA VIAJE ====================
        // WHEN - Conductor navigates to trip detail and starts trip
        conductorPage.navigate("localhost:8080/spring/viaje/detalle?id=4");
        conductorPage.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);

        VistaDetalleViaje conductorDetalle = new VistaDetalleViaje(conductorPage);
        cuandoElConductorIniciaViaje(conductorDetalle);

        // ==================== FASE 6: CONDUCTOR FINALIZA VIAJE ====================
        // WHEN - Conductor navigates back to trip detail and finishes trip
        conductorPage.navigate("localhost:8080/spring/viaje/detalle?id=4");
        conductorPage.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);

        conductorDetalle = new VistaDetalleViaje(conductorPage);
        cuandoElConductorFinalizaViaje(conductorDetalle);

        // ==================== FASE 7: VIAJERO VALORA CONDUCTOR ====================
        // WHEN - Viajero navigates to "Mis Viajes" and rates the conductor
        viajeroHome = new VistaHomeViajero(viajeroPage);
        viajeroHome.darClickEnMisViajes();
        viajeroPage.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);

        VistaMisViajes vistaMisViajes = new VistaMisViajes(viajeroPage);
        cuandoElViajeroValoraAlConductor(vistaMisViajes, 4L, "5", "Excelente viaje!");

        // ==================== FASE 8: CONDUCTOR VALORA VIAJEROS ====================
        // WHEN - Conductor rates the viajeros after trip completion
        // The conductor should still be on the accionViajeCompletada page after finishing the trip
        VistaAccionViajeCompletada vistaAccion = new VistaAccionViajeCompletada(conductorPage);
        cuandoElConductorValoraViajeros(vistaAccion);

        System.out.println("");
        System.out.println("=== ALL STEPS COMPLETED ===");
        System.out.println("✅ Conductor created trip (1 day ahead)");
        System.out.println("✅ Viajero made reservation");
        System.out.println("✅ Conductor accepted reservation");
        System.out.println("✅ Conductor changed trip time to NOW");
        System.out.println("✅ Conductor started trip");
        System.out.println("✅ Conductor finished trip");
        System.out.println("✅ Viajero rated conductor");
        System.out.println("✅ Conductor rated viajero");
    }

    // ============ CONDUCTOR HELPER METHODS ============

    private void dadoQueElConductorInicioSesion(VistaLogin login, String email, String clave) {
        login.escribirEMAIL(email);
        login.escribirClave(clave);
        login.darClickEnIniciarSesion();
        conductorPage.waitForURL("**/conductor/home**");
    }

    private void cuandoElConductorNavegaARegistrarVehiculo() {
        conductorPage.navigate("localhost:8080/spring/vehiculos/registrar");
    }

    private void cuandoElConductorRegistraVehiculo(VistaRegistrarVehiculo vista,
                                                     String modelo, String anio, String patente, String asientos) {
        vista.escribirModelo(modelo);
        vista.escribirAnio(anio);
        vista.escribirPatente(patente);
        vista.seleccionarAsientosTotales(asientos);
        vista.darClickEnSubmit();
        conductorPage.waitForURL("**/vehiculos/listarVehiculos**");
    }

    private void cuandoElConductorNavegaAPublicarViaje(VistaHomeConductor home) {
        home.darClickEnPublicarViaje();
    }

    private void cuandoElConductorPublicaViaje(VistaPublicarViaje vista, String idVehiculo,
                                                 String origen, String destino, String fecha,
                                                 String precio, String asientos) {
        vista.seleccionarVehiculo(idVehiculo);
        vista.seleccionarOrigen(origen);
        vista.seleccionarDestino(destino);
        vista.escribirFechaHoraSalida(fecha);
        vista.escribirPrecio(precio);
        vista.escribirAsientosDisponibles(asientos);
        vista.darClickEnSubmit();
        conductorPage.waitForURL("**/conductor/home**");
    }

    // ============ VIAJERO HELPER METHODS ============

    private void dadoQueElViajeroInicioSesion(VistaLogin login, String email, String clave) {
        login.escribirEMAIL(email);
        login.escribirClave(clave);
        login.darClickEnIniciarSesion();
        viajeroPage.waitForURL("**/viajero/home**");
    }

    private void cuandoElViajeroNavegaABuscarViajes(VistaHomeViajero home) {
        home.darClickEnBuscarViajes();
    }

    private void cuandoElViajeroBuscaViaje(VistaBuscarViaje vista, String origen, String destino) {
        vista.seleccionarOrigen(origen);
        vista.seleccionarDestino(destino);
        vista.darClickEnBuscar();
        viajeroPage.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
        viajeroPage.waitForSelector("#btn-ver-detalle",
            new Page.WaitForSelectorOptions().setTimeout(10000));
    }

    private void cuandoElViajeroSeleccionaPrimerViaje(VistaBuscarViaje vista) {
        vista.darClickEnVerDetalle();
    }

    private void cuandoElViajeroSolicitaReserva(VistaDetalleViaje vista) {
        vista.darClickEnSolicitarReserva();
    }

    private void cuandoElViajeroConfirmaReserva(VistaSolicitarReserva vista) {
        vista.darClickEnConfirmarSolicitud();
        viajeroPage.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
        viajeroPage.waitForSelector("#mensaje-exito",
            new Page.WaitForSelectorOptions().setTimeout(10000));
    }

    private void cuandoElConductorNavegaAGestionarReservas(VistaHomeConductor home) {
        home.darClickEnVerReservas();
    }

    private void cuandoElConductorAceptaReserva(VistaMisReservas vista) {
        vista.darClickEnAceptarReserva();
        // Wait for Bootstrap modal to be visible
        conductorPage.waitForSelector(".modal.show",
            new Page.WaitForSelectorOptions().setTimeout(3000));
        vista.darClickEnConfirmarAceptar();
        conductorPage.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }

    private void cuandoElConductorNavegaAEditarViaje(Long viajeId) {
        conductorPage.navigate("localhost:8080/spring/viaje/editar/" + viajeId);
        conductorPage.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
        // Wait for Tom Select and form to be fully initialized
        conductorPage.waitForSelector("#btn-guardar-cambios",
            new Page.WaitForSelectorOptions().setTimeout(10000));
    }

    private void cuandoElConductorCambiaFechaDeViaje(VistaEditarViaje vista, String nuevaFecha) {
        vista.escribirFechaHoraSalida(nuevaFecha);
        vista.darClickEnGuardarCambios();
        conductorPage.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }

    private void cuandoElConductorIniciaViaje(VistaDetalleViaje vista) {
        vista.darClickEnIniciarViaje();
        // Wait for Bootstrap modal to be visible
        conductorPage.waitForSelector(".modal.show",
            new Page.WaitForSelectorOptions().setTimeout(3000));
        vista.darClickEnConfirmarIniciarViaje();
        conductorPage.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }

    private void cuandoElConductorFinalizaViaje(VistaDetalleViaje vista) {
        vista.darClickEnFinalizarViaje();
        // Wait for Bootstrap modal to be visible
        conductorPage.waitForSelector(".modal.show",
            new Page.WaitForSelectorOptions().setTimeout(3000));
        vista.darClickEnConfirmarFinalizarViaje();
        conductorPage.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }

    private void cuandoElConductorValoraViajeros(VistaAccionViajeCompletada vista) {
        // Click the "Valorar Viajeros" button
        vista.darClickEnValorarViajeros();
        conductorPage.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);

        // Fill in the rating form for each viajero (in this test, there's only 1 viajero)
        VistaValorarViajeros vistaValorar = new VistaValorarViajeros(conductorPage);
        vistaValorar.escribirPuntuacionViajero(0, "5");
        vistaValorar.escribirComentarioViajero(0, "Excelente pasajero!");
        vistaValorar.darClickEnEnviarValoraciones();
        conductorPage.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }

    // ============ VIAJERO HELPER METHODS ============

    private void cuandoElViajeroValoraAlConductor(VistaMisViajes vistaMisViajes, Long viajeId,
                                                    String puntuacion, String comentario) {
        // Reload page to get fresh data from backend
        viajeroPage.reload();
        viajeroPage.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);

        // Wait for the rating button to appear (finalized trips section needs to load)
        // Increase timeout to ensure backend has processed the finalization
        viajeroPage.waitForSelector("#btn-valorar-conductor-" + viajeId,
            new Page.WaitForSelectorOptions().setTimeout(15000));

        vistaMisViajes.darClickEnValorarConductor(viajeId);
        viajeroPage.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);

        VistaValorarConductor vistaValorar = new VistaValorarConductor(viajeroPage);
        vistaValorar.escribirPuntuacion(puntuacion);
        vistaValorar.escribirComentario(comentario);
        vistaValorar.darClickEnEnviarValoracion();
        viajeroPage.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }

    // ============ UTILITY METHODS ============

    private String obtenerFechaMananaFormateada() {
        LocalDateTime manana = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return manana.format(formatter);
    }

    private String obtenerFechaAhoraFormateada() {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return ahora.format(formatter);
    }
}
