# Autenticação na V1

**Status:** Decidida **Data:** 2026-08-13

## Contexto

O PRD anterior definia a V1 como single-owner, sem autenticação. A geração do backend Java deve começar já com autenticação real, e o sistema precisa proteger os dados financeiros de cada conta.

## Decisão atual

Incluir autenticação na V1 usando OAuth 2.0 com OpenID Connect e Google como único provedor inicial. O fluxo de login será Authorization Code para aplicação web no servidor. Cada Usuário local será vinculado ao identificador estável do Google (`issuer` + `sub`), e todas as entidades financeiras serão isoladas por esse Usuário.

## Impacto

O modelo de dados e o domínio devem abandonar senha local como requisito e representar a identidade externa, sessão e proprietário dos dados. A arquitetura Java ainda deve definir o mecanismo de sessão, expirações, migração de dados legados e política de exclusão/desvinculação.

## Referências

- [PRD](../product/prd.md)
- [Modelo de dados](../technical/data-model/README.md)
- [Modelo de domínio](../technical/domain-model/README.md)
- [Google OpenID Connect](https://developers.google.com/identity/openid-connect/openid-connect)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/)
