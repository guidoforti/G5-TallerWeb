package com.tallerwebi.dominio;

import com.tallerwebi.dominio.IServicio.ServicioAlmacenamientoFoto;
import com.tallerwebi.dominio.ServiceImpl.ServicioAlmacenamientoFotoImpl;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

class ServicioAlmacenamientoFotoTest {

    private ServicioAlmacenamientoFoto servicioAlmacenamientoFoto;
    private MultipartFile fileMock;

    private static final String PUBLIC_PATH_PREFIX = "/img/profile_uploads/";

    @BeforeEach
    void setUp() {
        servicioAlmacenamientoFoto = new ServicioAlmacenamientoFotoImpl();
        fileMock = mock(MultipartFile.class);
    }


    @Test
    void deberiaGuardarArchivoYDevolverRutaPublicaConPrefijoCorrecto() throws IOException {
        // Arrange
        String originalFilename = "perfil.png";
        when(fileMock.isEmpty()).thenReturn(false);
        when(fileMock.getOriginalFilename()).thenReturn(originalFilename);

        doNothing().when(fileMock).transferTo(ArgumentMatchers.any(Path.class));

        // Act
        String resultadoUrl = servicioAlmacenamientoFoto.guardarArchivo(fileMock);

        // Assert
        assertThat(resultadoUrl, is(notNullValue()));
        assertThat(resultadoUrl, startsWith(PUBLIC_PATH_PREFIX));
        verify(fileMock, times(1)).transferTo(ArgumentMatchers.any(Path.class));
    }


    @Test
    void deberiaDevolverNullSiElArchivoEstaVacio() throws IOException {
        // Arrange
        when(fileMock.isEmpty()).thenReturn(true);

        // Act
        String resultadoUrl = servicioAlmacenamientoFoto.guardarArchivo(fileMock);

        // Assert
        assertThat(resultadoUrl, is(nullValue()));
        verify(fileMock, times(1)).isEmpty();
        verify(fileMock, never()).getOriginalFilename();
    }

    @Test
    void deberiaDevolverNullSiElArchivoEsNulo() throws IOException {
        // Act
        String resultadoUrl = servicioAlmacenamientoFoto.guardarArchivo(null);

        // Assert
        assertThat(resultadoUrl, is(nullValue()));
        verify(fileMock, never()).getOriginalFilename();
    }

    @Test
    void deberiaManejarArchivosSinExtension() throws IOException {
        // Arrange
        String originalFilename = "sin_extension";
        when(fileMock.isEmpty()).thenReturn(false);
        when(fileMock.getOriginalFilename()).thenReturn(originalFilename);
        doNothing().when(fileMock).transferTo(ArgumentMatchers.any(File.class));

        // Act
        String resultadoUrl = servicioAlmacenamientoFoto.guardarArchivo(fileMock);

        // Assert
        assertThat(resultadoUrl, is(notNullValue()));
        assertThat(resultadoUrl, not(endsWith(".")));
        assertThat(resultadoUrl, not(endsWith("/")));
    }

    @Test
    void deberiaPropagarIOExceptionSiFallaElGuardadoEnDisco() {
        // Arrange
        String originalFilename = "falla.jpg";
        when(fileMock.isEmpty()).thenReturn(false);
        when(fileMock.getOriginalFilename()).thenReturn(originalFilename);
        assertThrows(IOException.class, () -> {
            doThrow(new IOException("Simulated disk write failure")).when(fileMock).transferTo(ArgumentMatchers.any(Path.class));
            servicioAlmacenamientoFoto.guardarArchivo(fileMock);
        });
    }
}