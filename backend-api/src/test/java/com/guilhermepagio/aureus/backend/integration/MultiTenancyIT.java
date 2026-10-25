package com.guilhermepagio.aureus.backend.integration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.guilhermepagio.aureus.backend.AbstractIntegrationTest;
import com.guilhermepagio.aureus.backend.security.JwtUtil;
import com.guilhermepagio.aureus.backend.domain.dto.CategoriaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ContaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ContaResponseDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.IdReferenceDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaVariavelRequestDTO;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class MultiTenancyIT extends AbstractIntegrationTest {

    public MultiTenancyIT(
            final MockMvc mockMvc,
            final JwtUtil jwtUtil,
            @Value("${jwt.secret}") final String jwtSecret) {
        super(mockMvc, jwtUtil, jwtSecret);
    }

    private String novoTenant(final String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    private Long criarConta(final String tenant, final String descricao) throws Exception {
        final ContaRequestDTO request = new ContaRequestDTO(descricao, "Obs " + descricao);
        final MvcResult result = mockMvc.perform(post("/api/contas")
                .cookie(authCookie(tenant))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        final ContaResponseDTO response = objectMapper.readValue(result.getResponse().getContentAsString(), ContaResponseDTO.class);
        return response.id();
    }

    private Long criarCategoria(final String tenant, final String descricao) throws Exception {
        final CategoriaRequestDTO request = new CategoriaRequestDTO(descricao, "Obs " + descricao);
        final MvcResult result = mockMvc.perform(post("/api/categorias")
                .cookie(authCookie(tenant))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @DisplayName("Listagem deve retornar estritamente os recursos do próprio tenant")
    void deveListarApenasRecursosDoProprioTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        String descContaA = "ContaA-" + UUID.randomUUID().toString().substring(0, 8);
        String descContaB = "ContaB-" + UUID.randomUUID().toString().substring(0, 8);
        Long contaAId = criarConta(tenantA, descContaA);
        Long contaBId = criarConta(tenantB, descContaB);

        Long catAId = criarCategoria(tenantA, "CatA-" + UUID.randomUUID().toString().substring(0, 8));
        Long catBId = criarCategoria(tenantB, "CatB-" + UUID.randomUUID().toString().substring(0, 8));

        String descDespA = "DespA-" + UUID.randomUUID().toString().substring(0, 8);
        String descDespB = "DespB-" + UUID.randomUUID().toString().substring(0, 8);

        DespesaFixaRequestDTO despA = new DespesaFixaRequestDTO(
                descDespA, new BigDecimal("100.00"),
                new IdReferenceDTO(contaAId), new IdReferenceDTO(catAId),
                "Obs", LocalDate.of(2026, 1, 1)
        );
        mockMvc.perform(post("/api/despesas-fixas")
                .cookie(authCookie(tenantA))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(despA)))
                .andExpect(status().isOk());

        DespesaFixaRequestDTO despB = new DespesaFixaRequestDTO(
                descDespB, new BigDecimal("200.00"),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catBId),
                "Obs", LocalDate.of(2026, 1, 1)
        );
        mockMvc.perform(post("/api/despesas-fixas")
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(despB)))
                .andExpect(status().isOk());

        // Tenant A lista contas
        mockMvc.perform(get("/api/contas")
                .cookie(authCookie(tenantA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].descricao", hasItem(descContaA)))
                .andExpect(jsonPath("$[*].descricao", not(hasItem(descContaB))));

        // Tenant B lista contas
        mockMvc.perform(get("/api/contas")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].descricao", hasItem(descContaB)))
                .andExpect(jsonPath("$[*].descricao", not(hasItem(descContaA))));

        // Tenant A lista despesas fixas
        mockMvc.perform(get("/api/despesas-fixas")
                .cookie(authCookie(tenantA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].descricao", hasItem(descDespA)))
                .andExpect(jsonPath("$[*].descricao", not(hasItem(descDespB))));

        // Tenant B lista despesas fixas
        mockMvc.perform(get("/api/despesas-fixas")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].descricao", hasItem(descDespB)))
                .andExpect(jsonPath("$[*].descricao", not(hasItem(descDespA))));
    }

    @Test
    @DisplayName("Consulta de recurso pertencente a outro tenant deve retornar 404 Not Found")
    void deveRetornar404AoConsultarRecursoDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long contaAId = criarConta(tenantA, "ContaA-" + UUID.randomUUID().toString().substring(0, 8));

        mockMvc.perform(get("/api/contas/" + contaAId)
                .cookie(authCookie(tenantB)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Atualização de recurso pertencente a outro tenant deve retornar 404 Not Found")
    void deveRetornar404AoAtualizarRecursoDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        String descOriginal = "ContaA-" + UUID.randomUUID().toString().substring(0, 8);
        Long contaAId = criarConta(tenantA, descOriginal);

        ContaRequestDTO dtoAlteracao = new ContaRequestDTO("Invasor", "Hack");
        mockMvc.perform(put("/api/contas/" + contaAId)
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoAlteracao)))
                .andExpect(status().isNotFound());

        // Confirma que Conta de A permanece inalterada
        mockMvc.perform(get("/api/contas")
                .cookie(authCookie(tenantA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].descricao", hasItem(descOriginal)))
                .andExpect(jsonPath("$[*].descricao", not(hasItem("Invasor"))));
    }

    @Test
    @DisplayName("Exclusão de recurso pertencente a outro tenant deve retornar 404 Not Found")
    void deveRetornar404AoExcluirRecursoDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        String descOriginal = "ContaA-" + UUID.randomUUID().toString().substring(0, 8);
        Long contaAId = criarConta(tenantA, descOriginal);

        mockMvc.perform(delete("/api/contas/" + contaAId)
                .cookie(authCookie(tenantB))
                .with(csrf()))
                .andExpect(status().isNotFound());

        // Confirma que a Conta de A continua existindo
        mockMvc.perform(get("/api/contas")
                .cookie(authCookie(tenantA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].descricao", hasItem(descOriginal)));
    }

    @Test
    @DisplayName("Criação de despesa fixa vinculando conta de outro tenant deve retornar 403 Forbidden")
    void deveRetornar403AoCriarDespesaFixaComContaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long contaAId = criarConta(tenantA, "ContaA-" + UUID.randomUUID().toString().substring(0, 8));
        Long catBId = criarCategoria(tenantB, "CatB-" + UUID.randomUUID().toString().substring(0, 8));

        DespesaFixaRequestDTO desp = new DespesaFixaRequestDTO(
                "Desp B Cross", new BigDecimal("150.00"),
                new IdReferenceDTO(contaAId), new IdReferenceDTO(catBId),
                "Obs", LocalDate.of(2026, 1, 1)
        );

        mockMvc.perform(post("/api/despesas-fixas")
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(desp)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));

        mockMvc.perform(get("/api/despesas-fixas")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/api/despesas-fixas")
                .cookie(authCookie(tenantA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Criação de despesa fixa vinculando categoria de outro tenant deve retornar 403 Forbidden")
    void deveRetornar403AoCriarDespesaFixaComCategoriaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long catAId = criarCategoria(tenantA, "CatA-" + UUID.randomUUID().toString().substring(0, 8));
        Long contaBId = criarConta(tenantB, "ContaB-" + UUID.randomUUID().toString().substring(0, 8));

        DespesaFixaRequestDTO desp = new DespesaFixaRequestDTO(
                "Desp B Cross Cat", new BigDecimal("150.00"),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catAId),
                "Obs", LocalDate.of(2026, 1, 1)
        );

        mockMvc.perform(post("/api/despesas-fixas")
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(desp)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));

        mockMvc.perform(get("/api/despesas-fixas")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/api/despesas-fixas")
                .cookie(authCookie(tenantA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Criação de despesa variável vinculando conta de outro tenant deve retornar 403 Forbidden")
    void deveRetornar403AoCriarDespesaVariavelComContaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long contaAId = criarConta(tenantA, "ContaA-" + UUID.randomUUID().toString().substring(0, 8));
        Long catBId = criarCategoria(tenantB, "CatB-" + UUID.randomUUID().toString().substring(0, 8));

        DespesaVariavelRequestDTO desp = new DespesaVariavelRequestDTO(
                "Celular", "Loja X", LocalDate.of(2026, 1, 1),
                new BigDecimal("200.00"), 5, LocalDate.of(2026, 1, 1),
                new IdReferenceDTO(contaAId), new IdReferenceDTO(catBId), "Obs"
        );

        mockMvc.perform(post("/api/despesas-variaveis")
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(desp)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));

        mockMvc.perform(get("/api/despesas-variaveis")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/api/despesas-variaveis")
                .cookie(authCookie(tenantA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Criação de despesa variável vinculando categoria de outro tenant deve retornar 403 Forbidden")
    void deveRetornar403AoCriarDespesaVariavelComCategoriaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long contaBId = criarConta(tenantB, "ContaB-" + UUID.randomUUID().toString().substring(0, 8));
        Long catAId = criarCategoria(tenantA, "CatA-" + UUID.randomUUID().toString().substring(0, 8));

        DespesaVariavelRequestDTO desp = new DespesaVariavelRequestDTO(
                "Celular Cat", "Loja X", LocalDate.of(2026, 1, 1),
                new BigDecimal("200.00"), 5, LocalDate.of(2026, 1, 1),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catAId), "Obs"
        );

        mockMvc.perform(post("/api/despesas-variaveis")
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(desp)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));

        mockMvc.perform(get("/api/despesas-variaveis")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/api/despesas-variaveis")
                .cookie(authCookie(tenantA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Criação de receita fixa vinculando conta de outro tenant deve retornar 403 Forbidden")
    void deveRetornar403AoCriarReceitaFixaComContaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long contaAId = criarConta(tenantA, "ContaA-" + UUID.randomUUID().toString().substring(0, 8));
        Long catBId = criarCategoria(tenantB, "CatB-" + UUID.randomUUID().toString().substring(0, 8));

        ReceitaFixaRequestDTO rec = new ReceitaFixaRequestDTO(
                "Salário", new BigDecimal("3000.00"),
                new IdReferenceDTO(contaAId), new IdReferenceDTO(catBId),
                "Obs", LocalDate.of(2026, 1, 1)
        );

        mockMvc.perform(post("/api/receitas-fixas")
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rec)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));

        mockMvc.perform(get("/api/receitas-fixas")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/api/receitas-fixas")
                .cookie(authCookie(tenantA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Criação de receita fixa vinculando categoria de outro tenant deve retornar 403 Forbidden")
    void deveRetornar403AoCriarReceitaFixaComCategoriaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long contaBId = criarConta(tenantB, "ContaB-" + UUID.randomUUID().toString().substring(0, 8));
        Long catAId = criarCategoria(tenantA, "CatA-" + UUID.randomUUID().toString().substring(0, 8));

        ReceitaFixaRequestDTO rec = new ReceitaFixaRequestDTO(
                "Salário Cat", new BigDecimal("3000.00"),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catAId),
                "Obs", LocalDate.of(2026, 1, 1)
        );

        mockMvc.perform(post("/api/receitas-fixas")
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rec)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));

        mockMvc.perform(get("/api/receitas-fixas")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/api/receitas-fixas")
                .cookie(authCookie(tenantA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Criação de receita variável vinculando conta de outro tenant deve retornar 403 Forbidden")
    void deveRetornar403AoCriarReceitaVariavelComContaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long contaAId = criarConta(tenantA, "ContaA-" + UUID.randomUUID().toString().substring(0, 8));
        Long catBId = criarCategoria(tenantB, "CatB-" + UUID.randomUUID().toString().substring(0, 8));

        ReceitaVariavelRequestDTO rec = new ReceitaVariavelRequestDTO(
                "Freela Conta", new BigDecimal("500.00"), 1, LocalDate.of(2026, 1, 1),
                new IdReferenceDTO(contaAId), new IdReferenceDTO(catBId), "Obs"
        );

        mockMvc.perform(post("/api/receitas-variaveis")
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rec)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));

        mockMvc.perform(get("/api/receitas-variaveis")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/api/receitas-variaveis")
                .cookie(authCookie(tenantA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Criação de receita variável vinculando categoria de outro tenant deve retornar 403 Forbidden")
    void deveRetornar403AoCriarReceitaVariavelComCategoriaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long contaBId = criarConta(tenantB, "ContaB-" + UUID.randomUUID().toString().substring(0, 8));
        Long catAId = criarCategoria(tenantA, "CatA-" + UUID.randomUUID().toString().substring(0, 8));

        ReceitaVariavelRequestDTO rec = new ReceitaVariavelRequestDTO(
                "Freela", new BigDecimal("500.00"), 1, LocalDate.of(2026, 1, 1),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catAId), "Obs"
        );

        mockMvc.perform(post("/api/receitas-variaveis")
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rec)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));

        mockMvc.perform(get("/api/receitas-variaveis")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/api/receitas-variaveis")
                .cookie(authCookie(tenantA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Atualização (PUT) de despesa fixa vinculando conta de outro tenant deve retornar 403 Forbidden")
    void deveRetornar403AoAtualizarDespesaFixaComContaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long contaAId = criarConta(tenantA, "ContaA-" + UUID.randomUUID().toString().substring(0, 8));
        Long contaBId = criarConta(tenantB, "ContaB-" + UUID.randomUUID().toString().substring(0, 8));
        Long catBId = criarCategoria(tenantB, "CatB-" + UUID.randomUUID().toString().substring(0, 8));

        DespesaFixaRequestDTO despB = new DespesaFixaRequestDTO(
                "Desp B", new BigDecimal("100.00"),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catBId),
                "Obs", LocalDate.of(2026, 1, 1)
        );
        MvcResult res = mockMvc.perform(post("/api/despesas-fixas")
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(despB)))
                .andExpect(status().isOk())
                .andReturn();
        Long despesaBId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        DespesaFixaRequestDTO despAlterada = new DespesaFixaRequestDTO(
                "Desp B Alterada", new BigDecimal("950.00"),
                new IdReferenceDTO(contaAId), new IdReferenceDTO(catBId),
                "Obs Alterada", LocalDate.of(2026, 1, 1)
        );
        mockMvc.perform(put("/api/despesas-fixas/" + despesaBId)
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(despAlterada)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));

        mockMvc.perform(get("/api/despesas-fixas")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Desp B"))
                .andExpect(jsonPath("$[0].valor").value(100.00))
                .andExpect(jsonPath("$[0].conta.id").value(contaBId))
                .andExpect(jsonPath("$[0].categoria.id").value(catBId))
                .andExpect(jsonPath("$[0].observacoes").value("Obs"));
    }

    @Test
    @DisplayName("Atualização (PUT) de despesa fixa vinculando categoria de outro tenant deve retornar 403 Forbidden")
    void deveRetornar403AoAtualizarDespesaFixaComCategoriaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long catAId = criarCategoria(tenantA, "CatA-" + UUID.randomUUID().toString().substring(0, 8));
        Long contaBId = criarConta(tenantB, "ContaB-" + UUID.randomUUID().toString().substring(0, 8));
        Long catBId = criarCategoria(tenantB, "CatB-" + UUID.randomUUID().toString().substring(0, 8));

        DespesaFixaRequestDTO despB = new DespesaFixaRequestDTO(
                "Desp B Original", new BigDecimal("100.00"),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catBId),
                "Obs", LocalDate.of(2026, 1, 1)
        );
        MvcResult res = mockMvc.perform(post("/api/despesas-fixas")
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(despB)))
                .andExpect(status().isOk())
                .andReturn();
        Long despesaBId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        DespesaFixaRequestDTO despAlterada = new DespesaFixaRequestDTO(
                "Desp B Alterada Cat", new BigDecimal("950.00"),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catAId),
                "Obs Alterada", LocalDate.of(2026, 1, 1)
        );
        mockMvc.perform(put("/api/despesas-fixas/" + despesaBId)
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(despAlterada)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));

        mockMvc.perform(get("/api/despesas-fixas")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Desp B Original"))
                .andExpect(jsonPath("$[0].valor").value(100.00))
                .andExpect(jsonPath("$[0].conta.id").value(contaBId))
                .andExpect(jsonPath("$[0].categoria.id").value(catBId))
                .andExpect(jsonPath("$[0].observacoes").value("Obs"));
    }

    @Test
    @DisplayName("Atualização (PUT) de despesa variável vinculando conta ou categoria de outro tenant deve retornar 403 Forbidden")
    void deveRetornar403AoAtualizarDespesaVariavelComContaOuCategoriaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long contaAId = criarConta(tenantA, "ContaA-" + UUID.randomUUID().toString().substring(0, 8));
        Long catAId = criarCategoria(tenantA, "CatA-" + UUID.randomUUID().toString().substring(0, 8));
        Long contaBId = criarConta(tenantB, "ContaB-" + UUID.randomUUID().toString().substring(0, 8));
        Long catBId = criarCategoria(tenantB, "CatB-" + UUID.randomUUID().toString().substring(0, 8));

        DespesaVariavelRequestDTO despB = new DespesaVariavelRequestDTO(
                "Desp Var B", "Loja B", LocalDate.of(2026, 1, 1),
                new BigDecimal("200.00"), 3, LocalDate.of(2026, 1, 1),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catBId), "Obs"
        );
        MvcResult res = mockMvc.perform(post("/api/despesas-variaveis")
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(despB)))
                .andExpect(status().isOk())
                .andReturn();
        Long despesaVarBId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        DespesaVariavelRequestDTO alteracaoContaAlheia = new DespesaVariavelRequestDTO(
                "Desp Var Alterada Conta", "Loja X", LocalDate.of(2026, 1, 1),
                new BigDecimal("800.00"), 3, LocalDate.of(2026, 1, 1),
                new IdReferenceDTO(contaAId), new IdReferenceDTO(catBId), "Obs Alterada"
        );
        mockMvc.perform(put("/api/despesas-variaveis/" + despesaVarBId)
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(alteracaoContaAlheia)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));

        mockMvc.perform(get("/api/despesas-variaveis")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Desp Var B"))
                .andExpect(jsonPath("$[0].localCompra").value("Loja B"))
                .andExpect(jsonPath("$[0].valorParcela").value(200.00))
                .andExpect(jsonPath("$[0].conta.id").value(contaBId))
                .andExpect(jsonPath("$[0].categoria.id").value(catBId))
                .andExpect(jsonPath("$[0].observacoes").value("Obs"));

        DespesaVariavelRequestDTO alteracaoCatAlheia = new DespesaVariavelRequestDTO(
                "Desp Var Alterada Cat", "Loja X", LocalDate.of(2026, 1, 1),
                new BigDecimal("800.00"), 3, LocalDate.of(2026, 1, 1),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catAId), "Obs Alterada"
        );
        mockMvc.perform(put("/api/despesas-variaveis/" + despesaVarBId)
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(alteracaoCatAlheia)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));

        mockMvc.perform(get("/api/despesas-variaveis")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Desp Var B"))
                .andExpect(jsonPath("$[0].localCompra").value("Loja B"))
                .andExpect(jsonPath("$[0].valorParcela").value(200.00))
                .andExpect(jsonPath("$[0].conta.id").value(contaBId))
                .andExpect(jsonPath("$[0].categoria.id").value(catBId))
                .andExpect(jsonPath("$[0].observacoes").value("Obs"));
    }

    @Test
    @DisplayName("Atualização (PUT) de receita fixa vinculando conta ou categoria de outro tenant deve retornar 403 Forbidden")
    void deveRetornar403AoAtualizarReceitaFixaComContaOuCategoriaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long contaAId = criarConta(tenantA, "ContaA-" + UUID.randomUUID().toString().substring(0, 8));
        Long catAId = criarCategoria(tenantA, "CatA-" + UUID.randomUUID().toString().substring(0, 8));
        Long contaBId = criarConta(tenantB, "ContaB-" + UUID.randomUUID().toString().substring(0, 8));
        Long catBId = criarCategoria(tenantB, "CatB-" + UUID.randomUUID().toString().substring(0, 8));

        ReceitaFixaRequestDTO recB = new ReceitaFixaRequestDTO(
                "Rec Fixa B", new BigDecimal("3000.00"),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catBId),
                "Obs", LocalDate.of(2026, 1, 1)
        );
        MvcResult res = mockMvc.perform(post("/api/receitas-fixas")
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recB)))
                .andExpect(status().isOk())
                .andReturn();
        Long receitaFixaBId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        ReceitaFixaRequestDTO alteracaoContaAlheia = new ReceitaFixaRequestDTO(
                "Rec Fixa Alterada Conta", new BigDecimal("9000.00"),
                new IdReferenceDTO(contaAId), new IdReferenceDTO(catBId),
                "Obs Alterada", LocalDate.of(2026, 1, 1)
        );
        mockMvc.perform(put("/api/receitas-fixas/" + receitaFixaBId)
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(alteracaoContaAlheia)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));

        mockMvc.perform(get("/api/receitas-fixas")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Rec Fixa B"))
                .andExpect(jsonPath("$[0].valor").value(3000.00))
                .andExpect(jsonPath("$[0].conta.id").value(contaBId))
                .andExpect(jsonPath("$[0].categoria.id").value(catBId))
                .andExpect(jsonPath("$[0].observacoes").value("Obs"));

        ReceitaFixaRequestDTO alteracaoCatAlheia = new ReceitaFixaRequestDTO(
                "Rec Fixa Alterada Cat", new BigDecimal("9000.00"),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catAId),
                "Obs Alterada", LocalDate.of(2026, 1, 1)
        );
        mockMvc.perform(put("/api/receitas-fixas/" + receitaFixaBId)
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(alteracaoCatAlheia)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));

        mockMvc.perform(get("/api/receitas-fixas")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Rec Fixa B"))
                .andExpect(jsonPath("$[0].valor").value(3000.00))
                .andExpect(jsonPath("$[0].conta.id").value(contaBId))
                .andExpect(jsonPath("$[0].categoria.id").value(catBId))
                .andExpect(jsonPath("$[0].observacoes").value("Obs"));
    }

    @Test
    @DisplayName("Atualização (PUT) de receita variável vinculando conta ou categoria de outro tenant deve retornar 403 Forbidden")
    void deveRetornar403AoAtualizarReceitaVariavelComContaOuCategoriaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long contaAId = criarConta(tenantA, "ContaA-" + UUID.randomUUID().toString().substring(0, 8));
        Long catAId = criarCategoria(tenantA, "CatA-" + UUID.randomUUID().toString().substring(0, 8));
        Long contaBId = criarConta(tenantB, "ContaB-" + UUID.randomUUID().toString().substring(0, 8));
        Long catBId = criarCategoria(tenantB, "CatB-" + UUID.randomUUID().toString().substring(0, 8));

        ReceitaVariavelRequestDTO recB = new ReceitaVariavelRequestDTO(
                "Rec Var B", new BigDecimal("500.00"), 2, LocalDate.of(2026, 1, 1),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catBId), "Obs"
        );
        MvcResult res = mockMvc.perform(post("/api/receitas-variaveis")
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recB)))
                .andExpect(status().isOk())
                .andReturn();
        Long receitaVarBId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        ReceitaVariavelRequestDTO alteracaoContaAlheia = new ReceitaVariavelRequestDTO(
                "Rec Var Alterada Conta", new BigDecimal("1500.00"), 2, LocalDate.of(2026, 1, 1),
                new IdReferenceDTO(contaAId), new IdReferenceDTO(catBId), "Obs Alterada"
        );
        mockMvc.perform(put("/api/receitas-variaveis/" + receitaVarBId)
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(alteracaoContaAlheia)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));

        mockMvc.perform(get("/api/receitas-variaveis")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Rec Var B"))
                .andExpect(jsonPath("$[0].valorParcela").value(500.00))
                .andExpect(jsonPath("$[0].conta.id").value(contaBId))
                .andExpect(jsonPath("$[0].categoria.id").value(catBId))
                .andExpect(jsonPath("$[0].observacoes").value("Obs"));

        ReceitaVariavelRequestDTO alteracaoCatAlheia = new ReceitaVariavelRequestDTO(
                "Rec Var Alterada Cat", new BigDecimal("1500.00"), 2, LocalDate.of(2026, 1, 1),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catAId), "Obs Alterada"
        );
        mockMvc.perform(put("/api/receitas-variaveis/" + receitaVarBId)
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(alteracaoCatAlheia)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));

        mockMvc.perform(get("/api/receitas-variaveis")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Rec Var B"))
                .andExpect(jsonPath("$[0].valorParcela").value(500.00))
                .andExpect(jsonPath("$[0].conta.id").value(contaBId))
                .andExpect(jsonPath("$[0].categoria.id").value(catBId))
                .andExpect(jsonPath("$[0].observacoes").value("Obs"));
    }

    @Test
    @DisplayName("Consulta, atualização e exclusão de Categoria de outro tenant deve ocultar na listagem e retornar 404 Not Found em mutações")
    void deveRetornar404AoAtualizarOuExcluirCategoriaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        String descCatA = "CatA-" + UUID.randomUUID().toString().substring(0, 8);
        Long catAId = criarCategoria(tenantA, descCatA);

        mockMvc.perform(get("/api/categorias")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].descricao", not(hasItem(descCatA))));

        CategoriaRequestDTO dtoAlteracao = new CategoriaRequestDTO("Invasor", "Hack");
        mockMvc.perform(put("/api/categorias/" + catAId)
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoAlteracao)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/categorias/" + catAId)
                .cookie(authCookie(tenantB))
                .with(csrf()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/categorias")
                .cookie(authCookie(tenantA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].descricao", hasItem(descCatA)))
                .andExpect(jsonPath("$[*].descricao", not(hasItem("Invasor"))));
    }

    @Test
    @DisplayName("Atualização e exclusão de Despesa Fixa de outro tenant deve retornar 404 Not Found e manter registro intacto")
    void deveRetornar404AoAtualizarOuExcluirDespesaFixaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long contaAId = criarConta(tenantA, "ContaA-" + UUID.randomUUID().toString().substring(0, 8));
        Long catAId = criarCategoria(tenantA, "CatA-" + UUID.randomUUID().toString().substring(0, 8));
        Long contaBId = criarConta(tenantB, "ContaB-" + UUID.randomUUID().toString().substring(0, 8));
        Long catBId = criarCategoria(tenantB, "CatB-" + UUID.randomUUID().toString().substring(0, 8));

        DespesaFixaRequestDTO despA = new DespesaFixaRequestDTO(
                "Desp A", new BigDecimal("100.00"),
                new IdReferenceDTO(contaAId), new IdReferenceDTO(catAId),
                "Obs", LocalDate.of(2026, 1, 1)
        );
        MvcResult res = mockMvc.perform(post("/api/despesas-fixas")
                .cookie(authCookie(tenantA))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(despA)))
                .andExpect(status().isOk())
                .andReturn();
        Long despesaAId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/despesas-fixas")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        DespesaFixaRequestDTO despAlterada = new DespesaFixaRequestDTO(
                "Invasor Desp", new BigDecimal("999.00"),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catBId),
                "Hack", LocalDate.of(2026, 1, 1)
        );
        mockMvc.perform(put("/api/despesas-fixas/" + despesaAId)
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(despAlterada)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/despesas-fixas/" + despesaAId)
                .cookie(authCookie(tenantB))
                .with(csrf()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/despesas-fixas")
                .cookie(authCookie(tenantA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Desp A"))
                .andExpect(jsonPath("$[0].valor").value(100.00))
                .andExpect(jsonPath("$[0].conta.id").value(contaAId))
                .andExpect(jsonPath("$[0].categoria.id").value(catAId))
                .andExpect(jsonPath("$[0].observacoes").value("Obs"));
    }

    @Test
    @DisplayName("Acesso, atualização e exclusão de Despesa Variável de outro tenant deve ocultar na listagem, retornar 404 Not Found e manter registro intacto")
    void deveRetornar404AoConsultarAtualizarOuExcluirDespesaVariavelDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long contaAId = criarConta(tenantA, "ContaA-" + UUID.randomUUID().toString().substring(0, 8));
        Long catAId = criarCategoria(tenantA, "CatA-" + UUID.randomUUID().toString().substring(0, 8));
        Long contaBId = criarConta(tenantB, "ContaB-" + UUID.randomUUID().toString().substring(0, 8));
        Long catBId = criarCategoria(tenantB, "CatB-" + UUID.randomUUID().toString().substring(0, 8));

        DespesaVariavelRequestDTO despA = new DespesaVariavelRequestDTO(
                "Desp Var A", "Loja A", LocalDate.of(2026, 1, 1),
                new BigDecimal("150.00"), 2, LocalDate.of(2026, 1, 1),
                new IdReferenceDTO(contaAId), new IdReferenceDTO(catAId), "Obs A"
        );
        MvcResult res = mockMvc.perform(post("/api/despesas-variaveis")
                .cookie(authCookie(tenantA))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(despA)))
                .andExpect(status().isOk())
                .andReturn();
        Long despesaVarAId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/despesas-variaveis")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        DespesaVariavelRequestDTO despAlterada = new DespesaVariavelRequestDTO(
                "Invasor Var", "Loja Hack", LocalDate.of(2026, 1, 1),
                new BigDecimal("999.00"), 2, LocalDate.of(2026, 1, 1),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catBId), "Hack"
        );
        mockMvc.perform(put("/api/despesas-variaveis/" + despesaVarAId)
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(despAlterada)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/despesas-variaveis/" + despesaVarAId)
                .cookie(authCookie(tenantB))
                .with(csrf()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/despesas-variaveis")
                .cookie(authCookie(tenantA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Desp Var A"))
                .andExpect(jsonPath("$[0].localCompra").value("Loja A"))
                .andExpect(jsonPath("$[0].valorParcela").value(150.00))
                .andExpect(jsonPath("$[0].conta.id").value(contaAId))
                .andExpect(jsonPath("$[0].categoria.id").value(catAId))
                .andExpect(jsonPath("$[0].observacoes").value("Obs A"));
    }

    @Test
    @DisplayName("Acesso, atualização e exclusão de Receita Fixa de outro tenant deve ocultar na listagem, retornar 404 Not Found e manter registro intacto")
    void deveRetornar404AoConsultarAtualizarOuExcluirReceitaFixaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long contaAId = criarConta(tenantA, "ContaA-" + UUID.randomUUID().toString().substring(0, 8));
        Long catAId = criarCategoria(tenantA, "CatA-" + UUID.randomUUID().toString().substring(0, 8));
        Long contaBId = criarConta(tenantB, "ContaB-" + UUID.randomUUID().toString().substring(0, 8));
        Long catBId = criarCategoria(tenantB, "CatB-" + UUID.randomUUID().toString().substring(0, 8));

        ReceitaFixaRequestDTO recA = new ReceitaFixaRequestDTO(
                "Rec Fixa A", new BigDecimal("2500.00"),
                new IdReferenceDTO(contaAId), new IdReferenceDTO(catAId),
                "Obs A", LocalDate.of(2026, 1, 1)
        );
        MvcResult res = mockMvc.perform(post("/api/receitas-fixas")
                .cookie(authCookie(tenantA))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recA)))
                .andExpect(status().isOk())
                .andReturn();
        Long receitaFixaAId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/receitas-fixas")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        ReceitaFixaRequestDTO recAlterada = new ReceitaFixaRequestDTO(
                "Invasor Rec Fixa", new BigDecimal("9999.00"),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catBId),
                "Hack", LocalDate.of(2026, 1, 1)
        );
        mockMvc.perform(put("/api/receitas-fixas/" + receitaFixaAId)
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recAlterada)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/receitas-fixas/" + receitaFixaAId)
                .cookie(authCookie(tenantB))
                .with(csrf()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/receitas-fixas")
                .cookie(authCookie(tenantA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Rec Fixa A"))
                .andExpect(jsonPath("$[0].valor").value(2500.00))
                .andExpect(jsonPath("$[0].conta.id").value(contaAId))
                .andExpect(jsonPath("$[0].categoria.id").value(catAId))
                .andExpect(jsonPath("$[0].observacoes").value("Obs A"));
    }

    @Test
    @DisplayName("Acesso, atualização e exclusão de Receita Variável de outro tenant deve ocultar na listagem, retornar 404 Not Found e manter registro intacto")
    void deveRetornar404AoConsultarAtualizarOuExcluirReceitaVariavelDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long contaAId = criarConta(tenantA, "ContaA-" + UUID.randomUUID().toString().substring(0, 8));
        Long catAId = criarCategoria(tenantA, "CatA-" + UUID.randomUUID().toString().substring(0, 8));
        Long contaBId = criarConta(tenantB, "ContaB-" + UUID.randomUUID().toString().substring(0, 8));
        Long catBId = criarCategoria(tenantB, "CatB-" + UUID.randomUUID().toString().substring(0, 8));

        ReceitaVariavelRequestDTO recA = new ReceitaVariavelRequestDTO(
                "Rec Var A", new BigDecimal("400.00"), 3, LocalDate.of(2026, 1, 1),
                new IdReferenceDTO(contaAId), new IdReferenceDTO(catAId), "Obs A"
        );
        MvcResult res = mockMvc.perform(post("/api/receitas-variaveis")
                .cookie(authCookie(tenantA))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recA)))
                .andExpect(status().isOk())
                .andReturn();
        Long receitaVarAId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/receitas-variaveis")
                .cookie(authCookie(tenantB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        ReceitaVariavelRequestDTO recAlterada = new ReceitaVariavelRequestDTO(
                "Invasor Rec Var", new BigDecimal("9999.00"), 3, LocalDate.of(2026, 1, 1),
                new IdReferenceDTO(contaBId), new IdReferenceDTO(catBId), "Hack"
        );
        mockMvc.perform(put("/api/receitas-variaveis/" + receitaVarAId)
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recAlterada)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/receitas-variaveis/" + receitaVarAId)
                .cookie(authCookie(tenantB))
                .with(csrf()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/receitas-variaveis")
                .cookie(authCookie(tenantA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Rec Var A"))
                .andExpect(jsonPath("$[0].valorParcela").value(400.00))
                .andExpect(jsonPath("$[0].conta.id").value(contaAId))
                .andExpect(jsonPath("$[0].categoria.id").value(catAId))
                .andExpect(jsonPath("$[0].observacoes").value("Obs A"));
    }
}

