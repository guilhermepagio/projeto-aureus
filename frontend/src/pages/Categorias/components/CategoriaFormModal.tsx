import React, { useState, useEffect } from 'react';
import Modal from '../../../components/ui/Modal';
import { useCreateCategoria, useUpdateCategoria, type Categoria } from '../../../hooks/useCategorias';

interface CategoriaFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  categoriaToEdit?: Categoria | null;
}

const CategoriaFormModal: React.FC<CategoriaFormModalProps> = ({ isOpen, onClose, categoriaToEdit }) => {
  const [descricao, setDescricao] = useState('');
  const [observacoes, setObservacoes] = useState('');
  const [error, setError] = useState('');

  const createMutation = useCreateCategoria();
  const updateMutation = useUpdateCategoria();

  useEffect(() => {
    if (isOpen) {
      if (categoriaToEdit) {
        setDescricao(categoriaToEdit.descricao || '');
        setObservacoes(categoriaToEdit.observacoes || '');
      } else {
        setDescricao('');
        setObservacoes('');
      }
      setError('');
    }
  }, [isOpen, categoriaToEdit]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!descricao.trim()) {
      setError('A descrição é obrigatória.');
      return;
    }

    if (categoriaToEdit) {
      updateMutation.mutate(
        { id: categoriaToEdit.id, descricao: descricao.trim(), observacoes: observacoes.trim() },
        { 
          onSuccess: () => onClose(),
          onError: (err) => setError(err.message)
        }
      );
    } else {
      createMutation.mutate(
        { descricao: descricao.trim(), observacoes: observacoes.trim() },
        { 
          onSuccess: () => onClose(),
          onError: (err) => setError(err.message)
        }
      );
    }
  };

  const isPending = createMutation.isPending || updateMutation.isPending;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={categoriaToEdit ? 'Editar Categoria' : 'Nova Categoria'}
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
            id="categoria-descricao"
            autoFocus
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
            placeholder="Ex: Alimentação, Transporte, Salário"
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

export default CategoriaFormModal;
