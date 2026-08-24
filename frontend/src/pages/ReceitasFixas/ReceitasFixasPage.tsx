import ActionMenu from '../../components/ui/ActionMenu';
import React, { useState } from 'react';
import { useReceitasFixas, type ReceitaFixa } from '../../hooks/useReceitasFixas';
import ReceitaFixaFormModal from './components/ReceitaFixaFormModal';
import DeleteConfirmModal from './components/DeleteConfirmModal';
import EmptyState from '../../components/ui/EmptyState';

const ReceitasFixasPage: React.FC = () => {
  const { data: receitas, isLoading, isError } = useReceitasFixas();
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedReceita, setSelectedReceita] = useState<ReceitaFixa | null>(null);

  const handleCreate = () => {
    setSelectedReceita(null);
    setIsFormModalOpen(true);
  };

  const handleEdit = (receita: ReceitaFixa) => {
    setSelectedReceita(receita);
    setIsFormModalOpen(true);
  };

  const handleDelete = (receita: ReceitaFixa) => {
    setSelectedReceita(receita);
    setIsDeleteModalOpen(true);
  };

  const filteredReceitas = React.useMemo(() => {
    if (!receitas) return [];
    return receitas;
  }, [receitas]);

  if (isLoading) return <div className="p-6">Carregando receitas fixas...</div>;
  if (isError) return <div className="p-6 text-red-600">Erro ao carregar receitas fixas.</div>;

  const isEmpty = filteredReceitas.length === 0;

  return (
    <div className="px-4 pb-4 w-full h-full flex flex-col">
      <div className="mb-4 mt-2 flex justify-between items-center shrink-0">
        <div className="pl-2 border-l-4 border-primary">
          <h1 className="text-2xl font-bold text-gray-800">Receitas Fixas</h1>
        </div>
        <button
          onClick={handleCreate}
          className="cursor-pointer h-8 px-4 flex items-center justify-center bg-primary hover:bg-primary-light text-white rounded-md text-xs font-semibold whitespace-nowrap shadow-sm"
        >
          + Nova Receita
        </button>
      </div>
      {isEmpty ? (
        <EmptyState
          title="Nenhuma receita fixa"
          description="Comece criando sua primeira receita fixa."
          icon={
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true" className="w-12 h-12">
              <path vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          }
          action={{ label: 'Nova Receita Fixa', onClick: handleCreate }}
        />
      ) : (
        <div className="bg-white shadow sm:rounded-2xl overflow-auto mt-2 w-full flex-1 min-h-0">
          <table className="min-w-max w-full border-separate border-spacing-0 table-fixed relative">
            <thead className="bg-gray-50 sticky top-0 z-10 shadow-sm">
              <tr className="h-[44px] [&>th]:border-b [&>th]:border-gray-200 [&>th:not(:first-child)]:border-l">
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[380px]">
                  Descrição
                </th>
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[140px]">
                  Valor
                </th>
                
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[150px]">
                  Categoria
                </th>
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[150px]">
                  Conta
                </th>
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-full min-w-[330px]">
                  Observações
                </th>
                <th scope="col" className="sticky right-0 px-1 py-1 bg-gray-50 z-20 shadow-[-4px_0_8px_rgba(0,0,0,0.05)] w-[44px] min-w-[44px] max-w-[44px]">
                  <span className="sr-only">Ações</span>
                </th>
              </tr>
            </thead>
            <tbody className="bg-white">
              {filteredReceitas.map((receita) => (
                <tr key={receita.id} className="group even:bg-gray-200 odd:bg-white text-sm [&>td]:border-b [&>td]:border-gray-200 [&>td:not(:first-child)]:border-l">
                  <td className="px-3 py-1 font-medium text-gray-900 align-middle">
                    <div className="line-clamp-3 whitespace-normal break-words" title={receita.descricao}>
                      {receita.descricao}
                    </div>
                  </td>
                  <td className="px-3 py-1 text-center text-gray-900 tabular-nums whitespace-nowrap align-middle">
                    {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(receita.valor)}
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

      <ReceitaFixaFormModal
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

export default ReceitasFixasPage;
