import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import ConsolidacaoToolbar from './ConsolidacaoToolbar';
import { useMonthStore } from '../../store/monthStore';

describe('ConsolidacaoToolbar', () => {
  afterEach(() => {
    cleanup();
  });

  beforeEach(() => {
    useMonthStore.getState().setSelectedMonth('2026-08');
  });

  it('renderiza o título Consolidação, o seletor de mês e o botão Mês Atual', () => {
    render(<ConsolidacaoToolbar />);

    expect(screen.getByRole('heading', { level: 1, name: 'Consolidação' })).toBeDefined();
    expect(screen.getByText('Ago 2026')).toBeDefined();
    expect(screen.getByRole('button', { name: 'Mês Atual' })).toBeDefined();
    expect(screen.getByRole('button', { name: 'Mês anterior' })).toBeDefined();
    expect(screen.getByRole('button', { name: 'Próximo mês' })).toBeDefined();
  });

  it('avança e retrocede meses corretamente', () => {
    render(<ConsolidacaoToolbar />);

    const nextBtn = screen.getByRole('button', { name: 'Próximo mês' });
    fireEvent.click(nextBtn);
    expect(useMonthStore.getState().selectedMonth).toBe('2026-09');

    const prevBtn = screen.getByRole('button', { name: 'Mês anterior' });
    fireEvent.click(prevBtn);
    expect(useMonthStore.getState().selectedMonth).toBe('2026-08');
  });

  it('redefine para o mês atual ao clicar no botão Mês Atual', () => {
    useMonthStore.getState().setSelectedMonth('2025-01');
    render(<ConsolidacaoToolbar />);

    const btnAtual = screen.getByRole('button', { name: 'Mês Atual' });
    fireEvent.click(btnAtual);

    const now = new Date();
    const expected = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    expect(useMonthStore.getState().selectedMonth).toBe(expected);
  });

  it('chama onResetScroll quando fornecido e o botão Mês Atual é clicado', () => {
    const onResetScroll = vi.fn();
    render(<ConsolidacaoToolbar onResetScroll={onResetScroll} />);

    const btnAtual = screen.getByRole('button', { name: 'Mês Atual' });
    fireEvent.click(btnAtual);

    expect(onResetScroll).toHaveBeenCalledTimes(1);
  });

  it('executa scrollTo suave no elemento #consolidacao-scroll-container por fallback', () => {
    const container = document.createElement('div');
    container.id = 'consolidacao-scroll-container';
    container.scrollTo = vi.fn();
    document.body.appendChild(container);

    render(<ConsolidacaoToolbar />);

    const btnAtual = screen.getByRole('button', { name: 'Mês Atual' });
    fireEvent.click(btnAtual);

    expect(container.scrollTo).toHaveBeenCalledWith({ left: 0, behavior: 'smooth' });
    document.body.removeChild(container);
  });

  it('renderiza a chave seletora (on/off) para Ocultar tudo quando onToggleAll é fornecido', () => {
    const onToggleAll = vi.fn();
    const { rerender } = render(
      <ConsolidacaoToolbar
        isAllCollapsed={false}
        onToggleAll={onToggleAll}
      />
    );

    const switchBtn = screen.getByRole('switch', { name: 'Ocultar tudo' });
    expect(switchBtn).toBeDefined();
    expect(switchBtn.getAttribute('aria-checked')).toBe('false');

    fireEvent.click(switchBtn);
    expect(onToggleAll).toHaveBeenCalledTimes(1);

    // Rerender com estado ativo
    rerender(
      <ConsolidacaoToolbar
        isAllCollapsed={true}
        onToggleAll={onToggleAll}
      />
    );
    expect(switchBtn.getAttribute('aria-checked')).toBe('true');
  });
});
