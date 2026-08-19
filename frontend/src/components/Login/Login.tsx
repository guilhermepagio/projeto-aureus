import React, { useEffect } from 'react';
import { useAuthStore } from '../../store/authStore';
import { useNavigate, useSearchParams } from 'react-router-dom';
import toast, { Toaster } from 'react-hot-toast';
import './Login.css';

const Login: React.FC = () => {
    const { isAuthenticated, isLoading } = useAuthStore();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    useEffect(() => {
        if (!isLoading && isAuthenticated) {
            navigate('/');
        }
    }, [isAuthenticated, isLoading, navigate]);

    useEffect(() => {
        if (searchParams.get('error')) {
            toast.error('Autorização falhou ou foi cancelada.');
        }
    }, [searchParams]);

    const handleLogin = () => {
        window.location.href = '/api/oauth2/authorization/google';
    };

    if (isLoading) {
        return <div className="login-loading">Carregando...</div>;
    }

    return (
        <div className="login-container">
            <Toaster position="top-right" />
            <div className="login-card">
                <h1 className="login-title">Aureus</h1>
                <p className="login-subtitle">Acesse sua conta para continuar</p>
                <button className="login-button" onClick={handleLogin}>
                    Entrar com Google
                </button>
            </div>
        </div>
    );
};

export default Login;
