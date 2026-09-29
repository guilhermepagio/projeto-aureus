import { Component, type ErrorInfo, type ReactNode } from 'react';
import { AlertTriangle, RotateCcw } from 'lucide-react';

export interface ErrorBoundaryProps {
  children: ReactNode;
  fallback?: ReactNode | ((props: { error: Error | null; resetErrorBoundary: () => void }) => ReactNode);
  onReset?: () => void;
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
  resetKeys?: unknown[];
}

export interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null | unknown;
}

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  public override state: ErrorBoundaryState = {
    hasError: false,
    error: null,
  };

  public static getDerivedStateFromError(error: unknown): ErrorBoundaryState {
    return { hasError: true, error };
  }

  public override componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    console.error('ErrorBoundary capturou uma falha de renderização:', error, errorInfo);
    try {
      this.props.onError?.(error, errorInfo);
    } catch (callbackError) {
      console.error('Falha ao executar callback onError no ErrorBoundary:', callbackError);
    }
  }

  public override componentDidUpdate(prevProps: ErrorBoundaryProps): void {
    if (this.state.hasError && this.props.resetKeys) {
      const hasChanged =
        !prevProps.resetKeys ||
        prevProps.resetKeys.length !== this.props.resetKeys.length ||
        this.props.resetKeys.some((key, idx) => key !== prevProps.resetKeys?.[idx]);
      if (hasChanged) {
        this.resetErrorBoundary();
      }
    }
  }

  public resetErrorBoundary = (): void => {
    try {
      this.props.onReset?.();
    } finally {
      this.setState({ hasError: false, error: null });
    }
  };

  public override render(): ReactNode {
    if (this.state.hasError) {
      if (typeof this.props.fallback === 'function') {
        const errorObj = this.state.error instanceof Error ? this.state.error : new Error(String(this.state.error));
        return this.props.fallback({
          error: errorObj,
          resetErrorBoundary: this.resetErrorBoundary,
        });
      }

      if (this.props.fallback) {
        return this.props.fallback;
      }

      const errorMessage = String(
        this.state.error instanceof Error ? this.state.error.message : this.state.error || ''
      );

      return (
        <div
          role="alert"
          aria-live="assertive"
          className="flex flex-col items-center justify-center min-h-[300px] h-full p-8 text-center bg-surface rounded-xl shadow-[0_4px_16px_rgba(0,0,0,0.08)] border border-[rgba(0,0,0,0.08)] m-6 max-w-lg mx-auto"
        >
          <div className="w-14 h-14 rounded-full bg-red-50 text-red-500 flex items-center justify-center mb-4">
            <AlertTriangle size={32} />
          </div>
          <h2 className="text-xl font-bold text-text-main mb-2">
            Ops! Algo deu errado
          </h2>
          <p className="text-secondary text-sm mb-6">
            Ocorreu uma falha inesperada na interface. Você pode tentar novamente para restaurar o estado da tela.
          </p>
          {errorMessage && (
            <div className="w-full text-xs bg-main p-3 rounded text-left overflow-x-auto mb-6 border border-[rgba(0,0,0,0.05)] text-secondary font-mono">
              {errorMessage}
            </div>
          )}
          <button
            type="button"
            onClick={this.resetErrorBoundary}
            className="flex items-center justify-center gap-2 py-2.5 px-6 font-medium cursor-pointer rounded-lg text-white bg-primary hover:opacity-95 transition-opacity focus-visible:ring-2 focus-visible:ring-primary focus-visible:outline-none"
          >
            <RotateCcw size={16} />
            Tentar Novamente
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
