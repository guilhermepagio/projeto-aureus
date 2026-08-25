package com.guilhermepagio.aureus.backend.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.DespesaFixa;
import com.guilhermepagio.aureus.backend.domain.DespesaVariavel;
import com.guilhermepagio.aureus.backend.domain.ReceitaFixa;
import com.guilhermepagio.aureus.backend.domain.ReceitaVariavel;
import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorContaDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorContaDTO.LinhaConsolidacaoDTO;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaFixaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaVariavelRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaFixaRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaVariavelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsolidacaoService {

    private final ContaRepository contaRepository;
    private final ReceitaFixaRepository receitaFixaRepository;
    private final ReceitaVariavelRepository receitaVariavelRepository;
    private final DespesaFixaRepository despesaFixaRepository;
    private final DespesaVariavelRepository despesaVariavelRepository;

    @Transactional(readOnly = true)
    public ConsolidacaoPorContaDTO calcularConsolidacaoPorConta(String usuarioId, String mesAno) {
        YearMonth startMonth = YearMonth.parse(mesAno);
        
        List<Conta> contas = contaRepository.findByUsuarioId(usuarioId);
        
        Map<Long, LinhaConsolidacaoDTO> receitasMap = new LinkedHashMap<>();
        Map<Long, LinhaConsolidacaoDTO> despesasMap = new LinkedHashMap<>();
        
        for (Conta c : contas) {
            receitasMap.put(c.getId(), criarLinha(c));
            despesasMap.put(c.getId(), criarLinha(c));
        }

        List<ReceitaFixa> receitasFixas = receitaFixaRepository.findByUsuarioId(usuarioId);
        List<ReceitaVariavel> receitasVariaveis = receitaVariavelRepository.findByUsuarioId(usuarioId);
        List<DespesaFixa> despesasFixas = despesaFixaRepository.findByUsuarioId(usuarioId);
        List<DespesaVariavel> despesasVariaveis = despesaVariavelRepository.findByUsuarioId(usuarioId);

        // Pré-calcular inícios para otimização
        Map<Long, YearMonth> inicioReceitaVariavel = new HashMap<>();
        Map<Long, YearMonth> fimReceitaVariavel = new HashMap<>();
        for (ReceitaVariavel rv : receitasVariaveis) {
            if (rv.getDataInicio() != null && rv.getQuantidadeParcelas() != null) {
                YearMonth inicio = YearMonth.from(rv.getDataInicio());
                inicioReceitaVariavel.put(rv.getId(), inicio);
                fimReceitaVariavel.put(rv.getId(), inicio.plusMonths(rv.getQuantidadeParcelas() - 1));
            }
        }
        
        Map<Long, YearMonth> inicioDespesaVariavel = new HashMap<>();
        Map<Long, YearMonth> fimDespesaVariavel = new HashMap<>();
        for (DespesaVariavel dv : despesasVariaveis) {
            if (dv.getDataInicio() != null && dv.getQuantidadeParcelas() != null) {
                YearMonth inicio = YearMonth.from(dv.getDataInicio());
                inicioDespesaVariavel.put(dv.getId(), inicio);
                fimDespesaVariavel.put(dv.getId(), inicio.plusMonths(dv.getQuantidadeParcelas() - 1));
            }
        }

        Map<Long, YearMonth> inicioReceitaFixa = new HashMap<>();
        for (ReceitaFixa rf : receitasFixas) {
            if (rf.getDataInicio() != null) {
                inicioReceitaFixa.put(rf.getId(), YearMonth.from(rf.getDataInicio()));
            }
        }

        Map<Long, YearMonth> inicioDespesaFixa = new HashMap<>();
        for (DespesaFixa df : despesasFixas) {
            if (df.getDataInicio() != null) {
                inicioDespesaFixa.put(df.getId(), YearMonth.from(df.getDataInicio()));
            }
        }

        for (int i = 0; i < 24; i++) {
            YearMonth currentMonth = startMonth.plusMonths(i);

            for (ReceitaFixa rf : receitasFixas) {
                if (rf.getConta() == null) continue;
                YearMonth inicio = inicioReceitaFixa.get(rf.getId());
                if (inicio == null || !inicio.isAfter(currentMonth)) {
                    somarValor(receitasMap, rf.getConta().getId(), i, rf.getValor());
                }
            }

            for (ReceitaVariavel rv : receitasVariaveis) {
                if (rv.getConta() == null) continue;
                YearMonth inicio = inicioReceitaVariavel.get(rv.getId());
                YearMonth fim = fimReceitaVariavel.get(rv.getId());
                if (inicio != null && fim != null && !currentMonth.isBefore(inicio) && !currentMonth.isAfter(fim)) {
                    somarValor(receitasMap, rv.getConta().getId(), i, rv.getValorParcela());
                }
            }

            for (DespesaFixa df : despesasFixas) {
                if (df.getConta() == null) continue;
                YearMonth inicio = inicioDespesaFixa.get(df.getId());
                if (inicio == null || !inicio.isAfter(currentMonth)) {
                    somarValor(despesasMap, df.getConta().getId(), i, df.getValor());
                }
            }

            for (DespesaVariavel dv : despesasVariaveis) {
                if (dv.getConta() == null) continue;
                YearMonth inicio = inicioDespesaVariavel.get(dv.getId());
                YearMonth fim = fimDespesaVariavel.get(dv.getId());
                if (inicio != null && fim != null && !currentMonth.isBefore(inicio) && !currentMonth.isAfter(fim)) {
                    somarValor(despesasMap, dv.getConta().getId(), i, dv.getValorParcela());
                }
            }
        }

        return new ConsolidacaoPorContaDTO(
            new ArrayList<>(receitasMap.values()),
            new ArrayList<>(despesasMap.values())
        );
    }

    private LinhaConsolidacaoDTO criarLinha(Conta conta) {
        List<BigDecimal> valores = new ArrayList<>(24);
        for (int i = 0; i < 24; i++) {
            valores.add(BigDecimal.ZERO);
        }
        return new LinhaConsolidacaoDTO(conta.getId(), conta.getDescricao(), valores);
    }

    private void somarValor(Map<Long, LinhaConsolidacaoDTO> map, Long contaId, int index, BigDecimal valor) {
        LinhaConsolidacaoDTO linha = map.get(contaId);
        if (linha != null && valor != null) {
            BigDecimal atual = linha.getValoresMensais().get(index);
            linha.getValoresMensais().set(index, atual.add(valor));
        }
    }
}
