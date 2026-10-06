import React from 'react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, cleanup, within } from '@testing-library/react';
import ContasPage from './ContasPage';
import type { Conta } from '../../hooks/useContas';

let mockContasQuery = {
  data: [] as Conta[] | undefined,
  isLoading: false,
  isError: false,
};

const mockDeleteContaMutate = vi.fn();
vi.mock('../../hooks/useContas', () => ({
  useContas: () => mockContasQuery,
  useCreateConta: () => ({ mutate: vi.fn(), isPending: false }),
  useUpdateConta: () => ({ mutate: vi.fn(), isPending: false }),
  useDeleteConta: () => ({ mutate: mockDeleteContaMutate, isPending: false }),
}));

const mockContas: Conta[] = [
  { id: 1, descricao: 'Carteira', observacoes: 'Dinheiro em espécie' },
  { id: 2, descricao: 'NuBank', observacoes: 'Conta digital principal' },
];

describe('ContasPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockContasQuery = {
      data: mockContas,
      isLoading: false,
      isError: false,
    };
  });

  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  it('exibe mensagem de carregamento quando isLoading for true', () => {
    mockContasQuery = {
      data: undefined,
      isLoading: true,
      isError: false,
    };

    render(<ContasPage />);

    expect(screen.getByText('Carregando contas...')).toBeDefined();
    expect(screen.queryByRole('table')).toBeNull();
  });

  it('exibe mensagem de erro quando isError for true', () => {
    mockContasQuery = {
      data: undefined,
      isLoading: false,
      isError: true,
    };

    render(<ContasPage />);

    expect(screen.getByText('Erro ao carregar contas.')).toBeDefined();
    expect(screen.queryByRole('table')).toBeNull();
  });

  it('renderiza EmptyState com botão "Nova Conta" quando a lista estiver vazia', () => {
    mockContasQuery = {
      data: [],
      isLoading: false,
      isError: false,
    };

    render(<ContasPage />);

    expect(screen.getByRole('heading', { level: 3, name: 'Nenhuma conta' })).toBeDefined();
    expect(screen.getByText('Comece criando sua primeira conta.')).toBeDefined();
    expect(screen.getByRole('button', { name: 'Nova Conta' })).toBeDefined();
  });

  it('abre o modal de criação ao clicar no botão "Nova Conta" do EmptyState', () => {
    mockContasQuery = {
      data: [],
      isLoading: false,
      isError: false,
    };

    render(<ContasPage />);

    const emptyStateActionBtn = screen.getByRole('button', { name: 'Nova Conta' });
    fireEvent.click(emptyStateActionBtn);

    expect(screen.getByRole('dialog')).toBeDefined();
    expect(screen.getByRole('heading', { name: 'Nova Conta' })).toBeDefined();
  });

  it('renderiza a tabela de contas com suas descrições e observações', () => {
    render(<ContasPage />);

    expect(screen.getByRole('table')).toBeDefined();
    expect(screen.getByText('Carteira')).toBeDefined();
    expect(screen.getByText('Dinheiro em espécie')).toBeDefined();
    expect(screen.getByText('NuBank')).toBeDefined();
    expect(screen.getByText('Conta digital principal')).toBeDefined();
  });

  it('abre o modal de criação ao clicar no botão "+ Nova Conta" do cabeçalho', () => {
    render(<ContasPage />);

    const newBtn = screen.getByRole('button', { name: '+ Nova Conta' });
    fireEvent.click(newBtn);

    expect(screen.getByRole('dialog')).toBeDefined();
    expect(screen.getByRole('heading', { name: 'Nova Conta' })).toBeDefined();
  });

  it('abre o modal de edição com dados pré-preenchidos ao clicar em Editar no ActionMenu', () => {
    render(<ContasPage />);

    const actionBtns = screen.getAllByRole('button', { name: 'Ações' });
    fireEvent.click(actionBtns[0]);

    const editBtn = screen.getByRole('button', { name: 'Editar' });
    fireEvent.click(editBtn);

    expect(screen.getByRole('dialog')).toBeDefined();
    expect(screen.getByRole('heading', { name: 'Editar Conta' })).toBeDefined();
    expect(screen.getByDisplayValue('Carteira')).toBeDefined();
    expect(screen.getByDisplayValue('Dinheiro em espécie')).toBeDefined();
  });

  it('abre o modal de exclusão ao clicar em Excluir no ActionMenu', () => {
    render(<ContasPage />);

    const actionBtns = screen.getAllByRole('button', { name: 'Ações' });
    fireEvent.click(actionBtns[0]);

    const deleteBtn = screen.getByRole('button', { name: 'Excluir' });
    fireEvent.click(deleteBtn);

    const dialog = screen.getByRole('dialog');
    expect(dialog).toBeDefined();
    expect(screen.getByRole('heading', { name: 'Excluir Conta' })).toBeDefined();
    expect(within(dialog).getByText('Carteira')).toBeDefined();
  });

  it('dispara mutação de exclusão ao confirmar a exclusão no modal', () => {
    render(<ContasPage />);

    const actionBtns = screen.getAllByRole('button', { name: 'Ações' });
    fireEvent.click(actionBtns[0]);

    const menuDeleteBtn = screen.getByRole('button', { name: 'Excluir' });
    fireEvent.click(menuDeleteBtn);

    const dialog = screen.getByRole('dialog');
    const confirmDeleteBtn = within(dialog).getByRole('button', { name: 'Excluir' });
    fireEvent.click(confirmDeleteBtn);

    expect(mockDeleteContaMutate).toHaveBeenCalledWith(1, expect.any(Object));
  });
});
