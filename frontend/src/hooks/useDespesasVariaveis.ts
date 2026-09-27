import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { apiClient } from '../services/apiClient';
import type { Conta } from './useContas';
import type { Categoria } from './useCategorias';

export interface DespesaVariavel {
  id: number;
  descricao: string;
  localCompra?: string;
  dataCompra?: string;
  valorParcela: number;
  quantidadeParcelas: number;
  dataInicio: string;
  dataFim: string;
  conta: Conta;
  categoria: Categoria;
  observacoes?: string;
}

export interface DespesaVariavelInput {
  id?: number;
  descricao: string;
  localCompra?: string;
  dataCompra?: string;
  valorParcela: number;
  quantidadeParcelas: number;
  dataInicio: string;
  conta: { id: number };
  categoria: { id: number };
  observacoes?: string;
}

const API_URL = '/api/despesas-variaveis';

const fetchDespesasVariaveis = async (): Promise<DespesaVariavel[]> => {
  return apiClient.get<DespesaVariavel[]>(API_URL);
};

const createDespesaVariavel = async (despesa: DespesaVariavelInput): Promise<DespesaVariavel> => {
  return apiClient.post<DespesaVariavel>(API_URL, despesa);
};

const updateDespesaVariavel = async (despesa: DespesaVariavelInput): Promise<DespesaVariavel> => {
  if (!despesa.id) throw new Error('ID da despesa variável é obrigatório para atualização');
  return apiClient.put<DespesaVariavel>(`${API_URL}/${despesa.id}`, despesa);
};

const deleteDespesaVariavel = async (id: number): Promise<void> => {
  await apiClient.delete(`${API_URL}/${id}`);
};

export const useDespesasVariaveis = () => {
  return useQuery({
    queryKey: ['despesas-variaveis'],
    queryFn: fetchDespesasVariaveis,
  });
};

export const useCreateDespesaVariavel = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createDespesaVariavel,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['despesas-variaveis'] });
      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
      toast.success('Despesa variável criada com sucesso!');
    },
    onError: (error: Error) => {
      toast.error(error.message);
    }
  });
};

export const useUpdateDespesaVariavel = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateDespesaVariavel,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['despesas-variaveis'] });
      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
      toast.success('Despesa variável atualizada com sucesso!');
    },
    onError: (error: Error) => {
      toast.error(error.message);
    }
  });
};

export const useDeleteDespesaVariavel = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteDespesaVariavel,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['despesas-variaveis'] });
      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
      toast.success('Despesa variável excluída com sucesso!');
    },
    onError: (error: Error) => {
      toast.error(error.message);
    }
  });
};
