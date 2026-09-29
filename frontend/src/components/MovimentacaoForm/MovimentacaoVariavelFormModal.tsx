import React, { useState, useEffect } from 'react';
import Modal from '../ui/Modal';
import FormField from './FormField';
import ContaCategoriaFields from './ContaCategoriaFields';
import ParcelamentoFields from './ParcelamentoFields';
import ObservacoesField from './ObservacoesField';
import DatePicker from '../ui/DatePicker';
import { formatCurrency, parseCurrency } from '../../utils/currencyFormat';

export interface MovimentacaoVariavelData {
  descricao: string;
  valorParcela: number;
  quantidadeParcelas: number;
  dataInicio: string;
  conta: { id: number };
  categoria: { id: number };
  observacoes?: string;
  localCompra?: string;
  dataCompra?: string;
}

export interface MovimentacaoVariavelItem {
  id: number;
  descricao: string;
  valorParcela: number;
  quantidadeParcelas: number;
  dataInicio?: string;
  dataFim?: string;
  conta?: { id: number; descricao?: string };
  categoria?: { id: number; descricao?: string };
  observacoes?: string;
  localCompra?: string;
  dataCompra?: string;
}

export interface MovimentacaoVariavelFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  tipo: 'despesa' | 'receita';
  itemToEdit?: MovimentacaoVariavelItem | null;
  onSubmit: (
    data: MovimentacaoVariavelData,
    callbacks: { onSuccess: () => void; onError: (err: Error) => void }
  ) => void;
  isPending: boolean;
  hasLocalEDataCompra?: boolean;
  title?: string;
  idPrefix?: string;
  initialFocusRef?: React.RefObject<HTMLElement | null>;
}

export const MovimentacaoVariavelFormModal: React.FC<MovimentacaoVariavelFormModalProps> = ({
  isOpen,
  onClose,
  tipo,
  itemToEdit,
  onSubmit,
  isPending,
  hasLocalEDataCompra,
  title,
  idPrefix: customIdPrefix,
  initialFocusRef,
}) => {
  const isDespesa = tipo === 'despesa';
  const shouldIncludeLocalEDataCompra = hasLocalEDataCompra ?? isDespesa;
  const idPrefix = customIdPrefix || (isDespesa ? 'despesa' : 'receita');

  const [descricao, setDescricao] = useState('');
  const [localCompra, setLocalCompra] = useState('');
  const [dataCompra, setDataCompra] = useState('');
  const [valorParcela, setValorParcela] = useState<string>('');
  const [quantidadeParcelas, setQuantidadeParcelas] = useState<number | ''>('');
  const [dataInicio, setDataInicio] = useState('');
  const [contaId, setContaId] = useState<number | ''>('');
  const [categoriaId, setCategoriaId] = useState<number | ''>('');
  const [observacoes, setObservacoes] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (isOpen) {
      if (itemToEdit) {
        setDescricao(itemToEdit.descricao || '');
        setLocalCompra(itemToEdit.localCompra || '');
        setDataCompra(itemToEdit.dataCompra || '');
        setValorParcela(itemToEdit.valorParcela ? formatCurrency(itemToEdit.valorParcela) : '');
        setQuantidadeParcelas(itemToEdit.quantidadeParcelas || '');
        setDataInicio(itemToEdit.dataInicio ? itemToEdit.dataInicio.substring(0, 7) : '');
        setContaId(itemToEdit.conta?.id || '');
        setCategoriaId(itemToEdit.categoria?.id || '');
        setObservacoes(itemToEdit.observacoes || '');
      } else {
        setDescricao('');
        setLocalCompra('');
        setDataCompra('');
        setValorParcela('');
        setQuantidadeParcelas('');
        setDataInicio('');
        setContaId('');
        setCategoriaId('');
        setObservacoes('');
      }
      setErrors({});
    }
  }, [isOpen, itemToEdit]);

  const clearFieldError = (field: string) => {
    if (errors[field]) {
      setErrors((prev) => {
        const updated = { ...prev };
        delete updated[field];
        return updated;
      });
    }
  };

  const handleDescricaoChange = (val: string) => {
    setDescricao(val);
    clearFieldError('descricao');
  };

  const handleValorParcelaChange = (val: string) => {
    setValorParcela(val);
    clearFieldError('valorParcela');
  };

  const handleQuantidadeParcelasChange = (val: number | '') => {
    setQuantidadeParcelas(val);
    clearFieldError('quantidadeParcelas');
  };

  const handleDataInicioChange = (val: string) => {
    setDataInicio(val);
    clearFieldError('dataInicio');
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
    if (!valorParcela || parseCurrency(valorParcela) <= 0) {
      newErrors.valorParcela = 'O valor da parcela deve ser maior que zero';
    }
    if (!quantidadeParcelas || Number(quantidadeParcelas) < 1) {
      newErrors.quantidadeParcelas = 'Mínimo de 1 parcela';
    }
    if (!dataInicio) newErrors.dataInicio = 'Data da 1ª parcela é obrigatória';
    if (!contaId) newErrors.contaId = 'Selecione uma conta';
    if (!categoriaId) newErrors.categoriaId = 'Selecione uma categoria';

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    const payload: MovimentacaoVariavelData = {
      descricao: descricao.trim(),
      valorParcela: parseCurrency(valorParcela),
      quantidadeParcelas: Number(quantidadeParcelas),
      dataInicio: dataInicio.length === 7 ? `${dataInicio}-01` : dataInicio,
      conta: { id: Number(contaId) },
      categoria: { id: Number(categoriaId) },
      observacoes: observacoes.trim() || undefined,
      ...(shouldIncludeLocalEDataCompra
        ? {
            localCompra: localCompra.trim() || undefined,
            dataCompra: dataCompra || undefined,
          }
        : {}),
    };

    onSubmit(payload, {
      onSuccess: () => onClose(),
      onError: (err) => setErrors({ submit: err.message }),
    });
  };

  const defaultTitle = isDespesa
    ? itemToEdit
      ? 'Editar Despesa Variável'
      : 'Nova Despesa Variável'
    : itemToEdit
    ? 'Editar Receita Variável'
    : 'Nova Receita Variável';

  const modalTitle = title || defaultTitle;
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
      <form onSubmit={handleSubmit}>
        {errors.submit && (
          <div role="alert" className="text-red-600 text-sm mb-4">
            {errors.submit}
          </div>
        )}

        <div className="flex flex-col md:flex-row gap-6">
          {/* Left Column */}
          <div className="w-full md:w-1/2 space-y-4">
            <FormField
              id={`${idPrefix}-descricao`}
              label="Descrição"
              required
              error={errors.descricao}
            >
              {({ id, hasError, errorId, ariaRequired }) => (
                <input
                  type="text"
                  id={id}
                  autoFocus
                  maxLength={100}
                  value={descricao}
                  onChange={(e) => handleDescricaoChange(e.target.value)}
                  disabled={isPending}
                  aria-required={ariaRequired}
                  aria-invalid={hasError}
                  aria-describedby={errorId}
                  className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 ${
                    hasError
                      ? 'border-red-500 focus:border-red-500'
                      : 'border-gray-300 focus:border-primary'
                  }`}
                />
              )}
            </FormField>

            <ContaCategoriaFields
              contaId={contaId}
              categoriaId={categoriaId}
              onChangeConta={handleContaChange}
              onChangeCategoria={handleCategoriaChange}
              errors={{ contaId: errors.contaId, categoriaId: errors.categoriaId }}
              disabled={isPending}
              layout="grid"
              idPrefix={idPrefix}
            />

            <ParcelamentoFields
              valorParcela={valorParcela}
              onChangeValorParcela={handleValorParcelaChange}
              quantidadeParcelas={quantidadeParcelas}
              onChangeQuantidadeParcelas={handleQuantidadeParcelasChange}
              dataInicio={dataInicio}
              onChangeDataInicio={handleDataInicioChange}
              errors={{
                valorParcela: errors.valorParcela,
                quantidadeParcelas: errors.quantidadeParcelas,
                dataInicio: errors.dataInicio,
              }}
              disabled={isPending}
              theme={isDespesa ? 'red' : 'green'}
              idPrefix={idPrefix}
            />
          </div>

          {/* Right Column */}
          <div className="w-full md:w-1/2 space-y-4 flex flex-col min-h-0">
            {shouldIncludeLocalEDataCompra && (
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  id={`${idPrefix}-localCompra`}
                  label="Local da Compra"
                  error={errors.localCompra}
                >
                  {({ id, errorId, hasError }) => (
                    <input
                      type="text"
                      id={id}
                      maxLength={100}
                      value={localCompra}
                      onChange={(e) => {
                        setLocalCompra(e.target.value);
                        clearFieldError('localCompra');
                      }}
                      disabled={isPending}
                      aria-invalid={hasError}
                      aria-describedby={errorId}
                      className="mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border border-gray-300 disabled:opacity-50 disabled:bg-gray-100 focus:border-primary focus:ring-primary"
                    />
                  )}
                </FormField>

                <FormField
                  id={`${idPrefix}-dataCompra`}
                  label="Data da Compra"
                  error={errors.dataCompra}
                >
                  {({ id, hasError }) => (
                    <DatePicker
                      id={id}
                      value={dataCompra}
                      onChange={(val) => {
                        setDataCompra(val);
                        clearFieldError('dataCompra');
                      }}
                      disabled={isPending}
                      hasError={hasError}
                      theme={isDespesa ? 'red' : 'green'}
                      placeholder="DD/MM/AAAA"
                    />
                  )}
                </FormField>
              </div>
            )}

            <ObservacoesField
              id={`${idPrefix}-observacoes`}
              value={observacoes}
              onChange={setObservacoes}
              disabled={isPending}
              maxLength={300}
              className="flex-1 flex flex-col pt-2"
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

export default MovimentacaoVariavelFormModal;
