import React, { useEffect } from 'react';
import { useAuthStore } from '../../store/authStore';
import { useNavigate, useSearchParams } from 'react-router-dom';
import toast, { Toaster } from 'react-hot-toast';
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
        return <div className="flex justify-center items-center min-h-screen text-primary">Carregando...</div>;
    }

    return (
        <div className="flex justify-center items-center min-h-screen bg-[#f5f5f5]">
            <Toaster position="top-right" />
            <div className="bg-surface p-8 rounded-md shadow-md text-center w-full max-w-[400px]">
                <h1 className="text-primary text-[2rem] mb-2">Aureus</h1>
                <p className="text-[#666666] mb-8">Acesse sua conta para continuar</p>
                <button className="bg-primary text-white border-none rounded-sm px-6 py-3 text-base cursor-pointer w-full transition-colors duration-200 ease-in-out hover:bg-secondary hover:text-[#333333] focus-visible:ring-2 focus-visible:ring-primary focus-visible:outline-none" onClick={handleLogin}>
                    Entrar com Google
                </button>
            </div>
        </div>
    );
};

export default Login;
