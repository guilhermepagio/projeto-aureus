---
title: 'Story 5.3: Centralização do Cliente HTTP/API e Resiliência no Frontend'
type: 'refactor'
created: '2026-09-27'
status: 'done'
baseline_commit: 'bc68d4bb12936ebf8e9577abdc78bddb664644e6'
review_loop_iteration: 1
context:
  - '_bmad-output/implementation-artifacts/epic-5-context.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** As chamadas HTTP no frontend estão dispersas em múltiplos hooks e componentes com fetch raw duplicado, parsing redundante de cookies CSRF (`XSRF-TOKEN`), tratamento inconsistente de erros e ausência de timeout padronizado. Além disso, a aplicação não possui um ErrorBoundary global na raiz, fazendo com que qualquer erro inesperado de renderização resulte em tela branca completa.

**Approach:** Criar um cliente HTTP centralizado (`apiClient`) com injeção automática de CSRF para mutações, configuração padrão de headers/credenciais, timeout via AbortSignal e extração de erros estruturados compatíveis com o `GlobalExceptionHandler` da Story 5.2. Refatorar todos os hooks de dados para utilizá-lo e envelopar a aplicação em um `ErrorBoundary` global com interface amigável de recuperação.

## Boundaries & Constraints

**Always:**
- Todas as chamadas para as rotas `/api/*` devem transitar exclusivamente pelo `apiClient`, eliminando invocações diretas de `fetch` e parsing redundante de cookies dispersos nos hooks.
- O `apiClient` deve anexar automaticamente o cabeçalho `X-XSRF-TOKEN` lido do cookie `XSRF-TOKEN` em requisições de mutação (`POST`, `PUT`, `DELETE`, `PATCH`).
- Em caso de respostas com status HTTP de erro (`!response.ok`), o `apiClient` deve extrair a mensagem estruturada (`ApiErrorResponse.errors[0].defaultMessage`, `message`, `error` ou fallback por status) e lançar um `ApiError` tipado.
- Suportar timeout abortável (padrão de 15s) respeitando sinais externos de cancelamento (`AbortSignal`).
- O `ErrorBoundary` global deve capturar falhas em componentes React, exibir uma UI informativa com botão de "Tentar Novamente" e evitar a quebra em tela branca.
- Manter a compatibilidade com todos os testes existentes da suíte do frontend.

**Ask First:**
- Alterações na assinatura pública ou no tipo de retorno dos hooks customizados que afetem componentes de tela existentes.
- Modificações em contratos de rotas de backend `/api/*`.

**Never:**
- Não duplicar funções utilitárias de leitura de cookie ou parsing de CSRF nos hooks ou componentes.
- Não silenciar erros capturados pelo `ErrorBoundary` sem registrar em log (`console.error`).
- Não adicionar dependências externas desnecessárias no `package.json` (usar fetch nativo e React padrão).

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Requisição GET com sucesso | `apiClient.get<Conta[]>('/api/contas')` | Retorna lista parseada com status 200 | N/A |
| Mutação POST/PUT/DELETE com CSRF | `apiClient.post('/api/contas', body)` | Cabeçalhos `Content-Type: application/json` e `X-XSRF-TOKEN` enviados automaticamente | Se cookie CSRF ausente ou inválido, backend responde 403 que é tratado |
| Erro de validação da API (400) | Resposta backend com `ApiErrorResponse` (`errors[0].defaultMessage = "Descrição obrigatória"`) | Lança `ApiError` com mensagem "Descrição obrigatória", `status: 400` e dados completos | Capturado pelo `onError` da mutation e exibido via toast |
| Erro de integridade/regra (400/403/404) | Resposta backend com `ApiErrorResponse` (`message = "Registro possui vínculos"`) | Lança `ApiError` com mensagem extraída e `status: 400` | Toast com mensagem explicativa |
| Timeout da requisição | Servidor demora mais que o timeout configurado (ex.: 15s) | Requisição cancelada via `AbortController` | Lança `ApiError` com timeout informado |
| Erro de renderização em componente React | Componente filho lança exceção síncrona durante render | `ErrorBoundary` intercepta o erro e renderiza UI de fallback limpa com botão "Tentar Novamente" | Log no console e estado de erro resetável |

</frozen-after-approval>

## Code Map

- `frontend/src/services/apiClient.ts` -- Novo módulo de cliente HTTP centralizado (funções get, post, put, delete, tratamento de CSRF, timeout e extração de `ApiError`)
- `frontend/src/components/ErrorBoundary/ErrorBoundary.tsx` -- Novo componente React `ErrorBoundary` com UI de fallback elegante e botão "Tentar Novamente"
- `frontend/src/hooks/useContas.ts` -- Refatorar para substituir fetch raw e `getCsrfToken` local por `apiClient`
- `frontend/src/hooks/useCategorias.ts` -- Refatorar para substituir fetch raw e `getCsrfToken` local por `apiClient`
- `frontend/src/hooks/useDespesasFixas.ts` -- Refatorar para substituir fetch raw e `getCsrfToken` local por `apiClient`
- `frontend/src/hooks/useReceitasFixas.ts` -- Refatorar para substituir fetch raw e `getCsrfToken` local por `apiClient`
- `frontend/src/hooks/useDespesasVariaveis.ts` -- Refatorar para substituir fetch raw e `getCsrfToken` local por `apiClient`
- `frontend/src/hooks/useReceitasVariaveis.ts` -- Refatorar para substituir fetch raw e `getCsrfToken` local por `apiClient`
- `frontend/src/hooks/useConsolidacao.ts` -- Refatorar para substituir fetch raw por `apiClient`
- `frontend/src/hooks/useConsolidacaoCategoria.ts` -- Refatorar para substituir fetch raw por `apiClient`
- `frontend/src/App.tsx` -- Integrar `ErrorBoundary` envolvendo o conteúdo e refatorar `/api/auth/me` para `apiClient`
- `frontend/src/components/Header/Header.tsx` -- Refatorar `/api/auth/logout` para `apiClient`
- `frontend/src/services/apiClient.test.ts` -- Testes unitários para o `apiClient` (headers, CSRF, status, parsing, timeout e ApiError)
- `frontend/src/components/ErrorBoundary/ErrorBoundary.test.tsx` -- Testes unitários para o `ErrorBoundary` (renderização normal, captura de erro e recuperação via retry)

## Tasks & Acceptance

**Execution:**
- [x] `frontend/src/services/apiClient.ts` -- Implementar cliente HTTP centralizado com injeção automática de CSRF, extração de ApiError e suporte a timeout -- Elimina duplicação de chamadas raw e padroniza contratos HTTP
- [x] `frontend/src/components/ErrorBoundary/ErrorBoundary.tsx` -- Implementar ErrorBoundary global com UI de fallback limpa e ação de retry -- Previne tela branca na quebra de componentes React
- [x] `frontend/src/hooks/useContas.ts` e `frontend/src/hooks/useCategorias.ts` -- Refatorar fetchers para consumir `apiClient` e remover funções locais de CSRF -- Centralização da comunicação e simplificação dos hooks
- [x] `frontend/src/hooks/useDespesasFixas.ts` e `frontend/src/hooks/useReceitasFixas.ts` -- Refatorar fetchers para consumir `apiClient` e simplificar extração de mensagens de erro -- Elimina parsing redundante e alinha com ApiError
- [x] `frontend/src/hooks/useDespesasVariaveis.ts` e `frontend/src/hooks/useReceitasVariaveis.ts` -- Refatorar fetchers para consumir `apiClient` e padronizar mutações -- Consistência em lançamentos variáveis
- [x] `frontend/src/hooks/useConsolidacao.ts` e `frontend/src/hooks/useConsolidacaoCategoria.ts` -- Refatorar fetchers para consumir `apiClient` -- Padronização das queries de consolidação
- [x] `frontend/src/App.tsx` e `frontend/src/components/Header/Header.tsx` -- Envelopar aplicação com `ErrorBoundary` e refatorar chamadas de autenticação (`/api/auth/me` e `/api/auth/logout`) para `apiClient` -- Resiliência global e cobertura total de APIs
- [x] `frontend/src/services/apiClient.test.ts` -- Implementar testes unitários para o `apiClient` cobrindo CSRF, extração de erros, status HTTP e timeout -- Validação automatizada do cliente HTTP
### Review Findings

- [x] [Review][Patch] Prevenir race condition e logout acidental no check de autenticação ao desmontar [frontend/src/App.tsx:58]
- [x] [Review][Patch] Corrigir validação de alteração de resetKeys no ErrorBoundary quando tamanho do array diminui [frontend/src/components/ErrorBoundary/ErrorBoundary.tsx:34]
- [x] [Review][Patch] Configurar resetKeys com location.pathname no ErrorBoundary raiz de App.tsx para transições de rotas [frontend/src/App.tsx:70]
- [x] [Review][Patch] Preservar AbortError e reason em cancelamento externo e fortalecer asserção no teste unitário [frontend/src/services/apiClient.ts:241]
- [x] [Review][Patch] Adicionar teste de integração garantindo captura de falha de renderização pelo ErrorBoundary no App.test.tsx [frontend/src/App.test.tsx:43]
- [x] [Review][Patch] Tratar fragmento hash (#) e separadores residuais na construção de query string em buildUrl [frontend/src/services/apiClient.ts:75]
- [x] [Review][Patch] Envolver callback onError do ErrorBoundary em bloco defensivo try/catch [frontend/src/components/ErrorBoundary/ErrorBoundary.tsx:29]
- [x] [Review][Patch] Restringir injeção de cabeçalho CSRF a requisições de mesma origem ou relativas [frontend/src/services/apiClient.ts:100]
- [x] [Review][Patch] Migrar interpolação manual de mesAno nos hooks de consolidação para a propriedade params do apiClient [frontend/src/hooks/useConsolidacao.ts:18]
- [x] [Review][Patch] Extração defensiva de mensagens em fieldErrors com arrays e conversão estrita de message para string [frontend/src/services/apiClient.ts:195]
- [x] [Review][Defer] Integrar ErrorBoundary com QueryErrorResetBoundary do TanStack Query para limpeza de cache [frontend/src/components/ErrorBoundary/ErrorBoundary.tsx:31] — deferred, pre-existing

**Acceptance Criteria:**
- Given a aplicação frontend configurada, when qualquer mutação HTTP (`POST`, `PUT`, `DELETE`) for disparada, then o cabeçalho `X-XSRF-TOKEN` extraído do cookie `XSRF-TOKEN` deve ser incluído automaticamente na requisição
- Given uma resposta de erro da API backend (`status >= 400`), when processada pelo `apiClient`, then o cliente deve extrair a mensagem estruturada correspondente (`errors[0].defaultMessage`, `message` ou `error`) e lançar um `ApiError`
- Given uma requisição que exceda o tempo limite estipulado, when o timeout for atingido, then o `apiClient` deve abortar a conexão e lançar erro de timeout descritivo
- Given todos os hooks de movimentações, contas, categorias e consolidação, when realizarem operações de leitura ou mutação, then devem consumir exclusivamente o `apiClient` centralizado sem invocações diretas de `fetch` ou parsing manual de cookie
- Given uma falha inesperada de renderização em qualquer componente da árvore, when o erro for disparado, then o `ErrorBoundary` deve capturar o problema, registrar o log e exibir a interface de fallback com botão de "Tentar Novamente"

## Verification

**Commands:**
- `npm test` -- expected: Todos os testes unitários do frontend executam e passam com sucesso
- `npm run build` -- expected: Compilação TypeScript e build do Vite finalizam com sucesso sem erros

## Suggested Review Order

**Cliente HTTP Centralizado**

- Entry point: núcleo do fetcher wrapper com CSRF automático, extração de ApiError e timeout
  [`apiClient.ts:50`](../../frontend/src/services/apiClient.ts#L50)

- Definições de tipagem do erro da API e classe ApiError customizada
  [`apiClient.ts:17`](../../frontend/src/services/apiClient.ts#L17)

- Utilitário de parsing e extração do cookie CSRF
  [`apiClient.ts:30`](../../frontend/src/services/apiClient.ts#L30)

**Resiliência e Fallback Global**

- Componente ErrorBoundary global capturando falhas de renderização com botão de retry e resetKeys
  [`ErrorBoundary.tsx:16`](../../frontend/src/components/ErrorBoundary/ErrorBoundary.tsx#L16)

- Integração do ErrorBoundary na raiz da aplicação e proteção do ciclo de vida auth/me
  [`App.tsx:43`](../../frontend/src/App.tsx#L43)

**Refatoração dos Hooks de Consumo de Dados**

- Eliminação de fetch raw e uso do apiClient nas mutações e queries de contas
  [`useContas.ts:18`](../../frontend/src/hooks/useContas.ts#L18)

- Migração das chamadas de categorias para o apiClient centralizado
  [`useCategorias.ts:18`](../../frontend/src/hooks/useCategorias.ts#L18)

- Consumo unificado do apiClient em lançamentos fixos de despesas e receitas
  [`useDespesasFixas.ts:31`](../../frontend/src/hooks/useDespesasFixas.ts#L31)

- Consumo unificado do apiClient em lançamentos variáveis com parâmetros e DTOs
  [`useDespesasVariaveis.ts:40`](../../frontend/src/hooks/useDespesasVariaveis.ts#L40)

- Queries de consolidação temporal consumindo apiClient de forma limpa
  [`useConsolidacao.ts:18`](../../frontend/src/hooks/useConsolidacao.ts#L18)

- Refatoração do logout no cabeçalho eliminando parsing manual de cookie
  [`Header.tsx:28`](../../frontend/src/components/Header/Header.tsx#L28)

**Testes Automatizados**

- Suíte de testes unitários do apiClient cobrindo CSRF, timeout, PUT, erros e params
  [`apiClient.test.ts:7`](../../frontend/src/services/apiClient.test.ts#L7)

- Suíte de testes unitários do ErrorBoundary cobrindo interceptação de erro e recuperação
  [`ErrorBoundary.test.tsx:9`](../../frontend/src/components/ErrorBoundary/ErrorBoundary.test.tsx#L9)
