package com.guilhermepagio.aureus.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.guilhermepagio.aureus.backend.domain.dto.CategoriaResponseDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ContaResponseDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaFixaResponseDTO;
import com.guilhermepagio.aureus.backend.service.DespesaFixaService;

@ExtendWith(MockitoExtension.class)
public class DespesaFixaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DespesaFixaService despesaFixaService;

    @InjectMocks
    private DespesaFixaController despesaFixaController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(despesaFixaController).build();
    }

    @Test
    public void deveListarDespesasFixas() throws Exception {
        ContaResponseDTO conta = new ContaResponseDTO(1L, "Nubank", "Principal");
        CategoriaResponseDTO categoria = new CategoriaResponseDTO(2L, "Moradia", "Aluguel");
        DespesaFixaResponseDTO response = new DespesaFixaResponseDTO(10L, "Aluguel", new BigDecimal("1200.00"), conta, categoria, "Obs", LocalDate.of(2024, 1, 1));

        when(despesaFixaService.listar()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/despesas-fixas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].descricao").value("Aluguel"))
                .andExpect(jsonPath("$[0].conta.id").value(1L))
                .andExpect(jsonPath("$[0].categoria.id").value(2L));
    }

    @Test
    public void deveCriarDespesaFixaValida() throws Exception {
        String json = """
        {
            "descricao": "Aluguel",
            "valor": 1200.00,
            "conta": { "id": 1 },
            "categoria": { "id": 2 },
            "observacoes": "Obs",
            "dataInicio": "2024-01-01"
        }
        """;
        ContaResponseDTO conta = new ContaResponseDTO(1L, "Nubank", "Principal");
        CategoriaResponseDTO categoria = new CategoriaResponseDTO(2L, "Moradia", "Aluguel");
        DespesaFixaResponseDTO response = new DespesaFixaResponseDTO(10L, "Aluguel", new BigDecimal("1200.00"), conta, categoria, "Obs", LocalDate.of(2024, 1, 1));

        when(despesaFixaService.criar(any(DespesaFixaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/despesas-fixas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.descricao").value("Aluguel"));
    }

    @Test
    public void deveRejeitarCriarDespesaFixaInvalida() throws Exception {
        String json = "{\"descricao\":\"\",\"valor\":-10}";

        mockMvc.perform(post("/api/despesas-fixas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deveRetornar400QuandoViolacaoIntegridadeAoCriar() throws Exception {
        String json = """
        {
            "descricao": "Aluguel",
            "valor": 1200.00,
            "conta": { "id": 999 },
            "categoria": { "id": 2 },
            "observacoes": "Obs",
            "dataInicio": "2024-01-01"
        }
        """;

        when(despesaFixaService.criar(any(DespesaFixaRequestDTO.class)))
                .thenThrow(new DataIntegrityViolationException("FK"));

        mockMvc.perform(post("/api/despesas-fixas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de integridade relacional. Verifique os vínculos informados."));
    }

    @Test
    public void deveAtualizarDespesaFixaExistente() throws Exception {
        String json = """
        {
            "descricao": "Aluguel",
            "valor": 1300.00,
            "conta": { "id": 1 },
            "categoria": { "id": 2 },
            "observacoes": "Obs",
            "dataInicio": "2024-01-01"
        }
        """;
        ContaResponseDTO conta = new ContaResponseDTO(1L, "Nubank", "Principal");
        CategoriaResponseDTO categoria = new CategoriaResponseDTO(2L, "Moradia", "Aluguel");
        DespesaFixaResponseDTO response = new DespesaFixaResponseDTO(10L, "Aluguel", new BigDecimal("1300.00"), conta, categoria, "Obs", LocalDate.of(2024, 1, 1));

        when(despesaFixaService.atualizar(eq(10L), any(DespesaFixaRequestDTO.class))).thenReturn(Optional.of(response));

        mockMvc.perform(put("/api/despesas-fixas/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(1300.00));
    }

    @Test
    public void deveRetornar404AoAtualizarDespesaFixaInexistente() throws Exception {
        String json = """
        {
            "descricao": "Aluguel",
            "valor": 1300.00,
            "conta": { "id": 1 },
            "categoria": { "id": 2 },
            "observacoes": "Obs",
            "dataInicio": "2024-01-01"
        }
        """;

        when(despesaFixaService.atualizar(eq(999L), any(DespesaFixaRequestDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/despesas-fixas/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deveRejeitarAtualizarDespesaFixaInvalida() throws Exception {
        String json = """
        {
            "descricao": "",
            "valor": -10.00,
            "conta": null,
            "categoria": null,
            "dataInicio": null
        }
        """;

        mockMvc.perform(put("/api/despesas-fixas/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deveRetornar400QuandoViolacaoIntegridadeAoAtualizar() throws Exception {
        String json = """
        {
            "descricao": "Aluguel",
            "valor": 1200.00,
            "conta": { "id": 999 },
            "categoria": { "id": 999 },
            "observacoes": "Obs",
            "dataInicio": "2024-01-01"
        }
        """;

        when(despesaFixaService.atualizar(eq(10L), any(DespesaFixaRequestDTO.class)))
                .thenThrow(new DataIntegrityViolationException("FK"));

        mockMvc.perform(put("/api/despesas-fixas/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de integridade relacional. Verifique os vínculos informados."));
    }

    @Test
    public void deveExcluirDespesaFixaComSucesso() throws Exception {
        when(despesaFixaService.excluir(10L)).thenReturn(true);

        mockMvc.perform(delete("/api/despesas-fixas/10"))
                .andExpect(status().isNoContent());

        verify(despesaFixaService).excluir(10L);
    }

    @Test
    public void deveRetornar404AoExcluirDespesaFixaInexistente() throws Exception {
        when(despesaFixaService.excluir(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/despesas-fixas/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deveRetornar400AoExcluirDespesaFixaEmUso() throws Exception {
        when(despesaFixaService.excluir(10L)).thenThrow(new DataIntegrityViolationException("Em uso"));

        mockMvc.perform(delete("/api/despesas-fixas/10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Não é possível excluir esta despesa porque ela está em uso."));
    }
}
