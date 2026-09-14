import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import MonthPicker from './MonthPicker';

describe('MonthPicker', () => {
  afterEach(() => {
    cleanup();
  });

  it('renderiza o placeholder quando value estiver vazio', () => {
    render(<MonthPicker value="" onChange={vi.fn()} placeholder="Selecione o mês" />);
    expect(screen.getByText('Selecione o mês')).toBeDefined();
  });

  it('renderiza o mês e ano formatados quando value for fornecido', () => {
    render(<MonthPicker value="2026-09" onChange={vi.fn()} />);
    expect(screen.getByText('Set 2026 (09/2026)')).toBeDefined();
  });

  it('abre o popover, navega pelos anos e chama onChange ao clicar em um mês', () => {
    const onChange = vi.fn();
    render(<MonthPicker value="2026-05" onChange={onChange} theme="red" />);

    // Clica para abrir o popover
    const triggerBtn = screen.getByRole('button', { name: /Mai 2026/ });
    fireEvent.click(triggerBtn);

    expect(screen.getByRole('dialog', { name: 'Seletor de Mês e Ano' })).toBeDefined();
    expect(screen.getByText('2026')).toBeDefined();

    // Navega para 2027
    const nextYearBtn = screen.getByRole('button', { name: 'Próximo ano' });
    fireEvent.click(nextYearBtn);
    expect(screen.getByText('2027')).toBeDefined();

    // Seleciona Outubro (Out)
    const outBtn = screen.getByRole('button', { name: 'Out' });
    fireEvent.click(outBtn);

    expect(onChange).toHaveBeenCalledWith('2027-10');
    // Popover deve fechar após seleção
    expect(screen.queryByRole('dialog', { name: 'Seletor de Mês e Ano' })).toBeNull();
  });

  it('fecha o popover com Escape', () => {
    render(<MonthPicker value="2026-05" onChange={vi.fn()} />);
    const triggerBtn = screen.getByRole('button', { name: /Mai 2026/ });
    fireEvent.click(triggerBtn);
    expect(screen.getByRole('dialog', { name: 'Seletor de Mês e Ano' })).toBeDefined();

    fireEvent.keyDown(document, { key: 'Escape' });
    expect(screen.queryByRole('dialog', { name: 'Seletor de Mês e Ano' })).toBeNull();
  });

  it('não abre quando estiver desabilitado', () => {
    render(<MonthPicker value="2026-05" onChange={vi.fn()} disabled={true} />);
    const triggerBtn = screen.getByRole('button', { name: /Mai 2026/ });
    fireEvent.click(triggerBtn);
    expect(screen.queryByRole('dialog', { name: 'Seletor de Mês e Ano' })).toBeNull();
  });
});
