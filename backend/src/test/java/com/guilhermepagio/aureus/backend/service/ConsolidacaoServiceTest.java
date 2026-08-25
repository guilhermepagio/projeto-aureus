package com.guilhermepagio.aureus.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.DespesaFixa;
import com.guilhermepagio.aureus.backend.domain.DespesaVariavel;
import com.guilhermepagio.aureus.backend.domain.ReceitaFixa;
import com.guilhermepagio.aureus.backend.domain.ReceitaVariavel;
import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorContaDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorCategoriaDTO;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaFixaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaVariavelRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaFixaRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaVariavelRepository;

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
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProjecao() {
        Conta conta = new Conta();
        conta.setId(1L);
        conta.setDescricao("Conta 1");
        
        when(contaRepository.findByUsuarioId("user1")).thenReturn(Collections.singletonList(conta));
        
        DespesaFixa despesa = new DespesaFixa();
        despesa.setConta(conta);
        despesa.setDataInicio(LocalDate.of(2024, 1, 1));
        despesa.setValor(BigDecimal.valueOf(100));
        
        when(despesaFixaRepository.findByUsuarioId("user1")).thenReturn(Collections.singletonList(despesa));
        
        ConsolidacaoPorContaDTO dto = consolidacaoService.calcularConsolidacaoPorConta("user1", "2024-01");
        
        assertEquals(1, dto.getReceitas().size());
        assertEquals(1, dto.getDespesas().size());
    }

    @Test
    void testProjecaoCategoria() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setDescricao("Cat 1");
        
        when(categoriaRepository.findByUsuarioId("user1")).thenReturn(Collections.singletonList(categoria));
        
        ReceitaVariavel rv = new ReceitaVariavel();
        rv.setId(10L);
        rv.setCategoria(categoria);
        rv.setDataInicio(LocalDate.of(2024, 1, 1));
        rv.setQuantidadeParcelas(5);
        rv.setValorParcela(BigDecimal.valueOf(100));
        
        DespesaFixa df = new DespesaFixa();
        df.setId(20L);
        df.setCategoria(null); // Sem categoria
        df.setDataInicio(LocalDate.of(2024, 2, 1));
        df.setValor(BigDecimal.valueOf(50));
        
        when(receitaVariavelRepository.findByUsuarioId("user1")).thenReturn(List.of(rv));
        when(despesaFixaRepository.findByUsuarioId("user1")).thenReturn(List.of(df));
        
        ConsolidacaoPorCategoriaDTO dto = consolidacaoService.calcularConsolidacaoPorCategoria("user1", "2024-01");
        
        assertEquals(2, dto.getReceitas().size()); // 1 cat + "sem categoria"
        ConsolidacaoPorCategoriaDTO.LinhaConsolidacaoCategoriaDTO receita = dto.getReceitas().stream().filter(r -> r.getCategoriaId().equals(1L)).findFirst().get();
        assertEquals(0, BigDecimal.valueOf(100).compareTo(receita.getValoresMensais().get(0)));
        
        ConsolidacaoPorCategoriaDTO.LinhaConsolidacaoCategoriaDTO despesaSemCat = dto.getDespesas().stream().filter(d -> d.getCategoriaId().equals(ConsolidacaoService.SEM_CATEGORIA_ID)).findFirst().get();
        assertEquals(0, BigDecimal.valueOf(50).compareTo(despesaSemCat.getValoresMensais().get(1)));
    }
}
