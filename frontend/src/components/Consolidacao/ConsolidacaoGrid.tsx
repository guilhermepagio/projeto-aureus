import { formatCurrency } from "../../utils/currencyFormat";
import { useMemo } from 'react';
import { useMonthStore } from '../../store/monthStore';
import { useConsolidacao, type LinhaConsolidacaoDTO } from '../../hooks/useConsolidacao';
import { BlocoCategorias } from './BlocoCategorias';

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

/** Cabeçalho colorido que spaneja todas as colunas */
function BlockHeader({
  label,
  colCount,
  color,
}: {
  label: string;
  colCount: number;
  color: 'green' | 'red' | 'amber' | 'teal';
}) {
  const bg: Record<string, string> = {
    green: 'bg-green-50',
    red:   'bg-red-50',
    amber: 'bg-amber-50',
    teal:  'bg-teal-50',
  };
  const text: Record<string, string> = {
    green: 'text-green-600',
    red:   'text-red-600',
    amber: 'text-amber-700',
    teal:  'text-teal-700',
  };

  // Célula do rótulo: sticky left-0 para ficar visível durante scroll horizontal
  // Células vazias: preenchem o restante das colunas com o fundo colorido
  return (
    <tr>
      <td
        className={`px-3.5 py-1 text-[11px] font-bold uppercase tracking-[0.8px] border-b border-[#E5E7EB] sticky left-0 z-10 min-w-[180px] ${bg[color]} ${text[color]}`}
      >
        {label}
      </td>
      {Array.from({ length: colCount }).map((_, i) => (
        <td
          key={i}
          className={`border-b border-[#E5E7EB] ${bg[color]}`}
          style={{ minWidth: 110 }}
        />
      ))}
    </tr>
  );
}

/** Separador de 8px entre blocos */
function BlockSep({ colCount }: { colCount: number }) {
  return (
    <tr>
      <td className="h-2 bg-[#FAFAFA] border-b border-[#F3F4F6] p-0 sticky left-0 min-w-[180px]" />
      {Array.from({ length: colCount }).map((_, i) => (
        <td key={i} className="h-2 bg-[#FAFAFA] border-b border-[#F3F4F6] p-0" style={{ minWidth: 110 }} />
      ))}
    </tr>
  );
}

/** Linha de dado padrão — valores monetários */
function DataRow({
  label,
  values,
  currentIdx,
  valueClass,
}: {
  label: string;
  values: number[];
  currentIdx: number;
  valueClass?: (v: number, i: number) => string;
}) {
  return (
    <tr className="group hover:bg-[#FAFAFA] transition-colors">
      <td className="px-3.5 py-1 text-[13px] font-medium text-[#1A1A2E] sticky left-0 z-10 bg-white group-hover:bg-[#FAFAFA] border-b border-r border-[#F3F4F6] min-w-[180px] max-w-[180px] truncate transition-colors">
        {label}
      </td>
      {values.map((v, i) => {
        const isCurrent = i === currentIdx;
        const cls = valueClass ? valueClass(v, i) : v === 0 ? 'text-[#9CA3AF]' : 'text-[#6B7280]';
        return (
          <td
            key={i}
            className={`px-3.5 py-1 text-[13px] text-right tabular-nums whitespace-nowrap border-b border-r border-[#F3F4F6] ${cls} ${isCurrent ? 'bg-teal-50/50' : ''}`}
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

export default function ConsolidacaoGrid() {
  const { selectedMonth } = useMonthStore();

  const months = useMemo(() => {
    if (!selectedMonth || !selectedMonth.includes('-')) return [];
    return getNext24Months(selectedMonth);
  }, [selectedMonth]);

    // currentIdx should be dynamically found based on months
  const currentIdx = useMemo(() => {
    const now = new Date();
    const currentYYYYMM = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    return months.findIndex(m => m.value === currentYYYYMM);
  }, [months]);

  const { data: contaData, isLoading: loadingConta, isError: errorConta } = useConsolidacao(selectedMonth);
  

  if (months.length === 0) return null;

  const isLoading = loadingConta;
  const isError = errorConta;

  
  return (
    <div
      className="bg-white rounded-xl border border-[#E5E7EB] shadow-sm overflow-hidden h-full flex flex-col"
    >
      <div className="overflow-x-auto overflow-y-auto flex-1 scrollbar-thin scrollbar-thumb-[#9CA3AF] scrollbar-track-[#F3F4F6]">
        <table
          className="border-collapse"
          style={{ minWidth: 1400 }}
        >
          {/* ── CABEÇALHO DE MESES ── */}
          <thead>
            <tr>
              {/* Célula vazia da coluna de rótulos */}
              <th
                scope="col"
                className="sticky left-0 top-0 z-30 bg-white px-3.5 py-3 border-b-2 border-[#E5E7EB] min-w-[180px]"
              >
                <span className="sr-only">Rótulo das Linhas</span>
              </th>
              {months.map((m, i) => (
                <th
                  key={m.value}
                  scope="col"
                  className={`sticky top-0 z-20 bg-white px-3.5 py-3 text-right text-[11px] font-semibold uppercase tracking-[0.5px] whitespace-nowrap border-b-2 ${i === currentIdx ? 'text-teal-700 border-b-[#0D7377]' : 'text-[#9CA3AF] border-b-[#E5E7EB]'}`}
                  style={{ minWidth: 110 }}
                >
                  {m.label}
                </th>
              ))}
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
                <BlockHeader label="Receitas por Conta" colCount={24} color="green" />
                {!contaData.receitas || contaData.receitas.length === 0 ? (
                  <tr>
                    <td className="px-3.5 py-1 text-[13px] text-[#9CA3AF] border-b border-[#F3F4F6] sticky left-0 min-w-[180px]">Nenhuma receita cadastrada.</td>
                    {months.map((_, i) => <td key={i} className="border-b border-[#F3F4F6]" style={{ minWidth: 110 }} />)}
                  </tr>
                ) : (
                  contaData.receitas.map((linha: LinhaConsolidacaoDTO) => (
                    <DataRow key={`rec-${linha.contaId}`} label={linha.contaDescricao} values={linha.valoresMensais} currentIdx={currentIdx} />
                  ))
                )}

                <BlockSep colCount={24} />

                {/* ═══ BLOCO 2: DESPESAS POR CONTA ═══ */}
                <BlockHeader label="Despesas por Conta" colCount={24} color="red" />
                {!contaData.despesas || contaData.despesas.length === 0 ? (
                  <tr>
                    <td className="px-3.5 py-1 text-[13px] text-[#9CA3AF] border-b border-[#F3F4F6] sticky left-0 min-w-[180px]">Nenhuma despesa cadastrada.</td>
                    {months.map((_, i) => <td key={i} className="border-b border-[#F3F4F6]" style={{ minWidth: 110 }} />)}
                  </tr>
                ) : (
                  contaData.despesas.map((linha: LinhaConsolidacaoDTO) => (
                    <DataRow key={`desp-${linha.contaId}`} label={linha.contaDescricao} values={linha.valoresMensais} currentIdx={currentIdx} />
                  ))
                )}

                <BlockSep colCount={24} />

                {/* ═══ BLOCO DE CATEGORIAS ═══ */}
                <BlocoCategorias selectedMonth={selectedMonth} currentIdx={currentIdx} />
              </>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
