import { render, screen, fireEvent, cleanup } from '@testing-library/react';
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
    expect(screen.getByText('Sobra Retroativa Acumulada')).toBeDefined();

    // Deve computar e propagar saldoHistoricoPreGrade na linha de Sobra Retroativa Acumulada (250 + 600 = 850)
    expect(screen.getByText('R$ 850,00')).toBeDefined();
  });

  it('oculta linhas de contas que possuem todos os valores zerados na grade de 24 meses', () => {
    vi.spyOn(useConsolidacaoHook, 'useConsolidacao').mockReturnValue({
      data: {
        receitas: [
          { contaId: 1, contaDescricao: 'Conta Ativa', valoresMensais: [100, ...Array(23).fill(0)] },
          { contaId: 2, contaDescricao: 'Conta Zerada', valoresMensais: Array(24).fill(0) },
        ],
        despesas: [
          { contaId: 1, contaDescricao: 'Conta Ativa', valoresMensais: [50, ...Array(23).fill(0)] },
          { contaId: 3, contaDescricao: 'Conta Inativa', valoresMensais: Array(24).fill(0) },
        ],
        saldoHistoricoPreGrade: 0,
      },
      isLoading: false,
      isError: false,
    } as any);

    render(<ConsolidacaoGrid />);

    // Conta Ativa deve estar presente
    expect(screen.getAllByText('Conta Ativa').length).toBe(2);

    // Conta Zerada e Conta Inativa não devem aparecer
    expect(screen.queryByText('Conta Zerada')).toBeNull();
    expect(screen.queryByText('Conta Inativa')).toBeNull();
  });

  it('renderiza o supercabeçalho de exercício com os anos agrupados', () => {
    render(<ConsolidacaoGrid />);

    expect(screen.getByText('Exercício')).toBeDefined();
    expect(screen.getByText('Contas & Categorias')).toBeDefined();
    expect(screen.getByText('2026')).toBeDefined();
    expect(screen.getByText('2027')).toBeDefined();
    expect(screen.getByText('2028')).toBeDefined();
  });

  it('permite colapsar e expandir seções ao clicar no cabeçalho do bloco', () => {
    render(<ConsolidacaoGrid />);

    // Inicialmente, Banco do Brasil está visível
    expect(screen.getAllByText('Banco do Brasil').length).toBe(2);

    // Clica no cabeçalho de Receitas por Conta para colapsar
    const headerReceitas = screen.getByRole('button', { name: /Receitas por Conta/i });
    fireEvent.click(headerReceitas);

    // Agora deve restar apenas 1 ocorrência de Banco do Brasil (no bloco de Despesas)
    expect(screen.getAllByText('Banco do Brasil').length).toBe(1);

    // Clica novamente para expandir
    fireEvent.click(headerReceitas);
    expect(screen.getAllByText('Banco do Brasil').length).toBe(2);
  });

  it('não renderiza contadores nos cabeçalhos dos blocos', () => {
    render(<ConsolidacaoGrid />);

    // Nenhum contador como 'X conta(s)', 'X categoria(s)', 'X métricas' deve existir
    expect(screen.queryByText(/\d+\s+contas?/i)).toBeNull();
    expect(screen.queryByText(/\d+\s+categorias?/i)).toBeNull();
    expect(screen.queryByText(/\d+\s+métricas?/i)).toBeNull();
  });

  it('recolhe e expande seções de acordo com a propriedade collapsed controlada', () => {
    const { rerender } = render(
      <ConsolidacaoGrid
        collapsed={{
          receitas: false,
          despesas: false,
          categoriasVal: false,
          categoriasPct: false,
          resumo: false,
        }}
      />
    );

    // Conteúdos inicialmente visíveis
    expect(screen.getAllByText('Banco do Brasil').length).toBe(2);
    expect(screen.getAllByText('Alimentação').length).toBe(2);
    expect(screen.getByText('Total Gasto no Mês')).toBeDefined();

    // Rerender com tudo colapsado
    rerender(
      <ConsolidacaoGrid
        collapsed={{
          receitas: true,
          despesas: true,
          categoriasVal: true,
          categoriasPct: true,
          resumo: true,
        }}
      />
    );

    // Tudo deve ter sido colapsado
    expect(screen.queryByText('Banco do Brasil')).toBeNull();
    expect(screen.queryByText('Alimentação')).toBeNull();
    expect(screen.queryByText('Total Gasto no Mês')).toBeNull();
    expect(screen.queryByText('Sobra do Mês')).toBeNull();
    expect(screen.queryByText('Sobra Retroativa Acumulada')).toBeNull();
  });
});
