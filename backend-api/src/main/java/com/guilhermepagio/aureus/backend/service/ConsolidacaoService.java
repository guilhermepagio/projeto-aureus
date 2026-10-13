package com.guilhermepagio.aureus.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.DespesaFixa;
import com.guilhermepagio.aureus.backend.domain.DespesaVariavel;
import com.guilhermepagio.aureus.backend.domain.ReceitaFixa;
import com.guilhermepagio.aureus.backend.domain.ReceitaVariavel;
import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorCategoriaDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorCategoriaDTO.LinhaConsolidacaoCategoriaDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorContaDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorContaDTO.LinhaConsolidacaoDTO;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaFixaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaVariavelRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaFixaRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaVariavelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsolidacaoService {

    public static final Long SEM_CATEGORIA_ID = -1L;
    public static final Long SEM_CONTA_ID = -1L;

    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ReceitaFixaRepository receitaFixaRepository;
    private final ReceitaVariavelRepository receitaVariavelRepository;
    private final DespesaFixaRepository despesaFixaRepository;
    private final DespesaVariavelRepository despesaVariavelRepository;

    @Transactional(readOnly = true)
    public ConsolidacaoPorContaDTO calcularConsolidacaoPorConta(final String usuarioId, final String mesAno) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("usuarioId não pode ser nulo");
        }
        if (mesAno == null) {
            throw new IllegalArgumentException("mesAno não pode ser nulo");
        }
        final YearMonth startMonth = YearMonth.parse(mesAno);
        
        final List<Conta> contas = contaRepository.findByUsuarioId(usuarioId);
        
        final Map<Long, LinhaConsolidacaoDTO> receitasMap = new LinkedHashMap<>();
        final Map<Long, LinhaConsolidacaoDTO> despesasMap = new LinkedHashMap<>();
        
        for (final Conta c : contas) {
            receitasMap.put(c.getId(), criarLinha(c.getId(), c.getDescricao()));
            despesasMap.put(c.getId(), criarLinha(c.getId(), c.getDescricao()));
        }

        // Linha sintética para "Sem Conta"
        receitasMap.put(SEM_CONTA_ID, criarLinha(SEM_CONTA_ID, "Sem Conta"));
        despesasMap.put(SEM_CONTA_ID, criarLinha(SEM_CONTA_ID, "Sem Conta"));

        final List<ReceitaFixa> receitasFixas = receitaFixaRepository.findByUsuarioId(usuarioId);
        final List<ReceitaVariavel> receitasVariaveis = receitaVariavelRepository.findByUsuarioId(usuarioId);
        final List<DespesaFixa> despesasFixas = despesaFixaRepository.findByUsuarioId(usuarioId);
        final List<DespesaVariavel> despesasVariaveis = despesaVariavelRepository.findByUsuarioId(usuarioId);

        // Pré-calcular inícios para otimização
        final Map<Long, YearMonth> inicioReceitaVariavel = new HashMap<>();
        final Map<Long, YearMonth> fimReceitaVariavel = new HashMap<>();
        for (final ReceitaVariavel rv : receitasVariaveis) {
            if (rv.getDataInicio() != null && rv.getQuantidadeParcelas() != null) {
                final YearMonth inicio = YearMonth.from(rv.getDataInicio());
                inicioReceitaVariavel.put(rv.getId(), inicio);
                fimReceitaVariavel.put(rv.getId(), inicio.plusMonths(rv.getQuantidadeParcelas() - 1));
            }
        }
        
        final Map<Long, YearMonth> inicioDespesaVariavel = new HashMap<>();
        final Map<Long, YearMonth> fimDespesaVariavel = new HashMap<>();
        for (final DespesaVariavel dv : despesasVariaveis) {
            if (dv.getDataInicio() != null && dv.getQuantidadeParcelas() != null) {
                final YearMonth inicio = YearMonth.from(dv.getDataInicio());
                inicioDespesaVariavel.put(dv.getId(), inicio);
                fimDespesaVariavel.put(dv.getId(), inicio.plusMonths(dv.getQuantidadeParcelas() - 1));
            }
        }

        final Map<Long, YearMonth> inicioReceitaFixa = new HashMap<>();
        for (final ReceitaFixa rf : receitasFixas) {
            if (rf.getDataInicio() != null) {
                inicioReceitaFixa.put(rf.getId(), YearMonth.from(rf.getDataInicio()));
            }
        }

        final Map<Long, YearMonth> inicioDespesaFixa = new HashMap<>();
        for (final DespesaFixa df : despesasFixas) {
            if (df.getDataInicio() != null) {
                inicioDespesaFixa.put(df.getId(), YearMonth.from(df.getDataInicio()));
            }
        }

        for (int i = 0; i < 24; i++) {
            final YearMonth currentMonth = startMonth.plusMonths(i);

            for (final ReceitaFixa rf : receitasFixas) {
                final Long contaId = rf.getConta() != null ? rf.getConta().getId() : SEM_CONTA_ID;
                final YearMonth inicio = inicioReceitaFixa.get(rf.getId());
                if (inicio == null || !inicio.isAfter(currentMonth)) {
                    somarValor(receitasMap, contaId, i, rf.getValor());
                }
            }

            for (final ReceitaVariavel rv : receitasVariaveis) {
                final Long contaId = rv.getConta() != null ? rv.getConta().getId() : SEM_CONTA_ID;
                final YearMonth inicio = inicioReceitaVariavel.get(rv.getId());
                final YearMonth fim = fimReceitaVariavel.get(rv.getId());
                if (inicio != null && fim != null && !currentMonth.isBefore(inicio) && !currentMonth.isAfter(fim)) {
                    somarValor(receitasMap, contaId, i, rv.getValorParcela());
                }
            }

            for (final DespesaFixa df : despesasFixas) {
                final Long contaId = df.getConta() != null ? df.getConta().getId() : SEM_CONTA_ID;
                final YearMonth inicio = inicioDespesaFixa.get(df.getId());
                if (inicio == null || !inicio.isAfter(currentMonth)) {
                    somarValor(despesasMap, contaId, i, df.getValor());
                }
            }

            for (final DespesaVariavel dv : despesasVariaveis) {
                final Long contaId = dv.getConta() != null ? dv.getConta().getId() : SEM_CONTA_ID;
                final YearMonth inicio = inicioDespesaVariavel.get(dv.getId());
                final YearMonth fim = fimDespesaVariavel.get(dv.getId());
                if (inicio != null && fim != null && !currentMonth.isBefore(inicio) && !currentMonth.isAfter(fim)) {
                    somarValor(despesasMap, contaId, i, dv.getValorParcela());
                }
            }
        }

        final BigDecimal totalReceitasHistoricas = calcularTotalReceitasHistoricas(receitasFixas, receitasVariaveis, startMonth);
        final BigDecimal totalDespesasHistoricas = calcularTotalDespesasHistoricas(despesasFixas, despesasVariaveis, startMonth);

        final BigDecimal saldoHistoricoPreGrade = totalReceitasHistoricas.subtract(totalDespesasHistoricas).setScale(2, java.math.RoundingMode.HALF_UP);

        // Oculta contas que não possuem nenhum valor lançado na grade de 24 meses
        final List<LinhaConsolidacaoDTO> receitasFiltradas = receitasMap.values().stream()
                .filter(linha -> linha.getValoresMensais().stream().anyMatch(valor -> valor != null && valor.compareTo(BigDecimal.ZERO) != 0))
                .toList();

        final List<LinhaConsolidacaoDTO> despesasFiltradas = despesasMap.values().stream()
                .filter(linha -> linha.getValoresMensais().stream().anyMatch(valor -> valor != null && valor.compareTo(BigDecimal.ZERO) != 0))
                .toList();

        return new ConsolidacaoPorContaDTO(
            receitasFiltradas,
            despesasFiltradas,
            saldoHistoricoPreGrade
        );
    }

    private BigDecimal calcularTotalReceitasHistoricas(
            final List<ReceitaFixa> receitasFixas,
            final List<ReceitaVariavel> receitasVariaveis,
            final YearMonth startMonth) {
        final BigDecimal totalFixas = receitasFixas.stream()
                .filter(rf -> rf.getDataInicio() != null && rf.getValor() != null)
                .filter(rf -> YearMonth.from(rf.getDataInicio()).isBefore(startMonth))
                .map(rf -> {
                    final long meses = java.time.temporal.ChronoUnit.MONTHS.between(YearMonth.from(rf.getDataInicio()), startMonth);
                    return rf.getValor().multiply(BigDecimal.valueOf(meses));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal totalVariaveis = receitasVariaveis.stream()
                .filter(rv -> rv.getDataInicio() != null && rv.getQuantidadeParcelas() != null && rv.getQuantidadeParcelas() > 0 && rv.getValorParcela() != null)
                .filter(rv -> YearMonth.from(rv.getDataInicio()).isBefore(startMonth))
                .map(rv -> {
                    final long parcelasAntes = Math.min((long) rv.getQuantidadeParcelas(), java.time.temporal.ChronoUnit.MONTHS.between(YearMonth.from(rv.getDataInicio()), startMonth));
                    return parcelasAntes > 0 ? rv.getValorParcela().multiply(BigDecimal.valueOf(parcelasAntes)) : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalFixas.add(totalVariaveis);
    }

    private BigDecimal calcularTotalDespesasHistoricas(
            final List<DespesaFixa> despesasFixas,
            final List<DespesaVariavel> despesasVariaveis,
            final YearMonth startMonth) {
        final BigDecimal totalFixas = despesasFixas.stream()
                .filter(df -> df.getDataInicio() != null && df.getValor() != null)
                .filter(df -> YearMonth.from(df.getDataInicio()).isBefore(startMonth))
                .map(df -> {
                    final long meses = java.time.temporal.ChronoUnit.MONTHS.between(YearMonth.from(df.getDataInicio()), startMonth);
                    return df.getValor().multiply(BigDecimal.valueOf(meses));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal totalVariaveis = despesasVariaveis.stream()
                .filter(dv -> dv.getDataInicio() != null && dv.getQuantidadeParcelas() != null && dv.getQuantidadeParcelas() > 0 && dv.getValorParcela() != null)
                .filter(dv -> YearMonth.from(dv.getDataInicio()).isBefore(startMonth))
                .map(dv -> {
                    final long parcelasAntes = Math.min((long) dv.getQuantidadeParcelas(), java.time.temporal.ChronoUnit.MONTHS.between(YearMonth.from(dv.getDataInicio()), startMonth));
                    return parcelasAntes > 0 ? dv.getValorParcela().multiply(BigDecimal.valueOf(parcelasAntes)) : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalFixas.add(totalVariaveis);
    }

    private LinhaConsolidacaoDTO criarLinha(final Long id, final String descricao) {
        final List<BigDecimal> valores = new ArrayList<>(24);
        for (int i = 0; i < 24; i++) {
            valores.add(BigDecimal.ZERO);
        }
        return new LinhaConsolidacaoDTO(id, descricao, valores);
    }

    private void somarValor(final Map<Long, LinhaConsolidacaoDTO> map, final Long contaId, final int index, final BigDecimal valor) {
        final LinhaConsolidacaoDTO linhaEncontrada = map.get(contaId);
        final LinhaConsolidacaoDTO linha = (linhaEncontrada != null) ? linhaEncontrada : map.get(SEM_CONTA_ID);
        if (linha != null && valor != null) {
            final BigDecimal atual = linha.getValoresMensais().get(index);
            linha.getValoresMensais().set(index, atual.add(valor));
        }
    }

    @Transactional(readOnly = true)
    public ConsolidacaoPorCategoriaDTO calcularConsolidacaoPorCategoria(final String usuarioId, final String mesAno) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("usuarioId não pode ser nulo");
        }
        if (mesAno == null) {
            throw new IllegalArgumentException("mesAno não pode ser nulo");
        }
        final YearMonth startMonth = YearMonth.parse(mesAno);
        final YearMonth endMonth = startMonth.plusMonths(23);
        
        final List<Categoria> categorias = categoriaRepository.findByUsuarioIdOrderByDescricaoAsc(usuarioId);
        
        final Map<Long, LinhaConsolidacaoCategoriaDTO> despesasMap = new LinkedHashMap<>();
        
        for (final Categoria c : categorias) {
            despesasMap.put(c.getId(), criarLinhaCategoria(c.getId(), c.getDescricao()));
        }
        
        // Synthetic line for "Sem Categoria"
        despesasMap.put(SEM_CATEGORIA_ID, criarLinhaCategoria(SEM_CATEGORIA_ID, "Sem Categoria"));

        final List<DespesaFixa> despesasFixas = despesaFixaRepository.findByUsuarioId(usuarioId);
        final List<DespesaVariavel> despesasVariaveis = despesaVariavelRepository.findByUsuarioId(usuarioId);

        // O(N) loop for DespesaFixa
        for (final DespesaFixa df : despesasFixas) {
            if (df.getValor() == null) continue;
            final Long catId = df.getCategoria() != null ? df.getCategoria().getId() : SEM_CATEGORIA_ID;
            final int startIdx = df.getDataInicio() != null
                    ? Math.max(0, (int) java.time.temporal.ChronoUnit.MONTHS.between(startMonth, YearMonth.from(df.getDataInicio())))
                    : 0;
            
            for (int i = startIdx; i < 24; i++) {
                somarValorCategoria(despesasMap, catId, i, df.getValor());
            }
        }

        // O(N) loop for DespesaVariavel
        for (final DespesaVariavel dv : despesasVariaveis) {
            if (dv.getDataInicio() == null || dv.getQuantidadeParcelas() == null || dv.getQuantidadeParcelas() <= 0 || dv.getValorParcela() == null) continue;
            final Long catId = dv.getCategoria() != null ? dv.getCategoria().getId() : SEM_CATEGORIA_ID;
            final YearMonth inicio = YearMonth.from(dv.getDataInicio());
            final YearMonth fim = inicio.plusMonths(dv.getQuantidadeParcelas() - 1);
            
            // Skip if entirely outside the 24-month window
            if (fim.isBefore(startMonth) || inicio.isAfter(endMonth)) continue;
            
            // Calculate overlap bounds
            final int startIdx = Math.max(0, (int) java.time.temporal.ChronoUnit.MONTHS.between(startMonth, inicio));
            final int endIdx = Math.min(23, (int) java.time.temporal.ChronoUnit.MONTHS.between(startMonth, fim));
            
            for (int i = startIdx; i <= endIdx; i++) {
                somarValorCategoria(despesasMap, catId, i, dv.getValorParcela());
            }
        }

        // Filter out empty categories
        final List<LinhaConsolidacaoCategoriaDTO> despesasFiltradas = despesasMap.values().stream()
                .filter(linha -> linha.getValoresMensais().stream().anyMatch(valor -> valor != null && valor.compareTo(BigDecimal.ZERO) != 0))
                .toList();

        return new ConsolidacaoPorCategoriaDTO(despesasFiltradas);
    }

    private LinhaConsolidacaoCategoriaDTO criarLinhaCategoria(final Long id, final String descricao) {
        final List<BigDecimal> valores = new ArrayList<>(Collections.nCopies(24, BigDecimal.ZERO));
        return new LinhaConsolidacaoCategoriaDTO(id, descricao, valores);
    }

    private void somarValorCategoria(final Map<Long, LinhaConsolidacaoCategoriaDTO> map, final Long categoriaId, final int index, final BigDecimal valor) {
        final LinhaConsolidacaoCategoriaDTO linhaEncontrada = map.get(categoriaId);
        final LinhaConsolidacaoCategoriaDTO linha = (linhaEncontrada != null) ? linhaEncontrada : map.get(SEM_CATEGORIA_ID);
        if (linha != null && valor != null) {
            final BigDecimal atual = linha.getValoresMensais().get(index);
            linha.getValoresMensais().set(index, atual.add(valor));
        }
    }

    /**
     * Calcula o percentual de uma categoria em relação ao total de despesas.
     * Previne divisão por zero retornando 0.00 quando o total ou valor for nulo ou menor/igual a zero.
     *
     * @param valorCategoria Valor gasto na categoria
     * @param totalDespesas Total consolidado de despesas
     * @return Percentual calculado com escala 2 e arredondamento HALF_UP
     */
    public static BigDecimal calcularPercentual(final BigDecimal valorCategoria, final BigDecimal totalDespesas) {
        if (totalDespesas == null || totalDespesas.compareTo(BigDecimal.ZERO) <= 0 
                || valorCategoria == null || valorCategoria.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return valorCategoria.multiply(BigDecimal.valueOf(100))
                .divide(totalDespesas, 2, RoundingMode.HALF_UP);
    }
}
