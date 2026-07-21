package com.Sistema.Backend;

import com.Sistema.Backend.Productos.Services.Impl.UploadServiceImpl;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UploadServiceImplTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader; // Mock del cliente interno de Cloudinary

    @InjectMocks
    private UploadServiceImpl uploadService;

    private MockMultipartFile archivoValido;
    private MockMultipartFile archivoVacio;

    @BeforeEach
    void setUp() {
        archivoValido = new MockMultipartFile(
                "archivo",
                "producto-tacos.jpg",
                "image/jpeg",
                "contenido-de-imagen-falsa".getBytes()
        );

        archivoVacio = new MockMultipartFile(
                "archivo",
                "vacio.jpg",
                "image/jpeg",
                new byte[0]
        );
    }

    // =========================================================================
    // PRUEBAS PARA: subirImagen()
    // =========================================================================
    @Nested
    @DisplayName("Pruebas para la subida de imágenes a Cloudinary")
    class SubirImagenTests {

        @Test
        @DisplayName("Debe subir exitosamente la imagen y retornar la URL segura (secure_url)")
        void subirImagen_Exito_RetornaUrlSegura() throws IOException {
            // Given
            String urlEsperada = "https://res.cloudinary.com/demo/image/upload/v123456/productos/tacos.jpg";
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("secure_url", urlEsperada);

            given(cloudinary.uploader()).willReturn(uploader);
            given(uploader.upload(any(byte[].class), any(Map.class))).willReturn(mockResponse);

            // When
            String urlResultado = uploadService.subirImagen(archivoValido);

            // Then
            assertThat(urlResultado).isNotNull().isEqualTo(urlEsperada);

            then(cloudinary).should(times(1)).uploader();
            then(uploader).should(times(1)).upload(eq(archivoValido.getBytes()), any(Map.class));
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el archivo está vacío")
        void subirImagen_ArchivoVacio_LanzaExcepcion() {
            // When / Then
            assertThatThrownBy(() -> uploadService.subirImagen(archivoVacio))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El archivo no puede estar vacío");

            then(cloudinary).should(never()).uploader();
        }

        @Test
        @DisplayName("Debe propagar IOException si ocurre un fallo durante la carga a Cloudinary")
        void subirImagen_ErrorCloudinary_PropagaIOException() throws IOException {
            // Given
            given(cloudinary.uploader()).willReturn(uploader);
            given(uploader.upload(any(byte[].class), any(Map.class)))
                    .willThrow(new IOException("Error de conexión con Cloudinary"));

            // When / Then
            assertThatThrownBy(() -> uploadService.subirImagen(archivoValido))
                    .isInstanceOf(IOException.class)
                    .hasMessage("Error de conexión con Cloudinary");

            then(uploader).should(times(1)).upload(any(byte[].class), any(Map.class));
        }

        @Test
        @DisplayName("Debe propagar IOException si falla la lectura de bytes del MultipartFile")
        void subirImagen_FalloLecturaBytes_PropagaIOException() throws IOException {
            // Given
            MultipartFile archivoConFalloRead = mock(MultipartFile.class);
            given(archivoConFalloRead.isEmpty()).willReturn(false);
            given(archivoConFalloRead.getOriginalFilename()).willReturn("corrupto.png");
            given(archivoConFalloRead.getBytes()).willThrow(new IOException("Error de I/O en disco"));

            // Si tu código llama a cloudinary.uploader(), le enseñamos qué mock devolver
            given(cloudinary.uploader()).willReturn(uploader);

            // When / Then
            assertThatThrownBy(() -> uploadService.subirImagen(archivoConFalloRead))
                    .isInstanceOf(IOException.class)
                    .hasMessage("Error de I/O en disco");

            // Lo que NUNCA debió ejecutarse es la subida del archivo a Cloudinary
            then(uploader).should(never()).upload(any(), any());
        }
    }
}
