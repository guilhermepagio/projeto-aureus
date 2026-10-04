import { create } from 'zustand';

interface AuthState {
  isAuthenticated: boolean;
  isLoading: boolean;
  subjectId: string | null;
  profileImage: string | null;
  setAuth: (isAuthenticated: boolean, subjectId: string | null, profileImage?: string | null) => void;
  setLoading: (isLoading: boolean) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  isAuthenticated: false,
  isLoading: true,
  subjectId: null,
  profileImage: null,
  setAuth: (isAuthenticated, subjectId, profileImage = null) => set({ isAuthenticated, subjectId, profileImage, isLoading: false }),
  setLoading: (isLoading) => set({ isLoading }),
  logout: () => set({ isAuthenticated: false, subjectId: null, profileImage: null, isLoading: false })
}));
