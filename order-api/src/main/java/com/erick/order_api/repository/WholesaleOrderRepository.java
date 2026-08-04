package com.erick.order_api.repository;

import com.erick.order_api.entity.WholesaleOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface WholesaleOrderRepository extends JpaRepository<WholesaleOrder, UUID> {

    Page<WholesaleOrder> findByNomeClienteContainingIgnoreCase(
            String nomeCliente,
            Pageable pageable
    );

    Page<WholesaleOrder> findByNumeroClienteContainingIgnoreCase(
            String numeroCliente,
            Pageable pageable
    );

    Page<WholesaleOrder> findByNomeVendedorContainingIgnoreCase(
            String nomeVendedor,
            Pageable pageable
    );

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            Instant start,
            Instant end
    );

    @Query("""
        SELECT COALESCE(
            SUM(order.valorTotal),
            0
        )
        FROM WholesaleOrder order
        """)
    BigDecimal sumAllValues();

    @Query("""
        SELECT COUNT(
            DISTINCT LOWER(
                TRIM(order.nomeVendedor)
            )
        )
        FROM WholesaleOrder order
        """)
    long countDistinctSellers();
}
