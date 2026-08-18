import { NavLink } from 'react-router-dom';
import { LayoutDashboard, TrendingUp, TrendingDown, Landmark, Wallet } from 'lucide-react';
import './Navigation.css';

const Navigation: React.FC = () => {
  return (
    <nav className="navigation" aria-label="Navegação Principal">
      <ul className="nav-list">
        <li className="nav-item">
          <NavLink to="/" end className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
            <LayoutDashboard size={20} className="nav-icon" />
            <span>Consolidação</span>
          </NavLink>
        </li>
        <li className="nav-item">
          <NavLink to="/despesas-variaveis" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
            <TrendingDown size={20} className="nav-icon" />
            <span>Despesas Variáveis</span>
          </NavLink>
        </li>
        <li className="nav-item">
          <NavLink to="/despesas-fixas" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
            <Landmark size={20} className="nav-icon" />
            <span>Despesas Fixas</span>
          </NavLink>
        </li>
        <li className="nav-item">
          <NavLink to="/receitas-fixas" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
            <Wallet size={20} className="nav-icon" />
            <span>Receitas Fixas</span>
          </NavLink>
        </li>
        <li className="nav-item">
          <NavLink to="/receitas-variaveis" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
            <TrendingUp size={20} className="nav-icon" />
            <span>Receitas Variáveis</span>
          </NavLink>
        </li>
      </ul>
    </nav>
  );
};

export default Navigation;
