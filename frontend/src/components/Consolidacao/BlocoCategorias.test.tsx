import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { BlocoCategorias } from './BlocoCategorias';
import * as useConsolidacaoCategoriaHook from '../../hooks/useConsolidacaoCategoria';

vi.mock('../../hooks/useConsolidacaoCategoria', () => ({
  useConsolidacaoCategoria: vi.fn(),
}));

describe('BlocoCategorias', () => {
  it('não renderiza nada se selectedMonth for nulo', () => {
    const { container } = render(
      <table>
        <tbody>
          <BlocoCategorias selectedMonth={null} />
        </tbody>
      </table>
    );
    expect(container.querySelector('tr')).toBeNull();
  });

  it('exibe mensagem de erro se a query falhar', () => {
    vi.spyOn(useConsolidacaoCategoriaHook, 'useConsolidacaoCategoria').mockReturnValue({
      data: undefined,
      isLoading: false,
      isError: true,
      error: new Error(),
      isPending: false,
      isSuccess: false,
      status: 'error',
      fetchStatus: 'idle',
      dataUpdatedAt: 0,
      errorUpdatedAt: 0,
      failureCount: 1,
      failureReason: null,
      errorUpdateCount: 1,
      isFetched: true,
      isFetchedAfterMount: true,
      isFetching: false,
      isInitialLoading: false,
      isLoadingError: false,
      isPaused: false,
      isPlaceholderData: false,
      isRefetchError: false,
      isRefetching: false,
      isStale: true,
      refetch: vi.fn(),
      promise: Promise.resolve(undefined)
    } as any);

    render(
      <table>
        <tbody>
          <BlocoCategorias selectedMonth="2024-01" />
        </tbody>
      </table>
    );

    expect(screen.getByText(/Erro ao carregar os dados das categorias/i)).toBeInTheDocument();
  });

  it('renderiza os blocos de R$ e % com cálculos corretos e previne divisão por zero', () => {
    const mockData = {
      despesas: [
        {
          categoriaId: 1,
          categoriaDescricao: 'Alimentação',
          valoresMensais: [100, 200, 0, ...Array(21).fill(0)], // Total Mês 0 = 150, Mês 1 = 200
        },
        {
          categoriaId: 2,
          categoriaDescricao: 'Transporte',
          valoresMensais: [50, 0, 0, ...Array(21).fill(0)],
        }
      ]
    };

    vi.spyOn(useConsolidacaoCategoriaHook, 'useConsolidacaoCategoria').mockReturnValue({
      data: mockData,
      isLoading: false,
      isError: false,
      error: null,
      isPending: false,
      isSuccess: true,
      status: 'success',
      fetchStatus: 'idle',
      dataUpdatedAt: 1,
      errorUpdatedAt: 0,
      failureCount: 0,
      failureReason: null,
      errorUpdateCount: 0,
      isFetched: true,
      isFetchedAfterMount: true,
      isFetching: false,
      isInitialLoading: false,
      isLoadingError: false,
      isPaused: false,
      isPlaceholderData: false,
      isRefetchError: false,
      isRefetching: false,
      isStale: false,
      refetch: vi.fn(),
      promise: Promise.resolve(mockData)
    } as any);

    render(
      <table>
        <tbody>
          <BlocoCategorias selectedMonth="2024-01" />
        </tbody>
      </table>
    );

    // Blocos devem estar presentes
    expect(screen.getByText('Categorias (R$)')).toBeInTheDocument();
    expect(screen.getByText('Categorias (%)')).toBeInTheDocument();
    
    // Nomes das categorias
    expect(screen.getAllByText('Alimentação')).toHaveLength(2); // um no R$, um no %
    expect(screen.getAllByText('Transporte')).toHaveLength(2);

    // Mês 0 percentuais: Alimentação = 100/150 = 66.7%, Transporte = 50/150 = 33.3%
    expect(screen.getByText('66,7%')).toBeInTheDocument();
    expect(screen.getByText('33,3%')).toBeInTheDocument();

    // Mês 1 percentuais: Alimentação = 200/200 = 100%, Transporte = 0/200 = 0%
    expect(screen.getByText('100,0%')).toBeInTheDocument();
    
    // Mês 2 tem total 0, então divisões por zero devem resultar em 0,0%
    const zeroPercents = screen.getAllByText('0,0%');
    expect(zeroPercents.length).toBeGreaterThan(0);
  });
});
