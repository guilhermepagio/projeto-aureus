import { useConsolidacaoCategoria, type LinhaConsolidacaoCategoriaDTO } from '../../hooks/useConsolidacaoCategoria';
import { formatCurrency } from '../../utils/currencyFormat';

interface BlocoCategoriasProps {
  selectedMonth: string | null;
  currentIdx: number;
}

export function BlocoCategorias({ selectedMonth, currentIdx }: BlocoCategoriasProps) {
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
      <td className="px-3.5 py-1 text-[13px] font-medium text-[#1A1A2E] sticky left-0 z-10 bg-white group-hover:bg-[#FAFAFA] border-b border-r border-[#F3F4F6] min-w-[180px] max-w-[180px] truncate transition-colors">
        {row.categoriaDescricao}
      </td>
      {(row.valoresMensais || []).map((val, i) => (
        <td
          key={i}
          className={`px-3.5 py-1 text-[13px] text-right tabular-nums whitespace-nowrap border-b border-r border-[#F3F4F6] ${i === currentIdx ? 'bg-teal-50/50' : ''} ${val === 0 ? 'text-[#9CA3AF]' : 'text-[#6B7280]'}`}
          style={{ minWidth: 110 }}
        >
          {formatCurrency(val || 0)}
        </td>
      ))}
    </tr>
  );

  const renderPercentageRow = (row: LinhaConsolidacaoCategoriaDTO) => (
    <tr key={`cat-pct-${row.categoriaId}`} className="group hover:bg-[#FAFAFA] transition-colors">
      <td className="px-3.5 py-1 text-[13px] font-medium text-[#1A1A2E] sticky left-0 z-10 bg-white group-hover:bg-[#FAFAFA] border-b border-r border-[#F3F4F6] min-w-[180px] max-w-[180px] truncate transition-colors">
        {row.categoriaDescricao}
      </td>
      {(row.valoresMensais || []).map((val, i) => {
        const total = totalDespesasMensais[i];
        let percent = 0;
        const safeVal = val || 0;
        if (total > 0) {
          percent = (safeVal / total) * 100;
        }

        return (
          <td
            key={i}
            className={`px-3.5 py-1 text-[13px] text-right tabular-nums whitespace-nowrap border-b border-r border-[#F3F4F6] ${i === currentIdx ? 'bg-teal-50/50' : ''} ${val === 0 ? 'text-[#9CA3AF]' : 'text-[#6B7280]'}`}
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
      <tr>
        <th
          scope="row"
          className="px-3.5 py-1 text-[11px] font-bold uppercase tracking-[0.8px] border-b border-[#E5E7EB] sticky left-0 z-10 text-left min-w-[180px] bg-amber-50 text-amber-700"
        >
          Categorias (R$)
        </th>
        <td colSpan={24} className="border-b border-[#E5E7EB] bg-amber-50" />
      </tr>

      {despesas.length === 0 ? (
        <tr>
          <td className="px-3.5 py-1 text-[13px] text-[#9CA3AF] border-b border-[#F3F4F6] sticky left-0 min-w-[180px]">
            Nenhuma despesa categorizada no período.
          </td>
          {Array.from({ length: 24 }).map((_, i) => (
            <td key={i} className="border-b border-[#F3F4F6]" style={{ minWidth: 110 }} />
          ))}
        </tr>
      ) : (
        despesas.map(d => renderMonetaryRow(d))
      )}

      {/* Separador entre blocos */}
      <tr>
        <td colSpan={25} className="h-4 border-b border-[#F3F4F6]" />
      </tr>

      {/* ═══ BLOCO 4: CATEGORIAS % ═══ */}
      <tr>
        <th
          scope="row"
          className="px-3.5 py-1 text-[11px] font-bold uppercase tracking-[0.8px] border-b border-[#E5E7EB] sticky left-0 z-10 text-left min-w-[180px] bg-amber-50 text-amber-700"
        >
          Categorias (%)
        </th>
        <td colSpan={24} className="border-b border-[#E5E7EB] bg-amber-50" />
      </tr>

      {despesas.length === 0 ? (
        <tr>
          <td className="px-3.5 py-1 text-[13px] text-[#9CA3AF] border-b border-[#F3F4F6] sticky left-0 min-w-[180px]">
            Nenhuma despesa categorizada no período.
          </td>
          {Array.from({ length: 24 }).map((_, i) => (
            <td key={i} className="border-b border-[#F3F4F6]" style={{ minWidth: 110 }} />
          ))}
        </tr>
      ) : (
        despesas.map(d => renderPercentageRow(d))
      )}
    </>
  );
}
