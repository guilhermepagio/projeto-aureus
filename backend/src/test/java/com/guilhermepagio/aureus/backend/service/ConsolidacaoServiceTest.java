package com.guilhermepagio.aureus.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.ReceitaFixa;
import com.guilhermepagio.aureus.backend.domain.DespesaVariavel;
import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorContaDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorContaDTO.LinhaConsolidacaoDTO;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaFixaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaVariavelRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaFixaRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaVariavelRepository;

@ExtendWith(MockitoExtension.class)
public class ConsolidacaoServiceTest {

    @Mock private ContaRepository contaRepository;
    @Mock private DespesaFixaRepository despesaFixaRepository;
    @Mock private ReceitaFixaRepository receitaFixaRepository;
    @Mock private DespesaVariavelRepository despesaVariavelRepository;
    @Mock private ReceitaVariavelRepository receitaVariavelRepository;

    @InjectMocks
    private ConsolidacaoService consolidacaoService;

    private Conta conta;
    
    @BeforeEach
    public void setup() {
        conta = new Conta();
        conta.setId(1L);
        conta.setDescricao("Conta Corrente");
    }

    @Test
    public void testProjecaoFinanceira24Meses() {
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
}
