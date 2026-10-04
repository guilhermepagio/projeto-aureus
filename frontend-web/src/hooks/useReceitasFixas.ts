import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { apiClient } from '../services/apiClient';
import type { Conta } from './useContas';
import type { Categoria } from './useCategorias';

export interface ReceitaFixa {
  id: number;
  descricao: string;
  valor: number;
  conta: Conta;
  categoria: Categoria;
  observacoes: string;
}

export interface ReceitaFixaInput {
  id?: number;
  descricao: string;
  valor: number;
  conta: { id: number };
  categoria: { id: number };
  observacoes: string;
}

const API_URL = '/api/receitas-fixas';

const fetchReceitasFixas = async (): Promise<ReceitaFixa[]> => {
  return apiClient.get<ReceitaFixa[]>(API_URL);
};

const createReceitaFixa = async (receita: ReceitaFixaInput): Promise<ReceitaFixa> => {
  return apiClient.post<ReceitaFixa>(API_URL, receita);
};

const updateReceitaFixa = async (receita: ReceitaFixaInput): Promise<ReceitaFixa> => {
  if (!receita.id) throw new Error('ID da receita fixa é obrigatório para atualização');
  return apiClient.put<ReceitaFixa>(`${API_URL}/${receita.id}`, receita);
};

const deleteReceitaFixa = async (id: number): Promise<void> => {
  await apiClient.delete(`${API_URL}/${id}`);
};

export const useReceitasFixas = () => {
  return useQuery({
    queryKey: ['receitas-fixas'],
    queryFn: fetchReceitasFixas,
  });
};

export const useCreateReceitaFixa = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createReceitaFixa,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['receitas-fixas'] });
      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
      toast.success('Receita fixa criada com sucesso!');
    },
    onError: (error: Error) => {
      toast.error(error.message);
    }
  });
};

export const useUpdateReceitaFixa = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateReceitaFixa,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['receitas-fixas'] });
      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
      toast.success('Receita fixa atualizada com sucesso!');
    },
    onError: (error: Error) => {
      toast.error(error.message);
    }
  });
};

export const useDeleteReceitaFixa = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteReceitaFixa,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['receitas-fixas'] });
      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
      toast.success('Receita fixa excluída com sucesso!');
    },
    onError: (error: Error) => {
      toast.error(error.message);
    }
  });
};
