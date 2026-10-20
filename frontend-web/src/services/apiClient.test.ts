/**
 * @vitest-environment jsdom
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { apiClient, ApiError, getCsrfToken, setUnauthorizedHandler } from './apiClient';

describe('apiClient', () => {
  const originalFetch = globalThis.fetch;

  beforeEach(() => {
    vi.restoreAllMocks();
    document.cookie = '';
    setUnauthorizedHandler(null);
  });

  afterEach(() => {
    globalThis.fetch = originalFetch;
    document.cookie = '';
    setUnauthorizedHandler(null);
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

    await expect(promise).rejects.toMatchObject({ name: 'AbortError' });
    await expect(promise).rejects.not.toBeInstanceOf(ApiError);
  });

  it('deve preservar motivo customizado de cancelamento no AbortSignal', async () => {
    const controller = new AbortController();
    const customReason = new Error('Operação cancelada pelo usuário');

    globalThis.fetch = vi.fn().mockImplementation((_url, options) => {
      return new Promise((_resolve, reject) => {
        if (options?.signal) {
          options.signal.addEventListener('abort', () => {
            reject(options.signal.reason);
          });
        }
      });
    });

    const promise = apiClient.get('/api/contas', { signal: controller.signal, timeout: 10000 });
    controller.abort(customReason);

    await expect(promise).rejects.toThrow('Operação cancelada pelo usuário');
    await expect(promise).rejects.not.toBeInstanceOf(ApiError);
  });

  it('deve suportar método PATCH com serialização de corpo e cabeçalhos', async () => {
    document.cookie = 'XSRF-TOKEN=patch-token-val; path=/';
    const mockResponse = { id: 1, descricao: 'Atualizado' };

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      text: vi.fn().mockResolvedValue(JSON.stringify(mockResponse)),
    } as unknown as Response);

    const result = await apiClient.patch('/api/contas/1', { descricao: 'Atualizado' });

    expect(globalThis.fetch).toHaveBeenCalledWith(
      '/api/contas/1',
      expect.objectContaining({
        method: 'PATCH',
        body: JSON.stringify({ descricao: 'Atualizado' }),
        headers: expect.any(Headers),
      })
    );
    expect(result).toEqual(mockResponse);
  });

  it('não deve enviar cabeçalho X-XSRF-TOKEN para URLs externas absolutas', async () => {
    document.cookie = 'XSRF-TOKEN=secret-csrf; path=/';

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      text: vi.fn().mockResolvedValue('{}'),
    } as unknown as Response);

    await apiClient.post('https://external-service.com/api/webhook', { data: 1 });

    const fetchHeaders = (globalThis.fetch as any).mock.calls[0][1].headers as Headers;
    expect(fetchHeaders.has('X-XSRF-TOKEN')).toBe(false);
  });

  it('deve preservar âncora/hash e limpar separadores residuais na URL', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      text: vi.fn().mockResolvedValue('[]'),
    } as unknown as Response);

    await apiClient.get('/api/relatorio?#secao1', { params: { mesAno: '2026-09' } });

    expect(globalThis.fetch).toHaveBeenCalledWith(
      '/api/relatorio?mesAno=2026-09#secao1',
      expect.anything()
    );
  });

  it('deve extrair primeira mensagem de erro quando fieldErrors contiver array de strings', async () => {
    const errorBody = {
      fieldErrors: {
        descricao: ['Descrição é obrigatória', 'Descrição deve ter no mínimo 3 caracteres'],
      },
    };

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      statusText: 'Bad Request',
      headers: new Headers({ 'content-type': 'application/json' }),
      text: vi.fn().mockResolvedValue(JSON.stringify(errorBody)),
    } as unknown as Response);

    await expect(apiClient.get('/api/contas')).rejects.toThrow('Descrição é obrigatória');
  });

  it('deve disparar o handler central de 401 e lançar ApiError com status 401', async () => {
    const unauthorizedSpy = vi.fn();
    setUnauthorizedHandler(unauthorizedSpy);

    const errorPayload = { message: 'Sessão expirada' };
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      statusText: 'Unauthorized',
      headers: new Headers({ 'content-type': 'application/json' }),
      text: vi.fn().mockResolvedValue(JSON.stringify(errorPayload)),
    } as unknown as Response);

    await expect(apiClient.get('/api/contas')).rejects.toThrow(ApiError);
    expect(unauthorizedSpy).toHaveBeenCalledTimes(1);

    try {
      await apiClient.get('/api/contas');
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError);
      expect((err as ApiError).status).toBe(401);
    }
  });

  it('não deve disparar handler de 401 quando backend responder com HTTP 403 (Forbidden)', async () => {
    const unauthorizedSpy = vi.fn();
    setUnauthorizedHandler(unauthorizedSpy);

    const errorPayload = { message: 'Acesso negado ao recurso' };
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 403,
      statusText: 'Forbidden',
      headers: new Headers({ 'content-type': 'application/json' }),
      text: vi.fn().mockResolvedValue(JSON.stringify(errorPayload)),
    } as unknown as Response);

    await expect(apiClient.get('/api/contas/1')).rejects.toThrow('Acesso negado ao recurso');
    expect(unauthorizedSpy).not.toHaveBeenCalled();

    try {
      await apiClient.get('/api/contas/1');
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError);
      expect((err as ApiError).status).toBe(403);
    }
  });

  it('não deve disparar handler de 401 quando backend responder com HTTP 404 (Not Found)', async () => {
    const unauthorizedSpy = vi.fn();
    setUnauthorizedHandler(unauthorizedSpy);

    const errorPayload = { message: 'Recurso inexistente' };
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 404,
      statusText: 'Not Found',
      headers: new Headers({ 'content-type': 'application/json' }),
      text: vi.fn().mockResolvedValue(JSON.stringify(errorPayload)),
    } as unknown as Response);

    await expect(apiClient.get('/api/contas/999')).rejects.toThrow('Recurso inexistente');
    expect(unauthorizedSpy).not.toHaveBeenCalled();

    try {
      await apiClient.get('/api/contas/999');
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError);
      expect((err as ApiError).status).toBe(404);
    }
  });

  it('não deve disparar handler de 401 quando backend responder com HTTP 400 (Bad Request)', async () => {
    const unauthorizedSpy = vi.fn();
    setUnauthorizedHandler(unauthorizedSpy);

    const errorPayload = { message: 'Dados inválidos' };
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      statusText: 'Bad Request',
      headers: new Headers({ 'content-type': 'application/json' }),
      text: vi.fn().mockResolvedValue(JSON.stringify(errorPayload)),
    } as unknown as Response);

    await expect(apiClient.post('/api/contas', {})).rejects.toThrow('Dados inválidos');
    expect(unauthorizedSpy).not.toHaveBeenCalled();

    try {
      await apiClient.post('/api/contas', {});
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError);
      expect((err as ApiError).status).toBe(400);
    }
  });

  it('não deve disparar handler de 401 quando backend responder com HTTP 500 (Internal Server Error)', async () => {
    const unauthorizedSpy = vi.fn();
    setUnauthorizedHandler(unauthorizedSpy);

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 500,
      statusText: 'Internal Server Error',
      headers: new Headers({ 'content-type': 'text/plain' }),
      text: vi.fn().mockResolvedValue('Erro interno do servidor'),
    } as unknown as Response);

    await expect(apiClient.get('/api/contas')).rejects.toThrow('Erro interno do servidor');
    expect(unauthorizedSpy).not.toHaveBeenCalled();

    try {
      await apiClient.get('/api/contas');
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError);
      expect((err as ApiError).status).toBe(500);
    }
  });

  it('não deve falhar se o handler de 401 estiver nulo ou desregistrado', async () => {
    setUnauthorizedHandler(null);

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      statusText: 'Unauthorized',
      headers: new Headers({ 'content-type': 'application/json' }),
      text: vi.fn().mockResolvedValue(JSON.stringify({ message: 'Não autorizado' })),
    } as unknown as Response);

    await expect(apiClient.get('/api/contas')).rejects.toThrow('Não autorizado');
  });

  it('deve continuar lançando ApiError normalmente se o handler de 401 lançar exceção', async () => {
    const throwingHandler = vi.fn().mockImplementation(() => {
      throw new Error('Falha no callback');
    });
    setUnauthorizedHandler(throwingHandler);

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      statusText: 'Unauthorized',
      headers: new Headers({ 'content-type': 'application/json' }),
      text: vi.fn().mockResolvedValue(JSON.stringify({ message: 'Token expirado' })),
    } as unknown as Response);

    await expect(apiClient.get('/api/contas')).rejects.toThrow('Token expirado');
    expect(throwingHandler).toHaveBeenCalledTimes(1);
  });

  it('não deve disparar handler de 401 quando skipUnauthorizedHandler for true', async () => {
    const unauthorizedSpy = vi.fn();
    setUnauthorizedHandler(unauthorizedSpy);

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      statusText: 'Unauthorized',
      headers: new Headers({ 'content-type': 'application/json' }),
      text: vi.fn().mockResolvedValue(JSON.stringify({ message: 'Não autorizado' })),
    } as unknown as Response);

    await expect(apiClient.get('/api/auth/me', { skipUnauthorizedHandler: true })).rejects.toThrow('Não autorizado');
    expect(unauthorizedSpy).not.toHaveBeenCalled();
  });

  it('não deve disparar handler de 401 para requisições cross-origin (externas)', async () => {
    const unauthorizedSpy = vi.fn();
    setUnauthorizedHandler(unauthorizedSpy);

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      statusText: 'Unauthorized',
      headers: new Headers({ 'content-type': 'application/json' }),
      text: vi.fn().mockResolvedValue(JSON.stringify({ message: 'External 401' })),
    } as unknown as Response);

    await expect(apiClient.get('https://external-service.com/api/data')).rejects.toThrow('External 401');
    expect(unauthorizedSpy).not.toHaveBeenCalled();
  });

  it('deve registrar e disparar handler quando configurado via apiClient.setUnauthorizedHandler', async () => {
    const unauthorizedSpy = vi.fn();
    apiClient.setUnauthorizedHandler(unauthorizedSpy);

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      statusText: 'Unauthorized',
      headers: new Headers({ 'content-type': 'application/json' }),
      text: vi.fn().mockResolvedValue(JSON.stringify({ message: 'Sessão expirada' })),
    } as unknown as Response);

    await expect(apiClient.get('/api/contas')).rejects.toThrow('Sessão expirada');
    expect(unauthorizedSpy).toHaveBeenCalledTimes(1);
  });
});
