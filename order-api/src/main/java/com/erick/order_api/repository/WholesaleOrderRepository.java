package com.erick.order_api.repository;

import com.erick.order_api.entity.WholesaleOrder;
import com.erick.order_api.projection.SellerMonthlyProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WholesaleOrderRepository extends JpaRepository<WholesaleOrder, UUID> {

    Page<WholesaleOrder> findByNomeClienteContainingIgnoreCase(
            String nomeCliente,
            Pageable pageable
    );

    Page<WholesaleOrder> findByNumeroCliente(
            String numeroCliente,
            Pageable pageable
    );

    Page<WholesaleOrder> findByNomeVendedorContainingIgnoreCase(
            String nomeVendedor,
            Pageable pageable
    );

    Page<WholesaleOrder>
    findByNomeVendedorContainingIgnoreCaseAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            String nomeVendedor,
            Instant start,
            Instant end,
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

    @Query("""
        SELECT
            order.nomeVendedor AS nomeVendedor,
            COUNT(order.id) AS quantidadePedidos,
            COALESCE(
                SUM(order.valorTotal),
                0
            ) AS valorTotal
        FROM WholesaleOrder order
        WHERE order.createdAt >= :start
          AND order.createdAt < :end
          AND (
              :nomeVendedor IS NULL
              OR LOWER(order.nomeVendedor)
                 LIKE LOWER(
                     CONCAT(
                         '%',
                         :nomeVendedor,
                         '%'
                     )
                 )
          )
        GROUP BY order.nomeVendedor
        ORDER BY SUM(order.valorTotal) DESC
        """)
    List<SellerMonthlyProjection>
    findMonthlySalesBySeller(
            @Param("start")
            Instant start,

            @Param("end")
            Instant end,

            @Param("nomeVendedor")
            String nomeVendedor
    );
}
