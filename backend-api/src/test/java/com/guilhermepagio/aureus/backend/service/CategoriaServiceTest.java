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

import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.dto.CategoriaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.CategoriaResponseDTO;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    public void deveListarCategorias() {
        Categoria categoria = new Categoria(1L, "Alimentação", "Despesas do mês");
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));

        List<CategoriaResponseDTO> resultado = categoriaService.listar();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).id());
        assertEquals("Alimentação", resultado.get(0).descricao());
        assertEquals("Despesas do mês", resultado.get(0).observacoes());
        verify(categoriaRepository).findAll();
    }

    @Test
    public void deveBuscarCategoriaPorIdExistente() {
        Categoria categoria = new Categoria(1L, "Alimentação", "Despesas do mês");
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        Optional<CategoriaResponseDTO> resultado = categoriaService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().id());
        assertEquals("Alimentação", resultado.get().descricao());
    }

    @Test
    public void deveRetornarVazioAoBuscarCategoriaPorIdInexistente() {
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<CategoriaResponseDTO> resultado = categoriaService.buscarPorId(999L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    public void deveCriarCategoriaComSucesso() {
        CategoriaRequestDTO dto = new CategoriaRequestDTO("Moradia", "Contas de casa");
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria c = invocation.getArgument(0);
            c.setId(20L);
            return c;
        });

        CategoriaResponseDTO response = categoriaService.criar(dto);

        assertNotNull(response);
        assertEquals(20L, response.id());
        assertEquals("Moradia", response.descricao());
        assertEquals("Contas de casa", response.observacoes());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    public void deveAtualizarCategoriaExistente() {
        Categoria existente = new Categoria(1L, "Alimentação", "Antiga");
        CategoriaRequestDTO dto = new CategoriaRequestDTO("Supermercado", "Atualizada");
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoriaResponseDTO response = categoriaService.atualizar(1L, dto);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Supermercado", response.descricao());
        assertEquals("Atualizada", response.observacoes());
    }

    @Test
    public void deveLancarExcecaoAoAtualizarCategoriaInexistente() {
        CategoriaRequestDTO dto = new CategoriaRequestDTO("Nova", "Obs");
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
            com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException.class,
            () -> categoriaService.atualizar(999L, dto)
        );
    }

    @Test
    public void deveExcluirCategoriaExistente() {
        when(categoriaRepository.existsById(1L)).thenReturn(true);

        boolean excluido = categoriaService.excluir(1L);

        assertTrue(excluido);
        verify(categoriaRepository).deleteById(1L);
        verify(categoriaRepository).flush();
    }

    @Test
    public void deveRetornarFalsoAoExcluirCategoriaInexistente() {
        when(categoriaRepository.existsById(999L)).thenReturn(false);

        boolean excluido = categoriaService.excluir(999L);

        assertFalse(excluido);
    }
}
