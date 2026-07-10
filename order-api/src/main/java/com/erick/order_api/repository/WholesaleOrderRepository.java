package com.erick.order_api.repository;

import com.erick.order_api.entity.WholesaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WholesaleOrderRepository extends JpaRepository<WholesaleOrder, UUID> {
    List<WholesaleOrder> findAllByOrderByDataCriacaoDesc();

    List<WholesaleOrder> findByNomeCliente(String nomeCliente);

    List<WholesaleOrder> findByNumeroCliente(String numeroCliente);
}
