import ActionMenu from '../../components/ui/ActionMenu';
import React, { useState } from 'react';
import { useDespesasVariaveis, type DespesaVariavel } from '../../hooks/useDespesasVariaveis';
import DespesaVariavelFormModal from './components/DespesaVariavelFormModal';
import DeleteConfirmModal from './components/DeleteConfirmModal';
import { useMonthStore } from '../../store/monthStore';
import EmptyState from '../../components/ui/EmptyState';

const DespesasVariaveisPage: React.FC = () => {
  const { data: despesas, isLoading, isError } = useDespesasVariaveis();
  const { selectedMonth, isGlobalFilterActive, toggleGlobalFilter } = useMonthStore();
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedDespesa, setSelectedDespesa] = useState<DespesaVariavel | null>(null);

  const handleCreate = () => {
    setSelectedDespesa(null);
    setIsFormModalOpen(true);
  };

  const handleEdit = (despesa: DespesaVariavel) => {
    setSelectedDespesa(despesa);
    setIsFormModalOpen(true);
  };

  const handleDelete = (despesa: DespesaVariavel) => {
    setSelectedDespesa(despesa);
    setIsDeleteModalOpen(true);
  };

  const filteredDespesas = React.useMemo(() => {
    if (!despesas) return [];
    if (isGlobalFilterActive) return despesas;
    return despesas.filter(d => {
      // For variable expenses, check if selectedMonth falls between dataInicio and dataFim
      const start = d.dataInicio ? d.dataInicio.substring(0, 7) : ''; // YYYY-MM
      const end = d.dataFim ? d.dataFim.substring(0, 7) : '';
      return selectedMonth >= start && selectedMonth <= end;
    });
  }, [despesas, selectedMonth, isGlobalFilterActive]);

  if (isLoading) return <div className="p-6">Carregando despesas variáveis...</div>;
  if (isError) return <div className="p-6 text-red-600">Erro ao carregar despesas variáveis.</div>;

  const isEmpty = filteredDespesas.length === 0;

  return (
    <div className="px-4 pb-4 w-full">
      <div className="mb-4 mt-2 flex justify-between items-center">
        <div className="pl-2 border-l-4 border-red-600">
          <h1 className="text-2xl font-bold text-gray-800">Despesas Variáveis</h1>
        </div>
        <div className="flex items-center space-x-4">
          <label className="flex items-center space-x-2 cursor-pointer">
            <span className="text-sm text-gray-600 font-medium">Ver Todos</span>
            <div className="relative" role="switch" aria-checked={isGlobalFilterActive}>
              <input
                type="checkbox"
                className="sr-only"
                checked={isGlobalFilterActive}
                onChange={toggleGlobalFilter}
                aria-label="Ativar Filtro Global"
              />
              <div className={`block w-10 h-6 rounded-full transition-colors ${isGlobalFilterActive ? 'bg-red-600' : 'bg-gray-300'}`}></div>
              <div className={`dot absolute left-1 top-1 bg-white w-4 h-4 rounded-full transition-transform ${isGlobalFilterActive ? 'transform translate-x-4' : ''}`}></div>
            </div>
          </label>
          <button
            onClick={handleCreate}
            className="cursor-pointer h-8 px-4 flex items-center justify-center bg-red-600 hover:bg-red-700 text-white rounded-md text-xs font-semibold whitespace-nowrap shadow-sm"
          >
            + Nova Despesa
          </button>
        </div>
      </div>
      {isEmpty ? (
        <EmptyState
          title="Nenhuma despesa variável"
          description={isGlobalFilterActive ? "Comece criando sua primeira despesa variável." : "Nenhuma despesa para o mês selecionado."}
          icon={
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true" className="w-12 h-12">
              <path vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          }
          action={{ label: 'Nova Despesa Variável', onClick: handleCreate, color: 'danger' }}
        />
      ) : (
        <div className="bg-white shadow sm:rounded-2xl overflow-visible mt-2 w-full">
          <table className="min-w-max w-full divide-y divide-gray-200 table-fixed">
            <thead className="bg-gray-50 sticky top-0 z-10 shadow-sm">
              <tr className="divide-x divide-gray-200 h-[44px]">
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[255px]">
                  Descrição
                </th>
                <th scope="col" className="px-1 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[80px]">
                  Data Compra
                </th>
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[160px]">
                  Local Compra
                </th>
                <th scope="col" className="px-3 py-1 text-center tabular-nums text-xs font-semibold text-gray-700 uppercase tracking-wider w-[120px]">
                  Valor Parcela
                </th>
                <th scope="col" className="px-3 py-1 text-center tabular-nums text-xs font-semibold text-gray-700 uppercase tracking-wider w-[80px]">
                  Qtd. Parcelas
                </th>
                <th scope="col" className="px-1 py-1 text-center tabular-nums text-xs font-semibold text-gray-700 uppercase tracking-wider w-[120px]">
                  Valor Total
                </th>
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[150px]">
                  Categoria
                </th>
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[150px]">
                  Conta
                </th>
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[180px]">
                  Observações
                </th>
                <th scope="col" className="sticky right-0 px-1 py-1 bg-gray-50 z-20 shadow-[-4px_0_8px_rgba(0,0,0,0.05)] w-[44px]">
                  <span className="sr-only">Ações</span>
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredDespesas.map((despesa) => (
                <tr key={despesa.id} className="group even:bg-gray-200 odd:bg-white text-sm divide-x divide-gray-200">
                  <td className="px-3 py-1 font-medium text-gray-900 align-middle">
                    <div className="line-clamp-3 whitespace-normal break-words" title={despesa.descricao}>
                      {despesa.descricao}
                    </div>
                  </td>
                  <td className="px-1 py-1 text-center text-gray-900 align-middle whitespace-nowrap">
                    {despesa.dataCompra ? despesa.dataCompra.split('-').reverse().join('/') : '-'}
                  </td>
                  <td className="px-3 py-1 text-center text-gray-900 align-middle">
                    <div className="line-clamp-3 whitespace-normal break-words" title={despesa.localCompra || ''}>
                      {despesa.localCompra || '-'}
                    </div>
                  </td>
                  <td className="px-3 py-1 text-center text-gray-900 tabular-nums whitespace-nowrap align-middle">
                    {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(despesa.valorParcela)}
                  </td>
                  <td className="px-3 py-1 text-center tabular-nums text-gray-900 align-middle">
                    {despesa.quantidadeParcelas}
                  </td>
                  <td className="px-1 py-1 text-center text-gray-900 tabular-nums whitespace-nowrap align-middle">
                    {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(despesa.valorParcela * despesa.quantidadeParcelas)}
                  </td>
                  <td className="px-3 py-1 text-center text-gray-900 align-middle">
                    <div className="line-clamp-3 whitespace-normal break-words" title={despesa.categoria?.descricao}>
                      {despesa.categoria?.descricao}
                    </div>
                  </td>
                  <td className="px-3 py-1 text-center text-gray-900 align-middle">
                    <div className="line-clamp-3 whitespace-normal break-words" title={despesa.conta?.descricao}>
                      {despesa.conta?.descricao}
                    </div>
                  </td>
                  <td className="px-3 py-1 text-gray-900 align-middle">
                    <div className="line-clamp-3 whitespace-normal break-words" title={despesa.observacoes}>
                      {despesa.observacoes || '-'}
                    </div>
                  </td>
                  <td className="sticky right-0 px-1 py-1 group-even:bg-gray-200 group-odd:bg-white shadow-[-4px_0_8px_rgba(0,0,0,0.05)] align-middle">
                    <div className="flex justify-center w-full"><ActionMenu onEdit={() => handleEdit(despesa)} onDelete={() => handleDelete(despesa)} /></div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <DespesaVariavelFormModal
        isOpen={isFormModalOpen}
        onClose={() => setIsFormModalOpen(false)}
        despesaToEdit={selectedDespesa}
      />

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        despesaToDelete={selectedDespesa}
      />
    </div>
  );
};

export default DespesasVariaveisPage;
