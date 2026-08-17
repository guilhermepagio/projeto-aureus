import { NavLink } from 'react-router-dom';
import './Navigation.css';

const Navigation: React.FC = () => {
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
      </ul>
    </nav>
  );
};

export default Navigation;
