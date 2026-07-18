package com.erick.order_api.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public record WholesaleRequestDTO(@NotBlank(message = "Nome do Cliente é obrigatorio") String nomeCliente,
                                  @NotBlank(message = "Nome do Vendedor é obrigatorio") String nomeVendedor,
                                  @NotBlank(message = "Nome da Marca é obrigatorio") String marca,
BigDecimal valorTotal, String numeroCliente, MultipartFile foto) {

}
