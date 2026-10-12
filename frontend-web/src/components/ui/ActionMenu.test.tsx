import React from 'react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import ActionMenu from './ActionMenu';

describe('ActionMenu component', () => {
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  it('renderiza o botão gatilho com título "Ações" e menu fechado por padrão', () => {
    render(<ActionMenu onEdit={vi.fn()} onDelete={vi.fn()} />);

    const button = screen.getByRole('button', { name: 'Ações' });
    expect(button).toBeDefined();
    expect(screen.queryByRole('button', { name: 'Editar' })).toBeNull();
    expect(screen.queryByRole('button', { name: 'Excluir' })).toBeNull();
  });

  it('abre o menu no portal ao clicar no botão gatilho exibindo Editar e Excluir', () => {
    render(<ActionMenu onEdit={vi.fn()} onDelete={vi.fn()} />);

    const triggerBtn = screen.getByRole('button', { name: 'Ações' });
    fireEvent.click(triggerBtn);

    const editBtn = screen.getByRole('button', { name: 'Editar' });
    const deleteBtn = screen.getByRole('button', { name: 'Excluir' });

    expect(editBtn).toBeDefined();
    expect(deleteBtn).toBeDefined();
  });

  it('aciona onEdit e fecha o menu ao clicar em Editar', () => {
    const handleEdit = vi.fn();
    const handleDelete = vi.fn();

    render(<ActionMenu onEdit={handleEdit} onDelete={handleDelete} />);

    fireEvent.click(screen.getByRole('button', { name: 'Ações' }));
    const editBtn = screen.getByRole('button', { name: 'Editar' });

    fireEvent.click(editBtn);

    expect(handleEdit).toHaveBeenCalledTimes(1);
    expect(handleDelete).not.toHaveBeenCalled();
    expect(screen.queryByRole('button', { name: 'Editar' })).toBeNull();
  });

  it('aciona onDelete e fecha o menu ao clicar em Excluir', () => {
    const handleEdit = vi.fn();
    const handleDelete = vi.fn();

    render(<ActionMenu onEdit={handleEdit} onDelete={handleDelete} />);

    fireEvent.click(screen.getByRole('button', { name: 'Ações' }));
    const deleteBtn = screen.getByRole('button', { name: 'Excluir' });

    fireEvent.click(deleteBtn);

    expect(handleDelete).toHaveBeenCalledTimes(1);
    expect(handleEdit).not.toHaveBeenCalled();
    expect(screen.queryByRole('button', { name: 'Excluir' })).toBeNull();
  });

  it('fecha o menu ao clicar no botão gatilho novamente (toggle)', () => {
    render(<ActionMenu onEdit={vi.fn()} onDelete={vi.fn()} />);

    const triggerBtn = screen.getByRole('button', { name: 'Ações' });
    fireEvent.click(triggerBtn);
    expect(screen.getByRole('button', { name: 'Editar' })).toBeDefined();

    fireEvent.click(triggerBtn);
    expect(screen.queryByRole('button', { name: 'Editar' })).toBeNull();
  });

  it('fecha o menu ao clicar fora (mousedown em elemento externo)', () => {
    render(
      <div>
        <div data-testid="outside-element">Fora</div>
        <ActionMenu onEdit={vi.fn()} onDelete={vi.fn()} />
      </div>
    );

    fireEvent.click(screen.getByRole('button', { name: 'Ações' }));
    expect(screen.getByRole('button', { name: 'Editar' })).toBeDefined();

    // Dispara mousedown fora do menu
    const outside = screen.getByTestId('outside-element');
    fireEvent.mouseDown(outside);

    expect(screen.queryByRole('button', { name: 'Editar' })).toBeNull();
  });
});
