package com.Sistema.Backend.Promociones.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PromocionStatsDTO {

    private Long total;
    private Long activas;
    private Long programadas;
    private Long expiradas;
}
