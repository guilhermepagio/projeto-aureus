import React from 'react';
import { useCreateDespesaVariavel, useUpdateDespesaVariavel, type DespesaVariavel } from '../../../hooks/useDespesasVariaveis';
import MovimentacaoVariavelFormModal, { type MovimentacaoVariavelData } from '../../../components/MovimentacaoForm/MovimentacaoVariavelFormModal';

interface DespesaVariavelFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  despesaToEdit?: DespesaVariavel | null;
}

const DespesaVariavelFormModal: React.FC<DespesaVariavelFormModalProps> = ({
  isOpen,
  onClose,
  despesaToEdit,
}) => {
  const createMutation = useCreateDespesaVariavel();
  const updateMutation = useUpdateDespesaVariavel();

  const handleSubmit = (
    data: MovimentacaoVariavelData,
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
    <MovimentacaoVariavelFormModal
      isOpen={isOpen}
      onClose={onClose}
      tipo="despesa"
      hasLocalEDataCompra={true}
      itemToEdit={despesaToEdit}
      onSubmit={handleSubmit}
      isPending={isPending}
    />
  );
};

export default DespesaVariavelFormModal;
