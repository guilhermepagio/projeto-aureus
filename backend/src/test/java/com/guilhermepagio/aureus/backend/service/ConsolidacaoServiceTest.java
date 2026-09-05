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
        
        when(categoriaRepository.findByUsuarioId("user1")).thenReturn(Collections.singletonList(categoria));
        
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
        
        when(despesaVariavelRepository.findByUsuarioId("user1")).thenReturn(List.of(dv));
        when(despesaFixaRepository.findByUsuarioId("user1")).thenReturn(List.of(df));
        
        ConsolidacaoPorCategoriaDTO dto = consolidacaoService.calcularConsolidacaoPorCategoria("user1", "2024-01");
        
        assertEquals(2, dto.getDespesas().size()); // 1 cat + "sem categoria" (since they both have data)
        
        Optional<LinhaConsolidacaoCategoriaDTO> despesaOpt = dto.getDespesas().stream().filter(r -> r.getCategoriaId().equals(1L)).findFirst();
        assertTrue(despesaOpt.isPresent());
        LinhaConsolidacaoCategoriaDTO despesa = despesaOpt.get();
        assertEquals(0, BigDecimal.valueOf(100).compareTo(despesa.getValoresMensais().get(0))); // Mês 1
        assertEquals(0, BigDecimal.valueOf(100).compareTo(despesa.getValoresMensais().get(4))); // Mês 5 (boundary)
        assertEquals(0, BigDecimal.ZERO.compareTo(despesa.getValoresMensais().get(5))); // Mês 6 (outside boundary)
        
        Optional<LinhaConsolidacaoCategoriaDTO> despesaSemCatOpt = dto.getDespesas().stream().filter(d -> d.getCategoriaId().equals(ConsolidacaoService.SEM_CATEGORIA_ID)).findFirst();
        assertTrue(despesaSemCatOpt.isPresent());
        LinhaConsolidacaoCategoriaDTO despesaSemCat = despesaSemCatOpt.get();
        assertEquals(0, BigDecimal.ZERO.compareTo(despesaSemCat.getValoresMensais().get(0)));
        assertEquals(0, BigDecimal.valueOf(50).compareTo(despesaSemCat.getValoresMensais().get(1)));
        assertEquals(0, BigDecimal.valueOf(50).compareTo(despesaSemCat.getValoresMensais().get(23)));
    }
}
