package com.tallerwebi.dominio.ServiceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tallerwebi.dominio.IServicio.ServicioAlmacenamientoFoto;

@Service("servicioAlmacenamientoFoto")
@Transactional
public class ServicioAlmacenamientoFotoImpl implements ServicioAlmacenamientoFoto{

    // Ruta donde se guardarán los archivos dentro de webapp/resources/core/img/
    private static final String UPLOAD_DIR_PATH = Paths.get(
        System.getProperty("user.dir"),
        "src", "main", "webapp", "resources", "core", "img", "profile_uploads"
    ).toString();

    // Ruta pública para acceder a las imágenes desde el navegador
    private static final String PUBLIC_PATH_PREFIX = "/img/profile_uploads/";
    
    @Override
    public String guardarArchivo(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Crear el directorio si no existe
        Path uploadPath = Paths.get(UPLOAD_DIR_PATH);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 1. Generar nombre único
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // 2. Escribir el archivo en el disco
        Path filePath = uploadPath.resolve(uniqueFilename);
        file.transferTo(filePath);

        // 3. Devolver la URL relativa pública
        return PUBLIC_PATH_PREFIX + uniqueFilename;
    }
    }
    

