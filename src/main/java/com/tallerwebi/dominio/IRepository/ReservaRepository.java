package com.tallerwebi.dominio.IRepository;

import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Enums.EstadoReserva;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository {

    /**
     * Busca una reserva específica por viaje y viajero
     * @param viaje El viaje
     * @param viajero El viajero
     * @return Optional con la reserva si existe
     */
    Optional<Reserva> findByViajeAndViajero(Viaje viaje, Viajero viajero);

    /**
     * Busca todas las reservas de un viaje
     * @param viaje El viaje
     * @return Lista de reservas
     */
    List<Reserva> findByViaje(Viaje viaje);

    /**
     * Busca todas las reservas de un viajero
     * @param viajero El viajero
     * @return Lista de reservas
     */
    List<Reserva> findByViajero(Viajero viajero);

    /**
     * Guarda una nueva reserva
     * @param reserva La reserva a guardar
     * @return La reserva guardada
     */
    Reserva save(Reserva reserva);

    /**
     * Busca una reserva por ID
     * @param id El ID de la reserva
     * @return Optional con la reserva si existe
     */
    Optional<Reserva> findById(Long id);

    /**
     * Actualiza una reserva existente
     * @param reserva La reserva a actualizar
     */
    void update(Reserva reserva);

    /**
     * Busca todas las reservas confirmadas de un viaje
     * @param viaje El viaje
     * @return Lista de reservas confirmadas
     */
    List<Reserva> findConfirmadasByViaje(Viaje viaje);

    /**
     * Busca todas las reservas de un viajero filtradas por estados específicos
     * ordenadas por fecha de salida del viaje (ASC - más cercano primero)
     * @param viajero El viajero
     * @param estados Lista de estados a filtrar (ej: PENDIENTE, RECHAZADA)
     * @return Lista de reservas ordenadas por fecha de salida del viaje
     */
    List<Reserva> findByViajeroAndEstadoInOrderByViajesFechaSalidaAsc(Viajero viajero, List<EstadoReserva> estados);

}
