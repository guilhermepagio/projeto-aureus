---
title: 'Story 1.1: Esqueleto Visual e Barra de Navegação'
type: 'feature'
created: '2026-08-17'
status: 'done'
baseline_commit: '92f6fa1bb6b29bf2d568deec9753f24dbe8b1740'
review_loop_iteration: 0
context: ['_bmad-output/implementation-artifacts/epic-1-context.md']
---

<frozen-after-approval>

## Description
O esqueleto visual constitui a base responsiva da interface (Desktop e Mobile) seguindo os requisitos UX definidos (cores, tipografia). A barra de navegação principal permite o acesso fluido às grandes áreas funcionais sem refresh completo da página, utilizando um framework de roteamento single-page (SPA). Essa história entrega a UI primária estática e os recortes visuais de navegação.

## Constraints & Requirements
- **FR32:** Interface responsiva adaptável a desktop e mobile.
- **UX-DR1 / DR2 / DR3 / DR4:** Utilizar cores primárias (`Deep Teal`) e secundárias (`Warm Amber`), tipografia (`Plus Jakarta Sans`), bordas arredondadas e dados tabulares (`tabular-nums`) para consistência.
- **UX-DR5:** Bottom Navigation com 5 ícones (Mobile) e Pill Navigation (Desktop).
- **Roteamento:** A navegação deve direcionar para as telas (que, nesta story, serão telas vazias/placeholders) de `Consolidação`, `Despesas Variáveis`, `Despesas Fixas`, `Receitas Variáveis` e `Receitas Fixas`.
- **Arquitetura:** Frontend React com Vite e roteamento via `react-router-dom`. CSS Vanilla (sem Tailwind).

## I/O & Edge-Case Matrix

| State/Input | Context | Expected Output | Testing / Validation |
| --- | --- | --- | --- |
| Resolução `max-width: 768px` (Mobile) | Navegação visível na viewport | Barra de navegação na parte inferior fixada, estilo Bottom Bar | Ajustar tela/Device Mode |
| Resolução `min-width: 769px` (Desktop) | Navegação visível na viewport | Barra de navegação na parte superior, em formato de "Pill" centralizado | Ajustar tela/Device Mode |
| Renderização Global | Inicialização da aplicação | Cores (Deep Teal) e Fonte Plus Jakarta Sans presentes nativamente, sem estilo-base do Vite | Inspecionar Elemento |

</frozen-after-approval>

## Spec Change Log

*No changes yet.*

## Tasks & Acceptance

**Execution:**
- [x] `frontend/index.html` -- Adicionar `<link>` do Google Fonts para a fonte Plus Jakarta Sans -- Requisito de UX e Branding.
- [x] `frontend/src/index.css` -- Limpar estilos antigos do root Vite e declarar variáveis/tokens base (`--color-primary`, `--font-sans`, `--radius-md`, etc) com `font-variant-numeric: tabular-nums` -- Garantir coerência visual.
- [x] `frontend/src/components/Navigation/Navigation.tsx` -- Criar barra de navegação principal com 5 links, aplicando classes responsivas -- Construir a UI de roteamento principal.
- [x] `frontend/src/components/Navigation/Navigation.css` -- Aplicar os estilos do Pill (Desktop) e Bottom Bar (Mobile) usando CSS Grid/Flexbox e Media Queries -- Adequar ao tamanho de tela do usuário de forma fluida.
- [x] `frontend/src/App.tsx` -- Substituir placeholder inicial pela estrutura de Shell contendo `Navigation` e placeholders de conteúdo para cada aba (React Router) -- Compor a página principal.

**Acceptance Criteria:**
- Given que o usuário acessa o sistema via Desktop ou Mobile, when a interface básica é renderizada, then os tokens de cores, tipografia (Plus Jakarta Sans, tabular-nums) e bordas arredondadas devem estar aplicados visualmente.
- Given o layout renderizado, when o dispositivo é Desktop, then deve haver uma "Pill Navigation" superior.
- Given o layout renderizado, when o dispositivo é Mobile, then deve haver uma "Bottom Navigation" fixada na base.

## Verification
- [x] Verificação Visual Manual: Ajustar o tamanho da janela entre >768px e <=768px e validar a transição Pill <-> Bottom Bar.
- [x] Verificação Visual Manual: Navegar entre os 5 links e garantir a atualização da rota no navegador sem recarregar a página (comportamento de SPA).
- [x] O código segue os padrões do `frontend/src/index.css` sem importar Tailwind.
- [x] A barra de navegação funciona de forma responsiva sem "quebras" nos itens numéricos.

## Suggested Review Order

**Tokens e Estilos Base**

- Adição da fonte Plus Jakarta Sans no HTML principal.
  [`index.html:4`](../../frontend/index.html#L4)

- Definição dos tokens visuais e tipografia global.
  [`index.css:1`](../../frontend/src/index.css#L1)

**Roteamento e Componentização**

- Ponto de entrada das rotas e injeção do componente de navegação.
  [`App.tsx:16`](../../frontend/src/App.tsx#L16)

- Marcação semântica e links de rotas da barra de navegação.
  [`Navigation.tsx:6`](../../frontend/src/components/Navigation/Navigation.tsx#L6)

- Layout responsivo Mobile-first e Pill para Desktop.
  [`Navigation.css:1`](../../frontend/src/components/Navigation/Navigation.css#L1)

### Review Findings
- [ ] [Review][Patch] O botão "Sair" na Navigation bar quebra a restrição de "5 ícones" — Remover da nav e colocar em um header superior provisório (desktop/mobile) para posterior refinamento de UX.
- [ ] [Review][Patch] Tratamento do AbortError e falha de rede desloga usuário indesejadamente [frontend/src/App.tsx:181]
- [ ] [Review][Patch] Falta de ícones na Navigation bar [frontend/src/components/Navigation/Navigation.tsx:32]
- [ ] [Review][Patch] Extração frágil do token CSRF via `split('=')[1]` [frontend/src/components/Navigation/Navigation.tsx:13]
- [ ] [Review][Patch] Hover state "grudento" em dispositivos móveis [frontend/src/components/Navigation/Navigation.css:39]
- [ ] [Review][Patch] Falta de reset básico para margens de tipografia [frontend/src/index.css]
- [ ] [Review][Patch] Faltam as tags `lang="pt-BR"` e `<meta name="description">` [frontend/index.html]
- [x] [Review][Defer] Interface de carregamento (Loading) não estlizada [frontend/src/App.tsx] — deferred, pre-existing
- [x] [Review][Defer] Redirecionamento de login não preserva o estado de rota prévia (`location.state`) [frontend/src/App.tsx:47] — deferred, pre-existing
- [x] [Review][Defer] Rotas hardcoded ao invés de usar constantes centralizadas [frontend/src/App.tsx] — deferred, pre-existing
- [x] [Review][Defer] Ausência de Error Boundary genérico para falhas do React [frontend/src/App.tsx] — deferred, pre-existing
- [x] [Review][Defer] Ausência de testes end-to-end e componentes para o Auth Fetch e Logout — deferred, pre-existing
- [x] [Review][Defer] Ausência de verificação contra bypass de rota protegida — deferred, pre-existing
