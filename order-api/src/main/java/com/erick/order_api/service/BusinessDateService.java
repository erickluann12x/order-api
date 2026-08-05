package com.erick.order_api.service;

import com.erick.order_api.config.MonthRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

@Service
public class BusinessDateService {

    private final ZoneId businessZone;


    public BusinessDateService(
            @Value(
                    "${app.business-zone:America/Fortaleza}"
            )
            String businessZone
    ) {
        this.businessZone =
                ZoneId.of(businessZone);
    }

    public MonthRange monthRange(
            int year,
            int month
    ) {
        YearMonth yearMonth =
                YearMonth.of(year, month);

        Instant start = yearMonth
                .atDay(1)
                .atStartOfDay(businessZone)
                .toInstant();

        Instant end = yearMonth
                .plusMonths(1)
                .atDay(1)
                .atStartOfDay(businessZone)
                .toInstant();

        return new MonthRange(
                start,
                end
        );
    }

    public ZoneId getBusinessZone() {
        return businessZone;
    }
}
