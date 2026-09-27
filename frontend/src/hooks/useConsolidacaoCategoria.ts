import { useQuery } from '@tanstack/react-query';
import { useAuthStore } from '../store/authStore';
import { apiClient } from '../services/apiClient';

export interface LinhaConsolidacaoCategoriaDTO {
  categoriaId: number;
  categoriaDescricao: string;
  valoresMensais: number[];
}

export interface ConsolidacaoPorCategoriaDTO {
  despesas: LinhaConsolidacaoCategoriaDTO[];
}

const fetchConsolidacaoPorCategoria = async (mesAno: string): Promise<ConsolidacaoPorCategoriaDTO> => {
  return apiClient.get<ConsolidacaoPorCategoriaDTO>(`/api/consolidacao/por-categoria?mesAno=${mesAno}`);
};

export function useConsolidacaoCategoria(mesAno: string | null) {
  const { isAuthenticated } = useAuthStore();
  
  return useQuery({
    queryKey: ['consolidacao', 'categoria', mesAno],
    queryFn: () => fetchConsolidacaoPorCategoria(mesAno!),
    enabled: !!mesAno && isAuthenticated,
  });
}
