import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { apiClient } from '../services/apiClient';

export interface Conta {
  id: number;
  descricao: string;
  observacoes: string;
}

const API_URL = '/api/contas';

// Fetchers
const fetchContas = async (): Promise<Conta[]> => {
  return apiClient.get<Conta[]>(API_URL);
};

const createConta = async (conta: Omit<Conta, 'id'>): Promise<Conta> => {
  return apiClient.post<Conta>(API_URL, conta);
};

const updateConta = async (conta: Conta): Promise<Conta> => {
  return apiClient.put<Conta>(`${API_URL}/${conta.id}`, conta);
};

const deleteConta = async (id: number): Promise<void> => {
  await apiClient.delete(`${API_URL}/${id}`);
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
