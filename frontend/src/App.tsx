import { Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import Header from './components/Header/Header';
import Navigation from './components/Navigation/Navigation';
import Login from './components/Login/Login';
import { useAuthStore } from './store/authStore';
import { useEffect } from 'react';
import ContasPage from './pages/Contas/ContasPage';
import CategoriasPage from './pages/Categorias/CategoriasPage';
import RequiresDependencies from './components/RequiresDependencies';

import DespesasFixasPage from './pages/DespesasFixas/DespesasFixasPage';
import ReceitasFixasPage from './pages/ReceitasFixas/ReceitasFixasPage';

import DespesasVariaveisPage from './pages/DespesasVariaveis/DespesasVariaveisPage';
import ReceitasVariaveisPage from './pages/ReceitasVariaveis/ReceitasVariaveisPage';

// Placeholders for routes
const Consolidacao = () => (
  <div className="px-4 pb-4 w-full">
    <div className="mb-4 mt-2 pl-2 border-l-4 border-blue-600">
      <h1 className="text-2xl font-bold text-gray-800">Consolidação</h1>
    </div>
    <div style={{ padding: '24px' }}><p>Conteúdo da Consolidação</p></div>
  </div>
);

const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated, isLoading } = useAuthStore();
  
  if (isLoading) return <div>Carregando...</div>;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  
  return (
    <>
      
      <Header>
        <Navigation />
      </Header>
      <main className="pb-[80px] md:pb-0">
        {children}
      </main>
    </>
  );
};

function App() {
  const { setAuth, setLoading } = useAuthStore();

  useEffect(() => {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 5000);

    fetch('/api/auth/me', { signal: controller.signal })
      .then(res => {
        if (res.ok) {
          return res.json();
        }
        throw new Error('Não autorizado');
      })
      .then(data => {
        setAuth(true, data.subjectId, data.fotoPerfil);
      })
      .catch((_err) => {
        setAuth(false, null, null);
      })
      .finally(() => {
        clearTimeout(timeoutId);
        setLoading(false);
      });

    return () => {
      clearTimeout(timeoutId);
      controller.abort();
    };
  }, [setAuth, setLoading]);

  return (
    <>
      <Toaster position="top-right" />
      <Routes>
        <Route path="/login" element={<Login />} />
        
        <Route path="/" element={<ProtectedRoute><Consolidacao /></ProtectedRoute>} />
        <Route path="/despesas-variaveis" element={<ProtectedRoute><RequiresDependencies><DespesasVariaveisPage /></RequiresDependencies></ProtectedRoute>} />
        <Route path="/despesas-fixas" element={<ProtectedRoute><RequiresDependencies><DespesasFixasPage /></RequiresDependencies></ProtectedRoute>} />
        <Route path="/receitas-variaveis" element={<ProtectedRoute><RequiresDependencies><ReceitasVariaveisPage /></RequiresDependencies></ProtectedRoute>} />
        <Route path="/receitas-fixas" element={<ProtectedRoute><RequiresDependencies><ReceitasFixasPage /></RequiresDependencies></ProtectedRoute>} />
        <Route path="/contas" element={<ProtectedRoute><ContasPage /></ProtectedRoute>} />
        <Route path="/categorias" element={<ProtectedRoute><CategoriasPage /></ProtectedRoute>} />
        
        <Route path="*" element={<ProtectedRoute><div style={{ padding: '24px' }}><h2>404 - Página não encontrada</h2></div></ProtectedRoute>} />
      </Routes>
    </>
  );
}

export default App;
