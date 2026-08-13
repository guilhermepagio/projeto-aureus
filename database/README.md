# Database Docs (legado)

> A versão canônica para leitura humana está em
> [docs/technical/data-model](../docs/technical/data-model/README.md). Esta
> pasta é preservada para rastreabilidade durante a migração.

## Links do Modelo
[![Ver Código](https://img.shields.io/badge/Source-Código_DBML-blueviolet?style=for-the-badge)](./aureus-database-erd.txt)
[![Ferramenta DBML](https://img.shields.io/badge/TOOL_DBML-DBDIAGRAM.IO-blue?style=for-the-badge)](https://dbdiagram.io)

[![Ver Diagrama](https://img.shields.io/badge/Visualizar-Diagrama_PNG-orange?style=for-the-badge)](./aureus-database-erd.png)

A modelagam foi criada com linguagem DBML usando a plataforma [**DB Diagram**](https://dbdiagram.io) para a construção de um ERD (Entity Relationship Diagram).

Para visualizar o ERD do projeto **Aureus** temos duas opções:
- Clicar na badge laranja que leva a um PNG com o ERD
- Clicar na badge roxa, copiar o código DBML e posteriormente clicar na badge roxa, acessar o [**DB Diagram**](https://dbdiagram.io) e colar o código DBML para visualizar a interagir com o ERD.

## Estrutura Lógica
O sistema utiliza um modelo simplificado focado em **Fluxo de Caixa**. O objetivo é registrar movimentações e projetar o saldo futuro, sem controle complexo de status de pagamento, ou seja, controlar quais movimentações foram pagas ou estão pendentes.

- **Single-Owner:** todo dado do sistema pertence exclusivamente a um único `Usuario`, do nascimento ao descarte.
- **Origem/Destino:** A tabela `Contas` define onde o dinheiro entra e sai.
- **Classificação:** A tabela `Categorias` agrupa os gastos/ganhos para definir em conjunto com a tabela `Contas` onde o dinheiro entra e sai.
- **Movimentação:** `Despesas` e `Receitas` registram o valor das movimentações.

## Principais Entidades
- **Usuarios:** Centraliza o acesso e autenticação (email/senha).
- **Contas:** Representa as fontes de origem/destino do dinheiro (ex: Conta Corrente, Cartão Banco X, Cartão Banco Y).
- **Categorias:** Classificação das movimentações para relatórios.
- **Despesas / Receitas:** Tabelas que armazenam as transações financeiras.
    - *Suporte a Parcelamento:* Colunas dedicadas para controlar valor da parcela, quantidade e datas de início/fim.
    - *Tipagem:* Diferenciação entre movimentações Fixas e Variáveis.

## Decisões de Design
- Uso de `numeric` para valores monetários (evita erros de arredondamento de float).
- Uso de `timestamptz` para gestão correta de fusos horários nos campos `criado_em` e `atualizado_em`.
