import React from 'react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import DeleteConfirmModal from './DeleteConfirmModal';
import type { DespesaVariavel } from '../../../hooks/useDespesasVariaveis';

const mockMutate = vi.fn();
const mockReset = vi.fn();
let mockHookReturn = {
  mutate: mockMutate,
  reset: mockReset,
  isPending: false,
  isError: false,
  error: null as Error | null,
};

vi.mock('../../../hooks/useDespesasVariaveis', () => ({
  useDeleteDespesaVariavel: () => mockHookReturn,
}));

const mockDespesa: DespesaVariavel = {
  id: 101,
  descricao: 'Notebook Gamer',
  valorParcela: 500,
  quantidadeParcelas: 10,
  dataInicio: '2026-05-01',
  dataFim: '2027-02-28',
  conta: { id: 1, descricao: 'Nubank', observacoes: '' },
  categoria: { id: 2, descricao: 'Eletrônicos' },
};

describe('DeleteConfirmModal (Despesas Variáveis)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockHookReturn = {
      mutate: mockMutate,
      reset: mockReset,
      isPending: false,
      isError: false,
      error: null,
    };
  });

  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  it('não renderiza o modal quando isOpen é false', () => {
    render(
      <DeleteConfirmModal
        isOpen={false}
        onClose={vi.fn()}
        despesaToDelete={mockDespesa}
      />
    );

    expect(screen.queryByRole('dialog')).toBeNull();
  });

  it('renderiza o diálogo com nome do item a ser excluído e reseta mutação ao abrir', () => {
    render(
      <DeleteConfirmModal
        isOpen={true}
        onClose={vi.fn()}
        despesaToDelete={mockDespesa}
      />
    );

    expect(screen.getByRole('dialog')).toBeDefined();
    expect(screen.getByRole('heading', { name: 'Excluir Despesa Variável' })).toBeDefined();
    expect(screen.getByText('Notebook Gamer')).toBeDefined();
    expect(mockReset).toHaveBeenCalledTimes(1);
  });

  it('fecha o modal via onClose ao clicar em Cancelar sem disparar mutação', () => {
    const handleClose = vi.fn();
    render(
      <DeleteConfirmModal
        isOpen={true}
        onClose={handleClose}
        despesaToDelete={mockDespesa}
      />
    );

    const cancelBtn = screen.getByRole('button', { name: 'Cancelar' });
    fireEvent.click(cancelBtn);

    expect(handleClose).toHaveBeenCalledTimes(1);
    expect(mockMutate).not.toHaveBeenCalled();
  });

  it('dispara mutação com id do item ao confirmar e fecha no onSuccess', () => {
    const handleClose = vi.fn();
    render(
      <DeleteConfirmModal
        isOpen={true}
        onClose={handleClose}
        despesaToDelete={mockDespesa}
      />
    );

    const deleteBtn = screen.getByRole('button', { name: 'Excluir' });
    fireEvent.click(deleteBtn);

    expect(mockMutate).toHaveBeenCalledTimes(1);
    expect(mockMutate).toHaveBeenCalledWith(101, {
      onSuccess: expect.any(Function),
    });

    // Dispara o callback onSuccess registrado na mutação
    const mutationCall = mockMutate.mock.calls[0];
    mutationCall[1].onSuccess();

    expect(handleClose).toHaveBeenCalledTimes(1);
  });

  it('exibe estado de loading "Excluindo...", desabilita botões e bloqueia fechamento quando isPending for true', () => {
    const handleClose = vi.fn();
    mockHookReturn = {
      ...mockHookReturn,
      isPending: true,
    };

    render(
      <DeleteConfirmModal
        isOpen={true}
        onClose={handleClose}
        despesaToDelete={mockDespesa}
      />
    );

    const deletingBtn = screen.getByRole('button', { name: 'Excluindo...' });
    const cancelBtn = screen.getByRole('button', { name: 'Cancelar' });

    expect(deletingBtn).toBeDefined();
    expect(deletingBtn.hasAttribute('disabled')).toBe(true);
    expect(cancelBtn.hasAttribute('disabled')).toBe(true);

    // Tentativa de fechar via ESC deve ser bloqueada por disableClose={isPending}
    fireEvent.keyDown(document, { key: 'Escape' });
    expect(handleClose).not.toHaveBeenCalled();
  });

  it('exibe mensagem de erro quando a mutação falhar (isError=true)', () => {
    mockHookReturn = {
      ...mockHookReturn,
      isError: true,
      error: new Error('Não foi possível excluir o registro. Tente novamente.'),
    };

    render(
      <DeleteConfirmModal
        isOpen={true}
        onClose={vi.fn()}
        despesaToDelete={mockDespesa}
      />
    );

    expect(screen.getByText('Não foi possível excluir o registro. Tente novamente.')).toBeDefined();
  });

  it('não dispara mutate ao clicar em Excluir se despesaToDelete for null', () => {
    render(
      <DeleteConfirmModal
        isOpen={true}
        onClose={vi.fn()}
        despesaToDelete={null}
      />
    );

    const deleteBtn = screen.getByRole('button', { name: 'Excluir' });
    fireEvent.click(deleteBtn);

    expect(mockMutate).not.toHaveBeenCalled();
  });
});
