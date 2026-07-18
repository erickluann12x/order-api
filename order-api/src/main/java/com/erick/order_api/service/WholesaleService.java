package com.erick.order_api.service;

import com.erick.order_api.dto.WholesaleRequestDTO;
import com.erick.order_api.dto.WholesaleResponseDTO;
import com.erick.order_api.entity.User;
import com.erick.order_api.entity.WholesaleOrder;
import com.erick.order_api.exception.OrderNotFoundException;
import com.erick.order_api.mapper.WholesaleMapper;
import com.erick.order_api.repository.WholesaleOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
        return wholesaleOrderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(mapper::ToResponse)
                .toList();
    }

    public List<WholesaleResponseDTO> findByNameClient(String nomeCliente){

        return wholesaleOrderRepository.findByNomeClienteContainingIgnoreCase(nomeCliente)
                .stream()
                .map(mapper::ToResponse)
                .toList();

    }

    public List<WholesaleResponseDTO> findByNumberClient(String numeroCliente){

        return wholesaleOrderRepository.findByNumeroCliente(numeroCliente)
                .stream()
                .map(mapper::ToResponse)
                .toList();

    }

    public List<WholesaleResponseDTO> findByNameSeller(String nomeVendedor){
        return wholesaleOrderRepository.findByNomeVendedorContainingIgnoreCase(nomeVendedor)
                .stream()
                .map(mapper::ToResponse)
                .toList();
    }

    public WholesaleResponseDTO findById(UUID id){
        WholesaleOrder order = wholesaleOrderRepository.findById(id)
                .orElseThrow(()-> new OrderNotFoundException("Pedido não encontrado"));

        return mapper.ToResponse(order);
    }
}
