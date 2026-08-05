package com.erick.order_api.service;

import com.erick.order_api.dto.PageResponse;
import com.erick.order_api.dto.WholesaleRequestDTO;
import com.erick.order_api.dto.WholesaleResponseDTO;
import com.erick.order_api.entity.Roles;
import com.erick.order_api.entity.User;
import com.erick.order_api.entity.WholesaleOrder;
import com.erick.order_api.exception.OrderNotFoundException;
import com.erick.order_api.mapper.WholesaleMapper;
import com.erick.order_api.repository.UserRepository;
import com.erick.order_api.repository.WholesaleOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.Instant;

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

    private Pageable pageable;

    private UserRepository userRepository;

    private BusinessDateService businessDateService;

    @BeforeEach
    void setUp() {

        mapper = new WholesaleMapper();

        wholesaleService = new WholesaleService (
                repository,
                s3Service,
                mapper,
                businessDateService
        );

        pageable = PageRequest.of(
                0,
                12,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

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
        order1.setValorTotal(
                new BigDecimal("1500.00")
        );
        order1.setNumeroCliente("85999990001");
        order1.setFotoUrl(
                "https://bucket.s3.amazonaws.com/pedidos/foto1.jpg"
        );
        order1.setCreatedAt(Instant.now().now());
        order1.setUser(userLog);

        order2 = new WholesaleOrder();
        order2.setId(UUID.randomUUID());
        order2.setNomeCliente("Ana Maria");
        order2.setNomeVendedor("Carlos");
        order2.setMarca("Schutz");
        order2.setValorTotal(
                new BigDecimal("2500.00")
        );
        order2.setNumeroCliente("85999990002");
        order2.setFotoUrl(
                "https://bucket.s3.amazonaws.com/pedidos/foto2.jpg"
        );
        order2.setCreatedAt(
                Instant.now().minusMillis(1)
        );
        order2.setUser(userLog);
    }

    @Test
    @DisplayName(
            "deve criar pedido com sucesso e retornar URL da foto"
    )
    void createOrder() {

        MockMultipartFile foto =
                new MockMultipartFile(
                        "foto",
                        "foto.jpg",
                        "image/jpeg",
                        "conteudo".getBytes()
                );

        WholesaleRequestDTO dto =
                new WholesaleRequestDTO(
                        "Maria Silva",
                        "João",
                        "Arezzo",
                        new BigDecimal("1500.00"),
                        "85999990001",
                        foto
                );

        when(
                s3Service.uploadFile(foto)
        ).thenReturn(
                order1.getFotoUrl()
        );

        when(
                repository.save(
                        any(WholesaleOrder.class)
                )
        ).thenReturn(order1);

        WholesaleResponseDTO response =
                wholesaleService.createOrder(
                        dto,
                        userLog
                );

        assertThat(response).isNotNull();

        assertThat(
                response.nomeCliente()
        ).isEqualTo("Maria Silva");

        assertThat(
                response.numeroCliente()
        ).isEqualTo("85999990001");

        assertThat(
                response.fotoUrl()
        ).contains("amazonaws.com");

        verify(
                s3Service
        ).uploadFile(foto);

        verify(
                repository
        ).save(
                any(WholesaleOrder.class)
        );
    }

    @Test
    @DisplayName(
            "deve listar pedidos paginados ordenados por data"
    )
    void listOrders() {

        Page<WholesaleOrder> ordersPage =
                new PageImpl<>(
                        List.of(order1, order2),
                        pageable,
                        2
                );

        when(
                repository.findAll(pageable)
        ).thenReturn(ordersPage);

        PageResponse<WholesaleResponseDTO> response =
                wholesaleService.listOrders(pageable);

        assertThat(response).isNotNull();

        assertThat(
                response.getContent()
        ).hasSize(2);

        assertThat(
                response.getContent()
                        .get(0)
                        .nomeCliente()
        ).isEqualTo("Maria Silva");

        assertThat(
                response.getContent()
                        .get(1)
                        .nomeCliente()
        ).isEqualTo("Ana Maria");

        assertThat(
                response.getPage()
        ).isZero();

        assertThat(
                response.getSize()
        ).isEqualTo(12);

        assertThat(
                response.getTotalElements()
        ).isEqualTo(2);

        assertThat(
                response.getTotalPages()
        ).isEqualTo(1);

        assertThat(
                response.isFirst()
        ).isTrue();

        assertThat(
                response.isLast()
        ).isTrue();

        verify(
                repository
        ).findAll(pageable);
    }

    @Test
    @DisplayName(
            "deve buscar pedidos pelo nome do cliente de forma paginada"
    )
    void findByNameClient() {

        Page<WholesaleOrder> ordersPage =
                new PageImpl<>(
                        List.of(order1, order2),
                        pageable,
                        2
                );

        when(
                repository
                        .findByNomeClienteContainingIgnoreCase(
                                "maria",
                                pageable
                        )
        ).thenReturn(ordersPage);

        PageResponse<WholesaleResponseDTO> response =
                wholesaleService.findByNameClient(
                        "maria",
                        pageable
                );

        assertThat(response).isNotNull();

        assertThat(
                response.getContent()
        ).hasSize(2);

        assertThat(
                response.getContent()
        ).allMatch(
                order ->
                        order.nomeCliente()
                                .toLowerCase()
                                .contains("maria")
        );

        assertThat(
                response.getTotalElements()
        ).isEqualTo(2);

        verify(
                repository
        ).findByNomeClienteContainingIgnoreCase(
                "maria",
                pageable
        );
    }

    @Test
    @DisplayName(
            "deve remover espaços do nome antes de pesquisar cliente"
    )
    void findByNameClient_ShouldTrimValue() {

        Page<WholesaleOrder> ordersPage =
                new PageImpl<>(
                        List.of(order1),
                        pageable,
                        1
                );

        when(
                repository
                        .findByNomeClienteContainingIgnoreCase(
                                "Maria",
                                pageable
                        )
        ).thenReturn(ordersPage);

        PageResponse<WholesaleResponseDTO> response =
                wholesaleService.findByNameClient(
                        "  Maria  ",
                        pageable
                );

        assertThat(
                response.getContent()
        ).hasSize(1);

        verify(
                repository
        ).findByNomeClienteContainingIgnoreCase(
                "Maria",
                pageable
        );
    }

    @Test
    @DisplayName(
            "deve retornar página vazia quando cliente não existe"
    )
    void findByNameClient_WhenDoesNotExist() {

        Page<WholesaleOrder> emptyPage =
                Page.empty(pageable);

        when(
                repository
                        .findByNomeClienteContainingIgnoreCase(
                                "inexistente",
                                pageable
                        )
        ).thenReturn(emptyPage);

        PageResponse<WholesaleResponseDTO> response =
                wholesaleService.findByNameClient(
                        "inexistente",
                        pageable
                );

        assertThat(
                response.getContent()
        ).isEmpty();

        assertThat(
                response.getTotalElements()
        ).isZero();

        assertThat(
                response.getTotalPages()
        ).isZero();

        verify(
                repository
        ).findByNomeClienteContainingIgnoreCase(
                "inexistente",
                pageable
        );
    }

    @Test
    @DisplayName(
            "deve buscar pedidos pelo número do cliente"
    )
    void findByNumberClient() {

        Page<WholesaleOrder> ordersPage =
                new PageImpl<>(
                        List.of(order1),
                        pageable,
                        1
                );

        when(
                repository.findByNumeroCliente(
                        "85999990001",
                        pageable
                )
        ).thenReturn(ordersPage);

        PageResponse<WholesaleResponseDTO> response =
                wholesaleService.findByNumberClient(
                        "85999990001",
                        pageable
                );

        assertThat(
                response.getContent()
        ).hasSize(1);

        assertThat(
                response.getContent()
                        .get(0)
                        .numeroCliente()
        ).isEqualTo("85999990001");

        assertThat(
                response.getTotalElements()
        ).isEqualTo(1);

        verify(
                repository
        ).findByNumeroCliente(
                "85999990001",
                pageable
        );
    }

    @Test
    @DisplayName(
            "deve remover formatação do número antes de pesquisar"
    )
    void findByNumberClient_ShouldRemoveFormatting() {

        Page<WholesaleOrder> ordersPage =
                new PageImpl<>(
                        List.of(order1),
                        pageable,
                        1
                );

        when(
                repository.findByNumeroCliente(
                        "85999990001",
                        pageable
                )
        ).thenReturn(ordersPage);

        PageResponse<WholesaleResponseDTO> response =
                wholesaleService.findByNumberClient(
                        "(85) 99999-0001",
                        pageable
                );

        assertThat(
                response.getContent()
        ).hasSize(1);

        verify(
                repository
        ).findByNumeroCliente(
                "85999990001",
                pageable
        );
    }

    @Test
    @DisplayName(
            "deve retornar página vazia quando número não existe"
    )
    void findByNumberClient_WhenDoesNotExist() {

        Page<WholesaleOrder> emptyPage =
                Page.empty(pageable);

        when(
                repository.findByNumeroCliente(
                        "00000000000",
                        pageable
                )
        ).thenReturn(emptyPage);

        PageResponse<WholesaleResponseDTO> response =
                wholesaleService.findByNumberClient(
                        "00000000000",
                        pageable
                );

        assertThat(
                response.getContent()
        ).isEmpty();

        assertThat(
                response.getTotalElements()
        ).isZero();

        verify(
                repository
        ).findByNumeroCliente(
                "00000000000",
                pageable
        );
    }

    @Test
    @DisplayName(
            "deve lançar exceção quando número não possui 11 dígitos"
    )
    void findByNumberClient_WhenNumberIsInvalid() {

        assertThatThrownBy(
                () ->
                        wholesaleService
                                .findByNumberClient(
                                        "8599999",
                                        pageable
                                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "11 dígitos"
                );
    }

    @Test
    @DisplayName(
            "deve buscar pedidos pelo nome do vendedor"
    )
    void findByNameSeller() {

        Page<WholesaleOrder> ordersPage =
                new PageImpl<>(
                        List.of(order1),
                        pageable,
                        1
                );

        when(
                repository
                        .findByNomeVendedorContainingIgnoreCase(
                                "joão",
                                pageable
                        )
        ).thenReturn(ordersPage);

        PageResponse<WholesaleResponseDTO> response =
                wholesaleService.findBySeller(
                        "joão",
                        isNull(),
                        isNull(),
                        pageable
                );

        assertThat(
                response.getContent()
        ).hasSize(1);

        assertThat(
                response.getContent()
                        .get(0)
                        .nomeVendedor()
        ).isEqualTo("João");

        verify(
                repository
        ).findByNomeVendedorContainingIgnoreCase(
                "joão",
                pageable
        );
    }

    @Test
    @DisplayName(
            "deve lançar exceção quando termo da pesquisa estiver vazio"
    )
    void findByNameClient_WhenNameIsBlank() {

        assertThatThrownBy(
                () ->
                        wholesaleService
                                .findByNameClient(
                                        "   ",
                                        pageable
                                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "termo da pesquisa"
                );
    }

    @Test
    @DisplayName(
            "deve buscar pedido por id com sucesso"
    )
    void findById() {

        when(
                repository.findById(orderId)
        ).thenReturn(
                Optional.of(order1)
        );

        WholesaleResponseDTO response =
                wholesaleService.findById(orderId);

        assertThat(response).isNotNull();

        assertThat(
                response.id()
        ).isEqualTo(orderId);

        assertThat(
                response.nomeCliente()
        ).isEqualTo("Maria Silva");

        assertThat(
                response.numeroCliente()
        ).isEqualTo("85999990001");

        verify(
                repository
        ).findById(orderId);
    }

    @Test
    @DisplayName(
            "deve lançar exceção quando pedido não for encontrado por id"
    )
    void findById_NotFound() {

        UUID idInexistente =
                UUID.randomUUID();

        when(
                repository.findById(
                        idInexistente
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () ->
                        wholesaleService
                                .findById(
                                        idInexistente
                                )
        )
                .isInstanceOf(
                        OrderNotFoundException.class
                )
                .hasMessageContaining(
                        "Pedido não encontrado"
                );

        verify(
                repository
        ).findById(idInexistente);
    }

    @Test
    @DisplayName(
            "deve deletar pedido e remover foto do S3"
    )
    void deleteOrder() {

        when(
                repository.findById(orderId)
        ).thenReturn(
                Optional.of(order1)
        );

        wholesaleService.deleteOrder(orderId);

        verify(
                s3Service
        ).deleteFile(
                order1.getFotoUrl()
        );

        verify(
                repository
        ).delete(order1);
    }

    @Test
    @DisplayName(
            "deve lançar exceção ao tentar deletar pedido inexistente"
    )
    void deleteOrder_WhenOrderDoesNotExist() {

        when(
                repository.findById(orderId)
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () ->
                        wholesaleService
                                .deleteOrder(orderId)
        )
                .isInstanceOf(
                        OrderNotFoundException.class
                )
                .hasMessageContaining(
                        "Pedido não encontrado"
                );

        verify(
                repository
        ).findById(orderId);
    }
}