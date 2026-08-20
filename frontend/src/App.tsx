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

// Placeholders for routes
const Consolidacao = () => <div style={{ padding: '24px' }}><h2>Consolidação</h2><p>Conteúdo da Consolidação</p></div>;
const DespesasVariaveis = () => <div style={{ padding: '24px' }}><h2>Despesas Variáveis</h2><p>Conteúdo de Despesas Variáveis</p></div>;
const ReceitasVariaveis = () => <div style={{ padding: '24px' }}><h2>Receitas Variáveis</h2><p>Conteúdo de Receitas Variáveis</p></div>;

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
        <Route path="/despesas-variaveis" element={<ProtectedRoute><RequiresDependencies><DespesasVariaveis /></RequiresDependencies></ProtectedRoute>} />
        <Route path="/despesas-fixas" element={<ProtectedRoute><RequiresDependencies><DespesasFixasPage /></RequiresDependencies></ProtectedRoute>} />
        <Route path="/receitas-variaveis" element={<ProtectedRoute><RequiresDependencies><ReceitasVariaveis /></RequiresDependencies></ProtectedRoute>} />
        <Route path="/receitas-fixas" element={<ProtectedRoute><RequiresDependencies><ReceitasFixasPage /></RequiresDependencies></ProtectedRoute>} />
        <Route path="/contas" element={<ProtectedRoute><ContasPage /></ProtectedRoute>} />
        <Route path="/categorias" element={<ProtectedRoute><CategoriasPage /></ProtectedRoute>} />
        
        <Route path="*" element={<ProtectedRoute><div style={{ padding: '24px' }}><h2>404 - Página não encontrada</h2></div></ProtectedRoute>} />
      </Routes>
    </>
  );
}

export default App;
