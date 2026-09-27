export interface ApiValidationErrorItem {
  field?: string;
  message?: string;
  defaultMessage?: string;
}

export interface ApiErrorResponse {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  fieldErrors?: Record<string, string>;
  errors?: (ApiValidationErrorItem | string)[];
}

export class ApiError extends Error {
  readonly status: number;
  readonly data?: ApiErrorResponse | unknown;

  constructor(message: string, status: number, data?: ApiErrorResponse | unknown) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
    Object.setPrototypeOf(this, ApiError.prototype);
  }
}

export function getCsrfToken(): string | undefined {
  if (typeof document === 'undefined' || !document.cookie) {
    return undefined;
  }
  const match = document.cookie.match(/(^|;\s*)XSRF-TOKEN=([^;]+)/);
  if (!match) return undefined;
  try {
    return decodeURIComponent(match[2]);
  } catch {
    return match[2];
  }
}

export interface RequestOptions extends Omit<RequestInit, 'body'> {
  body?: unknown;
  timeout?: number;
  params?: Record<string, unknown> | URLSearchParams;
}

function buildUrl(url: string, params?: Record<string, unknown> | URLSearchParams): string {
  if (!params) return url;

  let searchParams: URLSearchParams;
  if (params instanceof URLSearchParams) {
    searchParams = new URLSearchParams(params);
  } else {
    searchParams = new URLSearchParams();
    for (const [key, value] of Object.entries(params)) {
      if (value !== undefined && value !== null) {
        if (Array.isArray(value)) {
          for (const item of value) {
            if (item !== undefined && item !== null) {
              searchParams.append(key, String(item));
            }
          }
        } else {
          searchParams.append(key, String(value));
        }
      }
    }
  }

  const queryString = searchParams.toString();
  if (!queryString) return url;

  const separator = url.includes('?') ? '&' : '?';
  return `${url}${separator}${queryString}`;
}

export async function request<T = unknown>(url: string, options: RequestOptions = {}): Promise<T> {
  const {
    method = 'GET',
    headers: customHeaders,
    body,
    timeout = 15000,
    signal: externalSignal,
    params,
    credentials = 'same-origin',
    ...restOptions
  } = options;

  const upperMethod = method.toUpperCase();
  const headers = new Headers(customHeaders);

  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json');
  }

  // Auto-inject CSRF token for state-mutating HTTP methods
  const mutationMethods = ['POST', 'PUT', 'DELETE', 'PATCH'];
  if (mutationMethods.includes(upperMethod)) {
    const csrfToken = getCsrfToken();
    if (csrfToken && !headers.has('X-XSRF-TOKEN')) {
      headers.set('X-XSRF-TOKEN', csrfToken);
    }
  }

  // Handle body formatting and Content-Type header
  let processedBody: BodyInit | null | undefined = undefined;
  if (body !== undefined && body !== null) {
    if (
      body instanceof FormData ||
      body instanceof Blob ||
      body instanceof ArrayBuffer ||
      body instanceof URLSearchParams
    ) {
      processedBody = body as BodyInit;
    } else if (typeof body === 'string') {
      processedBody = body;
      const trimmed = body.trim();
      if ((trimmed.startsWith('{') || trimmed.startsWith('[')) && !headers.has('Content-Type')) {
        headers.set('Content-Type', 'application/json');
      }
    } else {
      processedBody = JSON.stringify(body);
      if (!headers.has('Content-Type')) {
        headers.set('Content-Type', 'application/json');
      }
    }
  }

  // Abort signal and timeout coordination
  const controller = new AbortController();
  let isTimeoutAborted = false;
  let timerId: ReturnType<typeof setTimeout> | undefined;

  if (timeout > 0 && timeout <= 2147483647) {
    timerId = setTimeout(() => {
      isTimeoutAborted = true;
      controller.abort();
    }, timeout);
  }

  const handleExternalAbort = () => {
    controller.abort(externalSignal?.reason);
  };

  if (externalSignal) {
    if (externalSignal.aborted) {
      controller.abort(externalSignal.reason);
    } else {
      externalSignal.addEventListener('abort', handleExternalAbort);
    }
  }

  const finalUrl = buildUrl(url, params);

  try {
    const response = await fetch(finalUrl, {
      ...restOptions,
      method: upperMethod,
      headers,
      body: processedBody,
      credentials,
      signal: controller.signal,
    });

    if (!response.ok) {
      let errorData: ApiErrorResponse | undefined = undefined;
      try {
        const text = await response.text();
        if (text) {
          const trimmedText = text.trim();
          if (!trimmedText.startsWith('<')) {
            try {
              errorData = JSON.parse(text) as ApiErrorResponse;
            } catch {
              errorData = { message: text } as ApiErrorResponse;
            }
          }
        }
      } catch {
        // Ignorar falha na leitura do corpo
      }

      let errorMessage = '';
      if (errorData?.errors && Array.isArray(errorData.errors) && errorData.errors.length > 0) {
        const firstError = errorData.errors[0];
        if (typeof firstError === 'string') {
          errorMessage = firstError;
        } else if (firstError && typeof firstError === 'object') {
          errorMessage = firstError.defaultMessage || firstError.message || '';
        }
      }

      if (!errorMessage && errorData?.fieldErrors && typeof errorData.fieldErrors === 'object') {
        const fieldValues = Object.values(errorData.fieldErrors);
        if (fieldValues.length > 0 && typeof fieldValues[0] === 'string') {
          errorMessage = fieldValues[0];
        }
      }

      if (!errorMessage && errorData?.message) {
        errorMessage = errorData.message;
      }
      if (!errorMessage && errorData?.error) {
        errorMessage = errorData.error;
      }
      if (!errorMessage) {
        errorMessage = `Erro na requisição (${response.status}${response.statusText ? ': ' + response.statusText : ''})`;
      }

      throw new ApiError(errorMessage, response.status, errorData);
    }

    if (response.status === 204) {
      return undefined as T;
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      const text = await response.text();
      if (!text || !text.trim()) {
        return undefined as T;
      }
      try {
        return JSON.parse(text) as T;
      } catch {
        throw new ApiError('Resposta JSON inválida do servidor', response.status);
      }
    }

    const text = await response.text();
    return (text ? text : undefined) as unknown as T;
  } catch (error) {
    if (isTimeoutAborted) {
      throw new ApiError(`Tempo limite da requisição excedido (${timeout}ms)`, 408);
    }
    if (error instanceof ApiError) {
      throw error;
    }
    if (error instanceof Error && error.name === 'AbortError') {
      throw error;
    }
    const message = error instanceof Error ? error.message : 'Erro de rede desconhecido';
    throw new ApiError(message, 0);
  } finally {
    if (timerId !== undefined) {
      clearTimeout(timerId);
    }
    if (externalSignal) {
      externalSignal.removeEventListener('abort', handleExternalAbort);
    }
  }
}

export const apiClient = Object.assign(
  <T = unknown>(url: string, options?: RequestOptions) => request<T>(url, options),
  {
    get: <T = unknown>(url: string, options?: RequestOptions) =>
      request<T>(url, { ...options, method: 'GET' }),
    post: <T = unknown>(url: string, body?: unknown, options?: RequestOptions) =>
      request<T>(url, { ...options, method: 'POST', body }),
    put: <T = unknown>(url: string, body?: unknown, options?: RequestOptions) =>
      request<T>(url, { ...options, method: 'PUT', body }),
    delete: <T = unknown>(url: string, options?: RequestOptions) =>
      request<T>(url, { ...options, method: 'DELETE' }),
    patch: <T = unknown>(url: string, body?: unknown, options?: RequestOptions) =>
      request<T>(url, { ...options, method: 'PATCH', body }),
  }
);

export default apiClient;
