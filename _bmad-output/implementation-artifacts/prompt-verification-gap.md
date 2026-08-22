Read /home/guilhermepagio/developer/workspace/projeto-aureus/_bmad/render/bmad-build/projeto-aureus-f35e84214c10/8e641952afc4e7111c01/review-prompts/verification-gap.md completely and follow it as your review instructions.

Review content:

diff --git a/_bmad-output/implementation-artifacts/epic-3-context.md b/_bmad-output/implementation-artifacts/epic-3-context.md
index 8b93a27..1eba603 100644
--- a/_bmad-output/implementation-artifacts/epic-3-context.md
+++ b/_bmad-output/implementation-artifacts/epic-3-context.md
@@ -4,7 +4,7 @@
 
 ## Goal
 
-Permitir que o usuário registre, visualize, edite e exclua todas as suas movimentações financeiras — despesas e receitas, fixas ou variáveis — com cálculo em tempo real de parcelamentos, distribuição precisa de centavos residuais e sincronização contextual com o filtro de mês global.
+This epic establishes the core transaction engine of the Aureus application by implementing the complete lifecycle (Create, Read, Update, Delete) for both fixed and variable financial records (expenses and incomes). It handles complex logic for installments, including real-time calculation previews, precise cent distribution, and projection of recurring values. Furthermore, it ensures the user interface remains synchronized across tabs through global state management and automatic cache invalidation, providing a seamless data entry experience.
 
 ## Stories
 
@@ -15,41 +15,32 @@ Permitir que o usuário registre, visualize, edite e exclua todas as suas movime
 
 ## Requirements & Constraints
 
-- **Tipos de Movimentação**:
-  - **Fixas (Despesas e Receitas)**: Valores recorrentes mensais contínuos (ex: aluguel, internet, salário). Campos: Descrição, Valor, Conta, Categoria e Data de Início/Vigência. Não possuem parcelamento e são projetadas continuamente para a frente. Alterações de valor atualizam o registro in-place, sem versionamento de histórico passado.
-  - **Variáveis (Despesas e Receitas)**: Movimentações pontuais ou parceladas. Campos obrigatórios: Descrição, Valor Parcela, Nº Parcelas (≥ 1), Primeira Parcela (competência inicial), Categoria, Conta. Despesas Variáveis possuem campos adicionais para conferência: Local Compra e Data Compra (opcionais). Campos calculados automaticamente: Valor Total e Última Parcela.
-- **Distribuição de Centavos (Penny Rounding)**: Ao dividir o Valor Total pelo número de parcelas, divisões inexatas com centavos fracionados devem alocar a diferença residual na primeira parcela, garantindo que a soma exata das parcelas corresponda rigorosamente ao Valor Total.
-- **Parcela Única (À Vista)**: Quando o número de parcelas for igual a 1, o Valor Total iguala-se ao Valor da Parcela e a Última Parcela coincide com a Primeira Parcela, incidindo unicamente no mês de competência indicado.
-- **Horizonte de Parcelas**: Parcelamentos com duração superior a 24 meses devem ser persistidos integralmente no banco de dados para projeções futuras.
-- **Dependência de Cadastro**: O cadastro de qualquer movimentação exige a seleção obrigatória de uma Conta e uma Categoria existentes. Se não houver Contas ou Categorias cadastradas, o formulário deve bloquear a submissão e orientar a criação das dependências.
-- **Sincronização de Visão Mensal**: Por padrão, as listagens de Despesas e Receitas filtram e exibem apenas os lançamentos vigentes no mês ativo selecionado no estado global.
-- **Filtro Global ("Ver Todos")**: O usuário pode alternar um controle de filtro no cabeçalho da listagem para desativar a restrição mensal e visualizar todas as transações cadastradas.
+- **Variable Transactions:** Must support installment logic. Required fields: Description, Installment Value, Number of Installments (>= 1), First Installment (month/year), Category, and Account. Optional fields: Purchase Location, Purchase Date, Notes.
+- **Auto-Calculated Fields:** Total Value (`Installment Value × Number of Installments`) and Last Installment (`First Installment + (Number of Installments - 1) months`) must be calculated and displayed in real-time. These fields are not editable by the user.
+- **Single Installment (At-Sight):** Must support single installments (Number of Installments = 1), where Total Value equals Installment Value, and Last Installment equals First Installment. This applies the transaction to a single month.
+- **Fixed Transactions:** Must support recurring values without installments. Required fields: Description, Value, Category, Account, and Start Date. The value must be projected forward across all subsequent months. Modifying the value updates the projection from the new effective date.
+- **Data Synchronization:** Transaction lists must only display items relevant to the currently active month in the global state, unless the "Global Filter / View All" toggle is activated.
+- **Cache Invalidation:** Any mutation (create, edit, delete) must automatically invalidate the queries for the lists and the Consolidation panel to reflect changes in real-time without page reloads.
+- **Referential Integrity:** Transactions cannot be created if no Accounts or Categories exist. Selectors will display empty states linking to the respective creation forms if dependencies are missing.
 
 ## Technical Decisions
 
-- **Isolamento de Dados (Multi-Tenancy Lógico)**: Todas as tabelas de movimentações (`despesas`, `receitas`) possuem a coluna `usuario_id`. Todas as operações no backend devem validar e filtrar estritamente pelo ID do usuário autenticado na sessão (JWT em HttpOnly Cookie).
-- **Integridade Referencial e Bloqueio de Deleção**: `despesas` e `receitas` possuem chaves estrangeiras para `contas` e `categorias` configuradas com `ON DELETE RESTRICT` para impedir exclusão inadvertida de dependências vinculadas.
-- **Precisão Numérica**: Valores monetários devem ser manipulados como `numeric` / `BigDecimal` no backend e tratados com precisão decimal no frontend, evitando imprecisões de ponto flutuante.
-- **Cálculo de Parcelamento**:
-  - `Valor Total = Valor Parcela × Nº Parcelas` (ou distribuição reversa caso o total seja a entrada).
-  - `Data Última Parcela = Primeira Parcela + (Nº Parcelas - 1) meses`.
-  - Tratamento de centavos: `Parcela Base = floor(Total / N)`, `Primeira Parcela = Parcela Base + (Total - Parcela Base * N)`.
-- **Competências Temporais**: Meses de vigência e parcelas utilizam a representação de competência mensal (`YearMonth` ou formato de data no primeiro dia do mês `YYYY-MM-01`).
-- **Gerenciamento de Estado e Cache**:
-  - Estado do mês ativo compartilhado entre abas gerenciado via **Zustand**.
-  - Chamadas de API e cache gerenciados via **React Query (TanStack Query)**, com invalidação automática de cache nas mutações de criação, edição e exclusão de movimentações e consolidação.
+- **Data Isolation:** All transaction entities must include a `usuario_id` column. All repository queries must enforce filtering by the authenticated user's ID to ensure logical multi-tenancy.
+- **State Management:** Use Zustand exclusively for cross-tab shared context (e.g., the globally selected "Month" used to filter transaction lists). Local UI state remains in React `useState`.
+- **Data Fetching & Caching:** Use React Query (TanStack Query) with Axios/Fetch for all API calls. Mutations must trigger automatic cache invalidation to keep lists synchronized.
+- **API Paradigm:** Strict REST API (JSON over HTTP) communication between the React frontend and the Spring Boot backend.
+- **Database Schema:** Schema changes for new transaction tables must be managed via explicit Flyway migration scripts.
 
 ## UX & Interaction Patterns
 
-- **Formulários em Modal / Bottom Sheet**: A criação e edição de movimentações abrem em Modal centralizado (Desktop) ou Bottom Sheet ancorado na base (Mobile), implementando Focus Trapping para acessibilidade.
-- **Calculation Preview em Tempo Real**: Os formulários de movimentações variáveis incluem um bloco inline com fundo suave (`teal-light`) que calcula e exibe instantaneamente o Valor Total, o Valor da Parcela e o mês da Última Parcela conforme o usuário digita.
-- **Ações Rápidas de Criação**: Botão de Ação Flutuante (FAB) ou botão primário "Adicionar" nas abas de movimentação.
-- **Alinhamento e Formatação**: Todos os valores numéricos e monetários nas listagens devem ser alinhados à direita e utilizar fontes tabulares (`tabular-nums`).
-- **Feedback e Confirmação**: Exclusões exigem confirmação explícita via modal. Sucessos e falhas disparam Toasts informativos no topo da tela com borda semântica.
-- **Empty States**: Listagens sem lançamentos no mês ativo (ou histórico vazio) exibem ilustrações esmaecidas, mensagem contextual e CTA para registro.
+- **Forms (Modals/Bottom Sheets):** Registration forms must not be inline. They should open in a centered Modal on desktop (`radius-md`) and a docked Bottom Sheet on mobile (with heavy top shadow). Keyboard focus must be trapped inside the overlay.
+- **Calculation Preview:** Installment forms must include an inline calculation block (using `teal-light` background and `color.brand.primary` text) that instantly updates and displays the Total Impact and Last Installment date as the user types.
+- **Lists and Empty States:** The main view for transaction tabs is a list. Empty lists must display a standard Empty State block (centered, dashed border, muted icon, and CTA).
+- **Destructive Actions:** Deleting a transaction must trigger an explicit confirmation Modal to prevent accidental data loss.
+- **Success/Error Feedback:** Field-level validation should show a red border and message inline. Success states should use non-blocking floating Toasts at the top of the screen.
 
 ## Cross-Story Dependencies
 
-- **Dependência do Epic 1**: Necessita da infraestrutura de autenticação via Google, extração de sessão segura (JWT HttpOnly) e casca de navegação (Shell/Pill/Bottom Nav).
-- **Dependência do Epic 2**: Requer o CRUD de Contas e Categorias funcional para fornecer as opções obrigatórias de seleção nos formulários.
-- **Habilitador para o Epic 4**: As movimentações e parcelas registradas neste épico constituem a fonte primária de dados para a matriz de projeção de 24 meses do Painel de Consolidação.
+- **Epic 1 (Auth & Shell):** Requires the authenticated user context (`usuario_id`) and the application shell for navigation.
+- **Epic 2 (Accounts & Categories):** Requires the existence of Accounts and Categories, as transactions require foreign keys to both.
+- **Epic 4 (Consolidation):** The transaction data generated in this epic is consumed directly by the 24-month Consolidation panel.
diff --git a/_bmad-output/implementation-artifacts/sprint-status.yaml b/_bmad-output/implementation-artifacts/sprint-status.yaml
index 6914d09..45e67c1 100644
--- a/_bmad-output/implementation-artifacts/sprint-status.yaml
+++ b/_bmad-output/implementation-artifacts/sprint-status.yaml
@@ -51,8 +51,8 @@ development_status:
   epic-3: in-progress
   3-1-lançamentos-fixos-despesas-e-receitas-com-vigência: done
   3-2-lançamentos-variáveis-e-parcelados-com-arredondamento-de-cen: done
-  3-3-lançamentos-variáveis-à-vista-parcela-única: backlog
-  3-4-sincronização-de-visão-mensal-invalidação-de-cache-e-filtro: backlog
+  3-3-lançamentos-variáveis-à-vista-parcela-única: discarded # Absorvida pela story 3-2 (lançamentos com 1 parcela)
+  3-4-sincronização-de-visão-mensal-invalidação-de-cache-e-filtro: in-progress
   epic-3-retrospective: optional
 
   epic-4: backlog
diff --git a/_bmad-output/planning-artifacts/epics.md b/_bmad-output/planning-artifacts/epics.md
index 4fe5a66..c110156 100644
--- a/_bmad-output/planning-artifacts/epics.md
+++ b/_bmad-output/planning-artifacts/epics.md
@@ -138,7 +138,7 @@ FR30: Epic 4 - Bloco Resumo Geral
 
 * **Epic 1: Autenticação e Navegação Segura (Auth & Shell)** — Permitir que o usuário acesse o sistema de forma segura via Google e que seus dados fiquem completamente isolados por usuário, fornecendo a casca visual e navegação principal (Pill Nav Desktop e Bottom Nav Mobile).
 * **Epic 2: Configuração Financeira Básica (Contas e Categorias)** — Permitir que o usuário configure suas origens financeiras e categorias macro com integridade referencial protegida contra exclusões acidentais.
-* **Epic 3: Lançamentos Financeiros (Despesas e Receitas)** — Permitir o registro, edição, listagem e exclusão de receitas e despesas (fixas e variáveis), com pré-visualização de parcelas, arredondamento de centavos e sincronização de filtros.
+* **Epic 3: Lançamentos Financeiros (Despesas e Receitas)** — Permitir o registro, edição, listagem e exclusão de receitas e despesas (fixas e variáveis), com pré-visualização de parcelas e sincronização de filtros.
 * **Epic 4: Consolidação e Projeção Mensal (Painel de 24 Meses)** — Matriz analítica de projeção de 24 meses com subtotais por conta, despesas por categoria (R$ e %), resumo mensal, saldo histórico acumulado e navegação por Swipe mobile.
 
 ---
@@ -266,7 +266,7 @@ So that eu não enfrente formulários com seletores vazios ou erros de validaç
 
 ## Epic 3: Lançamentos Financeiros (Despesas e Receitas)
 
-Permitir que o usuário registre, edite, visualize e exclua todos os tipos de entrada e saída financeira, com pré-visualização de cálculos, distribuição precisa de centavos e sincronização de filtros.
+Permitir que o usuário registre, edite, visualize e exclua todos os tipos de entrada e saída financeira, com pré-visualização de cálculos e sincronização de filtros.
 
 ### Story 3.1: Lançamentos Fixos (Despesas e Receitas) com Vigência
 
@@ -282,10 +282,10 @@ So that valores mensais contínuos sejam projetados automaticamente ao longo de
 **And** ao editar o valor de um item fixo, a alteração define o novo valor projetado a partir da data de vigência editada
 **And** o usuário pode excluir o registro fixo permanentemente com confirmação via modal
 
-### Story 3.2: Lançamentos Variáveis e Parcelados com Arredondamento de Centavos
+### Story 3.2: Lançamentos Variáveis e Parcelados
 
 As a Usuário,
-I want registrar compras e entradas parceladas com cálculo em tempo real e distribuição exata de centavos,
+I want registrar compras e entradas parceladas com cálculo em tempo real,
 So that o sistema projete com exatidão as parcelas sem divergências de arredondamento.
 
 **Acceptance Criteria:**
@@ -293,10 +293,11 @@ So that o sistema projete com exatidão as parcelas sem divergências de arredon
 **Given** que o usuário preenche o formulário de itens Variáveis
 **When** ele informa Valor Total, Nº de Parcelas (>= 1) e Primeira Parcela (mês/ano)
 **Then** o componente Calculation Preview calcula e exibe em tempo real o Valor da Parcela e a Última Parcela no formato `YearMonth`
-**And** quando a divisão do Valor Total pelo Nº de Parcelas gerar centavos fracionados, o resíduo é alocado na primeira parcela para que a soma das parcelas seja rigorosamente igual ao Valor Total
 **And** parcelamentos com duração superior a 24 meses são persistidos integralmente e projetados na grade de acordo com a janela de meses visualizada
 
-### Story 3.3: Lançamentos Variáveis à Vista (Parcela Única)
+### Story 3.3: Lançamentos Variáveis à Vista (Parcela Única) [DESCARTADA/ABSORVIDA]
+
+> **Nota de Implementação:** Esta história foi absorvida pela **Story 3.2**. O conceito de lançamentos à vista foi implementado nativamente suportando "Nº Parcelas = 1" no mesmo fluxo.
 
 As a Usuário,
 I want informar compras ou entradas pontuais não parceladas,
diff --git a/_bmad-output/planning-artifacts/prds/prd-projeto-aureus-docs-2026-08-02/prd.md b/_bmad-output/planning-artifacts/prds/prd-projeto-aureus-docs-2026-08-02/prd.md
index 1c57682..9350712 100644
--- a/_bmad-output/planning-artifacts/prds/prd-projeto-aureus-docs-2026-08-02/prd.md
+++ b/_bmad-output/planning-artifacts/prds/prd-projeto-aureus-docs-2026-08-02/prd.md
@@ -67,7 +67,7 @@ O nome remete ao *aureus*, moeda de ouro da Roma Antiga, simbolizando valor e es
 >    - **Descrição:** "Fone de Ouvido Air Pods Pro 2nd Gen" *(obrigatório)*
 >    - **Local Compra:** "Mercado Livre" *(opcional — facilita conferência de fatura)*
 >    - **Data Compra:** 15/08/2026 *(opcional — data em que a compra foi computada no cartão)*
->    - **Valor Parcela:** R$ 124,00 *(obrigatório — arredondado para cima para evitar divergência de centavos)*
+>    - **Valor Parcela:** R$ 124,00 *(obrigatório)*
 >    - **Nº Parcelas:** 3 *(obrigatório)*
 >    - **Valor Total:** R$ 372,00 *(calculado: Valor Parcela × Nº Parcelas)*
 >    - **Primeira Parcela:** 01/09/2026 *(obrigatório — dia 1 do mês da fatura)*
diff --git a/frontend/src/hooks/useDespesasFixas.ts b/frontend/src/hooks/useDespesasFixas.ts
index 389f3e0..50fb3f9 100644
--- a/frontend/src/hooks/useDespesasFixas.ts
+++ b/frontend/src/hooks/useDespesasFixas.ts
@@ -102,6 +102,7 @@ export const useCreateDespesaFixa = () => {
     mutationFn: createDespesaFixa,
     onSuccess: () => {
       queryClient.invalidateQueries({ queryKey: ['despesas-fixas'] });
+      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
       toast.success('Despesa fixa criada com sucesso!');
     },
     onError: (error: Error) => {
@@ -116,6 +117,7 @@ export const useUpdateDespesaFixa = () => {
     mutationFn: updateDespesaFixa,
     onSuccess: () => {
       queryClient.invalidateQueries({ queryKey: ['despesas-fixas'] });
+      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
       toast.success('Despesa fixa atualizada com sucesso!');
     },
     onError: (error: Error) => {
@@ -130,6 +132,7 @@ export const useDeleteDespesaFixa = () => {
     mutationFn: deleteDespesaFixa,
     onSuccess: () => {
       queryClient.invalidateQueries({ queryKey: ['despesas-fixas'] });
+      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
       toast.success('Despesa fixa excluída com sucesso!');
     },
     onError: (error: Error) => {
diff --git a/frontend/src/hooks/useDespesasVariaveis.ts b/frontend/src/hooks/useDespesasVariaveis.ts
index 7a5a47b..b3232de 100644
--- a/frontend/src/hooks/useDespesasVariaveis.ts
+++ b/frontend/src/hooks/useDespesasVariaveis.ts
@@ -111,6 +111,7 @@ export const useCreateDespesaVariavel = () => {
     mutationFn: createDespesaVariavel,
     onSuccess: () => {
       queryClient.invalidateQueries({ queryKey: ['despesas-variaveis'] });
+      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
       toast.success('Despesa variável criada com sucesso!');
     },
     onError: (error: Error) => {
@@ -125,6 +126,7 @@ export const useUpdateDespesaVariavel = () => {
     mutationFn: updateDespesaVariavel,
     onSuccess: () => {
       queryClient.invalidateQueries({ queryKey: ['despesas-variaveis'] });
+      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
       toast.success('Despesa variável atualizada com sucesso!');
     },
     onError: (error: Error) => {
@@ -139,6 +141,7 @@ export const useDeleteDespesaVariavel = () => {
     mutationFn: deleteDespesaVariavel,
     onSuccess: () => {
       queryClient.invalidateQueries({ queryKey: ['despesas-variaveis'] });
+      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
       toast.success('Despesa variável excluída com sucesso!');
     },
     onError: (error: Error) => {
diff --git a/frontend/src/hooks/useReceitasFixas.ts b/frontend/src/hooks/useReceitasFixas.ts
index 919ec10..cdcb61a 100644
--- a/frontend/src/hooks/useReceitasFixas.ts
+++ b/frontend/src/hooks/useReceitasFixas.ts
@@ -102,6 +102,7 @@ export const useCreateReceitaFixa = () => {
     mutationFn: createReceitaFixa,
     onSuccess: () => {
       queryClient.invalidateQueries({ queryKey: ['receitas-fixas'] });
+      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
       toast.success('Receita fixa criada com sucesso!');
     },
     onError: (error: Error) => {
@@ -116,6 +117,7 @@ export const useUpdateReceitaFixa = () => {
     mutationFn: updateReceitaFixa,
     onSuccess: () => {
       queryClient.invalidateQueries({ queryKey: ['receitas-fixas'] });
+      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
       toast.success('Receita fixa atualizada com sucesso!');
     },
     onError: (error: Error) => {
@@ -130,6 +132,7 @@ export const useDeleteReceitaFixa = () => {
     mutationFn: deleteReceitaFixa,
     onSuccess: () => {
       queryClient.invalidateQueries({ queryKey: ['receitas-fixas'] });
+      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
       toast.success('Receita fixa excluída com sucesso!');
     },
     onError: (error: Error) => {
diff --git a/frontend/src/hooks/useReceitasVariaveis.ts b/frontend/src/hooks/useReceitasVariaveis.ts
index 376e099..2e0f949 100644
--- a/frontend/src/hooks/useReceitasVariaveis.ts
+++ b/frontend/src/hooks/useReceitasVariaveis.ts
@@ -107,6 +107,7 @@ export const useCreateReceitaVariavel = () => {
     mutationFn: createReceitaVariavel,
     onSuccess: () => {
       queryClient.invalidateQueries({ queryKey: ['receitas-variaveis'] });
+      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
       toast.success('Receita variável criada com sucesso!');
     },
     onError: (error: Error) => {
@@ -121,6 +122,7 @@ export const useUpdateReceitaVariavel = () => {
     mutationFn: updateReceitaVariavel,
     onSuccess: () => {
       queryClient.invalidateQueries({ queryKey: ['receitas-variaveis'] });
+      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
       toast.success('Receita variável atualizada com sucesso!');
     },
     onError: (error: Error) => {
@@ -135,6 +137,7 @@ export const useDeleteReceitaVariavel = () => {
     mutationFn: deleteReceitaVariavel,
     onSuccess: () => {
       queryClient.invalidateQueries({ queryKey: ['receitas-variaveis'] });
+      queryClient.invalidateQueries({ queryKey: ['consolidacao'] });
       toast.success('Receita variável excluída com sucesso!');
     },
     onError: (error: Error) => {
diff --git a/frontend/src/pages/Categorias/CategoriasPage.tsx b/frontend/src/pages/Categorias/CategoriasPage.tsx
index 0af6929..fda3d02 100644
--- a/frontend/src/pages/Categorias/CategoriasPage.tsx
+++ b/frontend/src/pages/Categorias/CategoriasPage.tsx
@@ -3,6 +3,7 @@ import React, { useState } from 'react';
 import { useCategorias, type Categoria } from '../../hooks/useCategorias';
 import CategoriaFormModal from './components/CategoriaFormModal';
 import DeleteConfirmModal from './components/DeleteConfirmModal';
+import EmptyState from '../../components/ui/EmptyState';
 
 const CategoriasPage: React.FC = () => {
   const { data: categorias, isLoading, isError } = useCategorias();
@@ -44,22 +45,11 @@ const CategoriasPage: React.FC = () => {
         </button>
       </div>
       {isEmpty ? (
-        <div className="text-center py-12 bg-white rounded-lg shadow-sm border border-gray-200 mt-4">
-          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
-            <path vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
-          </svg>
-          <h3 className="mt-2 text-sm font-semibold text-gray-900">Nenhuma categoria</h3>
-          <p className="mt-1 text-sm text-gray-500">Comece criando sua primeira categoria.</p>
-          <div className="mt-6">
-            <button
-              onClick={handleCreate}
-              type="button"
-              className="cursor-pointer inline-flex items-center rounded-md bg-primary px-3 py-1 text-sm font-semibold text-white shadow-sm hover:bg-primary-light focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
-            >
-              Nova Categoria
-            </button>
-          </div>
-        </div>
+        <EmptyState
+          title="Nenhuma categoria"
+          description="Comece criando sua primeira categoria."
+          action={{ label: 'Nova Categoria', onClick: handleCreate }}
+        />
       ) : (
         <div className="bg-white shadow sm:rounded-2xl overflow-visible mt-2 w-full">
           <table className="min-w-max w-full divide-y divide-gray-200 table-fixed">
diff --git a/frontend/src/pages/Contas/ContasPage.tsx b/frontend/src/pages/Contas/ContasPage.tsx
index aff1ea3..df0ae67 100644
--- a/frontend/src/pages/Contas/ContasPage.tsx
+++ b/frontend/src/pages/Contas/ContasPage.tsx
@@ -3,6 +3,7 @@ import React, { useState } from 'react';
 import { useContas, type Conta } from '../../hooks/useContas';
 import ContaFormModal from './components/ContaFormModal';
 import DeleteConfirmModal from './components/DeleteConfirmModal';
+import EmptyState from '../../components/ui/EmptyState';
 
 const ContasPage: React.FC = () => {
   const { data: contas, isLoading, isError } = useContas();
@@ -44,22 +45,11 @@ const ContasPage: React.FC = () => {
         </button>
       </div>
       {isEmpty ? (
-        <div className="text-center py-12 bg-white rounded-lg shadow-sm border border-gray-200 mt-4">
-          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
-            <path vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
-          </svg>
-          <h3 className="mt-2 text-sm font-semibold text-gray-900">Nenhuma conta</h3>
-          <p className="mt-1 text-sm text-gray-500">Comece criando sua primeira conta.</p>
-          <div className="mt-6">
-            <button
-              onClick={handleCreate}
-              type="button"
-              className="cursor-pointer inline-flex items-center rounded-md bg-primary px-3 py-1 text-sm font-semibold text-white shadow-sm hover:bg-primary-light focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
-            >
-              Nova Conta
-            </button>
-          </div>
-        </div>
+        <EmptyState
+          title="Nenhuma conta"
+          description="Comece criando sua primeira conta."
+          action={{ label: 'Nova Conta', onClick: handleCreate }}
+        />
       ) : (
         <div className="bg-white shadow sm:rounded-2xl overflow-visible mt-2 w-full">
           <table className="min-w-max w-full divide-y divide-gray-200 table-fixed">
diff --git a/frontend/src/pages/DespesasFixas/DespesasFixasPage.tsx b/frontend/src/pages/DespesasFixas/DespesasFixasPage.tsx
index e661d4d..319fe96 100644
--- a/frontend/src/pages/DespesasFixas/DespesasFixasPage.tsx
+++ b/frontend/src/pages/DespesasFixas/DespesasFixasPage.tsx
@@ -3,9 +3,12 @@ import React, { useState } from 'react';
 import { useDespesasFixas, type DespesaFixa } from '../../hooks/useDespesasFixas';
 import DespesaFixaFormModal from './components/DespesaFixaFormModal';
 import DeleteConfirmModal from './components/DeleteConfirmModal';
+import { useMonthStore } from '../../store/monthStore';
+import EmptyState from '../../components/ui/EmptyState';
 
 const DespesasFixasPage: React.FC = () => {
   const { data: despesas, isLoading, isError } = useDespesasFixas();
+  const { isGlobalFilterActive, toggleGlobalFilter } = useMonthStore();
   const [isFormModalOpen, setIsFormModalOpen] = useState(false);
   const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
   const [selectedDespesa, setSelectedDespesa] = useState<DespesaFixa | null>(null);
@@ -25,10 +28,15 @@ const DespesasFixasPage: React.FC = () => {
     setIsDeleteModalOpen(true);
   };
 
+  const filteredDespesas = React.useMemo(() => {
+    if (!despesas) return [];
+    return despesas; // Despesas Fixas não possuem dataInicio na entidade atual para filtrar.
+  }, [despesas]);
+
   if (isLoading) return <div className="p-6">Carregando despesas fixas...</div>;
   if (isError) return <div className="p-6 text-red-600">Erro ao carregar despesas fixas.</div>;
 
-  const isEmpty = !despesas || despesas.length === 0;
+  const isEmpty = filteredDespesas.length === 0;
 
   return (
     <div className="px-4 pb-4 w-full">
@@ -36,30 +44,34 @@ const DespesasFixasPage: React.FC = () => {
         <div className="pl-2 border-l-4 border-red-600">
           <h1 className="text-2xl font-bold text-gray-800">Despesas Fixas</h1>
         </div>
-        <button
-          onClick={handleCreate}
-          className="cursor-pointer h-8 px-4 flex items-center justify-center bg-red-600 hover:bg-red-700 text-white rounded-md text-xs font-semibold whitespace-nowrap shadow-sm"
-        >
-          + Nova Despesa
-        </button>
+        <div className="flex items-center space-x-4">
+          <label className="flex items-center space-x-2 cursor-pointer">
+            <span className="text-sm text-gray-600 font-medium">Ver Todos</span>
+            <div className="relative">
+              <input
+                type="checkbox"
+                className="sr-only"
+                checked={isGlobalFilterActive}
+                onChange={toggleGlobalFilter}
+              />
+              <div className={`block w-10 h-6 rounded-full transition-colors ${isGlobalFilterActive ? 'bg-primary' : 'bg-gray-300'}`}></div>
+              <div className={`dot absolute left-1 top-1 bg-white w-4 h-4 rounded-full transition-transform ${isGlobalFilterActive ? 'transform translate-x-4' : ''}`}></div>
+            </div>
+          </label>
+          <button
+            onClick={handleCreate}
+            className="cursor-pointer h-8 px-4 flex items-center justify-center bg-red-600 hover:bg-red-700 text-white rounded-md text-xs font-semibold whitespace-nowrap shadow-sm"
+          >
+            + Nova Despesa
+          </button>
+        </div>
       </div>
       {isEmpty ? (
-        <div className="text-center py-12 bg-white rounded-lg shadow-sm border border-gray-200 mt-4">
-          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
-            <path vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
-          </svg>
-          <h3 className="mt-2 text-sm font-semibold text-gray-900">Nenhuma despesa fixa</h3>
-          <p className="mt-1 text-sm text-gray-500">Comece criando sua primeira despesa fixa.</p>
-          <div className="mt-6">
-            <button
-              onClick={handleCreate}
-              type="button"
-              className="cursor-pointer inline-flex items-center rounded-md bg-red-600 px-3 py-1 text-sm font-semibold text-white shadow-sm hover:bg-red-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-600"
-            >
-              Nova Despesa Fixa
-            </button>
-          </div>
-        </div>
+        <EmptyState
+          title="Nenhuma despesa fixa"
+          description={isGlobalFilterActive ? "Comece criando sua primeira despesa fixa." : "Nenhuma despesa fixa encontrada."}
+          action={{ label: 'Nova Despesa Fixa', onClick: handleCreate }}
+        />
       ) : (
         <div className="bg-white shadow sm:rounded-2xl overflow-visible mt-2 w-full">
           <table className="min-w-max w-full divide-y divide-gray-200 table-fixed">
@@ -87,7 +99,7 @@ const DespesasFixasPage: React.FC = () => {
               </tr>
             </thead>
             <tbody className="bg-white divide-y divide-gray-200">
-              {despesas.map((despesa) => (
+              {filteredDespesas.map((despesa) => (
                 <tr key={despesa.id} className="group even:bg-gray-200 odd:bg-white text-sm divide-x divide-gray-200">
                   <td className="px-3 py-1 font-medium text-gray-900 align-middle">
                     <div className="line-clamp-3 whitespace-normal break-words" title={despesa.descricao}>
diff --git a/frontend/src/pages/DespesasVariaveis/DespesasVariaveisPage.tsx b/frontend/src/pages/DespesasVariaveis/DespesasVariaveisPage.tsx
index 6e76348..20c8e8e 100644
--- a/frontend/src/pages/DespesasVariaveis/DespesasVariaveisPage.tsx
+++ b/frontend/src/pages/DespesasVariaveis/DespesasVariaveisPage.tsx
@@ -3,9 +3,12 @@ import React, { useState } from 'react';
 import { useDespesasVariaveis, type DespesaVariavel } from '../../hooks/useDespesasVariaveis';
 import DespesaVariavelFormModal from './components/DespesaVariavelFormModal';
 import DeleteConfirmModal from './components/DeleteConfirmModal';
+import { useMonthStore } from '../../store/monthStore';
+import EmptyState from '../../components/ui/EmptyState';
 
 const DespesasVariaveisPage: React.FC = () => {
   const { data: despesas, isLoading, isError } = useDespesasVariaveis();
+  const { selectedMonth, isGlobalFilterActive, toggleGlobalFilter } = useMonthStore();
   const [isFormModalOpen, setIsFormModalOpen] = useState(false);
   const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
   const [selectedDespesa, setSelectedDespesa] = useState<DespesaVariavel | null>(null);
@@ -25,10 +28,21 @@ const DespesasVariaveisPage: React.FC = () => {
     setIsDeleteModalOpen(true);
   };
 
+  const filteredDespesas = React.useMemo(() => {
+    if (!despesas) return [];
+    if (isGlobalFilterActive) return despesas;
+    return despesas.filter(d => {
+      // For variable expenses, check if selectedMonth falls between dataInicio and dataFim
+      const start = d.dataInicio.substring(0, 7); // YYYY-MM
+      const end = d.dataFim.substring(0, 7);
+      return selectedMonth >= start && selectedMonth <= end;
+    });
+  }, [despesas, selectedMonth, isGlobalFilterActive]);
+
   if (isLoading) return <div className="p-6">Carregando despesas variáveis...</div>;
   if (isError) return <div className="p-6 text-red-600">Erro ao carregar despesas variáveis.</div>;
 
-  const isEmpty = !despesas || despesas.length === 0;
+  const isEmpty = filteredDespesas.length === 0;
 
   return (
     <div className="px-4 pb-4 w-full">
@@ -36,30 +50,34 @@ const DespesasVariaveisPage: React.FC = () => {
         <div className="pl-2 border-l-4 border-red-600">
           <h1 className="text-2xl font-bold text-gray-800">Despesas Variáveis</h1>
         </div>
-        <button
-          onClick={handleCreate}
-          className="cursor-pointer h-8 px-4 flex items-center justify-center bg-red-600 hover:bg-red-700 text-white rounded-md text-xs font-semibold whitespace-nowrap shadow-sm"
-        >
-          + Nova Despesa
-        </button>
+        <div className="flex items-center space-x-4">
+          <label className="flex items-center space-x-2 cursor-pointer">
+            <span className="text-sm text-gray-600 font-medium">Ver Todos</span>
+            <div className="relative">
+              <input
+                type="checkbox"
+                className="sr-only"
+                checked={isGlobalFilterActive}
+                onChange={toggleGlobalFilter}
+              />
+              <div className={`block w-10 h-6 rounded-full transition-colors ${isGlobalFilterActive ? 'bg-primary' : 'bg-gray-300'}`}></div>
+              <div className={`dot absolute left-1 top-1 bg-white w-4 h-4 rounded-full transition-transform ${isGlobalFilterActive ? 'transform translate-x-4' : ''}`}></div>
+            </div>
+          </label>
+          <button
+            onClick={handleCreate}
+            className="cursor-pointer h-8 px-4 flex items-center justify-center bg-red-600 hover:bg-red-700 text-white rounded-md text-xs font-semibold whitespace-nowrap shadow-sm"
+          >
+            + Nova Despesa
+          </button>
+        </div>
       </div>
       {isEmpty ? (
-        <div className="text-center py-12 bg-white rounded-lg shadow-sm border border-gray-200 mt-4">
-          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
-            <path vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
-          </svg>
-          <h3 className="mt-2 text-sm font-semibold text-gray-900">Nenhuma despesa variável</h3>
-          <p className="mt-1 text-sm text-gray-500">Comece criando sua primeira despesa variável.</p>
-          <div className="mt-6">
-            <button
-              onClick={handleCreate}
-              type="button"
-              className="cursor-pointer inline-flex items-center rounded-md bg-red-600 px-3 py-1 text-sm font-semibold text-white shadow-sm hover:bg-red-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-600"
-            >
-              Nova Despesa Variável
-            </button>
-          </div>
-        </div>
+        <EmptyState
+          title="Nenhuma despesa variável"
+          description={isGlobalFilterActive ? "Comece criando sua primeira despesa variável." : "Nenhuma despesa para o mês selecionado."}
+          action={{ label: 'Nova Despesa Variável', onClick: handleCreate }}
+        />
       ) : (
         <div className="bg-white shadow sm:rounded-2xl overflow-visible mt-2 w-full">
           <table className="min-w-max w-full divide-y divide-gray-200 table-fixed">
@@ -98,7 +116,7 @@ const DespesasVariaveisPage: React.FC = () => {
               </tr>
             </thead>
             <tbody className="bg-white divide-y divide-gray-200">
-              {despesas.map((despesa) => (
+              {filteredDespesas.map((despesa) => (
                 <tr key={despesa.id} className="group even:bg-gray-200 odd:bg-white text-sm divide-x divide-gray-200">
                   <td className="px-3 py-1 font-medium text-gray-900 align-middle">
                     <div className="line-clamp-3 whitespace-normal break-words" title={despesa.descricao}>
diff --git a/frontend/src/pages/ReceitasFixas/ReceitasFixasPage.tsx b/frontend/src/pages/ReceitasFixas/ReceitasFixasPage.tsx
index 0967d91..136abfb 100644
--- a/frontend/src/pages/ReceitasFixas/ReceitasFixasPage.tsx
+++ b/frontend/src/pages/ReceitasFixas/ReceitasFixasPage.tsx
@@ -3,9 +3,12 @@ import React, { useState } from 'react';
 import { useReceitasFixas, type ReceitaFixa } from '../../hooks/useReceitasFixas';
 import ReceitaFixaFormModal from './components/ReceitaFixaFormModal';
 import DeleteConfirmModal from './components/DeleteConfirmModal';
+import { useMonthStore } from '../../store/monthStore';
+import EmptyState from '../../components/ui/EmptyState';
 
 const ReceitasFixasPage: React.FC = () => {
   const { data: receitas, isLoading, isError } = useReceitasFixas();
+  const { isGlobalFilterActive, toggleGlobalFilter } = useMonthStore();
   const [isFormModalOpen, setIsFormModalOpen] = useState(false);
   const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
   const [selectedReceita, setSelectedReceita] = useState<ReceitaFixa | null>(null);
@@ -25,10 +28,15 @@ const ReceitasFixasPage: React.FC = () => {
     setIsDeleteModalOpen(true);
   };
 
+  const filteredReceitas = React.useMemo(() => {
+    if (!receitas) return [];
+    return receitas;
+  }, [receitas]);
+
   if (isLoading) return <div className="p-6">Carregando receitas fixas...</div>;
   if (isError) return <div className="p-6 text-red-600">Erro ao carregar receitas fixas.</div>;
 
-  const isEmpty = !receitas || receitas.length === 0;
+  const isEmpty = filteredReceitas.length === 0;
 
   return (
     <div className="px-4 pb-4 w-full">
@@ -36,30 +44,34 @@ const ReceitasFixasPage: React.FC = () => {
         <div className="pl-2 border-l-4 border-primary">
           <h1 className="text-2xl font-bold text-gray-800">Receitas Fixas</h1>
         </div>
-        <button
-          onClick={handleCreate}
-          className="cursor-pointer h-8 px-4 flex items-center justify-center bg-primary hover:bg-primary-light text-white rounded-md text-xs font-semibold whitespace-nowrap shadow-sm"
-        >
-          + Nova Receita
-        </button>
+        <div className="flex items-center space-x-4">
+          <label className="flex items-center space-x-2 cursor-pointer">
+            <span className="text-sm text-gray-600 font-medium">Ver Todos</span>
+            <div className="relative">
+              <input
+                type="checkbox"
+                className="sr-only"
+                checked={isGlobalFilterActive}
+                onChange={toggleGlobalFilter}
+              />
+              <div className={`block w-10 h-6 rounded-full transition-colors ${isGlobalFilterActive ? 'bg-primary' : 'bg-gray-300'}`}></div>
+              <div className={`dot absolute left-1 top-1 bg-white w-4 h-4 rounded-full transition-transform ${isGlobalFilterActive ? 'transform translate-x-4' : ''}`}></div>
+            </div>
+          </label>
+          <button
+            onClick={handleCreate}
+            className="cursor-pointer h-8 px-4 flex items-center justify-center bg-primary hover:bg-primary-light text-white rounded-md text-xs font-semibold whitespace-nowrap shadow-sm"
+          >
+            + Nova Receita
+          </button>
+        </div>
       </div>
       {isEmpty ? (
-        <div className="text-center py-12 bg-white rounded-lg shadow-sm border border-gray-200 mt-4">
-          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
-            <path vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
-          </svg>
-          <h3 className="mt-2 text-sm font-semibold text-gray-900">Nenhuma receita fixa</h3>
-          <p className="mt-1 text-sm text-gray-500">Comece criando sua primeira receita fixa.</p>
-          <div className="mt-6">
-            <button
-              onClick={handleCreate}
-              type="button"
-              className="cursor-pointer inline-flex items-center rounded-md bg-primary px-3 py-1 text-sm font-semibold text-white shadow-sm hover:bg-primary-light focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
-            >
-              Nova Receita Fixa
-            </button>
-          </div>
-        </div>
+        <EmptyState
+          title="Nenhuma receita fixa"
+          description={isGlobalFilterActive ? "Comece criando sua primeira receita fixa." : "Nenhuma receita fixa encontrada."}
+          action={{ label: 'Nova Receita Fixa', onClick: handleCreate }}
+        />
       ) : (
         <div className="bg-white shadow sm:rounded-2xl overflow-visible mt-2 w-full">
           <table className="min-w-max w-full divide-y divide-gray-200 table-fixed">
@@ -87,7 +99,7 @@ const ReceitasFixasPage: React.FC = () => {
               </tr>
             </thead>
             <tbody className="bg-white divide-y divide-gray-200">
-              {receitas.map((receita) => (
+              {filteredReceitas.map((receita) => (
                 <tr key={receita.id} className="group even:bg-gray-200 odd:bg-white text-sm divide-x divide-gray-200">
                   <td className="px-3 py-1 font-medium text-gray-900 align-middle">
                     <div className="line-clamp-3 whitespace-normal break-words" title={receita.descricao}>
diff --git a/frontend/src/pages/ReceitasVariaveis/ReceitasVariaveisPage.tsx b/frontend/src/pages/ReceitasVariaveis/ReceitasVariaveisPage.tsx
index e0b6572..981d85b 100644
--- a/frontend/src/pages/ReceitasVariaveis/ReceitasVariaveisPage.tsx
+++ b/frontend/src/pages/ReceitasVariaveis/ReceitasVariaveisPage.tsx
@@ -3,9 +3,12 @@ import React, { useState } from 'react';
 import { useReceitasVariaveis, type ReceitaVariavel } from '../../hooks/useReceitasVariaveis';
 import ReceitaVariavelFormModal from './components/ReceitaVariavelFormModal';
 import DeleteConfirmModal from './components/DeleteConfirmModal';
+import { useMonthStore } from '../../store/monthStore';
+import EmptyState from '../../components/ui/EmptyState';
 
 const ReceitasVariaveisPage: React.FC = () => {
   const { data: receitas, isLoading, isError } = useReceitasVariaveis();
+  const { selectedMonth, isGlobalFilterActive, toggleGlobalFilter } = useMonthStore();
   const [isFormModalOpen, setIsFormModalOpen] = useState(false);
   const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
   const [selectedReceita, setSelectedReceita] = useState<ReceitaVariavel | null>(null);
@@ -25,10 +28,21 @@ const ReceitasVariaveisPage: React.FC = () => {
     setIsDeleteModalOpen(true);
   };
 
+  const filteredReceitas = React.useMemo(() => {
+    if (!receitas) return [];
+    if (isGlobalFilterActive) return receitas;
+    return receitas.filter(r => {
+      // For variable expenses, check if selectedMonth falls between dataInicio and dataFim
+      const start = r.dataInicio.substring(0, 7); // YYYY-MM
+      const end = r.dataFim.substring(0, 7);
+      return selectedMonth >= start && selectedMonth <= end;
+    });
+  }, [receitas, selectedMonth, isGlobalFilterActive]);
+
   if (isLoading) return <div className="p-6">Carregando receitas variáveis...</div>;
   if (isError) return <div className="p-6 text-red-600">Erro ao carregar receitas variáveis.</div>;
 
-  const isEmpty = !receitas || receitas.length === 0;
+  const isEmpty = filteredReceitas.length === 0;
 
   return (
     <div className="px-4 pb-4 w-full">
@@ -36,30 +50,34 @@ const ReceitasVariaveisPage: React.FC = () => {
         <div className="pl-2 border-l-4 border-primary">
           <h1 className="text-2xl font-bold text-gray-800">Receitas Variáveis</h1>
         </div>
-        <button
-          onClick={handleCreate}
-          className="cursor-pointer h-8 px-4 flex items-center justify-center bg-primary hover:bg-primary-light text-white rounded-md text-xs font-semibold whitespace-nowrap shadow-sm"
-        >
-          + Nova Receita
-        </button>
+        <div className="flex items-center space-x-4">
+          <label className="flex items-center space-x-2 cursor-pointer">
+            <span className="text-sm text-gray-600 font-medium">Ver Todos</span>
+            <div className="relative">
+              <input
+                type="checkbox"
+                className="sr-only"
+                checked={isGlobalFilterActive}
+                onChange={toggleGlobalFilter}
+              />
+              <div className={`block w-10 h-6 rounded-full transition-colors ${isGlobalFilterActive ? 'bg-primary' : 'bg-gray-300'}`}></div>
+              <div className={`dot absolute left-1 top-1 bg-white w-4 h-4 rounded-full transition-transform ${isGlobalFilterActive ? 'transform translate-x-4' : ''}`}></div>
+            </div>
+          </label>
+          <button
+            onClick={handleCreate}
+            className="cursor-pointer h-8 px-4 flex items-center justify-center bg-primary hover:bg-primary-light text-white rounded-md text-xs font-semibold whitespace-nowrap shadow-sm"
+          >
+            + Nova Receita
+          </button>
+        </div>
       </div>
       {isEmpty ? (
-        <div className="text-center py-12 bg-white rounded-lg shadow-sm border border-gray-200 mt-4">
-          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
-            <path vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
-          </svg>
-          <h3 className="mt-2 text-sm font-semibold text-gray-900">Nenhuma receita variável</h3>
-          <p className="mt-1 text-sm text-gray-500">Comece criando sua primeira receita variável.</p>
-          <div className="mt-6">
-            <button
-              onClick={handleCreate}
-              type="button"
-              className="cursor-pointer inline-flex items-center rounded-md bg-primary px-3 py-1 text-sm font-semibold text-white shadow-sm hover:bg-primary-light focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
-            >
-              Nova Receita Variável
-            </button>
-          </div>
-        </div>
+        <EmptyState
+          title="Nenhuma receita variável"
+          description={isGlobalFilterActive ? "Comece criando sua primeira receita variável." : "Nenhuma receita para o mês selecionado."}
+          action={{ label: 'Nova Receita Variável', onClick: handleCreate }}
+        />
       ) : (
         <div className="bg-white shadow sm:rounded-2xl overflow-visible mt-2 w-full">
           <table className="min-w-max w-full divide-y divide-gray-200 table-fixed">
@@ -92,7 +110,7 @@ const ReceitasVariaveisPage: React.FC = () => {
               </tr>
             </thead>
             <tbody className="bg-white divide-y divide-gray-200">
-              {receitas.map((receita) => (
+              {filteredReceitas.map((receita) => (
                 <tr key={receita.id} className="group even:bg-gray-200 odd:bg-white text-sm divide-x divide-gray-200">
                   <td className="px-3 py-1 font-medium text-gray-900 align-middle">
                     <div className="line-clamp-3 whitespace-normal break-words" title={receita.descricao}>
