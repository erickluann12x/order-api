package com.erick.order_api.controller;


import com.erick.order_api.entity.Roles;
import com.erick.order_api.entity.User;
import com.erick.order_api.repository.UserRepository;
import com.erick.order_api.repository.WholesaleOrderRepository;
import com.erick.order_api.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WholesaleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WholesaleOrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private S3Service s3Service;

    private String token;
    private UUID createdOrderId;

    @BeforeEach
    void setUp() throws Exception {
        orderRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.builder()
                .username("vendedor_test")
                .password(passwordEncoder.encode("senha123"))
                .roles(Roles.SELLER)
                .build();
        userRepository.save(user);


        String loginBody = """
                {"username": "vendedor_test", "password": "senha123"}
                """;

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        String resposta = result.getResponse().getContentAsString();
        token = resposta.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        when(s3Service.uploadFile(any()))
                .thenReturn("https://bucket.s3.amazonaws.com/pedidos/foto1.jpg");

        MockMultipartFile foto = new MockMultipartFile(
                "foto", "foto.jpg", "image/jpeg", "conteudo".getBytes()
        );

        MvcResult orderResult = mockMvc.perform(multipart("/orders")
                        .file(foto)
                        .param("nomeCliente", "Maria Silva")
                        .param("nomeVendedor", "João")
                        .param("marca", "Arezzo")
                        .param("valorTotal", "1500.00")
                        .param("numeroCliente", "85999990001")
                        .header("Authorization", "Bearer " + token))
                        .andExpect(status().isCreated())
                .andReturn();

        String orderResposta = orderResult.getResponse().getContentAsString();
        String idStr = orderResposta.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
        createdOrderId = UUID.fromString(idStr);
    }
    @Test
    @DisplayName("deve criar pedido com sucesso retornando 201")
    void deveCriarPedidoComSucesso() throws Exception {
        when(s3Service.uploadFile(any()))
                .thenReturn("https://bucket.s3.amazonaws.com/pedidos/foto2.jpg");

        MockMultipartFile foto = new MockMultipartFile(
                "foto", "foto2.jpg", "image/jpeg", "conteudo".getBytes()
        );

        mockMvc.perform(multipart("/orders")
                        .file(foto)
                        .param("nomeCliente", "Ana Maria")
                        .param("nomeVendedor", "Carlos")
                        .param("marca", "Schutz")
                        .param("valorTotal", "2500.00")
                        .param("numeroCliente", "85999990002")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeCliente").value("Ana Maria"))
                .andExpect(jsonPath("$.nomeVendedor").value("Carlos"))
                .andExpect(jsonPath("$.numeroCliente").value("85999990002"))
                .andExpect(jsonPath("$.fotoUrl").value("https://bucket.s3.amazonaws.com/pedidos/foto2.jpg"));
    }

    @Test
    @DisplayName("deve retornar 401 ao acessar sem token")
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deve listar todos os pedidos")
    void deveListarTodosOsPedidos() throws Exception {
        mockMvc.perform(get("/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nomeCliente").value("Maria Silva"));
    }

    @Test
    @DisplayName("deve buscar pedido por id com sucesso")
    void deveBuscarPedidoPorId() throws Exception {
        mockMvc.perform(get("/orders/" + createdOrderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdOrderId.toString()))
                .andExpect(jsonPath("$.nomeCliente").value("Maria Silva"));
    }

    @Test
    @DisplayName("deve retornar 404 quando pedido não encontrado por id")
    void deveRetornar404QuandoNaoEncontrado() throws Exception {
        mockMvc.perform(get("/orders/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("deve buscar pedidos pelo nome do cliente")
    void deveBuscarPorNomeCliente() throws Exception {
        mockMvc.perform(get("/orders/client")
                        .param("nomeCliente", "Maria")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nomeCliente").value("Maria Silva"));
    }

    @Test
    @DisplayName("deve retornar lista vazia quando cliente não encontrado")
    void deveRetornarListaVaziaClienteNaoEncontrado() throws Exception {
        mockMvc.perform(get("/orders/client")
                        .param("nomeCliente", "Inexistente")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("deve buscar pedidos pelo número do cliente")
    void deveBuscarPorNumeroCliente() throws Exception {
        mockMvc.perform(get("/orders/number")
                        .param("numeroCliente", "85999990001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroCliente").value("85999990001"));
    }

    @Test
    @DisplayName("deve buscar pedidos pelo nome do vendedor")
    void deveBuscarPorNomeVendedor() throws Exception {
        mockMvc.perform(get("/orders/seller")
                        .param("nomeVendedor", "João")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nomeVendedor").value("João"));
    }

    @Test
    @DisplayName("deve retornar 400 ao criar pedido sem campos obrigatórios")
    void deveRetornar400SemCamposObrigatorios() throws Exception {
        MockMultipartFile foto = new MockMultipartFile(
                "foto", "foto.jpg", "image/jpeg", "conteudo".getBytes()
        );

        // envia sem nomeCliente e valorTotal
        mockMvc.perform(multipart("/orders")
                        .file(foto)
                        .param("nomeVendedor", "João")
                        .param("marca", "Arezzo")
                        .param("numeroCliente", "85999990001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
}
