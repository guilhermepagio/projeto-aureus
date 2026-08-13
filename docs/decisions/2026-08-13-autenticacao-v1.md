# Autenticação na V1

**Status:** Em revisão  
**Data:** 2026-08-13

## Contexto

O PRD define a V1 como single-owner, sem autenticação e sem múltiplos usuários.
O ERD e o modelo de classes históricos ainda incluem `Usuario`, e-mail e senha.

## Decisão atual

Manter a divergência explicitamente visível e não tratar os diagramas como
contratos finais até a arquitetura ratificar o modelo.

## Impacto

O modelo de dados e o domínio permanecem **Em revisão**. A implementação deve
seguir o PRD até que uma decisão arquitetural altere esse escopo.

## Referências

- [PRD](../product/prd.md)
- [Modelo de dados](../technical/data-model/README.md)
- [Modelo de domínio](../technical/domain-model/README.md)
