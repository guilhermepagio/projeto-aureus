import { Link, useLocation } from 'react-router-dom';
import { Landmark, Tags } from 'lucide-react';
import { useContas } from '../hooks/useContas';
import { useCategorias } from '../hooks/useCategorias';
import type { ReactNode, FC } from 'react';

interface RequiresDependenciesProps {
  children: ReactNode;
}

const RequiresDependencies: FC<RequiresDependenciesProps> = ({ children }) => {
  const location = useLocation();
  const { data: contas, isLoading: isLoadingContas, isError: isErrorContas } = useContas();
  const { data: categorias, isLoading: isLoadingCategorias, isError: isErrorCategorias } = useCategorias();

  if (isLoadingContas || isLoadingCategorias) {
    return <div className="p-6 text-center">Carregando informações...</div>;
  }

  if (isErrorContas || isErrorCategorias) {
    console.error('Erro ao carregar dependências para os formulários de movimentação');
    return (
      <div className="p-6 text-center text-red-600">
        Erro ao carregar contas ou categorias. Por favor, tente novamente.
      </div>
    );
  }

  const hasContas = contas && contas.length > 0;
  const hasCategorias = categorias && categorias.length > 0;

  if (!hasContas) {
    return (
      <div className="p-6 max-w-lg mx-auto">
        <div className="text-center py-12 bg-white rounded-lg shadow-sm border border-gray-200">
          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
          </svg>
          <h3 className="mt-2 text-sm font-semibold text-gray-900">Nenhuma conta cadastrada</h3>
          <p className="mt-1 text-sm text-gray-500">Você precisa de pelo menos uma conta para registrar movimentações.</p>
          <div className="mt-6">
            <Link
              to="/contas"
              state={{ from: location.pathname }}
              className="inline-flex items-center rounded-md bg-primary px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-primary-light focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
            >
              <Landmark size={20} className="-ml-0.5 mr-1.5" />
              Cadastrar Conta
            </Link>
          </div>
        </div>
      </div>
    );
  }

  if (!hasCategorias) {
    return (
      <div className="p-6 max-w-lg mx-auto">
        <div className="text-center py-12 bg-white rounded-lg shadow-sm border border-gray-200">
          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
          </svg>
          <h3 className="mt-2 text-sm font-semibold text-gray-900">Nenhuma categoria cadastrada</h3>
          <p className="mt-1 text-sm text-gray-500">Você precisa de pelo menos uma categoria para registrar movimentações.</p>
          <div className="mt-6">
            <Link
              to="/categorias"
              state={{ from: location.pathname }}
              className="inline-flex items-center rounded-md bg-primary px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-primary-light focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
            >
              <Tags size={20} className="-ml-0.5 mr-1.5" />
              Cadastrar Categoria
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return <>{children}</>;
};

export default RequiresDependencies;
