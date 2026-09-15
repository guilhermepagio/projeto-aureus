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
import com.guilhermepagio.aureus.backend.domain.dto.ContaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ContaResponseDTO;
import com.guilhermepagio.aureus.backend.service.ContaService;

@ExtendWith(MockitoExtension.class)
public class ContaControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ContaService contaService;

    @InjectMocks
    private ContaController contaController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(contaController).build();
    }

    @Test
    public void deveListarContas() throws Exception {
        when(contaService.listar()).thenReturn(List.of(new ContaResponseDTO(1L, "Nubank", "Principal")));

        mockMvc.perform(get("/api/contas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].descricao").value("Nubank"))
                .andExpect(jsonPath("$[0].observacoes").value("Principal"));
    }

    @Test
    public void deveCriarContaValida() throws Exception {
        ContaRequestDTO request = new ContaRequestDTO("Nubank", "Principal");
        ContaResponseDTO response = new ContaResponseDTO(1L, "Nubank", "Principal");
        when(contaService.criar(any(ContaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/contas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.descricao").value("Nubank"));
    }

    @Test
    public void deveRejeitarCriarContaInvalida() throws Exception {
        String json = "{\"descricao\":\"\",\"observacoes\":\"\"}";

        mockMvc.perform(post("/api/contas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deveAtualizarContaExistente() throws Exception {
        ContaRequestDTO request = new ContaRequestDTO("Nubank PJ", "Atualizada");
        ContaResponseDTO response = new ContaResponseDTO(1L, "Nubank PJ", "Atualizada");
        when(contaService.atualizar(eq(1L), any(ContaRequestDTO.class))).thenReturn(Optional.of(response));

        mockMvc.perform(put("/api/contas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Nubank PJ"));
    }

    @Test
    public void deveRetornar404AoAtualizarContaInexistente() throws Exception {
        ContaRequestDTO request = new ContaRequestDTO("Nova", "");
        when(contaService.atualizar(eq(999L), any(ContaRequestDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/contas/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deveExcluirContaComSucesso() throws Exception {
        when(contaService.excluir(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/contas/1"))
                .andExpect(status().isNoContent());

        verify(contaService).excluir(1L);
    }

    @Test
    public void deveRetornar404AoExcluirContaInexistente() throws Exception {
        when(contaService.excluir(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/contas/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deveRetornar400AoExcluirContaComVinculos() throws Exception {
        when(contaService.excluir(1L)).thenThrow(new DataIntegrityViolationException("FK"));

        mockMvc.perform(delete("/api/contas/1"))
                .andExpect(status().isBadRequest());
    }
}
