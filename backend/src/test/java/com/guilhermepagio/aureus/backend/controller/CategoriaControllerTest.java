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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guilhermepagio.aureus.backend.domain.dto.CategoriaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.CategoriaResponseDTO;
import com.guilhermepagio.aureus.backend.service.CategoriaService;

@ExtendWith(MockitoExtension.class)
public class CategoriaControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private CategoriaController categoriaController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoriaController)
                .setControllerAdvice(new com.guilhermepagio.aureus.backend.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    public void deveListarCategorias() throws Exception {
        when(categoriaService.listar()).thenReturn(List.of(new CategoriaResponseDTO(1L, "Alimentação", "Despesas do mês")));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].descricao").value("Alimentação"))
                .andExpect(jsonPath("$[0].observacoes").value("Despesas do mês"));
    }

    @Test
    public void deveCriarCategoriaValida() throws Exception {
        CategoriaRequestDTO request = new CategoriaRequestDTO("Alimentação", "Despesas do mês");
        CategoriaResponseDTO response = new CategoriaResponseDTO(1L, "Alimentação", "Despesas do mês");
        when(categoriaService.criar(any(CategoriaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.descricao").value("Alimentação"));
    }

    @Test
    public void deveRejeitarCriarCategoriaInvalida() throws Exception {
        String json = "{\"descricao\":\"\",\"observacoes\":\"\"}";

        mockMvc.perform(post("/api/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deveAtualizarCategoriaExistente() throws Exception {
        CategoriaRequestDTO request = new CategoriaRequestDTO("Mercado", "Atualizada");
        CategoriaResponseDTO response = new CategoriaResponseDTO(1L, "Mercado", "Atualizada");
        when(categoriaService.atualizar(eq(1L), any(CategoriaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/categorias/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Mercado"));
    }

    @Test
    public void deveRetornar404AoAtualizarCategoriaInexistente() throws Exception {
        CategoriaRequestDTO request = new CategoriaRequestDTO("Nova", "");
        when(categoriaService.atualizar(eq(999L), any(CategoriaRequestDTO.class)))
                .thenThrow(new com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException("Categoria não encontrada: 999"));

        mockMvc.perform(put("/api/categorias/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deveRejeitarAtualizarCategoriaInvalida() throws Exception {
        String json = "{\"descricao\":\"\",\"observacoes\":\"\"}";

        mockMvc.perform(put("/api/categorias/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deveExcluirCategoriaComSucesso() throws Exception {
        when(categoriaService.excluir(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isNoContent());

        verify(categoriaService).excluir(1L);
    }

    @Test
    public void deveRetornar404AoExcluirCategoriaInexistente() throws Exception {
        when(categoriaService.excluir(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/categorias/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deveRetornar400AoExcluirCategoriaComVinculos() throws Exception {
        when(categoriaService.excluir(1L)).thenThrow(new DataIntegrityViolationException("FK"));

        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isBadRequest());
    }
}
