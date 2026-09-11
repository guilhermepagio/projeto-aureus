import { formatCurrency } from "../../utils/currencyFormat";
import { useState, useMemo } from 'react';
import { ChevronDown, ChevronRight } from 'lucide-react';
import { useMonthStore } from '../../store/monthStore';
import { useConsolidacao, type LinhaConsolidacaoDTO } from '../../hooks/useConsolidacao';
import { BlocoCategorias } from './BlocoCategorias';
import { BlocoResumo } from './BlocoResumo';

// ── helpers ────────────────────────────────────────────────────────────────
const MONTH_ABBR = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

function getNext24Months(startYYYYMM: string): Array<{ label: string; value: string }> {
  const [y, m] = startYYYYMM.split('-').map(Number);
  const result = [];
  for (let i = 0; i < 24; i++) {
    const totalMonths = (m - 1) + i;
    const month = totalMonths % 12;
    const year = y + Math.floor(totalMonths / 12);
    result.push({
      label: `${MONTH_ABBR[month]} ${year}`,
      value: `${year}-${String(month + 1).padStart(2, '0')}`,
    });
  }
  return result;
}

const fmtVal = (v: number) => formatCurrency(v);

// ── sub-components ─────────────────────────────────────────────────────────

/** Cabeçalho colorido que spaneja todas as colunas com suporte a colapso */
function BlockHeader({
  label,
  colCount,
  color,
  isCollapsed,
  onToggle,
}: {
  label: string;
  colCount: number;
  color: 'green' | 'red' | 'amber' | 'teal';
  isCollapsed?: boolean;
  onToggle?: () => void;
}) {
  const bg: Record<string, string> = {
    green: 'bg-emerald-100/80',
    red:   'bg-rose-100/80',
    amber: 'bg-amber-100/80',
    teal:  'bg-teal-100/80',
  };
  const text: Record<string, string> = {
    green: 'text-emerald-900',
    red:   'text-rose-900',
    amber: 'text-amber-900',
    teal:  'text-teal-900',
  };
  const border: Record<string, string> = {
    green: 'border-emerald-200',
    red:   'border-rose-200',
    amber: 'border-amber-200',
    teal:  'border-teal-200',
  };

  return (
    <tr
      onClick={onToggle}
      className={`select-none transition-colors ${onToggle ? 'cursor-pointer hover:brightness-95' : ''}`}
      role={onToggle ? 'button' : undefined}
      aria-expanded={onToggle ? !isCollapsed : undefined}
    >
      <td
        className={`px-3.5 py-1 text-[11px] font-bold uppercase tracking-[0.8px] border-b border-r border-[#E5E7EB] shadow-[inset_-1px_0_0_#E5E7EB,2px_0_5px_-1px_rgba(0,0,0,0.07)] sticky left-0 z-10 w-[220px] min-w-[220px] max-w-[220px] ${bg[color]} ${text[color]} ${border[color]}`}
      >
        <span className="flex items-center gap-1.5">
          {onToggle && (
            isCollapsed ? (
              <ChevronRight className="w-3.5 h-3.5 shrink-0" />
            ) : (
              <ChevronDown className="w-3.5 h-3.5 shrink-0" />
            )
          )}
          <span>{label}</span>
        </span>
      </td>
      {Array.from({ length: colCount }).map((_, i) => (
        <td
          key={i}
          className={`border-b ${border[color]} ${bg[color]}`}
          style={{ minWidth: 110 }}
        />
      ))}
    </tr>
  );
}

/** Separador entre blocos */
function BlockSep({ colCount }: { colCount: number }) {
  return (
    <tr>
      <td className="h-3 bg-[#EEF0F2] border-y border-[#E2E5E9] border-r border-[#E2E5E9] shadow-[inset_-1px_0_0_#E2E5E9,2px_0_5px_-1px_rgba(0,0,0,0.07)] p-0 sticky left-0 z-10 w-[220px] min-w-[220px] max-w-[220px]" />
      {Array.from({ length: colCount }).map((_, i) => (
        <td key={i} className="h-3 bg-[#EEF0F2] border-y border-[#E2E5E9] p-0" style={{ minWidth: 110 }} />
      ))}
    </tr>
  );
}

/** Linha de dado padrão — valores monetários */
function DataRow({
  label,
  values,
  valueClass,
  isYearEnd,
}: {
  label: string;
  values: number[];
  currentIdx?: number;
  valueClass?: (v: number, i: number) => string;
  isYearEnd?: (i: number) => boolean;
}) {
  return (
    <tr className="group hover:bg-[#FAFAFA] transition-colors">
      <td className="px-3.5 py-1 text-[13px] font-medium text-[#1A1A2E] sticky left-0 z-10 bg-white group-hover:bg-[#FAFAFA] border-b border-r border-[#E5E7EB] shadow-[inset_-1px_0_0_#E5E7EB,2px_0_5px_-1px_rgba(0,0,0,0.07)] w-[220px] min-w-[220px] max-w-[220px] truncate transition-colors">
        {label}
      </td>
      {values.map((v, i) => {
        const cls = valueClass ? valueClass(v, i) : v === 0 ? 'text-[#9CA3AF]' : 'text-[#1A1A2E] font-medium';
        const isYearBoundary = isYearEnd ? isYearEnd(i) : false;

        return (
          <td
            key={i}
            className={`px-3.5 py-1 text-[13px] text-right tabular-nums whitespace-nowrap border-b border-b-[#F3F4F6] transition-colors ${
              isYearBoundary ? 'border-r-2 border-r-slate-300' : 'border-r border-r-[#F3F4F6]'
            } ${cls}`}
            style={{ minWidth: 110 }}
          >
            {fmtVal(v)}
          </td>
        );
      })}
    </tr>
  );
}

// ── main component ─────────────────────────────────────────────────────────

export interface ConsolidacaoGridProps {
  scrollContainerRef?: React.RefObject<HTMLDivElement | null>;
  collapsed?: {
    receitas: boolean;
    despesas: boolean;
    categoriasVal: boolean;
    categoriasPct: boolean;
    resumo: boolean;
  };
  onToggleSection?: (section: 'receitas' | 'despesas' | 'categoriasVal' | 'categoriasPct' | 'resumo') => void;
}

export default function ConsolidacaoGrid({
  scrollContainerRef,
  collapsed: propsCollapsed,
  onToggleSection: propsOnToggleSection,
}: ConsolidacaoGridProps = {}) {
  const { selectedMonth } = useMonthStore();

  const months = useMemo(() => {
    if (!selectedMonth || !selectedMonth.includes('-')) return [];
    return getNext24Months(selectedMonth);
  }, [selectedMonth]);

  const { data: contaData, isLoading: loadingConta, isError: errorConta } = useConsolidacao(selectedMonth);
  

  if (months.length === 0) return null;

  const isLoading = loadingConta;
  const isError = errorConta;

  const [internalCollapsed, setInternalCollapsed] = useState<{
    receitas: boolean;
    despesas: boolean;
    categoriasVal: boolean;
    categoriasPct: boolean;
    resumo: boolean;
  }>({
    receitas: false,
    despesas: false,
    categoriasVal: false,
    categoriasPct: false,
    resumo: false,
  });

  const collapsed = propsCollapsed ?? internalCollapsed;

  const toggleSection = (section: keyof typeof collapsed) => {
    if (propsOnToggleSection) {
      propsOnToggleSection(section);
    } else {
      setInternalCollapsed(prev => ({ ...prev, [section]: !prev[section] }));
    }
  };

  const isYearEnd = (i: number) => {
    return months[i]?.value.endsWith('-12') ?? false;
  };

  const yearGroups = useMemo(() => {
    const groups: Array<{ year: number; count: number }> = [];
    months.forEach((m) => {
      const year = parseInt(m.value.split('-')[0], 10);
      const last = groups[groups.length - 1];
      if (last && last.year === year) {
        last.count += 1;
      } else {
        groups.push({ year, count: 1 });
      }
    });
    return groups;
  }, [months]);

  const visibleReceitas = useMemo(() => {
    return (contaData?.receitas || []).filter((linha: LinhaConsolidacaoDTO) =>
      linha.valoresMensais?.some(v => v !== 0 && v != null)
    );
  }, [contaData?.receitas]);

  const visibleDespesas = useMemo(() => {
    return (contaData?.despesas || []).filter((linha: LinhaConsolidacaoDTO) =>
      linha.valoresMensais?.some(v => v !== 0 && v != null)
    );
  }, [contaData?.despesas]);
  
  return (
    <div
      className="bg-white rounded-xl border border-[#E5E7EB] shadow-sm overflow-hidden h-full flex flex-col"
    >
      <div
        ref={scrollContainerRef}
        id="consolidacao-scroll-container"
        className="overflow-x-auto overflow-y-auto flex-1 scroll-smooth"
      >
        <table
          className="border-separate border-spacing-0"
          style={{ minWidth: 1400 }}
        >
          {/* ── CABEÇALHO DE MESES COM SUPERCABEÇALHO DE ANO ── */}
          <thead>
            {/* Linha 1: Supercabeçalho de Ano */}
            <tr className="h-7">
              <th
                scope="col"
                className="sticky left-0 top-0 z-30 bg-slate-100 px-3.5 py-1 text-left text-[11px] font-bold text-slate-600 uppercase tracking-wider border-b border-r border-[#E5E7EB] shadow-[inset_-1px_0_0_#E5E7EB,2px_0_5px_-1px_rgba(0,0,0,0.07)] w-[220px] min-w-[220px] max-w-[220px]"
              >
                Exercício
              </th>
              {yearGroups.map((group) => (
                <th
                  key={group.year}
                  colSpan={group.count}
                  scope="colgroup"
                  className="sticky top-0 z-20 bg-slate-100 border-b border-r-2 border-r-slate-300 px-3 py-1 text-center text-[11px] font-bold tracking-wider text-slate-700 uppercase"
                >
                  <div className="flex items-center justify-center gap-1.5">
                    <span>{group.year}</span>
                    <span className="text-[10px] font-normal text-slate-500">
                      ({group.count} {group.count === 1 ? 'mês' : 'meses'})
                    </span>
                  </div>
                </th>
              ))}
            </tr>

            {/* Linha 2: Cabeçalho de Meses */}
            <tr>
              <th
                scope="col"
                className="sticky left-0 top-[28px] z-30 bg-white px-3.5 py-2.5 text-left text-[11px] font-bold text-slate-600 uppercase tracking-wider border-b-2 border-[#E5E7EB] border-r border-[#E5E7EB] shadow-[inset_-1px_0_0_#E5E7EB,2px_0_5px_-1px_rgba(0,0,0,0.07)] w-[220px] min-w-[220px] max-w-[220px]"
              >
                Contas & Categorias
              </th>
              {months.map((m, i) => {
                const isYearBoundary = isYearEnd(i);
                return (
                  <th
                    key={m.value}
                    scope="col"
                    title={m.label}
                    className={`sticky top-[28px] z-20 px-3.5 py-2.5 text-center text-[11px] font-bold uppercase tracking-[0.5px] whitespace-nowrap border-b-2 border-b-[#E5E7EB] transition-colors cursor-default ${
                      isYearBoundary ? 'border-r-2 border-r-slate-300' : 'border-r border-r-[#F3F4F6]'
                    } bg-white text-[#1A1A2E]`}
                    style={{ minWidth: 110 }}
                  >
                    {m.label}
                  </th>
                );
              })}
            </tr>
          </thead>

          <tbody>
            {isLoading && (
              <tr>
                <td className="py-16 text-center text-[13px] text-[#6B7280] sticky left-0" colSpan={25}>
                  Carregando consolidação…
                </td>
              </tr>
            )}

            {isError && !isLoading && (
              <tr>
                <td className="py-16 text-center text-[13px] text-red-600 sticky left-0" colSpan={25}>
                  Erro ao carregar dados da consolidação.
                </td>
              </tr>
            )}

            {!isLoading && !isError && contaData && (
              <>
                {/* ═══ BLOCO 1: RECEITAS POR CONTA ═══ */}
                <BlockHeader
                  label="Receitas por Conta"
                  colCount={24}
                  color="green"
                  isCollapsed={collapsed.receitas}
                  onToggle={() => toggleSection('receitas')}
                />
                {!collapsed.receitas && (
                  visibleReceitas.length === 0 ? (
                    <tr>
                      <td className="px-3.5 py-1 text-[13px] text-[#9CA3AF] border-b border-[#F3F4F6] border-r border-[#E5E7EB] shadow-[inset_-1px_0_0_#E5E7EB,2px_0_5px_-1px_rgba(0,0,0,0.07)] sticky left-0 z-10 w-[220px] min-w-[220px] max-w-[220px]">Nenhuma receita no período.</td>
                      {months.map((_, i) => <td key={i} className="border-b border-[#F3F4F6]" style={{ minWidth: 110 }} />)}
                    </tr>
                  ) : (
                    visibleReceitas.map((linha: LinhaConsolidacaoDTO) => (
                      <DataRow
                        key={`rec-${linha.contaId}`}
                        label={linha.contaDescricao}
                        values={linha.valoresMensais}
                        isYearEnd={isYearEnd}
                      />
                    ))
                  )
                )}

                <BlockSep colCount={24} />

                {/* ═══ BLOCO 2: DESPESAS POR CONTA ═══ */}
                <BlockHeader
                  label="Despesas por Conta"
                  colCount={24}
                  color="red"
                  isCollapsed={collapsed.despesas}
                  onToggle={() => toggleSection('despesas')}
                />
                {!collapsed.despesas && (
                  visibleDespesas.length === 0 ? (
                    <tr>
                      <td className="px-3.5 py-1 text-[13px] text-[#9CA3AF] border-b border-[#F3F4F6] border-r border-[#E5E7EB] shadow-[inset_-1px_0_0_#E5E7EB,2px_0_5px_-1px_rgba(0,0,0,0.07)] sticky left-0 z-10 w-[220px] min-w-[220px] max-w-[220px]">Nenhuma despesa no período.</td>
                      {months.map((_, i) => <td key={i} className="border-b border-[#F3F4F6]" style={{ minWidth: 110 }} />)}
                    </tr>
                  ) : (
                    visibleDespesas.map((linha: LinhaConsolidacaoDTO) => (
                      <DataRow
                        key={`desp-${linha.contaId}`}
                        label={linha.contaDescricao}
                        values={linha.valoresMensais}
                        isYearEnd={isYearEnd}
                      />
                    ))
                  )
                )}

                <BlockSep colCount={24} />

                {/* ═══ BLOCO DE CATEGORIAS ═══ */}
                <BlocoCategorias
                  selectedMonth={selectedMonth}
                  isYearEnd={isYearEnd}
                  collapsedVal={collapsed.categoriasVal}
                  onToggleVal={() => toggleSection('categoriasVal')}
                  collapsedPct={collapsed.categoriasPct}
                  onTogglePct={() => toggleSection('categoriasPct')}
                />

                <BlockSep colCount={24} />

                {/* ═══ BLOCO DE RESUMO GERAL ═══ */}
                <BlocoResumo
                  selectedMonth={selectedMonth}
                  receitas={visibleReceitas}
                  despesas={visibleDespesas}
                  saldoHistoricoPreGrade={contaData.saldoHistoricoPreGrade}
                  isYearEnd={isYearEnd}
                  isCollapsed={collapsed.resumo}
                  onToggle={() => toggleSection('resumo')}
                />
              </>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
