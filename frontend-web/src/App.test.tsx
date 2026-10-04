import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import App from './App';

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

// We need to mock useAuthStore since App uses it
vi.mock('./store/authStore', () => ({
  useAuthStore: () => ({
    isAuthenticated: true,
    isLoading: false,
    profileImage: null,
    setAuth: vi.fn(),
    setLoading: vi.fn(),
    logout: vi.fn()
  })
}));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
    },
  },
});

import { beforeEach } from 'vitest';

vi.mock('./pages/Categorias/CategoriasPage', () => ({
  default: () => {
    throw new Error('Falha simulada na página de categorias');
  },
}));

beforeEach(() => {
  globalThis.fetch = vi.fn().mockResolvedValue({
    ok: true,
    status: 200,
    headers: new Headers({ 'content-type': 'application/json' }),
    text: vi.fn().mockResolvedValue('[]'),
  } as unknown as Response);
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
    expect(screen.getByText('Consolidação')).toBeDefined();
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
});
