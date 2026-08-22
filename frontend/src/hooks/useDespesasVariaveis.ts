import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
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

const getCsrfToken = () => {
  const match = document.cookie.match(new RegExp('(^| )XSRF-TOKEN=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : '';
};

const fetchDespesasVariaveis = async (): Promise<DespesaVariavel[]> => {
  const response = await fetch(API_URL);
  if (!response.ok) throw new Error('Erro ao carregar despesas variáveis');
  return response.json();
};

const createDespesaVariavel = async (despesa: DespesaVariavelInput): Promise<DespesaVariavel> => {
  const response = await fetch(API_URL, {
    method: 'POST',
    headers: { 
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': getCsrfToken()
    },
    body: JSON.stringify(despesa),
  });
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    if (errorData.errors && errorData.errors.length > 0) {
      throw new Error(errorData.errors[0].defaultMessage);
    }
    throw new Error(errorData.message || 'Erro ao criar despesa variável');
  }
  return response.json();
};

const updateDespesaVariavel = async (despesa: DespesaVariavelInput): Promise<DespesaVariavel> => {
  if (!despesa.id) throw new Error('ID da despesa variável é obrigatório para atualização');
  const response = await fetch(`${API_URL}/${despesa.id}`, {
    method: 'PUT',
    headers: { 
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': getCsrfToken()
    },
    body: JSON.stringify(despesa),
  });
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    if (errorData.errors && errorData.errors.length > 0) {
      throw new Error(errorData.errors[0].defaultMessage);
    }
    throw new Error(errorData.message || 'Erro ao atualizar despesa variável');
  }
  return response.json();
};

const deleteDespesaVariavel = async (id: number): Promise<void> => {
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
    throw new Error('Erro ao excluir despesa variável');
  }
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
