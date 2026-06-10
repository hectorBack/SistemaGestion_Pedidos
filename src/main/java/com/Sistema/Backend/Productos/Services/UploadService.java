package com.Sistema.Backend.Productos.Services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UploadService {

    String subirImagen(MultipartFile archivo) throws IOException;
}
