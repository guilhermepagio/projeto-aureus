import { useMemo } from 'react';
import { useMonthStore } from '../../store/monthStore';
import { useConsolidacao, type LinhaConsolidacaoDTO } from '../../hooks/useConsolidacao';

const getNext24Months = (startMonthYYYYMM: string) => {
  const [yearStr, monthStr] = startMonthYYYYMM.split('-');
  let year = parseInt(yearStr, 10);
  let monthIndex = parseInt(monthStr, 10) - 1;

  const result = [];
  for (let i = 0; i < 24; i++) {
    result.push({
      label: `${String(monthIndex + 1).padStart(2, '0')}/${year}`,
      value: `${year}-${String(monthIndex + 1).padStart(2, '0')}`
    });
    
    monthIndex++;
    if (monthIndex > 11) {
      monthIndex = 0;
      year++;
    }
  }
  return result;
};

const formatter = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

export default function ConsolidacaoGrid() {
  const { selectedMonth } = useMonthStore();
  
  const { data, isLoading, isError } = useConsolidacao(selectedMonth);

  const months = useMemo(() => {
    if (!selectedMonth || !selectedMonth.includes('-')) return [];
    return getNext24Months(selectedMonth);
  }, [selectedMonth]);

  if (months.length === 0) return null;

  return (
    <div className="w-full border rounded-lg bg-white shadow-sm h-full flex flex-col">
      <div className="overflow-auto h-[calc(100vh-250px)] md:h-[calc(100vh-180px)]">
        <table className="w-full border-separate border-spacing-0 min-w-max relative">
          <thead>
            <tr className="bg-gray-50">
              <th aria-hidden="true" className="sticky left-0 top-0 z-20 bg-gray-50 p-4 text-left font-medium text-gray-700 min-w-[200px] border-b border-r border-gray-200">
                {/* Empty cell for row labels */}
              </th>
              {months.map(m => (
                <th scope="col" key={m.value} className="sticky top-0 z-10 bg-gray-50 p-4 text-center font-medium text-gray-700 min-w-[120px] border-b border-r border-gray-200">
                  {m.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {isLoading && (
              <tr>
                <td colSpan={25} className="p-8 text-center text-gray-500">
                  Carregando consolidação...
                </td>
              </tr>
            )}
            
            {isError && (
              <tr>
                <td colSpan={25} className="p-8 text-center text-red-500">
                  Erro ao carregar dados da consolidação.
                </td>
              </tr>
            )}

            {!isLoading && !isError && data && (
              <>
                {/* Receitas */}
                <tr className="bg-green-50">
                  <th scope="row" className="sticky left-0 z-10 bg-green-50 p-4 text-left font-bold text-green-800 border-b border-r border-green-200" colSpan={25}>
                    Receitas por Conta
                  </th>
                </tr>
                {data.receitas.map((linha: LinhaConsolidacaoDTO) => (
                  <tr key={`rec-${linha.contaId}`} className="group hover:bg-gray-50 transition-colors">
                    <th scope="row" className="sticky left-0 z-10 bg-white group-hover:bg-gray-50 p-4 text-left font-medium text-gray-800 border-b border-r border-gray-200">
                      {linha.contaDescricao}
                    </th>
                    {linha.valoresMensais.map((valor, idx) => (
                      <td key={idx} className="p-4 text-center text-gray-600 border-b border-r border-gray-200">
                        {formatter.format(valor)}
                      </td>
                    ))}
                  </tr>
                ))}
                {(!data.receitas || data.receitas.length === 0) && (
                  <tr>
                    <td colSpan={25} className="p-4 text-center text-gray-500 border-b border-r border-gray-200">
                      Nenhuma conta encontrada para receitas.
                    </td>
                  </tr>
                )}

                {/* Despesas */}
                <tr className="bg-red-50">
                  <th scope="row" className="sticky left-0 z-10 bg-red-50 p-4 text-left font-bold text-red-800 border-b border-r border-red-200" colSpan={25}>
                    Despesas por Conta
                  </th>
                </tr>
                {data.despesas.map((linha: LinhaConsolidacaoDTO) => (
                  <tr key={`desp-${linha.contaId}`} className="group hover:bg-gray-50 transition-colors">
                    <th scope="row" className="sticky left-0 z-10 bg-white group-hover:bg-gray-50 p-4 text-left font-medium text-gray-800 border-b border-r border-gray-200">
                      {linha.contaDescricao}
                    </th>
                    {linha.valoresMensais.map((valor, idx) => (
                      <td key={idx} className="p-4 text-center text-gray-600 border-b border-r border-gray-200">
                        {formatter.format(valor)}
                      </td>
                    ))}
                  </tr>
                ))}
                {data.despesas.length === 0 && (
                  <tr>
                    <td colSpan={25} className="p-4 text-center text-gray-500 border-b border-gray-200">
                      Nenhuma despesa encontrada.
                    </td>
                  </tr>
                )}
              </>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
