# Epic 2 Context: Configuração Financeira Básica (Contas e Categorias)

<!-- Compiled from planning artifacts. Edit freely. Regenerate with compile-epic-context if planning docs change. -->

## Goal

Permitir que o usuário configure suas origens de dinheiro e suas categorias macro, preparando o sistema para que as movimentações financeiras possam ser cadastradas com integridade referencial e proteção contra exclusões acidentais.

## Stories

- Story 2.1: Gestão de Contas (CRUD) com Proteção de Vínculo
- Story 2.2: Gestão de Categorias (CRUD) com Proteção de Vínculo
- Story 2.3: Restrição Granular de Cadastro sem Dependências

## Requirements & Constraints

- **Campos**: Contas e Categorias possuem os mesmos campos: Descrição (obrigatório) e Observações (opcional).
- **Categorias simples**: As Categorias são "flat" (não há hierarquia de subcategorias).
- **Proteção de Vínculo**: A exclusão de uma Conta ou Categoria que já possua movimentações vinculadas deve ser rigorosamente bloqueada, exibindo mensagem explícita e a contagem de itens que utilizam a categoria/conta.
- **Edição irrestrita**: A edição de Descrição e Observações é sempre permitida, mesmo para itens com vínculos ativos.
- **Restrição de Cadastro**: Formulários de Movimentação devem ficar inativos/bloqueados se não houver Contas ou Categorias cadastradas, apresentando mensagens de estado vazio (Empty State) e botões de chamada-para-ação (CTA) direcionando para o cadastro das dependências faltantes.

## Technical Decisions

- **Isolamento de Dados (Multi-Tenancy lógico)**: Todas as consultas, inserções, atualizações e exclusões de Contas e Categorias devem obrigatoriamente incluir e validar o `usuario_id` obtido a partir da sessão autenticada.
- **Bloqueio de Exclusão (Banco de Dados)**: A proteção de vínculo deve ser garantida a nível de banco de dados (via `ON DELETE RESTRICT`), com tratamento adequado da exceção no backend para retornar a contagem de itens ao frontend.
- **Gerenciamento de Cache**: Requisições ao backend devem utilizar React Query (TanStack Query) garantindo a invalidação automática do cache após mutações de criação, edição ou exclusão.

## UX & Interaction Patterns

- **Acesso Secundário**: O gerenciamento de Contas e Categorias deve ser acessado por um menu secundário (ex: Menu Hamburger / Avatar), e não pela barra de navegação principal.
- **Formulários**: Devem abrir dentro de um Modal (Desktop) ou Bottom Sheet (Mobile), aprisionando o foco do teclado ("Focus Trapping").
- **Confirmação de Deleção**: A exclusão requer confirmação explícita através de uma janela interativa (Modal) antes de enviar a requisição ao backend.
- **Empty States**: Listagens vazias e formulários com dependências ausentes devem exibir um layout padrão de estado vazio (ícone esmaecido, título, descrição e botão CTA para a ação de cadastro).
- **Feedback Visual**: Utilizar Toasts com bordas semânticas para notificar sucesso nas operações (criação, edição) ou erros.

## Cross-Story Dependencies

- **Dependência do Epic 1**: O Epic 1 (Autenticação) já deve estar funcional para fornecer o contexto de `usuario_id` e a segurança via Cookie HTTPOnly necessários ao isolamento dos dados gerenciados neste épico.
- **Pré-requisito para o Epic 3**: O cadastro prévio de Contas e Categorias é restrição absoluta para o funcionamento dos formulários de movimentações do Epic 3.
