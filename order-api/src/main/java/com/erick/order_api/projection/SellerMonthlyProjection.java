package com.erick.order_api.projection;

import java.math.BigDecimal;

public interface SellerMonthlyProjection {
    String getNomeVendedor();
    Long getQuantidadePedidos();
    BigDecimal getValorTotal();
}
