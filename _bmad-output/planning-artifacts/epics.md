---
stepsCompleted: ["step-01-validate-prerequisites", "step-02-design-epics", "step-03-create-stories"]
inputDocuments:
  - "prds/prd-projeto-aureus-docs-2026-08-02/prd.md"
  - "architecture/architecture-projeto-aureus-docs-2026-08-14/ARCHITECTURE-SPINE.md"
  - "ux-designs/ux-aureus-product-2026-08-14/DESIGN.md"
  - "ux-designs/ux-aureus-product-2026-08-14/EXPERIENCE.md"
---

# Aureus - Epic Breakdown

## Overview

Este documento apresenta a decomposição completa de requisitos do PRD, UX Design e Arquitetura do Aureus em Épicos e Histórias de Usuário implementáveis, refinado com base em revisões adversariais, tratamento de casos de borda e critérios de integridade técnica.

## Requirements Inventory

### Functional Requirements

FR1: Criar Conta informando Descrição e Observações.
FR2: Visualizar todas as Contas em formato de lista.
FR3: Editar Descrição e Observações de uma Conta existente.
FR4: Excluir Conta existente (bloqueado se tiver movimentações vinculadas).
FR5: Criar Categoria macro informando Descrição e Observações.
FR6: Visualizar Categorias macro em formato de lista.
FR7: Editar Descrição e Observações de Categoria existente.
FR8: Excluir Categoria existente (bloqueado se tiver movimentações vinculadas).
FR9: Criar Despesa Variável (com campos parcelamento calculados e opcionais).
FR10: Visualizar Despesas Variáveis em lista.
FR11: Editar Despesa Variável existente com recálculo automático.
FR12: Excluir Despesa Variável existente.
FR13: Criar Despesa Fixa (valor projetado para 24 meses).
FR14: Visualizar Despesas Fixas em lista.
FR15: Editar Despesa Fixa projetando novo valor.
FR16: Excluir Despesa Fixa.
FR17: Criar Receita Variável (com campos parcelamento calculados).
FR18: Visualizar Receitas Variáveis em lista.
FR19: Editar Receita Variável com recálculo automático.
FR20: Excluir Receita Variável.
FR21: Criar Receita Fixa (valor projetado para 24 meses).
FR22: Visualizar Receitas Fixas em lista.
FR23: Editar Receita Fixa projetando novo valor.
FR24: Excluir Receita Fixa.
FR25: Selecionar mês inicial da grade de Consolidação (manual ou botão Mês Atual).
FR26: Bloco Receitas por Conta exibe totais de receitas mensais por conta.
FR27: Bloco Despesas por Conta exibe totais de despesas mensais por conta.
FR28: Bloco Categorias (R$) exibe totais absolutos de despesas por categoria.
FR29: Bloco Categorias (%) exibe percentual de cada categoria sobre o total gasto no mês.
FR30: Bloco Resumo Geral exibe Total Gasto no Mês, Sobra do Mês e Sobra Retroativa Acumulada.
FR31: Impedir cadastro de Movimentação se Contas ou Categorias não estiverem cadastradas.
FR32: Barra de Navegação no topo com 5 abas em ordem fixa.
FR33: Aceitar Nº Parcelas = 1 (à vista) onde Valor Total = Valor Parcela.
FR34: Entrar com Google usando OAuth 2.0 (OpenID Connect).
FR35: Criar conta local na primeira entrada ou reconhecer conta existente via Identidade Externa.
FR36: Isolar dados por Usuário para que só acesse seus próprios registros.
FR37: Encerrar sessão pelo comando "Sair".

### NonFunctional Requirements

NFR1: A aplicação deve rodar localmente com backend construído em Java Spring Boot e banco de dados PostgreSQL rodando via Docker.
NFR2: O sistema deve isolar dados por usuário (Multi-Tenancy lógico).
NFR3: A sincronização de estado (mês selecionado) deve persistir entre as diferentes abas.
NFR4: Respostas rápidas e assíncronas do frontend para o backend, sem full-page reloads durante a navegação em abas.

### Additional Requirements

- [AD-1] A comunicação Frontend-Backend deve ser estritamente via REST API (JSON sobre HTTP).
- [AD-2] O Backend deve ser estruturado em Monolito Modular (Modular Monolith).
- [AD-3] Todas as alterações de schema do banco devem usar Flyway (ex: V1__init.sql).
- [AD-4] O Frontend deve gerenciar o estado global, como Mês Selecionado, com Zustand.
- [AD-5] O Frontend deve utilizar React Query (TanStack Query) com Axios/Fetch para consultas de dados e invalidação de cache automática nas mutações.
- [AD-6] O Token JWT deve ser armazenado exclusivamente em um HttpOnly Cookie seguro gerado pelo Backend com `SameSite=Lax`.

### UX Design Requirements

UX-DR1: Implementar tokens de Cores da Marca (Deep Teal primário, Warm Amber secundário).
UX-DR2: Implementar estilo tipográfico unificado com a fonte Plus Jakarta Sans e suporte a tabular-nums.
UX-DR3: Implementar padronização de bordas arredondadas (radius-sm 8px, radius-md 12px, radius-pill).
UX-DR4: Criar Barra de Navegação Principal em formato Pill (Desktop).
UX-DR5: Criar Bottom Navigation fixo com 5 ícones (Mobile).
UX-DR6: Criar Floating Action Button (FAB) para adição de itens.
UX-DR7: Criar Componente Data Block Card com cabeçalhos de cores semânticas.
UX-DR8: Criar Form Input com estilos unificados para focus, hover e error.
UX-DR9: Criar Componente Modal (Desktop) e Bottom Sheet (Mobile) para exibição dos formulários com trapping de teclado.
UX-DR10: Desenvolver Componente Empty State visual (ícone esmaecido + texto secundário) para listas vazias.
UX-DR11: Adicionar componente Global Filter Toggle na header das listas (alternar entre mês atual e visão total).
UX-DR12: Criar visualização de Calculation Preview assíncrona exibida dentro dos formulários de movimentação parcelada.
UX-DR13: Adicionar Notificações em Toast para confirmar ações concluídas e erros não contornáveis.
UX-DR14: Garantir capacidade de interação via Swipe para mudar o mês visualizado na Consolidação Mobile.
UX-DR15: Adicionar transição com Skeleton Loaders no lugar de spinners bloqueantes globais durante o fetch.
UX-DR16: Implementar confirmação através de janela interativa explícita para prevenir perda de dados em deleções.
UX-DR17: Utilizar atributos ARIA (grid, row, gridcell) obrigatórios na matriz 24-meses para Screen Readers.

### FR Coverage Map

FR34: Epic 1 - Entrar com Google usando OAuth 2.0
FR35: Epic 1 - Criar conta local ou reconhecer existente
FR36: Epic 1 - Isolar dados por Usuário
FR37: Epic 1 - Encerrar sessão
FR32: Epic 1 - Barra de Navegação no topo com 5 abas

FR1: Epic 2 - Criar Conta
FR2: Epic 2 - Visualizar Contas
FR3: Epic 2 - Editar Conta
FR4: Epic 2 - Excluir Conta
FR5: Epic 2 - Criar Categoria
FR6: Epic 2 - Visualizar Categorias
FR7: Epic 2 - Editar Categoria
FR8: Epic 2 - Excluir Categoria
FR31: Epic 2 - Impedir cadastro se faltam dependências

FR9: Epic 3 - Criar Despesa Variável
FR10: Epic 3 - Visualizar Despesas Variáveis
FR11: Epic 3 - Editar Despesa Variável
FR12: Epic 3 - Excluir Despesa Variável
FR13: Epic 3 - Criar Despesa Fixa
FR14: Epic 3 - Visualizar Despesas Fixas
FR15: Epic 3 - Editar Despesa Fixa
FR16: Epic 3 - Excluir Despesa Fixa
FR17: Epic 3 - Criar Receita Variável
FR18: Epic 3 - Visualizar Receitas Variáveis
FR19: Epic 3 - Editar Receita Variável
FR20: Epic 3 - Excluir Receita Variável
FR21: Epic 3 - Criar Receita Fixa
FR22: Epic 3 - Visualizar Receitas Fixas
FR23: Epic 3 - Editar Receita Fixa
FR24: Epic 3 - Excluir Receita Fixa
FR33: Epic 3 - Aceitar Parcela Única

FR25: Epic 4 - Selecionar mês inicial da grade
FR26: Epic 4 - Bloco Receitas por Conta
FR27: Epic 4 - Bloco Despesas por Conta
FR28: Epic 4 - Bloco Categorias (R$)
FR29: Epic 4 - Bloco Categorias (%)
FR30: Epic 4 - Bloco Resumo Geral

## Epic List

* **Epic 1: Autenticação e Navegação Segura (Auth & Shell)** — Permitir que o usuário acesse o sistema de forma segura via Google e que seus dados fiquem completamente isolados por usuário, fornecendo a casca visual e navegação principal (Pill Nav Desktop e Bottom Nav Mobile).
* **Epic 2: Configuração Financeira Básica (Contas e Categorias)** — Permitir que o usuário configure suas origens financeiras e categorias macro com integridade referencial protegida contra exclusões acidentais.
* **Epic 3: Lançamentos Financeiros (Despesas e Receitas)** — Permitir o registro, edição, listagem e exclusão de receitas e despesas (fixas e variáveis), com pré-visualização de parcelas e sincronização de filtros.
* **Epic 4: Consolidação e Projeção Mensal (Painel de 24 Meses)** — Matriz analítica de projeção de 24 meses com subtotais por conta, despesas por categoria (R$ e %), resumo mensal, saldo histórico acumulado e navegação por Swipe mobile.

---

## Epic 1: Autenticação e Navegação Segura (Auth & Shell)

Permitir que o usuário acesse o sistema de forma segura via Google e que seus dados fiquem completamente isolados por usuário. Fornece a casca visual unificada (shell e barra de navegação) com os tokens de design do sistema.

### Story 1.1: Esqueleto Visual e Barra de Navegação

As a Usuário,
I want visualizar a aplicação com o tema visual correto e acessar a barra de navegação principal,
So that eu possa me familiarizar com a interface e navegar facilmente entre as 5 áreas funcionais do sistema.

**Acceptance Criteria:**

**Given** que o usuário acessa o sistema via Desktop ou Mobile
**When** a interface básica é renderizada
**Then** os tokens de cores (Deep Teal, Warm Amber), tipografia (Plus Jakarta Sans, tabular-nums) e bordas arredondadas devem estar aplicados
**And** no Desktop deve haver uma "Pill Navigation" superior, e no Mobile um "Bottom Navigation" com 5 abas em ordem fixa (Consolidação, Despesas Variáveis, Despesas Fixas, Receitas Variáveis, Receitas Fixas)

### Story 1.2: Autenticação via Google (OAuth 2.0) e Tratamento de Erros

As a Usuário não autenticado,
I want entrar com minha conta do Google,
So that eu possa acessar o Aureus com segurança e sem senha manual, criando ou vinculando minha conta local.

**Acceptance Criteria:**

**Given** que o usuário não está autenticado e está na tela de login
**When** ele clica em "Entrar com Google" e autoriza o acesso
**Then** o sistema valida o token OpenID Connect emitido pelo Google
**And** cria uma conta local (se for o primeiro acesso) ou reconhece a conta existente via subject ID externo
**And** redireciona o usuário para a aba "Consolidação" de forma autenticada
**Given** que o usuário cancela a autorização no Google ou ocorre erro de comunicação
**When** o callback OAuth falha
**Then** uma notificação Toast de erro amigável é exibida e o usuário permanece na tela de login de forma segura

### Story 1.3: Proteção de Sessão e Isolamento de Dados

As a Usuário autenticado,
I want que minhas requisições sejam feitas sob uma sessão segura e isolada,
So that nenhum outro usuário do sistema possa visualizar ou modificar meus dados.

**Acceptance Criteria:**

**Given** um usuário autenticado com sucesso
**When** o backend emite o token JWT de sessão
**Then** o token deve ser armazenado exclusivamente em um Cookie seguro com flags `HttpOnly`, `SameSite=Lax` e `Secure` (em ambiente de produção)
**And** todas as consultas e comandos no backend devem filtrar e validar obrigatoriamente a posse do registro pela chave estrangeira `usuario_id`

### Story 1.4: Encerrar Sessão (Logout) e Limpeza de Estado

As a Usuário autenticado,
I want ter a opção de encerrar minha sessão através de um menu de perfil,
So that eu possa proteger meus dados ao deixar o dispositivo sem que o botão de sair ocupe espaço na navegação principal.

**Acceptance Criteria:**

**Given** que o usuário está autenticado na interface principal
**When** ele visualiza o canto superior direito da tela (no Header mobile ou no Desktop)
**Then** deve ser exibida a sua foto de perfil recebida através da conta do Google
**When** o usuário clica na sua foto de perfil
**Then** um menu dropdown (ou Bottom Sheet no Mobile) é aberto exibindo a opção de "Sair"
**Given** que o usuário clica em "Sair"
**Then** o backend invalida o Cookie de sessão e o frontend limpa todos os dados em cache (Zustand e React Query)
**And** o usuário é redirecionado para a tela de login inicial sem retenção de estado anterior

---

## Epic 2: Configuração Financeira Básica (Contas e Categorias)

Permitir que o usuário configure suas origens de dinheiro e suas categorias macro, preparando o sistema para que as movimentações possam ser cadastradas com integridade referencial.

### Story 2.1: Gestão de Contas (CRUD) com Proteção de Vínculo

As a Usuário,
I want criar, visualizar, editar e excluir minhas Contas financeiras,
So that eu possa cadastrar locais de origem ou destino (ex: Conta Corrente, Carteira, Cartão) para minhas movimentações.

**Acceptance Criteria:**

**Given** que o usuário está autenticado
**When** ele acessa o gerenciamento de Contas e preenche Descrição (obrigatório) e Observações (opcional)
**Then** ele cria a Conta e a visualiza na listagem ordenada
**And** ele pode editar a Descrição e Observações posteriormente
**Given** que o usuário solicita a exclusão de uma Conta
**When** a Conta possui movimentações ativas vinculadas
**Then** o backend rejeita a exclusão via regra `ON DELETE RESTRICT` e retorna a contagem de movimentações associadas
**And** o frontend exibe um modal explicativo informando a quantidade de lançamentos vinculados e bloqueando a remoção até que eles sejam realocados ou excluídos

### Story 2.2: Gestão de Categorias (CRUD) com Proteção de Vínculo

As a Usuário,
I want criar, visualizar, editar e excluir Categorias financeiras,
So that eu possa classificar minhas receitas e despesas de forma organizada.

**Acceptance Criteria:**

**Given** que o usuário está autenticado
**When** ele acessa o gerenciamento de Categorias e preenche Descrição e Observações
**Then** ele cria e visualiza a listagem de Categorias
**And** pode atualizar os dados a qualquer momento
**Given** uma Categoria vinculada a lançamentos financeiros
**When** o usuário tenta excluí-la
**Then** a exclusão é bloqueada com mensagem explícita e contagem de itens que utilizam a categoria

### Story 2.3: Restrição Granular de Cadastro sem Dependências

As a Usuário com conta recém-criada,
I want ser orientado sobre quais dependências (Contas ou Categorias) preciso cadastrar antes de fazer lançamentos,
So that eu não enfrente formulários com seletores vazios ou erros de validação.

**Acceptance Criteria:**

**Given** que o usuário abre o formulário de cadastro de Movimentação
**When** não há Contas cadastradas (mas existem Categorias)
**Then** o seletor de Contas exibe estado vazio e um botão/link com CTA direto para "Criar Conta"
**When** não há Categorias cadastradas (mas existem Contas)
**Then** o seletor de Categorias exibe estado vazio e um botão/link com CTA direto para "Criar Categoria"
**When** não há nem Contas nem Categorias
**Then** o formulário exibe um Empty State orientando a criação inicial de ambas as dependências

---

## Epic 3: Lançamentos Financeiros (Despesas e Receitas)

Permitir que o usuário registre, edite, visualize e exclua todos os tipos de entrada e saída financeira, com pré-visualização de cálculos e sincronização de filtros.

### Story 3.1: Lançamentos Fixos (Despesas e Receitas) com Vigência

As a Usuário,
I want registrar receitas e despesas fixas recorrentes,
So that valores mensais contínuos sejam projetados automaticamente ao longo de toda a grade.

**Acceptance Criteria:**

**Given** que existem Contas e Categorias cadastradas
**When** o usuário preenche e salva o formulário Modal/Bottom Sheet de itens Fixos informando Valor, Descrição, Conta, Categoria e Data de Início
**Then** o registro é salvo com sua competência inicial e projetado nos meses subsequentes
**And** ao editar o valor de um item fixo, a alteração define o novo valor projetado a partir da data de vigência editada
**And** o usuário pode excluir o registro fixo permanentemente com confirmação via modal

### Story 3.2: Lançamentos Variáveis e Parcelados

As a Usuário,
I want registrar compras e entradas parceladas com cálculo em tempo real,
So that o sistema projete com exatidão as parcelas sem divergências de arredondamento.

**Acceptance Criteria:**

**Given** que o usuário preenche o formulário de itens Variáveis
**When** ele informa Valor Total, Nº de Parcelas (>= 1) e Primeira Parcela (mês/ano)
**Then** o componente Calculation Preview calcula e exibe em tempo real o Valor da Parcela e a Última Parcela no formato `YearMonth`
**And** parcelamentos com duração superior a 24 meses são persistidos integralmente e projetados na grade de acordo com a janela de meses visualizada

### Story 3.3: Lançamentos Variáveis à Vista (Parcela Única) [DESCARTADA/ABSORVIDA]

> **Nota de Implementação:** Esta história foi absorvida pela **Story 3.2**. O conceito de lançamentos à vista foi implementado nativamente suportando "Nº Parcelas = 1" no mesmo fluxo.

As a Usuário,
I want informar compras ou entradas pontuais não parceladas,
So that a movimentação incida de forma isolada em um único mês da minha grade.

**Acceptance Criteria:**

**Given** que o usuário está no formulário de itens Variáveis
**When** ele define o Nº Parcelas como "1"
**Then** o sistema valida a entrada como compra à vista
**And** o Valor Total reflete instantaneamente o Valor Parcela, e a Última Parcela é preenchida com o mesmo mês da Primeira Parcela
**And** o registro é salvo afetando exclusivamente o mês selecionado

### Story 3.4: Sincronização de Visão Mensal, Invalidação de Cache e Filtro Global

As a Usuário,
I want que minhas listagens mostrem os itens do mês ativo com atualização imediata após mutações,
So that meus dados estejam sempre sincronizados entre as abas sem recarregamento de página.

**Acceptance Criteria:**

**Given** que o usuário visualiza a lista de Despesas ou Receitas
**When** o mês X está ativo no estado global (Zustand)
**Then** a lista exibe apenas as movimentações vigentes no mês X
**And** se não houver registros no mês X, um componente `EmptyState` contextualizado é renderizado
**When** o usuário clica no toggle "Filtro Global / Ver Todos", a restrição de mês é desligada e todas as transações são listadas
**Given** que uma movimentação é criada, editada ou excluída
**When** a mutação conclui com sucesso
**Then** as queries do React Query para a listagem e para o painel de Consolidação são invalidadas automaticamente, refletindo as alterações em tempo real

---

## Epic 4: Consolidação e Projeção Mensal (Painel de 24 Meses)

Dar ao usuário o poder de visualizar o impacto de suas decisões financeiras 24 meses no futuro, através de uma matriz consolidada com totais, categorias, sobras acumuladas e navegação responsiva.

### Story 4.1: Seleção de Mês, Navegação Temporal e Grid com Suporte a Gestos Mobile

As a Usuário,
I want selecionar um mês inicial e navegar em uma grade de 24 colunas com suporte a scroll e gestos mobile,
So that eu possa visualizar o horizonte financeiro com fluidez no Desktop e Mobile.

**Acceptance Criteria:**

**Given** que o usuário acessa a aba "Consolidação"
**When** a tela carrega ou o usuário seleciona um mês no Month Picker (limitado operacionalmente entre -5 anos e +5 anos)
**Then** a matriz renderiza horizontalmente 24 colunas de meses a partir do mês escolhido
**And** a primeira coluna lateral com os nomes de Contas e Categorias permanece fixa (`sticky`) durante a rolagem horizontal
**And** no Mobile, o usuário pode realizar gestos de Swipe horizontal para avançar ou recuar o mês de foco da visualização

### Story 4.2: Bloco de Consolidação por Conta (Receitas e Despesas)

As a Usuário,
I want visualizar blocos consolidados que agrupam meus ganhos e gastos por Conta,
So that eu conheça o fluxo mensal de cada uma das minhas contas cadastradas.

**Acceptance Criteria:**

**Given** a grade de 24 meses carregada
**When** os dados financeiros são processados
**Then** o "Bloco Receitas por Conta" exibe a soma de receitas fixas e variáveis mês a mês para cada Conta
**And** o "Bloco Despesas por Conta" exibe a soma de despesas fixas e variáveis mês a mês para cada Conta
**And** células de meses sem movimentação para determinada Conta exibem o valor `R$ 0,00`

### Story 4.3: Bloco Analítico de Categorias (Valores e Percentuais com Proteção Zero)

As a Usuário,
I want visualizar as despesas somadas por Categoria e sua representação percentual em relação ao total gasto,
So that eu identifique quais áreas concentram a maior fatia do meu orçamento.

**Acceptance Criteria:**

**Given** a renderização da seção de Categorias na Consolidação
**When** os cálculos por categoria são efetuados para cada mês
**Then** o "Bloco Categorias (R$)" exibe o valor total gasto por categoria
**And** o "Bloco Categorias (%)" calcula e exibe a proporção percentual `(Gasto da Categoria / Gasto Total do Mês) * 100`
**And** se o Total Gasto do mês for igual a R$ 0,00, todas as categorias exibem `0%`, prevenindo divisão por zero (`NaN`)

### Story 4.4: Bloco de Resumo Geral com Sobra Histórica Acumulada

As a Usuário,
I want visualizar a sobra líquida mensal e a sobra retroativa acumulada desde o histórico pré-grade,
So that eu saiba a real evolução patrimonial e a sustentabilidade das minhas finanças ao longo dos 24 meses.

**Acceptance Criteria:**

**Given** todos os blocos anteriores calculados
**When** as linhas de resumo da Consolidação são renderizadas
**Then** a linha "Total Gasto" exibe a somatória de todas as despesas do mês
**And** a linha "Sobra do Mês" exibe a diferença `Receitas do Mês - Despesas do Mês`
**And** o backend calcula e envia o saldo acumulado histórico anterior ao primeiro mês da grade
**And** a linha "Sobra Retroativa Acumulada" computa no primeiro mês `Saldo Histórico Pré-Grade + Sobra do Mês 1` e, para os meses subsequentes `n`, `Sobra Retroativa Mês n = Sobra Retroativa Mês n-1 + Sobra do Mês n`

---

## Epic 5: Production Readiness & Tech Debt Resolution

Preparar a aplicação para implantação em ambiente produtivo, garantindo que todas as especificações arquiteturais e de estabilidade (como o versionamento de banco) sejam estritamente cumpridas.

### Story 5.1: Congelamento de Schema e Ativação do Flyway

As a Administrador do Sistema,
I want que o esquema do banco de dados seja versionado e controlado explicitamente via scripts SQL,
So that implantações em produção sejam seguras, rastreáveis e livres de alterações destrutivas acidentais.

**Acceptance Criteria:**

**Given** que a fase de prototipação (Epics 1 a 4) foi finalizada
**When** o desenvolvedor inicia a preparação para produção
**Then** um script de migração basilar (`V1__init_schema.sql`) é gerado capturando o estado final de todas as tabelas
**And** o Hibernate é reconfigurado de `ddl-auto: update` para `ddl-auto: validate`
**And** a dependência do Flyway é ativada no `pom.xml` para executar as migrações na inicialização do Spring Boot
