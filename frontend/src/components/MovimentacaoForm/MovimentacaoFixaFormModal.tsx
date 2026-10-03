import React, { useState, useEffect } from 'react';
import Modal from '../ui/Modal';
import FormField from './FormField';
import ValorInput from './ValorInput';
import ContaCategoriaFields from './ContaCategoriaFields';
import ObservacoesField from './ObservacoesField';
import { formatCurrency, parseCurrency } from '../../utils/currencyFormat';

export interface MovimentacaoFixaData {
  descricao: string;
  valor: number;
  conta: { id: number };
  categoria: { id: number };
  observacoes: string;
}

export interface MovimentacaoFixaItem {
  id: number;
  descricao: string;
  valor: number;
  conta?: { id: number; descricao?: string };
  categoria?: { id: number; descricao?: string };
  observacoes?: string;
}

export interface MovimentacaoFixaFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  tipo: 'despesa' | 'receita';
  itemToEdit?: MovimentacaoFixaItem | null;
  onSubmit: (
    data: MovimentacaoFixaData,
    callbacks: { onSuccess: () => void; onError: (err: Error) => void }
  ) => void;
  isPending: boolean;
  title?: string;
  initialFocusRef?: React.RefObject<HTMLElement | null>;
  idPrefix?: string;
}

export const MovimentacaoFixaFormModal: React.FC<MovimentacaoFixaFormModalProps> = ({
  isOpen,
  onClose,
  tipo,
  itemToEdit,
  onSubmit,
  isPending,
  title,
  initialFocusRef,
  idPrefix: customIdPrefix,
}) => {
  const [descricao, setDescricao] = useState('');
  const [valor, setValor] = useState<string>('');
  const [contaId, setContaId] = useState<number | ''>('');
  const [categoriaId, setCategoriaId] = useState<number | ''>('');
  const [observacoes, setObservacoes] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});

  const isDespesa = tipo === 'despesa';
  const idPrefix = customIdPrefix || (isDespesa ? 'despesa-fixa' : 'receita-fixa');

  useEffect(() => {
    if (isOpen) {
      if (itemToEdit) {
        setDescricao(itemToEdit.descricao || '');
        setValor(itemToEdit.valor ? formatCurrency(itemToEdit.valor) : '');
        setContaId(itemToEdit.conta?.id || '');
        setCategoriaId(itemToEdit.categoria?.id || '');
        setObservacoes(itemToEdit.observacoes || '');
      } else {
        setDescricao('');
        setValor('');
        setContaId('');
        setCategoriaId('');
        setObservacoes('');
      }
      setErrors({});
    }
  }, [isOpen, itemToEdit]);

  const clearFieldError = (field: string) => {
    if (errors[field] || errors.submit) {
      setErrors((prev) => {
        const updated = { ...prev };
        delete updated[field];
        delete updated.submit;
        return updated;
      });
    }
  };

  const handleDescricaoChange = (val: string) => {
    setDescricao(val);
    clearFieldError('descricao');
  };

  const handleValorChange = (val: string) => {
    setValor(val);
    clearFieldError('valor');
  };

  const handleContaChange = (val: number | '') => {
    setContaId(val);
    clearFieldError('contaId');
  };

  const handleCategoriaChange = (val: number | '') => {
    setCategoriaId(val);
    clearFieldError('categoriaId');
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const newErrors: Record<string, string> = {};

    if (!descricao.trim()) newErrors.descricao = 'A descrição é obrigatória';
    if (!valor || parseCurrency(valor) <= 0) newErrors.valor = 'O valor deve ser maior que zero';
    if (!contaId) newErrors.contaId = 'Selecione uma conta';
    if (!categoriaId) newErrors.categoriaId = 'Selecione uma categoria';

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    const payload: MovimentacaoFixaData = {
      descricao: descricao.trim(),
      valor: parseCurrency(valor),
      conta: { id: Number(contaId) },
      categoria: { id: Number(categoriaId) },
      observacoes: observacoes.trim(),
    };

    onSubmit(payload, {
      onSuccess: () => onClose(),
      onError: (err) => setErrors({ submit: err.message }),
    });
  };

  const defaultTitle = isDespesa
    ? itemToEdit
      ? 'Editar Despesa Fixa'
      : 'Nova Despesa Fixa'
    : itemToEdit
    ? 'Editar Receita Fixa'
    : 'Nova Receita Fixa';

  const modalTitle = title || defaultTitle;
  const placeholderDescricao = isDespesa ? 'Ex: Aluguel' : 'Ex: Salário';
  const submitButtonColor = isDespesa
    ? 'bg-red-600 hover:bg-red-700 focus-visible:ring-red-500'
    : 'bg-primary hover:bg-primary-light focus-visible:ring-primary';

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={modalTitle}
      disableClose={isPending}
      maxWidth="max-w-4xl"
      initialFocusRef={initialFocusRef}
    >
      <form onSubmit={handleSubmit} noValidate>
        {errors.submit && (
          <div role="alert" className="text-red-600 text-sm mb-4">
            {errors.submit}
          </div>
        )}

        <div className="flex flex-col md:flex-row gap-6">
          {/* Left Column */}
          <div className="w-full md:w-1/2 space-y-4">
            <FormField id={`${idPrefix}-descricao`} label="Descrição" required error={errors.descricao}>
              {({ id, hasError, errorId, ariaRequired }) => (
                <input
                  type="text"
                  id={id}
                  autoFocus
                  value={descricao}
                  onChange={(e) => handleDescricaoChange(e.target.value)}
                  disabled={isPending}
                  placeholder={placeholderDescricao}
                  maxLength={100}
                  aria-required={ariaRequired}
                  aria-invalid={hasError}
                  aria-describedby={errorId}
                  className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 ${
                    hasError
                      ? 'border-red-500 focus:border-red-500 focus:ring-red-500'
                      : 'border-gray-300 focus:border-primary focus:ring-primary'
                  }`}
                />
              )}
            </FormField>

            <ValorInput
              id={`${idPrefix}-valor`}
              label="Valor"
              value={valor}
              onChange={handleValorChange}
              error={errors.valor}
              disabled={isPending}
              placeholder="R$ 0,00"
            />

            <ContaCategoriaFields
              idPrefix={idPrefix}
              contaId={contaId}
              categoriaId={categoriaId}
              onChangeConta={handleContaChange}
              onChangeCategoria={handleCategoriaChange}
              errors={{ contaId: errors.contaId, categoriaId: errors.categoriaId }}
              disabled={isPending}
              layout="stacked"
            />
          </div>

          {/* Right Column */}
          <div className="w-full md:w-1/2 flex flex-col min-h-0">
            <ObservacoesField
              id={`${idPrefix}-observacoes`}
              value={observacoes}
              onChange={setObservacoes}
              disabled={isPending}
              maxLength={300}
            />
          </div>
        </div>

        <div className="flex justify-end space-x-3 mt-6">
          <button
            type="button"
            onClick={onClose}
            className="cursor-pointer px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-primary"
            disabled={isPending}
          >
            Cancelar
          </button>
          <button
            type="submit"
            className={`cursor-pointer px-4 py-2 text-sm font-medium text-white border border-transparent rounded-md focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 ${submitButtonColor}`}
            disabled={isPending}
          >
            {isPending ? 'Salvando...' : 'Salvar'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default MovimentacaoFixaFormModal;
