package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.excepcion.*;

import java.util.List;

public interface ServicioReserva {

    /**
     * Solicita una reserva para un viaje específico.
     * La reserva se crea en estado PENDIENTE.
     *
     * @param viaje El viaje para el cual se solicita la reserva
     * @param viajero El viajero que solicita la reserva
     * @return La reserva creada
     * @throws ReservaYaExisteException Si ya existe una reserva para este viajero en este viaje
     * @throws SinAsientosDisponiblesException Si no hay asientos disponibles
     * @throws ViajeYaIniciadoException Si el viaje ya inició
     * @throws DatoObligatorioException Si faltan datos obligatorios
     */
    Reserva solicitarReserva(Viaje viaje, Viajero viajero) throws ReservaYaExisteException, SinAsientosDisponiblesException, ViajeYaIniciadoException, DatoObligatorioException;

    /**
     * Lista todas las reservas de un viaje ordenadas por fecha de solicitud (ASC)
     *
     * @param viaje El viaje
     * @return Lista de reservas
     */
    List<Reserva> listarReservasPorViaje(Viaje viaje);

    /**
     * Lista todas las reservas de un viajero ordenadas por fecha de solicitud (DESC)
     *
     * @param viajero El viajero
     * @return Lista de reservas
     */
    List<Reserva> listarReservasPorViajero(Viajero viajero);

    /**
     * Obtiene una reserva por su ID
     *
     * @param id El ID de la reserva
     * @return La reserva
     * @throws NotFoundException Si no se encuentra la reserva
     */
    Reserva obtenerReservaPorId(Long id) throws NotFoundException;

    /**
     * Obtiene los viajeros confirmados de un viaje (reservas en estado CONFIRMADA)
     *
     * @param viaje El viaje
     * @return Lista de viajeros confirmados
     */
    List<Viajero> obtenerViajerosConfirmados(Viaje viaje);
}
