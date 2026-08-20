import React, { useState } from 'react';
import { useDespesasFixas, type DespesaFixa } from '../../hooks/useDespesasFixas';
import DespesaFixaFormModal from './components/DespesaFixaFormModal';
import DeleteConfirmModal from './components/DeleteConfirmModal';

const DespesasFixasPage: React.FC = () => {
  const { data: despesas, isLoading, isError } = useDespesasFixas();
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedDespesa, setSelectedDespesa] = useState<DespesaFixa | null>(null);

  const handleCreate = () => {
    setSelectedDespesa(null);
    setIsFormModalOpen(true);
  };

  const handleEdit = (despesa: DespesaFixa) => {
    setSelectedDespesa(despesa);
    setIsFormModalOpen(true);
  };

  const handleDelete = (despesa: DespesaFixa) => {
    setSelectedDespesa(despesa);
    setIsDeleteModalOpen(true);
  };

  if (isLoading) return <div className="p-6">Carregando despesas fixas...</div>;
  if (isError) return <div className="p-6 text-red-600">Erro ao carregar despesas fixas.</div>;

  const isEmpty = !despesas || despesas.length === 0;

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Despesas Fixas</h1>
        <button
          onClick={handleCreate}
          className="cursor-pointer bg-primary text-white px-4 py-2 rounded-md hover:bg-primary-light text-sm font-medium"
        >
          Nova Despesa Fixa
        </button>
      </div>

      {isEmpty ? (
        <div className="text-center py-12 bg-white rounded-lg shadow-sm border border-gray-200">
          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <h3 className="mt-2 text-sm font-semibold text-gray-900">Nenhuma despesa fixa</h3>
          <p className="mt-1 text-sm text-gray-500">Comece criando sua primeira despesa fixa.</p>
          <div className="mt-6">
            <button
              onClick={handleCreate}
              type="button"
              className="cursor-pointer inline-flex items-center rounded-md bg-primary px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-primary focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
            >
              Nova Despesa Fixa
            </button>
          </div>
        </div>
      ) : (
        <div className="bg-white shadow overflow-hidden sm:rounded-md">
          <ul role="list" className="divide-y divide-gray-200">
            {despesas.map((despesa) => (
              <li key={despesa.id} className="px-4 py-4 sm:px-6 flex items-center justify-between hover:bg-gray-50">
                <div className="flex-1 min-w-0">
                  <div className="flex justify-between items-center mb-1">
                    <p className="text-sm font-medium text-primary truncate">{despesa.descricao}</p>
                    <p className="text-sm font-semibold text-red-600 tabular-nums">
                      - {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(despesa.valor)}
                    </p>
                  </div>
                  <div className="flex text-xs text-gray-500 space-x-4">
                    <span>{despesa.conta?.descricao}</span>
                    <span>&bull;</span>
                    <span>{despesa.categoria?.descricao}</span>
                    <span>&bull;</span>
                    <span>Início: {despesa.dataInicio?.substring(0, 7)}</span>
                  </div>
                </div>
                <div className="flex space-x-3 ml-4">
                  <button
                    type="button"
                    onClick={() => handleEdit(despesa)}
                    className="font-inherit cursor-pointer text-gray-400 hover:text-primary"
                    title="Editar"
                  >
                    Editar
                  </button>
                  <button
                    type="button"
                    onClick={() => handleDelete(despesa)}
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

      <DespesaFixaFormModal
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

export default DespesasFixasPage;
