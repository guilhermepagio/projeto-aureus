import React from 'react';
import { useCreateReceitaVariavel, useUpdateReceitaVariavel, type ReceitaVariavel } from '../../../hooks/useReceitasVariaveis';
import MovimentacaoVariavelFormModal, { type MovimentacaoVariavelData } from '../../../components/MovimentacaoForm/MovimentacaoVariavelFormModal';

interface ReceitaVariavelFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  receitaToEdit?: ReceitaVariavel | null;
}

const ReceitaVariavelFormModal: React.FC<ReceitaVariavelFormModalProps> = ({
  isOpen,
  onClose,
  receitaToEdit,
}) => {
  const createMutation = useCreateReceitaVariavel();
  const updateMutation = useUpdateReceitaVariavel();

  const handleSubmit = (
    data: MovimentacaoVariavelData,
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
    <MovimentacaoVariavelFormModal
      isOpen={isOpen}
      onClose={onClose}
      tipo="receita"
      hasLocalEDataCompra={false}
      itemToEdit={receitaToEdit}
      onSubmit={handleSubmit}
      isPending={isPending}
    />
  );
};

export default ReceitaVariavelFormModal;
