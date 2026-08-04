package com.erick.order_api.service;

import com.erick.order_api.dto.OrderSummaryResponse;
import com.erick.order_api.repository.WholesaleOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderSummaryService {
    private final WholesaleOrderRepository repository;

    @Value(
            "${app.business-zone:America/Fortaleza}"
    )
    private String businessZone;

    public OrderSummaryResponse getSummary() {
        ZoneId zoneId =
                ZoneId.of(businessZone);

        LocalDate today =
                LocalDate.now(zoneId);

        Instant startOfDay =
                today
                        .atStartOfDay(zoneId)
                        .toInstant();

        Instant startOfNextDay =
                today
                        .plusDays(1)
                        .atStartOfDay(zoneId)
                        .toInstant();

        return OrderSummaryResponse
                .builder()
                .totalPedidos(
                        repository.count()
                )
                .cadastradosHoje(
                        repository
                                .countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                        startOfDay,
                                        startOfNextDay
                                )
                )
                .valorTotal(
                        repository
                                .sumAllValues()
                )
                .totalVendedores(
                        repository
                                .countDistinctSellers()
                )
                .build();
    }
}
