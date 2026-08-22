import ActionMenu from '../../components/ui/ActionMenu';
import React, { useState } from 'react';
import { useReceitasVariaveis, type ReceitaVariavel } from '../../hooks/useReceitasVariaveis';
import ReceitaVariavelFormModal from './components/ReceitaVariavelFormModal';
import DeleteConfirmModal from './components/DeleteConfirmModal';
import { useMonthStore } from '../../store/monthStore';
import EmptyState from '../../components/ui/EmptyState';

const ReceitasVariaveisPage: React.FC = () => {
  const { data: receitas, isLoading, isError } = useReceitasVariaveis();
  const { selectedMonth, isGlobalFilterActive, toggleGlobalFilter } = useMonthStore();
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedReceita, setSelectedReceita] = useState<ReceitaVariavel | null>(null);

  const handleCreate = () => {
    setSelectedReceita(null);
    setIsFormModalOpen(true);
  };

  const handleEdit = (receita: ReceitaVariavel) => {
    setSelectedReceita(receita);
    setIsFormModalOpen(true);
  };

  const handleDelete = (receita: ReceitaVariavel) => {
    setSelectedReceita(receita);
    setIsDeleteModalOpen(true);
  };

  const filteredReceitas = React.useMemo(() => {
    if (!receitas) return [];
    if (isGlobalFilterActive) return receitas;
    return receitas.filter(r => {
      // For variable expenses, check if selectedMonth falls between dataInicio and dataFim
      const start = r.dataInicio ? r.dataInicio.substring(0, 7) : ''; // YYYY-MM
      const end = r.dataFim ? r.dataFim.substring(0, 7) : '';
      return selectedMonth >= start && selectedMonth <= end;
    });
  }, [receitas, selectedMonth, isGlobalFilterActive]);

  if (isLoading) return <div className="p-6">Carregando receitas variáveis...</div>;
  if (isError) return <div className="p-6 text-red-600">Erro ao carregar receitas variáveis.</div>;

  const isEmpty = filteredReceitas.length === 0;

  return (
    <div className="px-4 pb-4 w-full">
      <div className="mb-4 mt-2 flex justify-between items-center">
        <div className="pl-2 border-l-4 border-primary">
          <h1 className="text-2xl font-bold text-gray-800">Receitas Variáveis</h1>
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
              <div className={`block w-10 h-6 rounded-full transition-colors ${isGlobalFilterActive ? 'bg-primary' : 'bg-gray-300'}`}></div>
              <div className={`dot absolute left-1 top-1 bg-white w-4 h-4 rounded-full transition-transform ${isGlobalFilterActive ? 'transform translate-x-4' : ''}`}></div>
            </div>
          </label>
          <button
            onClick={handleCreate}
            className="cursor-pointer h-8 px-4 flex items-center justify-center bg-primary hover:bg-primary-light text-white rounded-md text-xs font-semibold whitespace-nowrap shadow-sm"
          >
            + Nova Receita
          </button>
        </div>
      </div>
      {isEmpty ? (
        <EmptyState
          title="Nenhuma receita variável"
          description={isGlobalFilterActive ? "Comece criando sua primeira receita variável." : "Nenhuma receita para o mês selecionado."}
          icon={
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true" className="w-12 h-12">
              <path vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          }
          action={{ label: 'Nova Receita Variável', onClick: handleCreate }}
        />
      ) : (
        <div className="bg-white shadow sm:rounded-2xl overflow-visible mt-2 w-full">
          <table className="min-w-max w-full divide-y divide-gray-200 table-fixed">
            <thead className="bg-gray-50 sticky top-0 z-10 shadow-sm">
              <tr className="divide-x divide-gray-200 h-[44px]">
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[345px]">
                  Descrição
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
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[330px]">
                  Observações
                </th>
                <th scope="col" className="sticky right-0 px-1 py-1 bg-gray-50 z-20 shadow-[-4px_0_8px_rgba(0,0,0,0.05)] w-[44px]">
                  <span className="sr-only">Ações</span>
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredReceitas.map((receita) => (
                <tr key={receita.id} className="group even:bg-gray-200 odd:bg-white text-sm divide-x divide-gray-200">
                  <td className="px-3 py-1 font-medium text-gray-900 align-middle">
                    <div className="line-clamp-3 whitespace-normal break-words" title={receita.descricao}>
                      {receita.descricao}
                    </div>
                  </td>
                  <td className="px-3 py-1 text-center text-gray-900 tabular-nums whitespace-nowrap align-middle">
                    {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(receita.valorParcela)}
                  </td>
                  <td className="px-3 py-1 text-center tabular-nums text-gray-900 align-middle">
                    {receita.quantidadeParcelas}
                  </td>
                  <td className="px-1 py-1 text-center text-gray-900 tabular-nums whitespace-nowrap align-middle">
                    {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(receita.valorParcela * receita.quantidadeParcelas)}
                  </td>
                  <td className="px-3 py-1 text-center text-gray-900 align-middle">
                    <div className="line-clamp-3 whitespace-normal break-words" title={receita.categoria?.descricao}>
                      {receita.categoria?.descricao}
                    </div>
                  </td>
                  <td className="px-3 py-1 text-center text-gray-900 align-middle">
                    <div className="line-clamp-3 whitespace-normal break-words" title={receita.conta?.descricao}>
                      {receita.conta?.descricao}
                    </div>
                  </td>
                  <td className="px-3 py-1 text-gray-900 align-middle">
                    <div className="line-clamp-3 whitespace-normal break-words" title={receita.observacoes}>
                      {receita.observacoes || '-'}
                    </div>
                  </td>
                  <td className="sticky right-0 px-1 py-1 group-even:bg-gray-200 group-odd:bg-white shadow-[-4px_0_8px_rgba(0,0,0,0.05)] align-middle">
                    <div className="flex justify-center w-full"><ActionMenu onEdit={() => handleEdit(receita)} onDelete={() => handleDelete(receita)} /></div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <ReceitaVariavelFormModal
        isOpen={isFormModalOpen}
        onClose={() => setIsFormModalOpen(false)}
        receitaToEdit={selectedReceita}
      />

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        receitaToDelete={selectedReceita}
      />
    </div>
  );
};

export default ReceitasVariaveisPage;
