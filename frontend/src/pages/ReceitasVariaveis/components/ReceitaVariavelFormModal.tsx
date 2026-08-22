import React, { useState, useEffect, useMemo } from 'react';
import { formatCurrency, parseCurrency } from '../../../utils/currencyFormat';
import Modal from '../../../components/ui/Modal';
import { useCreateReceitaVariavel, useUpdateReceitaVariavel, type ReceitaVariavel } from '../../../hooks/useReceitasVariaveis';
import { useContas } from '../../../hooks/useContas';
import { useCategorias } from '../../../hooks/useCategorias';

interface ReceitaVariavelFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  receitaToEdit?: ReceitaVariavel | null;
}

const ReceitaVariavelFormModal: React.FC<ReceitaVariavelFormModalProps> = ({ isOpen, onClose, receitaToEdit }) => {
  const [descricao, setDescricao] = useState('');
  const [valorParcela, setValorParcela] = useState<string>('');
  const [quantidadeParcelas, setQuantidadeParcelas] = useState<number | ''>('');
  const [dataInicio, setDataInicio] = useState('');
  const [contaId, setContaId] = useState<number | ''>('');
  const [categoriaId, setCategoriaId] = useState<number | ''>('');
  const [observacoes, setObservacoes] = useState('');
  
  const [errors, setErrors] = useState<Record<string, string>>({});

  const { data: contas } = useContas();
  const { data: categorias } = useCategorias();

  const createMutation = useCreateReceitaVariavel();
  const updateMutation = useUpdateReceitaVariavel();

  useEffect(() => {
    if (isOpen) {
      if (receitaToEdit) {
        setDescricao(receitaToEdit.descricao || '');
        setValorParcela(receitaToEdit.valorParcela ? formatCurrency(receitaToEdit.valorParcela) : '');
        setQuantidadeParcelas(receitaToEdit.quantidadeParcelas || '');
        setDataInicio(receitaToEdit.dataInicio ? receitaToEdit.dataInicio.substring(0, 7) : '');
        setContaId(receitaToEdit.conta?.id || '');
        setCategoriaId(receitaToEdit.categoria?.id || '');
        setObservacoes(receitaToEdit.observacoes || '');
      } else {
        setDescricao('');
        setValorParcela('');
        setQuantidadeParcelas('');
        setDataInicio('');
        setContaId('');
        setCategoriaId('');
        setObservacoes('');
      }
      setErrors({});
    }
  }, [isOpen, receitaToEdit]);

  const valorTotalPreview = useMemo(() => {
    const valorNum = parseCurrency(valorParcela);
    const qtdNum = Number(quantidadeParcelas);
    if (valorNum > 0 && qtdNum > 0) {
      const totalSafe = Math.round(valorNum * qtdNum * 100) / 100;
      return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(totalSafe);
    }
    return '-';
  }, [valorParcela, quantidadeParcelas]);

  const ultimaParcelaPreview = useMemo(() => {
    const qtdNum = Number(quantidadeParcelas);
    if (dataInicio && qtdNum > 0) {
      const [year, month] = dataInicio.split('-');
      const d = new Date(Number(year), Number(month) - 1, 1);
      d.setMonth(d.getMonth() + qtdNum - 1);
      const monthName = d.toLocaleDateString('pt-BR', { month: 'long' });
      const capitalizedMonth = monthName.charAt(0).toUpperCase() + monthName.slice(1);
      return `${capitalizedMonth} de ${d.getFullYear()}`;
    }
    return '-';
  }, [dataInicio, quantidadeParcelas]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const newErrors: Record<string, string> = {};

    if (!descricao.trim()) newErrors.descricao = 'A descrição é obrigatória';
    if (!valorParcela || Number(parseCurrency(valorParcela)) <= 0) newErrors.valorParcela = 'O valor da parcela deve ser maior que zero';
    if (!quantidadeParcelas || Number(quantidadeParcelas) < 1) newErrors.quantidadeParcelas = 'Mínimo de 1 parcela';
    if (!dataInicio) newErrors.dataInicio = 'Data da 1ª parcela é obrigatória';
    if (!contaId) newErrors.contaId = 'Selecione uma conta';
    if (!categoriaId) newErrors.categoriaId = 'Selecione uma categoria';

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    const payload = {
      descricao: descricao.trim(),
      valorParcela: parseCurrency(valorParcela),
      quantidadeParcelas: Number(quantidadeParcelas),
      dataInicio: dataInicio.length === 7 ? `${dataInicio}-01` : dataInicio,
      conta: { id: Number(contaId) },
      categoria: { id: Number(categoriaId) },
      observacoes: observacoes.trim() || undefined
    };

    if (receitaToEdit) {
      updateMutation.mutate(
        { id: receitaToEdit.id, ...payload },
        { 
          onSuccess: () => onClose(),
          onError: (err) => setErrors({ submit: err.message })
        }
      );
    } else {
      createMutation.mutate(
        payload,
        { 
          onSuccess: () => onClose(),
          onError: (err) => setErrors({ submit: err.message })
        }
      );
    }
  };

  const isPending = createMutation.isPending || updateMutation.isPending;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={receitaToEdit ? 'Editar Receita Variável' : 'Nova Receita Variável'}
      disableClose={isPending}
      maxWidth="max-w-4xl"
    >
      <form onSubmit={handleSubmit}>
        {errors.submit && <div className="text-red-600 text-sm mb-4">{errors.submit}</div>}

        <div className="flex flex-col md:flex-row gap-6">
          <div className="w-full md:w-1/2 space-y-4">
            <div>
              <label htmlFor="receita-descricao" className="block text-sm font-medium text-gray-700">
                Descrição *
              </label>
              <input
                type="text"
                id="receita-descricao"
                autoFocus
                maxLength={100}
                value={descricao}
                onChange={(e) => setDescricao(e.target.value)}
                disabled={isPending}
                className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 ${errors.descricao ? 'border-red-500 focus:border-red-500' : 'border-gray-300 focus:border-primary'}`}
              />
              {errors.descricao && <p className="mt-1 text-sm text-red-600">{errors.descricao}</p>}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="receita-categoriaId" className="block text-sm font-medium text-gray-700">
                  Categoria *
                </label>
                <select
                  id="receita-categoriaId"
                  value={categoriaId}
                  onChange={(e) => setCategoriaId(e.target.value ? Number(e.target.value) : '')}
                  disabled={isPending}
                  className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 ${errors.categoriaId ? 'border-red-500 focus:border-red-500' : 'border-gray-300 focus:border-primary'}`}
                >
                  <option value="">Selecione...</option>
                  {categorias?.map(c => (
                    <option key={c.id} value={c.id}>{c.descricao}</option>
                  ))}
                </select>
                {errors.categoriaId && <p className="mt-1 text-sm text-red-600">{errors.categoriaId}</p>}
              </div>
              <div>
                <label htmlFor="receita-contaId" className="block text-sm font-medium text-gray-700">
                  Conta *
                </label>
                <select
                  id="receita-contaId"
                  value={contaId}
                  onChange={(e) => setContaId(e.target.value ? Number(e.target.value) : '')}
                  disabled={isPending}
                  className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 ${errors.contaId ? 'border-red-500 focus:border-red-500' : 'border-gray-300 focus:border-primary'}`}
                >
                  <option value="">Selecione...</option>
                  {contas?.map(c => (
                    <option key={c.id} value={c.id}>{c.descricao}</option>
                  ))}
                </select>
                {errors.contaId && <p className="mt-1 text-sm text-red-600">{errors.contaId}</p>}
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="receita-valorParcela" className="block text-sm font-medium text-gray-700">
                  Valor Parcela *
                </label>
                <input
                  type="text"
                  id="receita-valorParcela"
                  value={valorParcela}
                  onChange={(e) => setValorParcela(formatCurrency(e.target.value))}
                  disabled={isPending}
                  className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 ${errors.valorParcela ? 'border-red-500 focus:border-red-500' : 'border-gray-300 focus:border-primary'}`}
                />
                {errors.valorParcela && <p className="mt-1 text-sm text-red-600">{errors.valorParcela}</p>}
              </div>
              <div>
                <label htmlFor="receita-quantidadeParcelas" className="block text-sm font-medium text-gray-700">
                  Qtd. Parcelas *
                </label>
                <input
                  type="number"
                  step="1"
                  id="receita-quantidadeParcelas"
                  value={quantidadeParcelas}
                  onChange={(e) => setQuantidadeParcelas(e.target.value ? Number(e.target.value) : '')}
                  disabled={isPending}
                  min={1}
                  className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 ${errors.quantidadeParcelas ? 'border-red-500 focus:border-red-500' : 'border-gray-300 focus:border-primary'}`}
                />
                {errors.quantidadeParcelas && <p className="mt-1 text-sm text-red-600">{errors.quantidadeParcelas}</p>}
              </div>
            </div>

            <div className="bg-teal-50 border border-teal-200 rounded-md p-3">
              <p className="text-sm text-teal-800 font-medium">Valor Total: {valorTotalPreview}</p>
            </div>
            
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="receita-dataInicio" className="block text-sm font-medium text-gray-700">
                  Primeira Parcela (Mês) *
                </label>
                <input
                  type="month"
                  id="receita-dataInicio"
                  value={dataInicio}
                  onChange={(e) => setDataInicio(e.target.value)}
                  disabled={isPending}
                  className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 ${errors.dataInicio ? 'border-red-500 focus:border-red-500' : 'border-gray-300 focus:border-primary'}`}
                />
                {errors.dataInicio && <p className="mt-1 text-sm text-red-600">{errors.dataInicio}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Última Parcela (Preview)
                </label>
                <div className="mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border border-gray-300 bg-gray-50 text-gray-500">
                  {ultimaParcelaPreview}
                </div>
              </div>
            </div>
          </div>

          <div className="w-full md:w-1/2 space-y-4 flex flex-col min-h-0">
            

            <div className="flex-1 flex flex-col pt-2">
              <label htmlFor="receita-observacoes" className="block text-sm font-medium text-gray-700">
                Observações
              </label>
              <textarea
                id="receita-observacoes"
                value={observacoes}
                onChange={(e) => setObservacoes(e.target.value)}
                disabled={isPending}
                className="mt-1 block w-full flex-1 min-h-[160px] rounded-md border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 resize-none overflow-y-auto"
                maxLength={300}
              />
            </div>
          </div>
        </div>

        <div className="flex justify-end space-x-3 mt-6">
          <button
            type="button"
            onClick={onClose}
            className="cursor-pointer px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none"
            disabled={isPending}
          >
            Cancelar
          </button>
          <button
            type="submit"
            className="cursor-pointer px-4 py-2 text-sm font-medium text-white bg-primary border border-transparent rounded-md hover:bg-primary-light focus:outline-none"
            disabled={isPending}
          >
            {isPending ? 'Salvando...' : 'Salvar'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default ReceitaVariavelFormModal;
