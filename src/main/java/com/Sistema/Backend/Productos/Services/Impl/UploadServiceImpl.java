package com.Sistema.Backend.Productos.Services.Impl;

import com.Sistema.Backend.Productos.Services.UploadService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    private final Cloudinary cloudinary;

    public UploadServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }


    @Override
    public String subirImagen(MultipartFile archivo) throws IOException {
        log.info("Iniciando subida de archivo a Cloudinary. Nombre original: '{}'", archivo.getOriginalFilename());

        if (archivo.isEmpty()) {
            log.error("Fallo al subir archivo: El archivo binario está vacío");
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        // Subir a Cloudinary dentro de una carpeta organizada para tu proyecto
        Map options = ObjectUtils.asMap(
                "folder", "Gestion Pedidos/productos",
                "resource_type", "image"
        );

        // Subimos el arreglo de bytes del archivo directamente
        Map uploadResult = cloudinary.uploader().upload(archivo.getBytes(), options);

        String urlFinal = (String) uploadResult.get("secure_url");
        log.info("Archivo subido con éxito. URL generada: '{}'", urlFinal);

        return urlFinal;
    }
}
