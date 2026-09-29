import { Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import Header from './components/Header/Header';
import Navigation from './components/Navigation/Navigation';
import Login from './components/Login/Login';
import { useAuthStore } from './store/authStore';
import { useEffect } from 'react';
import ContasPage from './pages/Contas/ContasPage';
import CategoriasPage from './pages/Categorias/CategoriasPage';
import RequiresDependencies from './components/RequiresDependencies';
import ErrorBoundary from './components/ErrorBoundary/ErrorBoundary';
import { apiClient } from './services/apiClient';

import DespesasFixasPage from './pages/DespesasFixas/DespesasFixasPage';
import ReceitasFixasPage from './pages/ReceitasFixas/ReceitasFixasPage';

import DespesasVariaveisPage from './pages/DespesasVariaveis/DespesasVariaveisPage';
import ReceitasVariaveisPage from './pages/ReceitasVariaveis/ReceitasVariaveisPage';

import ConsolidacaoPage from './pages/Consolidacao/ConsolidacaoPage';

const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated, isLoading } = useAuthStore();
  
  if (isLoading) return <div>Carregando...</div>;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  
  return (
    <div className="flex flex-col h-[100dvh] overflow-hidden">
      <Header>
        <Navigation />
      </Header>
      <main className="flex-1 overflow-hidden pb-[80px] md:pb-0 flex flex-col">
        {children}
      </main>
    </div>
  );
};

function App() {
  const { setAuth, setLoading } = useAuthStore();
  const location = useLocation();

  useEffect(() => {
    const controller = new AbortController();

    apiClient
      .get<{ subjectId: string; fotoPerfil?: string }>('/api/auth/me', {
        signal: controller.signal,
        timeout: 5000,
      })
      .then((data) => {
        setAuth(true, data.subjectId, data.fotoPerfil);
      })
      .catch((err: any) => {
        if (err?.name === 'AbortError' || err?.message === 'AbortError') return;
        setAuth(false, null, null);
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      });

    return () => {
      controller.abort();
    };
  }, [setAuth, setLoading]);

  return (
    <>
      <Toaster position="top-right" />
      <ErrorBoundary resetKeys={[location.pathname]}>
        <Routes>
          <Route path="/login" element={<Login />} />
          
          <Route path="/" element={<ProtectedRoute><RequiresDependencies><ConsolidacaoPage /></RequiresDependencies></ProtectedRoute>} />
          <Route path="/despesas-variaveis" element={<ProtectedRoute><RequiresDependencies><DespesasVariaveisPage /></RequiresDependencies></ProtectedRoute>} />
          <Route path="/despesas-fixas" element={<ProtectedRoute><RequiresDependencies><DespesasFixasPage /></RequiresDependencies></ProtectedRoute>} />
          <Route path="/receitas-variaveis" element={<ProtectedRoute><RequiresDependencies><ReceitasVariaveisPage /></RequiresDependencies></ProtectedRoute>} />
          <Route path="/receitas-fixas" element={<ProtectedRoute><RequiresDependencies><ReceitasFixasPage /></RequiresDependencies></ProtectedRoute>} />
          <Route path="/contas" element={<ProtectedRoute><ContasPage /></ProtectedRoute>} />
          <Route path="/categorias" element={<ProtectedRoute><CategoriasPage /></ProtectedRoute>} />
          
          <Route path="*" element={<ProtectedRoute><div style={{ padding: '24px' }}><h2>404 - Página não encontrada</h2></div></ProtectedRoute>} />
        </Routes>
      </ErrorBoundary>
    </>
  );
}

export default App;

