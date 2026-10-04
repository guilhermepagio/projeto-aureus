import { render, screen, cleanup } from '@testing-library/react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { BlocoResumo } from './BlocoResumo';
import * as useConsolidacaoHook from '../../hooks/useConsolidacao';

vi.mock('../../hooks/useConsolidacao', () => ({
  useConsolidacao: vi.fn().mockReturnValue({
    data: undefined,
    isLoading: false,
    isError: false,
  }),
}));

describe('BlocoResumo', () => {
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });
  it('renderiza os blocos e linhas de resumo com cálculos corretos de Total Gasto, Sobra do Mês e Sobra Retroativa Acumulada', () => {
    const receitas = [
      {
        contaId: 1,
        contaDescricao: 'Conta Corrente',
        valoresMensais: [1000, 1000, 500, ...Array(21).fill(0)],
      },
    ];

    const despesas = [
      {
        contaId: 1,
        contaDescricao: 'Conta Corrente',
        valoresMensais: [300, 400, 600, ...Array(21).fill(0)],
      },
      {
        contaId: 2,
        contaDescricao: 'Cartão de Crédito',
        valoresMensais: [200, 100, 0, ...Array(21).fill(0)],
      },
    ];

    const saldoHistoricoPreGrade = 150;

    render(
      <table>
        <tbody>
          <BlocoResumo
            currentIdx={0}
            receitas={receitas}
            despesas={despesas}
            saldoHistoricoPreGrade={saldoHistoricoPreGrade}
          />
        </tbody>
      </table>
    );

    // Deve exibir o cabeçalho Resumo Geral
    expect(screen.getByText('Resumo Geral')).toBeDefined();

    // Deve exibir as três linhas de consolidação
    expect(screen.getByText('Total Gasto no Mês')).toBeDefined();
    expect(screen.getByText('Sobra do Mês')).toBeDefined();
    expect(screen.getByText('Sobra Retroativa Acumulada')).toBeDefined();

    // Mês 0 (Jan):
    // Total Gasto: 300 + 200 = 500
    // Sobra do Mês: 1000 - 500 = +500
    // Sobra Retroativa Acumulada: 150 (saldoHistórico) + 500 = +650
    expect(screen.getAllByText('R$ 500,00').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('R$ 650,00')).toBeDefined();

    // Mês 1 (Fev):
    // Total Gasto: 400 + 100 = 500
    // Sobra do Mês: 1000 - 500 = +500
    // Sobra Retroativa Acumulada: 650 + 500 = 1150
    expect(screen.getByText('R$ 1.150,00')).toBeDefined();

    // Mês 2 (Mar):
    // Total Gasto: 600 + 0 = 600
    // Sobra do Mês: 500 - 600 = -100 (negativo!)
    // Sobra Retroativa Acumulada: 1150 - 100 = 1050
    expect(screen.getAllByText('R$ 600,00').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('-R$ 100,00')).toBeDefined();
    expect(screen.getAllByText('R$ 1.050,00').length).toBeGreaterThanOrEqual(1);
  });

  it('lida com ausência de histórico pré-grade e meses zerados graciosamente', () => {
    render(
      <table>
        <tbody>
          <BlocoResumo
            currentIdx={0}
            receitas={[]}
            despesas={[]}
            saldoHistoricoPreGrade={0}
          />
        </tbody>
      </table>
    );

    expect(screen.getByText('Resumo Geral')).toBeDefined();
    expect(screen.getByText('Total Gasto no Mês')).toBeDefined();
    expect(screen.getByText('Sobra do Mês')).toBeDefined();
    expect(screen.getByText('Sobra Retroativa Acumulada')).toBeDefined();

    // Com 0 receitas e 0 despesas, deve renderizar R$ 0,00 sem erros
    const zeros = screen.getAllByText('R$ 0,00');
    expect(zeros.length).toBeGreaterThanOrEqual(24 * 3);
  });

  it('renderiza corretamente dados vindos do hook quando props não são fornecidas', () => {
    vi.spyOn(useConsolidacaoHook, 'useConsolidacao').mockReturnValue({
      data: {
        receitas: [
          { contaId: 1, contaDescricao: 'Conta 1', valoresMensais: [200, ...Array(23).fill(0)] }
        ],
        despesas: [
          { contaId: 1, contaDescricao: 'Conta 1', valoresMensais: [50, ...Array(23).fill(0)] }
        ],
        saldoHistoricoPreGrade: 50,
      },
      isLoading: false,
      isError: false,
    } as any);

    render(
      <table>
        <tbody>
          <BlocoResumo
            selectedMonth="2024-01"
            currentIdx={0}
          />
        </tbody>
      </table>
    );

    expect(screen.getByText('Resumo Geral')).toBeDefined();
    // Total Gasto Mês 0 = 50
    expect(screen.getByText('R$ 50,00')).toBeDefined();
    // Sobra Mês 0 = 200 - 50 = 150
    expect(screen.getByText('R$ 150,00')).toBeDefined();
    // Sobra Retroativa Mês 0 = 50 + 150 = 200
    expect(screen.getAllByText('R$ 200,00').length).toBeGreaterThanOrEqual(1);
  });

  it('renderiza o estado de carregamento sem violar a contagem de hooks', () => {
    vi.spyOn(useConsolidacaoHook, 'useConsolidacao').mockReturnValue({
      data: undefined,
      isLoading: true,
      isError: false,
    } as any);

    render(
      <table>
        <tbody>
          <BlocoResumo
            selectedMonth="2024-01"
            currentIdx={0}
          />
        </tbody>
      </table>
    );

    expect(screen.getByText('Carregando resumo…')).toBeDefined();
  });

  it('renderiza o estado de erro quando a query falha', () => {
    vi.spyOn(useConsolidacaoHook, 'useConsolidacao').mockReturnValue({
      data: undefined,
      isLoading: false,
      isError: true,
    } as any);

    render(
      <table>
        <tbody>
          <BlocoResumo
            selectedMonth="2024-01"
            currentIdx={0}
          />
        </tbody>
      </table>
    );

    expect(screen.getByText('Erro ao carregar dados do resumo.')).toBeDefined();
  });
});
