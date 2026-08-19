import React, { useState } from 'react';
import { useContas, type Conta } from '../../hooks/useContas';
import ContaFormModal from './components/ContaFormModal';
import DeleteConfirmModal from './components/DeleteConfirmModal';

const ContasPage: React.FC = () => {
  const { data: contas, isLoading, isError } = useContas();
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedConta, setSelectedConta] = useState<Conta | null>(null);

  const handleCreate = () => {
    setSelectedConta(null);
    setIsFormModalOpen(true);
  };

  const handleEdit = (conta: Conta) => {
    setSelectedConta(conta);
    setIsFormModalOpen(true);
  };

  const handleDelete = (conta: Conta) => {
    setSelectedConta(conta);
    setIsDeleteModalOpen(true);
  };

  if (isLoading) return <div className="p-6">Carregando contas...</div>;
  if (isError) return <div className="p-6 text-red-600">Erro ao carregar contas.</div>;

  const isEmpty = !contas || contas.length === 0;

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Contas</h1>
        <button
          onClick={handleCreate}
          className="bg-indigo-600 text-white px-4 py-2 rounded-md hover:bg-indigo-700 text-sm font-medium"
        >
          Nova Conta
        </button>
      </div>

      {isEmpty ? (
        <div className="text-center py-12 bg-white rounded-lg shadow-sm border border-gray-200">
          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
          </svg>
          <h3 className="mt-2 text-sm font-semibold text-gray-900">Nenhuma conta</h3>
          <p className="mt-1 text-sm text-gray-500">Comece criando sua primeira conta.</p>
          <div className="mt-6">
            <button
              onClick={handleCreate}
              type="button"
              className="inline-flex items-center rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
            >
              <svg className="-ml-0.5 mr-1.5 h-5 w-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                <path d="M10.75 4.75a.75.75 0 00-1.5 0v4.5h-4.5a.75.75 0 000 1.5h4.5v4.5a.75.75 0 001.5 0v-4.5h4.5a.75.75 0 000-1.5h-4.5v-4.5z" />
              </svg>
              Nova Conta
            </button>
          </div>
        </div>
      ) : (
        <div className="bg-white shadow overflow-hidden sm:rounded-md">
          <ul role="list" className="divide-y divide-gray-200">
            {contas.map((conta) => (
              <li key={conta.id} className="px-4 py-4 sm:px-6 flex items-center justify-between hover:bg-gray-50">
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-indigo-600 truncate">{conta.descricao}</p>
                  {conta.observacoes && (
                    <p className="mt-1 text-sm text-gray-500 truncate">{conta.observacoes}</p>
                  )}
                </div>
                <div className="flex space-x-3 ml-4">
                  <button
                    onClick={() => handleEdit(conta)}
                    className="text-gray-400 hover:text-indigo-600"
                    title="Editar"
                  >
                    Editar
                  </button>
                  <button
                    onClick={() => handleDelete(conta)}
                    className="text-gray-400 hover:text-red-600"
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

      <ContaFormModal
        isOpen={isFormModalOpen}
        onClose={() => setIsFormModalOpen(false)}
        contaToEdit={selectedConta}
      />

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        contaToDelete={selectedConta}
      />
    </div>
  );
};

export default ContasPage;
