import React from 'react';
import Modal from '../../../components/ui/Modal';
import { useDeleteDespesaVariavel, type DespesaVariavel } from '../../../hooks/useDespesasVariaveis';

interface DeleteConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  despesaToDelete: DespesaVariavel | null;
}

const DeleteConfirmModal: React.FC<DeleteConfirmModalProps> = ({ isOpen, onClose, despesaToDelete }) => {
  const deleteMutation = useDeleteDespesaVariavel();

  React.useEffect(() => {
    if (isOpen) {
      deleteMutation.reset();
    }
  }, [isOpen]);

  const handleDelete = () => {
    if (despesaToDelete) {
      deleteMutation.mutate(despesaToDelete.id, {
        onSuccess: () => {
          onClose();
        }
      });
    }
  };

  const isPending = deleteMutation.isPending;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Excluir Despesa Variável"
      disableClose={isPending}
      maxWidth="max-w-md"
    >
      <div className="mt-2">
        <p className="text-sm text-gray-500">
          Tem certeza que deseja excluir a despesa variável <span className="font-semibold text-gray-800">{despesaToDelete?.descricao}</span>?
          Esta ação não poderá ser desfeita.
        </p>
        {deleteMutation.isError && (
          <p className="mt-2 text-sm text-red-600">{deleteMutation.error.message}</p>
        )}
      </div>

      <div className="mt-5 sm:mt-6 sm:grid sm:grid-flow-row-dense sm:grid-cols-2 sm:gap-3">
        <button
          type="button"
          className="cursor-pointer inline-flex w-full justify-center rounded-md bg-red-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-red-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-600 sm:col-start-2 disabled:opacity-50"
          onClick={handleDelete}
          disabled={isPending}
        >
          {isPending ? 'Excluindo...' : 'Excluir'}
        </button>
        <button
          type="button"
          className="cursor-pointer mt-3 inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 sm:col-start-1 sm:mt-0 disabled:opacity-50"
          onClick={onClose}
          disabled={isPending}
        >
          Cancelar
        </button>
      </div>
    </Modal>
  );
};

export default DeleteConfirmModal;
