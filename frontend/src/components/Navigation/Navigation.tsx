import { NavLink } from 'react-router-dom';
import { LayoutDashboard, TrendingUp, TrendingDown, Landmark, Wallet } from 'lucide-react';

const Navigation: React.FC = () => {
  const getNavLinkClass = ({ isActive }: { isActive: boolean }) => {
    const baseClasses = "flex flex-col justify-center items-center no-underline text-[10px] h-full px-1 py-2 transition-all duration-200 ease-in-out focus-visible:outline-2 focus-visible:outline-primary focus-visible:outline-offset-2 md:text-[13px] md:py-1.5 md:px-4 md:rounded-pill md:flex-row";
    const activeClasses = "text-primary font-bold md:bg-primary md:text-white md:font-semibold md:shadow-sm";
    const inactiveClasses = "text-text-muted font-medium hover:text-primary-light md:hover:bg-main md:hover:text-text-main";
    return `${baseClasses} ${isActive ? activeClasses : inactiveClasses}`;
  };

  return (
    <nav className="fixed bottom-0 left-0 w-full bg-surface shadow-[0_-2px_10px_rgba(0,0,0,0.05)] z-[1000] pb-[env(safe-area-inset-bottom)] md:static md:w-auto md:bg-transparent md:shadow-none md:p-0 md:flex md:justify-center" aria-label="Navegação Principal">
      <ul className="flex justify-around items-center list-none m-0 p-0 h-[64px] md:bg-surface md:rounded-pill md:shadow-sm md:p-1 md:h-auto md:gap-1 md:border md:border-border">
        <li className="flex-1 text-center md:flex-none">
          <NavLink to="/" end className={getNavLinkClass}>
            <LayoutDashboard size={20} className="mb-1 md:hidden" />
            <span>Consolidação</span>
          </NavLink>
        </li>
        <li className="flex-1 text-center md:flex-none">
          <NavLink to="/despesas-variaveis" className={getNavLinkClass}>
            <TrendingDown size={20} className="mb-1 md:hidden" />
            <span>Despesas Variáveis</span>
          </NavLink>
        </li>
        <li className="flex-1 text-center md:flex-none">
          <NavLink to="/despesas-fixas" className={getNavLinkClass}>
            <Landmark size={20} className="mb-1 md:hidden" />
            <span>Despesas Fixas</span>
          </NavLink>
        </li>
        <li className="flex-1 text-center md:flex-none">
          <NavLink to="/receitas-fixas" className={getNavLinkClass}>
            <Wallet size={20} className="mb-1 md:hidden" />
            <span>Receitas Fixas</span>
          </NavLink>
        </li>
        <li className="flex-1 text-center md:flex-none">
          <NavLink to="/receitas-variaveis" className={getNavLinkClass}>
            <TrendingUp size={20} className="mb-1 md:hidden" />
            <span>Receitas Variáveis</span>
          </NavLink>
        </li>
      </ul>
    </nav>
  );
};

export default Navigation;
