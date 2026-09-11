import { render, screen, cleanup } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import ConsolidacaoGrid from './ConsolidacaoGrid';
import { useMonthStore } from '../../store/monthStore';
import * as useConsolidacaoHook from '../../hooks/useConsolidacao';
import * as useConsolidacaoCategoriaHook from '../../hooks/useConsolidacaoCategoria';

vi.mock('../../hooks/useConsolidacao', () => ({
  useConsolidacao: vi.fn(),
}));

vi.mock('../../hooks/useConsolidacaoCategoria', () => ({
  useConsolidacaoCategoria: vi.fn(),
}));

describe('ConsolidacaoGrid', () => {
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  beforeEach(() => {
    useMonthStore.getState().setSelectedMonth('2026-08');

    vi.spyOn(useConsolidacaoHook, 'useConsolidacao').mockReturnValue({
      data: {
        receitas: [
          { contaId: 1, contaDescricao: 'Banco do Brasil', valoresMensais: Array(24).fill(1000) }
        ],
        despesas: [
          { contaId: 1, contaDescricao: 'Banco do Brasil', valoresMensais: Array(24).fill(400) }
        ],
        saldoHistoricoPreGrade: 250,
      },
      isLoading: false,
      isError: false,
    } as any);

    vi.spyOn(useConsolidacaoCategoriaHook, 'useConsolidacaoCategoria').mockReturnValue({
      data: {
        despesas: [
          { categoriaId: 10, categoriaDescricao: 'Alimentação', valoresMensais: Array(24).fill(200) }
        ]
      },
      isLoading: false,
      isError: false,
    } as any);
  });

  it('renderiza as colunas temporais, seções de contas, categorias e o Bloco de Resumo Geral', () => {
    render(<ConsolidacaoGrid />);

    // Deve exibir contas e categorias
    expect(screen.getByText('Receitas por Conta')).toBeDefined();
    expect(screen.getByText('Despesas por Conta')).toBeDefined();
    expect(screen.getByText('Categorias (R$)')).toBeDefined();
    expect(screen.getByText('Categorias (%)')).toBeDefined();

    // Deve integrar e renderizar a seção Resumo Geral e suas linhas
    expect(screen.getByText('Resumo Geral')).toBeDefined();
    expect(screen.getByText('Total Gasto no Mês')).toBeDefined();
    expect(screen.getByText('Sobra do Mês')).toBeDefined();
    expect(screen.getByText('Sobra Retroativa Acum.')).toBeDefined();

    // Deve computar e propagar saldoHistoricoPreGrade na linha de Sobra Retroativa Acumulada (250 + 600 = 850)
    expect(screen.getByText('R$ 850,00')).toBeDefined();
  });
});
