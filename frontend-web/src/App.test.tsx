import { render, screen, waitFor, cleanup } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import App from './App';
import { apiClient, setUnauthorizedHandler } from './services/apiClient';
import { useAuthStore } from './store/authStore';

// Mock matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
});

vi.mock('./store/authStore', async () => {
  const { create } = await import('zustand');
  const store = create<any>((set) => ({
    isAuthenticated: true,
    isLoading: false,
    profileImage: null,
    setAuth: vi.fn((isAuthenticated: boolean, subjectId: string | null = null, profileImage: string | null = null) =>
      set({ isAuthenticated, subjectId, profileImage, isLoading: false })
    ),
    setLoading: vi.fn((isLoading: boolean) => set({ isLoading })),
    logout: vi.fn(() =>
      set({ isAuthenticated: false, subjectId: null, profileImage: null, isLoading: false })
    ),
  }));
  return {
    useAuthStore: store,
  };
});

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
    },
  },
});

vi.mock('./pages/Categorias/CategoriasPage', () => ({
  default: () => {
    throw new Error('Falha simulada na página de categorias');
  },
}));

beforeEach(() => {
  setUnauthorizedHandler(null);
  (useAuthStore as any).setState({
    isAuthenticated: true,
    isLoading: false,
    profileImage: null,
  });
  (useAuthStore as any).getState().logout.mockClear();
  (useAuthStore as any).getState().setAuth.mockClear();
  (useAuthStore as any).getState().setLoading.mockClear();
  queryClient.clear();

  globalThis.fetch = vi.fn().mockResolvedValue({
    ok: true,
    status: 200,
    headers: new Headers({ 'content-type': 'application/json' }),
    text: vi.fn().mockResolvedValue('[]'),
  } as unknown as Response);
});

afterEach(() => {
  cleanup();
  setUnauthorizedHandler(null);
});

describe('App Routing', () => {
  it('renders navigation and navigates correctly', async () => {
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/']}>
          <App />
        </MemoryRouter>
      </QueryClientProvider>
    );
    
    // Check if initial route renders correctly
    expect(screen.getAllByText('Consolidação').length).toBeGreaterThan(0);
  });

  it('captura falhas de renderização em rotas com o ErrorBoundary global', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/categorias']}>
          <App />
        </MemoryRouter>
      </QueryClientProvider>
    );

    expect(screen.getByRole('alert')).toBeDefined();
    expect(screen.getByText('Ops! Algo deu errado')).toBeDefined();
    expect(screen.getByText(/Falha simulada na página de categorias/)).toBeDefined();

    consoleErrorSpy.mockRestore();
  });

  it('deve invalidar a sessão local, limpar cache do QueryClient e redirecionar para /login ao receber 401 durante o uso', async () => {
    queryClient.setQueryData(['dados-privados-saldo'], { total: 10000 });
    queryClient.setQueryData(['dados-privados-contas'], [{ id: 1, descricao: 'Conta privada' }]);
    const privateMutation = queryClient.getMutationCache().build(queryClient, {
      mutationFn: async (variables: { descricao: string }) => ({ id: 42, ...variables }),
    });
    await privateMutation.execute({ descricao: 'Transação privada' });
    expect(queryClient.getQueryCache().getAll()).toHaveLength(2);
    expect(queryClient.getMutationCache().getAll()).toHaveLength(1);
    expect(privateMutation.state.variables).toEqual({ descricao: 'Transação privada' });
    expect(privateMutation.state.data).toEqual({ id: 42, descricao: 'Transação privada' });

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/']}>
          <App />
        </MemoryRouter>
      </QueryClientProvider>
    );

    expect(screen.getAllByText('Consolidação').length).toBeGreaterThan(0);

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      statusText: 'Unauthorized',
      headers: new Headers({ 'content-type': 'application/json' }),
      text: vi.fn().mockResolvedValue(JSON.stringify({ message: 'Sessão expirada' })),
    } as unknown as Response);

    await expect(apiClient.get('/api/contas')).rejects.toThrow();

    expect((useAuthStore as any).getState().logout).toHaveBeenCalled();
    expect((useAuthStore as any).getState().isAuthenticated).toBe(false);

    await waitFor(() => {
      expect(screen.getByText('Acesse sua conta para continuar')).toBeDefined();
    });
    expect(queryClient.getQueryCache().getAll()).toHaveLength(0);
    expect(queryClient.getMutationCache().getAll()).toHaveLength(0);
  });

  it('deve inicializar desautenticado e redirecionar para /login quando /api/auth/me responder 401 na carga inicial', async () => {
    (useAuthStore as any).setState({
      isAuthenticated: false,
      isLoading: true,
    });

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      statusText: 'Unauthorized',
      headers: new Headers({ 'content-type': 'application/json' }),
      text: vi.fn().mockResolvedValue(JSON.stringify({ message: 'Não autenticado' })),
    } as unknown as Response);

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/']}>
          <App />
        </MemoryRouter>
      </QueryClientProvider>
    );

    await waitFor(() => {
      expect((useAuthStore as any).getState().setAuth).toHaveBeenCalledWith(false, null, null);
      expect((useAuthStore as any).getState().setLoading).toHaveBeenCalledWith(false);
      expect(screen.getByText('Acesse sua conta para continuar')).toBeDefined();
      expect(screen.getByText('Entrar com Google')).toBeDefined();
    });
  });
});
