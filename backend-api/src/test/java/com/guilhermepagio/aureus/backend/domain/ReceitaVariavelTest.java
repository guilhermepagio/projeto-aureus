package com.guilhermepagio.aureus.backend.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReceitaVariavelTest {

    @Test
    @DisplayName("Deve calcular valor total de receita parcelada")
    void deveCalcularValorTotalParcelado() {
        ReceitaVariavel receita = new ReceitaVariavel();
        receita.setValorParcela(new BigDecimal("150.50"));
        receita.setQuantidadeParcelas(10);

        BigDecimal total = receita.getValorTotal();

        assertEquals(0, new BigDecimal("1505.00").compareTo(total));
    }

    @Test
    @DisplayName("Deve calcular valor total e datas para parcela única")
    void deveCalcularValorTotalEDatasParcelaUnica() {
        ReceitaVariavel receita = new ReceitaVariavel();
        receita.setValorParcela(new BigDecimal("250.00"));
        receita.setQuantidadeParcelas(1);
        receita.setDataInicio(LocalDate.of(2024, 5, 15));

        BigDecimal total = receita.getValorTotal();
        LocalDate ultimaParcela = receita.getUltimaParcela();

        assertEquals(0, new BigDecimal("250.00").compareTo(total));
        assertEquals(LocalDate.of(2024, 5, 1), ultimaParcela);
    }

    @Test
    @DisplayName("Deve projetar última parcela em parcelamento multi-mês")
    void deveProjetarUltimaParcelaMultiMes() {
        ReceitaVariavel receita = new ReceitaVariavel();
        receita.setDataInicio(LocalDate.of(2024, 1, 10));
        receita.setQuantidadeParcelas(12);

        LocalDate ultimaParcela = receita.getUltimaParcela();

        assertEquals(LocalDate.of(2024, 12, 1), ultimaParcela);
    }

    @Test
    @DisplayName("Deve retornar 0.00 com escala 2 quando valor da parcela ou quantidade forem nulos")
    void deveRetornarZeroQuandoCamposNulosParaValorTotal() {
        ReceitaVariavel receita = new ReceitaVariavel();

        assertEquals(new BigDecimal("0.00"), receita.getValorTotal());

        receita.setValorParcela(new BigDecimal("100.00"));
        receita.setQuantidadeParcelas(null);
        assertEquals(new BigDecimal("0.00"), receita.getValorTotal());

        receita.setValorParcela(null);
        receita.setQuantidadeParcelas(5);
        assertEquals(new BigDecimal("0.00"), receita.getValorTotal());
    }

    @Test
    @DisplayName("Deve retornar null para última parcela quando campos forem inválidos ou nulos")
    void deveRetornarNullQuandoCamposInvalidosParaUltimaParcela() {
        ReceitaVariavel receita = new ReceitaVariavel();

        assertNull(receita.getUltimaParcela());

        receita.setDataInicio(LocalDate.of(2024, 1, 1));
        receita.setQuantidadeParcelas(null);
        assertNull(receita.getUltimaParcela());

        receita.setQuantidadeParcelas(0);
        assertNull(receita.getUltimaParcela());

        receita.setQuantidadeParcelas(-5);
        assertNull(receita.getUltimaParcela());

        receita.setDataInicio(null);
        receita.setQuantidadeParcelas(3);
        assertNull(receita.getUltimaParcela());
    }

    @Test
    @DisplayName("Deve cobrir construtores e propriedades de ReceitaVariavel")
    void deveCobrirPropriedadesEConstrutores() {
        Categoria categoria = new Categoria(1L, "Renda Extra", "Freelance");
        Conta conta = new Conta(2L, "Nubank", "Principal");
        LocalDate dataInicio = LocalDate.of(2024, 2, 1);
        LocalDate dataFim = LocalDate.of(2024, 4, 1);

        ReceitaVariavel receita = new ReceitaVariavel(
                20L, "Projeto Freelance", new BigDecimal("500.00"), 3,
                dataInicio, dataFim, categoria, conta, "Trabalho pontual"
        );

        assertEquals(20L, receita.getId());
        assertEquals("Projeto Freelance", receita.getDescricao());
        assertEquals(new BigDecimal("500.00"), receita.getValorParcela());
        assertEquals(3, receita.getQuantidadeParcelas());
        assertEquals(dataInicio, receita.getDataInicio());
        assertEquals(dataFim, receita.getDataFim());
        assertEquals(categoria, receita.getCategoria());
        assertEquals(conta, receita.getConta());
        assertEquals("Trabalho pontual", receita.getObservacoes());
    }
}
