/**
 * @vitest-environment jsdom
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import React, { useState } from 'react';
import { ErrorBoundary } from './ErrorBoundary';

describe('ErrorBoundary', () => {
  let consoleErrorSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    cleanup();
    consoleErrorSpy.mockRestore();
  });

  const ProblemChild = ({ shouldThrow }: { shouldThrow: boolean }) => {
    if (shouldThrow) {
      throw new Error('Falha simulada de renderização');
    }
    return <div>Conteúdo renderizado com sucesso</div>;
  };

  it('deve renderizar os componentes filhos normalmente quando não houver erros', () => {
    render(
      <ErrorBoundary>
        <div>Filho Seguro</div>
      </ErrorBoundary>
    );

    expect(screen.getByText('Filho Seguro')).toBeDefined();
    expect(consoleErrorSpy).not.toHaveBeenCalled();
  });

  it('deve interceptar erro de renderização, logar no console e exibir fallback com botão Tentar Novamente', () => {
    render(
      <ErrorBoundary>
        <ProblemChild shouldThrow={true} />
      </ErrorBoundary>
    );

    expect(screen.getByRole('alert')).toBeDefined();
    expect(screen.getByText('Ops! Algo deu errado')).toBeDefined();
    expect(screen.getByText('Falha simulada de renderização')).toBeDefined();
    expect(screen.getByRole('button', { name: /tentar novamente/i })).toBeDefined();

    expect(consoleErrorSpy).toHaveBeenCalledWith(
      'ErrorBoundary capturou uma falha de renderização:',
      expect.any(Error),
      expect.anything()
    );
  });

  it('deve permitir recuperação de erro ao clicar em Tentar Novamente', () => {
    const TestComponent = () => {
      const [hasError, setHasError] = useState(true);

      return (
        <ErrorBoundary onReset={() => setHasError(false)}>
          <ProblemChild shouldThrow={hasError} />
        </ErrorBoundary>
      );
    };

    render(<TestComponent />);

    expect(screen.getByText('Ops! Algo deu errado')).toBeDefined();

    const retryButton = screen.getByRole('button', { name: /tentar novamente/i });
    fireEvent.click(retryButton);

    expect(screen.getByText('Conteúdo renderizado com sucesso')).toBeDefined();
    expect(screen.queryByText('Ops! Algo deu errado')).toBeNull();
  });

  it('deve suportar fallback customizado estático', () => {
    render(
      <ErrorBoundary fallback={<div>Fallback Customizado</div>}>
        <ProblemChild shouldThrow={true} />
      </ErrorBoundary>
    );

    expect(screen.getByText('Fallback Customizado')).toBeDefined();
    expect(screen.queryByText('Ops! Algo deu errado')).toBeNull();
  });

  it('deve suportar fallback customizado funcional com resetErrorBoundary', () => {
    let resetFn: (() => void) | undefined;

    render(
      <ErrorBoundary
        fallback={({ error, resetErrorBoundary }) => {
          resetFn = resetErrorBoundary;
          return <div>Erro capturado: {error?.message}</div>;
        }}
      >
        <ProblemChild shouldThrow={true} />
      </ErrorBoundary>
    );

    expect(screen.getByText('Erro capturado: Falha simulada de renderização')).toBeDefined();
    expect(typeof resetFn).toBe('function');
  });

  it('deve resetar o estado de erro quando resetKeys mudar', () => {
    const TestComponent = ({ resetKey }: { resetKey: number }) => {
      return (
        <ErrorBoundary resetKeys={[resetKey]}>
          <ProblemChild shouldThrow={resetKey === 1} />
        </ErrorBoundary>
      );
    };

    const { rerender } = render(<TestComponent resetKey={1} />);

    expect(screen.getByText('Ops! Algo deu errado')).toBeDefined();

    rerender(<TestComponent resetKey={2} />);

    expect(screen.getByText('Conteúdo renderizado com sucesso')).toBeDefined();
    expect(screen.queryByText('Ops! Algo deu errado')).toBeNull();
  });

  it('deve lidar corretamente com exceções que não são instâncias de Error', () => {
    const NonErrorChild = () => {
      throw 'Erro em formato de string literal';
    };

    render(
      <ErrorBoundary>
        <NonErrorChild />
      </ErrorBoundary>
    );

    expect(screen.getByText('Ops! Algo deu errado')).toBeDefined();
    expect(screen.getByText('Erro em formato de string literal')).toBeDefined();
  });

  it('deve resetar o estado quando o tamanho do array resetKeys diminuir', () => {
    const TestComponent = ({ keys }: { keys: number[] }) => {
      return (
        <ErrorBoundary resetKeys={keys}>
          <ProblemChild shouldThrow={keys.length > 1} />
        </ErrorBoundary>
      );
    };

    const { rerender } = render(<TestComponent keys={[1, 2]} />);
    expect(screen.getByText('Ops! Algo deu errado')).toBeDefined();

    // Reduz comprimento mantendo prefixo [1]
    rerender(<TestComponent keys={[1]} />);
    expect(screen.getByText('Conteúdo renderizado com sucesso')).toBeDefined();
    expect(screen.queryByText('Ops! Algo deu errado')).toBeNull();
  });

  it('deve renderizar fallback mesmo se o callback onError lançar uma exceção', () => {
    const throwingOnError = vi.fn().mockImplementation(() => {
      throw new Error('Falha no serviço de telemetria externa');
    });

    render(
      <ErrorBoundary onError={throwingOnError}>
        <ProblemChild shouldThrow={true} />
      </ErrorBoundary>
    );

    expect(throwingOnError).toHaveBeenCalled();
    expect(screen.getByText('Ops! Algo deu errado')).toBeDefined();
  });
});
