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
import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelResponseDTO;
import com.guilhermepagio.aureus.backend.service.DespesaVariavelService;

@ExtendWith(MockitoExtension.class)
public class DespesaVariavelControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DespesaVariavelService despesaVariavelService;

    @InjectMocks
    private DespesaVariavelController despesaVariavelController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(despesaVariavelController).build();
    }

    @Test
    public void deveListarDespesasVariaveis() throws Exception {
        ContaResponseDTO conta = new ContaResponseDTO(1L, "Nubank", "Principal");
        CategoriaResponseDTO categoria = new CategoriaResponseDTO(2L, "Compras", "Gerais");
        DespesaVariavelResponseDTO response = new DespesaVariavelResponseDTO(
            10L, "Notebook", "Kabum", LocalDate.of(2024, 1, 10),
            new BigDecimal("1000.00"), 3, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 1),
            conta, categoria, "Obs"
        );

        when(despesaVariavelService.listar()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/despesas-variaveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].descricao").value("Notebook"))
                .andExpect(jsonPath("$[0].localCompra").value("Kabum"))
                .andExpect(jsonPath("$[0].conta.id").value(1L))
                .andExpect(jsonPath("$[0].categoria.id").value(2L));
    }

    @Test
    public void deveCriarDespesaVariavelValida() throws Exception {
        String json = """
        {
            "descricao": "Notebook",
            "localCompra": "Kabum",
            "dataCompra": "2024-01-10",
            "valorParcela": 1000.00,
            "quantidadeParcelas": 3,
            "dataInicio": "2024-01-15",
            "conta": { "id": 1 },
            "categoria": { "id": 2 },
            "observacoes": "Obs"
        }
        """;
        ContaResponseDTO conta = new ContaResponseDTO(1L, "Nubank", "Principal");
        CategoriaResponseDTO categoria = new CategoriaResponseDTO(2L, "Compras", "Gerais");
        DespesaVariavelResponseDTO response = new DespesaVariavelResponseDTO(
            10L, "Notebook", "Kabum", LocalDate.of(2024, 1, 10),
            new BigDecimal("1000.00"), 3, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 1),
            conta, categoria, "Obs"
        );

        when(despesaVariavelService.criar(any(DespesaVariavelRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/despesas-variaveis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.dataInicio").value("2024-01-01"))
                .andExpect(jsonPath("$.dataFim").value("2024-03-01"));
    }

    @Test
    public void deveRejeitarCriarDespesaVariavelInvalida() throws Exception {
        String json = "{\"descricao\":\"\",\"quantidadeParcelas\":0}";

        mockMvc.perform(post("/api/despesas-variaveis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deveRetornar400QuandoViolacaoIntegridadeAoCriar() throws Exception {
        String json = """
        {
            "descricao": "Notebook",
            "localCompra": "Kabum",
            "dataCompra": "2024-01-10",
            "valorParcela": 1000.00,
            "quantidadeParcelas": 3,
            "dataInicio": "2024-01-15",
            "conta": { "id": 999 },
            "categoria": { "id": 2 },
            "observacoes": "Obs"
        }
        """;

        when(despesaVariavelService.criar(any(DespesaVariavelRequestDTO.class)))
                .thenThrow(new DataIntegrityViolationException("FK"));

        mockMvc.perform(post("/api/despesas-variaveis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de integridade relacional. Verifique os vínculos informados."));
    }

    @Test
    public void deveAtualizarDespesaVariavelExistente() throws Exception {
        String json = """
        {
            "descricao": "Notebook Pro",
            "localCompra": "Kabum Tech",
            "dataCompra": "2024-01-10",
            "valorParcela": 1200.00,
            "quantidadeParcelas": 4,
            "dataInicio": "2024-01-15",
            "conta": { "id": 1 },
            "categoria": { "id": 2 },
            "observacoes": "Obs"
        }
        """;
        ContaResponseDTO conta = new ContaResponseDTO(1L, "Nubank", "Principal");
        CategoriaResponseDTO categoria = new CategoriaResponseDTO(2L, "Compras", "Gerais");
        DespesaVariavelResponseDTO response = new DespesaVariavelResponseDTO(
            10L, "Notebook Pro", "Kabum Tech", LocalDate.of(2024, 1, 10),
            new BigDecimal("1200.00"), 4, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 4, 1),
            conta, categoria, "Obs"
        );

        when(despesaVariavelService.atualizar(eq(10L), any(DespesaVariavelRequestDTO.class))).thenReturn(Optional.of(response));

        mockMvc.perform(put("/api/despesas-variaveis/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorParcela").value(1200.00));
    }

    @Test
    public void deveRetornar404AoAtualizarDespesaVariavelInexistente() throws Exception {
        String json = """
        {
            "descricao": "Notebook Pro",
            "localCompra": "Kabum Tech",
            "dataCompra": "2024-01-10",
            "valorParcela": 1200.00,
            "quantidadeParcelas": 4,
            "dataInicio": "2024-01-15",
            "conta": { "id": 1 },
            "categoria": { "id": 2 },
            "observacoes": "Obs"
        }
        """;

        when(despesaVariavelService.atualizar(eq(999L), any(DespesaVariavelRequestDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/despesas-variaveis/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deveRejeitarAtualizarDespesaVariavelInvalida() throws Exception {
        String json = """
        {
            "descricao": "",
            "localCompra": "",
            "valorParcela": -10.00,
            "quantidadeParcelas": 0,
            "dataInicio": null,
            "conta": null,
            "categoria": null
        }
        """;

        mockMvc.perform(put("/api/despesas-variaveis/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deveRetornar400QuandoViolacaoIntegridadeAoAtualizar() throws Exception {
        String json = """
        {
            "descricao": "Notebook",
            "localCompra": "Kabum",
            "dataCompra": "2024-01-10",
            "valorParcela": 1200.00,
            "quantidadeParcelas": 4,
            "dataInicio": "2024-01-15",
            "conta": { "id": 999 },
            "categoria": { "id": 999 },
            "observacoes": "Obs"
        }
        """;

        when(despesaVariavelService.atualizar(eq(10L), any(DespesaVariavelRequestDTO.class)))
                .thenThrow(new DataIntegrityViolationException("FK"));

        mockMvc.perform(put("/api/despesas-variaveis/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de integridade relacional. Verifique os vínculos informados."));
    }

    @Test
    public void deveExcluirDespesaVariavelComSucesso() throws Exception {
        when(despesaVariavelService.excluir(10L)).thenReturn(true);

        mockMvc.perform(delete("/api/despesas-variaveis/10"))
                .andExpect(status().isNoContent());

        verify(despesaVariavelService).excluir(10L);
    }

    @Test
    public void deveRetornar404AoExcluirDespesaVariavelInexistente() throws Exception {
        when(despesaVariavelService.excluir(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/despesas-variaveis/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deveRetornar400AoExcluirDespesaVariavelComErroIntegridade() throws Exception {
        when(despesaVariavelService.excluir(10L)).thenThrow(new DataIntegrityViolationException("Erro"));

        mockMvc.perform(delete("/api/despesas-variaveis/10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro ao excluir o registro."));
    }
}
