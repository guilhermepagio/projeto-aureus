# Modelo de dados

**Estado:** Em revisão.

O ERD descreve entidades de contas, categorias, despesas e receitas, incluindo
parcelamento. A fonte editável está em [aureus-erd.dbml](aureus-erd.dbml); a
visualização está em [aureus-erd.png](aureus-erd.png).

O modelo ainda contém `usuarios`, e-mail e senha, enquanto o PRD define a V1
como single-owner sem autenticação. Essa divergência será resolvida em uma
decisão arquitetural antes da implementação.

A imagem representa relações entre movimentações, contas e categorias; consulte
o DBML para a definição completa e atualizável.
