# Addendum — Aureus PRD

Este documento preserva contexto técnico, decisões adiadas e profundidade que complementa o PRD sem sobrecarregá-lo.

## 1. Subcategorias (Visão Futura)

- **Status:** Deferido para versão futura (pós-V1).
- **Contexto:** Na V1, as Categorias são macro (Essencial, Não Essencial, Investimentos, Lazer). O usuário expressou intenção de adicionar subcategorias no futuro — por exemplo, dentro de "Não Essencial" existiriam subcategorias como "Eletrônicos", "Compras Online", etc. Da mesma forma, categorias como "Alimentação" e "Transporte" seriam subcategorias dentro de "Essencial".
- **Impacto no ERD:** A tabela `categorias` atual não possui relação hierárquica (parent_id). A implementação futura exigirá uma coluna de auto-referência ou tabela auxiliar.
- **Decisão V1:** Categorias são flat (sem hierarquia). O modelo atual atende ao escopo.
