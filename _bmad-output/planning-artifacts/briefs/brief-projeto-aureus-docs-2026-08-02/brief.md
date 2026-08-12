---
title: "Product Brief: Aureus"
status: complete
created: 2026-08-02
updated: 2026-08-02
---

# Product Brief: Aureus

## Resumo Executivo

**Aureus** é um sistema fullstack de gestão de finanças pessoais que digitaliza um fluxo comprovado de mais de dois anos operado em planilha. A aplicação permite registrar receitas e despesas — fixas ou variáveis — e apresenta um painel de consolidação mensal em formato grid com visão de 24 meses, exibindo receitas, despesas, distribuição e percentual por categoria, além da projeção de saldo acumulado.

O projeto é desenvolvido utilizando uma stack moderna (Java 21 e Spring Boot 4 no backend, React 19, Vite e TailwindCSS no frontend, com banco de dados PostgreSQL) e atua como veículo prático para o domínio do modelo **Spec-Driven Development (SDD)** com abordagem **AI-First**. No Aureus, especificações técnicas rigorosas guiam a codificação assistida por IA, mantendo o desenvolvedor no controle integral da arquitetura e das regras de negócio.

O nome remete ao *aureus*, moeda de ouro da Roma Antiga, simbolizando valor, estabilidade financeira e solidez estrutural.

## O Problema

Gerir finanças pessoais com clareza — sabendo com exatidão o fluxo de caixa, o impacto de cada categoria e a projeção de saldo futuro — é uma necessidade concreta que ferramentas genéricas ou bancárias raramente atendem com simplicidade e precisão.

O Aureus surge a partir de uma metodologia de controle financeiro validada na prática por mais de dois anos através de planilhas. Essa experiência sólida serviu como base para construir uma aplicação que resolve um problema real: o controle fino de movimentações financeiras combinando parcelamentos, projeções retroativas e categorização sem complexidade desnecessária na interface.

Hoje o projeto opera como instrumento de aprendizado e portfólio de engenharia; no futuro, conta com arquitetura estruturada para evoluir de ferramenta pessoal para um produto aberto ao mercado.

## A Solução

O Aureus é uma aplicação web organizada em cinco áreas funcionais integradas:

- **Registro de Movimentações:** Quatro abas dedicadas permitem cadastrar despesas variáveis, despesas fixas, receitas variáveis e receitas fixas. Cada modalidade exibe campos calibrados para seu contexto — movimentações variáveis controlam locais de compra, parcelamento e datas, enquanto fixas focam em valores recorrentes. Campos calculados (valor total e data da última parcela) são gerados automaticamente pelo sistema.
- **Contas e Categorias Universais:** O usuário cadastra suas Contas (origens e destinos do recurso) e Categorias uma única vez. Esses registros são compartilhados globalmente em toda a aplicação, podendo ser utilizados para qualquer movimentação (receitas ou despesas, fixas ou variáveis).
- **Painel de Consolidação Mensal:** A quinta aba apresenta uma grade financeira com 24 meses de alcance, a partir de um mês inicial definido pelo usuário. A consolidação compila cinco blocos sequenciais:
  1. **Receitas:** Linhas por conta demonstrando os ganhos mensais.
  2. **Despesas:** Linhas por conta demonstrando os gastos mensais.
  3. **Categorias (R$):** Valor absoluto gasto em cada categoria por mês.
  4. **Categorias (%):** Percentual de representatividade de cada categoria em relação ao total gasto no mês.
  5. **Resumo Geral:** Linhas consolidando Total Gasto no Mês, Sobra de Receita do Mês e Sobra Retroativa Acumulada (somatório das sobras anteriores, habilitando projeções seguras).

A usabilidade orienta-se pela filosofia de design da Apple: visual limpo, moderno e direto, tornando invisível a complexidade das regras matemáticas e financeiras subjacentes.

## Propósito do Projeto (Abordagem SDD e AI-First)

Mais do que um sistema financeiro, o Aureus é um projeto delineado para demonstrar e refinar práticas profissionais de engenharia de software na era da Inteligência Artificial:

- **Spec-Driven Development (SDD):** A codificação é precedida por contratos e especificações detalhadas (PRD, ERD, diagramas de domínio e contratos de API). A especificação é a fonte única da verdade (*Single Source of Truth*).
- **Engenharia AI-First:** A IA atua como aceleradora na geração do código em estrita conformidade com as especificações. O desenvolvedor assume o papel de arquiteto de sistemas e validador, provando domínio absoluto sobre o que é implementado.
- **Rigor de Craft:** Documentação contínua, arquitetura clara e modelagem prévia comprovam capacidade de entrega ponta a ponta sem atalhos ou código ad-hoc.

## Escopo V1

**O que entra na V1 (Escopo Imediato):**
- Cadastro e reutilização universal de Contas e Categorias.
- 4 abas especializadas para inserção de Despesas Variáveis, Despesas Fixas, Receitas Variáveis e Receitas Fixas.
- Automação de cálculos em tempo real para campos derivados (valores totais e datas finais de parcelamentos).
- Aba de Consolidation Grid (24 meses programáveis) exibindo os 5 blocos analíticos e saldo retroativo acumulado.
- Execução local orientada a uso pessoal em formato Single-Owner.

**O que está fora da V1 (Não-Escopo):**
- Autenticação e autorização multi-usuário (Login / OAuth).
- Dark Mode ou temas visuais customizáveis.
- Deploy em nuvens públicas ou infraestruturas orquestradas em produção.

## Stack e Decisões Técnicas

- **Backend:** Java 21 com Spring Boot 4. Implementação orientada a contratos (API-First / OpenAPI) e modelo de domínio em arquitetura limpa (`ContextoFinanceiro`, `Parcelamento`, `AuditInfo`). Execução local em máquina diretamente a partir do código-fonte.
- **Frontend:** React 19, Vite e TailwindCSS. Execução local diretamente a partir do código-fonte, focado no carregamento rápido e navegação fluida na matriz 24-meses.
- **Banco de Dados:** PostgreSQL para armazenamento transacional isolado (único componente executado em container Docker no ambiente local). Uso mandatório do tipo `numeric` para consistência contábil em valores monetários e `timestamptz` para rastreabilidade de datas.
- **Ambiente V1:** Execução leve (código-fonte + container Docker exclusivo para PostgreSQL), isolando a infraestrutura de rede para foco total em regras de negócio e usabilidade.

## Visão Futura

O Aureus possui alicerces arquiteturais para crescer de forma sustentável após a V1:
- **Autenticação (OAuth 2.0):** Integração de login seguro (Google) para suportar múltiplos usuários (ver `addendum.md`).
- **Interface e Temas:** Suporte a Dark Mode e personalizações ergonômicas de visualização.
- **DevOps e Infraestrutura Avançada:** Orquestração via Kubernetes e pipelines automatizados de CI/CD para exibição de governança e escalabilidade em portfólio.
- **Evolução de Mercado:** Expansão de portfólio acadêmico para produto SaaS para o consumidor final.
