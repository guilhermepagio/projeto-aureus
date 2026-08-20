import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { Conta } from './useContas';
import { Categoria } from './useCategorias';

export interface ReceitaFixa {
  id: number;
  descricao: string;
  valor: number;
  dataInicio: string;
  conta: Conta;
  categoria: Categoria;
  observacoes: string;
}

export interface ReceitaFixaInput {
  id?: number;
  descricao: string;
  valor: number;
  dataInicio: string;
  conta: { id: number };
  categoria: { id: number };
  observacoes: string;
}

const API_URL = '/api/receitas-fixas';

const getCsrfToken = () => {
  const match = document.cookie.match(new RegExp('(^| )XSRF-TOKEN=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : '';
};

const fetchReceitasFixas = async (): Promise<ReceitaFixa[]> => {
  const response = await fetch(API_URL);
  if (!response.ok) throw new Error('Erro ao carregar receitas fixas');
  return response.json();
};

const createReceitaFixa = async (receita: ReceitaFixaInput): Promise<ReceitaFixa> => {
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
    throw new Error(errorData.message || 'Erro ao criar receita fixa');
  }
  return response.json();
};

const updateReceitaFixa = async (receita: ReceitaFixaInput): Promise<ReceitaFixa> => {
  if (!receita.id) throw new Error('ID da receita fixa é obrigatório para atualização');
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
    throw new Error(errorData.message || 'Erro ao atualizar receita fixa');
  }
  return response.json();
};

const deleteReceitaFixa = async (id: number): Promise<void> => {
  const response = await fetch(`${API_URL}/${id}`, {
    method: 'DELETE',
    headers: {
      'X-XSRF-TOKEN': getCsrfToken()
    }
  });
  if (!response.ok) {
    if (response.status === 400) {
       throw new Error('Não é possível excluir esta receita fixa.');
    }
    throw new Error('Erro ao excluir receita fixa');
  }
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
      toast.success('Receita fixa excluída com sucesso!');
    },
    onError: (error: Error) => {
      toast.error(error.message);
    }
  });
};
