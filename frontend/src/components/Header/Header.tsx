import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '../../store/authStore';
import toast from 'react-hot-toast';
import { Landmark, Tags, LogOut } from 'lucide-react';

interface HeaderProps {
  children?: React.ReactNode;
}

const Header: React.FC<HeaderProps> = ({ children }) => {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [imgError, setImgError] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { profileImage, logout } = useAuthStore();

  const handleLogout = async () => {
    if (isLoggingOut) return;
    setIsLoggingOut(true);
    const csrfToken = document.cookie.split('; ').find(row => row.startsWith('XSRF-TOKEN='))?.substring('XSRF-TOKEN='.length);
    
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
      setIsLoggingOut(false);
      navigate('/login');
    }
  };

  const toggleDropdown = () => {
    setIsDropdownOpen(prev => !prev);
  };

  useEffect(() => {
    const handleClickOutside = (event: PointerEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsDropdownOpen(false);
      }
    };
    
    document.addEventListener('pointerdown', handleClickOutside as EventListener);
    return () => {
      document.removeEventListener('pointerdown', handleClickOutside as EventListener);
    };
  }, []);

  return (
    <header className="grid grid-cols-[1fr_auto_1fr] items-center p-4 bg-transparent relative z-[100] pointer-events-none">
      <div className="flex justify-start pointer-events-auto">
        <div className="text-[22px] font-bold text-primary tracking-[-0.5px] flex items-center gap-2 cursor-pointer" onClick={() => navigate('/')}>
          Aureus
          <span className="w-2 h-2 bg-secondary rounded-full"></span>
        </div>
      </div>
      
      <div className="flex justify-center pointer-events-auto">
        {children}
      </div>

      <div className="relative flex justify-end pointer-events-auto" ref={dropdownRef}>
        <button 
          className="bg-transparent border-none p-0 cursor-pointer rounded-full w-10 h-10 overflow-hidden flex items-center justify-center shadow-[0_2px_8px_rgba(0,0,0,0.1)] transition-transform transition-shadow duration-200 ease-out hover:scale-105 hover:shadow-[0_4px_12px_rgba(0,0,0,0.15)] focus-visible:ring-2 focus-visible:ring-primary focus-visible:outline-none" 
          onClick={toggleDropdown}
          aria-expanded={isDropdownOpen}
          aria-haspopup="menu"
          aria-label="Menu do usuário"
        >
          {profileImage && !imgError ? (
            <img 
              src={profileImage} 
              alt="" 
              className="w-full h-full object-cover" 
              referrerPolicy="no-referrer" 
              onError={() => setImgError(true)} 
            />
          ) : (
            <div className="w-full h-full bg-primary text-white flex items-center justify-center font-bold text-xl">U</div>
          )}
        </button>
        
        {isDropdownOpen && (
          <div className="absolute top-[50px] right-0 bg-surface rounded-lg shadow-[0_4px_12px_rgba(0,0,0,0.15)] min-w-[150px] overflow-hidden z-[101] border border-[rgba(0,0,0,0.05)]" role="menu">
            <button className="flex items-center gap-2 w-full py-3 px-4 text-left bg-transparent border-none cursor-pointer font-inherit text-base text-text-main font-medium transition-colors duration-200 ease-out hover:bg-main" onClick={() => { navigate('/contas'); setIsDropdownOpen(false); }} role="menuitem">
              <Landmark size={18} /> Contas
            </button>
            <button className="flex items-center gap-2 w-full py-3 px-4 text-left bg-transparent border-none cursor-pointer font-inherit text-base text-text-main font-medium transition-colors duration-200 ease-out hover:bg-main" onClick={() => { navigate('/categorias'); setIsDropdownOpen(false); }} role="menuitem">
              <Tags size={18} /> Categorias
            </button>
            <button className="flex items-center gap-2 w-full py-3 px-4 text-left bg-transparent border-none cursor-pointer font-inherit text-base text-[#d32f2f] font-medium transition-colors duration-200 ease-out hover:bg-main disabled:opacity-50 disabled:cursor-not-allowed border-t border-[rgba(0,0,0,0.05)]" onClick={handleLogout} role="menuitem" disabled={isLoggingOut}>
              <LogOut size={18} /> {isLoggingOut ? 'Saindo...' : 'Sair'}
            </button>
          </div>
        )}
      </div>
    </header>
  );
};

export default Header;
