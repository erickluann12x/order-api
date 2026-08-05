package com.erick.order_api.service;

import com.erick.order_api.config.MonthRange;
import com.erick.order_api.dto.MonthlySalesReportResponse;
import com.erick.order_api.dto.SellerMonthlySummaryResponse;
import com.erick.order_api.repository.WholesaleOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerReportService {

    private final WholesaleOrderRepository
            repository;

    private final BusinessDateService
            businessDateService;

    public MonthlySalesReportResponse
    getMonthlyReport(
            int year,
            int month,
            String nomeVendedor
    ) {
        MonthRange range =
                businessDateService.monthRange(
                        year,
                        month
                );

        String normalizedSeller =
                normalizeOptionalSeller(
                        nomeVendedor
                );

        List<SellerMonthlySummaryResponse>
                sellers = repository
                .findMonthlySalesBySeller(
                        range.start(),
                        range.end(),
                        normalizedSeller
                )
                .stream()
                .map(
                        SellerMonthlySummaryResponse
                                ::from
                )
                .toList();

        long totalOrders = sellers
                .stream()
                .mapToLong(
                        SellerMonthlySummaryResponse
                                ::getQuantidadePedidos
                )
                .sum();

        BigDecimal totalValue = sellers
                .stream()
                .map(
                        SellerMonthlySummaryResponse
                                ::getValorTotal
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        return MonthlySalesReportResponse
                .builder()
                .year(year)
                .month(month)
                .quantidadePedidos(
                        totalOrders
                )
                .valorTotal(
                        totalValue
                )
                .vendedores(sellers)
                .build();
    }

    private String normalizeOptionalSeller(
            String nomeVendedor
    ) {
        if (
                nomeVendedor == null ||
                        nomeVendedor.isBlank()
        ) {
            return null;
        }

        return nomeVendedor.trim();
    }
}
