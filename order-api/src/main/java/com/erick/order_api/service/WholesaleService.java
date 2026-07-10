package com.erick.order_api.service;

import com.erick.order_api.dto.WholesaleRequestDTO;
import com.erick.order_api.dto.WholesaleResponseDTO;
import com.erick.order_api.entity.User;
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


    public WholesaleResponseDTO createOrder(WholesaleRequestDTO dto, User userLog){
        String fotoUrl = s3Service.uploadFile(dto.foto());

        WholesaleOrder order = new WholesaleOrder();
                order.setNomeCliente(dto.nomeCliente());
                order.setNomeVendedor(dto.nomeVendedor());
                order.setMarca(dto.marca());
                order.setValorTotal(dto.valorTotal());
                order.setNumeroCliente(dto.numeroCliente());
                order.setFotoUrl(fotoUrl);
                order.setUser(userLog);

                WholesaleOrder savedOrder = wholesaleOrderRepository.save(order);
                return mapper.ToResponse(savedOrder);
    }

    public List<WholesaleResponseDTO> listOrders() {
        return wholesaleOrderRepository.findAllByOrderByDataCriacaoDesc()
                .stream()
                .map(mapper::ToResponse)
                .toList();
    }

    public List<WholesaleResponseDTO> findByNameClient(String nomeCliente){

        return wholesaleOrderRepository.findByNomeCliente(nomeCliente)
                .stream()
                .map(mapper::ToResponse)
                .toList();

    }

    public List<WholesaleResponseDTO> findByNumberClient(String numeroCliente){

        return wholesaleOrderRepository.findByNomeCliente(numeroCliente)
                .stream()
                .map(mapper::ToResponse)
                .toList();

    }
}
