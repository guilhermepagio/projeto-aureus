---
name: Aureus
status: final
sources:
  - ../../../briefs/brief-projeto-aureus-docs-2026-08-02/brief.md
  - ../../../prds/prd-projeto-aureus-docs-2026-08-02/prd.md
  - ../../../../../README.md
  - ../../../../../CONTRIBUTING.md
  - ../../../../../docs/technical/data-model/README.md
  - ../../../../../docs/technical/domain-model/README.md
updated: 2026-08-13
---

# Experiência da documentação do Aureus

## Foundation

Este contrato cobre a experiência de documentação do repositório GitHub do Aureus, não a interface do futuro sistema financeiro. O Aureus é um projeto de portfólio pessoal, sem fins comerciais no momento, mantido por um único desenvolvedor. A documentação deve evidenciar organização e pensamento de produto sem criar uma carga operacional desproporcional.

**Form factor:** GitHub web e mobile, lido em Markdown/GFM. **Sistema de UI:** GitHub nativo. [DESIGN.md](DESIGN.md) é a referência visual; aplicar o tema, a tipografia e o espaçamento nativos do GitHub. Não há wireframes nesta etapa: eles pertencem à futura UX do produto e não bloqueiam a organização do repositório.

## Information Architecture

A jornada canônica é **produto antes de técnico**: o visitante entende por que e
o que será construído antes de examinar como isso será estruturado.

```text
README.md
CONTRIBUTING.md
docs/
  README.md
  product/
    README.md
    brief.md
    prd.md
    roadmap.md
  technical/
    README.md
    data-model/
      README.md
      aureus-erd.dbml
      aureus-erd.png
    domain-model/
      README.md
      class-diagram.md
  ux/
    README.md
  decisions/
    README.md
    YYYY-MM-DD-slug.md
```

| Superfície | Pergunta respondida | Próximo destino |
| --- | --- | --- |
| `README.md` | O que é o Aureus, em que estágio está e por que este repositório existe? | `docs/product/prd.md` |
| `docs/README.md` | Onde está a fonte canônica de cada assunto? | Produto primeiro; técnico em seguida |
| `docs/product/prd.md` | Por que e o que a V1 precisa resolver? | `docs/technical/README.md` |
| `docs/technical/README.md` | Como o produto é modelado tecnicamente hoje? | Modelo de dados e modelo de domínio |
| `docs/ux/README.md` | Há UX do produto disponível? | Informar que wireframes são futuros; não fingir que existem |
| `docs/decisions/README.md` | Quais decisões alteram entendimento, escopo ou coerência? | Decisão relevante e seu contexto |
| `CONTRIBUTING.md` | Como propor ou manter mudanças sem burocracia artificial? | Fonte canônica afetada |

O Brief, o PRD, o roadmap e as decisões curadas em `docs/` são canônicos para
leitura humana. `_bmad-output/` preserva o histórico de trabalho do BMad;
depois de validado, um artefato é consolidado em `docs/`. O histórico não é
apagado, mas não compete como segunda versão canônica.

## Voice and Tone

Escreva em português brasileiro, com tom profissional, simples e verificável.
Explique limites sem se desculpar: “Projeto de portfólio, sem fins comerciais no
momento” informa contexto; não é uma promessa de suporte. Prefira frases curtas,
verbos ativos e termos estáveis. A voz de marca é definida em
[DESIGN.md](DESIGN.md#brand--style).

Microcopy de referência:

| Situação | Texto |
| --- | --- |
| Contexto | “Projeto de portfólio pessoal, desenvolvido por um único mantenedor.” |
| Artefato disponível | “Disponível — fonte canônica atual.” |
| Artefato planejado | “Planejado — ainda não disponível neste repositório.” |
| Artefato em mudança | “Em revisão — consulte o documento vinculado para o estado atual.” |
| Próxima leitura | “Entenda o porquê e o escopo no PRD.” |

## Component Patterns

| Padrão | Comportamento |
| --- | --- |
| Declaração de contexto | Surge próximo ao início do README: produto de portfólio, solo e não comercial. |
| Rota primária | O primeiro link de aprofundamento leva ao PRD, não aos diagramas. |
| Catálogo de documentos | Cada índice apresenta finalidade, estado e link; não duplica o conteúdo do destino. |
| Status de artefato | Usa os textos definidos em Voz e tom e, se aplicável, uma data. O estado nunca é inferido pela existência de uma pasta vazia. |
| Diagrama técnico | É acompanhado de README com propósito, fonte editável e premissas/limitações. ERD e diagrama de classes são documentação técnica, não contratos finais antes da arquitetura. |
| Documento futuro | A página reservada explica o que será criado, quando relevante, e aponta a etapa que o precede. |

As escolhas visuais desses padrões usam os componentes nativos de
links, tabelas e alertas nativos do GitHub.

## State Patterns

| Estado | Significado | Tratamento |
| --- | --- | --- |
| Disponível | Há conteúdo canônico utilizável. | Link direto, finalidade e data de atualização quando útil. |
| Planejado | O artefato foi identificado, mas ainda não foi produzido. | Declarar ausência; não criar links quebrados nem conteúdo fictício. |
| Em revisão | Existe conteúdo, porém uma decisão importante ainda pode mudá-lo. | Identificar a pendência e apontar a decisão/PRD correspondente. |
| Arquivado | Material histórico, não referência atual. | Manter fora da rota principal e marcar o substituto, se existir. |

Exemplo obrigatório de coerência: enquanto a divergência entre o PRD (V1 sem
autenticação) e modelos que citam `Usuario` não estiver resolvida, os documentos
técnicos relevantes ficam **Em revisão** e explicam a premissa em vez de sugerir
que ambos são definitivos.

## Interaction Primitives

- Links internos usam caminhos relativos e texto que descreve o destino.
- O README raiz oferece uma única sequência explícita: visão → PRD → técnico.
- Índices retornam ao nível acima e indicam o próximo passo, evitando becos sem
  saída em navegação mobile.
- Uma alteração de requisito começa no PRD; uma alteração de solução começa na
  decisão/artefato técnico correspondente; cada uma atualiza os links e estados
  afetados.
- O fluxo é leve: uma pessoa pode propor, revisar e registrar a própria decisão,
  desde que preserve fonte canônica, motivo e impacto. Contribuições externas
  seguem o mesmo padrão, sem exigir ritos de equipe inexistentes.
- Cada decisão relevante é um arquivo `docs/decisions/YYYY-MM-DD-slug.md` com
  contexto, decisão, impacto, status e links. A primeira registra a divergência
  entre a V1 sem autenticação e os modelos que ainda citam `Usuario`.
- O diagrama de classes atual migra para
  `docs/technical/domain-model/class-diagram.md`, com a fonte Mermaid, resumo
  textual e status **Em revisão** até a decisão de autenticação e a arquitetura.

## Accessibility Floor

- A informação essencial deve existir como texto Markdown, não apenas em imagens
  de diagrama.
- Diagramas têm fonte editável, título claro e resumo textual de relações ou
  premissas; imagens recebem texto alternativo significativo quando publicadas.
- Hierarquia usa níveis sequenciais de título; links têm nomes descritivos.
- Tabelas têm cabeçalhos simples e não carregam conteúdo indispensável sem uma
  alternativa em prosa para leitura estreita/mobile.
- Status combina palavra, contexto e data quando necessário; nunca depende só de
  cor.
- Evitar HTML decorativo e layouts que prejudiquem zoom, tema escuro, leitor de
  tela ou navegação por teclado do GitHub.

## Key Flows

### Visita de Marina, recrutadora técnica

1. Marina abre `README.md` pelo GitHub e lê, nos primeiros blocos, que Aureus é
   um sistema simples e poderoso para controle de finanças pessoais.
2. Ela vê o contexto honesto: portfólio solo, não comercial, e o estado atual do
   trabalho.
3. Ela segue o link principal para o PRD e entende o problema, o comportamento
   esperado e os limites da V1.
4. **Ponto de decisão:** ao terminar o PRD, Marina escolhe “Como está sendo pensado” e
   chega ao índice técnico, onde encontra o ERD e o modelo de domínio com seus
   estados explícitos.
5. Ela conclui que a organização, rastreabilidade e limites assumidos refletem o
   padrão profissional do mantenedor, mesmo sem uma interface pronta.

### Manutenção solo por Guilherme

1. Guilherme muda uma decisão de escopo ou identifica uma inconsistência.
2. Ele atualiza primeiro a fonte canônica apropriada em `docs/` e registra a
   decisão quando ela muda entendimento ou impacto.
3. Ele atualiza artefatos derivados, seus estados e os índices que os apontam.
4. **Ponto de decisão:** antes de publicar, ele percorre README → PRD → técnico e confirma
   que não há promessa de artefato ausente ou versão concorrente em
   `_bmad-output/`.
5. O repositório continua simples de manter e claro para o próximo visitante.

## Planned UX Boundary

Wireframes de baixa fidelidade do produto serão criados em uma etapa posterior,
quando os fluxos do Aureus forem desenhados. Eles devem cobrir navegação,
cadastros, estados vazios, formulários/listas e a consolidação de 24 meses, mas
não fazem parte deste contrato de organização do repositório.
