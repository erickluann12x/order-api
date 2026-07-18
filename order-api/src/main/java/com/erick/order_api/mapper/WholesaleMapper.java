package com.erick.order_api.mapper;

import com.erick.order_api.dto.WholesaleResponseDTO;
import com.erick.order_api.entity.WholesaleOrder;
import org.springframework.stereotype.Component;

@Component
public class WholesaleMapper {
    public WholesaleResponseDTO ToResponse(WholesaleOrder wholesaleOrder){
        return new WholesaleResponseDTO(wholesaleOrder.getId(),
                wholesaleOrder.getNomeCliente(),
                wholesaleOrder.getNomeVendedor(),
                wholesaleOrder.getMarca(),
                wholesaleOrder.getValorTotal(),
                wholesaleOrder.getNumeroCliente(),
                wholesaleOrder.getFotoUrl(),
                wholesaleOrder.getCreatedAt());
    }
}
