package com.erick.order_api.service;

import com.erick.order_api.dto.WholesaleRequestDTO;
import com.erick.order_api.dto.WholesaleResponseDTO;
import com.erick.order_api.entity.Roles;
import com.erick.order_api.entity.User;
import com.erick.order_api.entity.WholesaleOrder;
import com.erick.order_api.exception.OrderNotFoundException;
import com.erick.order_api.mapper.WholesaleMapper;
import com.erick.order_api.repository.WholesaleOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WholesaleServiceTest {

    @Mock
    private WholesaleOrderRepository repository;

    @Mock
    private S3Service s3Service;


    private WholesaleMapper mapper;
    private WholesaleService wholesaleService;

    private User userLog;
    private WholesaleOrder order1;
    private WholesaleOrder order2;
    private UUID orderId;

    @BeforeEach
    void setUp() {

        mapper = new WholesaleMapper();


        wholesaleService = new WholesaleService(repository, s3Service, mapper);

        orderId = UUID.randomUUID();

        userLog = User.builder()
                .id(UUID.randomUUID())
                .username("vendedor1")
                .password("senha123")
                .roles(Roles.SELLER)
                .build();

        order1 = new WholesaleOrder();
        order1.setId(orderId);
        order1.setNomeCliente("Maria Silva");
        order1.setNomeVendedor("João");
        order1.setMarca("Arezzo");
        order1.setValorTotal(new BigDecimal("1500.00"));
        order1.setNumeroCliente("85999990001");
        order1.setFotoUrl("https://bucket.s3.amazonaws.com/pedidos/foto1.jpg");
        order1.setCreatedAt(LocalDateTime.now());
        order1.setUser(userLog);

        order2 = new WholesaleOrder();
        order2.setId(UUID.randomUUID());
        order2.setNomeCliente("Ana Maria");
        order2.setNomeVendedor("Carlos");
        order2.setMarca("Schutz");
        order2.setValorTotal(new BigDecimal("2500.00"));
        order2.setNumeroCliente("85999990002");
        order2.setFotoUrl("https://bucket.s3.amazonaws.com/pedidos/foto2.jpg");
        order2.setCreatedAt(LocalDateTime.now().minusDays(1));
        order2.setUser(userLog);
    }

    @Test
    @DisplayName("deve criar pedido com sucesso e retornar URL da foto")
    void createOrder() {
        MockMultipartFile foto = new MockMultipartFile(
                "foto", "foto.jpg", "image/jpeg", "conteudo".getBytes()
        );

        WholesaleRequestDTO dto = new WholesaleRequestDTO(
                "Maria Silva", "João", "Arezzo",
                new BigDecimal("1500.00"), "85999990001", foto
        );

        when(s3Service.uploadFile(foto)).thenReturn(order1.getFotoUrl());
        when(repository.save(any(WholesaleOrder.class))).thenReturn(order1);

        WholesaleResponseDTO response = wholesaleService.createOrder(dto, userLog);

        assertThat(response).isNotNull();
        assertThat(response.nomeCliente()).isEqualTo("Maria Silva");
        assertThat(response.numeroCliente()).isEqualTo("85999990001");
        assertThat(response.fotoUrl()).contains("amazonaws.com");

        verify(s3Service).uploadFile(foto);
        verify(repository).save(any(WholesaleOrder.class));
    }

    @Test
    @DisplayName("deve listar todos os pedidos ordenados por data de criação")
    void listOrders() {
        when(repository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(order1, order2));

        List<WholesaleResponseDTO> list = wholesaleService.listOrders();

        assertThat(list).hasSize(2);
        assertThat(list.get(0).nomeCliente()).isEqualTo("Maria Silva");
        assertThat(list.get(1).nomeCliente()).isEqualTo("Ana Maria");

        verify(repository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("deve buscar pedidos pelo nome do cliente ignorando maiúsculas")
    void findByNameClient() {
        when(repository.findByNomeClienteContainingIgnoreCase("maria"))
                .thenReturn(List.of(order1, order2));

        List<WholesaleResponseDTO> list = wholesaleService.findByNameClient("maria");

        assertThat(list).hasSize(2);
        assertThat(list).allMatch(p -> p.nomeCliente().toLowerCase().contains("maria"));

        verify(repository).findByNomeClienteContainingIgnoreCase("maria");
    }

    @Test
    @DisplayName("deve retornar lista vazia quando nome do cliente não existe")
    void findByNameClient_WhenDoesNotExist() {
        when(repository.findByNomeClienteContainingIgnoreCase("inexistente"))
                .thenReturn(Collections.emptyList());

        List<WholesaleResponseDTO> list = wholesaleService.findByNameClient("inexistente");

        assertThat(list).isEmpty();
        verify(repository).findByNomeClienteContainingIgnoreCase("inexistente");
    }

    @Test
    @DisplayName("deve buscar pedidos pelo número do cliente")
    void findByNumberClient() {
        when(repository.findByNumeroCliente("85999990001"))
                .thenReturn(List.of(order1));

        List<WholesaleResponseDTO> list = wholesaleService.findByNumberClient("85999990001");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).numeroCliente()).isEqualTo("85999990001");

        verify(repository).findByNumeroCliente("85999990001");
    }

    @Test
    @DisplayName("deve retornar lista vazia quando número do cliente não existe")
    void findByNumberClient_WhenDoesNotExist() {
        when(repository.findByNumeroCliente("00000000000"))
                .thenReturn(Collections.emptyList());

        List<WholesaleResponseDTO> list = wholesaleService.findByNumberClient("00000000000");

        assertThat(list).isEmpty();
        verify(repository).findByNumeroCliente("00000000000");
    }

    @Test
    @DisplayName("deve buscar pedidos pelo nome do vendedor ignorando maiúsculas")
    void findByNameSeller() {
        when(repository.findByNomeVendedorContainingIgnoreCase("joão"))
                .thenReturn(List.of(order1));

        List<WholesaleResponseDTO> list = wholesaleService.findByNameSeller("joão");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).nomeVendedor()).isEqualTo("João");

        verify(repository).findByNomeVendedorContainingIgnoreCase("joão");
    }

    @Test
    @DisplayName("deve buscar pedido por id com sucesso")
    void findById() {
        when(repository.findById(orderId)).thenReturn(Optional.of(order1));

        WholesaleResponseDTO response = wholesaleService.findById(orderId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(orderId);
        assertThat(response.nomeCliente()).isEqualTo("Maria Silva");
        assertThat(response.numeroCliente()).isEqualTo("85999990001");

        verify(repository).findById(orderId);
    }

    @Test
    @DisplayName("deve lançar exceção quando pedido não encontrado por id")
    void findById_NotFound() {
        UUID idInexistente = UUID.randomUUID();

        when(repository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wholesaleService.findById(idInexistente))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Pedido não encontrado");

        verify(repository).findById(idInexistente);
    }
}