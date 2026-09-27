import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { apiClient } from '../services/apiClient';
import type { Conta } from './useContas';
import type { Categoria } from './useCategorias';

export interface ReceitaVariavel {
  id: number;
  descricao: string;
  valorParcela: number;
  quantidadeParcelas: number;
  dataInicio: string;
  dataFim: string;
  conta: Conta;
  categoria: Categoria;
  observacoes?: string;
}

export interface ReceitaVariavelInput {
  id?: number;
  descricao: string;
  valorParcela: number;
  quantidadeParcelas: number;
  dataInicio: string;
  conta: { id: number };
  categoria: { id: number };
  observacoes?: string;
}

const API_URL = '/api/receitas-variaveis';

const fetchReceitasVariaveis = async (): Promise<ReceitaVariavel[]> => {
  return apiClient.get<ReceitaVariavel[]>(API_URL);
};

const createReceitaVariavel = async (receita: ReceitaVariavelInput): Promise<ReceitaVariavel> => {
  return apiClient.post<ReceitaVariavel>(API_URL, receita);
};

const updateReceitaVariavel = async (receita: ReceitaVariavelInput): Promise<ReceitaVariavel> => {
  if (!receita.id) throw new Error('ID da receita variável é obrigatório para atualização');
  return apiClient.put<ReceitaVariavel>(`${API_URL}/${receita.id}`, receita);
};

const deleteReceitaVariavel = async (id: number): Promise<void> => {
  await apiClient.delete(`${API_URL}/${id}`);
};

export const useReceitasVariaveis = () => {
  return useQuery({
    queryKey: ['receitas-variaveis'],
    queryFn: fetchReceitasVariaveis,
  });
};

export const useCreateReceitaVariavel = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createReceitaVariavel,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['receitas-variaveis'] });
      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
      toast.success('Receita variável criada com sucesso!');
    },
    onError: (error: Error) => {
      toast.error(error.message);
    }
  });
};

export const useUpdateReceitaVariavel = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateReceitaVariavel,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['receitas-variaveis'] });
      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
      toast.success('Receita variável atualizada com sucesso!');
    },
    onError: (error: Error) => {
      toast.error(error.message);
    }
  });
};

export const useDeleteReceitaVariavel = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteReceitaVariavel,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['receitas-variaveis'] });
      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
      toast.success('Receita variável excluída com sucesso!');
    },
    onError: (error: Error) => {
      toast.error(error.message);
    }
  });
};
