---
sources: ["../../../docs/product/prd.md", "../../../docs/product/brief.md"]
---

# Aureus Experience Spine

## Visual References
- [Wireframe: Consolidação Desktop](.working/consolidacao-desktop.html)
- [Wireframe: Consolidação Mobile](.working/consolidacao-mobile.html)
- [Wireframe: Registro de Despesas Mobile](.working/registro-despesas-mobile.html)

## Foundation
Aureus is a web-based responsive application designed for both mobile and desktop. The core paradigm is "Registration on Mobile, Consolidation on Desktop", although both form factors support all actions. The application prioritizes speed and cognitive ease, adopting a "State Synchronization" model where the context (e.g., selected month) is shared across tabs to minimize repetitive user inputs.

## Information Architecture
The architecture is flat, divided into 5 primary tabs:
1. Consolidação (Home/Dashboard)
2. Despesas Variáveis
3. Despesas Fixas
4. Receitas Fixas
5. Receitas Variáveis

Secondary features (Accounts, Categories, Settings, Logout) are accessed via a global Hamburger Menu/Avatar in the header. Login and Authentication flows precede this architecture.

## Voice and Tone
- **Direct & Professional:** Language is precise. Uses standard financial terminology without jargon (e.g., "Sobra do Mês" instead of complex accounting terms).
- **Assuring:** Success and error states are clear but not alarming.
- **Language:** Portuguese BR exclusively (no i18n).

## Component Patterns

### Main Navigation
- **Desktop:** A centralized floating pill bar in the header. The active state highlights the current tab.
- **Mobile:** A fixed bottom navigation bar containing 5 icons with short labels.

### Data Grids (Consolidação)
- **Desktop:** A large, horizontally scrolling table showing 24 months. Grouped vertically into colored thematic blocks (Receitas, Despesas, Categorias, Resumo). The first column (labels) is sticky.
- **Mobile:** The table breaks down into vertically stacked block cards. Only ONE month is visible at a time. Users navigate between months using horizontal swipe gestures, header arrows, or by explicitly clicking the month label to jump to a specific month.

### Forms & Registration
- Forms are **not** inline within the main tab screens.
- Registration is triggered by a Floating Action Button (FAB) or prominent "Adicionar" button.
- The form opens in a **Modal** (Desktop) or a **Bottom Sheet** (Mobile).
- **Calculation Preview:** Registration forms for recurring/installment items contain a real-time calculation block that updates instantly ("Impacto Total", "Última Parcela") before submission.

### Lists (Registration Tabs)
- The main view for all transaction tabs (Despesas, Receitas) is a List.
- **State Synchronization:** By default, the list ONLY shows items relevant to the month currently selected in the Consolidação tab.
- **Global Filter Toggle:** A prominent filter button allows the user to turn off the monthly sync and view the entire historical list.
- Each list item provides direct "Editar" and "Excluir" actions.

## State Patterns
- **Cold-load (Loading):** Initial data loads display skeleton loaders matching the structural shape of the grid or lists. No blocking full-screen spinners.
- **Empty States:** 
  - **Grid:** If it's the user's first access, the Consolidação grid displays a Welcome State block instructing them to add Accounts and Categories.
  - **Lists:** When a list is empty, an empty state block appears, featuring a muted icon and a direct call-to-action encouraging the user to use the FAB.
- **Error States:** Field-level validation is inline. Erroneous fields receive a red border and a specific error message below the input. **Missing dependencies (like Accounts or Categories) explicitly block form submission until resolved.**
- **Success States:** Non-blocking floating Toasts at the top of the screen (e.g., "MacBook Pro registrado com sucesso!").
- **Destructive Actions:** Deleting a record triggers a confirmation Modal to prevent accidental data loss.

## Interaction Primitives
- **Swipe:** Used on mobile to navigate between months in the Consolidação view.
- **Real-time Input:** Numeric calculations (like installments * value) happen instantly as the user types, without requiring a "Calculate" button.

## Accessibility Floor
- **Focus:** Standard browser focus rings are overridden with brand-compliant teal rings. 
- **Focus Trapping:** When a Modal or Bottom Sheet opens, keyboard focus MUST be trapped inside the overlay until dismissed.
- **Contrast:** Status colors (red/green) are used in conjunction with text weight or background tints to ensure readability. Red is never the sole indicator of an error.
- **Screen Readers (ARIA):** The complex 24-month grid MUST implement `role="grid"`, `role="rowgroup"`, `role="row"`, and `role="gridcell"` attributes. Header cells must use `aria-label` to explicitly announce context to screen readers (e.g., "Ago 2026, Receitas por Conta, Aureus Conta: R$ 3.500").

## Key Flows

### Flow 0: Autenticação (Login)
- **Protagonist:** Unauthenticated User.
- **Step 1:** User accesses the Aureus URL.
- **Step 2:** User lands on the Login screen presenting "Entrar com Google" or standard email/password fields.
- **Step 3:** User selects Google OAuth and authenticates.
- **Climax:** User is redirected to the Consolidação tab (Home).
- **Failure Path:** Authentication rejected. Error toast displays "Falha ao autenticar. Tente novamente."

### Flow 1: Visualizar Consolidação
- **Protagonist:** Returning User analyzing their month.
- **Step 1:** Lands on "Consolidação". The system calculates and renders the current month's grid.
- **Step 2:** User sees "Total Gasto", "Sobra do Mês", and "Sobra Retroativa Acumulada".
- **Step 3 (Mobile):** User swipes left to see the previous month.
- **Climax:** User understands their financial health at a glance.
- **Failure Path:** Network error loading the grid data. Grid shows a retry state: "Erro ao carregar dados. [Tentar Novamente]".

### Flow 2: Registrar Despesa Variável Parcelada
- **Protagonist:** Active User buying a new laptop on a credit card.
- **Step 1:** User navigates to "Despesas Variáveis".
- **Step 2:** Clicks the FAB (+). The form modal slides up.
- **Step 3:** Fills in "MacBook Pro", selects "Conta", "Categoria", inputs "Valor da Parcela" and "Parcelas (12)".
- **Step 4:** The Calculation Preview instantly shows the Total Impact and the end date of the installments.
- **Step 5:** User clicks "Registrar Despesa".
- **Climax:** Modal closes, a success Toast appears, and the new item is visible in the list.
- **Failure Path:** User forgets to select a category. The modal stays open, the category dropdown gets a red border, and "Campo obrigatório" appears below it.

### Flow 3: Alternar Contexto de Data
- **Protagonist:** Active User wanting to verify past expenses.
- **Step 1:** User is on "Despesas Variáveis", viewing August 2026 (synced from Consolidação).
- **Step 2:** User clicks the "Ver Todos / Filtro" toggle in the list header.
- **Step 3:** The list expands to show all historical expenses, grouped by month or date.
- **Climax:** User finds an expense from January without losing their global August context.
- **Failure Path:** Search/filter request times out. Toast displays "Erro ao carregar histórico".
