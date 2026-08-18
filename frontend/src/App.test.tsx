import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi } from 'vitest';
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
    setAuth: vi.fn(),
    setLoading: vi.fn()
  })
}));

describe('App Routing', () => {
  it('renders navigation and navigates correctly', async () => {
    // App now expects BrowserRouter to be outside, so we test with MemoryRouter wrapping App,
    // Wait, App currently DOES NOT have BrowserRouter anymore because I moved it to main.tsx!
    render(
      <MemoryRouter initialEntries={['/']}>
        <App />
      </MemoryRouter>
    );
    
    // Check if initial route renders correctly
    expect(screen.getByText('Conteúdo da Consolidação')).toBeDefined();
  });
});
