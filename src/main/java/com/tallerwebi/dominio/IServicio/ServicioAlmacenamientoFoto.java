package com.tallerwebi.dominio.IServicio;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface ServicioAlmacenamientoFoto {
    
    String guardarArchivo(MultipartFile file) throws IOException;
    
    // Si más adelante se añade lógica para eliminar o cargar archivos.
    // void eliminarArchivo(String url);
}
