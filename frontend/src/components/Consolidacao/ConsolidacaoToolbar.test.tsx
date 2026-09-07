import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
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
});
