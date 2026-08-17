# 📚 Documentação do Aureus

Esta é a biblioteca curada para leitura humana. O fluxo recomendado é entender primeiro o produto e depois a solução técnica. 

## 🎯 Produto

Entenda por que o Aureus existe e o que a V1 precisa resolver.

- [Brief](product/brief.md) — visão, problema, público e direção.
- [PRD](product/prd.md) — requisitos, jornadas, escopo e critérios da V1.
- [Addendum do Brief](product/brief-addendum.md) — possibilidades futuras.
- [Addendum do PRD](product/prd-addendum.md) — decisões complementares.
- [Roadmap](product/roadmap.md) — sequência de planejamento e implementação.

## ⚙️ Arquitetura e Técnica

Esta seção explica como o produto será estruturado depois que o PRD definiu o porquê e o quê.

- **[Modelo de dados](technical/data-model/aureus-erd.dbml)** — ERD, fonte DBML e premissas. Veja também o diagrama [PNG](technical/data-model/aureus-erd.png).
- **[Modelo de domínio](technical/domain-model/class-diagram.md)** — Diagrama de classes e fonte Mermaid.
  
*A arquitetura final ainda será produzida. Por isso, ERD e domínio são referências de trabalho, não contratos finais.*

## 🎨 UX e Interfaces

**Estado:** Projetado.

Abaixo estão as referências de mockups e especificações para a V1, definindo a identidade visual e o comportamento da interface:

- **Contratos de UX:** 
  - [DESIGN.md](../_bmad-output/planning-artifacts/ux-designs/ux-aureus-product-2026-08-14/DESIGN.md) (Estilo / Visual)
  - [EXPERIENCE.md](../_bmad-output/planning-artifacts/ux-designs/ux-aureus-product-2026-08-14/EXPERIENCE.md) (Comportamento / Jornadas)
- **Mockups e Wireframes:**
  - [Registro de Despesas (Mobile)](../_bmad-output/planning-artifacts/ux-designs/ux-aureus-product-2026-08-14/mockups/registro-despesas-mobile.html)
  - [Consolidação (Mobile)](../_bmad-output/planning-artifacts/ux-designs/ux-aureus-product-2026-08-14/mockups/consolidacao-mobile.html)
  - [Consolidação (Desktop)](../_bmad-output/planning-artifacts/ux-designs/ux-aureus-product-2026-08-14/mockups/consolidacao-desktop.html)

## ⚖️ Decisões

Registre decisões que alterem escopo, entendimento ou coerência entre produto e técnica na pasta `decisions/`, usando o formato `YYYY-MM-DD-slug.md`. Cada decisão deve conter: contexto, decisão, impacto, status e links para as fontes afetadas.

- [Autenticação na V1](decisions/2026-08-13-autenticacao-v1.md) — adota OAuth 2.0 (Google), substituindo a senha local e isolando dados por usuário.

---
*Nota: A pasta `_bmad-output/` na raiz mantém a trilha histórica de geração dos agentes e não substitui esta biblioteca pública consolidada.*
