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
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaFixaResponseDTO;
import com.guilhermepagio.aureus.backend.service.ReceitaFixaService;

@ExtendWith(MockitoExtension.class)
public class ReceitaFixaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReceitaFixaService receitaFixaService;

    @InjectMocks
    private ReceitaFixaController receitaFixaController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(receitaFixaController)
                .setControllerAdvice(new com.guilhermepagio.aureus.backend.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    public void deveListarReceitasFixas() throws Exception {
        ContaResponseDTO conta = new ContaResponseDTO(1L, "Nubank", "Principal");
        CategoriaResponseDTO categoria = new CategoriaResponseDTO(2L, "Trabalho", "Salário");
        ReceitaFixaResponseDTO response = new ReceitaFixaResponseDTO(10L, "Salário", new BigDecimal("5000.00"), conta, categoria, "Obs", LocalDate.of(2024, 1, 1));

        when(receitaFixaService.listar()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/receitas-fixas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].descricao").value("Salário"))
                .andExpect(jsonPath("$[0].conta.id").value(1L))
                .andExpect(jsonPath("$[0].categoria.id").value(2L));
    }

    @Test
    public void deveCriarReceitaFixaValida() throws Exception {
        String json = """
        {
            "descricao": "Salário",
            "valor": 5000.00,
            "conta": { "id": 1 },
            "categoria": { "id": 2 },
            "observacoes": "Obs",
            "dataInicio": "2024-01-01"
        }
        """;
        ContaResponseDTO conta = new ContaResponseDTO(1L, "Nubank", "Principal");
        CategoriaResponseDTO categoria = new CategoriaResponseDTO(2L, "Trabalho", "Salário");
        ReceitaFixaResponseDTO response = new ReceitaFixaResponseDTO(10L, "Salário", new BigDecimal("5000.00"), conta, categoria, "Obs", LocalDate.of(2024, 1, 1));

        when(receitaFixaService.criar(any(ReceitaFixaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/receitas-fixas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.descricao").value("Salário"));
    }

    @Test
    public void deveRejeitarCriarReceitaFixaInvalida() throws Exception {
        String json = "{\"descricao\":\"\",\"valor\":-10}";

        mockMvc.perform(post("/api/receitas-fixas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deveRetornar400QuandoViolacaoIntegridadeAoCriar() throws Exception {
        String json = """
        {
            "descricao": "Salário",
            "valor": 5000.00,
            "conta": { "id": 999 },
            "categoria": { "id": 2 },
            "observacoes": "Obs",
            "dataInicio": "2024-01-01"
        }
        """;

        when(receitaFixaService.criar(any(ReceitaFixaRequestDTO.class)))
                .thenThrow(new DataIntegrityViolationException("FK"));

        mockMvc.perform(post("/api/receitas-fixas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    public void deveAtualizarReceitaFixaExistente() throws Exception {
        String json = """
        {
            "descricao": "Salário",
            "valor": 5500.00,
            "conta": { "id": 1 },
            "categoria": { "id": 2 },
            "observacoes": "Obs",
            "dataInicio": "2024-01-01"
        }
        """;
        ContaResponseDTO conta = new ContaResponseDTO(1L, "Nubank", "Principal");
        CategoriaResponseDTO categoria = new CategoriaResponseDTO(2L, "Trabalho", "Salário");
        ReceitaFixaResponseDTO response = new ReceitaFixaResponseDTO(10L, "Salário", new BigDecimal("5500.00"), conta, categoria, "Obs", LocalDate.of(2024, 1, 1));

        when(receitaFixaService.atualizar(eq(10L), any(ReceitaFixaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/receitas-fixas/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(5500.00));
    }

    @Test
    public void deveRetornar404AoAtualizarReceitaFixaInexistente() throws Exception {
        String json = """
        {
            "descricao": "Salário",
            "valor": 5500.00,
            "conta": { "id": 1 },
            "categoria": { "id": 2 },
            "observacoes": "Obs",
            "dataInicio": "2024-01-01"
        }
        """;

        when(receitaFixaService.atualizar(eq(999L), any(ReceitaFixaRequestDTO.class)))
                .thenThrow(new com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException("Receita fixa não encontrada: 999"));

        mockMvc.perform(put("/api/receitas-fixas/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deveRejeitarAtualizarReceitaFixaInvalida() throws Exception {
        String json = """
        {
            "descricao": "",
            "valor": -10.00,
            "conta": null,
            "categoria": null,
            "dataInicio": null
        }
        """;

        mockMvc.perform(put("/api/receitas-fixas/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deveRetornar400QuandoViolacaoIntegridadeAoAtualizar() throws Exception {
        String json = """
        {
            "descricao": "Salário",
            "valor": 5500.00,
            "conta": { "id": 999 },
            "categoria": { "id": 999 },
            "observacoes": "Obs",
            "dataInicio": "2024-01-01"
        }
        """;

        when(receitaFixaService.atualizar(eq(10L), any(ReceitaFixaRequestDTO.class)))
                .thenThrow(new DataIntegrityViolationException("FK"));

        mockMvc.perform(put("/api/receitas-fixas/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    public void deveExcluirReceitaFixaComSucesso() throws Exception {
        when(receitaFixaService.excluir(10L)).thenReturn(true);

        mockMvc.perform(delete("/api/receitas-fixas/10"))
                .andExpect(status().isNoContent());

        verify(receitaFixaService).excluir(10L);
    }

    @Test
    public void deveRetornar404AoExcluirReceitaFixaInexistente() throws Exception {
        when(receitaFixaService.excluir(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/receitas-fixas/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deveRetornar400AoExcluirReceitaFixaEmUso() throws Exception {
        when(receitaFixaService.excluir(10L)).thenThrow(new DataIntegrityViolationException("Em uso"));

        mockMvc.perform(delete("/api/receitas-fixas/10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
