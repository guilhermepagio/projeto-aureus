# Addendum — Aureus PRD

Este documento preserva contexto técnico, decisões adiadas e profundidade que complementa o PRD sem sobrecarregá-lo.

## 1. Subcategorias (Visão Futura)

- **Status:** Deferido para versão futura (pós-V1).
- **Contexto:** Na V1, as Categorias são macro (Essencial, Não Essencial, Investimentos, Lazer). O usuário expressou intenção de adicionar subcategorias no futuro — por exemplo, dentro de "Não Essencial" existiriam subcategorias como "Eletrônicos", "Compras Online", etc. Da mesma forma, categorias como "Alimentação" e "Transporte" seriam subcategorias dentro de "Essencial".
- **Impacto no ERD:** A tabela `categorias` atual não possui relação hierárquica (parent_id). A implementação futura exigirá uma coluna de auto-referência ou tabela auxiliar.
- **Decisão V1:** Categorias são flat (sem hierarquia). O modelo atual atende ao escopo.

## 2. Autenticação Google e OAuth2/OIDC

- **Status:** Confirmado para o MVP/V1.
- **Objetivo:** permitir que o Usuário entre sem cadastrar senha no Aureus e preparar o backend Java para autenticação real desde o primeiro ciclo.
- **Protocolo:** OAuth 2.0 com OpenID Connect, usando Authorization Code para aplicação web no servidor. O Google é o único Provedor de Identidade da V1.
- **Escopos:** solicitar somente `openid`, `profile` e `email`; a V1 não acessa APIs do Google em nome do Usuário.
- **Identidade:** o vínculo local deve usar o identificador estável do provedor (`issuer` + `sub`), nunca o e-mail isoladamente. Nome, e-mail e imagem são atributos sincronizáveis, não a chave do vínculo.
- **Conta local:** primeiro login cria o Usuário local; logins seguintes reconhecem a mesma Identidade Externa sem duplicar conta. A tabela/entidade de Usuário deve suportar provedor, subject externo, e-mail atual, nome/imagem e timestamps.
- **Sessão:** o login cria uma sessão local protegida. Expiração absoluta, expiração por inatividade, rotação/renovação, armazenamento (cookie de sessão ou token) e invalidação devem ser definidos na arquitetura Java antes da implementação.
- **Retorno OAuth:** `redirect_uri`, `state`, `nonce`, PKCE quando aplicável, validação de issuer/audience/assinatura/expiração do ID Token e tratamento de erro devem ser cobertos pela solução de segurança escolhida. Segredos e client credentials ficam fora do repositório e são fornecidos por configuração segura.
- **Isolamento:** todas as entidades financeiras devem possuir vínculo obrigatório com Usuário, e toda consulta/mutação deve derivar o Usuário do contexto autenticado; nunca aceitar `userId` vindo do cliente como autoridade.
- **Privacidade e segurança:** não armazenar senha Google nem tokens desnecessários. Falhas, contas inexistentes e IDs de outro Usuário não devem permitir enumeração de dados. Logout invalida a sessão local; revogação global da conta Google é escopo futuro.
- **Ambientes:** credenciais, redirect URIs e origens autorizadas devem ser configuráveis por ambiente (local, teste e produção), sem valores secretos versionados.

### 2.1 Referências oficiais consultadas

- [Google OpenID Connect](https://developers.google.com/identity/openid-connect/openid-connect)
- [Google OAuth 2.0 para aplicações web no servidor](https://developers.google.com/identity/protocols/oauth2/web-server)
- [Spring Security — OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/)

### 2.2 Decisões que permanecem para Arquitetura

- Escolher o mecanismo de sessão adequado ao contrato frontend/backend e ao ambiente de deploy.
- Definir migração do modelo atual de `Usuario` para a identidade externa e estratégia para dados legados sem proprietário.
- Definir política de exclusão de conta, desvinculação do Google e retenção de dados financeiros.
- Definir matriz de testes de segurança: callback inválido, replay, CSRF/state, nonce, token expirado, conta duplicada, isolamento entre usuários e logout.

## 3. Saneamento Arquitetural e Testes Automatizados (V1)

- **Status:** Confirmado para a V1 como etapas sequenciais indispensáveis antes do lançamento.
- **Contexto:** Para garantir que os testes automatizados sejam construídos sobre uma fundação estável, limpa e desacoplada, a V1 divide os esforços em dois épicos dedicados:
  1. **Épico 5 (Saneamento Arquitetural e Débitos Técnicos):** Resolução dos action items acumulados das retros (Service Layer, DTOs Records, Handler Global de Exceções, validação de propriedade multi-tenant em relacionamentos, índices de banco, cliente HTTP centralizado no frontend e abstração comum de formulários DRY).
  2. **Épico 6 (Testes Automatizados e Qualidade Contínua):** Suíte de testes em três camadas cobrindo a arquitetura definitiva e saneada.

### 3.1 Estratégia de Camadas de Testes (Épico 6)

| Camada | Framework / Ferramenta | Escopo |
|---|---|---|
| Integração Backend | JUnit 5 + Spring Boot Test + Testcontainers (PostgreSQL) + Maven Failsafe | Endpoints REST, segurança JWT, multitenancy, integridade referencial |
| Unitário Backend | JUnit 5 + Mockito + Maven Surefire | Lógica de domínio, Services desacoplados, cálculos de parcelas e consolidação financeira |
| Componentes Frontend | Vitest + React Testing Library + jsdom | Formulários abstraídos, modais com focus trap, MonthPicker, DatePicker, filtro global |

### 3.2 Decisões Técnicas Consolidadas para a V1

- **Sequenciamento Obrigatório:** O Épico 5 (Saneamento) precede o Épico 6 (Testes), garantindo que a suíte seja escrita contra contratos finais, DTOs Records e Services, sem risco de quebras por refatoração tardia.
- **Testes E2E com Playwright:** Deferidos para a V2 (após a implementação do Modo Escuro e da experiência Mobile) para evitar atrito prematuro de orquestração, bypass de autenticação Google e retrabalho de seletores visuais.
- **Threshold de Cobertura:** Abordagem qualitativa estrita com foco em regras de negócio críticas, cálculos financeiros, integridade referencial, isolamento multi-tenant e usabilidade de componentes visuais (sem imposição de métricas cegas de porcentagem no JaCoCo).
- **Separação de Ciclo no Maven:** Configuração canônica no `pom.xml` separando testes unitários rápidos via `maven-surefire-plugin` (`*Test.java` no `mvn test` sem necessidade de Docker) e testes de integração com Testcontainers via `maven-failsafe-plugin` (`*IT.java` no `mvn verify`).
- **Refatoração Arquitetural de Controllers:** Extermínio mandatório de regras de negócio e de entidades JPA nos controllers da aplicação no Épico 5, estabelecendo Service Layer dedicada e DTOs imutáveis via Java Records.

## 4. Modo Escuro (Visão V2)

- **Status:** Deferido para V2 (junto com Mobile).
- **Contexto:** A V1 foca na validação da experiência com o tema claro e consolidação de uma suíte de testes robusta. O Modo Escuro será implementado na V2 junto com o refinamento da experiência mobile.
- **Diretrizes Técnicas Preservadas:**
  - Tailwind v4 dark mode com classe `dark` no `<html>` e `@theme` tokens.
  - Persistência em `localStorage` (key `aureus-theme`, valores `light` | `dark` | `system`) lida no `<head>` anti-FOUC.
  - Observação de `prefers-color-scheme` quando em modo `system`.
  - Transição suave via CSS (`200ms ease`) desabilitada no carregamento inicial.
- **Decisões Pendentes para UX Design (V2):**
  - Paleta exata de cores escuras (valores hexadecimais para `main-dark`, `surface-dark`, `text-main-dark`, `text-muted-dark`, `border-dark`, `shadow-dark`).
  - Ajuste de luminosidade/saturação das cores primária Deep Teal e secundária Warm Amber para manter contraste WCAG AA no fundo escuro.
  - Variação dos blocos de cores semânticas da Consolidação (verde receita, vermelho despesa, azul consolidação) no tema escuro.
  - Design do toggle de tema no header (ícone sol/lua, dropdown ou segmented control).

## 5. Experiência Mobile (Visão V2)

- **Status:** Deferido para V2.
- **Contexto:** A V1 foca exclusivamente na experiência desktop. O usuário definiu que a experiência mobile será o próximo grande épico após o lançamento da V1.
- **Escopo futuro previsto:**
  - Bottom Navigation Bar fixa com 5 ícones (substituindo a Pill Nav em viewports < 768px).
  - Swipe horizontal para navegação temporal na grade de 24 meses da Consolidação.
  - Bottom Sheet modals (slide-up) substituindo modais centralizados em viewports pequenos.
  - Tabelas com scroll horizontal com snap para visualização da grade de Consolidação.
  - Breakpoint definido via `--breakpoint-md: 769px` já presente nos design tokens.
- **Impacto no ERD/Backend:** Nenhum — a experiência mobile é puramente frontend/UX.
