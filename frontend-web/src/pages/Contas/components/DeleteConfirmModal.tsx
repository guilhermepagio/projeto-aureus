import React from 'react';
import Modal from '../../../components/ui/Modal';
import { useDeleteConta, type Conta } from '../../../hooks/useContas';

interface DeleteConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  contaToDelete?: Conta | null;
}

const DeleteConfirmModal: React.FC<DeleteConfirmModalProps> = ({ isOpen, onClose, contaToDelete }) => {
  const deleteMutation = useDeleteConta();

  const handleDelete = () => {
    if (contaToDelete) {
      deleteMutation.mutate(contaToDelete.id, {
        onSuccess: () => onClose()
      });
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Excluir Conta"
      disableClose={deleteMutation.isPending}
    >
      <div className="space-y-4">
        <p className="text-sm text-gray-600">
          Tem certeza de que deseja excluir a conta <strong>{contaToDelete ? contaToDelete.descricao : ''}</strong>?
          Esta ação não poderá ser desfeita.
        </p>

        <div className="flex justify-end space-x-3 mt-6">
          <button
            type="button"
            onClick={onClose}
            className="cursor-pointer px-4 h-8 inline-flex items-center justify-center text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none"
            disabled={deleteMutation.isPending}
          >
            Cancelar
          </button>
          <button
            type="button"
            onClick={handleDelete}
            className="cursor-pointer px-4 h-8 inline-flex items-center justify-center text-sm font-medium text-white bg-red-600 border border-transparent rounded-md hover:bg-red-700 focus:outline-none"
            disabled={deleteMutation.isPending}
          >
            {deleteMutation.isPending ? 'Excluindo...' : 'Excluir'}
          </button>
        </div>
      </div>
    </Modal>
  );
};

export default DeleteConfirmModal;
