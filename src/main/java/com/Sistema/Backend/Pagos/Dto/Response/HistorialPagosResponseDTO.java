package com.Sistema.Backend.Pagos.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class HistorialPagosResponseDTO {

    private Page<PagoResponseDTO> paginas;
    private BigDecimal totalAcumuladoFiltro;
}
