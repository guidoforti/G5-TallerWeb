package com.tallerwebi.dominio;

import com.tallerwebi.config.ManualModelMapper;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IRepository.RepositorioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.ServiceImpl.ServicioVehiculoImpl;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.PatenteDuplicadaException;
import com.tallerwebi.presentacion.DTO.InputsDTO.VehiculoInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.VehiculoOutputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ServicioVehiculoTest {


    @Mock
    private RepositorioVehiculo repositorioVehiculoMock;

    @Mock
    private RepositorioConductor repositorioConductorMock;

    @Mock
    private ManualModelMapper manualModelMapperMock;

    private ServicioVehiculo servicioVehiculo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servicioVehiculo = new ServicioVehiculoImpl(repositorioVehiculoMock, manualModelMapperMock, repositorioConductorMock);
    }

    @Test
    void obtenerVehiculoPorId() throws NotFoundException {
        Long id = 1L;
        Vehiculo vehiculo = new Vehiculo(id, null, "Toyota", "2020", "ABC123", 4, EstadoVerificacion.VERIFICADO);
        VehiculoOutputDTO dto = new VehiculoOutputDTO();
        dto.setModelo("Toyota");

        when(repositorioVehiculoMock.findById(id)).thenReturn(vehiculo);
        when(manualModelMapperMock.toVehiculoOutputDTO(vehiculo)).thenReturn(dto);

        VehiculoOutputDTO resultado = servicioVehiculo.getById(id);

        assertNotNull(resultado);
        assertEquals("Toyota", resultado.getModelo());
    }

    @Test
    void obtenerVehiculoPorPatente() throws NotFoundException {
        String patente = "ABC123";
        Vehiculo vehiculo = new Vehiculo(1L, null, "Toyota", "2020", patente, 4, EstadoVerificacion.VERIFICADO);
        VehiculoOutputDTO dto = new VehiculoOutputDTO();
        dto.setPatente(patente);

        when(repositorioVehiculoMock.encontrarVehiculoConPatente(patente)).thenReturn(vehiculo);
        when(manualModelMapperMock.toVehiculoOutputDTO(vehiculo)).thenReturn(dto);

        VehiculoOutputDTO resultado = servicioVehiculo.obtenerVehiculoConPatente(patente);

        assertNotNull(resultado);
        assertEquals(patente, resultado.getPatente());
    }

    @Test
    void guardarVehiculoCorrectamente() throws PatenteDuplicadaException, NotFoundException {
        Long conductorId = 1L;
        String patente = "ABC123";

        VehiculoInputDTO inputDTO = new VehiculoInputDTO();
        inputDTO.setPatente(patente);
        inputDTO.setModelo("Toyota");

        Conductor conductor = new Conductor();
        Vehiculo vehiculoGuardado = new Vehiculo(1L, conductor, "Toyota", "2020", patente, 4, EstadoVerificacion.PENDIENTE);
        VehiculoOutputDTO dto = new VehiculoOutputDTO();
        dto.setPatente(patente);

        when(repositorioConductorMock.buscarPorId(conductorId)).thenReturn(Optional.of(conductor));
        when(repositorioVehiculoMock.encontrarVehiculoConPatente(patente)).thenReturn(null);
        when(manualModelMapperMock.toVehiculo(any(VehiculoInputDTO.class), any(Conductor.class))).thenReturn(vehiculoGuardado);
        when(repositorioVehiculoMock.guardarVehiculo(any(Vehiculo.class))).thenReturn(vehiculoGuardado);
        when(manualModelMapperMock.toVehiculoOutputDTO(vehiculoGuardado)).thenReturn(dto);

        VehiculoOutputDTO resultado = servicioVehiculo.guardarVehiculo(inputDTO, conductorId);

        assertNotNull(resultado);
        assertEquals(patente, resultado.getPatente());
    }

    @Test
    void noGuardarVehiculoConPatenteDuplicada() {
        String patenteExistente = "ABC123";
        VehiculoInputDTO inputDTO = new VehiculoInputDTO();
        inputDTO.setPatente(patenteExistente);

        when(repositorioVehiculoMock.encontrarVehiculoConPatente(patenteExistente)).thenReturn(new Vehiculo());

        assertThrows(PatenteDuplicadaException.class,
                () -> servicioVehiculo.guardarVehiculo(inputDTO, 1L));
    }

    @Test
    void obtenerVehiculosPorConductor() {
        Long conductorId = 1L;
        List<Vehiculo> vehiculos = List.of(
                new Vehiculo(1L, null, "Toyota", "2020", "ABC123", 4, EstadoVerificacion.VERIFICADO)
        );
        VehiculoOutputDTO dto = new VehiculoOutputDTO();
        dto.setModelo("Toyota");

        when(repositorioVehiculoMock.obtenerVehiculosParaConductor(conductorId)).thenReturn(vehiculos);
        when(manualModelMapperMock.toVehiculoOutputDTO(any(Vehiculo.class))).thenReturn(dto);

        List<VehiculoOutputDTO> resultados = servicioVehiculo.obtenerVehiculosParaConductor(conductorId);

        assertEquals(1, resultados.size());
        assertEquals("Toyota", resultados.get(0).getModelo());
    }

}
