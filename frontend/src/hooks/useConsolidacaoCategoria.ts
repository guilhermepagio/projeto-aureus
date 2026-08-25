import { useQuery } from '@tanstack/react-query';
import { useAuthStore } from '../store/authStore';

export interface LinhaConsolidacaoCategoriaDTO {
  categoriaId: number;
  categoriaDescricao: string;
  valoresMensais: number[];
}

export interface ConsolidacaoPorCategoriaDTO {
  receitas: LinhaConsolidacaoCategoriaDTO[];
  despesas: LinhaConsolidacaoCategoriaDTO[];
}

const getCsrfToken = () => {
  const match = document.cookie.match(new RegExp('(^| )XSRF-TOKEN=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : '';
};

const fetchConsolidacaoPorCategoria = async (mesAno: string): Promise<ConsolidacaoPorCategoriaDTO> => {
  const response = await fetch(`/api/consolidacao/por-categoria?mesAno=${mesAno}`, {
    headers: {
      'X-XSRF-TOKEN': getCsrfToken()
    }
  });
  if (!response.ok) throw new Error('Erro ao buscar consolidação por categoria');
  return response.json();
};

export function useConsolidacaoCategoria(mesAno: string | null) {
  const { isAuthenticated } = useAuthStore();
  
  return useQuery({
    queryKey: ['consolidacao-categoria', mesAno],
    queryFn: () => fetchConsolidacaoPorCategoria(mesAno!),
    enabled: !!mesAno && isAuthenticated,
  });
}
