package com.erick.order_api.controller;

import com.erick.order_api.dto.MonthlySalesReportResponse;
import com.erick.order_api.service.SellerReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports/sellers")
@RequiredArgsConstructor
public class SellerReportController {

    private final SellerReportService
            sellerReportService;

    @GetMapping("/monthly")
    public MonthlySalesReportResponse
    getMonthlyReport(
            @RequestParam
            int year,

            @RequestParam
            int month,

            @RequestParam(required = false)
            String nomeVendedor
    ) {
        return sellerReportService
                .getMonthlyReport(
                        year,
                        month,
                        nomeVendedor
                );
    }
}
