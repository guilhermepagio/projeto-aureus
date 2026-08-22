# Epic 3 Context: Lançamentos Financeiros (Despesas e Receitas)

<!-- Compiled from planning artifacts. Edit freely. Regenerate with compile-epic-context if planning docs change. -->

## Goal

This epic establishes the core transaction engine of the Aureus application by implementing the complete lifecycle (Create, Read, Update, Delete) for both fixed and variable financial records (expenses and incomes). It handles complex logic for installments, including real-time calculation previews, precise cent distribution, and projection of recurring values. Furthermore, it ensures the user interface remains synchronized across tabs through global state management and automatic cache invalidation, providing a seamless data entry experience.

## Stories

- Story 3.1: Lançamentos Fixos (Despesas e Receitas) com Vigência
- Story 3.2: Lançamentos Variáveis e Parcelados com Arredondamento de Centavos
- Story 3.3: Lançamentos Variáveis à Vista (Parcela Única)
- Story 3.4: Sincronização de Visão Mensal, Invalidação de Cache e Filtro Global

## Requirements & Constraints

- **Variable Transactions:** Must support installment logic. Required fields: Description, Installment Value, Number of Installments (>= 1), First Installment (month/year), Category, and Account. Optional fields: Purchase Location, Purchase Date, Notes.
- **Auto-Calculated Fields:** Total Value (`Installment Value × Number of Installments`) and Last Installment (`First Installment + (Number of Installments - 1) months`) must be calculated and displayed in real-time. These fields are not editable by the user.
- **Single Installment (At-Sight):** Must support single installments (Number of Installments = 1), where Total Value equals Installment Value, and Last Installment equals First Installment. This applies the transaction to a single month.
- **Fixed Transactions:** Must support recurring values without installments. Required fields: Description, Value, Category, Account, and Start Date. The value must be projected forward across all subsequent months. Modifying the value updates the projection from the new effective date.
- **Data Synchronization:** Transaction lists must only display items relevant to the currently active month in the global state, unless the "Global Filter / View All" toggle is activated.
- **Cache Invalidation:** Any mutation (create, edit, delete) must automatically invalidate the queries for the lists and the Consolidation panel to reflect changes in real-time without page reloads.
- **Referential Integrity:** Transactions cannot be created if no Accounts or Categories exist. Selectors will display empty states linking to the respective creation forms if dependencies are missing.

## Technical Decisions

- **Data Isolation:** All transaction entities must include a `usuario_id` column. All repository queries must enforce filtering by the authenticated user's ID to ensure logical multi-tenancy.
- **State Management:** Use Zustand exclusively for cross-tab shared context (e.g., the globally selected "Month" used to filter transaction lists). Local UI state remains in React `useState`.
- **Data Fetching & Caching:** Use React Query (TanStack Query) with Axios/Fetch for all API calls. Mutations must trigger automatic cache invalidation to keep lists synchronized.
- **API Paradigm:** Strict REST API (JSON over HTTP) communication between the React frontend and the Spring Boot backend.
- **Database Schema:** Schema changes for new transaction tables must be managed via explicit Flyway migration scripts.

## UX & Interaction Patterns

- **Forms (Modals/Bottom Sheets):** Registration forms must not be inline. They should open in a centered Modal on desktop (`radius-md`) and a docked Bottom Sheet on mobile (with heavy top shadow). Keyboard focus must be trapped inside the overlay.
- **Calculation Preview:** Installment forms must include an inline calculation block (using `teal-light` background and `color.brand.primary` text) that instantly updates and displays the Total Impact and Last Installment date as the user types.
- **Lists and Empty States:** The main view for transaction tabs is a list. Empty lists must display a standard Empty State block (centered, dashed border, muted icon, and CTA).
- **Destructive Actions:** Deleting a transaction must trigger an explicit confirmation Modal to prevent accidental data loss.
- **Success/Error Feedback:** Field-level validation should show a red border and message inline. Success states should use non-blocking floating Toasts at the top of the screen.

## Cross-Story Dependencies

- **Epic 1 (Auth & Shell):** Requires the authenticated user context (`usuario_id`) and the application shell for navigation.
- **Epic 2 (Accounts & Categories):** Requires the existence of Accounts and Categories, as transactions require foreign keys to both.
- **Epic 4 (Consolidation):** The transaction data generated in this epic is consumed directly by the 24-month Consolidation panel.
