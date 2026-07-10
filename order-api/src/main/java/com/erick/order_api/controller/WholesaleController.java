package com.erick.order_api.controller;

import com.erick.order_api.dto.WholesaleRequestDTO;
import com.erick.order_api.dto.WholesaleResponseDTO;
import com.erick.order_api.entity.User;
import com.erick.order_api.service.WholesaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.security.autoconfigure.SecurityProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class WholesaleController {

    private final WholesaleService wholesaleService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WholesaleResponseDTO> createWholesale(@Valid @ModelAttribute WholesaleRequestDTO dto, @AuthenticationPrincipal User userLogg){
        WholesaleResponseDTO responseDTO = wholesaleService.createOrder(dto, userLogg);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<WholesaleResponseDTO>> listAll(){
        return ResponseEntity.ok(wholesaleService.listOrders());
    }

    @GetMapping("/client")
    public ResponseEntity<List<WholesaleResponseDTO>> findByClient(@RequestParam String nomeCliente){
        return ResponseEntity.ok(wholesaleService.findByNameClient(nomeCliente));
    }

    @GetMapping("/number")
    public ResponseEntity<List<WholesaleResponseDTO>> findByNumber(@RequestParam String numeroCliente){
        return ResponseEntity.ok(wholesaleService.findByNumberClient(numeroCliente));
    }
}
