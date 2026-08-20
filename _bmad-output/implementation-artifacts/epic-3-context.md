# Epic 3 Context: Lançamentos Financeiros (Despesas e Receitas)

<!-- Compiled from planning artifacts. Edit freely. Regenerate with compile-epic-context if planning docs change. -->

## Goal

Permitir que o usuário registre, visualize, edite e exclua todas as suas movimentações financeiras — despesas e receitas, fixas ou variáveis — com cálculo em tempo real de parcelamentos, distribuição precisa de centavos residuais e sincronização contextual com o filtro de mês global.

## Stories

- Story 3.1: Lançamentos Fixos (Despesas e Receitas) com Vigência
- Story 3.2: Lançamentos Variáveis e Parcelados com Arredondamento de Centavos
- Story 3.3: Lançamentos Variáveis à Vista (Parcela Única)
- Story 3.4: Sincronização de Visão Mensal, Invalidação de Cache e Filtro Global

## Requirements & Constraints

- **Tipos de Movimentação**:
  - **Fixas (Despesas e Receitas)**: Valores recorrentes mensais contínuos (ex: aluguel, internet, salário). Campos: Descrição, Valor, Conta, Categoria e Data de Início/Vigência. Não possuem parcelamento e são projetadas continuamente para a frente. Alterações de valor atualizam o registro in-place, sem versionamento de histórico passado.
  - **Variáveis (Despesas e Receitas)**: Movimentações pontuais ou parceladas. Campos obrigatórios: Descrição, Valor Parcela, Nº Parcelas (≥ 1), Primeira Parcela (competência inicial), Categoria, Conta. Despesas Variáveis possuem campos adicionais para conferência: Local Compra e Data Compra (opcionais). Campos calculados automaticamente: Valor Total e Última Parcela.
- **Distribuição de Centavos (Penny Rounding)**: Ao dividir o Valor Total pelo número de parcelas, divisões inexatas com centavos fracionados devem alocar a diferença residual na primeira parcela, garantindo que a soma exata das parcelas corresponda rigorosamente ao Valor Total.
- **Parcela Única (À Vista)**: Quando o número de parcelas for igual a 1, o Valor Total iguala-se ao Valor da Parcela e a Última Parcela coincide com a Primeira Parcela, incidindo unicamente no mês de competência indicado.
- **Horizonte de Parcelas**: Parcelamentos com duração superior a 24 meses devem ser persistidos integralmente no banco de dados para projeções futuras.
- **Dependência de Cadastro**: O cadastro de qualquer movimentação exige a seleção obrigatória de uma Conta e uma Categoria existentes. Se não houver Contas ou Categorias cadastradas, o formulário deve bloquear a submissão e orientar a criação das dependências.
- **Sincronização de Visão Mensal**: Por padrão, as listagens de Despesas e Receitas filtram e exibem apenas os lançamentos vigentes no mês ativo selecionado no estado global.
- **Filtro Global ("Ver Todos")**: O usuário pode alternar um controle de filtro no cabeçalho da listagem para desativar a restrição mensal e visualizar todas as transações cadastradas.

## Technical Decisions

- **Isolamento de Dados (Multi-Tenancy Lógico)**: Todas as tabelas de movimentações (`despesas`, `receitas`) possuem a coluna `usuario_id`. Todas as operações no backend devem validar e filtrar estritamente pelo ID do usuário autenticado na sessão (JWT em HttpOnly Cookie).
- **Integridade Referencial e Bloqueio de Deleção**: `despesas` e `receitas` possuem chaves estrangeiras para `contas` e `categorias` configuradas com `ON DELETE RESTRICT` para impedir exclusão inadvertida de dependências vinculadas.
- **Precisão Numérica**: Valores monetários devem ser manipulados como `numeric` / `BigDecimal` no backend e tratados com precisão decimal no frontend, evitando imprecisões de ponto flutuante.
- **Cálculo de Parcelamento**:
  - `Valor Total = Valor Parcela × Nº Parcelas` (ou distribuição reversa caso o total seja a entrada).
  - `Data Última Parcela = Primeira Parcela + (Nº Parcelas - 1) meses`.
  - Tratamento de centavos: `Parcela Base = floor(Total / N)`, `Primeira Parcela = Parcela Base + (Total - Parcela Base * N)`.
- **Competências Temporais**: Meses de vigência e parcelas utilizam a representação de competência mensal (`YearMonth` ou formato de data no primeiro dia do mês `YYYY-MM-01`).
- **Gerenciamento de Estado e Cache**:
  - Estado do mês ativo compartilhado entre abas gerenciado via **Zustand**.
  - Chamadas de API e cache gerenciados via **React Query (TanStack Query)**, com invalidação automática de cache nas mutações de criação, edição e exclusão de movimentações e consolidação.

## UX & Interaction Patterns

- **Formulários em Modal / Bottom Sheet**: A criação e edição de movimentações abrem em Modal centralizado (Desktop) ou Bottom Sheet ancorado na base (Mobile), implementando Focus Trapping para acessibilidade.
- **Calculation Preview em Tempo Real**: Os formulários de movimentações variáveis incluem um bloco inline com fundo suave (`teal-light`) que calcula e exibe instantaneamente o Valor Total, o Valor da Parcela e o mês da Última Parcela conforme o usuário digita.
- **Ações Rápidas de Criação**: Botão de Ação Flutuante (FAB) ou botão primário "Adicionar" nas abas de movimentação.
- **Alinhamento e Formatação**: Todos os valores numéricos e monetários nas listagens devem ser alinhados à direita e utilizar fontes tabulares (`tabular-nums`).
- **Feedback e Confirmação**: Exclusões exigem confirmação explícita via modal. Sucessos e falhas disparam Toasts informativos no topo da tela com borda semântica.
- **Empty States**: Listagens sem lançamentos no mês ativo (ou histórico vazio) exibem ilustrações esmaecidas, mensagem contextual e CTA para registro.

## Cross-Story Dependencies

- **Dependência do Epic 1**: Necessita da infraestrutura de autenticação via Google, extração de sessão segura (JWT HttpOnly) e casca de navegação (Shell/Pill/Bottom Nav).
- **Dependência do Epic 2**: Requer o CRUD de Contas e Categorias funcional para fornecer as opções obrigatórias de seleção nos formulários.
- **Habilitador para o Epic 4**: As movimentações e parcelas registradas neste épico constituem a fonte primária de dados para a matriz de projeção de 24 meses do Painel de Consolidação.
