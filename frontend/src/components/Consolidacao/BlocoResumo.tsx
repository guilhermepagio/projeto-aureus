import { useState, useMemo } from 'react';
import { ChevronDown, ChevronRight } from 'lucide-react';
import { useConsolidacao, type LinhaConsolidacaoDTO } from '../../hooks/useConsolidacao';
import { formatCurrency } from '../../utils/currencyFormat';

export interface BlocoResumoProps {
  selectedMonth?: string | null;
  currentIdx?: number;
  receitas?: LinhaConsolidacaoDTO[];
  despesas?: LinhaConsolidacaoDTO[];
  saldoHistoricoPreGrade?: number;
  isYearEnd?: (idx: number) => boolean;
  isCollapsed?: boolean;
  onToggle?: () => void;
}

export function BlocoResumo({
  selectedMonth,
  receitas: propsReceitas,
  despesas: propsDespesas,
  saldoHistoricoPreGrade: propsSaldoHistorico,
  isYearEnd,
  isCollapsed: propsIsCollapsed,
  onToggle: propsOnToggle,
}: BlocoResumoProps) {
  const [internalCollapsed, setInternalCollapsed] = useState(false);
  const isCollapsed = propsIsCollapsed !== undefined ? propsIsCollapsed : internalCollapsed;
  const toggleCollapsed = propsOnToggle ?? (() => setInternalCollapsed(p => !p));
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
      <tr
        onClick={toggleCollapsed}
        className="cursor-pointer select-none hover:brightness-95 transition-all"
        role="button"
        aria-expanded={!isCollapsed}
      >
        <th
          scope="row"
          className="px-3.5 py-1 text-[11px] font-bold uppercase tracking-[0.8px] border-b border-teal-200 border-r border-[#E5E7EB] shadow-[inset_-1px_0_0_#E5E7EB,2px_0_5px_-1px_rgba(0,0,0,0.07)] sticky left-0 z-10 text-left w-[220px] min-w-[220px] max-w-[220px] bg-teal-100/80 text-teal-900"
        >
          <span className="flex items-center gap-1.5">
            {isCollapsed ? (
              <ChevronRight className="w-3.5 h-3.5 shrink-0" />
            ) : (
              <ChevronDown className="w-3.5 h-3.5 shrink-0" />
            )}
            <span>Resumo Geral</span>
          </span>
        </th>
        <td colSpan={24} className="border-b border-teal-200 bg-teal-100/80" />
      </tr>

      {!isCollapsed && (
        <>
          {/* Linha 1: Total Gasto no Mês */}
          <tr className="group hover:bg-[#FAFAFA] transition-colors">
            <td className="px-3.5 py-2 text-[13px] font-semibold text-[#1A1A2E] sticky left-0 z-10 bg-white group-hover:bg-[#FAFAFA] border-b border-r border-[#E5E7EB] shadow-[inset_-1px_0_0_#E5E7EB,2px_0_5px_-1px_rgba(0,0,0,0.07)] w-[220px] min-w-[220px] max-w-[220px] truncate transition-colors">
              <span>Total Gasto no Mês</span>
            </td>
            {totalDespesas.map((val, i) => {
              const isYearBoundary = isYearEnd ? isYearEnd(i) : false;
              return (
                <td
                  key={i}
                  className={`px-3.5 py-2 text-[13px] font-semibold text-right tabular-nums whitespace-nowrap border-b border-b-[#F3F4F6] transition-colors ${
                    isYearBoundary ? 'border-r-2 border-r-slate-300' : 'border-r border-r-[#F3F4F6]'
                  } ${val > 0 ? 'text-red-600' : 'text-[#9CA3AF]'}`}
                  style={{ minWidth: 110 }}
                >
                  {formatCurrency(val)}
                </td>
              );
            })}
          </tr>

          {/* Linha 2: Sobra do Mês */}
          <tr className="group hover:bg-[#FAFAFA] transition-colors">
            <td className="px-3.5 py-2 text-[13px] font-semibold text-[#1A1A2E] sticky left-0 z-10 bg-white group-hover:bg-[#FAFAFA] border-b border-r border-[#E5E7EB] shadow-[inset_-1px_0_0_#E5E7EB,2px_0_5px_-1px_rgba(0,0,0,0.07)] w-[220px] min-w-[220px] max-w-[220px] truncate transition-colors">
              <span>Sobra do Mês</span>
            </td>
            {sobraMes.map((val, i) => {
              const isYearBoundary = isYearEnd ? isYearEnd(i) : false;
              return (
                <td
                  key={i}
                  className={`px-3.5 py-2 text-[13px] text-right tabular-nums whitespace-nowrap border-b border-b-[#F3F4F6] transition-colors ${
                    isYearBoundary ? 'border-r-2 border-r-slate-300' : 'border-r border-r-[#F3F4F6]'
                  } ${getSemanticColorClass(val)}`}
                  style={{ minWidth: 110 }}
                >
                  {formatCurrency(val)}
                </td>
              );
            })}
          </tr>

          {/* Linha 3: Sobra Retroativa Acumulada */}
          <tr className="group hover:bg-[#FAFAFA] transition-colors">
            <td className="px-3.5 py-2 text-[13px] font-semibold text-[#1A1A2E] sticky left-0 z-10 bg-white group-hover:bg-[#FAFAFA] border-b border-r border-[#E5E7EB] shadow-[inset_-1px_0_0_#E5E7EB,2px_0_5px_-1px_rgba(0,0,0,0.07)] w-[220px] min-w-[220px] max-w-[220px] truncate transition-colors">
              <span>Sobra Retroativa Acumulada</span>
            </td>
            {sobraRetroativa.map((val, i) => {
              const isYearBoundary = isYearEnd ? isYearEnd(i) : false;
              return (
                <td
                  key={i}
                  className={`px-3.5 py-2 text-[13px] text-right tabular-nums whitespace-nowrap border-b border-b-[#F3F4F6] transition-colors ${
                    isYearBoundary ? 'border-r-2 border-r-slate-300' : 'border-r border-r-[#F3F4F6]'
                  } ${getSemanticColorClass(val)}`}
                  style={{ minWidth: 110 }}
                >
                  {formatCurrency(val)}
                </td>
              );
            })}
          </tr>
        </>
      )}
    </>
  );
}
