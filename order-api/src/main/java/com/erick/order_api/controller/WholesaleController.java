package com.erick.order_api.controller;

import com.erick.order_api.dto.OrderSummaryResponse;
import com.erick.order_api.dto.PageResponse;
import com.erick.order_api.dto.WholesaleRequestDTO;
import com.erick.order_api.dto.WholesaleResponseDTO;
import com.erick.order_api.entity.User;
import com.erick.order_api.service.OrderSummaryService;
import com.erick.order_api.service.WholesaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.security.autoconfigure.SecurityProperties;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
@Tag(name = "Pedidos de Atacado")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class WholesaleController {

    private final WholesaleService wholesaleService;
    private final OrderSummaryService orderSummaryService;

    @Operation(summary = "Criar novo pedido com foto")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WholesaleResponseDTO> createWholesale(@Valid @ModelAttribute WholesaleRequestDTO dto, @AuthenticationPrincipal User userLogg) {
        WholesaleResponseDTO responseDTO = wholesaleService.createOrder(dto, userLogg);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
    @Operation(
            summary = "Listar pedidos paginados",
            description = "Lista os pedidos ordenados por data de criação, do mais recente para o mais antigo"
    )
    @GetMapping
    public ResponseEntity<PageResponse<WholesaleResponseDTO>> listAll(
            @PageableDefault(
                    size = 12,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                wholesaleService.listOrders(pageable)
        );
    }

    @Operation(
            summary = "Buscar pedidos pelo nome do cliente",
            description = "Busca parcial, ignorando letras maiúsculas e minúsculas"
    )
    @GetMapping("/client")
    public ResponseEntity<PageResponse<WholesaleResponseDTO>> findByClient(
            @RequestParam String nomeCliente,

            @PageableDefault(
                    size = 12,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                wholesaleService.findByNameClient(
                        nomeCliente,
                        pageable
                )
        );
    }

    @Operation(
            summary = "Buscar pedidos pelo número do cliente",
            description = "O número deve possuir 11 dígitos"
    )
    @GetMapping("/number")
    public ResponseEntity<PageResponse<WholesaleResponseDTO>> findByNumber(
            @RequestParam String numeroCliente,

            @PageableDefault(
                    size = 12,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                wholesaleService.findByNumberClient(
                        numeroCliente,
                        pageable
                )
        );
    }

    @Operation(
            summary = "Buscar pedidos pelo nome do vendedor",
            description = "Busca parcial, ignorando letras maiúsculas e minúsculas"
    )
    @GetMapping("/seller")
    public PageResponse<WholesaleResponseDTO>
    findBySeller(
            @RequestParam
            String nomeVendedor,

            @RequestParam(required = false)
            Integer year,

            @RequestParam(required = false)
            Integer month,

            @PageableDefault(
                    size = 12,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return wholesaleService
                .findBySeller(
                        nomeVendedor,
                        year,
                        month,
                        pageable
                );
    }

    @Operation(summary = "Buscar pedido por ID")
    @GetMapping("/{id}")
    public ResponseEntity<WholesaleResponseDTO> findById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                wholesaleService.findById(id)
        );
    }

    @Operation(summary = "Excluir pedido")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable UUID id
    ) {
        wholesaleService.deleteOrder(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/summary")
    public OrderSummaryResponse getSummary() {
        return orderSummaryService
                .getSummary();
    }
}
