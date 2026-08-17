---
title: 'Sync Auth Models'
type: 'story'
created: '2026-08-17'
status: 'done'
route: 'one-shot'
---

# Sync Auth Models

## Intent

**Problem:** The technical models (ERD and class diagram) still represented legacy local password authentication, which was deprecated in favor of Google OAuth 2.0 in the V1 PRD.
**Approach:** Removed local password fields and added Google OAuth 2.0 identity fields (`provider_issuer`, `provider_subject`) to the data model and domain class diagram. Added `Usuario` explicitly to the class diagram and updated `README.md` to finalize the authentication decision.

## Suggested Review Order

1. [docs/technical/data-model/aureus-erd.dbml](../../docs/technical/data-model/aureus-erd.dbml) — Updated `usuarios` table with OAuth fields, image URL, and last access.
2. [docs/technical/domain-model/class-diagram.md](../../docs/technical/domain-model/class-diagram.md) — Replaced draft status, added `Usuario` entity with OAuth fields, linked to `ContextoFinanceiro`.
3. [docs/README.md](../../docs/README.md) — Updated ADR status to finalize the authentication decision.
