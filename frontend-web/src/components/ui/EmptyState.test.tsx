import React from 'react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import EmptyState from './EmptyState';

describe('EmptyState component', () => {
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  it('renderiza título e descrição informados', () => {
    render(
      <EmptyState
        title="Nenhum registro encontrado"
        description="Crie seu primeiro registro para começar a gerenciar seus dados."
      />
    );

    expect(screen.getByRole('heading', { level: 3, name: 'Nenhum registro encontrado' })).toBeDefined();
    expect(screen.getByText('Crie seu primeiro registro para começar a gerenciar seus dados.')).toBeDefined();
  });

  it('renderiza ícone SVG padrão quando nenhuma prop icon é informada', () => {
    const { container } = render(
      <EmptyState
        title="Sem dados"
        description="Nenhum item na lista."
      />
    );

    const svg = container.querySelector('svg');
    expect(svg).not.toBeNull();
    expect(svg?.getAttribute('aria-hidden')).toBe('true');
  });

  it('renderiza ícone customizado quando prop icon é fornecida', () => {
    render(
      <EmptyState
        title="Com ícone"
        description="Descrição qualquer"
        icon={<span data-testid="custom-icon">★</span>}
      />
    );

    expect(screen.getByTestId('custom-icon')).toBeDefined();
    expect(screen.getByText('★')).toBeDefined();
  });

  it('não renderiza botão de ação quando a prop action for omitida', () => {
    render(
      <EmptyState
        title="Sem ação"
        description="Apenas visualização."
      />
    );

    expect(screen.queryByRole('button')).toBeNull();
  });

  it('renderiza botão de ação com label e dispara onClick ao ser clicado', () => {
    const handleClick = vi.fn();
    render(
      <EmptyState
        title="Com ação"
        description="Clique para adicionar."
        action={{
          label: 'Adicionar Item',
          onClick: handleClick,
        }}
      />
    );

    const button = screen.getByRole('button', { name: 'Adicionar Item' });
    expect(button).toBeDefined();

    fireEvent.click(button);
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('aplica as classes de estilo para variante padrão (primary)', () => {
    render(
      <EmptyState
        title="Tema Primário"
        description="Teste de estilo"
        action={{
          label: 'Ação Primária',
          onClick: vi.fn(),
          color: 'primary',
        }}
      />
    );

    const button = screen.getByRole('button', { name: 'Ação Primária' });
    expect(button.className).toContain('bg-primary');
    expect(button.className).toContain('hover:bg-primary-light');
  });

  it('aplica classes de cor padrão (primary) quando prop color for omitida', () => {
    render(
      <EmptyState
        title="Sem Cor Definida"
        description="Fallback para primary"
        action={{
          label: 'Ação Padrão',
          onClick: vi.fn(),
        }}
      />
    );

    const button = screen.getByRole('button', { name: 'Ação Padrão' });
    expect(button.className).toContain('bg-primary');
    expect(button.className).toContain('hover:bg-primary-light');
  });

  it('aplica classes de cor para variante danger', () => {
    render(
      <EmptyState
        title="Tema Danger"
        description="Teste de estilo danger"
        action={{
          label: 'Excluir / Nova Despesa',
          onClick: vi.fn(),
          color: 'danger',
        }}
      />
    );

    const button = screen.getByRole('button', { name: 'Excluir / Nova Despesa' });
    expect(button.className).toContain('bg-red-600');
    expect(button.className).toContain('hover:bg-red-700');
  });

  it('aplica classes de cor para variante blue', () => {
    render(
      <EmptyState
        title="Tema Azul"
        description="Teste de estilo blue"
        action={{
          label: 'Nova Conta',
          onClick: vi.fn(),
          color: 'blue',
        }}
      />
    );

    const button = screen.getByRole('button', { name: 'Nova Conta' });
    expect(button.className).toContain('bg-blue-600');
    expect(button.className).toContain('hover:bg-blue-700');
  });
});
