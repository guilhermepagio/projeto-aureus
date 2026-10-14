import { Link, useLocation } from 'react-router-dom';
import { useContas } from '../hooks/useContas';
import { useCategorias } from '../hooks/useCategorias';
import type { ReactNode, FC } from 'react';
import PlusIcon from './ui/PlusIcon';

interface RequiresDependenciesProps {
  children: ReactNode;
}

const RequiresDependencies: FC<RequiresDependenciesProps> = ({ children }) => {
  const location = useLocation();
  const { data: contas, isLoading: isLoadingContas, isError: isErrorContas, refetch: refetchContas } = useContas();
  const { data: categorias, isLoading: isLoadingCategorias, isError: isErrorCategorias, refetch: refetchCategorias } = useCategorias();

  if (isLoadingContas || isLoadingCategorias) {
    return (
      <div className="p-6 max-w-lg mx-auto">
        <div className="text-center py-12">
          <p className="text-gray-500">Carregando informações...</p>
        </div>
      </div>
    );
  }

  const hasContas = contas && contas.length > 0;
  const hasCategorias = categorias && categorias.length > 0;

  const isDespesa = location.pathname.includes('despesas');
  const isReceita = location.pathname.includes('receitas');
  const themeColorClass = isDespesa
    ? 'bg-rose-800 hover:bg-rose-900 focus-visible:outline-rose-800'
    : isReceita
    ? 'bg-primary hover:bg-primary-light focus-visible:outline-primary'
    : 'bg-blue-600 hover:bg-blue-700 focus-visible:outline-blue-600';

  if ((isErrorContas && !hasContas) || (isErrorCategorias && !hasCategorias)) {
    console.error('Erro ao carregar dependências para os formulários de movimentação');
    return (
      <div className="p-6 max-w-lg mx-auto">
        <div className="text-center py-12">
          <h3 className="mt-2 text-sm font-semibold text-red-600">Erro ao carregar dados</h3>
          <p className="mt-1 text-sm text-gray-500 mb-6">Não foi possível carregar contas ou categorias. Por favor, tente novamente.</p>
          <button 
            onClick={() => { refetchContas(); refetchCategorias(); }}
            className={`cursor-pointer inline-flex items-center rounded-md px-3 py-2 text-sm font-semibold text-white shadow-sm focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 ${themeColorClass}`}
          >
            Tentar novamente
          </button>
        </div>
      </div>
    );
  }

  if (!hasContas || !hasCategorias) {
    const returnState = { from: location.pathname + location.search + location.hash };
    
    return (
      <div className="p-6 max-w-lg mx-auto">
        <div className="text-center py-12">
          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            {!hasContas ? (
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
            ) : (
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
            )}
          </svg>
          <h3 className="mt-2 text-sm font-semibold text-gray-900">
            {!hasContas && !hasCategorias ? 'Nenhuma conta ou categoria cadastrada' : !hasContas ? 'Nenhuma conta cadastrada' : 'Nenhuma categoria cadastrada'}
          </h3>
          <p className="mt-1 text-sm text-gray-500">
            {!hasContas && !hasCategorias 
              ? 'Você precisa de pelo menos uma conta e uma categoria para registrar movimentações.' 
              : !hasContas 
                ? 'Você precisa de pelo menos uma conta para registrar movimentações.' 
                : 'Você precisa de pelo menos uma categoria para registrar movimentações.'}
          </p>
          <div className="mt-6 flex flex-col sm:flex-row gap-3 justify-center items-center">
            {!hasContas && (
              <Link
                to="/contas"
                state={returnState}
                className={`cursor-pointer h-8 px-4 inline-flex items-center justify-center rounded-md text-xs font-semibold text-white shadow-sm focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 ${themeColorClass}`}
              >
                <PlusIcon />
                Cadastrar Conta
              </Link>
            )}
            {!hasCategorias && (
              <Link
                to="/categorias"
                state={returnState}
                className={`cursor-pointer h-8 px-4 inline-flex items-center justify-center rounded-md text-xs font-semibold text-white shadow-sm focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 ${themeColorClass}`}
              >
                <PlusIcon />
                Cadastrar Categoria
              </Link>
            )}
          </div>
        </div>
      </div>
    );
  }

  return <>{children}</>;
};

export default RequiresDependencies;
