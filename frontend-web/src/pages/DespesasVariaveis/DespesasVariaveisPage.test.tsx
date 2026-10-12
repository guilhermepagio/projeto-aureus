import React from 'react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, cleanup, within } from '@testing-library/react';
import DespesasVariaveisPage from './DespesasVariaveisPage';
import { useMonthStore } from '../../store/monthStore';
import type { DespesaVariavel } from '../../hooks/useDespesasVariaveis';

let mockDespesasQuery = {
  data: [] as DespesaVariavel[] | undefined,
  isLoading: false,
  isError: false,
};

const mockDeleteDespesaMutate = vi.fn();
vi.mock('../../hooks/useDespesasVariaveis', () => ({
  useDespesasVariaveis: () => mockDespesasQuery,
  useCreateDespesaVariavel: () => ({ mutate: vi.fn(), isPending: false }),
  useUpdateDespesaVariavel: () => ({ mutate: vi.fn(), isPending: false }),
  useDeleteDespesaVariavel: () => ({ mutate: mockDeleteDespesaMutate, reset: vi.fn(), isPending: false, isError: false }),
}));

vi.mock('../../hooks/useContas', () => ({
  useContas: () => ({
    data: [{ id: 1, descricao: 'Nubank' }],
    isLoading: false,
  }),
}));

vi.mock('../../hooks/useCategorias', () => ({
  useCategorias: () => ({
    data: [{ id: 10, descricao: 'Alimentação' }],
    isLoading: false,
  }),
}));

const mockItems: DespesaVariavel[] = [
  {
    id: 1,
    descricao: 'Tênis Esportivo',
    localCompra: 'Centauro',
    dataCompra: '2026-05-10',
    valorParcela: 150,
    quantidadeParcelas: 4,
    dataInicio: '2026-05-01',
    dataFim: '2026-08-31',
    conta: { id: 1, descricao: 'Nubank', observacoes: '' },
    categoria: { id: 10, descricao: 'Alimentação' },
    observacoes: 'Parcelado 4x',
  },
  {
    id: 2,
    descricao: 'Livros Didáticos',
    localCompra: 'Amazon',
    dataCompra: '2026-01-15',
    valorParcela: 90,
    quantidadeParcelas: 2,
    dataInicio: '2026-01-01',
    dataFim: '2026-02-28',
    conta: { id: 1, descricao: 'Nubank', observacoes: '' },
    categoria: { id: 10, descricao: 'Alimentação' },
    observacoes: 'Compra antiga',
  },
];

describe('DespesasVariaveisPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useMonthStore.getState().setSelectedMonth('2026-05');
    useMonthStore.getState().setGlobalFilter(false);
    mockDespesasQuery = {
      data: mockItems,
      isLoading: false,
      isError: false,
    };
  });

  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
    useMonthStore.getState().setGlobalFilter(false);
    useMonthStore.getState().setSelectedMonth('2026-05');
  });

  it('exibe mensagem de carregamento quando isLoading for true', () => {
    mockDespesasQuery = {
      data: undefined,
      isLoading: true,
      isError: false,
    };

    render(<DespesasVariaveisPage />);

    expect(screen.getByText('Carregando despesas variáveis...')).toBeDefined();
    expect(screen.queryByRole('table')).toBeNull();
  });

  it('exibe mensagem de erro quando isError for true', () => {
    mockDespesasQuery = {
      data: undefined,
      isLoading: false,
      isError: true,
    };

    render(<DespesasVariaveisPage />);

    expect(screen.getByText('Erro ao carregar despesas variáveis.')).toBeDefined();
    expect(screen.queryByRole('table')).toBeNull();
  });

  it('renderiza EmptyState contextual quando lista estiver vazia para o mês selecionado', () => {
    mockDespesasQuery = {
      data: [],
      isLoading: false,
      isError: false,
    };

    render(<DespesasVariaveisPage />);

    expect(screen.getByRole('heading', { level: 3, name: 'Nenhuma despesa variável' })).toBeDefined();
    expect(screen.getByText('Nenhuma despesa para o mês selecionado.')).toBeDefined();
    expect(screen.getByRole('button', { name: 'Nova Despesa Variável' })).toBeDefined();
  });

  it('renderiza EmptyState com mensagem inicial quando Ver Todos estiver ativo e não houver dados', () => {
    useMonthStore.getState().setGlobalFilter(true);
    mockDespesasQuery = {
      data: [],
      isLoading: false,
      isError: false,
    };

    render(<DespesasVariaveisPage />);

    expect(screen.getByRole('heading', { level: 3, name: 'Nenhuma despesa variável' })).toBeDefined();
    expect(screen.getByText('Comece criando sua primeira despesa variável.')).toBeDefined();
  });

  it('filtra despesas pelo mês selecionado e exibe todas ao alternar o switch Ver Todos', () => {
    render(<DespesasVariaveisPage />);

    // Com filtro ativo (selectedMonth = 2026-05, isGlobalFilterActive = false):
    // "Tênis Esportivo" (vigência 2026-05 a 2026-08) deve aparecer.
    // "Livros Didáticos" (vigência 2026-01 a 2026-02) não deve aparecer.
    expect(screen.getByText('Tênis Esportivo')).toBeDefined();
    expect(screen.queryByText('Livros Didáticos')).toBeNull();

    // Alterna switch "Ver Todos"
    const checkbox = screen.getByLabelText('Ativar Filtro Global');
    fireEvent.click(checkbox);

    // O store atualiza isGlobalFilterActive para true
    expect(useMonthStore.getState().isGlobalFilterActive).toBe(true);

    // Agora ambas as despesas devem estar visíveis na tabela
    expect(screen.getByText('Tênis Esportivo')).toBeDefined();
    expect(screen.getByText('Livros Didáticos')).toBeDefined();

    // Ao desativar o switch novamente, volta a filtrar pelo mês selecionado
    fireEvent.click(checkbox);
    expect(useMonthStore.getState().isGlobalFilterActive).toBe(false);
    expect(screen.getByText('Tênis Esportivo')).toBeDefined();
    expect(screen.queryByText('Livros Didáticos')).toBeNull();
  });

  it('permite alternar switch Ver Todos com teclado (Enter e Espaço)', () => {
    render(<DespesasVariaveisPage />);

    const switchElement = screen.getByRole('switch');

    fireEvent.keyDown(switchElement, { key: 'Enter' });
    expect(useMonthStore.getState().isGlobalFilterActive).toBe(true);

    fireEvent.keyDown(switchElement, { key: ' ' });
    expect(useMonthStore.getState().isGlobalFilterActive).toBe(false);
  });

  it('abre o modal de criação ao clicar no botão "+ Nova Despesa"', () => {
    render(<DespesasVariaveisPage />);

    const createBtn = screen.getByRole('button', { name: '+ Nova Despesa' });
    fireEvent.click(createBtn);

    expect(screen.getByRole('dialog')).toBeDefined();
    expect(screen.getByRole('heading', { name: 'Nova Despesa Variável' })).toBeDefined();
  });

  it('abre o modal de exclusão ao clicar em Excluir no ActionMenu da linha', () => {
    render(<DespesasVariaveisPage />);

    const actionBtn = screen.getByRole('button', { name: 'Ações' });
    fireEvent.click(actionBtn);

    const deleteOption = screen.getByRole('button', { name: 'Excluir' });
    fireEvent.click(deleteOption);

    // O DeleteConfirmModal deve abrir com o título e nome do item
    const dialog = screen.getByRole('dialog');
    expect(dialog).toBeDefined();
    expect(screen.getByRole('heading', { name: 'Excluir Despesa Variável' })).toBeDefined();
    expect(within(dialog).getByText('Tênis Esportivo')).toBeDefined();
  });

  it('dispara mutação de exclusão ao confirmar a exclusão no modal', () => {
    render(<DespesasVariaveisPage />);

    const actionBtn = screen.getByRole('button', { name: 'Ações' });
    fireEvent.click(actionBtn);

    const deleteOption = screen.getByRole('button', { name: 'Excluir' });
    fireEvent.click(deleteOption);

    const dialog = screen.getByRole('dialog');
    const confirmDeleteBtn = within(dialog).getByRole('button', { name: 'Excluir' });
    fireEvent.click(confirmDeleteBtn);

    expect(mockDeleteDespesaMutate).toHaveBeenCalledWith(1, expect.any(Object));
  });

  it('abre o modal de edição ao clicar em Editar no ActionMenu da linha', () => {
    render(<DespesasVariaveisPage />);

    const actionBtn = screen.getByRole('button', { name: 'Ações' });
    fireEvent.click(actionBtn);

    const editOption = screen.getByRole('button', { name: 'Editar' });
    fireEvent.click(editOption);

    // O DespesaVariavelFormModal deve abrir em modo edição
    expect(screen.getByRole('dialog')).toBeDefined();
    expect(screen.getByRole('heading', { name: 'Editar Despesa Variável' })).toBeDefined();
    expect(screen.getByDisplayValue('Tênis Esportivo')).toBeDefined();
  });

  it('reage à alteração de selectedMonth filtrando itens dinamicamente incluindo limites de vigência', () => {
    const { rerender } = render(<DespesasVariaveisPage />);

    expect(screen.getByText('Tênis Esportivo')).toBeDefined();
    expect(screen.queryByText('Livros Didáticos')).toBeNull();

    // Altera o mês selecionado no store Zustand para 2026-01 (período de Livros Didáticos)
    useMonthStore.getState().setSelectedMonth('2026-01');
    rerender(<DespesasVariaveisPage />);

    expect(screen.queryByText('Tênis Esportivo')).toBeNull();
    expect(screen.getByText('Livros Didáticos')).toBeDefined();

    // Testa limite de vigência de Tênis Esportivo: 2026-08 (mês final de vigência - deve aparecer)
    useMonthStore.getState().setSelectedMonth('2026-08');
    rerender(<DespesasVariaveisPage />);
    expect(screen.getByText('Tênis Esportivo')).toBeDefined();

    // Testa mês imediatamente seguinte: 2026-09 (fora da vigência - não deve aparecer)
    useMonthStore.getState().setSelectedMonth('2026-09');
    rerender(<DespesasVariaveisPage />);
    expect(screen.queryByText('Tênis Esportivo')).toBeNull();
  });

  it('renderiza colunas formatadas de valores, datas e parcelas na tabela', () => {
    render(<DespesasVariaveisPage />);

    // Valor da parcela R$ 150,00 e valor total 150 * 4 = R$ 600,00
    expect(screen.getByText('R$ 150,00')).toBeDefined();
    expect(screen.getByText('R$ 600,00')).toBeDefined();
    expect(screen.getByText('Centauro')).toBeDefined();
    expect(screen.getByText('Parcelado 4x')).toBeDefined();

    // Datas formatadas
    expect(screen.getByText('10/05/2026')).toBeDefined();
    expect(screen.getByText('05/2026')).toBeDefined();
    expect(screen.getByText('08/2026')).toBeDefined();
  });
});
