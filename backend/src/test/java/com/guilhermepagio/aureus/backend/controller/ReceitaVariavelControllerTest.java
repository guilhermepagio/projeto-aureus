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
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaVariavelResponseDTO;
import com.guilhermepagio.aureus.backend.service.ReceitaVariavelService;

@ExtendWith(MockitoExtension.class)
public class ReceitaVariavelControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReceitaVariavelService receitaVariavelService;

    @InjectMocks
    private ReceitaVariavelController receitaVariavelController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(receitaVariavelController).build();
    }

    @Test
    public void deveListarReceitasVariaveis() throws Exception {
        ContaResponseDTO conta = new ContaResponseDTO(1L, "Nubank", "Principal");
        CategoriaResponseDTO categoria = new CategoriaResponseDTO(2L, "Renda Extra", "Freelance");
        ReceitaVariavelResponseDTO response = new ReceitaVariavelResponseDTO(
            10L, "Freelance", new BigDecimal("1500.00"), 3,
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 1),
            conta, categoria, "Obs"
        );

        when(receitaVariavelService.listar()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/receitas-variaveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].descricao").value("Freelance"))
                .andExpect(jsonPath("$[0].conta.id").value(1L))
                .andExpect(jsonPath("$[0].categoria.id").value(2L));
    }

    @Test
    public void deveCriarReceitaVariavelValida() throws Exception {
        String json = """
        {
            "descricao": "Freelance",
            "valorParcela": 1500.00,
            "quantidadeParcelas": 3,
            "dataInicio": "2024-01-15",
            "conta": { "id": 1 },
            "categoria": { "id": 2 },
            "observacoes": "Obs"
        }
        """;
        ContaResponseDTO conta = new ContaResponseDTO(1L, "Nubank", "Principal");
        CategoriaResponseDTO categoria = new CategoriaResponseDTO(2L, "Renda Extra", "Freelance");
        ReceitaVariavelResponseDTO response = new ReceitaVariavelResponseDTO(
            10L, "Freelance", new BigDecimal("1500.00"), 3,
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 1),
            conta, categoria, "Obs"
        );

        when(receitaVariavelService.criar(any(ReceitaVariavelRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/receitas-variaveis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.dataInicio").value("2024-01-01"))
                .andExpect(jsonPath("$.dataFim").value("2024-03-01"));
    }

    @Test
    public void deveRejeitarCriarReceitaVariavelInvalida() throws Exception {
        String json = "{\"descricao\":\"\",\"quantidadeParcelas\":0}";

        mockMvc.perform(post("/api/receitas-variaveis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deveRetornar400QuandoViolacaoIntegridadeAoCriar() throws Exception {
        String json = """
        {
            "descricao": "Freelance",
            "valorParcela": 1500.00,
            "quantidadeParcelas": 3,
            "dataInicio": "2024-01-15",
            "conta": { "id": 999 },
            "categoria": { "id": 2 },
            "observacoes": "Obs"
        }
        """;

        when(receitaVariavelService.criar(any(ReceitaVariavelRequestDTO.class)))
                .thenThrow(new DataIntegrityViolationException("FK"));

        mockMvc.perform(post("/api/receitas-variaveis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de integridade relacional. Verifique os vínculos informados."));
    }

    @Test
    public void deveAtualizarReceitaVariavelExistente() throws Exception {
        String json = """
        {
            "descricao": "Freelance Senior",
            "valorParcela": 2000.00,
            "quantidadeParcelas": 4,
            "dataInicio": "2024-01-15",
            "conta": { "id": 1 },
            "categoria": { "id": 2 },
            "observacoes": "Obs"
        }
        """;
        ContaResponseDTO conta = new ContaResponseDTO(1L, "Nubank", "Principal");
        CategoriaResponseDTO categoria = new CategoriaResponseDTO(2L, "Renda Extra", "Freelance");
        ReceitaVariavelResponseDTO response = new ReceitaVariavelResponseDTO(
            10L, "Freelance Senior", new BigDecimal("2000.00"), 4,
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 4, 1),
            conta, categoria, "Obs"
        );

        when(receitaVariavelService.atualizar(eq(10L), any(ReceitaVariavelRequestDTO.class))).thenReturn(Optional.of(response));

        mockMvc.perform(put("/api/receitas-variaveis/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorParcela").value(2000.00));
    }

    @Test
    public void deveRetornar404AoAtualizarReceitaVariavelInexistente() throws Exception {
        String json = """
        {
            "descricao": "Freelance Senior",
            "valorParcela": 2000.00,
            "quantidadeParcelas": 4,
            "dataInicio": "2024-01-15",
            "conta": { "id": 1 },
            "categoria": { "id": 2 },
            "observacoes": "Obs"
        }
        """;

        when(receitaVariavelService.atualizar(eq(999L), any(ReceitaVariavelRequestDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/receitas-variaveis/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deveRejeitarAtualizarReceitaVariavelInvalida() throws Exception {
        String json = """
        {
            "descricao": "",
            "valorParcela": -10.00,
            "quantidadeParcelas": 0,
            "dataInicio": null,
            "conta": null,
            "categoria": null
        }
        """;

        mockMvc.perform(put("/api/receitas-variaveis/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deveRetornar400QuandoViolacaoIntegridadeAoAtualizar() throws Exception {
        String json = """
        {
            "descricao": "Freelance",
            "valorParcela": 2000.00,
            "quantidadeParcelas": 4,
            "dataInicio": "2024-01-15",
            "conta": { "id": 999 },
            "categoria": { "id": 999 },
            "observacoes": "Obs"
        }
        """;

        when(receitaVariavelService.atualizar(eq(10L), any(ReceitaVariavelRequestDTO.class)))
                .thenThrow(new DataIntegrityViolationException("FK"));

        mockMvc.perform(put("/api/receitas-variaveis/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de integridade relacional. Verifique os vínculos informados."));
    }

    @Test
    public void deveExcluirReceitaVariavelComSucesso() throws Exception {
        when(receitaVariavelService.excluir(10L)).thenReturn(true);

        mockMvc.perform(delete("/api/receitas-variaveis/10"))
                .andExpect(status().isNoContent());

        verify(receitaVariavelService).excluir(10L);
    }

    @Test
    public void deveRetornar404AoExcluirReceitaVariavelInexistente() throws Exception {
        when(receitaVariavelService.excluir(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/receitas-variaveis/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deveRetornar400AoExcluirReceitaVariavelComErroIntegridade() throws Exception {
        when(receitaVariavelService.excluir(10L)).thenThrow(new DataIntegrityViolationException("Erro"));

        mockMvc.perform(delete("/api/receitas-variaveis/10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro ao excluir o registro."));
    }
}
