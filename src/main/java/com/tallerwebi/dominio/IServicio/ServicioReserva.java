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
     * @throws ViajeNoEncontradoException Si no se encuentra el viaje
     * @throws NotFoundException Si no se encuentra alguna entidad
     * @throws UsuarioNoAutorizadoException Si el usuario no está autorizado
     * @throws UsuarioInexistente Si el usuario no existe
     */
    Reserva solicitarReserva(Viaje viaje, Viajero viajero) throws ReservaYaExisteException, SinAsientosDisponiblesException, ViajeYaIniciadoException, DatoObligatorioException, ViajeNoEncontradoException, NotFoundException, UsuarioNoAutorizadoException, UsuarioInexistente;

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
    List<Viajero> obtenerViajerosConfirmados(Viaje viaje) throws ViajeNoEncontradoException, NotFoundException, UsuarioNoAutorizadoException;

    /**
     * Confirma una reserva pendiente y decrementa los asientos disponibles del viaje
     *
     * @param reservaId El ID de la reserva a confirmar
     * @param conductorId El ID del conductor que confirma
     * @throws NotFoundException Si no se encuentra la reserva
     * @throws UsuarioNoAutorizadoException Si el conductor no es dueño del viaje
     * @throws ReservaYaExisteException Si la reserva no está en estado PENDIENTE
     * @throws SinAsientosDisponiblesException Si no hay asientos disponibles
     * @throws ViajeNoEncontradoException Si no se encuentra el viaje
     */
    void confirmarReserva(Long reservaId, Long conductorId) throws NotFoundException, UsuarioNoAutorizadoException, ReservaYaExisteException, SinAsientosDisponiblesException, ViajeNoEncontradoException;

    /**
     * Rechaza una reserva pendiente con un motivo
     *
     * @param reservaId El ID de la reserva a rechazar
     * @param conductorId El ID del conductor que rechaza
     * @param motivo El motivo del rechazo (obligatorio)
     * @throws NotFoundException Si no se encuentra la reserva
     * @throws UsuarioNoAutorizadoException Si el conductor no es dueño del viaje
     * @throws ReservaYaExisteException Si la reserva no está en estado PENDIENTE
     * @throws DatoObligatorioException Si el motivo está vacío
     */
    void rechazarReserva(Long reservaId, Long conductorId, String motivo) throws NotFoundException, UsuarioNoAutorizadoException, ReservaYaExisteException, DatoObligatorioException;
}
