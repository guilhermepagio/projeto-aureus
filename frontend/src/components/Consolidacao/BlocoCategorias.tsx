import { useState } from 'react';
import { ChevronDown, ChevronRight } from 'lucide-react';
import { useConsolidacaoCategoria, type LinhaConsolidacaoCategoriaDTO } from '../../hooks/useConsolidacaoCategoria';
import { formatCurrency } from '../../utils/currencyFormat';

export interface BlocoCategoriasProps {
  selectedMonth: string | null;
  currentIdx?: number;
  isYearEnd?: (idx: number) => boolean;
  collapsedVal?: boolean;
  onToggleVal?: () => void;
  collapsedPct?: boolean;
  onTogglePct?: () => void;
}

export function BlocoCategorias({
  selectedMonth,
  isYearEnd,
  collapsedVal: propsCollapsedVal,
  onToggleVal: propsOnToggleVal,
  collapsedPct: propsCollapsedPct,
  onTogglePct: propsOnTogglePct,
}: BlocoCategoriasProps) {
  const [internalCollapsedVal, setInternalCollapsedVal] = useState(false);
  const [internalCollapsedPct, setInternalCollapsedPct] = useState(false);

  const isCollapsedVal = propsCollapsedVal !== undefined ? propsCollapsedVal : internalCollapsedVal;
  const toggleVal = propsOnToggleVal ?? (() => setInternalCollapsedVal(p => !p));

  const isCollapsedPct = propsCollapsedPct !== undefined ? propsCollapsedPct : internalCollapsedPct;
  const togglePct = propsOnTogglePct ?? (() => setInternalCollapsedPct(p => !p));

  if (!selectedMonth) return null;

  const { data, isLoading, isError } = useConsolidacaoCategoria(selectedMonth);

  if (isLoading) {
    return (
      <tr>
        <td className="py-16 text-center text-[13px] text-[#6B7280] sticky left-0" colSpan={25}>
          Carregando categorias…
        </td>
      </tr>
    );
  }

  if (isError || !data) {
    return (
      <tr>
        <td className="py-16 text-center text-[13px] text-red-600 sticky left-0" colSpan={25}>
          Erro ao carregar dados das categorias.
        </td>
      </tr>
    );
  }

  const despesas = data.despesas || [];

  // Calcula totais mensais (seguro contra precisão)
  const totalDespesasMensais = Array(24).fill(0);
  despesas.forEach(d => {
    (d.valoresMensais || []).forEach((val, i) => {
      totalDespesasMensais[i] = Math.round((totalDespesasMensais[i] + val) * 100) / 100;
    });
  });

  const renderMonetaryRow = (row: LinhaConsolidacaoCategoriaDTO) => (
    <tr key={`cat-val-${row.categoriaId}`} className="group hover:bg-[#FAFAFA] transition-colors">
      <td className="px-3.5 py-1 text-[13px] font-medium text-[#1A1A2E] sticky left-0 z-10 bg-white group-hover:bg-[#FAFAFA] border-b border-r border-[#E5E7EB] shadow-[inset_-1px_0_0_#E5E7EB,2px_0_5px_-1px_rgba(0,0,0,0.07)] w-[220px] min-w-[220px] max-w-[220px] truncate transition-colors">
        {row.categoriaDescricao}
      </td>
      {(row.valoresMensais || []).map((val, i) => {
        const isYearBoundary = isYearEnd ? isYearEnd(i) : false;
        return (
          <td
            key={i}
            className={`px-3.5 py-1 text-[13px] text-right tabular-nums whitespace-nowrap border-b border-b-[#F3F4F6] transition-colors ${
              isYearBoundary ? 'border-r-2 border-r-slate-300' : 'border-r border-r-[#F3F4F6]'
            } ${val === 0 ? 'text-[#9CA3AF]' : 'text-[#1A1A2E] font-medium'}`}
            style={{ minWidth: 110 }}
          >
            {formatCurrency(val || 0)}
          </td>
        );
      })}
    </tr>
  );

  const renderPercentageRow = (row: LinhaConsolidacaoCategoriaDTO) => (
    <tr key={`cat-pct-${row.categoriaId}`} className="group hover:bg-[#FAFAFA] transition-colors">
      <td className="px-3.5 py-1 text-[13px] font-medium text-[#1A1A2E] sticky left-0 z-10 bg-white group-hover:bg-[#FAFAFA] border-b border-r border-[#E5E7EB] shadow-[inset_-1px_0_0_#E5E7EB,2px_0_5px_-1px_rgba(0,0,0,0.07)] w-[220px] min-w-[220px] max-w-[220px] truncate transition-colors">
        {row.categoriaDescricao}
      </td>
      {(row.valoresMensais || []).map((val, i) => {
        const total = totalDespesasMensais[i];
        let percent = 0;
        const safeVal = val || 0;
        if (total > 0) {
          percent = (safeVal / total) * 100;
        }
        const isYearBoundary = isYearEnd ? isYearEnd(i) : false;

        return (
          <td
            key={i}
            className={`px-3.5 py-1 text-[13px] text-right tabular-nums whitespace-nowrap border-b border-b-[#F3F4F6] transition-colors ${
              isYearBoundary ? 'border-r-2 border-r-slate-300' : 'border-r border-r-[#F3F4F6]'
            } ${val === 0 ? 'text-[#9CA3AF]' : 'text-[#1A1A2E] font-medium'}`}
            style={{ minWidth: 110 }}
          >
            {percent.toFixed(1).replace('.', ',')}%
          </td>
        );
      })}
    </tr>
  );

  return (
    <>
      {/* ═══ BLOCO 3: CATEGORIAS R$ ═══ */}
      <tr
        onClick={toggleVal}
        className="cursor-pointer select-none hover:brightness-95 transition-all"
        role="button"
        aria-expanded={!isCollapsedVal}
      >
        <th
          scope="row"
          className="px-3.5 py-1 text-[11px] font-bold uppercase tracking-[0.8px] border-b border-amber-200 border-r border-[#E5E7EB] shadow-[inset_-1px_0_0_#E5E7EB,2px_0_5px_-1px_rgba(0,0,0,0.07)] sticky left-0 z-10 text-left w-[220px] min-w-[220px] max-w-[220px] bg-amber-100/80 text-amber-900"
        >
          <span className="flex items-center gap-1.5">
            {isCollapsedVal ? (
              <ChevronRight className="w-3.5 h-3.5 shrink-0" />
            ) : (
              <ChevronDown className="w-3.5 h-3.5 shrink-0" />
            )}
            <span>Categorias (R$)</span>
          </span>
        </th>
        <td colSpan={24} className="border-b border-amber-200 bg-amber-100/80" />
      </tr>

      {!isCollapsedVal && (
        despesas.length === 0 ? (
          <tr>
            <td className="px-3.5 py-1 text-[13px] text-[#9CA3AF] border-b border-[#F3F4F6] border-r border-[#E5E7EB] shadow-[inset_-1px_0_0_#E5E7EB,2px_0_5px_-1px_rgba(0,0,0,0.07)] sticky left-0 z-10 w-[220px] min-w-[220px] max-w-[220px]">
              Nenhuma despesa categorizada no período.
            </td>
            {Array.from({ length: 24 }).map((_, i) => (
              <td key={i} className="border-b border-[#F3F4F6]" style={{ minWidth: 110 }} />
            ))}
          </tr>
        ) : (
          despesas.map(d => renderMonetaryRow(d))
        )
      )}

      {/* Separador entre blocos */}
      <tr>
        <td className="h-3 bg-[#EEF0F2] border-y border-[#E2E5E9] border-r border-[#E2E5E9] shadow-[inset_-1px_0_0_#E2E5E9,2px_0_5px_-1px_rgba(0,0,0,0.07)] p-0 sticky left-0 z-10 w-[220px] min-w-[220px] max-w-[220px] " />
        {Array.from({ length: 24 }).map((_, i) => (
          <td key={i} className="h-3 bg-[#EEF0F2] border-y border-[#E2E5E9] p-0" style={{ minWidth: 110 }} />
        ))}
      </tr>

      {/* ═══ BLOCO 4: CATEGORIAS % ═══ */}
      <tr
        onClick={togglePct}
        className="cursor-pointer select-none hover:brightness-95 transition-all"
        role="button"
        aria-expanded={!isCollapsedPct}
      >
        <th
          scope="row"
          className="px-3.5 py-1 text-[11px] font-bold uppercase tracking-[0.8px] border-b border-amber-200 border-r border-[#E5E7EB] shadow-[inset_-1px_0_0_#E5E7EB,2px_0_5px_-1px_rgba(0,0,0,0.07)] sticky left-0 z-10 text-left w-[220px] min-w-[220px] max-w-[220px] bg-amber-100/80 text-amber-900"
        >
          <span className="flex items-center gap-1.5">
            {isCollapsedPct ? (
              <ChevronRight className="w-3.5 h-3.5 shrink-0" />
            ) : (
              <ChevronDown className="w-3.5 h-3.5 shrink-0" />
            )}
            <span>Categorias (%)</span>
          </span>
        </th>
        <td colSpan={24} className="border-b border-amber-200 bg-amber-100/80" />
      </tr>

      {!isCollapsedPct && (
        despesas.length === 0 ? (
          <tr>
            <td className="px-3.5 py-1 text-[13px] text-[#9CA3AF] border-b border-[#F3F4F6] border-r border-[#E5E7EB] shadow-[inset_-1px_0_0_#E5E7EB,2px_0_5px_-1px_rgba(0,0,0,0.07)] sticky left-0 z-10 w-[220px] min-w-[220px] max-w-[220px]">
              Nenhuma despesa categorizada no período.
            </td>
            {Array.from({ length: 24 }).map((_, i) => (
              <td key={i} className="border-b border-[#F3F4F6]" style={{ minWidth: 110 }} />
            ))}
          </tr>
        ) : (
          despesas.map(d => renderPercentageRow(d))
        )
      )}
    </>
  );
}
