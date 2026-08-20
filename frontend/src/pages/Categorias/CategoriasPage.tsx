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
    <div className="p-6 max-w-4xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Categorias</h1>
        <button
          onClick={handleCreate}
          className="cursor-pointer bg-primary text-white px-4 py-2 rounded-md hover:bg-primary-light text-sm font-medium"
        >
          Nova Categoria
        </button>
      </div>

      {isEmpty ? (
        <div className="text-center py-12 bg-white rounded-lg shadow-sm border border-gray-200">
          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
          </svg>
          <h3 className="mt-2 text-sm font-semibold text-gray-900">Nenhuma categoria</h3>
          <p className="mt-1 text-sm text-gray-500">Comece criando sua primeira categoria.</p>
          <div className="mt-6">
            <button
              onClick={handleCreate}
              type="button"
              className="cursor-pointer inline-flex items-center rounded-md bg-primary px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-primary focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
            >
              <svg className="-ml-0.5 mr-1.5 h-5 w-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                <path d="M10.75 4.75a.75.75 0 00-1.5 0v4.5h-4.5a.75.75 0 000 1.5h4.5v4.5a.75.75 0 001.5 0v-4.5h4.5a.75.75 0 000-1.5h-4.5v-4.5z" />
              </svg>
              Nova Categoria
            </button>
          </div>
        </div>
      ) : (
        <div className="bg-white shadow overflow-hidden sm:rounded-md">
          <ul role="list" className="divide-y divide-gray-200">
            {categorias.map((categoria) => (
              <li key={categoria.id} className="px-4 py-4 sm:px-6 flex items-center justify-between hover:bg-gray-50">
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-primary truncate">{categoria.descricao}</p>
                  {categoria.observacoes && (
                    <p className="mt-1 text-sm text-gray-500 truncate">{categoria.observacoes}</p>
                  )}
                </div>
                <div className="flex space-x-3 ml-4">
                  <button
                    type="button"
                    onClick={() => handleEdit(categoria)}
                    className="font-inherit cursor-pointer text-gray-400 hover:text-primary"
                    title="Editar"
                  >
                    Editar
                  </button>
                  <button
                    type="button"
                    onClick={() => handleDelete(categoria)}
                    className="font-inherit cursor-pointer text-gray-400 hover:text-red-600"
                    title="Excluir"
                  >
                    Excluir
                  </button>
                </div>
              </li>
            ))}
          </ul>
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
