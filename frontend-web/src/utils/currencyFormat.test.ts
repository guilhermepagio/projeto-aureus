import { describe, it, expect } from 'vitest';
import { formatCurrency, parseCurrency } from './currencyFormat';

describe('currencyFormat', () => {
  describe('formatCurrency', () => {
    it('retorna string vazia para valores vazios, nulos ou indefinidos', () => {
      expect(formatCurrency('')).toBe('');
      expect(formatCurrency(null as any)).toBe('');
      expect(formatCurrency(undefined as any)).toBe('');
    });

    it('formata números positivos corretamente em formato pt-BR', () => {
      const formatted = formatCurrency(1234.56);
      expect(formatted).toContain('1.234,56');
      expect(formatted).toContain('R$');
    });

    it('formata números negativos corretamente com sinal de menos', () => {
      const formatted = formatCurrency(-500.75);
      expect(formatted).toContain('500,75');
      expect(formatted).toContain('-');
    });

    it('normaliza zero e zero negativo para R$ 0,00 sem sinal de menos', () => {
      const zeroPos = formatCurrency(0);
      const zeroNeg = formatCurrency(-0);
      expect(zeroPos).toContain('0,00');
      expect(zeroPos).not.toContain('-');
      expect(zeroNeg).toContain('0,00');
      expect(zeroNeg).not.toContain('-');
    });

    it('formata strings de máscara de entrada numérica', () => {
      // 100 centavos = R$ 1,00
      expect(formatCurrency('100')).toContain('1,00');
      // -100 centavos = -R$ 1,00
      const negativeMask = formatCurrency('-100');
      expect(negativeMask).toContain('1,00');
      expect(negativeMask).toContain('-');
    });
  });

  describe('parseCurrency', () => {
    it('retorna 0 para strings vazias, nulas ou indefinidas', () => {
      expect(parseCurrency('')).toBe(0);
      expect(parseCurrency(null as any)).toBe(0);
      expect(parseCurrency(undefined as any)).toBe(0);
    });

    it('converte strings monetárias formatadas em centavos para float em reais', () => {
      expect(parseCurrency('R$ 150,00')).toBe(150);
      expect(parseCurrency('1.234,56')).toBe(1234.56);
    });

    it('converte valores negativos corretamente', () => {
      expect(parseCurrency('-R$ 150,00')).toBe(-150);
      expect(parseCurrency('-15000')).toBe(-150);
      expect(parseCurrency('—R$ 150,00')).toBe(-150);
      expect(parseCurrency('—15000')).toBe(-150);
    });

    it('trata tipos não-string e não-numéricos graciosamente sem lançar exceção', () => {
      expect(parseCurrency(123.45 as any)).toBe(123.45);
      expect(parseCurrency({} as any)).toBe(0);
      expect(parseCurrency(NaN as any)).toBe(0);
      expect(parseCurrency(Infinity as any)).toBe(0);
      expect(parseCurrency(true as any)).toBe(0);
      expect(formatCurrency({} as any)).toBe('');
      expect(formatCurrency(NaN as any)).toBe('');
      expect(formatCurrency(Infinity as any)).toBe('');
    });

    it('suporta caracteres Unicode de menos como dash e minus sign', () => {
      expect(parseCurrency('−R$ 150,00')).toBe(-150);
      expect(parseCurrency('–R$ 150,00')).toBe(-150);
      expect(formatCurrency('−100')).toContain('-');
    });
  });
});
