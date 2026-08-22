import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
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

const getCsrfToken = () => {
  const match = document.cookie.match(new RegExp('(^| )XSRF-TOKEN=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : '';
};

const fetchDespesasFixas = async (): Promise<DespesaFixa[]> => {
  const response = await fetch(API_URL);
  if (!response.ok) throw new Error('Erro ao carregar despesas fixas');
  return response.json();
};

const createDespesaFixa = async (despesa: DespesaFixaInput): Promise<DespesaFixa> => {
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
    throw new Error(errorData.message || 'Erro ao criar despesa fixa');
  }
  return response.json();
};

const updateDespesaFixa = async (despesa: DespesaFixaInput): Promise<DespesaFixa> => {
  if (!despesa.id) throw new Error('ID da despesa fixa é obrigatório para atualização');
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
    throw new Error(errorData.message || 'Erro ao atualizar despesa fixa');
  }
  return response.json();
};

const deleteDespesaFixa = async (id: number): Promise<void> => {
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
    throw new Error('Erro ao excluir despesa fixa');
  }
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
