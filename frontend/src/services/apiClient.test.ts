/**
 * @vitest-environment jsdom
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { apiClient, ApiError, getCsrfToken } from './apiClient';

describe('apiClient', () => {
  const originalFetch = globalThis.fetch;

  beforeEach(() => {
    vi.restoreAllMocks();
    document.cookie = '';
  });

  afterEach(() => {
    globalThis.fetch = originalFetch;
    document.cookie = '';
  });

  it('deve realizar requisição GET com sucesso e retornar JSON parseado', async () => {
    const mockData = [{ id: 1, descricao: 'Conta Corrente' }];
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      text: async () => JSON.stringify(mockData),
    });

    const result = await apiClient.get('/api/contas');

    expect(result).toEqual(mockData);
    expect(globalThis.fetch).toHaveBeenCalledWith(
      '/api/contas',
      expect.objectContaining({
        method: 'GET',
        credentials: 'same-origin',
      })
    );
    const callHeaders = (globalThis.fetch as any).mock.calls[0][1].headers as Headers;
    expect(callHeaders.get('Accept')).toBe('application/json');
  });

  it('deve ler cookie CSRF mesmo sem espaço após ponto-e-vírgula', () => {
    Object.defineProperty(document, 'cookie', {
      writable: true,
      configurable: true,
      value: 'foo=bar;XSRF-TOKEN=no-space-token;baz=qux',
    });
    expect(getCsrfToken()).toBe('no-space-token');
  });

  it('deve injetar automaticamente o cabeçalho X-XSRF-TOKEN em mutações (POST, PUT, DELETE)', async () => {
    document.cookie = 'XSRF-TOKEN=test-csrf-token-123; path=/';
    expect(getCsrfToken()).toBe('test-csrf-token-123');

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      text: async () => JSON.stringify({ id: 1 }),
    });

    const body = { descricao: 'Nova Conta' };
    await apiClient.post('/api/contas', body);

    expect(globalThis.fetch).toHaveBeenCalledWith(
      '/api/contas',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify(body),
        headers: expect.any(Headers),
        credentials: 'same-origin',
      })
    );

    const callHeaders = (globalThis.fetch as any).mock.calls[0][1].headers as Headers;
    expect(callHeaders.get('X-XSRF-TOKEN')).toBe('test-csrf-token-123');
    expect(callHeaders.get('Content-Type')).toBe('application/json');
    expect(callHeaders.get('Accept')).toBe('application/json');
  });

  it('deve realizar requisição PUT com corpo JSON, Content-Type e X-XSRF-TOKEN', async () => {
    document.cookie = 'XSRF-TOKEN=put-token-xyz; path=/';

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      text: async () => JSON.stringify({ id: 1, descricao: 'Conta Atualizada' }),
    });

    const body = { id: 1, descricao: 'Conta Atualizada' };
    const result = await apiClient.put('/api/contas/1', body);

    expect(result).toEqual({ id: 1, descricao: 'Conta Atualizada' });
    expect(globalThis.fetch).toHaveBeenCalledWith(
      '/api/contas/1',
      expect.objectContaining({
        method: 'PUT',
        body: JSON.stringify(body),
        credentials: 'same-origin',
      })
    );

    const callHeaders = (globalThis.fetch as any).mock.calls[0][1].headers as Headers;
    expect(callHeaders.get('X-XSRF-TOKEN')).toBe('put-token-xyz');
    expect(callHeaders.get('Content-Type')).toBe('application/json');
  });

  it('não deve injetar cabeçalho X-XSRF-TOKEN em requisições GET mesmo com cookie presente', async () => {
    document.cookie = 'XSRF-TOKEN=test-csrf-token-123; path=/';

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      text: async () => JSON.stringify([]),
    });

    await apiClient.get('/api/contas');

    const callHeaders = (globalThis.fetch as any).mock.calls[0][1].headers as Headers;
    expect(callHeaders.get('X-XSRF-TOKEN')).toBeNull();
  });

  it('deve suportar chamadas DELETE com status 204 e cabeçalho CSRF', async () => {
    document.cookie = 'XSRF-TOKEN=delete-token; path=/';

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 204,
      headers: new Headers(),
      text: async () => '',
    });

    const result = await apiClient.delete('/api/contas/1');

    expect(result).toBeUndefined();
    const callHeaders = (globalThis.fetch as any).mock.calls[0][1].headers as Headers;
    expect(callHeaders.get('X-XSRF-TOKEN')).toBe('delete-token');
  });

  it('deve extrair errors[0].defaultMessage da resposta de erro da API', async () => {
    const errorPayload = {
      timestamp: '2026-09-27T12:00:00Z',
      status: 400,
      error: 'Bad Request',
      message: 'Erro de validação nos campos informados',
      errors: [
        { field: 'descricao', defaultMessage: 'Descrição é obrigatória' }
      ]
    };

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      statusText: 'Bad Request',
      headers: new Headers({ 'content-type': 'application/json' }),
      text: async () => JSON.stringify(errorPayload),
    });

    await expect(apiClient.post('/api/contas', {})).rejects.toThrow('Descrição é obrigatória');

    try {
      await apiClient.post('/api/contas', {});
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError);
      const apiErr = err as ApiError;
      expect(apiErr.status).toBe(400);
      expect(apiErr.data).toEqual(errorPayload);
    }
  });

  it('deve extrair erro a partir de fieldErrors quando errors estiver ausente', async () => {
    const errorPayload = {
      timestamp: '2026-09-27T12:00:00Z',
      status: 400,
      error: 'Bad Request',
      message: 'Erro de validação nos campos informados',
      fieldErrors: {
        descricao: 'A descrição da conta é um campo obrigatório'
      }
    };

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      statusText: 'Bad Request',
      headers: new Headers({ 'content-type': 'application/json' }),
      text: async () => JSON.stringify(errorPayload),
    });

    await expect(apiClient.post('/api/contas', {})).rejects.toThrow(
      'A descrição da conta é um campo obrigatório'
    );
  });

  it('deve extrair message da resposta de erro quando errors e fieldErrors estiverem ausentes', async () => {
    const errorPayload = {
      timestamp: '2026-09-27T12:00:00Z',
      status: 400,
      error: 'Bad Request',
      message: 'Não é possível realizar a operação devido a vínculos ativos ou restrições de integridade.',
    };

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      statusText: 'Bad Request',
      headers: new Headers({ 'content-type': 'application/json' }),
      text: async () => JSON.stringify(errorPayload),
    });

    await expect(apiClient.delete('/api/contas/1')).rejects.toThrow(
      'Não é possível realizar a operação devido a vínculos ativos ou restrições de integridade.'
    );
  });

  it('deve extrair error da resposta quando message e errors estiverem ausentes', async () => {
    const errorPayload = {
      timestamp: '2026-09-27T12:00:00Z',
      status: 404,
      error: 'Recurso não encontrado',
    };

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 404,
      statusText: 'Not Found',
      headers: new Headers({ 'content-type': 'application/json' }),
      text: async () => JSON.stringify(errorPayload),
    });

    await expect(apiClient.get('/api/contas/999')).rejects.toThrow('Recurso não encontrado');
  });

  it('deve utilizar fallback por status quando resposta não for JSON estruturado', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 500,
      statusText: 'Internal Server Error',
      headers: new Headers({ 'content-type': 'text/plain' }),
      text: async () => 'Falha geral do servidor',
    });

    await expect(apiClient.get('/api/contas')).rejects.toThrow('Falha geral do servidor');
  });

  it('deve sanitizar resposta de erro com corpo HTML e utilizar mensagem de fallback', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 502,
      statusText: 'Bad Gateway',
      headers: new Headers({ 'content-type': 'text/html' }),
      text: async () => '<html><body><h1>502 Bad Gateway</h1><p>Nginx error</p></body></html>',
    });

    await expect(apiClient.get('/api/contas')).rejects.toThrow('Erro na requisição (502: Bad Gateway)');
  });

  it('deve lançar ApiError com status 200 quando resposta 200 contiver JSON malformado', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      text: async () => 'malformed JSON {',
    });

    const promise = apiClient.get('/api/contas');
    await expect(promise).rejects.toThrow('Resposta JSON inválida do servidor');

    try {
      await promise;
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError);
      expect((err as ApiError).status).toBe(200);
    }
  });

  it('deve anexar parâmetros de busca (query string) à URL', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      text: async () => JSON.stringify([]),
    });

    await apiClient.get('/api/consolidacao/por-conta', {
      params: { mesAno: '2026-09', ativo: true, nulo: null }
    });

    expect(globalThis.fetch).toHaveBeenCalledWith(
      '/api/consolidacao/por-conta?mesAno=2026-09&ativo=true',
      expect.anything()
    );
  });

  it('deve lançar ApiError com status 408 quando timeout for excedido', async () => {
    vi.useFakeTimers();

    globalThis.fetch = vi.fn().mockImplementation((_url, options) => {
      return new Promise((_resolve, reject) => {
        if (options?.signal) {
          options.signal.addEventListener('abort', () => {
            const err = new Error('The operation was aborted');
            err.name = 'AbortError';
            reject(err);
          });
        }
      });
    });

    const promise = apiClient.get('/api/contas', { timeout: 1000 });

    vi.advanceTimersByTime(1001);

    await expect(promise).rejects.toThrow('Tempo limite da requisição excedido (1000ms)');

    try {
      await promise;
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError);
      expect((err as ApiError).status).toBe(408);
    }

    vi.useRealTimers();
  });

  it('deve respeitar sinal de cancelamento externo (AbortSignal)', async () => {
    const controller = new AbortController();

    globalThis.fetch = vi.fn().mockImplementation((_url, options) => {
      return new Promise((_resolve, reject) => {
        if (options?.signal) {
          options.signal.addEventListener('abort', () => {
            const err = new Error('AbortError');
            err.name = 'AbortError';
            reject(err);
          });
        }
      });
    });

    const promise = apiClient.get('/api/contas', { signal: controller.signal, timeout: 10000 });
    controller.abort();

    await expect(promise).rejects.toThrow();
  });
});
