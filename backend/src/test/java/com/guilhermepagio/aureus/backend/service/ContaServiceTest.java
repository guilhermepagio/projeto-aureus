package com.guilhermepagio.aureus.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.dto.ContaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ContaResponseDTO;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;

@ExtendWith(MockitoExtension.class)
public class ContaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @InjectMocks
    private ContaService contaService;

    @Test
    public void deveListarContas() {
        Conta conta = new Conta(1L, "Nubank", "Conta principal");
        when(contaRepository.findAll()).thenReturn(List.of(conta));

        List<ContaResponseDTO> resultado = contaService.listar();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).id());
        assertEquals("Nubank", resultado.get(0).descricao());
        assertEquals("Conta principal", resultado.get(0).observacoes());
        verify(contaRepository).findAll();
    }

    @Test
    public void deveBuscarContaPorIdExistente() {
        Conta conta = new Conta(1L, "Nubank", "Conta principal");
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));

        Optional<ContaResponseDTO> resultado = contaService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().id());
        assertEquals("Nubank", resultado.get().descricao());
    }

    @Test
    public void deveRetornarVazioAoBuscarContaPorIdInexistente() {
        when(contaRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<ContaResponseDTO> resultado = contaService.buscarPorId(999L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    public void deveCriarContaComSucesso() {
        ContaRequestDTO dto = new ContaRequestDTO("Nubank", "Principal");
        when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> {
            Conta c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });

        ContaResponseDTO response = contaService.criar(dto);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Nubank", response.descricao());
        assertEquals("Principal", response.observacoes());
        verify(contaRepository).save(any(Conta.class));
    }

    @Test
    public void deveAtualizarContaExistente() {
        Conta existente = new Conta(1L, "Nubank", "Antiga");
        ContaRequestDTO dto = new ContaRequestDTO("Nubank PJ", "Atualizada");
        when(contaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<ContaResponseDTO> response = contaService.atualizar(1L, dto);

        assertTrue(response.isPresent());
        assertEquals(1L, response.get().id());
        assertEquals("Nubank PJ", response.get().descricao());
        assertEquals("Atualizada", response.get().observacoes());
    }

    @Test
    public void deveRetornarVazioAoAtualizarContaInexistente() {
        ContaRequestDTO dto = new ContaRequestDTO("Nova", "Obs");
        when(contaRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<ContaResponseDTO> response = contaService.atualizar(999L, dto);

        assertTrue(response.isEmpty());
    }

    @Test
    public void deveExcluirContaExistente() {
        when(contaRepository.existsById(1L)).thenReturn(true);

        boolean excluido = contaService.excluir(1L);

        assertTrue(excluido);
        verify(contaRepository).deleteById(1L);
        verify(contaRepository).flush();
    }

    @Test
    public void deveRetornarFalsoAoExcluirContaInexistente() {
        when(contaRepository.existsById(999L)).thenReturn(false);

        boolean excluido = contaService.excluir(999L);

        assertFalse(excluido);
    }
}
