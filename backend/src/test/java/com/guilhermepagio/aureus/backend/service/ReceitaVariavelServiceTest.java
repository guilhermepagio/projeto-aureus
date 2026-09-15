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

import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.ReceitaVariavel;
import com.guilhermepagio.aureus.backend.domain.dto.IdReferenceDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaVariavelResponseDTO;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaVariavelRepository;

@ExtendWith(MockitoExtension.class)
public class ReceitaVariavelServiceTest {

    @Mock
    private ReceitaVariavelRepository receitaVariavelRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ReceitaVariavelService receitaVariavelService;

    @Test
    public void deveListarReceitasVariaveisOrdenadas() {
        Conta conta = new Conta(1L, "Nubank", "Principal");
        Categoria categoria = new Categoria(2L, "Renda Extra", "Freelance");
        ReceitaVariavel receita = new ReceitaVariavel(
            10L, "Projeto Web", new BigDecimal("1500.00"), 3,
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 1),
            categoria, conta, "Observação"
        );

        when(receitaVariavelRepository.findAllByOrderByDescricaoAsc()).thenReturn(List.of(receita));

        List<ReceitaVariavelResponseDTO> resultado = receitaVariavelService.listar();

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).id());
        assertEquals("Projeto Web", resultado.get(0).descricao());
        assertEquals(new BigDecimal("1500.00"), resultado.get(0).valorParcela());
        assertEquals(LocalDate.of(2024, 1, 1), resultado.get(0).dataInicio());
        assertEquals(LocalDate.of(2024, 3, 1), resultado.get(0).dataFim());
        verify(receitaVariavelRepository).findAllByOrderByDescricaoAsc();
    }

    @Test
    public void deveBuscarReceitaVariavelPorId() {
        Conta conta = new Conta(1L, "Nubank", "Principal");
        Categoria categoria = new Categoria(2L, "Renda Extra", "Freelance");
        ReceitaVariavel receita = new ReceitaVariavel(
            10L, "Projeto Web", new BigDecimal("1500.00"), 3,
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 1),
            categoria, conta, "Observação"
        );

        when(receitaVariavelRepository.findById(10L)).thenReturn(Optional.of(receita));

        Optional<ReceitaVariavelResponseDTO> resultado = receitaVariavelService.buscarPorId(10L);

        assertTrue(resultado.isPresent());
        assertEquals(10L, resultado.get().id());
        assertEquals("Projeto Web", resultado.get().descricao());
    }

    @Test
    public void deveCriarReceitaVariavelECalcularDataFimParcelaUnica() {
        // Cenário da matriz: quantidadeParcelas: 1, dataInicio: "2024-05-10"
        // Esperado: dataInicio: "2024-05-01", dataFim: "2024-05-01"
        ReceitaVariavelRequestDTO dto = new ReceitaVariavelRequestDTO(
            "Venda Celular",
            new BigDecimal("1200.00"),
            1,
            LocalDate.of(2024, 5, 10),
            new IdReferenceDTO(1L),
            new IdReferenceDTO(2L),
            "Pagamento à vista"
        );

        Conta conta = new Conta(1L, "Nubank", "Principal");
        Categoria categoria = new Categoria(2L, "Vendas", "Bens");

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
        when(receitaVariavelRepository.saveAndFlush(any(ReceitaVariavel.class))).thenAnswer(invocation -> {
            ReceitaVariavel r = invocation.getArgument(0);
            r.setId(50L);
            return r;
        });

        ReceitaVariavelResponseDTO response = receitaVariavelService.criar(dto);

        assertNotNull(response);
        assertEquals(50L, response.id());
        assertEquals("Venda Celular", response.descricao());
        assertEquals(LocalDate.of(2024, 5, 1), response.dataInicio());
        assertEquals(LocalDate.of(2024, 5, 1), response.dataFim());
        assertEquals(1L, response.conta().id());
        assertEquals(2L, response.categoria().id());
        assertEquals(new BigDecimal("1200.00"), response.valorParcela());
        assertEquals(1, response.quantidadeParcelas());
        assertEquals("Pagamento à vista", response.observacoes());
    }

    @Test
    public void deveCriarReceitaVariavelMultiplasParcelas() {
        ReceitaVariavelRequestDTO dto = new ReceitaVariavelRequestDTO(
            "Consultoria",
            new BigDecimal("2000.00"),
            3,
            LocalDate.of(2024, 1, 15),
            new IdReferenceDTO(1L),
            new IdReferenceDTO(2L),
            "3 meses"
        );

        Conta conta = new Conta(1L, "Nubank", "Principal");
        Categoria categoria = new Categoria(2L, "Renda", "Extra");

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
        when(receitaVariavelRepository.saveAndFlush(any(ReceitaVariavel.class))).thenAnswer(invocation -> {
            ReceitaVariavel r = invocation.getArgument(0);
            r.setId(51L);
            return r;
        });

        ReceitaVariavelResponseDTO response = receitaVariavelService.criar(dto);

        assertNotNull(response);
        assertEquals(LocalDate.of(2024, 1, 1), response.dataInicio());
        assertEquals(LocalDate.of(2024, 3, 1), response.dataFim());
    }

    @Test
    public void deveLancarExcecaoAoCriarReceitaVariavelComContaInexistente() {
        ReceitaVariavelRequestDTO dto = new ReceitaVariavelRequestDTO(
            "Renda", new BigDecimal("100.00"), 1, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(999L), new IdReferenceDTO(2L), null
        );

        when(contaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(DataIntegrityViolationException.class, () -> receitaVariavelService.criar(dto));
    }

    @Test
    public void deveLancarExcecaoAoCriarReceitaVariavelComCategoriaInexistente() {
        ReceitaVariavelRequestDTO dto = new ReceitaVariavelRequestDTO(
            "Renda", new BigDecimal("100.00"), 1, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(1L), new IdReferenceDTO(999L), null
        );

        Conta conta = new Conta(1L, "Nubank", "Principal");
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(DataIntegrityViolationException.class, () -> receitaVariavelService.criar(dto));
    }

    @Test
    public void deveAtualizarReceitaVariavelECalcularDataFim() {
        Conta contaAntiga = new Conta(1L, "Nubank", "Principal");
        Categoria categoriaAntiga = new Categoria(2L, "Renda Extra", "Freelance");
        ReceitaVariavel existente = new ReceitaVariavel(
            50L, "Projeto", new BigDecimal("1000.00"), 2,
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1),
            categoriaAntiga, contaAntiga, "Obs"
        );

        Conta contaNova = new Conta(3L, "Inter", "Secundária");
        Categoria categoriaNova = new Categoria(4L, "Consultoria", "Tech");
        ReceitaVariavelRequestDTO dto = new ReceitaVariavelRequestDTO(
            "Projeto Renovado",
            new BigDecimal("1500.00"),
            4,
            LocalDate.of(2024, 3, 20),
            new IdReferenceDTO(3L),
            new IdReferenceDTO(4L),
            "Aditivo contratual"
        );

        when(receitaVariavelRepository.findById(50L)).thenReturn(Optional.of(existente));
        when(contaRepository.findById(3L)).thenReturn(Optional.of(contaNova));
        when(categoriaRepository.findById(4L)).thenReturn(Optional.of(categoriaNova));
        when(receitaVariavelRepository.saveAndFlush(any(ReceitaVariavel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<ReceitaVariavelResponseDTO> response = receitaVariavelService.atualizar(50L, dto);

        assertTrue(response.isPresent());
        assertEquals("Projeto Renovado", response.get().descricao());
        assertEquals(LocalDate.of(2024, 3, 1), response.get().dataInicio());
        assertEquals(LocalDate.of(2024, 6, 1), response.get().dataFim()); // 2024-03-01 + (4 - 1) = 2024-06-01
        assertEquals(3L, response.get().conta().id());
        assertEquals(4L, response.get().categoria().id());
        assertEquals(new BigDecimal("1500.00"), response.get().valorParcela());
        assertEquals(4, response.get().quantidadeParcelas());
        assertEquals("Aditivo contratual", response.get().observacoes());
    }

    @Test
    public void deveRetornarVazioAoAtualizarReceitaVariavelInexistente() {
        ReceitaVariavelRequestDTO dto = new ReceitaVariavelRequestDTO(
            "Renda", new BigDecimal("100.00"), 1, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(1L), new IdReferenceDTO(2L), null
        );

        when(receitaVariavelRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<ReceitaVariavelResponseDTO> response = receitaVariavelService.atualizar(999L, dto);

        assertTrue(response.isEmpty());
    }

    @Test
    public void deveExcluirReceitaVariavelExistente() {
        when(receitaVariavelRepository.existsById(10L)).thenReturn(true);

        boolean excluido = receitaVariavelService.excluir(10L);

        assertTrue(excluido);
        verify(receitaVariavelRepository).deleteById(10L);
        verify(receitaVariavelRepository).flush();
    }

    @Test
    public void deveRetornarFalsoAoExcluirReceitaVariavelInexistente() {
        when(receitaVariavelRepository.existsById(999L)).thenReturn(false);

        boolean excluido = receitaVariavelService.excluir(999L);

        assertFalse(excluido);
    }
}
