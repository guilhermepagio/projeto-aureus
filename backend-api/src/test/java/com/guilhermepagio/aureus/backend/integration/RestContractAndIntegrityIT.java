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
import com.guilhermepagio.aureus.backend.domain.dto.DespesaFixaResponseDTO;
import com.guilhermepagio.aureus.backend.domain.dto.IdReferenceDTO;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RestContractAndIntegrityIT extends AbstractIntegrationTest {

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

    private Long criarDespesaFixa(String tenant, String descricao, Long contaId, Long categoriaId) throws Exception {
        DespesaFixaRequestDTO request = new DespesaFixaRequestDTO(
                descricao, new BigDecimal("250.00"),
                new IdReferenceDTO(contaId), new IdReferenceDTO(categoriaId),
                "Obs", LocalDate.of(2026, 1, 1)
        );
        MvcResult result = mockMvc.perform(post("/api/despesas-fixas")
                .cookie(authCookie(tenant))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        DespesaFixaResponseDTO response = objectMapper.readValue(result.getResponse().getContentAsString(), DespesaFixaResponseDTO.class);
        return response.id();
    }

    @Test
    @DisplayName("Tentativa de exclusão de Conta ou Categoria com vínculos ativos deve falhar com 400 Bad Request via FK")
    void deveRetornar400AoExcluirContaOuCategoriaComVinculoAtivo() throws Exception {
        String tenant = novoTenant("tenant-fk");

        Long contaId = criarConta(tenant, "Conta FK");
        Long catId = criarCategoria(tenant, "Cat FK");
        Long despesaId = criarDespesaFixa(tenant, "Desp FK", contaId, catId);

        // Tentativa de exclusão da conta vinculada à despesa fixa
        mockMvc.perform(delete("/api/contas/" + contaId)
                .cookie(authCookie(tenant))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Não é possível realizar a operação devido a vínculos ativos ou restrições de integridade.")));

        // Tentativa de exclusão da categoria vinculada à despesa fixa
        mockMvc.perform(delete("/api/categorias/" + catId)
                .cookie(authCookie(tenant))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Não é possível realizar a operação devido a vínculos ativos ou restrições de integridade.")));

        // Exclui a despesa vinculada -> 204 No Content
        mockMvc.perform(delete("/api/despesas-fixas/" + despesaId)
                .cookie(authCookie(tenant))
                .with(csrf()))
                .andExpect(status().isNoContent());

        // Agora a exclusão da conta e da categoria deve ocorrer com sucesso -> 204 No Content
        mockMvc.perform(delete("/api/contas/" + contaId)
                .cookie(authCookie(tenant))
                .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/categorias/" + catId)
                .cookie(authCookie(tenant))
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Validação de DTOs Records deve rejeitar campos inválidos com 400 Bad Request estruturado")
    void deveValidarCamposDeDtoCom400() throws Exception {
        String tenant = novoTenant("tenant-val");

        // Descrição em branco
        String jsonDescricaoBranco = "{\"descricao\":\"   \",\"observacoes\":\"teste\"}";
        mockMvc.perform(post("/api/contas")
                .cookie(authCookie(tenant))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDescricaoBranco))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.descricao").exists());

        // Descrição acima do limite de caracteres (> 20)
        String jsonDescricaoLonga = "{\"descricao\":\"Esta descrição tem mais de vinte caracteres\",\"observacoes\":\"teste\"}";
        mockMvc.perform(post("/api/contas")
                .cookie(authCookie(tenant))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDescricaoLonga))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.descricao").exists());

        // Categoria com descrição em branco
        mockMvc.perform(post("/api/categorias")
                .cookie(authCookie(tenant))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDescricaoBranco))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.descricao").exists());

        // Despesa fixa sem conta e categoria
        String jsonDespesaSemRefs = "{\"descricao\":\"Internet\",\"valor\":100.00}";
        mockMvc.perform(post("/api/despesas-fixas")
                .cookie(authCookie(tenant))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDespesaSemRefs))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.conta").exists())
                .andExpect(jsonPath("$.fieldErrors.categoria").exists());

        // Despesa variável com parcelas inválidas (zero)
        String jsonDespesaVarParcelasInvalidas = """
                {
                    "descricao": "Mouse",
                    "valorParcela": 50.00,
                    "quantidadeParcelas": 0,
                    "dataInicio": "2026-01-01",
                    "conta": {"id": 1},
                    "categoria": {"id": 1}
                }
                """;
        mockMvc.perform(post("/api/despesas-variaveis")
                .cookie(authCookie(tenant))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDespesaVarParcelasInvalidas))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.quantidadeParcelas").exists());

        // Despesa fixa com valor negativo ou zero
        String jsonDespesaValorNegativo = "{\"descricao\":\"Luz\",\"valor\":-10.00,\"conta\":{\"id\":1},\"categoria\":{\"id\":1}}";
        mockMvc.perform(post("/api/despesas-fixas")
                .cookie(authCookie(tenant))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDespesaValorNegativo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.valor").exists());
    }

    @Test
    @DisplayName("Corpo de requisição JSON mal formatado deve retornar 400 Bad Request")
    void deveRetornar400ParaCorpoJsonMalFormatado() throws Exception {
        String tenant = novoTenant("tenant-malformado");

        mockMvc.perform(post("/api/contas")
                .cookie(authCookie(tenant))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"descricao\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Corpo da requisição inválido ou mal formatado.")));
    }

    @Test
    @DisplayName("Método HTTP não suportado para endpoint deve retornar 405 Method Not Allowed")
    void deveRetornar405ParaMetodoNaoSuportado() throws Exception {
        String tenant = novoTenant("tenant-method");

        mockMvc.perform(patch("/api/contas")
                .cookie(authCookie(tenant))
                .with(csrf()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message", containsString("Método HTTP não suportado")));
    }
}
