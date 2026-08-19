import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '../../store/authStore';
import toast from 'react-hot-toast';
import './Header.css';

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
    <header className="app-header">
      <div className="header-left"></div>
      
      <div className="header-center">
        {children}
      </div>

      <div className="header-right" ref={dropdownRef}>
        <button 
          className="profile-button" 
          onClick={toggleDropdown}
          aria-expanded={isDropdownOpen}
          aria-haspopup="menu"
          aria-label="Menu do usuário"
        >
          {profileImage && !imgError ? (
            <img 
              src={profileImage} 
              alt="" 
              className="profile-image" 
              referrerPolicy="no-referrer" 
              onError={() => setImgError(true)} 
            />
          ) : (
            <div className="profile-placeholder">U</div>
          )}
        </button>
        
        {isDropdownOpen && (
          <div className="profile-dropdown" role="menu">
            <button className="dropdown-item logout-button" onClick={handleLogout} role="menuitem" disabled={isLoggingOut}>
              {isLoggingOut ? 'Saindo...' : 'Sair'}
            </button>
          </div>
        )}
      </div>
    </header>
  );
};

export default Header;
