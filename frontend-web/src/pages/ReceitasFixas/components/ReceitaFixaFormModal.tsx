import React from 'react';
import { useCreateReceitaFixa, useUpdateReceitaFixa, type ReceitaFixa } from '../../../hooks/useReceitasFixas';
import MovimentacaoFixaFormModal, { type MovimentacaoFixaData } from '../../../components/MovimentacaoForm/MovimentacaoFixaFormModal';

interface ReceitaFixaFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  receitaToEdit?: ReceitaFixa | null;
}

const ReceitaFixaFormModal: React.FC<ReceitaFixaFormModalProps> = ({ isOpen, onClose, receitaToEdit }) => {
  const createMutation = useCreateReceitaFixa();
  const updateMutation = useUpdateReceitaFixa();

  const handleSubmit = (
    data: MovimentacaoFixaData,
    callbacks: { onSuccess: () => void; onError: (err: Error) => void }
  ) => {
    if (receitaToEdit) {
      updateMutation.mutate(
        { id: receitaToEdit.id, ...data },
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
      tipo="receita"
      itemToEdit={receitaToEdit}
      onSubmit={handleSubmit}
      isPending={isPending}
    />
  );
};

export default ReceitaFixaFormModal;
