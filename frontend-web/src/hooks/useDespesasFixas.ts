import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { apiClient } from '../services/apiClient';
import type { Conta } from './useContas';
import type { Categoria } from './useCategorias';

export interface DespesaFixa {
  id: number;
  descricao: string;
  valor: number;
  conta: Conta;
  categoria: Categoria;
  observacoes: string;
}

export interface DespesaFixaInput {
  id?: number;
  descricao: string;
  valor: number;
  conta: { id: number };
  categoria: { id: number };
  observacoes: string;
}

const API_URL = '/api/despesas-fixas';

const fetchDespesasFixas = async (): Promise<DespesaFixa[]> => {
  return apiClient.get<DespesaFixa[]>(API_URL);
};

const createDespesaFixa = async (despesa: DespesaFixaInput): Promise<DespesaFixa> => {
  return apiClient.post<DespesaFixa>(API_URL, despesa);
};

const updateDespesaFixa = async (despesa: DespesaFixaInput): Promise<DespesaFixa> => {
  if (!despesa.id) throw new Error('ID da despesa fixa é obrigatório para atualização');
  return apiClient.put<DespesaFixa>(`${API_URL}/${despesa.id}`, despesa);
};

const deleteDespesaFixa = async (id: number): Promise<void> => {
  await apiClient.delete(`${API_URL}/${id}`);
};

export const useDespesasFixas = () => {
  return useQuery({
    queryKey: ['despesas-fixas'],
    queryFn: fetchDespesasFixas,
  });
};

export const useCreateDespesaFixa = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createDespesaFixa,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['despesas-fixas'] });
      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
      toast.success('Despesa fixa criada com sucesso!');
    },
    onError: (error: Error) => {
      toast.error(error.message);
    }
  });
};

export const useUpdateDespesaFixa = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateDespesaFixa,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['despesas-fixas'] });
      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
      toast.success('Despesa fixa atualizada com sucesso!');
    },
    onError: (error: Error) => {
      toast.error(error.message);
    }
  });
};

export const useDeleteDespesaFixa = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteDespesaFixa,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['despesas-fixas'] });
      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
      toast.success('Despesa fixa excluída com sucesso!');
    },
    onError: (error: Error) => {
      toast.error(error.message);
    }
  });
};
