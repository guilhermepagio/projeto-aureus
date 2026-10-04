package com.guilhermepagio.aureus.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.ReceitaFixa;
import com.guilhermepagio.aureus.backend.domain.dto.IdReferenceDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaFixaResponseDTO;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaFixaRepository;

@ExtendWith(MockitoExtension.class)
public class ReceitaFixaServiceTest {

    @Mock
    private ReceitaFixaRepository receitaFixaRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ReceitaFixaService receitaFixaService;

    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        com.guilhermepagio.aureus.backend.security.TenantContext.setTenantId("user1");
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        com.guilhermepagio.aureus.backend.security.TenantContext.clear();
    }

    @Test
    public void deveListarReceitasFixasOrdenadas() {
        Conta conta = new Conta(1L, "Nubank", "Principal");
        Categoria categoria = new Categoria(2L, "Trabalho", "Salário");
        ReceitaFixa receita = new ReceitaFixa(10L, "Salário", new BigDecimal("5000.00"), conta, categoria, "Obs", LocalDate.of(2024, 1, 1));

        when(receitaFixaRepository.findAllByOrderByDescricaoAsc()).thenReturn(List.of(receita));

        List<ReceitaFixaResponseDTO> resultado = receitaFixaService.listar();

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).id());
        assertEquals("Salário", resultado.get(0).descricao());
        assertEquals(new BigDecimal("5000.00"), resultado.get(0).valor());
        assertEquals(1L, resultado.get(0).conta().id());
        assertEquals(2L, resultado.get(0).categoria().id());
        verify(receitaFixaRepository).findAllByOrderByDescricaoAsc();
    }

    @Test
    public void deveBuscarReceitaFixaPorId() {
        Conta conta = new Conta(1L, "Nubank", "Principal");
        Categoria categoria = new Categoria(2L, "Trabalho", "Salário");
        ReceitaFixa receita = new ReceitaFixa(10L, "Salário", new BigDecimal("5000.00"), conta, categoria, "Obs", LocalDate.of(2024, 1, 1));

        when(receitaFixaRepository.findById(10L)).thenReturn(Optional.of(receita));

        Optional<ReceitaFixaResponseDTO> resultado = receitaFixaService.buscarPorId(10L);

        assertTrue(resultado.isPresent());
        assertEquals(10L, resultado.get().id());
        assertEquals("Salário", resultado.get().descricao());
    }

    @Test
    public void deveCriarReceitaFixaComSucesso() {
        ReceitaFixaRequestDTO dto = new ReceitaFixaRequestDTO(
            "Salário",
            new BigDecimal("5000.00"),
            new IdReferenceDTO(1L),
            new IdReferenceDTO(2L),
            "Mensal",
            LocalDate.of(2024, 1, 1)
        );

        Conta conta = new Conta(1L, "Nubank", "Principal");
        Categoria categoria = new Categoria(2L, "Trabalho", "Salário");
        ReceitaFixa salva = new ReceitaFixa(100L, "Salário", new BigDecimal("5000.00"), conta, categoria, "Mensal", LocalDate.of(2024, 1, 1));

        when(contaRepository.findOwnerUsuarioId(1L)).thenReturn(Optional.of("user1"));
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findOwnerUsuarioId(2L)).thenReturn(Optional.of("user1"));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
        when(receitaFixaRepository.saveAndFlush(any(ReceitaFixa.class))).thenAnswer(invocation -> {
            ReceitaFixa r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        ReceitaFixaResponseDTO response = receitaFixaService.criar(dto);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("Salário", response.descricao());
        assertEquals(new BigDecimal("5000.00"), response.valor());
        assertEquals(1L, response.conta().id());
        assertEquals("Nubank", response.conta().descricao());
        assertEquals(2L, response.categoria().id());
        assertEquals("Trabalho", response.categoria().descricao());
        assertEquals("Mensal", response.observacoes());
        assertEquals(LocalDate.of(2024, 1, 1), response.dataInicio());
    }

    @Test
    public void deveLancarExcecaoAoCriarComContaInexistente() {
        ReceitaFixaRequestDTO dto = new ReceitaFixaRequestDTO(
            "Salário",
            new BigDecimal("5000.00"),
            new IdReferenceDTO(999L),
            new IdReferenceDTO(2L),
            "Obs",
            LocalDate.of(2024, 1, 1)
        );

        when(contaRepository.findOwnerUsuarioId(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> receitaFixaService.criar(dto));
    }

    @Test
    public void deveLancarExcecaoAoCriarComCategoriaInexistente() {
        ReceitaFixaRequestDTO dto = new ReceitaFixaRequestDTO(
            "Salário",
            new BigDecimal("5000.00"),
            new IdReferenceDTO(1L),
            new IdReferenceDTO(999L),
            "Obs",
            LocalDate.of(2024, 1, 1)
        );

        Conta conta = new Conta(1L, "Nubank", "Principal");
        when(contaRepository.findOwnerUsuarioId(1L)).thenReturn(Optional.of("user1"));
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findOwnerUsuarioId(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> receitaFixaService.criar(dto));
    }

    @Test
    public void deveAtualizarReceitaFixaComSucesso() {
        Conta contaAntiga = new Conta(1L, "Nubank", "Principal");
        Categoria categoriaAntiga = new Categoria(2L, "Trabalho", "Salário");
        ReceitaFixa existente = new ReceitaFixa(100L, "Salário", new BigDecimal("5000.00"), contaAntiga, categoriaAntiga, "Obs", LocalDate.of(2024, 1, 1));

        Conta contaNova = new Conta(3L, "Inter", "Secundária");
        Categoria categoriaNova = new Categoria(4L, "Renda Extra", "Freelance");
        ReceitaFixaRequestDTO dto = new ReceitaFixaRequestDTO(
            "Consultoria",
            new BigDecimal("6000.00"),
            new IdReferenceDTO(3L),
            new IdReferenceDTO(4L),
            "Novo contrato",
            LocalDate.of(2024, 2, 1)
        );

        when(receitaFixaRepository.findById(100L)).thenReturn(Optional.of(existente));
        when(contaRepository.findOwnerUsuarioId(3L)).thenReturn(Optional.of("user1"));
        when(contaRepository.findById(3L)).thenReturn(Optional.of(contaNova));
        when(categoriaRepository.findOwnerUsuarioId(4L)).thenReturn(Optional.of("user1"));
        when(categoriaRepository.findById(4L)).thenReturn(Optional.of(categoriaNova));
        when(receitaFixaRepository.saveAndFlush(any(ReceitaFixa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReceitaFixaResponseDTO response = receitaFixaService.atualizar(100L, dto);

        assertNotNull(response);
        assertEquals("Consultoria", response.descricao());
        assertEquals(new BigDecimal("6000.00"), response.valor());
        assertEquals(3L, response.conta().id());
        assertEquals(4L, response.categoria().id());
        assertEquals("Novo contrato", response.observacoes());
        assertEquals(LocalDate.of(2024, 2, 1), response.dataInicio());
    }

    @Test
    public void deveLancarExcecaoAoAtualizarReceitaFixaInexistente() {
        ReceitaFixaRequestDTO dto = new ReceitaFixaRequestDTO(
            "Inexistente",
            new BigDecimal("100.00"),
            new IdReferenceDTO(1L),
            new IdReferenceDTO(2L),
            "Obs",
            LocalDate.of(2024, 1, 1)
        );

        when(receitaFixaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> receitaFixaService.atualizar(999L, dto));
    }

    @Test
    public void deveLancarExcecaoAoAtualizarComContaOuCategoriaInexistente() {
        ReceitaFixa existente = new ReceitaFixa(100L, "Salário", new BigDecimal("5000.00"), null, null, "Obs", LocalDate.of(2024, 1, 1));
        ReceitaFixaRequestDTO dto = new ReceitaFixaRequestDTO(
            "Salário",
            new BigDecimal("5000.00"),
            new IdReferenceDTO(999L),
            new IdReferenceDTO(2L),
            "Obs",
            LocalDate.of(2024, 1, 1)
        );

        when(receitaFixaRepository.findById(100L)).thenReturn(Optional.of(existente));
        when(contaRepository.findOwnerUsuarioId(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> receitaFixaService.atualizar(100L, dto));
    }

    @Test
    public void deveExcluirReceitaFixaExistente() {
        when(receitaFixaRepository.existsById(10L)).thenReturn(true);

        boolean excluido = receitaFixaService.excluir(10L);

        assertTrue(excluido);
        verify(receitaFixaRepository).deleteById(10L);
        verify(receitaFixaRepository).flush();
    }

    @Test
    public void deveRetornarFalsoAoExcluirReceitaFixaInexistente() {
        when(receitaFixaRepository.existsById(999L)).thenReturn(false);

        boolean excluido = receitaFixaService.excluir(999L);

        assertFalse(excluido);
    }
}
