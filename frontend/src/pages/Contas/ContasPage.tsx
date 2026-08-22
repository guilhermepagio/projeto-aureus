import ActionMenu from '../../components/ui/ActionMenu';
import React, { useState } from 'react';
import { useContas, type Conta } from '../../hooks/useContas';
import ContaFormModal from './components/ContaFormModal';
import DeleteConfirmModal from './components/DeleteConfirmModal';
import EmptyState from '../../components/ui/EmptyState';

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
    <div className="px-4 pb-4 w-full">
      <div className="mb-4 mt-2 flex justify-between items-center">
        <div className="pl-2 border-l-4 border-blue-600">
          <h1 className="text-2xl font-bold text-gray-800">Contas</h1>
        </div>
        <button
          onClick={handleCreate}
          className="cursor-pointer h-8 px-4 flex items-center justify-center bg-blue-600 hover:bg-blue-700 text-white rounded-md text-xs font-semibold whitespace-nowrap shadow-sm"
        >
          + Nova Conta
        </button>
      </div>
      {isEmpty ? (
        <EmptyState
          title="Nenhuma conta"
          description="Comece criando sua primeira conta."
          action={{ label: 'Nova Conta', onClick: handleCreate, color: 'blue' }}
        />
      ) : (
        <div className="bg-white shadow sm:rounded-2xl overflow-visible mt-2 w-full">
          <table className="min-w-max w-full divide-y divide-gray-200 table-fixed">
            <thead className="bg-gray-50 sticky top-0 z-10 shadow-sm">
              <tr className="divide-x divide-gray-200 h-[44px]">
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[280px]">
                  Descrição
                </th>
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider min-w-[330px]">
                  Observações
                </th>
                <th scope="col" className="sticky right-0 px-1 py-1 bg-gray-50 z-20 shadow-[-4px_0_8px_rgba(0,0,0,0.05)] w-[44px]">
                  <span className="sr-only">Ações</span>
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {contas.map((conta) => (
                <tr key={conta.id} className="group even:bg-gray-200 odd:bg-white text-sm divide-x divide-gray-200">
                  <td className="px-3 py-1 font-medium text-gray-900 align-middle">
                    <div className="line-clamp-3 whitespace-normal break-words" title={conta.descricao}>
                      {conta.descricao}
                    </div>
                  </td>
                  <td className="px-3 py-1 text-gray-900 align-middle">
                    <div className="line-clamp-3 whitespace-normal break-words" title={conta.observacoes}>
                      {conta.observacoes || '-'}
                    </div>
                  </td>
                  <td className="sticky right-0 px-1 py-1 group-even:bg-gray-200 group-odd:bg-white shadow-[-4px_0_8px_rgba(0,0,0,0.05)] align-middle">
                    <div className="flex justify-center w-full"><ActionMenu onEdit={() => handleEdit(conta)} onDelete={() => handleDelete(conta)} /></div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
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
