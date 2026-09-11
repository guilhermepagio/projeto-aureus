package com.guilhermepagio.aureus.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.DespesaFixa;
import com.guilhermepagio.aureus.backend.domain.DespesaVariavel;
import com.guilhermepagio.aureus.backend.domain.ReceitaFixa;
import com.guilhermepagio.aureus.backend.domain.ReceitaVariavel;
import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorContaDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorContaDTO.LinhaConsolidacaoDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorCategoriaDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorCategoriaDTO.LinhaConsolidacaoCategoriaDTO;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaFixaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaVariavelRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaFixaRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaVariavelRepository;

@ExtendWith(MockitoExtension.class)
public class ConsolidacaoServiceTest {

    @InjectMocks
    private ConsolidacaoService consolidacaoService;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ReceitaFixaRepository receitaFixaRepository;

    @Mock
    private ReceitaVariavelRepository receitaVariavelRepository;

    @Mock
    private DespesaFixaRepository despesaFixaRepository;

    @Mock
    private DespesaVariavelRepository despesaVariavelRepository;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testProjecaoFinanceira24Meses() {
        Conta conta = new Conta();
        conta.setId(1L);
        conta.setDescricao("Conta Corrente");
        
        when(contaRepository.findByUsuarioId("user1")).thenReturn(Collections.singletonList(conta));
        
        ReceitaFixa rf = new ReceitaFixa();
        rf.setId(1L);
        rf.setConta(conta);
        rf.setValor(new BigDecimal("1000.00"));
        rf.setDataInicio(LocalDate.of(2024, 2, 10));
        when(receitaFixaRepository.findByUsuarioId("user1")).thenReturn(Collections.singletonList(rf));
        
        DespesaVariavel dv = new DespesaVariavel();
        dv.setId(1L);
        dv.setConta(conta);
        dv.setValorParcela(new BigDecimal("100.00"));
        dv.setDataInicio(LocalDate.of(2024, 1, 15));
        dv.setQuantidadeParcelas(2);
        when(despesaVariavelRepository.findByUsuarioId("user1")).thenReturn(Collections.singletonList(dv));
        
        when(despesaFixaRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
        when(receitaVariavelRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());

        ConsolidacaoPorContaDTO dto = consolidacaoService.calcularConsolidacaoPorConta("user1", "2024-01");
        
        LinhaConsolidacaoDTO receita = dto.getReceitas().get(0);
        assertEquals(0, BigDecimal.ZERO.compareTo(receita.getValoresMensais().get(0)));
        assertEquals(0, new BigDecimal("1000.00").compareTo(receita.getValoresMensais().get(1)));
        assertEquals(0, new BigDecimal("1000.00").compareTo(receita.getValoresMensais().get(23)));
        
        LinhaConsolidacaoDTO despesa = dto.getDespesas().get(0);
        assertEquals(0, new BigDecimal("100.00").compareTo(despesa.getValoresMensais().get(0)));
        assertEquals(0, new BigDecimal("100.00").compareTo(despesa.getValoresMensais().get(1)));
        assertEquals(0, BigDecimal.ZERO.compareTo(despesa.getValoresMensais().get(2)));
    }

    @Test
    void testProjecaoCategoria() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setDescricao("Cat 1");
        
        Categoria categoriaVazia = new Categoria();
        categoriaVazia.setId(2L);
        categoriaVazia.setDescricao("Vazia");
        
        when(categoriaRepository.findByUsuarioIdOrderByDescricaoAsc("user1")).thenReturn(List.of(categoria, categoriaVazia));
        
        DespesaVariavel dv = new DespesaVariavel();
        dv.setId(10L);
        dv.setCategoria(categoria);
        dv.setDataInicio(LocalDate.of(2024, 1, 1));
        dv.setQuantidadeParcelas(5);
        dv.setValorParcela(BigDecimal.valueOf(100));
        
        DespesaFixa df = new DespesaFixa();
        df.setId(20L);
        df.setCategoria(null); // Sem categoria
        df.setDataInicio(LocalDate.of(2024, 2, 1));
        df.setValor(BigDecimal.valueOf(50));
        
        DespesaFixa dfSemData = new DespesaFixa();
        dfSemData.setId(30L);
        dfSemData.setCategoria(categoria);
        dfSemData.setDataInicio(null); // Data nula incide em todos os meses
        dfSemData.setValor(BigDecimal.valueOf(20));
        
        when(despesaVariavelRepository.findByUsuarioId("user1")).thenReturn(List.of(dv));
        when(despesaFixaRepository.findByUsuarioId("user1")).thenReturn(List.of(df, dfSemData));
        
        ConsolidacaoPorCategoriaDTO dto = consolidacaoService.calcularConsolidacaoPorCategoria("user1", "2024-01");
        
        assertEquals(2, dto.getDespesas().size()); // 1 cat + "sem categoria" (since they both have data)
        
        Optional<LinhaConsolidacaoCategoriaDTO> despesaOpt = dto.getDespesas().stream().filter(r -> r.getCategoriaId().equals(1L)).findFirst();
        assertTrue(despesaOpt.isPresent());
        LinhaConsolidacaoCategoriaDTO despesa = despesaOpt.get();
        // Mês 1: 100 (dv) + 20 (dfSemData) = 120
        assertEquals(0, BigDecimal.valueOf(120).compareTo(despesa.getValoresMensais().get(0)));
        // Mês 5: 100 (dv) + 20 (dfSemData) = 120
        assertEquals(0, BigDecimal.valueOf(120).compareTo(despesa.getValoresMensais().get(4)));
        // Mês 6: 0 (dv) + 20 (dfSemData) = 20
        assertEquals(0, BigDecimal.valueOf(20).compareTo(despesa.getValoresMensais().get(5)));
        
        Optional<LinhaConsolidacaoCategoriaDTO> despesaSemCatOpt = dto.getDespesas().stream().filter(d -> d.getCategoriaId().equals(ConsolidacaoService.SEM_CATEGORIA_ID)).findFirst();
        assertTrue(despesaSemCatOpt.isPresent());
        LinhaConsolidacaoCategoriaDTO despesaSemCat = despesaSemCatOpt.get();
        assertEquals(0, BigDecimal.ZERO.compareTo(despesaSemCat.getValoresMensais().get(0)));
        assertEquals(0, BigDecimal.valueOf(50).compareTo(despesaSemCat.getValoresMensais().get(1)));
        assertEquals(0, BigDecimal.valueOf(50).compareTo(despesaSemCat.getValoresMensais().get(23)));
    }

    @Test
    void testCalcularConsolidacaoParametrosInvalidos() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            consolidacaoService.calcularConsolidacaoPorCategoria("user1", null);
        });
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            consolidacaoService.calcularConsolidacaoPorConta("user1", null);
        });
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            consolidacaoService.calcularConsolidacaoPorCategoria(null, "2024-01");
        });
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            consolidacaoService.calcularConsolidacaoPorConta(null, "2024-01");
        });
    }

    @Test
    void testSaldoHistoricoPreGradeSemHistorico() {
        Conta conta = new Conta();
        conta.setId(1L);
        when(contaRepository.findByUsuarioId("user1")).thenReturn(List.of(conta));

        ReceitaFixa rf = new ReceitaFixa();
        rf.setId(1L);
        rf.setConta(conta);
        rf.setDataInicio(LocalDate.of(2024, 5, 1));
        rf.setValor(new BigDecimal("500.00"));
        when(receitaFixaRepository.findByUsuarioId("user1")).thenReturn(List.of(rf));

        DespesaVariavel dv = new DespesaVariavel();
        dv.setId(2L);
        dv.setConta(conta);
        dv.setDataInicio(LocalDate.of(2024, 5, 1));
        dv.setQuantidadeParcelas(3);
        dv.setValorParcela(new BigDecimal("100.00"));
        when(despesaVariavelRepository.findByUsuarioId("user1")).thenReturn(List.of(dv));

        when(despesaFixaRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
        when(receitaVariavelRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());

        ConsolidacaoPorContaDTO dto = consolidacaoService.calcularConsolidacaoPorConta("user1", "2024-05");

        assertEquals(0, new BigDecimal("0.00").compareTo(dto.getSaldoHistoricoPreGrade()));
    }

    @Test
    void testSaldoHistoricoPreGradeComFixosEValoresVariaveis() {
        Conta conta = new Conta();
        conta.setId(1L);
        when(contaRepository.findByUsuarioId("user1")).thenReturn(List.of(conta));

        DespesaFixa df = new DespesaFixa();
        df.setId(1L);
        df.setConta(conta);
        df.setDataInicio(LocalDate.of(2024, 1, 1));
        df.setValor(new BigDecimal("50.00"));
        when(despesaFixaRepository.findByUsuarioId("user1")).thenReturn(List.of(df));

        ReceitaVariavel rv = new ReceitaVariavel();
        rv.setId(2L);
        rv.setConta(conta);
        rv.setDataInicio(LocalDate.of(2024, 1, 15));
        rv.setQuantidadeParcelas(3);
        rv.setValorParcela(new BigDecimal("100.00"));
        when(receitaVariavelRepository.findByUsuarioId("user1")).thenReturn(List.of(rv));

        when(receitaFixaRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
        when(despesaVariavelRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());

        ConsolidacaoPorContaDTO dto = consolidacaoService.calcularConsolidacaoPorConta("user1", "2024-03");

        assertEquals(0, new BigDecimal("100.00").compareTo(dto.getSaldoHistoricoPreGrade()));
    }

    @Test
    void testSaldoHistoricoPreGradeComReceitaFixaEDespesaVariavel() {
        Conta conta = new Conta();
        conta.setId(1L);
        when(contaRepository.findByUsuarioId("user1")).thenReturn(List.of(conta));

        ReceitaFixa rf = new ReceitaFixa();
        rf.setId(1L);
        rf.setConta(conta);
        rf.setDataInicio(LocalDate.of(2023, 11, 1));
        rf.setValor(new BigDecimal("200.00"));
        when(receitaFixaRepository.findByUsuarioId("user1")).thenReturn(List.of(rf));

        DespesaVariavel dv = new DespesaVariavel();
        dv.setId(2L);
        dv.setConta(conta);
        dv.setDataInicio(LocalDate.of(2023, 12, 1));
        dv.setQuantidadeParcelas(5);
        dv.setValorParcela(new BigDecimal("80.00"));
        when(despesaVariavelRepository.findByUsuarioId("user1")).thenReturn(List.of(dv));

        when(despesaFixaRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
        when(receitaVariavelRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());

        ConsolidacaoPorContaDTO dto = consolidacaoService.calcularConsolidacaoPorConta("user1", "2024-02");

        assertEquals(0, new BigDecimal("440.00").compareTo(dto.getSaldoHistoricoPreGrade()));
    }

    @Test
    void testSaldoHistoricoSemDataInicioNaoGeraRetroativo() {
        Conta conta = new Conta();
        conta.setId(1L);
        when(contaRepository.findByUsuarioId("user1")).thenReturn(List.of(conta));

        ReceitaFixa rfSemData = new ReceitaFixa();
        rfSemData.setId(1L);
        rfSemData.setConta(conta);
        rfSemData.setDataInicio(null);
        rfSemData.setValor(new BigDecimal("500.00"));
        when(receitaFixaRepository.findByUsuarioId("user1")).thenReturn(List.of(rfSemData));

        DespesaFixa dfSemData = new DespesaFixa();
        dfSemData.setId(2L);
        dfSemData.setConta(conta);
        dfSemData.setDataInicio(null);
        dfSemData.setValor(new BigDecimal("300.00"));
        when(despesaFixaRepository.findByUsuarioId("user1")).thenReturn(List.of(dfSemData));

        when(receitaVariavelRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
        when(despesaVariavelRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());

        ConsolidacaoPorContaDTO dto = consolidacaoService.calcularConsolidacaoPorConta("user1", "2024-01");

        assertEquals(0, new BigDecimal("0.00").compareTo(dto.getSaldoHistoricoPreGrade()));
    }

    @Test
    void testSaldoHistoricoPreGradeNegativo() {
        Conta conta = new Conta();
        conta.setId(1L);
        when(contaRepository.findByUsuarioId("user1")).thenReturn(List.of(conta));

        DespesaFixa df = new DespesaFixa();
        df.setId(1L);
        df.setConta(conta);
        df.setDataInicio(LocalDate.of(2023, 10, 1));
        df.setValor(new BigDecimal("200.00")); // 3 meses antes de 2024-01 = 600.00
        when(despesaFixaRepository.findByUsuarioId("user1")).thenReturn(List.of(df));

        ReceitaFixa rf = new ReceitaFixa();
        rf.setId(2L);
        rf.setConta(conta);
        rf.setDataInicio(LocalDate.of(2023, 11, 1));
        rf.setValor(new BigDecimal("100.00")); // 2 meses antes de 2024-01 = 200.00
        when(receitaFixaRepository.findByUsuarioId("user1")).thenReturn(List.of(rf));

        when(receitaVariavelRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
        when(despesaVariavelRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());

        ConsolidacaoPorContaDTO dto = consolidacaoService.calcularConsolidacaoPorConta("user1", "2024-01");

        // 200.00 (receitas) - 600.00 (despesas) = -400.00
        assertEquals(0, new BigDecimal("-400.00").compareTo(dto.getSaldoHistoricoPreGrade()));
    }

    @Test
    void testSaldoHistoricoPreGradeParcelasVariaveisLimitadasPelaQuantidade() {
        Conta conta = new Conta();
        conta.setId(1L);
        when(contaRepository.findByUsuarioId("user1")).thenReturn(List.of(conta));

        DespesaVariavel dv = new DespesaVariavel();
        dv.setId(1L);
        dv.setConta(conta);
        dv.setDataInicio(LocalDate.of(2024, 1, 1));
        dv.setQuantidadeParcelas(3);
        dv.setValorParcela(new BigDecimal("100.00"));
        when(despesaVariavelRepository.findByUsuarioId("user1")).thenReturn(List.of(dv));

        when(despesaFixaRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
        when(receitaFixaRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
        when(receitaVariavelRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());

        // 6 meses entre 2024-01 e 2024-07, mas limitado a 3 parcelas = 300.00 de despesas
        ConsolidacaoPorContaDTO dto = consolidacaoService.calcularConsolidacaoPorConta("user1", "2024-07");

        assertEquals(0, new BigDecimal("-300.00").compareTo(dto.getSaldoHistoricoPreGrade()));
    }

    @Test
    void testSaldoHistoricoPreGradeReceitasVariaveisLimitadasPelaQuantidade() {
        Conta conta = new Conta();
        conta.setId(1L);
        when(contaRepository.findByUsuarioId("user1")).thenReturn(List.of(conta));

        ReceitaVariavel rv = new ReceitaVariavel();
        rv.setId(1L);
        rv.setConta(conta);
        rv.setDataInicio(LocalDate.of(2024, 1, 1));
        rv.setQuantidadeParcelas(3);
        rv.setValorParcela(new BigDecimal("100.00"));
        when(receitaVariavelRepository.findByUsuarioId("user1")).thenReturn(List.of(rv));

        when(despesaFixaRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
        when(despesaVariavelRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
        when(receitaFixaRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());

        // 6 meses entre 2024-01 e 2024-07, mas limitado a 3 parcelas = 300.00 de receitas
        ConsolidacaoPorContaDTO dto = consolidacaoService.calcularConsolidacaoPorConta("user1", "2024-07");

        assertEquals(0, new BigDecimal("300.00").compareTo(dto.getSaldoHistoricoPreGrade()));
    }
}