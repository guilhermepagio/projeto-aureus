import { useQuery } from '@tanstack/react-query';
import { useAuthStore } from '../store/authStore';

export interface LinhaConsolidacaoDTO {
  contaId: number;
  contaDescricao: string;
  valoresMensais: number[];
}

export interface ConsolidacaoPorContaDTO {
  receitas: LinhaConsolidacaoDTO[];
  despesas: LinhaConsolidacaoDTO[];
}

const getCsrfToken = () => {
  const match = document.cookie.match(new RegExp('(^| )XSRF-TOKEN=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : '';
};

export const fetchConsolidacao = async (mesAno: string): Promise<ConsolidacaoPorContaDTO> => {
  const response = await fetch(`/api/consolidacao/por-conta?mesAno=${mesAno}`, {
    headers: {
      'X-XSRF-TOKEN': getCsrfToken()
    }
  });
  if (!response.ok) throw new Error('Erro ao buscar consolidação');
  return response.json();
};

export const useConsolidacao = (mesAno: string | null) => {
  const { isAuthenticated } = useAuthStore();
  
  return useQuery({
    queryKey: ['consolidacao', mesAno],
    queryFn: () => fetchConsolidacao(mesAno as string),
    enabled: !!mesAno && isAuthenticated,
  });
};
