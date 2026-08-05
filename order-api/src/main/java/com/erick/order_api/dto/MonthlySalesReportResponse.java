package com.erick.order_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MonthlySalesReportResponse {

    private int year;

    private int month;

    private long quantidadePedidos;

    private BigDecimal valorTotal;

    private List<
                SellerMonthlySummaryResponse
                > vendedores;
}
