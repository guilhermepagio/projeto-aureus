import ActionMenu from '../../components/ui/ActionMenu';
import React, { useState } from 'react';
import { useCategorias, type Categoria } from '../../hooks/useCategorias';
import CategoriaFormModal from './components/CategoriaFormModal';
import DeleteConfirmModal from './components/DeleteConfirmModal';
import EmptyState from '../../components/ui/EmptyState';

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
        <div className="pl-2 border-l-4 border-blue-600">
          <h1 className="text-2xl font-bold text-gray-800">Categorias</h1>
        </div>
        <button
          onClick={handleCreate}
          className="cursor-pointer h-8 px-4 flex items-center justify-center bg-blue-600 hover:bg-blue-700 text-white rounded-md text-xs font-semibold whitespace-nowrap shadow-sm"
        >
          + Nova Categoria
        </button>
      </div>
      {isEmpty ? (
        <EmptyState
          title="Nenhuma categoria"
          description="Comece criando sua primeira categoria."
          action={{ label: 'Nova Categoria', onClick: handleCreate, color: 'blue' }}
        />
      ) : (
        <div className="bg-white shadow sm:rounded-2xl overflow-visible mt-2 w-full">
          <table className="min-w-max w-full divide-y divide-gray-200 table-fixed">
            <thead className="bg-gray-50 sticky top-0 z-10 shadow-sm">
              <tr className="divide-x divide-gray-200 h-[44px]">
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider w-[280px]">
                  Descrição
                </th>
                <th scope="col" className="px-3 py-1 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider min-w-[330px]">
                  Observações
                </th>
                <th scope="col" className="sticky right-0 px-1 py-1 bg-gray-50 z-20 shadow-[-4px_0_8px_rgba(0,0,0,0.05)] w-[44px]">
                  <span className="sr-only">Ações</span>
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {categorias.map((categoria) => (
                <tr key={categoria.id} className="group even:bg-gray-200 odd:bg-white text-sm divide-x divide-gray-200">
                  <td className="px-3 py-1 font-medium text-gray-900 align-middle">
                    <div className="line-clamp-3 whitespace-normal break-words" title={categoria.descricao}>
                      {categoria.descricao}
                    </div>
                  </td>
                  <td className="px-3 py-1 text-gray-900 align-middle">
                    <div className="line-clamp-3 whitespace-normal break-words" title={categoria.observacoes}>
                      {categoria.observacoes || '-'}
                    </div>
                  </td>
                  <td className="sticky right-0 px-1 py-1 group-even:bg-gray-200 group-odd:bg-white shadow-[-4px_0_8px_rgba(0,0,0,0.05)] align-middle">
                    <div className="flex justify-center w-full"><ActionMenu onEdit={() => handleEdit(categoria)} onDelete={() => handleDelete(categoria)} /></div>
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
