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
    <div className="p-6 max-w-4xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Receitas Fixas</h1>
        <button
          onClick={handleCreate}
          className="cursor-pointer bg-primary text-white px-4 py-2 rounded-md hover:bg-primary-light text-sm font-medium"
        >
          Nova Receita Fixa
        </button>
      </div>

      {isEmpty ? (
        <div className="text-center py-12 bg-white rounded-lg shadow-sm border border-gray-200">
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
        <div className="bg-white shadow overflow-hidden sm:rounded-md">
          <ul role="list" className="divide-y divide-gray-200">
            {receitas.map((receita) => (
              <li key={receita.id} className="px-4 py-4 sm:px-6 flex items-center justify-between hover:bg-gray-50">
                <div className="flex-1 min-w-0">
                  <div className="flex justify-between items-center mb-1">
                    <p className="text-sm font-medium text-primary truncate">{receita.descricao}</p>
                    <p className="text-sm font-semibold text-green-600 tabular-nums">
                      + {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(receita.valor)}
                    </p>
                  </div>
                  <div className="flex text-xs text-gray-500 space-x-4">
                    <span>{receita.conta?.descricao}</span>
                    <span>&bull;</span>
                    <span>{receita.categoria?.descricao}</span>
                    <span>&bull;</span>
                    <span>Início: {receita.dataInicio?.substring(0, 7)}</span>
                  </div>
                </div>
                <div className="flex space-x-3 ml-4">
                  <button
                    type="button"
                    onClick={() => handleEdit(receita)}
                    className="font-inherit cursor-pointer text-gray-400 hover:text-primary"
                    title="Editar"
                  >
                    Editar
                  </button>
                  <button
                    type="button"
                    onClick={() => handleDelete(receita)}
                    className="font-inherit cursor-pointer text-gray-400 hover:text-red-600"
                    title="Excluir"
                  >
                    Excluir
                  </button>
                </div>
              </li>
            ))}
          </ul>
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
