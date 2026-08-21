import React, { useState } from 'react';
import { useCategorias, type Categoria } from '../../hooks/useCategorias';
import CategoriaFormModal from './components/CategoriaFormModal';
import DeleteConfirmModal from './components/DeleteConfirmModal';

const CategoriasPage: React.FC = () => {
  const { data: categorias, isLoading, isError } = useCategorias();
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedCategoria, setSelectedCategoria] = useState<Categoria | null>(null);

  const handleCreate = () => {
    setSelectedCategoria(null);
    setIsFormModalOpen(true);
  };

  const handleEdit = (categoria: Categoria) => {
    setSelectedCategoria(categoria);
    setIsFormModalOpen(true);
  };

  const handleDelete = (categoria: Categoria) => {
    setSelectedCategoria(categoria);
    setIsDeleteModalOpen(true);
  };

  if (isLoading) return <div className="p-6">Carregando categorias...</div>;
  if (isError) return <div className="p-6 text-red-600">Erro ao carregar categorias.</div>;

  const isEmpty = !categorias || categorias.length === 0;

  return (
    <div className="px-4 pb-4 w-full">
      <div className="mb-4 mt-2 flex justify-between items-center">
        <div className="pl-2 border-l-4 border-primary">
          <h1 className="text-2xl font-bold text-gray-800">Categorias</h1>
        </div>
        <button
          onClick={handleCreate}
          className="cursor-pointer h-8 px-4 flex items-center justify-center bg-primary hover:bg-primary-light text-white rounded-md text-xs font-semibold whitespace-nowrap shadow-sm"
        >
          + Nova Categoria
        </button>
      </div>
      {isEmpty ? (
        <div className="text-center py-12 bg-white rounded-lg shadow-sm border border-gray-200 mt-4">
          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
          </svg>
          <h3 className="mt-2 text-sm font-semibold text-gray-900">Nenhuma categoria</h3>
          <p className="mt-1 text-sm text-gray-500">Comece criando sua primeira categoria.</p>
          <div className="mt-6">
            <button
              onClick={handleCreate}
              type="button"
              className="cursor-pointer inline-flex items-center rounded-md bg-primary px-3 py-1 text-sm font-semibold text-white shadow-sm hover:bg-primary-light focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
            >
              Nova Categoria
            </button>
          </div>
        </div>
      ) : (
        <div className="bg-white shadow sm:rounded-2xl overflow-x-auto mt-2 w-full">
          <table className="min-w-max w-full divide-y divide-gray-200 table-fixed">
            <thead className="bg-gray-50 sticky top-0 z-10 shadow-sm">
              <tr className="divide-x divide-gray-200">
                <th scope="col" className="px-6 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[250px]">
                  Descrição
                </th>
                <th scope="col" className="px-6 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider min-w-[300px]">
                  Observações
                </th>
                <th scope="col" className="sticky right-0 px-6 py-1 bg-gray-50 z-20 shadow-[-4px_0_8px_rgba(0,0,0,0.05)] w-[180px] text-center text-xs font-semibold text-gray-700 uppercase tracking-wider">
                  Ações
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {categorias.map((categoria) => (
                <tr key={categoria.id} className="group even:bg-gray-200 odd:bg-white text-sm divide-x divide-gray-200">
                  <td className="px-6 py-1 font-medium text-gray-900 align-middle">
                    <div className="line-clamp-2 whitespace-normal break-words" title={categoria.descricao}>
                      {categoria.descricao}
                    </div>
                  </td>
                  <td className="px-6 py-1 text-gray-900 align-middle">
                    <div className="line-clamp-2 whitespace-normal break-words" title={categoria.observacoes}>
                      {categoria.observacoes || '-'}
                    </div>
                  </td>
                  <td className="sticky right-0 px-6 py-1 group-even:bg-gray-200 group-odd:bg-white shadow-[-4px_0_8px_rgba(0,0,0,0.05)] align-middle">
                    <div className="flex justify-end ml-auto w-full gap-2">
                      <button
                        type="button"
                        onClick={() => handleEdit(categoria)}
                        className="flex-1 h-8 flex items-center justify-center cursor-pointer bg-transparent text-primary hover:text-primary-light text-xs font-semibold text-center transition-colors"
                        title="Editar"
                      >
                        Editar
                      </button>
                      <button
                        type="button"
                        onClick={() => handleDelete(categoria)}
                        className="flex-1 h-8 flex items-center justify-center cursor-pointer bg-transparent text-red-600 hover:text-red-500 text-xs font-semibold text-center transition-colors"
                        title="Excluir"
                      >
                        Excluir
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <CategoriaFormModal
        isOpen={isFormModalOpen}
        onClose={() => setIsFormModalOpen(false)}
        categoriaToEdit={selectedCategoria}
      />

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        categoriaToDelete={selectedCategoria}
      />
    </div>
  );
};

export default CategoriasPage;
