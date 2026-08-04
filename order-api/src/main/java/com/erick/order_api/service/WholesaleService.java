package com.erick.order_api.service;

import com.erick.order_api.dto.PageResponse;
import com.erick.order_api.dto.WholesaleRequestDTO;
import com.erick.order_api.dto.WholesaleResponseDTO;
import com.erick.order_api.entity.User;
import com.erick.order_api.entity.WholesaleOrder;
import com.erick.order_api.exception.OrderNotFoundException;
import com.erick.order_api.mapper.WholesaleMapper;
import com.erick.order_api.repository.WholesaleOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public PageResponse<WholesaleResponseDTO> listOrders(
            Pageable pageable
    ) {
        Page<WholesaleResponseDTO> page =
                wholesaleOrderRepository
                        .findAll(pageable)
                        .map(mapper::ToResponse);

        return PageResponse.from(page);
    }

    public PageResponse<WholesaleResponseDTO> findByNameClient(
            String nomeCliente,
            Pageable pageable
    ) {
        String nomeNormalizado = normalizeText(nomeCliente);

        Page<WholesaleResponseDTO> page =
                wholesaleOrderRepository
                        .findByNomeClienteContainingIgnoreCase(
                                nomeNormalizado,
                                pageable
                        )
                        .map(mapper::ToResponse);

        return PageResponse.from(page);
    }

    public PageResponse<WholesaleResponseDTO> findByNumberClient(
            String numeroCliente,
            Pageable pageable
    ) {
        String numeroNormalizado =
                normalizePhone(numeroCliente);

        Page<WholesaleResponseDTO> page =
                wholesaleOrderRepository
                        .findByNumeroClienteContainingIgnoreCase(
                                numeroNormalizado,
                                pageable
                        )
                        .map(mapper::ToResponse);

        return PageResponse.from(page);
    }

    public PageResponse<WholesaleResponseDTO> findByNameSeller(
            String nomeVendedor,
            Pageable pageable
    ) {
        String nomeNormalizado = normalizeText(nomeVendedor);

        Page<WholesaleResponseDTO> page =
                wholesaleOrderRepository
                        .findByNomeVendedorContainingIgnoreCase(
                                nomeNormalizado,
                                pageable
                        )
                        .map(mapper::ToResponse);

        return PageResponse.from(page);
    }

    public WholesaleResponseDTO findById(UUID id) {
        WholesaleOrder order =
                wholesaleOrderRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        "Pedido não encontrado"
                                )
                        );

        return mapper.ToResponse(order);
    }

    public void deleteOrder(UUID id) {
        WholesaleOrder order =
                wholesaleOrderRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        "Pedido não encontrado " + id
                                )
                        );

        s3Service.deleteFile(order.getFotoUrl());
        wholesaleOrderRepository.delete(order);
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "O termo da pesquisa é obrigatório"
            );
        }

        return value.trim();
    }

    private String normalizePhone(String value) {
        String normalized =
                normalizeText(value).replaceAll("\\D", "");

        if (normalized.length() != 11) {
            throw new IllegalArgumentException(
                    "O número precisa ter 11 dígitos"
            );
        }

        return normalized;
    }
}


