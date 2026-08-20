import React, { useState } from 'react';
import { useReceitasFixas, type ReceitaFixa } from '../../hooks/useReceitasFixas';
import ReceitaFixaFormModal from './components/ReceitaFixaFormModal';
import DeleteConfirmModal from './components/DeleteConfirmModal';

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

  if (isLoading) return <div className="p-6">Carregando receitas fixas...</div>;
  if (isError) return <div className="p-6 text-red-600">Erro ao carregar receitas fixas.</div>;

  const isEmpty = !receitas || receitas.length === 0;

  return (
    <div className="px-4 pb-4 w-full">
      {isEmpty ? (
        <div className="text-center py-12 bg-white rounded-lg shadow-sm border border-gray-200 mt-4">
          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <h3 className="mt-2 text-sm font-semibold text-gray-900">Nenhuma receita fixa</h3>
          <p className="mt-1 text-sm text-gray-500">Comece criando sua primeira receita fixa.</p>
          <div className="mt-6">
            <button
              onClick={handleCreate}
              type="button"
              className="cursor-pointer inline-flex items-center rounded-md bg-primary px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-primary focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
            >
              Nova Receita Fixa
            </button>
          </div>
        </div>
      ) : (
        <div className="bg-white shadow sm:rounded-2xl overflow-hidden mt-2">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50 sticky top-0 z-10 shadow-sm">
              <tr>
                <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider w-1/3">
                  Descrição
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                  Valor
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                  Início
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                  Categoria
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                  Conta
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                  Observações
                </th>
                <th scope="col" className="px-6 py-3">
                  <div className="flex justify-end ml-auto w-[160px]">
                    <button
                      onClick={handleCreate}
                      className="cursor-pointer w-full h-8 flex items-center justify-center bg-primary text-white rounded-full hover:bg-primary-light text-xs font-semibold whitespace-nowrap"
                    >
                      + Nova Receita
                    </button>
                  </div>
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {receitas.map((receita) => (
                <tr key={receita.id} className="even:bg-gray-100/60 odd:bg-white text-sm">
                  <td className="px-6 py-[3px] font-medium text-primary whitespace-normal break-words">
                    {receita.descricao}
                  </td>
                  <td className="px-6 py-[3px] font-semibold text-green-600 tabular-nums whitespace-nowrap">
                    + {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(receita.valor)}
                  </td>
                  <td className="px-6 py-[3px] text-gray-600 whitespace-nowrap">
                    {receita.dataInicio ? `${receita.dataInicio.substring(5, 7)}/${receita.dataInicio.substring(0, 4)}` : '-'}
                  </td>
                  <td className="px-6 py-[3px] text-gray-600 truncate max-w-[150px]" title={receita.categoria?.descricao}>
                    {receita.categoria?.descricao}
                  </td>
                  <td className="px-6 py-[3px] text-gray-600 truncate max-w-[150px]" title={receita.conta?.descricao}>
                    {receita.conta?.descricao}
                  </td>
                  <td className="px-6 py-[3px] text-gray-500 max-w-[150px] truncate" title={receita.observacoes}>
                    {receita.observacoes || '-'}
                  </td>
                  <td className="px-6 py-[3px]">
                    <div className="flex justify-end ml-auto w-[160px] gap-2">
                      <button
                        type="button"
                        onClick={() => handleEdit(receita)}
                        className="flex-1 h-8 flex items-center justify-center cursor-pointer bg-green-50 text-green-700 rounded-full hover:bg-green-100 text-xs font-semibold text-center transition-colors"
                        title="Editar"
                      >
                        Editar
                      </button>
                      <button
                        type="button"
                        onClick={() => handleDelete(receita)}
                        className="flex-1 h-8 flex items-center justify-center cursor-pointer bg-red-50 text-red-700 rounded-full hover:bg-red-100 text-xs font-semibold text-center transition-colors"
                        title="Excluir"
                      >
                        Excluir
                      </button>
                    </div>
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
