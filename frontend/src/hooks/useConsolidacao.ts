import { useQuery } from '@tanstack/react-query';
import { useAuthStore } from '../store/authStore';
import { apiClient } from '../services/apiClient';

export interface LinhaConsolidacaoDTO {
  contaId: number;
  contaDescricao: string;
  valoresMensais: number[];
}

export interface ConsolidacaoPorContaDTO {
  receitas: LinhaConsolidacaoDTO[];
  despesas: LinhaConsolidacaoDTO[];
  saldoHistoricoPreGrade?: number;
}

export const fetchConsolidacao = async (mesAno: string): Promise<ConsolidacaoPorContaDTO> => {
  return apiClient.get<ConsolidacaoPorContaDTO>(`/api/consolidacao/por-conta?mesAno=${mesAno}`);
};

export const useConsolidacao = (mesAno: string | null) => {
  const { isAuthenticated } = useAuthStore();
  
  return useQuery({
    queryKey: ['consolidacao', mesAno],
    queryFn: () => fetchConsolidacao(mesAno as string),
    enabled: !!mesAno && isAuthenticated,
  });
};
