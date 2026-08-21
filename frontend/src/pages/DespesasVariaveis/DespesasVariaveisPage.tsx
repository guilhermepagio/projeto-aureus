import ActionMenu from '../../components/ui/ActionMenu';
import React, { useState } from 'react';
import { useDespesasVariaveis, type DespesaVariavel } from '../../hooks/useDespesasVariaveis';
import DespesaVariavelFormModal from './components/DespesaVariavelFormModal';
import DeleteConfirmModal from './components/DeleteConfirmModal';

const DespesasVariaveisPage: React.FC = () => {
  const { data: despesas, isLoading, isError } = useDespesasVariaveis();
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedDespesa, setSelectedDespesa] = useState<DespesaVariavel | null>(null);

  const handleCreate = () => {
    setSelectedDespesa(null);
    setIsFormModalOpen(true);
  };

  const handleEdit = (despesa: DespesaVariavel) => {
    setSelectedDespesa(despesa);
    setIsFormModalOpen(true);
  };

  const handleDelete = (despesa: DespesaVariavel) => {
    setSelectedDespesa(despesa);
    setIsDeleteModalOpen(true);
  };

  if (isLoading) return <div className="p-6">Carregando despesas variáveis...</div>;
  if (isError) return <div className="p-6 text-red-600">Erro ao carregar despesas variáveis.</div>;

  const isEmpty = !despesas || despesas.length === 0;

  return (
    <div className="px-4 pb-4 w-full">
      <div className="mb-4 mt-2 flex justify-between items-center">
        <div className="pl-2 border-l-4 border-red-600">
          <h1 className="text-2xl font-bold text-gray-800">Despesas Variáveis</h1>
        </div>
        <button
          onClick={handleCreate}
          className="cursor-pointer h-8 px-4 flex items-center justify-center bg-red-600 hover:bg-red-700 text-white rounded-md text-xs font-semibold whitespace-nowrap shadow-sm"
        >
          + Nova Despesa
        </button>
      </div>
      {isEmpty ? (
        <div className="text-center py-12 bg-white rounded-lg shadow-sm border border-gray-200 mt-4">
          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <h3 className="mt-2 text-sm font-semibold text-gray-900">Nenhuma despesa variável</h3>
          <p className="mt-1 text-sm text-gray-500">Comece criando sua primeira despesa variável.</p>
          <div className="mt-6">
            <button
              onClick={handleCreate}
              type="button"
              className="cursor-pointer inline-flex items-center rounded-md bg-red-600 px-3 py-1 text-sm font-semibold text-white shadow-sm hover:bg-red-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-600"
            >
              Nova Despesa Variável
            </button>
          </div>
        </div>
      ) : (
        <div className="bg-white shadow sm:rounded-2xl overflow-visible mt-2 w-full">
          <table className="min-w-max w-full divide-y divide-gray-200 table-fixed">
            <thead className="bg-gray-50 sticky top-0 z-10 shadow-sm">
              <tr className="divide-x divide-gray-200">
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[190px]">
                  Descrição
                </th>
                <th scope="col" className="px-1 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[80px]">
                  Data Compra
                </th>
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[160px]">
                  Local Compra
                </th>
                <th scope="col" className="px-3 py-1 text-center tabular-nums text-xs font-semibold text-gray-700 uppercase tracking-wider w-[100px]">
                  Parcela
                </th>
                <th scope="col" className="px-3 py-1 text-center tabular-nums text-xs font-semibold text-gray-700 uppercase tracking-wider w-[80px]">
                  Qtd. Parcelas
                </th>
                <th scope="col" className="px-3 py-1 text-center tabular-nums text-xs font-semibold text-gray-700 uppercase tracking-wider w-[120px]">
                  Valor Total
                </th>
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[150px]">
                  Categoria
                </th>
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[150px]">
                  Conta
                </th>
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[180px]">
                  Observações
                </th>
                <th scope="col" className="sticky right-0 px-1 py-1 bg-gray-50 z-20 shadow-[-4px_0_8px_rgba(0,0,0,0.05)] w-[44px]">
                  <span className="sr-only">Ações</span>
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {despesas.map((despesa) => (
                <tr key={despesa.id} className="group even:bg-gray-200 odd:bg-white text-xs divide-x divide-gray-200">
                  <td className="px-3 py-1 font-medium text-gray-900 align-middle">
                    <div className="truncate w-full" title={despesa.descricao}>
                      {despesa.descricao}
                    </div>
                  </td>
                  <td className="px-1 py-1 text-center text-gray-900 align-middle whitespace-nowrap">
                    {despesa.dataCompra ? despesa.dataCompra.split('-').reverse().join('/') : '-'}
                  </td>
                  <td className="px-3 py-1 text-center text-gray-900 align-middle">
                    <div className="truncate w-full" title={despesa.localCompra || ''}>
                      {despesa.localCompra || '-'}
                    </div>
                  </td>
                  <td className="px-3 py-1 text-center font-semibold text-red-600 tabular-nums whitespace-nowrap align-middle">
                    {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(despesa.valorParcela)}
                  </td>
                  <td className="px-3 py-1 text-center tabular-nums text-gray-900 align-middle">
                    {despesa.quantidadeParcelas}
                  </td>
                  <td className="px-3 py-1 text-center font-semibold text-gray-900 tabular-nums whitespace-nowrap align-middle">
                    {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(despesa.valorParcela * despesa.quantidadeParcelas)}
                  </td>
                  <td className="px-3 py-1 text-center text-gray-900 align-middle">
                    <div className="truncate w-full" title={despesa.categoria?.descricao}>
                      {despesa.categoria?.descricao}
                    </div>
                  </td>
                  <td className="px-3 py-1 text-center text-gray-900 align-middle">
                    <div className="truncate w-full" title={despesa.conta?.descricao}>
                      {despesa.conta?.descricao}
                    </div>
                  </td>
                  <td className="px-3 py-1 text-gray-900 align-middle">
                    <div className="truncate w-full" title={despesa.observacoes}>
                      {despesa.observacoes || '-'}
                    </div>
                  </td>
                  <td className="sticky right-0 px-1 py-1 group-even:bg-gray-200 group-odd:bg-white shadow-[-4px_0_8px_rgba(0,0,0,0.05)] align-middle">
                    <div className="flex justify-center w-full"><ActionMenu onEdit={() => handleEdit(despesa)} onDelete={() => handleDelete(despesa)} /></div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <DespesaVariavelFormModal
        isOpen={isFormModalOpen}
        onClose={() => setIsFormModalOpen(false)}
        despesaToEdit={selectedDespesa}
      />

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        despesaToDelete={selectedDespesa}
      />
    </div>
  );
};

export default DespesasVariaveisPage;
