import { describe, it, expect } from 'vitest';
import { calculateValorTotal, calculateUltimaParcela } from './parcelamentoUtils';

describe('parcelamentoUtils', () => {
  describe('calculateValorTotal', () => {
    it('calcula o valor total corretamente para valores e parcelas válidos', () => {
      expect(calculateValorTotal('150,00', 4)).toContain('600,00');
      expect(calculateValorTotal('100,00', 1)).toContain('100,00');
      expect(calculateValorTotal('49,99', 2)).toContain('99,98');
    });

    it('retorna "-" defensivamente quando parcelas for 0, negativo ou vazio', () => {
      expect(calculateValorTotal('150,00', 0)).toBe('-');
      expect(calculateValorTotal('150,00', -1)).toBe('-');
      expect(calculateValorTotal('150,00', '')).toBe('-');
    });

    it('retorna "-" defensivamente quando valor da parcela for vazio, zero ou negativo', () => {
      expect(calculateValorTotal('', 4)).toBe('-');
      expect(calculateValorTotal('0,00', 4)).toBe('-');
      expect(calculateValorTotal('-50,00', 4)).toBe('-');
    });

    it('arredonda adequadamente centavos com frações de multiplicação', () => {
      // 33,33 * 3 = 99,99
      expect(calculateValorTotal('33,33', 3)).toContain('99,99');
    });
  });

  describe('calculateUltimaParcela', () => {
    it('calcula a última parcela no mesmo ano', () => {
      // Mai 2026 com 4 parcelas -> Mai, Jun, Jul, Ago 2026
      expect(calculateUltimaParcela('2026-05', 4)).toBe('Ago 2026 (08/2026)');
    });

    it('calcula a última parcela com parcela única', () => {
      expect(calculateUltimaParcela('2026-05', 1)).toBe('Mai 2026 (05/2026)');
    });

    it('calcula a última parcela transpassando a virada de ano', () => {
      // Nov 2026 com 4 parcelas -> Nov/26 (1), Dez/26 (2), Jan/27 (3), Fev/27 (4)
      expect(calculateUltimaParcela('2026-11', 4)).toBe('Fev 2027 (02/2027)');
    });

    it('calcula a última parcela para múltiplos anos de duração', () => {
      // Jan 2026 com 25 parcelas -> Jan 2028
      expect(calculateUltimaParcela('2026-01', 25)).toBe('Jan 2028 (01/2028)');
    });

    it('retorna "-" para datas inválidas, malformatadas ou vazias', () => {
      expect(calculateUltimaParcela('', 4)).toBe('-');
      expect(calculateUltimaParcela('invalido', 4)).toBe('-');
      expect(calculateUltimaParcela('2026', 4)).toBe('-');
      expect(calculateUltimaParcela('2026-13', 4)).toBe('-');
      expect(calculateUltimaParcela('2026-00', 4)).toBe('-');
    });

    it('retorna "-" quando quantidade de parcelas for menor que 1, vazia ou inválida', () => {
      expect(calculateUltimaParcela('2026-05', 0)).toBe('-');
      expect(calculateUltimaParcela('2026-05', -2)).toBe('-');
      expect(calculateUltimaParcela('2026-05', '')).toBe('-');
    });
  });
});
