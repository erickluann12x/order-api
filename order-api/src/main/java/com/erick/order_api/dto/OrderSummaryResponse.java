package com.erick.order_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class OrderSummaryResponse {

    private long totalPedidos;

    private long cadastradosHoje;

    private BigDecimal valorTotal;

    private long totalVendedores;

}
