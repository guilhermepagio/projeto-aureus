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
import com.guilhermepagio.aureus.backend.domain.DespesaVariavel;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelResponseDTO;
import com.guilhermepagio.aureus.backend.domain.dto.IdReferenceDTO;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaVariavelRepository;

@ExtendWith(MockitoExtension.class)
public class DespesaVariavelServiceTest {

    @Mock
    private DespesaVariavelRepository despesaVariavelRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private DespesaVariavelService despesaVariavelService;

    @Test
    public void deveListarDespesasVariaveisOrdenadas() {
        Conta conta = new Conta(1L, "Nubank", "Principal");
        Categoria categoria = new Categoria(2L, "Compras", "Gerais");
        DespesaVariavel despesa = new DespesaVariavel(
            10L, "Notebook", "Kabum", LocalDate.of(2024, 1, 10),
            new BigDecimal("1000.00"), 3, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 1),
            categoria, conta, "Observação"
        );

        when(despesaVariavelRepository.findAllByOrderByDescricaoAsc()).thenReturn(List.of(despesa));

        List<DespesaVariavelResponseDTO> resultado = despesaVariavelService.listar();

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).id());
        assertEquals("Notebook", resultado.get(0).descricao());
        assertEquals("Kabum", resultado.get(0).localCompra());
        assertEquals(LocalDate.of(2024, 1, 1), resultado.get(0).dataInicio());
        assertEquals(LocalDate.of(2024, 3, 1), resultado.get(0).dataFim());
        verify(despesaVariavelRepository).findAllByOrderByDescricaoAsc();
    }

    @Test
    public void deveBuscarDespesaVariavelPorId() {
        Conta conta = new Conta(1L, "Nubank", "Principal");
        Categoria categoria = new Categoria(2L, "Compras", "Gerais");
        DespesaVariavel despesa = new DespesaVariavel(
            10L, "Notebook", "Kabum", LocalDate.of(2024, 1, 10),
            new BigDecimal("1000.00"), 3, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 1),
            categoria, conta, "Observação"
        );

        when(despesaVariavelRepository.findById(10L)).thenReturn(Optional.of(despesa));

        Optional<DespesaVariavelResponseDTO> resultado = despesaVariavelService.buscarPorId(10L);

        assertTrue(resultado.isPresent());
        assertEquals(10L, resultado.get().id());
        assertEquals("Notebook", resultado.get().descricao());
    }

    @Test
    public void deveCriarDespesaVariavelECalcularDataInicioEDataFim() {
        // Cenário da matriz: quantidadeParcelas: 3, dataInicio: "2024-01-15"
        // Esperado: dataInicio: "2024-01-01", dataFim: "2024-03-01"
        DespesaVariavelRequestDTO dto = new DespesaVariavelRequestDTO(
            "Sofá",
            "Tok&Stok",
            LocalDate.of(2024, 1, 15),
            new BigDecimal("500.00"),
            3,
            LocalDate.of(2024, 1, 15),
            new IdReferenceDTO(1L),
            new IdReferenceDTO(2L),
            "Parcelado em 3x"
        );

        Conta conta = new Conta(1L, "Nubank", "Principal");
        Categoria categoria = new Categoria(2L, "Móveis", "Casa");

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
        when(despesaVariavelRepository.saveAndFlush(any(DespesaVariavel.class))).thenAnswer(invocation -> {
            DespesaVariavel d = invocation.getArgument(0);
            d.setId(100L);
            return d;
        });

        DespesaVariavelResponseDTO response = despesaVariavelService.criar(dto);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("Sofá", response.descricao());
        assertEquals(LocalDate.of(2024, 1, 1), response.dataInicio());
        assertEquals(LocalDate.of(2024, 3, 1), response.dataFim());
        assertEquals(1L, response.conta().id());
        assertEquals(2L, response.categoria().id());
        assertEquals(new BigDecimal("500.00"), response.valorParcela());
        assertEquals(3, response.quantidadeParcelas());
        assertEquals("Tok&Stok", response.localCompra());
        assertEquals(LocalDate.of(2024, 1, 15), response.dataCompra());
        assertEquals("Parcelado em 3x", response.observacoes());
    }

    @Test
    public void deveCriarDespesaVariavelComParcelaUnica() {
        // Cenário: quantidadeParcelas: 1, dataInicio: "2024-05-10" -> dataInicio: "2024-05-01", dataFim: "2024-05-01"
        DespesaVariavelRequestDTO dto = new DespesaVariavelRequestDTO(
            "Almoço",
            "Restaurante",
            LocalDate.of(2024, 5, 10),
            new BigDecimal("80.00"),
            1,
            LocalDate.of(2024, 5, 10),
            new IdReferenceDTO(1L),
            new IdReferenceDTO(2L),
            "À vista"
        );

        Conta conta = new Conta(1L, "Nubank", "Principal");
        Categoria categoria = new Categoria(2L, "Alimentação", "Refeição");

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
        when(despesaVariavelRepository.saveAndFlush(any(DespesaVariavel.class))).thenAnswer(invocation -> {
            DespesaVariavel d = invocation.getArgument(0);
            d.setId(101L);
            return d;
        });

        DespesaVariavelResponseDTO response = despesaVariavelService.criar(dto);

        assertNotNull(response);
        assertEquals(LocalDate.of(2024, 5, 1), response.dataInicio());
        assertEquals(LocalDate.of(2024, 5, 1), response.dataFim());
    }

    @Test
    public void deveLancarExcecaoAoCriarDespesaVariavelComContaInexistente() {
        DespesaVariavelRequestDTO dto = new DespesaVariavelRequestDTO(
            "Compra", null, null, new BigDecimal("100.00"), 1, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(999L), new IdReferenceDTO(2L), null
        );

        when(contaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(DataIntegrityViolationException.class, () -> despesaVariavelService.criar(dto));
    }

    @Test
    public void deveLancarExcecaoAoCriarDespesaVariavelComCategoriaInexistente() {
        DespesaVariavelRequestDTO dto = new DespesaVariavelRequestDTO(
            "Compra", null, null, new BigDecimal("100.00"), 1, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(1L), new IdReferenceDTO(999L), null
        );

        Conta conta = new Conta(1L, "Nubank", "Principal");
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(DataIntegrityViolationException.class, () -> despesaVariavelService.criar(dto));
    }

    @Test
    public void deveAtualizarDespesaVariavelECalcularDataFim() {
        Conta contaAntiga = new Conta(1L, "Nubank", "Principal");
        Categoria categoriaAntiga = new Categoria(2L, "Compras", "Gerais");
        DespesaVariavel existente = new DespesaVariavel(
            100L, "Notebook", "Kabum", LocalDate.of(2024, 1, 10),
            new BigDecimal("1000.00"), 2, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1),
            categoriaAntiga, contaAntiga, "Obs"
        );

        Conta contaNova = new Conta(3L, "Inter", "Secundária");
        Categoria categoriaNova = new Categoria(4L, "Informática", "Tech");
        DespesaVariavelRequestDTO dto = new DespesaVariavelRequestDTO(
            "Notebook Gamer",
            "Kabum Tech",
            LocalDate.of(2024, 1, 10),
            new BigDecimal("1200.00"),
            4,
            LocalDate.of(2024, 2, 20),
            new IdReferenceDTO(3L),
            new IdReferenceDTO(4L),
            "Reparcelado"
        );

        when(despesaVariavelRepository.findById(100L)).thenReturn(Optional.of(existente));
        when(contaRepository.findById(3L)).thenReturn(Optional.of(contaNova));
        when(categoriaRepository.findById(4L)).thenReturn(Optional.of(categoriaNova));
        when(despesaVariavelRepository.saveAndFlush(any(DespesaVariavel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<DespesaVariavelResponseDTO> response = despesaVariavelService.atualizar(100L, dto);

        assertTrue(response.isPresent());
        assertEquals("Notebook Gamer", response.get().descricao());
        assertEquals(LocalDate.of(2024, 2, 1), response.get().dataInicio());
        assertEquals(LocalDate.of(2024, 5, 1), response.get().dataFim()); // 2024-02-01 + (4 - 1) meses = 2024-05-01
        assertEquals(3L, response.get().conta().id());
        assertEquals(4L, response.get().categoria().id());
        assertEquals(new BigDecimal("1200.00"), response.get().valorParcela());
        assertEquals(4, response.get().quantidadeParcelas());
        assertEquals("Kabum Tech", response.get().localCompra());
        assertEquals("Reparcelado", response.get().observacoes());
    }

    @Test
    public void deveRetornarVazioAoAtualizarDespesaVariavelInexistente() {
        DespesaVariavelRequestDTO dto = new DespesaVariavelRequestDTO(
            "Compra", null, null, new BigDecimal("100.00"), 1, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(1L), new IdReferenceDTO(2L), null
        );

        when(despesaVariavelRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<DespesaVariavelResponseDTO> response = despesaVariavelService.atualizar(999L, dto);

        assertTrue(response.isEmpty());
    }

    @Test
    public void deveLancarExcecaoAoAtualizarComContaOuCategoriaInexistente() {
        DespesaVariavel existente = new DespesaVariavel(
            100L, "Notebook", "Kabum", LocalDate.of(2024, 1, 10),
            new BigDecimal("1500.00"), 3, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 1),
            null, null, "Obs"
        );
        DespesaVariavelRequestDTO dto = new DespesaVariavelRequestDTO(
            "Notebook", "Kabum", LocalDate.of(2024, 1, 10),
            new BigDecimal("1500.00"), 3, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(999L), new IdReferenceDTO(2L), "Obs"
        );

        when(despesaVariavelRepository.findById(100L)).thenReturn(Optional.of(existente));
        when(contaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(DataIntegrityViolationException.class, () -> despesaVariavelService.atualizar(100L, dto));
    }

    @Test
    public void deveExcluirDespesaVariavelExistente() {
        when(despesaVariavelRepository.existsById(10L)).thenReturn(true);

        boolean excluido = despesaVariavelService.excluir(10L);

        assertTrue(excluido);
        verify(despesaVariavelRepository).deleteById(10L);
        verify(despesaVariavelRepository).flush();
    }

    @Test
    public void deveRetornarFalsoAoExcluirDespesaVariavelInexistente() {
        when(despesaVariavelRepository.existsById(999L)).thenReturn(false);

        boolean excluido = despesaVariavelService.excluir(999L);

        assertFalse(excluido);
    }
}
