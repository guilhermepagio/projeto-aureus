import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';

export interface Conta {
  id: number;
  descricao: string;
  observacoes: string;
}

const API_URL = '/api/contas';

const getCsrfToken = () => {
  const match = document.cookie.match(new RegExp('(^| )XSRF-TOKEN=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : '';
};

// Fetchers
const fetchContas = async (): Promise<Conta[]> => {
  const response = await fetch(API_URL);
  if (!response.ok) throw new Error('Erro ao carregar contas');
  return response.json();
};

const createConta = async (conta: Omit<Conta, 'id'>): Promise<Conta> => {
  const response = await fetch(API_URL, {
    method: 'POST',
    headers: { 
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': getCsrfToken()
    },
    body: JSON.stringify(conta),
  });
  if (!response.ok) throw new Error('Erro ao criar conta');
  return response.json();
};

const updateConta = async (conta: Conta): Promise<Conta> => {
  const response = await fetch(`${API_URL}/${conta.id}`, {
    method: 'PUT',
    headers: { 
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': getCsrfToken()
    },
    body: JSON.stringify(conta),
  });
  if (!response.ok) throw new Error('Erro ao atualizar conta');
  return response.json();
};

const deleteConta = async (id: number): Promise<void> => {
  const response = await fetch(`${API_URL}/${id}`, {
    method: 'DELETE',
    headers: {
      'X-XSRF-TOKEN': getCsrfToken()
    }
  });
  if (!response.ok) {
    if (response.status === 400) {
       throw new Error('Não é possível excluir esta conta pois ela possui vínculos ativos.');
    }
    throw new Error('Erro ao excluir conta');
  }
};

// Hooks
export const useContas = () => {
  return useQuery({
    queryKey: ['contas'],
    queryFn: fetchContas,
  });
};

export const useCreateConta = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createConta,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['contas'] });
      toast.success('Conta criada com sucesso!');
    },
    onError: (error) => {
      toast.error(error.message);
    }
  });
};

export const useUpdateConta = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateConta,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['contas'] });
      toast.success('Conta atualizada com sucesso!');
    },
    onError: (error) => {
      toast.error(error.message);
    }
  });
};

export const useDeleteConta = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteConta,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['contas'] });
      toast.success('Conta excluída com sucesso!');
    },
    onError: (error) => {
      toast.error(error.message);
    }
  });
};
