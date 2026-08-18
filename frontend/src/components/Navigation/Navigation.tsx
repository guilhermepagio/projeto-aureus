import { NavLink, useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '../../store/authStore';
import toast from 'react-hot-toast';
import './Navigation.css';

const Navigation: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { logout } = useAuthStore();

  const handleLogout = async () => {
    const csrfToken = document.cookie.split('; ').find(row => row.startsWith('XSRF-TOKEN='))?.split('=')[1];
    
    try {
      const response = await fetch('/api/auth/logout', { 
        method: 'POST',
        headers: csrfToken ? { 'X-XSRF-TOKEN': csrfToken } : undefined
      });
      if (!response.ok) throw new Error('Logout failed');
    } catch (error) {
      toast.error('Sessão encerrada localmente. (Erro de rede)');
    } finally {
      queryClient.clear();
      logout();
      navigate('/login');
    }
  };
  return (
    <nav className="navigation" aria-label="Main Navigation">
      <ul className="nav-list">
        <li className="nav-item">
          <NavLink to="/" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
            Consolidação
          </NavLink>
        </li>
        <li className="nav-item">
          <NavLink to="/despesas-variaveis" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
            Despesas Variáveis
          </NavLink>
        </li>
        <li className="nav-item">
          <NavLink to="/despesas-fixas" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
            Despesas Fixas
          </NavLink>
        </li>
        <li className="nav-item">
          <NavLink to="/receitas-variaveis" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
            Receitas Variáveis
          </NavLink>
        </li>
        <li className="nav-item">
          <NavLink to="/receitas-fixas" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
            Receitas Fixas
          </NavLink>
        </li>
        <li className="nav-item">
          <button onClick={handleLogout} className="nav-link logout-button">
            Sair
          </button>
        </li>
      </ul>
    </nav>
  );
};

export default Navigation;
