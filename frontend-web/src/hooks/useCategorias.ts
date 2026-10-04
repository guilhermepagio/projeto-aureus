import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { apiClient } from '../services/apiClient';

export interface Categoria {
  id: number;
  descricao: string;
  observacoes: string;
}

const API_URL = '/api/categorias';

// Fetchers
const fetchCategorias = async (): Promise<Categoria[]> => {
  return apiClient.get<Categoria[]>(API_URL);
};

const createCategoria = async (categoria: Omit<Categoria, 'id'>): Promise<Categoria> => {
  return apiClient.post<Categoria>(API_URL, categoria);
};

const updateCategoria = async (categoria: Categoria): Promise<Categoria> => {
  return apiClient.put<Categoria>(`${API_URL}/${categoria.id}`, categoria);
};

const deleteCategoria = async (id: number): Promise<void> => {
  await apiClient.delete(`${API_URL}/${id}`);
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
      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
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
      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
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
      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
      toast.success('Categoria excluída com sucesso!');
    },
    onError: (error) => {
      toast.error(error.message);
    }
  });
};
