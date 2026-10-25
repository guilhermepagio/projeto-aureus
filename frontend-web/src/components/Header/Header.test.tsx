import React from 'react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor, cleanup } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Header from './Header';
import { useAuthStore } from '../../store/authStore';
import { apiClient } from '../../services/apiClient';
import toast from 'react-hot-toast';

const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Header component', () => {
  let queryClient: QueryClient;

  beforeEach(async () => {
    vi.restoreAllMocks();
    mockNavigate.mockReset();
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    });

    useAuthStore.setState({
      isAuthenticated: true,
      isLoading: false,
      subjectId: 'user-teste-123',
      profileImage: 'https://example.com/avatar.png',
    });

    queryClient.setQueryData(['dados-privados-contas'], [{ id: 1, saldo: 5000 }]);
    queryClient.setQueryData(['dados-privados-transacoes'], [{ id: 42, valor: 350 }]);
    const privateMutation = queryClient.getMutationCache().build(queryClient, {
      mutationFn: async (variables: { descricao: string }) => ({ id: 42, ...variables }),
    });
    await privateMutation.execute({ descricao: 'Transação privada' });
    expect(queryClient.getQueryCache().getAll()).toHaveLength(2);
    expect(queryClient.getMutationCache().getAll()).toHaveLength(1);
    expect(privateMutation.state.variables).toEqual({ descricao: 'Transação privada' });
    expect(privateMutation.state.data).toEqual({ id: 42, descricao: 'Transação privada' });
  });

  afterEach(() => {
    cleanup();
    queryClient.clear();
  });

  it('deve realizar logout com sucesso, limpando cache do queryClient, authStore e redirecionando para /login', async () => {
    vi.spyOn(apiClient, 'post').mockResolvedValueOnce(undefined as any);
    const toastErrorSpy = vi.spyOn(toast, 'error');

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <Header />
        </MemoryRouter>
      </QueryClientProvider>
    );

    const userMenuButton = screen.getByRole('button', { name: /menu do usuário/i });
    fireEvent.click(userMenuButton);

    const logoutButton = screen.getByRole('menuitem', { name: /sair/i });
    fireEvent.click(logoutButton);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/login');
    });

    expect(apiClient.post).toHaveBeenCalledWith('/api/auth/logout', undefined, { skipUnauthorizedHandler: true });
    expect(queryClient.getQueryCache().getAll()).toHaveLength(0);
    expect(queryClient.getMutationCache().getAll()).toHaveLength(0);
    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(useAuthStore.getState().subjectId).toBeNull();
    expect(useAuthStore.getState().profileImage).toBeNull();
    expect(mockNavigate).toHaveBeenCalledWith('/login');
    expect(toastErrorSpy).not.toHaveBeenCalled();
  });

  it('deve realizar limpeza local completa e exibir toast informativo ao ocorrer falha de rede no logout', async () => {
    vi.spyOn(apiClient, 'post').mockRejectedValueOnce(new Error('Falha de rede'));
    const toastErrorSpy = vi.spyOn(toast, 'error');

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <Header />
        </MemoryRouter>
      </QueryClientProvider>
    );

    const userMenuButton = screen.getByRole('button', { name: /menu do usuário/i });
    fireEvent.click(userMenuButton);

    const logoutButton = screen.getByRole('menuitem', { name: /sair/i });
    fireEvent.click(logoutButton);

    await waitFor(() => {
      expect(toastErrorSpy).toHaveBeenCalledWith('Sessão encerrada localmente. (Erro de rede)');
    });

    expect(queryClient.getQueryCache().getAll()).toHaveLength(0);
    expect(queryClient.getMutationCache().getAll()).toHaveLength(0);
    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(useAuthStore.getState().subjectId).toBeNull();
    expect(useAuthStore.getState().profileImage).toBeNull();
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  it('deve prevenir múltiplos disparos concorrentes de logout ao clicar repetidamente', async () => {
    let resolveLogoutPromise: (val: any) => void = () => {};
    const pendingPromise = new Promise((resolve) => {
      resolveLogoutPromise = resolve;
    });

    vi.spyOn(apiClient, 'post').mockReturnValueOnce(pendingPromise as any);

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <Header />
        </MemoryRouter>
      </QueryClientProvider>
    );

    const userMenuButton = screen.getByRole('button', { name: /menu do usuário/i });
    fireEvent.click(userMenuButton);

    const logoutButton = screen.getByRole('menuitem', { name: /sair/i });
    fireEvent.click(logoutButton);

    expect((logoutButton as HTMLButtonElement).disabled).toBe(true);
    expect(screen.getByText('Saindo...')).toBeDefined();

    fireEvent.click(logoutButton);
    expect(apiClient.post).toHaveBeenCalledTimes(1);

    resolveLogoutPromise(undefined);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/login');
    });

    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(queryClient.getQueryCache().getAll()).toHaveLength(0);
    expect(queryClient.getMutationCache().getAll()).toHaveLength(0);
  });
});
