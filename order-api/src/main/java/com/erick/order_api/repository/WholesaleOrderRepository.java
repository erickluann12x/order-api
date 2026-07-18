package com.erick.order_api.repository;

import com.erick.order_api.entity.WholesaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WholesaleOrderRepository extends JpaRepository<WholesaleOrder, UUID> {
    List<WholesaleOrder> findAllByOrderByCreatedAtDesc();

    List<WholesaleOrder> findByNomeClienteContainingIgnoreCase(String nomeCliente);

    List<WholesaleOrder> findByNumeroCliente(String numeroCliente);

    List<WholesaleOrder> findByNomeVendedorContainingIgnoreCase(String nomeVendedor);

    Optional<WholesaleOrder> findById(UUID id);
}
