package com.erick.order_api.dto;

import com.erick.order_api.projection.SellerMonthlyProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class SellerMonthlySummaryResponse {

    private String nomeVendedor;

    private long quantidadePedidos;

    private BigDecimal valorTotal;

    public static SellerMonthlySummaryResponse
    from(
            SellerMonthlyProjection projection
    ) {
        return SellerMonthlySummaryResponse
                .builder()
                .nomeVendedor(
                        projection.getNomeVendedor()
                )
                .quantidadePedidos(
                        projection
                                .getQuantidadePedidos()
                )
                .valorTotal(
                        projection.getValorTotal()
                )
                .build();
    }
}
