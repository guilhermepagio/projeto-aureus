package com.guilhermepagio.aureus.backend.integration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.guilhermepagio.aureus.backend.AbstractIntegrationTest;
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

public class MultiTenancyIT extends AbstractIntegrationTest {

    private String novoTenant(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    private Long criarConta(String tenant, String descricao) throws Exception {
        ContaRequestDTO request = new ContaRequestDTO(descricao, "Obs " + descricao);
        MvcResult result = mockMvc.perform(post("/api/contas")
                .cookie(authCookie(tenant))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        ContaResponseDTO response = objectMapper.readValue(result.getResponse().getContentAsString(), ContaResponseDTO.class);
        return response.id();
    }

    private Long criarCategoria(String tenant, String descricao) throws Exception {
        CategoriaRequestDTO request = new CategoriaRequestDTO(descricao, "Obs " + descricao);
        MvcResult result = mockMvc.perform(post("/api/categorias")
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
    }

    @Test
    @DisplayName("Atualização (PUT) de despesa fixa vinculando conta de outro tenant deve retornar 403 Forbidden")
    void deveRetornar403AoAtualizarDespesaFixaComContaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long contaAId = criarConta(tenantA, "ContaA-" + UUID.randomUUID().toString().substring(0, 8));
        Long contaBId = criarConta(tenantB, "ContaB-" + UUID.randomUUID().toString().substring(0, 8));
        Long catBId = criarCategoria(tenantB, "CatB-" + UUID.randomUUID().toString().substring(0, 8));

        // B cria despesa legítima
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

        // B tenta alterar a despesa vinculando à Conta de A
        DespesaFixaRequestDTO despAlterada = new DespesaFixaRequestDTO(
                "Desp B Alterada", new BigDecimal("100.00"),
                new IdReferenceDTO(contaAId), new IdReferenceDTO(catBId),
                "Obs", LocalDate.of(2026, 1, 1)
        );
        mockMvc.perform(put("/api/despesas-fixas/" + despesaBId)
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(despAlterada)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));
    }

    @Test
    @DisplayName("Atualização e exclusão de Categoria de outro tenant deve retornar 404 Not Found")
    void deveRetornar404AoAtualizarOuExcluirCategoriaDeOutroTenant() throws Exception {
        String tenantA = novoTenant("tenant-a");
        String tenantB = novoTenant("tenant-b");

        Long catAId = criarCategoria(tenantA, "CatA-" + UUID.randomUUID().toString().substring(0, 8));

        // B tenta atualizar categoria de A
        CategoriaRequestDTO dtoAlteracao = new CategoriaRequestDTO("Invasor", "Hack");
        mockMvc.perform(put("/api/categorias/" + catAId)
                .cookie(authCookie(tenantB))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoAlteracao)))
                .andExpect(status().isNotFound());

        // B tenta excluir categoria de A
        mockMvc.perform(delete("/api/categorias/" + catAId)
                .cookie(authCookie(tenantB))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
