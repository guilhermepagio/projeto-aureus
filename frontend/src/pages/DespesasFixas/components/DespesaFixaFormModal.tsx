import React from 'react';
import { useCreateDespesaFixa, useUpdateDespesaFixa, type DespesaFixa } from '../../../hooks/useDespesasFixas';
import MovimentacaoFixaFormModal, { type MovimentacaoFixaData } from '../../../components/MovimentacaoForm/MovimentacaoFixaFormModal';

interface DespesaFixaFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  despesaToEdit?: DespesaFixa | null;
}

const DespesaFixaFormModal: React.FC<DespesaFixaFormModalProps> = ({ isOpen, onClose, despesaToEdit }) => {
  const createMutation = useCreateDespesaFixa();
  const updateMutation = useUpdateDespesaFixa();

  const handleSubmit = (
    data: MovimentacaoFixaData,
    callbacks: { onSuccess: () => void; onError: (err: Error) => void }
  ) => {
    if (despesaToEdit) {
      updateMutation.mutate(
        { id: despesaToEdit.id, ...data },
        callbacks
      );
    } else {
      createMutation.mutate(
        data,
        callbacks
      );
    }
  };

  const isPending = createMutation.isPending || updateMutation.isPending;

  return (
    <MovimentacaoFixaFormModal
      isOpen={isOpen}
      onClose={onClose}
      tipo="despesa"
      itemToEdit={despesaToEdit}
      onSubmit={handleSubmit}
      isPending={isPending}
    />
  );
};

export default DespesaFixaFormModal;
