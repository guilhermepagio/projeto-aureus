import React from 'react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, cleanup, within } from '@testing-library/react';
import ReceitasVariaveisPage from './ReceitasVariaveisPage';
import { useMonthStore } from '../../store/monthStore';
import type { ReceitaVariavel } from '../../hooks/useReceitasVariaveis';

let mockReceitasQuery = {
  data: [] as ReceitaVariavel[] | undefined,
  isLoading: false,
  isError: false,
};

const mockDeleteReceitaMutate = vi.fn();
vi.mock('../../hooks/useReceitasVariaveis', () => ({
  useReceitasVariaveis: () => mockReceitasQuery,
  useCreateReceitaVariavel: () => ({ mutate: vi.fn(), isPending: false }),
  useUpdateReceitaVariavel: () => ({ mutate: vi.fn(), isPending: false }),
  useDeleteReceitaVariavel: () => ({ mutate: mockDeleteReceitaMutate, reset: vi.fn(), isPending: false, isError: false }),
}));

vi.mock('../../hooks/useContas', () => ({
  useContas: () => ({
    data: [{ id: 1, descricao: 'Nubank' }],
    isLoading: false,
  }),
}));

vi.mock('../../hooks/useCategorias', () => ({
  useCategorias: () => ({
    data: [{ id: 10, descricao: 'Salário' }],
    isLoading: false,
  }),
}));

const mockItems: ReceitaVariavel[] = [
  {
    id: 1,
    descricao: 'Freelance Frontend',
    valorParcela: 1200,
    quantidadeParcelas: 3,
    dataInicio: '2026-05-01',
    dataFim: '2026-07-31',
    conta: { id: 1, descricao: 'Nubank', observacoes: '' },
    categoria: { id: 10, descricao: 'Salário' },
    observacoes: 'Contrato 3 meses',
  },
  {
    id: 2,
    descricao: 'Bônus Anual',
    valorParcela: 5000,
    quantidadeParcelas: 1,
    dataInicio: '2026-12-01',
    dataFim: '2026-12-31',
    conta: { id: 1, descricao: 'Nubank', observacoes: '' },
    categoria: { id: 10, descricao: 'Salário' },
    observacoes: 'Final de ano',
  },
];

describe('ReceitasVariaveisPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useMonthStore.getState().setSelectedMonth('2026-05');
    useMonthStore.getState().setGlobalFilter(false);
    mockReceitasQuery = {
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
    mockReceitasQuery = {
      data: undefined,
      isLoading: true,
      isError: false,
    };

    render(<ReceitasVariaveisPage />);

    expect(screen.getByText('Carregando receitas variáveis...')).toBeDefined();
    expect(screen.queryByRole('table')).toBeNull();
  });

  it('exibe mensagem de erro quando isError for true', () => {
    mockReceitasQuery = {
      data: undefined,
      isLoading: false,
      isError: true,
    };

    render(<ReceitasVariaveisPage />);

    expect(screen.getByText('Erro ao carregar receitas variáveis.')).toBeDefined();
    expect(screen.queryByRole('table')).toBeNull();
  });

  it('renderiza EmptyState contextual quando a lista de receitas estiver vazia no mês selecionado', () => {
    mockReceitasQuery = {
      data: [],
      isLoading: false,
      isError: false,
    };

    render(<ReceitasVariaveisPage />);

    expect(screen.getByRole('heading', { level: 3, name: 'Nenhuma receita variável' })).toBeDefined();
    expect(screen.getByText('Nenhuma receita para o mês selecionado.')).toBeDefined();
    expect(screen.getByRole('button', { name: 'Nova Receita Variável' })).toBeDefined();
  });

  it('renderiza EmptyState com mensagem de boas-vindas quando Ver Todos estiver ativo e lista vazia', () => {
    useMonthStore.getState().setGlobalFilter(true);
    mockReceitasQuery = {
      data: [],
      isLoading: false,
      isError: false,
    };

    render(<ReceitasVariaveisPage />);

    expect(screen.getByRole('heading', { level: 3, name: 'Nenhuma receita variável' })).toBeDefined();
    expect(screen.getByText('Comece criando sua primeira receita variável.')).toBeDefined();
  });

  it('filtra receitas por mês de vigência e exibe todas ao alternar switch Ver Todos', () => {
    render(<ReceitasVariaveisPage />);

    // Com filtro ativo no mês 2026-05:
    // "Freelance Frontend" (vigência 2026-05 a 2026-07) deve aparecer.
    // "Bônus Anual" (vigência 2026-12 a 2026-12) não deve aparecer.
    expect(screen.getByText('Freelance Frontend')).toBeDefined();
    expect(screen.queryByText('Bônus Anual')).toBeNull();

    // Alterna switch "Ver Todos"
    const checkbox = screen.getByLabelText('Ativar Filtro Global');
    fireEvent.click(checkbox);

    expect(useMonthStore.getState().isGlobalFilterActive).toBe(true);

    // Ambas as receitas devem ser exibidas
    expect(screen.getByText('Freelance Frontend')).toBeDefined();
    expect(screen.getByText('Bônus Anual')).toBeDefined();

    // Desativa switch "Ver Todos"
    fireEvent.click(checkbox);
    expect(useMonthStore.getState().isGlobalFilterActive).toBe(false);
    expect(screen.getByText('Freelance Frontend')).toBeDefined();
    expect(screen.queryByText('Bônus Anual')).toBeNull();
  });

  it('permite alternar switch Ver Todos com teclas Enter e Espaço', () => {
    render(<ReceitasVariaveisPage />);

    const switchElement = screen.getByRole('switch');

    fireEvent.keyDown(switchElement, { key: 'Enter' });
    expect(useMonthStore.getState().isGlobalFilterActive).toBe(true);

    fireEvent.keyDown(switchElement, { key: ' ' });
    expect(useMonthStore.getState().isGlobalFilterActive).toBe(false);
  });

  it('abre o modal de criação ao clicar no botão "+ Nova Receita"', () => {
    render(<ReceitasVariaveisPage />);

    const createBtn = screen.getByRole('button', { name: '+ Nova Receita' });
    fireEvent.click(createBtn);

    expect(screen.getByRole('dialog')).toBeDefined();
    expect(screen.getByRole('heading', { name: 'Nova Receita Variável' })).toBeDefined();
  });

  it('abre o modal de exclusão ao clicar em Excluir no ActionMenu da linha', () => {
    render(<ReceitasVariaveisPage />);

    const actionBtn = screen.getByRole('button', { name: 'Ações' });
    fireEvent.click(actionBtn);

    const deleteOption = screen.getByRole('button', { name: 'Excluir' });
    fireEvent.click(deleteOption);

    const dialog = screen.getByRole('dialog');
    expect(dialog).toBeDefined();
    expect(screen.getByRole('heading', { name: 'Excluir Receita Variável' })).toBeDefined();
    expect(within(dialog).getByText('Freelance Frontend')).toBeDefined();
  });

  it('dispara mutação de exclusão ao confirmar a exclusão no modal', () => {
    render(<ReceitasVariaveisPage />);

    const actionBtn = screen.getByRole('button', { name: 'Ações' });
    fireEvent.click(actionBtn);

    const deleteOption = screen.getByRole('button', { name: 'Excluir' });
    fireEvent.click(deleteOption);

    const dialog = screen.getByRole('dialog');
    const confirmDeleteBtn = within(dialog).getByRole('button', { name: 'Excluir' });
    fireEvent.click(confirmDeleteBtn);

    expect(mockDeleteReceitaMutate).toHaveBeenCalledWith(1, expect.any(Object));
  });

  it('abre o modal de edição ao clicar em Editar no ActionMenu da linha', () => {
    render(<ReceitasVariaveisPage />);

    const actionBtn = screen.getByRole('button', { name: 'Ações' });
    fireEvent.click(actionBtn);

    const editOption = screen.getByRole('button', { name: 'Editar' });
    fireEvent.click(editOption);

    expect(screen.getByRole('dialog')).toBeDefined();
    expect(screen.getByRole('heading', { name: 'Editar Receita Variável' })).toBeDefined();
    expect(screen.getByDisplayValue('Freelance Frontend')).toBeDefined();
  });

  it('reage à alteração de selectedMonth filtrando receitas dinamicamente', () => {
    const { rerender } = render(<ReceitasVariaveisPage />);

    expect(screen.getByText('Freelance Frontend')).toBeDefined();
    expect(screen.queryByText('Bônus Anual')).toBeNull();

    // Altera o mês selecionado para 2026-12 (período de vigência do Bônus Anual)
    useMonthStore.getState().setSelectedMonth('2026-12');
    rerender(<ReceitasVariaveisPage />);

    expect(screen.queryByText('Freelance Frontend')).toBeNull();
    expect(screen.getByText('Bônus Anual')).toBeDefined();
  });

  it('renderiza colunas formatadas de valores e parcelas na tabela de receitas', () => {
    render(<ReceitasVariaveisPage />);

    // Valor da parcela R$ 1.200,00 e total 1.200 * 3 = R$ 3.600,00
    expect(screen.getByText('R$ 1.200,00')).toBeDefined();
    expect(screen.getByText('R$ 3.600,00')).toBeDefined();
    expect(screen.getByText('Contrato 3 meses')).toBeDefined();
  });
});
