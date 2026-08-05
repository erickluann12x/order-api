package com.erick.order_api.controller;


import com.erick.order_api.entity.Roles;
import com.erick.order_api.entity.User;
import com.erick.order_api.repository.RefreshTokenRepository;
import com.erick.order_api.repository.UserRepository;
import com.erick.order_api.repository.WholesaleOrderRepository;
import com.erick.order_api.service.S3Service;
import com.jayway.jsonpath.JsonPath;
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


import java.time.YearMonth;
import java.time.ZoneId;
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
class WholesaleControllerIntegrationTest {

    private static final String USERNAME = "vendedor_test";
    private static final String PASSWORD = "senha123";
    private static final String FIRST_IMAGE_URL =
            "https://bucket.s3.amazonaws.com/pedidos/foto1.jpg";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WholesaleOrderRepository orderRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private S3Service s3Service;

    private String accessToken;
    private UUID createdOrderId;

    @BeforeEach
    void setUp() throws Exception {
        orderRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.builder()
                .username(USERNAME)
                .password(passwordEncoder.encode(PASSWORD))
                .roles(Roles.SELLER)
                .build();
        userRepository.save(user);

        accessToken = authenticate();

        when(s3Service.uploadFile(any())).thenReturn(FIRST_IMAGE_URL);

        MvcResult result = createOrder(
                "foto.jpg",
                "Maria Silva",
                "João",
                "Arezzo",
                "1500.00",
                "85999990001"
        );

        String responseBody = result.getResponse().getContentAsString();
        createdOrderId = UUID.fromString(JsonPath.read(responseBody, "$.id"));
    }

    @Test
    @DisplayName("deve criar pedido com sucesso retornando 201")
    void deveCriarPedidoComSucesso() throws Exception {
        String imageUrl = "https://bucket.s3.amazonaws.com/pedidos/foto2.jpg";
        when(s3Service.uploadFile(any())).thenReturn(imageUrl);

        MockMultipartFile foto = image("foto2.jpg");

        mockMvc.perform(multipart("/orders")
                        .file(foto)
                        .param("nomeCliente", "Ana Maria")
                        .param("nomeVendedor", "Carlos")
                        .param("marca", "Schutz")
                        .param("valorTotal", "2500.00")
                        .param("numeroCliente", "85999990002")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeCliente").value("Ana Maria"))
                .andExpect(jsonPath("$.nomeVendedor").value("Carlos"))
                .andExpect(jsonPath("$.numeroCliente").value("85999990002"))
                .andExpect(jsonPath("$.fotoUrl").value(imageUrl));
    }

    @Test
    @DisplayName("deve retornar 401 ao acessar sem token")
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deve listar todos os pedidos de forma paginada")
    void deveListarTodosOsPedidos() throws Exception {
        mockMvc.perform(get("/orders")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nomeCliente").value("Maria Silva"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(12))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("deve buscar pedido por id com sucesso")
    void deveBuscarPedidoPorId() throws Exception {
        mockMvc.perform(get("/orders/{id}", createdOrderId)
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdOrderId.toString()))
                .andExpect(jsonPath("$.nomeCliente").value("Maria Silva"));
    }

    @Test
    @DisplayName("deve retornar 404 quando pedido não for encontrado por id")
    void deveRetornar404QuandoNaoEncontrado() throws Exception {
        mockMvc.perform(get("/orders/{id}", UUID.randomUUID())
                        .header("Authorization", bearerToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("deve buscar pedidos pelo nome do cliente")
    void deveBuscarPorNomeCliente() throws Exception {
        mockMvc.perform(get("/orders/client")
                        .param("nomeCliente", "Maria")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nomeCliente").value("Maria Silva"));
    }

    @Test
    @DisplayName("deve retornar página vazia quando cliente não for encontrado")
    void deveRetornarPaginaVaziaClienteNaoEncontrado() throws Exception {
        mockMvc.perform(get("/orders/client")
                        .param("nomeCliente", "Inexistente")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("deve buscar pedidos pelo número do cliente")
    void deveBuscarPorNumeroCliente() throws Exception {
        mockMvc.perform(get("/orders/number")
                        .param("numeroCliente", "85999990001")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].numeroCliente")
                        .value("85999990001"));
    }

    @Test
    @DisplayName("deve buscar pedidos pelo nome do vendedor")
    void deveBuscarPorNomeVendedor() throws Exception {
        mockMvc.perform(get("/orders/seller")
                        .param("nomeVendedor", "João")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nomeVendedor").value("João"));
    }

    @Test
    @DisplayName("deve buscar pedidos pelo vendedor, ano e mês")
    void deveBuscarPorNomeVendedorAnoEMes() throws Exception {
        YearMonth currentMonth = YearMonth.now(
                ZoneId.of("America/Fortaleza")
        );

        mockMvc.perform(get("/orders/seller")
                        .param("nomeVendedor", "João")
                        .param("year", String.valueOf(currentMonth.getYear()))
                        .param("month", String.valueOf(currentMonth.getMonthValue()))
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nomeVendedor").value("João"));
    }

    @Test
    @DisplayName("deve retornar 400 quando somente o ano do filtro for enviado")
    void deveRetornar400QuandoFiltroDeMesEstiverIncompleto() throws Exception {
        mockMvc.perform(get("/orders/seller")
                        .param("nomeVendedor", "João")
                        .param("year", "2026")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Ano e mês precisam ser enviados juntos"));
    }

    @Test
    @DisplayName("deve retornar 400 ao criar pedido sem campos obrigatórios")
    void deveRetornar400SemCamposObrigatorios() throws Exception {
        mockMvc.perform(multipart("/orders")
                        .file(image("foto.jpg"))
                        .param("nomeVendedor", "João")
                        .param("marca", "Arezzo")
                        .param("numeroCliente", "85999990001")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isBadRequest());
    }

    private String authenticate() throws Exception {
        String loginBody = """
                {
                  "username": "vendedor_test",
                  "password": "senha123"
                }
                """;

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andReturn();

        return JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.accessToken"
        );
    }

    private MvcResult createOrder(
            String fileName,
            String nomeCliente,
            String nomeVendedor,
            String marca,
            String valorTotal,
            String numeroCliente
    ) throws Exception {
        return mockMvc.perform(multipart("/orders")
                        .file(image(fileName))
                        .param("nomeCliente", nomeCliente)
                        .param("nomeVendedor", nomeVendedor)
                        .param("marca", marca)
                        .param("valorTotal", valorTotal)
                        .param("numeroCliente", numeroCliente)
                        .header("Authorization", bearerToken()))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private MockMultipartFile image(String fileName) {
        return new MockMultipartFile(
                "foto",
                fileName,
                MediaType.IMAGE_JPEG_VALUE,
                "conteudo".getBytes()
        );
    }

    private String bearerToken() {
        return "Bearer " + accessToken;
    }
}
