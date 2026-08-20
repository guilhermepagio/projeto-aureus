import React, { useState, useEffect } from 'react';
import Modal from '../../../components/ui/Modal';
import { useCreateReceitaFixa, useUpdateReceitaFixa, type ReceitaFixa } from '../../../hooks/useReceitasFixas';
import { useContas } from '../../../hooks/useContas';
import { useCategorias } from '../../../hooks/useCategorias';

interface ReceitaFixaFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  receitaToEdit?: ReceitaFixa | null;
}

const ReceitaFixaFormModal: React.FC<ReceitaFixaFormModalProps> = ({ isOpen, onClose, receitaToEdit }) => {
  const [descricao, setDescricao] = useState('');
  const [valor, setValor] = useState<number | ''>('');
  const [contaId, setContaId] = useState<number | ''>('');
  const [categoriaId, setCategoriaId] = useState<number | ''>('');
  const [dataInicio, setDataInicio] = useState('');
  const [observacoes, setObservacoes] = useState('');
  
  const [errors, setErrors] = useState<Record<string, string>>({});

  const { data: contas } = useContas();
  const { data: categorias } = useCategorias();

  const createMutation = useCreateReceitaFixa();
  const updateMutation = useUpdateReceitaFixa();

  useEffect(() => {
    if (isOpen) {
      if (receitaToEdit) {
        setDescricao(receitaToEdit.descricao || '');
        setValor(receitaToEdit.valor);
        setContaId(receitaToEdit.conta?.id || '');
        setCategoriaId(receitaToEdit.categoria?.id || '');
        setDataInicio(receitaToEdit.dataInicio ? receitaToEdit.dataInicio.substring(0, 7) : '');
        setObservacoes(receitaToEdit.observacoes || '');
      } else {
        setDescricao('');
        setValor('');
        setContaId('');
        setCategoriaId('');
        
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        setDataInicio(`${year}-${month}`);
        
        setObservacoes('');
      }
      setErrors({});
    }
  }, [isOpen, receitaToEdit]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const newErrors: Record<string, string> = {};

    if (!descricao.trim()) newErrors.descricao = 'A descrição é obrigatória';
    if (!valor || Number(valor) <= 0) newErrors.valor = 'O valor deve ser maior que zero';
    if (!contaId) newErrors.contaId = 'Selecione uma conta';
    if (!categoriaId) newErrors.categoriaId = 'Selecione uma categoria';
    if (!dataInicio) newErrors.dataInicio = 'Data de início é obrigatória';

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    const payload = {
      descricao: descricao.trim(),
      valor: Number(valor),
      conta: { id: Number(contaId) },
      categoria: { id: Number(categoriaId) },
      dataInicio: `${dataInicio}-01`,
      observacoes: observacoes.trim()
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
      title={receitaToEdit ? 'Editar Receita Fixa' : 'Nova Receita Fixa'}
      disableClose={isPending}
      maxWidth="max-w-4xl"
    >
      <form onSubmit={handleSubmit}>
        {errors.submit && <div className="text-red-600 text-sm mb-4">{errors.submit}</div>}

        <div className="flex flex-col md:flex-row gap-6">
          {/* Left Column */}
          <div className="w-full md:w-1/2 space-y-4">
            <div>
              <label htmlFor="descricao" className="block text-sm font-medium text-gray-700">
                Descrição *
              </label>
              <input
                type="text"
                id="descricao"
                autoFocus
                value={descricao}
                onChange={(e) => setDescricao(e.target.value)}
                disabled={isPending}
                className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 ${errors.descricao ? 'border-red-500 focus:border-red-500 focus:ring-red-500' : 'border-gray-300 focus:border-primary focus:ring-primary'}`}
                placeholder="Ex: Salário"
                maxLength={100}
              />
              {errors.descricao && <p className="mt-1 text-sm text-red-600">{errors.descricao}</p>}
            </div>

            <div>
              <label htmlFor="valor" className="block text-sm font-medium text-gray-700">
                Valor *
              </label>
              <input
                type="number"
                id="valor"
                step="0.01"
                min="0.01"
                value={valor}
                onChange={(e) => setValor(e.target.value ? Number(e.target.value) : '')}
                disabled={isPending}
                className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 ${errors.valor ? 'border-red-500 focus:border-red-500 focus:ring-red-500' : 'border-gray-300 focus:border-primary focus:ring-primary'}`}
                placeholder="0.00"
              />
              {errors.valor && <p className="mt-1 text-sm text-red-600">{errors.valor}</p>}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="contaId" className="block text-sm font-medium text-gray-700">
                  Conta *
                </label>
                <select
                  id="contaId"
                  value={contaId}
                  onChange={(e) => setContaId(e.target.value ? Number(e.target.value) : '')}
                  disabled={isPending}
                  className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 ${errors.contaId ? 'border-red-500 focus:border-red-500 focus:ring-red-500' : 'border-gray-300 focus:border-primary focus:ring-primary'}`}
                >
                  <option value="">Selecione...</option>
                  {contas?.map(c => (
                    <option key={c.id} value={c.id}>{c.descricao}</option>
                  ))}
                </select>
                {errors.contaId && <p className="mt-1 text-sm text-red-600">{errors.contaId}</p>}
              </div>

              <div>
                <label htmlFor="categoriaId" className="block text-sm font-medium text-gray-700">
                  Categoria *
                </label>
                <select
                  id="categoriaId"
                  value={categoriaId}
                  onChange={(e) => setCategoriaId(e.target.value ? Number(e.target.value) : '')}
                  disabled={isPending}
                  className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 ${errors.categoriaId ? 'border-red-500 focus:border-red-500 focus:ring-red-500' : 'border-gray-300 focus:border-primary focus:ring-primary'}`}
                >
                  <option value="">Selecione...</option>
                  {categorias?.map(c => (
                    <option key={c.id} value={c.id}>{c.descricao}</option>
                  ))}
                </select>
                {errors.categoriaId && <p className="mt-1 text-sm text-red-600">{errors.categoriaId}</p>}
              </div>
            </div>

            <div>
              <label htmlFor="dataInicio" className="block text-sm font-medium text-gray-700">
                Competência (Início) *
              </label>
              <input
                type="month"
                id="dataInicio"
                value={dataInicio}
                onChange={(e) => setDataInicio(e.target.value)}
                disabled={isPending}
                className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 ${errors.dataInicio ? 'border-red-500 focus:border-red-500 focus:ring-red-500' : 'border-gray-300 focus:border-primary focus:ring-primary'}`}
              />
              {errors.dataInicio && <p className="mt-1 text-sm text-red-600">{errors.dataInicio}</p>}
            </div>
          </div>

          {/* Right Column */}
          <div className="w-full md:w-1/2 flex flex-col min-h-0">
            <label htmlFor="observacoes" className="block text-sm font-medium text-gray-700">
              Observações
            </label>
            <textarea
              id="observacoes"
              value={observacoes}
              onChange={(e) => setObservacoes(e.target.value)}
              disabled={isPending}
              className="mt-1 block w-full flex-1 min-h-0 rounded-md border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 resize-none overflow-y-auto"
              maxLength={300}
            />
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

export default ReceitaFixaFormModal;
