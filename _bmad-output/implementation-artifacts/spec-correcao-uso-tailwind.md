---
title: 'Correção: Uso do Tailwind CSS v4'
type: 'bugfix'
created: '2026-08-18'
status: 'done'
baseline_commit: '51ffa3f74fbf0e80511c02ebd4e118eb99cda8d9'
review_loop_iteration: 0
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O PRD e a arquitetura especificavam o uso do Tailwind CSS v4 para o frontend, mas o projeto foi implementado usando CSS puro (Vanilla CSS).
**Approach:** Instalar e configurar o Tailwind CSS v4 (como plugin do Vite), migrar todos os componentes atuais que utilizam arquivos `.css` isolados para classes utilitárias do Tailwind e, por fim, deletar os arquivos `.css` obsoletos, mantendo a aparência e responsividade originais.

## Boundaries & Constraints

**Always:**
- Utilizar Tailwind CSS v4 com a integração `@tailwindcss/vite` no `vite.config.ts`.
- Manter o aspecto visual original, incluindo cores, espaçamentos, flex/grid layouts, e transições/efeitos de hover.
- O arquivo `index.css` deve importar o tailwind (`@import "tailwindcss";`) e pode manter variáveis CSS globais de cores primárias/secundárias (tokens de design) caso sejam referenciados, mas todo o resto do styling deve ser migrado.

**Ask First:**
- Se algum comportamento visual (ex: animações complexas) não puder ser migrado de forma limpa para utilitários do Tailwind e requerer CSS arbitrário complexo.

**Never:**
- Não alterar regras de negócio ou estrutura do DOM desnecessariamente; mude apenas as `className` e elementos necessários para acomodar as classes do Tailwind.
- Não introduzir novos frameworks CSS além do Tailwind.

</frozen-after-approval>

## Code Map

- `frontend/package.json` -- Adicionar dependências `tailwindcss` e `@tailwindcss/vite`.
- `frontend/vite.config.ts` -- Configurar plugin do Tailwind.
- `frontend/src/index.css` -- Substituir estilos globais por `@import "tailwindcss";` e mapear variáveis de cores (design tokens).
- `frontend/src/App.tsx` e `frontend/src/App.css` -- Refatorar `App.tsx` e apagar `App.css`.
- `frontend/src/components/Header/Header.tsx` e `frontend/src/components/Header/Header.css` -- Refatorar para classes e apagar arquivo css.
- `frontend/src/components/Navigation/Navigation.tsx` e `frontend/src/components/Navigation/Navigation.css` -- Refatorar para classes e apagar arquivo css.
- `frontend/src/components/Login/Login.tsx` e `frontend/src/components/Login/Login.css` -- Refatorar para classes e apagar arquivo css.

## Tasks & Acceptance

**Execution:**
- [x] `frontend/package.json` -- Instalar dependências `tailwindcss` e `@tailwindcss/vite` como devDependencies via script/npm.
- [x] `frontend/vite.config.ts` -- Adicionar `@tailwindcss/vite` aos plugins.
- [x] `frontend/src/index.css` -- Atualizar para `@import "tailwindcss";` e incluir as variáveis de cor (ex: `--color-brand-primary`) se ainda for útil para a customização no v4, limpando resets base que o Tailwind já possui.
- [x] `frontend/src/App.tsx` -- Remover importação de `App.css` e migrar as marcações/className.
- [x] `frontend/src/App.css` -- Deletar arquivo.
- [x] `frontend/src/components/Header/Header.tsx` -- Migrar estilos de `Header.css` para classes Tailwind inline.
- [x] `frontend/src/components/Header/Header.css` -- Deletar arquivo.
- [x] `frontend/src/components/Navigation/Navigation.tsx` -- Migrar estilos de `Navigation.css` para classes Tailwind.
- [x] `frontend/src/components/Navigation/Navigation.css` -- Deletar arquivo.
- [x] `frontend/src/components/Login/Login.tsx` -- Migrar estilos de `Login.css` para classes Tailwind.
- [x] `frontend/src/components/Login/Login.css` -- Deletar arquivo.

**Acceptance Criteria:**
- Given a aplicação rodando, when o usuário navega e acessa o Login, Header, ou Navigation, then a renderização visual é idêntica à versão anterior (com CSS puro).
- Given o código fonte, when verificado, then não devem existir arquivos CSS isolados por componente (apenas o `index.css`).

## Spec Change Log

## Verification

**Commands:**
- `cd frontend && npm run build` -- expected: O build do Vite completa com sucesso (não há erros de compilação ou do TS relativos aos imports apagados).

**Manual checks (if no CLI):**
- Inicie a aplicação com `npm run dev` e acesse pelo navegador; as telas de login e interior (header, menu principal) não devem apresentar quebras de layout.

## Suggested Review Order

**Configuração do Tailwind**

- Instalação e configuração do Vite plugin para Tailwind v4.
  [`vite.config.ts:6`](../../frontend/vite.config.ts#L6)

- Importação e definição de variáveis de tema no CSS global.
  [`index.css:1`](../../frontend/src/index.css#L1)

- Dependências do Tailwind adicionadas ao projeto.
  [`package.json:22`](../../frontend/package.json#L22)

**Refatoração de Componentes (Migração para classes utilitárias)**

- Layout principal migrado, eliminação de `.main-content`.
  [`App.tsx:28`](../../frontend/src/App.tsx#L28)

- Botões de perfil e logout refatorados, incluindo estados disabled/focus.
  [`Header.tsx:15`](../../frontend/src/components/Header/Header.tsx#L15)

- Navegação inferior (tabs) responsiva refatorada usando states do Tailwind.
  [`Navigation.tsx:10`](../../frontend/src/components/Navigation/Navigation.tsx#L10)

- Tela de login refatorada para utilitários do Tailwind.
  [`Login.tsx:15`](../../frontend/src/components/Login/Login.tsx#L15)
