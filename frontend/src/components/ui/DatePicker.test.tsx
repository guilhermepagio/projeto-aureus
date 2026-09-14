import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import DatePicker from './DatePicker';

describe('DatePicker', () => {
  afterEach(() => {
    cleanup();
  });

  it('renderiza placeholder quando value estiver vazio', () => {
    render(<DatePicker value="" onChange={vi.fn()} placeholder="DD/MM/AAAA" />);
    expect(screen.getByText('DD/MM/AAAA')).toBeDefined();
  });

  it('renderiza a data formatada como DD/MM/AAAA quando value for fornecido', () => {
    render(<DatePicker value="2026-09-13" onChange={vi.fn()} />);
    expect(screen.getByText('13/09/2026')).toBeDefined();
  });

  it('abre o popover, navega pelos meses e seleciona um dia', () => {
    const onChange = vi.fn();
    render(<DatePicker value="2026-08-15" onChange={onChange} theme="red" />);

    // Abre popover
    const triggerBtn = screen.getByRole('button', { name: /15\/08\/2026/ });
    fireEvent.click(triggerBtn);

    expect(screen.getByRole('dialog', { name: 'Seletor de Data' })).toBeDefined();
    expect(screen.getByText('Ago 2026')).toBeDefined();

    // Avança para Setembro
    const nextBtn = screen.getByRole('button', { name: 'Próximo mês' });
    fireEvent.click(nextBtn);
    expect(screen.getByText('Set 2026')).toBeDefined();

    // Clica no dia 20
    const day20 = screen.getByRole('button', { name: '20' });
    fireEvent.click(day20);

    expect(onChange).toHaveBeenCalledWith('2026-09-20');
    expect(screen.queryByRole('dialog', { name: 'Seletor de Data' })).toBeNull();
  });

  it('permite limpar a data clicando no botão de limpar', () => {
    const onChange = vi.fn();
    render(<DatePicker value="2026-08-15" onChange={onChange} />);

    const clearBtn = screen.getByRole('button', { name: 'Limpar data' });
    fireEvent.click(clearBtn);

    expect(onChange).toHaveBeenCalledWith('');
  });

  it('seleciona a data de hoje ao clicar em Hoje no popover', () => {
    const onChange = vi.fn();
    render(<DatePicker value="2026-08-15" onChange={onChange} />);

    const triggerBtn = screen.getByRole('button', { name: /15\/08\/2026/ });
    fireEvent.click(triggerBtn);

    const hojeBtn = screen.getByRole('button', { name: 'Hoje' });
    fireEvent.click(hojeBtn);

    const now = new Date();
    const expected = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
    expect(onChange).toHaveBeenCalledWith(expected);
  });
});
