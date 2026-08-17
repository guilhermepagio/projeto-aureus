import { create } from 'zustand';

interface AuthState {
  isAuthenticated: boolean;
  isLoading: boolean;
  subjectId: string | null;
  setAuth: (isAuthenticated: boolean, subjectId: string | null) => void;
  setLoading: (isLoading: boolean) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  isAuthenticated: false,
  isLoading: true,
  subjectId: null,
  setAuth: (isAuthenticated, subjectId) => set({ isAuthenticated, subjectId, isLoading: false }),
  setLoading: (isLoading) => set({ isLoading }),
  logout: () => set({ isAuthenticated: false, subjectId: null, isLoading: false })
}));
