import { useMemo } from 'react';
import { useMonthStore } from '../../store/monthStore';
import { useConsolidacao, type LinhaConsolidacaoDTO } from '../../hooks/useConsolidacao';
import { useConsolidacaoCategoria, type LinhaConsolidacaoCategoriaDTO } from '../../hooks/useConsolidacaoCategoria';

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

const fmtBRL = new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

function fmtVal(v: number) {
  return fmtBRL.format(v);
}

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
    green: 'bg-[#F0FDF4]',
    red:   'bg-[#FEF2F2]',
    amber: 'bg-[#FDF6E8]',
    teal:  'bg-[#E8F4F4]',
  };
  const text: Record<string, string> = {
    green: 'text-[#16A34A]',
    red:   'text-[#DC2626]',
    amber: 'text-[#92740D]',
    teal:  'text-[#0D7377]',
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
            className={`px-3.5 py-1 text-[13px] text-right tabular-nums whitespace-nowrap border-b border-r border-[#F3F4F6] ${cls} ${isCurrent ? 'bg-[rgba(13,115,119,0.03)]' : ''}`}
            style={{ minWidth: 110 }}
          >
            {fmtVal(v)}
          </td>
        );
      })}
    </tr>
  );
}

/** Linha de dado — valores percentuais */
function PercentRow({
  label,
  values,
  totals,
  currentIdx,
}: {
  label: string;
  values: number[];
  totals: number[];
  currentIdx: number;
}) {
  return (
    <tr className="group hover:bg-[#FAFAFA] transition-colors">
      <td className="px-3.5 py-1 text-[13px] font-medium text-[#1A1A2E] sticky left-0 z-10 bg-white group-hover:bg-[#FAFAFA] border-b border-r border-[#F3F4F6] min-w-[180px] max-w-[180px] truncate transition-colors">
        {label}
      </td>
      {values.map((v, i) => {
        const total = totals[i] || 0;
        const pct = total > 0 ? (v / total) * 100 : 0;
        const isCurrent = i === currentIdx;
        const cls = pct === 0 ? 'text-[#9CA3AF]' : 'text-[#6B7280]';
        return (
          <td
            key={i}
            className={`px-3.5 py-1 text-[13px] text-right tabular-nums whitespace-nowrap border-b border-r border-[#F3F4F6] ${cls} ${isCurrent ? 'bg-[rgba(13,115,119,0.03)]' : ''}`}
            style={{ minWidth: 110 }}
          >
            {pct.toFixed(1).replace('.', ',')}%
          </td>
        );
      })}
    </tr>
  );
}

/** Linha de resumo (negrito, borda superior) */
function SummaryRow({
  label,
  values,
  currentIdx,
  strong,
}: {
  label: string;
  values: number[];
  currentIdx: number;
  strong?: boolean;
}) {
  const rowClass = strong
    ? 'border-t-2 border-t-[#0D7377]'
    : 'border-t border-t-[#E5E7EB]';

  return (
    <tr className={`group hover:bg-[#FAFAFA] transition-colors ${rowClass}`}>
      <td className={`px-3.5 py-1 text-[13px] sticky left-0 z-10 bg-white group-hover:bg-[#FAFAFA] border-b border-r border-[#F3F4F6] min-w-[180px] max-w-[180px] truncate transition-colors ${strong ? 'font-bold text-[14px]' : 'font-semibold'}`}>
        {label}
      </td>
      {values.map((v, i) => {
        const isCurrent = i === currentIdx;
        const cls = v >= 0 ? 'text-[#16A34A] font-semibold' : 'text-[#DC2626] font-semibold';
        return (
          <td
            key={i}
            className={`px-3.5 py-1 text-[13px] text-right tabular-nums whitespace-nowrap border-b border-r border-[#F3F4F6] ${cls} ${isCurrent ? 'bg-[rgba(13,115,119,0.03)]' : ''} ${strong ? 'font-bold text-[14px]' : ''}`}
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

  const currentIdx = 0; // coluna 0 = mês selecionado

  const { data: contaData, isLoading: loadingConta, isError: errorConta } = useConsolidacao(selectedMonth);
  const { data: catData, isLoading: loadingCat, isError: errorCat } = useConsolidacaoCategoria(selectedMonth);

  if (months.length === 0) return null;

  const isLoading = loadingConta || loadingCat;
  const isError = errorConta || errorCat;

  // Totais mensais de despesas (para cálculo de percentuais)
  const totalDespesasMensais: number[] = Array(24).fill(0);
  if (catData?.despesas) {
    catData.despesas.forEach(d => {
      (d.valoresMensais || []).forEach((v, i) => {
        totalDespesasMensais[i] = Math.round((totalDespesasMensais[i] + v) * 100) / 100;
      });
    });
  }

  // Sobra do mês = total receitas - total despesas por conta
  const sobraMensais: number[] = Array(24).fill(0);
  if (contaData) {
    const receitasTotais: number[] = Array(24).fill(0);
    const despesasTotais: number[] = Array(24).fill(0);
    contaData.receitas.forEach(r => r.valoresMensais.forEach((v, i) => { receitasTotais[i] += v; }));
    contaData.despesas.forEach(d => d.valoresMensais.forEach((v, i) => { despesasTotais[i] += v; }));
    for (let i = 0; i < 24; i++) {
      sobraMensais[i] = Math.round((receitasTotais[i] - despesasTotais[i]) * 100) / 100;
    }
  }

  // Sobra retroativa acumulada
  const sobraRetroAcum: number[] = [];
  let acum = 0;
  for (let i = 0; i < 24; i++) {
    acum = Math.round((acum + sobraMensais[i]) * 100) / 100;
    sobraRetroAcum.push(acum);
  }

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
              />
              {months.map((m, i) => (
                <th
                  key={m.value}
                  scope="col"
                  className={`sticky top-0 z-20 bg-white px-3.5 py-3 text-right text-[11px] font-semibold uppercase tracking-[0.5px] whitespace-nowrap border-b-2 ${i === currentIdx ? 'text-[#0D7377] border-b-[#0D7377]' : 'text-[#9CA3AF] border-b-[#E5E7EB]'}`}
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
                <td className="py-16 text-center text-[13px] text-[#DC2626] sticky left-0" colSpan={25}>
                  Erro ao carregar dados da consolidação.
                </td>
              </tr>
            )}

            {!isLoading && !isError && contaData && (
              <>
                {/* ═══ BLOCO 1: RECEITAS POR CONTA ═══ */}
                <BlockHeader label="Receitas por Conta" colCount={24} color="green" />
                {contaData.receitas.length === 0 ? (
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
                {contaData.despesas.length === 0 ? (
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

                {/* ═══ BLOCO 3: CATEGORIAS R$ ═══ */}
                {catData && catData.despesas.length > 0 && (
                  <>
                    <BlockHeader label="Categorias (R$)" colCount={24} color="amber" />
                    {catData.despesas.map((linha: LinhaConsolidacaoCategoriaDTO) => (
                      <DataRow key={`cat-brl-${linha.categoriaId}`} label={linha.categoriaDescricao} values={linha.valoresMensais} currentIdx={currentIdx} />
                    ))}

                    <BlockSep colCount={24} />

                    {/* ═══ BLOCO 4: CATEGORIAS % ═══ */}
                    <BlockHeader label="Categorias (%)" colCount={24} color="amber" />
                    {catData.despesas.map((linha: LinhaConsolidacaoCategoriaDTO) => (
                      <PercentRow
                        key={`cat-pct-${linha.categoriaId}`}
                        label={linha.categoriaDescricao}
                        values={linha.valoresMensais}
                        totals={totalDespesasMensais}
                        currentIdx={currentIdx}
                      />
                    ))}

                    <BlockSep colCount={24} />
                  </>
                )}

                {/* ═══ BLOCO 5: RESUMO GERAL ═══ */}
                <BlockHeader label="Resumo Geral" colCount={24} color="teal" />
                <SummaryRow label="Total Gasto no Mês" values={totalDespesasMensais} currentIdx={currentIdx} />
                <SummaryRow label="Sobra do Mês" values={sobraMensais} currentIdx={currentIdx} />
                <SummaryRow label="Sobra Retroativa Acum." values={sobraRetroAcum} currentIdx={currentIdx} strong />
              </>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
