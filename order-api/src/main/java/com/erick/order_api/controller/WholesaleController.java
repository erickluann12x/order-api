package com.erick.order_api.controller;

import com.erick.order_api.dto.WholesaleRequestDTO;
import com.erick.order_api.dto.WholesaleResponseDTO;
import com.erick.order_api.entity.User;
import com.erick.order_api.service.WholesaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.security.autoconfigure.SecurityProperties;
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

    @Operation(summary = "Criar novo pedido com foto")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WholesaleResponseDTO> createWholesale(@Valid @ModelAttribute WholesaleRequestDTO dto, @AuthenticationPrincipal User userLogg) {
        WholesaleResponseDTO responseDTO = wholesaleService.createOrder(dto, userLogg);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @Operation(summary = "Listar todos os pedidos ordenados por data")
    @GetMapping
    public ResponseEntity<List<WholesaleResponseDTO>> listAll() {
        return ResponseEntity.ok(wholesaleService.listOrders());
    }

    @Operation(summary = "Buscar pedidos pelo nome do cliente")
    @GetMapping("/client")
    public ResponseEntity<List<WholesaleResponseDTO>> findByClient(@RequestParam String nomeCliente) {
        return ResponseEntity.ok(wholesaleService.findByNameClient(nomeCliente));
    }

    @Operation(summary = "Buscar pedidos pelo número do cliente")
    @GetMapping("/number")
    public ResponseEntity<List<WholesaleResponseDTO>> findByNumber(@RequestParam String numeroCliente) {
        return ResponseEntity.ok(wholesaleService.findByNumberClient(numeroCliente));
    }

    @Operation(summary = "Buscar pedidos pelo nome do vendedor")
    @GetMapping("/seller")
    public ResponseEntity<List<WholesaleResponseDTO>> findBySeller(@RequestParam String nomeVendedor) {
        return ResponseEntity.ok(wholesaleService.findByNameSeller(nomeVendedor));
    }

    @Operation(summary = "Buscar pedido por ID")
    @GetMapping("/{id}")
    public ResponseEntity<WholesaleResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(wholesaleService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        wholesaleService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

}
