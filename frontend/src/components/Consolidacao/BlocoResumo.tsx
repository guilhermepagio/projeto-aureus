import { useMemo } from 'react';
import { useConsolidacao, type LinhaConsolidacaoDTO } from '../../hooks/useConsolidacao';
import { formatCurrency } from '../../utils/currencyFormat';

export interface BlocoResumoProps {
  selectedMonth?: string | null;
  currentIdx: number;
  receitas?: LinhaConsolidacaoDTO[];
  despesas?: LinhaConsolidacaoDTO[];
  saldoHistoricoPreGrade?: number;
}

export function BlocoResumo({
  selectedMonth,
  currentIdx,
  receitas: propsReceitas,
  despesas: propsDespesas,
  saldoHistoricoPreGrade: propsSaldoHistorico,
}: BlocoResumoProps) {
  // If props are provided, use them; otherwise fetch via hook
  const hasProps = Boolean(propsReceitas && propsDespesas);
  const queryResult = useConsolidacao(hasProps || !selectedMonth ? null : selectedMonth);

  const receitas = propsReceitas ?? queryResult.data?.receitas ?? [];
  const despesas = propsDespesas ?? queryResult.data?.despesas ?? [];
  const saldoHistoricoPreGrade = Number(propsSaldoHistorico ?? queryResult.data?.saldoHistoricoPreGrade ?? 0) || 0;

  // Compute monthly total expenses (Total Gasto no Mês)
  const totalDespesas = useMemo(() => {
    const arr = Array(24).fill(0);
    despesas.forEach(d => {
      (d?.valoresMensais || []).forEach((val, i) => {
        if (i < 24) {
          arr[i] = Math.round((arr[i] + (Number(val) || 0)) * 100) / 100;
        }
      });
    });
    return arr;
  }, [despesas]);

  // Compute monthly total revenues
  const totalReceitas = useMemo(() => {
    const arr = Array(24).fill(0);
    receitas.forEach(r => {
      (r?.valoresMensais || []).forEach((val, i) => {
        if (i < 24) {
          arr[i] = Math.round((arr[i] + (Number(val) || 0)) * 100) / 100;
        }
      });
    });
    return arr;
  }, [receitas]);

  // Compute monthly surplus (Sobra do Mês = Receitas - Despesas)
  const sobraMes = useMemo(() => {
    const arr = Array(24).fill(0);
    for (let i = 0; i < 24; i++) {
      arr[i] = Math.round((totalReceitas[i] - totalDespesas[i]) * 100) / 100;
    }
    return arr;
  }, [totalReceitas, totalDespesas]);

  // Compute cumulative surplus (Sobra Retroativa Acumulada)
  // Month 0: saldoHistoricoPreGrade + sobraMes[0]
  // Month n: sobraRetroativa[n - 1] + sobraMes[n]
  const sobraRetroativa = useMemo(() => {
    const arr = Array(24).fill(0);
    let acumulado = saldoHistoricoPreGrade;
    for (let i = 0; i < 24; i++) {
      acumulado = Math.round((acumulado + sobraMes[i]) * 100) / 100;
      arr[i] = acumulado;
    }
    return arr;
  }, [saldoHistoricoPreGrade, sobraMes]);

  if (!hasProps && !selectedMonth) {
    return null;
  }

  if (!hasProps && queryResult.isLoading) {
    return (
      <tr>
        <td className="py-8 text-center text-[13px] text-[#6B7280] sticky left-0" colSpan={25}>
          Carregando resumo…
        </td>
      </tr>
    );
  }

  if (!hasProps && (queryResult.isError || (!queryResult.isLoading && !queryResult.data))) {
    return (
      <tr>
        <td className="py-8 text-center text-[13px] text-red-600 sticky left-0" colSpan={25}>
          Erro ao carregar dados do resumo.
        </td>
      </tr>
    );
  }

  const getSemanticColorClass = (val: number, isBold: boolean = false) => {
    const weight = isBold ? 'font-bold' : 'font-semibold';
    if (val > 0) return `text-green-600 ${weight}`;
    if (val < 0) return `text-red-600 ${weight}`;
    return `text-[#9CA3AF] ${weight}`;
  };

  return (
    <>
      {/* ═══ BLOCO 5: RESUMO GERAL ═══ */}
      <tr>
        <th
          scope="row"
          className="px-3.5 py-1 text-[11px] font-bold uppercase tracking-[0.8px] border-b border-[#E5E7EB] sticky left-0 z-10 text-left min-w-[180px] bg-teal-50 text-teal-700"
        >
          Resumo Geral
        </th>
        <td colSpan={24} className="border-b border-[#E5E7EB] bg-teal-50" />
      </tr>

      {/* Linha 1: Total Gasto no Mês */}
      <tr className="group hover:bg-[#FAFAFA] transition-colors border-t border-[#E5E7EB]">
        <td className="px-3.5 py-2 text-[13px] font-semibold text-[#1A1A2E] sticky left-0 z-10 bg-white group-hover:bg-[#FAFAFA] border-b border-r border-[#F3F4F6] min-w-[180px] max-w-[180px] truncate transition-colors">
          <span>Total Gasto no Mês</span>
        </td>
        {totalDespesas.map((val, i) => (
          <td
            key={i}
            className={`px-3.5 py-2 text-[13px] font-semibold text-right tabular-nums whitespace-nowrap border-b border-r border-[#F3F4F6] ${i === currentIdx ? 'bg-teal-50/50' : ''} ${val === 0 ? 'text-[#9CA3AF]' : 'text-[#6B7280]'}`}
            style={{ minWidth: 110 }}
          >
            {formatCurrency(val)}
          </td>
        ))}
      </tr>

      {/* Linha 2: Sobra do Mês */}
      <tr className="group hover:bg-[#FAFAFA] transition-colors">
        <td className="px-3.5 py-2 text-[13px] font-semibold text-[#1A1A2E] sticky left-0 z-10 bg-white group-hover:bg-[#FAFAFA] border-b border-r border-[#F3F4F6] min-w-[180px] max-w-[180px] truncate transition-colors">
          <span>Sobra do Mês</span>
        </td>
        {sobraMes.map((val, i) => (
          <td
            key={i}
            className={`px-3.5 py-2 text-[13px] text-right tabular-nums whitespace-nowrap border-b border-r border-[#F3F4F6] ${i === currentIdx ? 'bg-teal-50/50' : ''} ${getSemanticColorClass(val)}`}
            style={{ minWidth: 110 }}
          >
            {formatCurrency(val)}
          </td>
        ))}
      </tr>

      {/* Linha 3: Sobra Retroativa Acumulada */}
      <tr className="group hover:bg-[#FAFAFA] transition-colors border-t-2 border-t-[#0D7377]">
        <td className="px-3.5 py-2 text-[14px] font-bold text-[#1A1A2E] sticky left-0 z-10 bg-white group-hover:bg-[#FAFAFA] border-t-2 border-t-[#0D7377] border-b border-r border-[#F3F4F6] min-w-[180px] max-w-[180px] truncate transition-colors">
          <span className="sr-only">Sobra Retroativa Acumulada</span>
          <span aria-hidden="true">Sobra Retroativa Acum.</span>
        </td>
        {sobraRetroativa.map((val, i) => (
          <td
            key={i}
            className={`px-3.5 py-2 text-[14px] text-right tabular-nums whitespace-nowrap border-t-2 border-t-[#0D7377] border-b border-r border-[#F3F4F6] ${i === currentIdx ? 'bg-teal-50/50' : ''} ${getSemanticColorClass(val, true)}`}
            style={{ minWidth: 110 }}
          >
            {formatCurrency(val)}
          </td>
        ))}
      </tr>
    </>
  );
}
