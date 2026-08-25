import { useConsolidacaoCategoria, type LinhaConsolidacaoCategoriaDTO } from '../../hooks/useConsolidacaoCategoria';
import { formatCurrency } from '../../utils/currencyFormat';

interface BlocoCategoriasProps {
  selectedMonth: string;
}

export function BlocoCategorias({ selectedMonth }: BlocoCategoriasProps) {
  const { data, isLoading, isError } = useConsolidacaoCategoria(selectedMonth);

  if (isLoading) {
    return <div className="p-4 text-center">Carregando categorias...</div>;
  }

  if (isError || !data) {
    return <div className="p-4 text-center text-red-500">Erro ao carregar os dados das categorias.</div>;
  }

  const { receitas, despesas } = data;

  // Calculamos totais mensais de receitas por categoria
  const totalReceitasMensais = Array(24).fill(0);
  receitas.forEach(r => {
    r.valoresMensais.forEach((val, i) => {
      totalReceitasMensais[i] += val;
    });
  });

  // Calculamos totais mensais de despesas por categoria
  const totalDespesasMensais = Array(24).fill(0);
  despesas.forEach(d => {
    d.valoresMensais.forEach((val, i) => {
      totalDespesasMensais[i] += val;
    });
  });

  const renderRow = (row: LinhaConsolidacaoCategoriaDTO, isTotal: boolean = false) => (
    <tr key={row.categoriaId} className={`hover:bg-gray-50 border-b ${isTotal ? 'font-semibold bg-gray-50' : ''}`}>
      <td className="sticky left-0 bg-white min-w-[200px] max-w-[200px] p-3 shadow-[1px_0_0_0_#e5e7eb]">
        <div className="truncate" title={row.categoriaDescricao}>
          {row.categoriaDescricao}
        </div>
      </td>
      {row.valoresMensais.map((val, i) => (
        <td key={i} className="min-w-[120px] p-3 text-right tabular-nums whitespace-nowrap border-l">
          {formatCurrency(val)}
        </td>
      ))}
    </tr>
  );

  return (
    <>
      {/* Bloco de Receitas por Categoria */}
      <tr className="bg-gray-100/50">
        <td colSpan={25} className="p-3 sticky left-0 font-semibold text-gray-700 shadow-[1px_0_0_0_#e5e7eb]">
          Receitas por Categoria
        </td>
      </tr>
      
      {receitas.length === 0 ? (
        <tr>
          <td colSpan={25} className="p-3 text-center text-gray-500 sticky left-0 shadow-[1px_0_0_0_#e5e7eb]">
            Nenhuma receita cadastrada
          </td>
        </tr>
      ) : (
        <>
          {receitas.map(r => renderRow(r))}
          {/* Total Row */}
          <tr className="font-semibold bg-gray-50 border-b">
            <td className="sticky left-0 bg-white min-w-[200px] max-w-[200px] p-3 shadow-[1px_0_0_0_#e5e7eb]">
              Total Receitas
            </td>
            {totalReceitasMensais.map((val, i) => (
              <td key={i} className="min-w-[120px] p-3 text-right tabular-nums whitespace-nowrap border-l">
                {formatCurrency(val)}
              </td>
            ))}
          </tr>
        </>
      )}

      {/* Bloco de Despesas por Categoria */}
      <tr className="bg-gray-100/50">
        <td colSpan={25} className="p-3 sticky left-0 font-semibold text-gray-700 shadow-[1px_0_0_0_#e5e7eb]">
          Despesas por Categoria
        </td>
      </tr>
      
      {despesas.length === 0 ? (
        <tr>
          <td colSpan={25} className="p-3 text-center text-gray-500 sticky left-0 shadow-[1px_0_0_0_#e5e7eb]">
            Nenhuma despesa cadastrada
          </td>
        </tr>
      ) : (
        <>
          {despesas.map(d => renderRow(d))}
          {/* Total Row */}
          <tr className="font-semibold bg-gray-50 border-b">
            <td className="sticky left-0 bg-white min-w-[200px] max-w-[200px] p-3 shadow-[1px_0_0_0_#e5e7eb]">
              Total Despesas
            </td>
            {totalDespesasMensais.map((val, i) => (
              <td key={i} className="min-w-[120px] p-3 text-right tabular-nums whitespace-nowrap border-l">
                {formatCurrency(val)}
              </td>
            ))}
          </tr>
        </>
      )}
    </>
  );
}
