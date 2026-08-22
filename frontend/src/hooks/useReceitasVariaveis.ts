import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
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

const getCsrfToken = () => {
  const match = document.cookie.match(new RegExp('(^| )XSRF-TOKEN=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : '';
};

const fetchReceitasVariaveis = async (): Promise<ReceitaVariavel[]> => {
  const response = await fetch(API_URL);
  if (!response.ok) throw new Error('Erro ao carregar receitas variáveis');
  return response.json();
};

const createReceitaVariavel = async (receita: ReceitaVariavelInput): Promise<ReceitaVariavel> => {
  const response = await fetch(API_URL, {
    method: 'POST',
    headers: { 
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': getCsrfToken()
    },
    body: JSON.stringify(receita),
  });
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    if (errorData.errors && errorData.errors.length > 0) {
      throw new Error(errorData.errors[0].defaultMessage);
    }
    throw new Error(errorData.message || 'Erro ao criar receita variável');
  }
  return response.json();
};

const updateReceitaVariavel = async (receita: ReceitaVariavelInput): Promise<ReceitaVariavel> => {
  if (!receita.id) throw new Error('ID da receita variável é obrigatório para atualização');
  const response = await fetch(`${API_URL}/${receita.id}`, {
    method: 'PUT',
    headers: { 
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': getCsrfToken()
    },
    body: JSON.stringify(receita),
  });
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    if (errorData.errors && errorData.errors.length > 0) {
      throw new Error(errorData.errors[0].defaultMessage);
    }
    throw new Error(errorData.message || 'Erro ao atualizar receita variável');
  }
  return response.json();
};

const deleteReceitaVariavel = async (id: number): Promise<void> => {
  const response = await fetch(`${API_URL}/${id}`, {
    method: 'DELETE',
    headers: {
      'X-XSRF-TOKEN': getCsrfToken()
    }
  });
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    if (errorData.message) {
      throw new Error(errorData.message);
    }
    throw new Error('Erro ao excluir receita variável');
  }
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
