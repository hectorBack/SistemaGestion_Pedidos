package com.Sistema.Backend.Productos.Controllers;

import com.Sistema.Backend.Productos.Services.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/imagen")
    public ResponseEntity<String> subirImagen(
            @RequestParam("archivo") MultipartFile archivo)
            throws IOException {

        String url = uploadService.subirImagen(archivo);

        return ResponseEntity.ok(url);
    }
}
