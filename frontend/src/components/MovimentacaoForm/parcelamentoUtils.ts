import { MONTHS } from '../ui/dateConstants';
import { parseCurrency } from '../../utils/currencyFormat';

export function calculateValorTotal(valorParcela: string, quantidadeParcelas: number | ''): string {
  const valorNum = parseCurrency(valorParcela);
  const qtdNum = Number(quantidadeParcelas);
  if (valorNum > 0 && qtdNum > 0) {
    const totalSafe = Math.round(valorNum * qtdNum * 100) / 100;
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(totalSafe);
  }
  return '-';
}

export function calculateUltimaParcela(dataInicio: string, quantidadeParcelas: number | ''): string {
  const qtdNum = Number(quantidadeParcelas);
  if (!dataInicio || !dataInicio.includes('-') || !qtdNum || qtdNum < 1 || isNaN(qtdNum)) {
    return '-';
  }

  const parts = dataInicio.split('-');
  if (parts.length < 2) {
    return '-';
  }

  const yearNum = parseInt(parts[0], 10);
  const monthNum = parseInt(parts[1], 10);

  if (isNaN(yearNum) || isNaN(monthNum) || monthNum < 1 || monthNum > 12) {
    return '-';
  }

  const d = new Date(yearNum, monthNum - 1, 1);
  d.setMonth(d.getMonth() + qtdNum - 1);
  const mIdx = d.getMonth();
  const m = String(mIdx + 1).padStart(2, '0');
  const y = d.getFullYear();
  return `${MONTHS[mIdx]} ${y} (${m}/${y})`;
}
