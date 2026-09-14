---
title: 'Story 1.4: Correção do Botão Sair (UX e Header)'
type: 'bugfix'
created: '2026-08-18'
status: 'done'
baseline_commit: 'c976877eee7d719bfa12eefdf213609fd326b3c7'
review_loop_iteration: 0
context: ['_bmad-output/implementation-artifacts/epic-1-context.md']
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O botão de 'Sair' estava localizado na navegação principal (Pill Navigation / Bottom Nav), o que causava sujeira visual e fugia dos padrões convencionais onde opções de conta ficam vinculadas ao perfil do usuário.

**Approach:** Remover o botão 'Sair' da `Navigation`. Criar um componente `Header` no topo da tela que exibirá a foto de perfil recebida do Google (canto superior direito). Ao clicar na foto, um menu (Dropdown/Bottom Sheet) exibirá a opção "Sair", acionando o fluxo de logout já existente. A foto de perfil precisará ser salva no backend e exposta pelo endpoint `/api/auth/me`.

## Boundaries & Constraints

**Always:**
- Manter o comportamento técnico de limpeza de sessão local (Zustand e React Query) e backend idênticos ao que já foi implementado.
- Extrair a foto de perfil (claim `picture`) no login OAuth2 e disponibilizá-la no payload do `/api/auth/me`.
- Garantir que o `Header` se adapte tanto ao layout desktop quanto mobile.

**Ask First:**
- Modificar o roteamento ou a estrutura principal do `App.tsx` além da adição do `<Header />`.

**Never:**
- Alterar as cores ou a tipografia padrão já definidos para o Épico 1 sem consultar o guia de estilos visual.
- Quebrar os testes existentes.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Exibição do Header | Usuário autenticado com foto de perfil válida do Google | Header exibe a foto do perfil no canto superior direito | Foto de fallback / Initials caso URL seja vazia/inválida |
| Logout via Dropdown | Usuário clica na foto e depois em "Sair" | Sessão encerrada e redirecionamento para `/login` (fluxo atual) | Mesmo fallback local do `Navigation` atual |

</frozen-after-approval>

## Code Map

- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/Usuario.java` -- Entidade precisa de novo atributo `fotoPerfil`
- `backend/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java` -- Capturar claim `picture` do `OAuth2User` e salvar no `Usuario`
- `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java` -- O endpoint `/me` precisa retornar a `fotoPerfil` a partir do `Usuario` do banco ou cache, ou então o ID para buscar (mas o Auth só retorna `subjectId` hoje. Precisa buscar no repositório)
- `frontend/src/store/authStore.ts` -- Estado de autenticação precisa de `profileImage` string (além do `subjectId`)
- `frontend/src/App.tsx` -- Receber o `profileImage` do fetch no `/me` e atualizar a `authStore`. Adicionar `<Header />` dentro das rotas protegidas, removendo a importação caso necessário do Sair.
- `frontend/src/components/Navigation/Navigation.tsx` -- Remover lógica de logout e UI do botão Sair
- `frontend/src/components/Header/Header.tsx` (novo) -- Implementar UI do perfil, dropdown e lógica do botão Sair.

## Tasks & Acceptance

**Execution:**
- [ ] `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/Usuario.java` -- Adicionar campo `fotoPerfil` (String) na entidade. -- Rationale: armazenar a URL da foto do Google.
- [ ] `backend/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java` -- Extrair `picture` do Google e salvar em `Usuario`. -- Rationale: a foto será capturada a cada login.
- [ ] `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java` -- Injetar `UsuarioRepository` (se necessário) para retornar `{ "subjectId": "...", "fotoPerfil": "..." }` no endpoint `/me`. -- Rationale: o frontend precisa exibir a foto.
- [ ] `frontend/src/store/authStore.ts` -- Adicionar `profileImage` ao estado e método `setAuth`. -- Rationale: disponibilizar foto para o Header.
- [ ] `frontend/src/components/Navigation/Navigation.tsx` -- Remover lógica de logout e o botão. -- Rationale: o botão mudou de lugar.
- [ ] `frontend/src/components/Header/Header.tsx` -- Criar componente exibindo foto do perfil no canto superior direito. Ao clicar, mostrar menu dropdown com botão "Sair". O botão executa fetch `/api/auth/logout`, limpa cache e Zustand. -- Rationale: nova localização do logout.
- [ ] `frontend/src/App.tsx` -- Atualizar fetch do `/me` para passar `profileImage` para `setAuth`. Incluir componente `<Header />` acima do `main` dentro do `ProtectedRoute`. -- Rationale: Header precisa ser visível nas rotas protegidas.

**Acceptance Criteria:**
- Given que o usuário está autenticado na interface principal
- When ele visualiza o canto superior direito da tela (no Header mobile ou no Desktop)
- Then deve ser exibida a sua foto de perfil recebida através da conta do Google
- When o usuário clica na sua foto de perfil
- Then um menu dropdown (ou Bottom Sheet no Mobile) é aberto exibindo a opção de "Sair"
- Given que o usuário clica em "Sair"
- Then o backend invalida o Cookie de sessão e o frontend limpa todos os dados em cache (Zustand e React Query)
- And o usuário é redirecionado para a tela de login inicial sem retenção de estado anterior

## Verification

**Commands:**
- `cd backend && ./mvnw test` -- expected: O contexto do Spring Boot sobe e os testes passam.
- `cd frontend && npm run build` -- expected: Compilação TypeScript do Vite conclui sem erros no novo layout.

## Suggested Review Order

**Backend Data Flow**

- Adiciona campo de URL da imagem no banco de dados
  [`Usuario.java:23`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/domain/Usuario.java#L23)

- Extrai claim de profile picture no login via Google e grava
  [`OAuth2LoginSuccessHandler.java:30`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java#L30)

- Retorna a foto do perfil no endpoint /me
  [`AuthController.java:32`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java#L32)

**Frontend Data & UX Fix**

- Estado global (Zustand) atualizado com profileImage
  [`authStore.ts:24`](../../frontend/src/store/authStore.ts#L24)

- Injeção da foto no bootstrap e inclusão do Header no Router
  [`App.tsx:24`](../../frontend/src/App.tsx#L24)

- Novo header posicionado no topo com dropdown do botão Sair
  [`Header.tsx:8`](../../frontend/src/components/Header/Header.tsx#L8)

- Botão Sair removido da barra inferior
  [`Navigation.tsx:32`](../../frontend/src/components/Navigation/Navigation.tsx#L32)
