import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';

export interface Categoria {
  id: number;
  descricao: string;
  observacoes: string;
}

const API_URL = '/api/categorias';

const getCsrfToken = () => {
  const match = document.cookie.match(new RegExp('(^| )XSRF-TOKEN=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : '';
};

// Fetchers
const fetchCategorias = async (): Promise<Categoria[]> => {
  const response = await fetch(API_URL);
  if (!response.ok) throw new Error('Erro ao carregar categorias');
  return response.json();
};

const createCategoria = async (categoria: Omit<Categoria, 'id'>): Promise<Categoria> => {
  const response = await fetch(API_URL, {
    method: 'POST',
    headers: { 
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': getCsrfToken()
    },
    body: JSON.stringify(categoria),
  });
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    if (errorData.errors && errorData.errors.length > 0) {
      throw new Error(errorData.errors[0].defaultMessage);
    }
    throw new Error(errorData.message || 'Erro ao criar categoria');
  }
  return response.json();
};

const updateCategoria = async (categoria: Categoria): Promise<Categoria> => {
  const response = await fetch(`${API_URL}/${categoria.id}`, {
    method: 'PUT',
    headers: { 
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': getCsrfToken()
    },
    body: JSON.stringify(categoria),
  });
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    if (errorData.errors && errorData.errors.length > 0) {
      throw new Error(errorData.errors[0].defaultMessage);
    }
    throw new Error(errorData.message || 'Erro ao atualizar categoria');
  }
  return response.json();
};

const deleteCategoria = async (id: number): Promise<void> => {
  const response = await fetch(`${API_URL}/${id}`, {
    method: 'DELETE',
    headers: {
      'X-XSRF-TOKEN': getCsrfToken()
    }
  });
  if (!response.ok) {
    if (response.status === 400) {
       throw new Error('Não é possível excluir esta categoria pois ela possui vínculos ativos.');
    }
    throw new Error('Erro ao excluir categoria');
  }
};

// Hooks
export const useCategorias = () => {
  return useQuery({
    queryKey: ['categorias'],
    queryFn: fetchCategorias,
  });
};

export const useCreateCategoria = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createCategoria,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categorias'] });
      toast.success('Categoria criada com sucesso!');
    },
    onError: (error) => {
      toast.error(error.message);
    }
  });
};

export const useUpdateCategoria = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateCategoria,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categorias'] });
      toast.success('Categoria atualizada com sucesso!');
    },
    onError: (error) => {
      toast.error(error.message);
    }
  });
};

export const useDeleteCategoria = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteCategoria,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categorias'] });
      toast.success('Categoria excluída com sucesso!');
    },
    onError: (error) => {
      toast.error(error.message);
    }
  });
};
