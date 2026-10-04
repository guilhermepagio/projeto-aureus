import { NavLink } from 'react-router-dom';
import { LayoutDashboard, TrendingUp, TrendingDown, Landmark, Wallet } from 'lucide-react';

const Navigation: React.FC = () => {
    const getNavLinkClass = (type: 'primary' | 'red' | 'green' | 'blue' | 'secondary' | 'slate') => ({ isActive }: { isActive: boolean }) => {
    const baseClasses = "flex flex-col justify-center items-center no-underline text-[10px] h-full px-1 py-2 transition-all duration-200 ease-in-out focus-visible:outline-2 focus-visible:outline-offset-2 md:text-[13px] md:py-1.5 md:px-4 md:rounded-md md:flex-row";
    
    let activeClasses = "";
    let inactiveClasses = "text-text-muted font-medium md:hover:bg-main md:hover:text-text-main";
    
    if (type === 'primary') {
      activeClasses = "text-primary font-bold md:bg-primary md:text-white md:font-semibold md:shadow-sm focus-visible:outline-primary";
      inactiveClasses += " hover:text-primary-light";
    } else if (type === 'red') {
      activeClasses = "text-red-600 font-bold md:bg-red-600 md:text-white md:font-semibold md:shadow-sm focus-visible:outline-red-600";
      inactiveClasses += " hover:text-red-400";
        } else if (type === 'blue') {
      activeClasses = "text-blue-600 font-bold md:bg-blue-600 md:text-white md:font-semibold md:shadow-sm focus-visible:outline-blue-600";
      inactiveClasses += " hover:text-blue-400";
    } else if (type === 'secondary') {
      activeClasses = "text-secondary font-bold md:bg-secondary md:text-white md:font-semibold md:shadow-sm focus-visible:outline-secondary";
      inactiveClasses += " hover:text-secondary-light";
    } else if (type === 'slate') {
      activeClasses = "text-slate-700 font-bold md:bg-slate-700 md:text-white md:font-semibold md:shadow-sm focus-visible:outline-slate-700";
      inactiveClasses += " hover:text-slate-500";
    } else if (type === 'green') {
      activeClasses = "text-green-600 font-bold md:bg-green-600 md:text-white md:font-semibold md:shadow-sm focus-visible:outline-green-600";
      inactiveClasses += " hover:text-green-400";
    }
    
    return `${baseClasses} ${isActive ? activeClasses : inactiveClasses}`;
  };

  return (
    <nav className="fixed bottom-0 left-0 w-full bg-surface shadow-[0_-2px_10px_rgba(0,0,0,0.05)] z-[1000] pb-[env(safe-area-inset-bottom)] md:static md:w-auto md:bg-transparent md:shadow-none md:p-0 md:flex md:justify-center" aria-label="Navegação Principal">
      <ul className="flex justify-around items-center list-none m-0 p-0 h-[64px] md:bg-surface md:rounded-md md:shadow-sm md:p-1 md:h-auto md:gap-1 md:border md:border-border">
        <li className="flex-1 text-center md:flex-none">
          <NavLink to="/" end className={getNavLinkClass('blue')}>
            <LayoutDashboard size={20} className="mb-1 md:hidden" />
            <span>Consolidação</span>
          </NavLink>
        </li>
        <li className="flex-1 text-center md:flex-none">
          <NavLink to="/despesas-variaveis" className={getNavLinkClass('red')}>
            <TrendingDown size={20} className="mb-1 md:hidden" />
            <span>Despesas Variáveis</span>
          </NavLink>
        </li>
        <li className="flex-1 text-center md:flex-none">
          <NavLink to="/despesas-fixas" className={getNavLinkClass('red')}>
            <Landmark size={20} className="mb-1 md:hidden" />
            <span>Despesas Fixas</span>
          </NavLink>
        </li>
        <li className="flex-1 text-center md:flex-none">
          <NavLink to="/receitas-fixas" className={getNavLinkClass('primary')}>
            <Wallet size={20} className="mb-1 md:hidden" />
            <span>Receitas Fixas</span>
          </NavLink>
        </li>
        <li className="flex-1 text-center md:flex-none">
          <NavLink to="/receitas-variaveis" className={getNavLinkClass('primary')}>
            <TrendingUp size={20} className="mb-1 md:hidden" />
            <span>Receitas Variáveis</span>
          </NavLink>
        </li>
      </ul>
    </nav>
  );
};

export default Navigation;
