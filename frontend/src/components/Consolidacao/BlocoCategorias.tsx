import { useConsolidacaoCategoria, type LinhaConsolidacaoCategoriaDTO } from '../../hooks/useConsolidacaoCategoria';
import { formatCurrency } from '../../utils/currencyFormat';

interface BlocoCategoriasProps {
  selectedMonth: string | null;
}

export function BlocoCategorias({ selectedMonth }: BlocoCategoriasProps) {
  if (!selectedMonth) return null;

  const { data, isLoading, isError } = useConsolidacaoCategoria(selectedMonth);

  if (isLoading) {
    return (
      <tr>
        <td colSpan={25} className="p-8 text-center text-gray-500">
          Carregando categorias...
        </td>
      </tr>
    );
  }

  if (isError || !data) {
    return (
      <tr>
        <td colSpan={25} className="p-8 text-center text-red-500">
          Erro ao carregar os dados das categorias.
        </td>
      </tr>
    );
  }

  const { despesas } = data;

  // Calcula totais mensais (seguro contra precisão)
  const totalDespesasMensais = Array(24).fill(0);
  despesas.forEach(d => {
    (d.valoresMensais || []).forEach((val, i) => {
      // Evita problemas de precisão arredondando para 2 casas
      totalDespesasMensais[i] = Math.round((totalDespesasMensais[i] + val) * 100) / 100;
    });
  });

  const renderMonetaryRow = (row: LinhaConsolidacaoCategoriaDTO) => (
    <tr key={row.categoriaId} className="group hover:bg-gray-50 transition-colors">
      <th scope="row" className="sticky left-0 z-10 bg-white group-hover:bg-gray-50 p-4 text-left font-medium text-gray-800 border-b border-r border-gray-200">
        <div className="truncate" title={row.categoriaDescricao}>
          {row.categoriaDescricao}
        </div>
      </th>
      {(row.valoresMensais || []).map((val, i) => (
        <td key={i} className={`p-4 text-center tabular-nums whitespace-nowrap border-b border-r border-gray-200 ${val === 0 ? 'text-gray-400' : 'text-gray-600'}`}>
          {formatCurrency(val)}
        </td>
      ))}
    </tr>
  );

  const renderPercentageRow = (row: LinhaConsolidacaoCategoriaDTO) => (
    <tr key={row.categoriaId} className="group hover:bg-gray-50 transition-colors">
      <th scope="row" className="sticky left-0 z-10 bg-white group-hover:bg-gray-50 p-4 text-left font-medium text-gray-800 border-b border-r border-gray-200">
        <div className="truncate" title={row.categoriaDescricao}>
          {row.categoriaDescricao}
        </div>
      </th>
      {(row.valoresMensais || []).map((val, i) => {
        const total = totalDespesasMensais[i];
        let percent = 0;
        if (total > 0) {
          percent = (val / total) * 100;
        }
        
        return (
          <td key={i} className={`p-4 text-center tabular-nums whitespace-nowrap border-b border-r border-gray-200 ${val === 0 ? 'text-gray-400' : 'text-gray-600'}`}>
            {percent.toFixed(1).replace('.', ',')}%
          </td>
        );
      })}
    </tr>
  );

  return (
    <>
      {/* ═══ BLOCO 3: CATEGORIAS R$ ═══ */}
      <tr className="bg-amber-50">
        <th scope="row" className="sticky left-0 z-10 bg-amber-50 p-4 text-left font-bold text-amber-800 border-b border-r border-amber-200" colSpan={25}>
          Categorias (R$)
        </th>
      </tr>
      
      {despesas.length === 0 ? (
        <tr>
          <td colSpan={25} className="p-4 text-center text-gray-500 border-b border-r border-gray-200">
            Nenhuma despesa categorizada no período.
          </td>
        </tr>
      ) : (
        despesas.map(d => renderMonetaryRow(d))
      )}

      {/* Separador entre blocos */}
      <tr>
        <td colSpan={25} className="h-8 bg-gray-50 border-b border-gray-200"></td>
      </tr>

      {/* ═══ BLOCO 4: CATEGORIAS % ═══ */}
      <tr className="bg-amber-50">
        <th scope="row" className="sticky left-0 z-10 bg-amber-50 p-4 text-left font-bold text-amber-800 border-b border-r border-amber-200" colSpan={25}>
          Categorias (%)
        </th>
      </tr>
      
      {despesas.length === 0 ? (
        <tr>
          <td colSpan={25} className="p-4 text-center text-gray-500 border-b border-r border-gray-200">
            Nenhuma despesa categorizada no período.
          </td>
        </tr>
      ) : (
        despesas.map(d => renderPercentageRow(d))
      )}
    </>
  );
}
