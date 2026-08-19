# Epic 1 Context: Autenticação e Navegação Segura (Auth & Shell)

<!-- Compiled from planning artifacts. Edit freely. Regenerate with compile-epic-context if planning docs change. -->

## Goal

Permitir que o usuário acesse o sistema de forma segura via Google e que seus dados fiquem completamente isolados por usuário. Fornece a casca visual unificada (shell e barra de navegação) com os tokens de design do sistema.

## Stories

- Story 1.1: Esqueleto Visual e Barra de Navegação
- Story 1.2: Autenticação via Google (OAuth 2.0) e Tratamento de Erros
- Story 1.3: Proteção de Sessão e Isolamento de Dados
- Story 1.4: Encerrar Sessão (Logout) e Limpeza de Estado

## Requirements & Constraints

- Autenticação deve ser realizada estritamente via Google OAuth 2.0 (OpenID Connect), solicitando os escopos `openid`, `profile` e `email`.
- Na primeira entrada, deve-se criar uma conta local; nos logins subsequentes, deve-se reconhecer a conta existente pelo subject ID do Google.
- A aplicação deve prover isolamento absoluto dos dados (Multi-Tenancy lógico). Nenhuma requisição autenticada pode ler ou alterar dados de outros usuários.
- O término de sessão ("Sair") deve invalidar a sessão no servidor e limpar todo o cache local no cliente (Zustand e React Query).
- A aplicação deve apresentar uma Barra de Navegação com 5 abas em ordem fixa: Consolidação, Despesas Variáveis, Despesas Fixas, Receitas Fixas, Receitas Variáveis.

## Technical Decisions

- **Armazenamento de Sessão:** O Token JWT gerado no backend após o login OAuth deve ser armazenado exclusivamente em um Cookie `HttpOnly` seguro com a flag `SameSite=Lax`. O frontend nunca deve armazenar tokens em `localStorage` ou variáveis de acesso global no JS.
- **Isolamento de Dados:** Todo e qualquer dado financeiro e as tabelas de entidades de domínio devem possuir a coluna `usuario_id`. Todos os repositórios backend devem aplicar filtragem compulsória por esta coluna validada a partir do token de sessão vigente.
- **Frontend State:** Utilizar Zustand para gerenciar contexto global entre abas e React Query para cacheamento, data fetching e invalidação automática de estado local na saída do usuário.

## UX & Interaction Patterns

- **Navegação Desktop (Pill Nav):** A barra de navegação principal deve ser uma pílula centralizada flutuante (`radius-pill`) com fundo branco no topo da tela. A aba ativa recebe fundo *Deep Teal* (`color.brand.primary`) e texto branco.
- **Navegação Mobile (Bottom Nav):** A navegação deve ser fixa no rodapé com sombra projetada acima (`shadow-up`), exibindo 5 ícones com labels curtos. A aba ativa recebe cor *Deep Teal*, inativas cinza.
- **Design Tokens:** O layout inicial deve aplicar as cores da marca (Deep Teal primário, Warm Amber secundário), a tipografia unificada (Plus Jakarta Sans com `tabular-nums`) e formatos padronizados de bordas (radius-sm, radius-md).
- **Feedback de Autenticação:** Falha na autorização ou retorno OAuth inválido devem disparar uma notificação do tipo Toast, informando erro não-bloqueante no frontend, mantendo o usuário na tela de login de forma segura.

## Cross-Story Dependencies

- Esta é a fundação do sistema. Todos os demais épicos dependem da camada de autenticação, do isolamento de sessão fornecido por este épico e da interface de "shell" para sua ancoragem visual e navegação.
