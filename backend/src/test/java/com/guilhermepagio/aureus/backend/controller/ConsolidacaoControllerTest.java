package com.guilhermepagio.aureus.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;

import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorContaDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorCategoriaDTO;
import com.guilhermepagio.aureus.backend.service.ConsolidacaoService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(ConsolidacaoController.class)
public class ConsolidacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsolidacaoService consolidacaoService;

    @Test
    @WithMockUser(username = "user1")
    void deveRetornarConsolidacaoPorConta() throws Exception {
        when(consolidacaoService.calcularConsolidacaoPorConta("user1", "2024-01"))
            .thenReturn(new ConsolidacaoPorContaDTO(Collections.emptyList(), Collections.emptyList()));

        mockMvc.perform(get("/api/consolidacao/por-conta")
                .param("mesAno", "2024-01"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1")
    void deveRejeitarMesAnoInvalido() throws Exception {
        when(consolidacaoService.calcularConsolidacaoPorConta("user1", "2024-13"))
            .thenThrow(new IllegalArgumentException("mesAno inválido"));

        mockMvc.perform(get("/api/consolidacao/por-conta")
                .param("mesAno", "2024-13"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user1")
    void deveRetornarConsolidacaoPorCategoria() throws Exception {
        when(consolidacaoService.calcularConsolidacaoPorCategoria("user1", "2024-01"))
            .thenReturn(new ConsolidacaoPorCategoriaDTO(Collections.emptyList(), Collections.emptyList()));

        mockMvc.perform(get("/api/consolidacao/por-categoria")
                .param("mesAno", "2024-01"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1")
    void deveRejeitarMesAnoInvalidoParaCategoria() throws Exception {
        when(consolidacaoService.calcularConsolidacaoPorCategoria("user1", "2024-13"))
            .thenThrow(new IllegalArgumentException("mesAno inválido"));

        mockMvc.perform(get("/api/consolidacao/por-categoria")
                .param("mesAno", "2024-13"))
            .andExpect(status().isBadRequest());
    }
}
