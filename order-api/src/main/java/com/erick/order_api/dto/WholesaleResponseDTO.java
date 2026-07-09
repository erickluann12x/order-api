package com.erick.order_api.dto;

import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WholesaleResponseDTO(UUID id,
                                   String nomeCliente,
                                   String nomeVendedor,
                                   String marca,
                                   BigDecimal valorTotal,
                                   LocalDateTime createdAt,
                                   String fotoUrl) {
}
