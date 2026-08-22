import React, { useState, useEffect } from 'react';
import Modal from '../../../components/ui/Modal';
import { useCreateConta, useUpdateConta, type Conta } from '../../../hooks/useContas';

interface ContaFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  contaToEdit?: Conta | null;
}

const ContaFormModal: React.FC<ContaFormModalProps> = ({ isOpen, onClose, contaToEdit }) => {
  const [descricao, setDescricao] = useState('');
  const [observacoes, setObservacoes] = useState('');
  const [error, setError] = useState('');

  const createMutation = useCreateConta();
  const updateMutation = useUpdateConta();

  useEffect(() => {
    if (isOpen) {
      if (contaToEdit) {
        setDescricao(contaToEdit.descricao || '');
        setObservacoes(contaToEdit.observacoes || '');
      } else {
        setDescricao('');
        setObservacoes('');
      }
      setError('');
    }
  }, [isOpen, contaToEdit]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!descricao.trim()) {
      setError('A descrição é obrigatória.');
      return;
    }

    if (contaToEdit) {
      updateMutation.mutate(
        { id: contaToEdit.id, descricao: descricao.trim(), observacoes: observacoes.trim() },
        { onSuccess: () => onClose() }
      );
    } else {
      createMutation.mutate(
        { descricao: descricao.trim(), observacoes: observacoes.trim() },
        { onSuccess: () => onClose() }
      );
    }
  };

  const isPending = createMutation.isPending || updateMutation.isPending;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={contaToEdit ? 'Editar Conta' : 'Nova Conta'}
      disableClose={isPending}
      maxWidth="max-w-lg"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label htmlFor="descricao" className="block text-sm font-medium text-gray-700">
            Descrição *
          </label>
          <input
            type="text"
            id="descricao"
            value={descricao}
            onChange={(e) => {
              setDescricao(e.target.value);
              if (error && e.target.value.trim()) setError('');
            }}
            onBlur={(e) => {
              if (!e.target.value.trim()) setError('A descrição é obrigatória.');
            }}
            disabled={isPending}
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100"
            placeholder="Ex: Carteira, NuBank, Itaú"
            maxLength={20}
          />
          {error && <p className="mt-1 text-sm text-red-600">{error}</p>}
        </div>

        <div>
          <label htmlFor="observacoes" className="block text-sm font-medium text-gray-700">
            Observações
          </label>
          <textarea
            id="observacoes"
            value={observacoes}
            onChange={(e) => setObservacoes(e.target.value)}
            disabled={isPending}
            rows={12}
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 resize-none overflow-y-auto"
            maxLength={300}
          />
        </div>

        <div className="flex justify-end space-x-3 mt-6">
          <button
            type="button"
            onClick={onClose}
            className="cursor-pointer px-4 h-8 inline-flex items-center justify-center text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none"
            disabled={isPending}
          >
            Cancelar
          </button>
          <button
            type="submit"
            className="cursor-pointer px-4 h-8 inline-flex items-center justify-center text-sm font-medium text-white bg-primary border border-transparent rounded-md hover:bg-primary-light focus:outline-none"
            disabled={isPending}
          >
            {isPending ? 'Salvando...' : 'Salvar'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default ContaFormModal;
