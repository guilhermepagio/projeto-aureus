# Epic 4 Context: Consolidação e Projeção Mensal (Painel de 24 Meses)

<!-- Compiled from planning artifacts. Edit freely. Regenerate with compile-epic-context if planning docs change. -->

## Goal

Dar ao usuário o poder de visualizar o impacto de suas decisões financeiras 24 meses no futuro, através de uma matriz consolidada com totais, categorias, sobras acumuladas e navegação responsiva (Painel de Consolidação).

## Stories

- Story 4.1: Seleção de Mês, Navegação Temporal e Grid com Suporte a Gestos Mobile
- Story 4.2: Bloco de Consolidação por Conta (Receitas e Despesas)
- Story 4.3: Bloco Analítico de Categorias (Valores e Percentuais com Proteção Zero)
- Story 4.4: Bloco de Resumo Geral com Sobra Histórica Acumulada

## Requirements & Constraints

- A matriz exibe horizontalmente 24 colunas de meses a partir de um mês escolhido pelo usuário (manualmente ou via atalho "Mês Atual").
- Movimentações Fixas se repetem em todos os meses; Movimentações Variáveis aparecem apenas nos meses de suas parcelas (Movimento de parcela única aparece em apenas um mês).
- A primeira coluna lateral com os nomes de Contas e Categorias permanece fixa (`sticky`) durante a rolagem horizontal.
- A navegação inclui suporte a scroll e gestos mobile (Swipe horizontal) para avançar/recuar o foco da visualização.
- Isolamento de dados: O painel deve usar apenas as movimentações do usuário autenticado.

## Technical Decisions

- O sistema usa PostgreSQL via Docker.
- Backend desenvolvido em Java (possivelmente Spring Boot), frontend com componentes e hooks comuns a serem reaproveitados.

## UX & Interaction Patterns

- Uma grade de 24 meses exibida no estilo planilha com scroll horizontal.
- A primeira coluna é fixa.
- No mobile permite Swipe para navegar temporalmente.
- O Painel é a aba "Consolidação", que deve estar listada na barra de navegação principal.
