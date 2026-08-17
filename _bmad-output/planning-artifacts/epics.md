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

This document provides the complete epic and story breakdown for Aureus, decomposing the requirements from the PRD, UX Design if it exists, and Architecture requirements into implementable stories.

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
- [AD-5] O Frontend deve utilizar React Query (TanStack Query) com Axios/Fetch para consultas de dados.
- [AD-6] O Token JWT deve ser armazenado exclusivamente em um HttpOnly Cookie seguro gerado pelo Backend.

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

### Epic 1: Autenticação e Navegação Segura (Auth & Shell)
Permitir que o usuário acesse o sistema de forma segura via Google e que seus dados fiquem completamente isolados. Além disso, fornece o "esqueleto" visual (shell e barra de navegação) para suportar as próximas funcionalidades.
**FRs covered:** FR32, FR34, FR35, FR36, FR37

### Epic 2: Configuração Financeira Básica (Contas e Categorias)
Permitir que o usuário configure suas origens de dinheiro e suas categorias macro, preparando o sistema para que as movimentações possam ser cadastradas.
**FRs covered:** FR1, FR2, FR3, FR4, FR5, FR6, FR7, FR8, FR31

### Epic 3: Lançamentos Financeiros (Despesas e Receitas)
Permitir que o usuário registre, edite, visualize e exclua todos os tipos de entrada e saída financeira, visualizando cálculos de parcelamento em tempo real.
**FRs covered:** FR9, FR10, FR11, FR12, FR13, FR14, FR15, FR16, FR17, FR18, FR19, FR20, FR21, FR22, FR23, FR24, FR33

### Epic 4: Consolidação e Projeção Mensal (Painel de 24 Meses)
Dar ao usuário o poder de visualizar o impacto de suas decisões financeiras 24 meses no futuro, através de uma matriz consolidada com totais, categorias e sobras acumuladas.
**FRs covered:** FR25, FR26, FR27, FR28, FR29, FR30

## Epic 1: Autenticação e Navegação Segura (Auth & Shell)

Permitir que o usuário acesse o sistema de forma segura via Google e que seus dados fiquem completamente isolados. Além disso, fornece o "esqueleto" visual (shell e barra de navegação) para suportar as próximas funcionalidades.

### Story 1.1: Esqueleto Visual e Barra de Navegação

As a Usuário,
I want visualizar a aplicação com o tema visual correto e acessar a barra de navegação principal,
So that eu possa me familiarizar com a interface e me preparar para acessar as funcionalidades financeiras.

**Acceptance Criteria:**

**Given** que o usuário acessa o sistema pelo Desktop ou Mobile
**When** a interface básica é renderizada
**Then** os tokens de cores, tipografia e bordas arredondadas devem estar aplicados
**And** no Desktop deve haver uma "Pill Navigation" superior, e no Mobile um "Bottom Navigation" com 5 abas em ordem fixa

### Story 1.2: Autenticação via Google (OAuth 2.0)

As a Usuário não autenticado,
I want entrar com minha conta do Google,
So that eu possa acessar o Aureus sem precisar criar uma nova senha, garantindo a criação ou vínculo da minha conta local.

**Acceptance Criteria:**

**Given** que o usuário não está autenticado e está na tela de entrada
**When** ele clica em "Entrar com Google" e autoriza o acesso
**Then** o sistema valida a identidade externa
**And** cria uma conta local (se for o primeiro acesso) ou reconhece a conta existente
**And** redireciona o usuário para a aba "Consolidação" de forma autenticada

### Story 1.3: Proteção de Sessão e Isolamento de Dados

As a Usuário logado,
I want que minhas requisições sejam feitas sob uma sessão segura e isolada,
So that nenhum outro usuário do sistema possa ler ou alterar minhas informações.

**Acceptance Criteria:**

**Given** um usuário recém autenticado com sucesso
**When** o backend emite as credenciais
**Then** a sessão deve ser mantida via Cookie HttpOnly
**And** todas as chamadas futuras de API devem filtrar dados obrigatoriamente pela coluna `usuario_id`

### Story 1.4: Encerrar Sessão (Logout)

As a Usuário logado,
I want ter a opção de encerrar minha sessão,
So that eu possa proteger meus dados ao deixar o dispositivo.

**Acceptance Criteria:**

**Given** que o usuário está autenticado
**When** ele aciona o comando "Sair"
**Then** o Cookie HttpOnly é invalidado e o estado autenticado local é limpo
**And** o usuário é redirecionado para a tela de login inicial

## Epic 2: Configuração Financeira Básica (Contas e Categorias)

Permitir que o usuário configure suas origens de dinheiro e suas categorias macro, preparando o sistema para que as movimentações possam ser cadastradas.

### Story 2.1: Gestão de Contas (CRUD)

As a Usuário,
I want criar, visualizar, editar e excluir minhas Contas financeiras,
So that eu possa cadastrar locais de origem ou destino (ex: Conta Corrente, Cartão de Crédito) para minhas movimentações.

**Acceptance Criteria:**

**Given** que o usuário está autenticado
**When** ele acessa o gerenciamento de Contas e preenche Descrição (obrigatório) e Observações (opcional)
**Then** ele deve ser capaz de criar a Conta e visualizá-la na listagem
**And** ele pode editar esses campos posteriormente
**And** pode excluir a Conta, exceto se o backend retornar erro informando que ela já possui Movimentações vinculadas (neste caso, a exclusão é bloqueada)

### Story 2.2: Gestão de Categorias (CRUD)

As a Usuário,
I want criar, visualizar, editar e excluir Categorias financeiras,
So that eu tenha como classificar minhas entradas e saídas de forma organizada.

**Acceptance Criteria:**

**Given** que o usuário está autenticado
**When** ele acessa o gerenciamento de Categorias e preenche Descrição e Observações
**Then** ele pode criar e visualizar a listagem de Categorias
**And** pode atualizar os dados
**And** a exclusão só será permitida se não existirem movimentações associadas a essa Categoria

### Story 2.3: Restrição de Cadastro sem Dependências

As a Usuário com conta recém-criada,
I want ser instruído a criar minhas Contas e Categorias antes de fazer lançamentos financeiros,
So that eu não me depare com formulários vazios de seleção e erros no momento de salvar.

**Acceptance Criteria:**

**Given** que o usuário não possui nenhuma Conta ou Nenhuma Categoria cadastrada
**When** ele abre o Modal ou Bottom Sheet para adicionar qualquer Movimentação
**Then** o formulário deve impedir o cadastro e os selects devem mostrar que não há opções
**And** uma mensagem amigável de "Empty State" deve orientá-lo a cadastrar as dependências primeiro, com um link direto para a tela de gerenciamento

## Epic 3: Lançamentos Financeiros (Despesas e Receitas)

Permitir que o usuário registre, edite, visualize e exclua todos os tipos de entrada e saída financeira, visualizando cálculos de parcelamento em tempo real.

### Story 3.1: Lançamentos Fixos (Despesas e Receitas)

As a Usuário,
I want registrar receitas e despesas fixas recorrentes,
So that eu cadastre valores que se repetem todo mês sem me preocupar com data de término.

**Acceptance Criteria:**

**Given** que existem Contas e Categorias criadas
**When** o usuário preenche e salva o formulário Modal/Bottom Sheet de itens Fixos
**Then** os dados são listados na aba correspondente
**And** ao editar um valor, a atualização substitui o dado anterior no banco
**And** ele pode excluir o registro caso a despesa/receita deixe de existir permanentemente

### Story 3.2: Lançamentos Variáveis e Parcelados (Despesas e Receitas)

As a Usuário,
I want registrar compras e entradas parceladas,
So that o sistema saiba que aquele valor afeta apenas um intervalo específico de meses.

**Acceptance Criteria:**

**Given** que o usuário está no formulário Modal/Bottom Sheet de itens Variáveis
**When** ele preenche os campos numéricos e de data
**Then** ele é obrigado a informar Valor Parcela, Nº Parcelas e Primeira Parcela
**And** os campos calculados (Valor Total e Última Parcela) são gerados em tempo real na interface (Calculation Preview)
**And** o item é salvo na base de dados com esses metadados para projeção futura

### Story 3.3: Lançamentos Variáveis à Vista (Parcela Única)

As a Usuário,
I want informar compras ou entradas pontuais que não foram parceladas,
So that a movimentação incida de forma isolada em um único mês da minha matriz.

**Acceptance Criteria:**

**Given** que estou preenchendo o formulário de itens Variáveis
**When** eu defino o Nº Parcelas como "1"
**Then** o sistema entende e valida a entrada como compra à vista
**And** o Valor Total reflete instantaneamente o Valor Parcela, e a Última Parcela fica igual à Primeira
**And** o registro é processado e salvo com sucesso

### Story 3.4: Sincronização de Visão Mensal e Filtro Global (Listas)

As a Usuário,
I want que minhas abas de listagem mostrem apenas o que afeta o mês selecionado atualmente ou, opcionalmente, todo o histórico,
So that a tela fique limpa e eu encontre rapidamente as transações relevantes.

**Acceptance Criteria:**

**Given** que a lista de Movimentações (Despesas ou Receitas Variáveis) está sendo exibida
**When** o mês X está ativo no estado global de navegação
**Then** a lista exibe nativamente apenas as movimentações cujas parcelas cobrem aquele mês X
**And** o usuário pode clicar no "Filtro Global / Ver Todos" para desativar a sincronização do mês e listar o histórico completo

## Epic 4: Consolidação e Projeção Mensal (Painel de 24 Meses)

Dar ao usuário o poder de visualizar o impacto de suas decisões financeiras 24 meses no futuro, através de uma matriz consolidada com totais, categorias e sobras acumuladas.

### Story 4.1: Seleção de Mês e Estrutura do Data Grid

As a Usuário,
I want selecionar um mês inicial e ver a estrutura base de uma grade de 24 colunas,
So that eu possa navegar no tempo com facilidade através do scroll horizontal sem perder o contexto.

**Acceptance Criteria:**

**Given** que acesso a aba "Consolidação"
**When** a tela carrega ou eu escolho um mês no calendário global (Month Picker)
**Then** o sistema renderiza horizontalmente 24 colunas de meses a partir do selecionado
**And** a primeira coluna lateral (que contém os nomes das Contas/Categorias) deve ficar fixa ("sticky") enquanto eu realizo o scroll horizontal

### Story 4.2: Bloco de Consolidação por Conta (Receitas e Despesas)

As a Usuário,
I want visualizar sub-tabelas que agrupam meus ganhos e gastos por Conta,
So that eu saiba exatamente o saldo movimentado em cada origem de dinheiro mês a mês.

**Acceptance Criteria:**

**Given** a estrutura da Matriz de 24 meses carregada
**When** os dados financeiros são inseridos no Data Grid
**Then** o sistema exibe o "Bloco Receitas", somando variáveis e fixas por mês para cada Conta existente
**And** exibe o "Bloco Despesas", repetindo a mesma lógica para as saídas
**And** se uma conta não tiver movimentações num determinado mês, a célula exibe o valor zerado (Empty State)

### Story 4.3: Bloco Analítico de Categorias (Valores e Percentuais)

As a Usuário,
I want visualizar as Despesas somadas por Categoria e sua representação percentual,
So that eu tenha uma visão imediata de quais macro-áreas estão consumindo minha renda.

**Acceptance Criteria:**

**Given** o carregamento da Matriz de 24 meses
**When** a seção de Categorias for renderizada
**Then** o "Bloco Categorias (R$)" deve calcular a soma de todos os gastos por categoria em cada mês
**And** o "Bloco Categorias (%)" deve exibir a proporção matemática `(Gasto da Categoria / Gasto Total do Mês) * 100`
**And** se o mês não possuir nenhum gasto, o percentual de todas as categorias deve apresentar "0%" para evitar erros de divisão

### Story 4.4: Bloco de Resumo Geral e Sobra Acumulada

As a Usuário,
I want visualizar a "Sobra" de cada mês e o saldo retroativo sendo propagado para o futuro,
So that eu saiba com exatidão se minhas finanças estão evoluindo de forma sustentável ou se vou entrar no negativo lá na frente.

**Acceptance Criteria:**

**Given** todos os blocos anteriores calculados e visíveis
**When** a matriz de Consolidação renderiza a linha final
**Then** deve existir uma linha "Total Gasto" (somatória de todas as despesas) e uma linha "Sobra do Mês" (Receitas - Despesas daquele mês isolado)
**And** deve existir uma linha "Sobra Retroativa Acumulada", cujo valor no mês `n` é a `Sobra do Mês n` somada à `Sobra Retroativa Acumulada do Mês n-1`
