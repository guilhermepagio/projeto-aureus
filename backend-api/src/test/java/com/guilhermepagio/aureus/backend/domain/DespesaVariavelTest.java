package com.guilhermepagio.aureus.backend.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DespesaVariavelTest {

    @Test
    @DisplayName("Deve calcular valor total de despesa parcelada")
    void deveCalcularValorTotalParcelado() {
        DespesaVariavel despesa = new DespesaVariavel();
        despesa.setValorParcela(new BigDecimal("150.50"));
        despesa.setQuantidadeParcelas(10);

        BigDecimal total = despesa.getValorTotal();

        assertEquals(0, new BigDecimal("1505.00").compareTo(total));
    }

    @Test
    @DisplayName("Deve calcular valor total e datas para parcela única")
    void deveCalcularValorTotalEDatasParcelaUnica() {
        DespesaVariavel despesa = new DespesaVariavel();
        despesa.setValorParcela(new BigDecimal("250.00"));
        despesa.setQuantidadeParcelas(1);
        despesa.setDataInicio(LocalDate.of(2024, 5, 15));

        BigDecimal total = despesa.getValorTotal();
        LocalDate ultimaParcela = despesa.getUltimaParcela();

        assertEquals(0, new BigDecimal("250.00").compareTo(total));
        assertEquals(LocalDate.of(2024, 5, 1), ultimaParcela);
    }

    @Test
    @DisplayName("Deve projetar última parcela em parcelamento multi-mês")
    void deveProjetarUltimaParcelaMultiMes() {
        DespesaVariavel despesa = new DespesaVariavel();
        despesa.setDataInicio(LocalDate.of(2024, 1, 10));
        despesa.setQuantidadeParcelas(12);

        LocalDate ultimaParcela = despesa.getUltimaParcela();

        assertEquals(LocalDate.of(2024, 12, 1), ultimaParcela);
    }

    @Test
    @DisplayName("Deve retornar 0.00 com escala 2 quando valor da parcela ou quantidade forem nulos")
    void deveRetornarZeroQuandoCamposNulosParaValorTotal() {
        DespesaVariavel despesa = new DespesaVariavel();

        assertEquals(new BigDecimal("0.00"), despesa.getValorTotal());

        despesa.setValorParcela(new BigDecimal("100.00"));
        despesa.setQuantidadeParcelas(null);
        assertEquals(new BigDecimal("0.00"), despesa.getValorTotal());

        despesa.setValorParcela(null);
        despesa.setQuantidadeParcelas(5);
        assertEquals(new BigDecimal("0.00"), despesa.getValorTotal());
    }

    @Test
    @DisplayName("Deve retornar null para última parcela quando campos forem inválidos ou nulos")
    void deveRetornarNullQuandoCamposInvalidosParaUltimaParcela() {
        DespesaVariavel despesa = new DespesaVariavel();

        assertNull(despesa.getUltimaParcela());

        despesa.setDataInicio(LocalDate.of(2024, 1, 1));
        despesa.setQuantidadeParcelas(null);
        assertNull(despesa.getUltimaParcela());

        despesa.setQuantidadeParcelas(0);
        assertNull(despesa.getUltimaParcela());

        despesa.setQuantidadeParcelas(-5);
        assertNull(despesa.getUltimaParcela());

        despesa.setDataInicio(null);
        despesa.setQuantidadeParcelas(3);
        assertNull(despesa.getUltimaParcela());
    }

    @Test
    @DisplayName("Deve cobrir construtores e propriedades de DespesaVariavel")
    void deveCobrirPropriedadesEConstrutores() {
        Categoria categoria = new Categoria(1L, "Alimentação", "Mercado");
        Conta conta = new Conta(2L, "Nubank", "Principal");
        LocalDate dataCompra = LocalDate.of(2024, 3, 10);
        LocalDate dataInicio = LocalDate.of(2024, 3, 1);
        LocalDate dataFim = LocalDate.of(2024, 5, 1);

        DespesaVariavel despesa = new DespesaVariavel(
                10L, "Supermercado", "Carrefour", dataCompra,
                new BigDecimal("300.00"), 3, dataInicio, dataFim,
                categoria, conta, "Compras do mês"
        );

        assertEquals(10L, despesa.getId());
        assertEquals("Supermercado", despesa.getDescricao());
        assertEquals("Carrefour", despesa.getLocalCompra());
        assertEquals(dataCompra, despesa.getDataCompra());
        assertEquals(new BigDecimal("300.00"), despesa.getValorParcela());
        assertEquals(3, despesa.getQuantidadeParcelas());
        assertEquals(dataInicio, despesa.getDataInicio());
        assertEquals(dataFim, despesa.getDataFim());
        assertEquals(categoria, despesa.getCategoria());
        assertEquals(conta, despesa.getConta());
        assertEquals("Compras do mês", despesa.getObservacoes());
    }
}
