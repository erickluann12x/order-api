package com.erick.order_api.service;

import com.erick.order_api.dto.WholesaleRequestDTO;
import com.erick.order_api.dto.WholesaleResponseDTO;
import com.erick.order_api.entity.WholesaleOrder;
import com.erick.order_api.mapper.WholesaleMapper;
import com.erick.order_api.repository.WholesaleOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WholesaleService {

    private final WholesaleOrderRepository wholesaleOrderRepository;
    private final S3Service s3Service;
    private final WholesaleMapper mapper;


    public WholesaleResponseDTO createOrder(WholesaleRequestDTO dto){
        String fotoUrl = s3Service.uploadFile(dto.foto());

        WholesaleOrder order = new WholesaleOrder();
                order.setNomeCliente(dto.nomeCliente());
                order.setNomeVendedor(dto.nomeVendedor());
                order.setMarca(dto.marca());
                order.setValorTotal(dto.valorTotal());
                order.setFotoUrl(fotoUrl);

                WholesaleOrder savedOrder = wholesaleOrderRepository.save(order);
                return mapper.ToResponse(savedOrder);
    }

    public List<WholesaleResponseDTO> listOrders() {
        return wholesaleOrderRepository.findAllByOrderByDataCriacaoDesc()
                .stream()
                .map(mapper::ToResponse)
                .toList();
    }

}
