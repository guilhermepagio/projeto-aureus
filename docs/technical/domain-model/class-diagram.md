---
title: Modelo de domínio Java
status: em revisão
---

# Diagrama de classes

Este é um rascunho de domínio. `Conta`, `Categoria`, `Despesa` e `Receita` compõem o contexto financeiro; `Parcelamento` representa a distribuição mensal. `Usuario` permanece no desenho por compatibilidade histórica, mas autenticação está fora da V1 conforme o PRD.

```mermaid
classDiagram
class Conta { +UUID id +String descricao +String observacoes }
class Categoria { +UUID id +String descricao +String observacoes }
class TipoMovimento { <<enumeration>> FIXO VARIAVEL }
class ContextoFinanceiro { +Conta conta +Categoria categoria +TipoMovimento tipoMovimento }
class Parcelamento { +BigDecimal valorParcela +Integer quantidadeParcelas +LocalDate dataPrimeiraParcela +LocalDate dataUltimaParcela }
class Despesa { +UUID id +ContextoFinanceiro contexto +Parcelamento parcelamento +String descricao }
class Receita { +UUID id +ContextoFinanceiro contexto +Parcelamento parcelamento +String descricao }
Despesa --> ContextoFinanceiro
Despesa --> Parcelamento
Receita --> ContextoFinanceiro
Receita --> Parcelamento
```

Este modelo foi consolidado a partir do diagrama de classes inicial. A arquitetura definirá o modelo final.
