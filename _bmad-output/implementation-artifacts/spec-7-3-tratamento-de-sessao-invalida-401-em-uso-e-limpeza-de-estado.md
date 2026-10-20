---
title: 'Story 7.3: Tratamento de Sessão Inválida (401 em Uso) e Limpeza de Estado Privado no Logout (Frontend)'
type: 'feature'
created: '2026-10-20'
status: 'done'
baseline_commit: 'ad7cf515dd7a4701e00a3a1b0a045cba2fc58d7a'
review_loop_iteration: 0
context:
  - '_bmad-output/implementation-artifacts/epic-7-context.md'
  - '_bmad-output/specs/spec-epico-7/SPEC.md'
  - '_bmad-output/specs/spec-epico-7/seguranca.md'
  - '_bmad-output/specs/spec-epico-7/rastreabilidade.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O cliente frontend não possui tratamento centralizado para respostas HTTP 401 durante o uso da aplicação (DW-60), deixando dados privados em cache e estado de autenticação em memória caso a sessão expire ou seja invalidada, além de carecer de verificação automatizada dedicada para o fluxo de logout e limpeza de estado sob sucesso e sob falha de rede no `Header.tsx` (DW-26).

**Approach:** Implementar no `apiClient.ts` um mecanismo de interceptação central para respostas 401 que aciona a invalidação da sessão local, limpando o `authStore`, esvaziando o cache privado do `QueryClient` e redirecionando para a rota de login `/login`, mantendo estritamente preservados os contratos de erros 403 (Forbidden), 404 (Not Found) e demais status; criar testes unitários para o `Header.tsx` comprovando a limpeza de estado e cache tanto no logout normal quanto em erro de rede; e atualizar os registros DW-26 e DW-60 no deferred-work.md com evidências e distinção explícita entre limpeza local no cliente e revogação no servidor (SEG-09).

## Boundaries & Constraints

**Always:**
- **Escopo rastreado:** `CAP-3` (`DW-26`, `DW-60`) | **Action items:** `nenhum`.
- Tratar centralmente HTTP 401 em chamadas durante o uso no `apiClient.ts` (ou handler integrado), garantindo invalidação de `authStore` (`logout()`), limpeza integral do cache do `QueryClient` (`queryClient.clear()`) e retorno à tela de login (`/login`) (`SEG-08`).
- Preservar integralmente os contratos e o lançamento de `ApiError` para outros status de erro HTTP no `apiClient.ts`, em especial 403 (acesso negado/vínculo inválido), 404 (recurso não encontrado) e 400 (erros de validação), sem disparar limpeza de sessão ou redirecionamento involuntário (`SEG-08`).
- Testar e garantir que no logout em `Header.tsx`, o `QueryClient` e o `authStore` sejam limpos tanto no caminho feliz (resposta 200 no backend) quanto em falha de rede/exceção (catch com toast informativo), redirecionando sempre para `/login` (`SEG-07`).
- Registrar expressamente nos registros de rastreabilidade a distinção entre limpeza local de dados no cliente e revogação/expiração de token no servidor (a limpeza local com falha de rede não prova nem substitui a revogação no backend) (`SEG-09`).
- Atualizar `DW-26` e `DW-60` em `_bmad-output/implementation-artifacts/deferred-work.md` com data ISO, evidências de testes, revisão e encerramento antes de marcar a história como `done`.

**Ask First:**
- Qualquer alteração em endpoints backend de autenticação ou adição de novas dependências externas ao frontend.

**Never:**
- Não adicionar suíte genérica de testes E2E, testes de avatar/imagem de perfil ou testes visuais cosméticos fora do escopo estrito de limpeza e invalidação de sessão (conforme explicitamente vetado em `seguranca.md`).
- Não alterar os contratos de retorno de erro 403 e 404 nos hooks ou componentes existentes.
- Não inserir comentários no código-fonte com referências a épicos, stories ou IDs de backlog (`DW-*`).

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Logout com sucesso (`SEG-07`, `DW-26`) | Usuário autenticado clica em "Sair"; `POST /api/auth/logout` responde 200 | `apiClient.post('/api/auth/logout')` concluído; `queryClient.clear()` executado; `authStore.logout()` executado; redirecionamento para `/login` | Sem erros; estado local limpo |
| Logout com falha de rede (`SEG-07`, `DW-26`) | Usuário autenticado clica em "Sair"; rede ou API indisponível | Toast de erro exibido ("Sessão encerrada localmente. (Erro de rede)"); `queryClient.clear()` executado; `authStore.logout()` executado; navegação para `/login` | `finally` garante limpeza local e navegação |
| Sessão inválida durante uso (`SEG-08`, `DW-60`) | Aplicação em uso (`isAuthenticated: true`), chamada de API autenticada recebe HTTP 401 | Lança `ApiError` (status 401); dispara invalidação de sessão local: `authStore.logout()`, `queryClient.clear()` e redirecionamento para `/login` | Tratamento centralizado sem vazamento de cache |
| Erro 403 Forbidden durante uso (`SEG-08`) | Chamada de API retorna HTTP 403 (ex: CSRF inválido ou vínculo alheio) | Lança `ApiError` (status 403); NÃO desloga usuário, NÃO limpa `queryClient`, NÃO redireciona para `/login` | Erro repassado para o chamador/componente tratar |
| Erro 404 Not Found durante uso (`SEG-08`) | Chamada de API retorna HTTP 404 (recurso inexistente) | Lança `ApiError` (status 404); NÃO desloga usuário, NÃO limpa `queryClient`, NÃO redireciona para `/login` | Erro repassado para o chamador/componente tratar |
| Inicialização anônima (`/api/auth/me`) | Usuário anônimo carrega a aplicação; `/api/auth/me` inicial retorna 401 | `App.tsx` define `setAuth(false)` e `setLoading(false)` e `ProtectedRoute` redireciona para `/login` sem loops ou efeitos colaterais anômalos | Sem alertas indevidos de sessão expirada |

</frozen-after-approval>

## Code Map

- `frontend-web/src/services/apiClient.ts` -- Adicionar handler central de 401 (`setUnauthorizedHandler` / disparo ao receber `response.status === 401`), mantendo o lançamento normal de `ApiError` e a integridade de requisições 403, 404, 400 e 500 (linhas 176–227).
- `frontend-web/src/components/Header/Header.tsx` -- Manter limpeza garantida em `finally` via `queryClient.clear()`, `logout()` e `navigate('/login')`, além de exibição de toast em falha de rede no logout (linhas 23–36).
- `frontend-web/src/App.tsx` -- Registrar no ciclo de vida da aplicação o interceptor de 401 do `apiClient` conectando `authStore.logout()`, `queryClient.clear()` e navegação para `/login` ao detectar sessão inválida durante o uso (linhas 44–68).
- `frontend-web/src/components/Header/Header.test.tsx` -- Novo teste unitário do `Header` verificando logout bem-sucedido, logout com erro de rede (toast, limpeza do queryClient e do authStore, redirecionamento para /login) e proteção contra múltiplos cliques concorrentes (`DW-26`, `SEG-07`).
- `frontend-web/src/services/apiClient.test.ts` -- Testes unitários para `apiClient` cobrindo disparo do handler central em respostas 401 e garantia de que respostas 403, 404, 400 e 500 lançam `ApiError` normalmente sem acionar o handler de 401 (`DW-60`, `SEG-08`).
- `frontend-web/src/App.test.tsx` -- Teste de integração de rota e sessão comprovando que uma chamada de API respondendo 401 durante o uso invalida a sessão local, limpa o cache do React Query e redireciona para `/login` (`DW-60`, `SEG-08`).
- `_bmad-output/implementation-artifacts/deferred-work.md` -- Atualizar os registros `DW-26` e `DW-60` com data ISO, evidências de testes, revisão e fechamento com distinção explícita entre limpeza cliente e revogação servidor (`SEG-09`).

## Tasks & Acceptance

**Execution:**
- [x] `frontend-web/src/services/apiClient.ts` -- Implementar registro e disparo de handler central para respostas 401 mantendo lançamento de `ApiError` e contratos inalterados para 403/404/demais status -- Habilitar detecção centralizada de sessão inválida (`DW-60`, `SEG-08`)
- [x] `frontend-web/src/App.tsx` -- Conectar o handler central de 401 do `apiClient` para acionar `authStore.logout()`, esvaziar `queryClient.clear()` e redirecionar para `/login` quando ocorrer 401 durante o uso -- Garantir descarte imediato de dados em memória e saída segura (`DW-60`, `SEG-08`)
- [x] `frontend-web/src/components/Header/Header.test.tsx` -- Criar testes unitários para `Header` cobrindo logout com sucesso e com erro de rede, verificando limpeza de cache, `authStore` e navegação para `/login` -- Comprovar garantias de logout e falha de rede (`DW-26`, `SEG-07`)
- [x] `frontend-web/src/services/apiClient.test.ts` -- Adicionar testes para o handler de 401, comprovando disparo em 401 e não disparo em 403, 404, 400 e 500 com lançamento de `ApiError` preservado -- Validar isolamento de contratos HTTP (`DW-60`, `SEG-08`)
- [x] `frontend-web/src/App.test.tsx` -- Adicionar teste de integração simulando sessão em uso que recebe 401, verificando invalidação de estado, limpeza de cache privado e retorno ao login -- Assegurar comportamento integrado de sessão expirada (`DW-60`, `SEG-08`)
- [x] `_bmad-output/implementation-artifacts/deferred-work.md` -- Atualizar registros `DW-26` e `DW-60` com data ISO, evidências, revisão e encerramento com distinção cliente/servidor -- Cumprir rastreabilidade obrigatória (`SEG-09`)

**Acceptance Criteria:**
- Given uma sessão autenticada com dados privados carregados em cache (`queryClient`), when o usuário aciona o logout pelo menu e o backend responde sucesso (200) ou falha de rede/erro, then em ambos os casos o cache do `queryClient` deve ser totalmente limpo, o `authStore` redefinido para não autenticado e o usuário redirecionado para `/login` (`SEG-07`, `DW-26`).
- Given a aplicação Aureus em uso com usuário autenticado e dados em memória, when qualquer requisição autenticada do `apiClient` receber status HTTP 401, then o handler central deve ser acionado, invalidando a sessão local no `authStore`, limpando todos os dados privados do `queryClient` e conduzindo o usuário de volta à rota `/login` (`SEG-08`, `DW-60`).
- Given uma requisição realizada pelo `apiClient`, when o backend responder com HTTP 403 (Forbidden), 404 (Not Found) ou 400 (Bad Request), then o `apiClient` deve lançar `ApiError` com status correspondente e os dados da resposta, sem disparar limpeza de autenticação, sem limpar o `queryClient` e sem redirecionar para `/login` (`SEG-08`).

## Spec Change Log

- 2026-10-20: Criação inicial do plano da Story 7.3 cobrindo DW-26 e DW-60 (CAP-3).
- 2026-10-20: Revisão em 3 camadas concluída com 5 patches aplicados: adição de `skipUnauthorizedHandler` em `RequestOptions`, restrição same-origin para acionamento do handler no `apiClient.ts`, cancelamento de queries (`queryClient.cancelQueries()`) e navegação `{ replace: true }` no `App.tsx`, `skipUnauthorizedHandler` no logout explícito do `Header.tsx`, testes unitários adicionais em `apiClient.test.ts` e isolamento do handler com asserção estendida de login em `App.test.tsx`.

## Verification

**Commands:**
- `npm test --prefix frontend-web` -- expected: `Test Files passed` com 100% dos testes verdes, incluindo novos testes de `Header`, `apiClient` e sessão 401.
- `npm run build --prefix frontend-web` -- expected: Build TypeScript/Vite concluído com sucesso sem erros de compilação.

## Suggested Review Order

**Interceptação Central de 401 e Contratos HTTP**

- Definição do handler central e checagem estrita para origens locais com bypass seguro.
  [`apiClient.ts:234`](../../frontend-web/src/services/apiClient.ts#L234)

- Configuração de registro e exportação no cliente HTTP compartilhado da aplicação.
  [`apiClient.ts:34`](../../frontend-web/src/services/apiClient.ts#L34)

**Ciclo de Vida da Aplicação e Invalidação de Sessão**

- Registro no App do handler central com cancelamento de queries, limpeza e substituição de histórico.
  [`App.tsx:47`](../../frontend-web/src/App.tsx#L47)

- Proteção na carga anônima inicial para ignorar o handler de sessão em uso.
  [`App.tsx:65`](../../frontend-web/src/App.tsx#L65)

- Prevenção de duplo disparo de invalidação durante o logout explícito do cabeçalho.
  [`Header.tsx:27`](../../frontend-web/src/components/Header/Header.tsx#L27)

**Testes Automatizados e Garantias de Isolamento**

- Validação unitária do fluxo de logout bem-sucedido e sob falha de conexão com toast.
  [`Header.test.tsx:46`](../../frontend-web/src/components/Header/Header.test.tsx#L46)

- Validação da preservação de erros 403, 404, 400 e 500 sem disparar logout indevido.
  [`apiClient.test.ts:611`](../../frontend-web/src/services/apiClient.test.ts#L611)

- Teste integrado de invalidação e descarte de dados privados em memória após 401.
  [`App.test.tsx:119`](../../frontend-web/src/App.test.tsx#L119)

**Rastreabilidade e Governança de Segurança**

- Fechamento formal de DW-26 e DW-60 com comprovação e distinção cliente/servidor.
  [`deferred-work.md:80`](deferred-work.md#L80)
