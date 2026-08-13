---
name: Aureus
status: final
sources:
  - ../../../briefs/brief-projeto-aureus-docs-2026-08-02/brief.md
  - ../../../prds/prd-projeto-aureus-docs-2026-08-02/prd.md
  - ../../../../../README.md
  - ../../../../../CONTRIBUTING.md
updated: 2026-08-13
colors:
  system: Tema nativo claro/escuro do GitHub
typography:
  system: Renderização Markdown do GitHub
rounded:
  system: Componentes nativos do GitHub
spacing:
  system: Espaçamento padrão de GFM
components:
  links: Links Markdown nativos
  tables: Tabelas GFM compactas
  callouts: Alertas GitHub quando necessários
---

# Design da documentação do Aureus

## Marca e estilo

O repositório é a entrada pública do **Aureus**, um projeto de finanças pessoais
desenvolvido como portfólio por um único desenvolvedor e sem fins comerciais no
momento. A qualidade visual vem de clareza, nomes estáveis e status honestos —
nunca de ornamentação ou aparência corporativa artificial.

Use Markdown e GFM nativos. Emojis podem ser usados com parcimônia em títulos
como sinalização semântica e personalidade do portfólio; o texto do título deve
continuar claro sem depender do emoji. Não introduza logo, ilustrações, paleta,
badges ou biblioteca de componentes próprios nesta fase. Este documento e
`EXPERIENCE.md` prevalecem sobre futuros mockups em caso de conflito.

## Cores

Use o tema nativo claro ou escuro do GitHub. Todo status é escrito por extenso:
**Disponível**, **Planejado**, **Em revisão** ou **Arquivado**; cor nunca é o
único portador de significado.

## Tipografia

Use títulos Markdown em hierarquia curta, prosa concisa na fonte nativa do
GitHub e fonte monoespaçada apenas para caminhos, artefatos e comandos. Escreva
em português brasileiro, com títulos concretos como “O que é”, “Estado atual”,
“Produto” e “Técnico”.

## Layout e espaçamento

Use o espaçamento padrão do GitHub. O README raiz começa pela proposta do
produto, seguida do estado atual e da rota documental principal. Mantenha seções
curtas, uma ação principal por seção e blocos de código apenas para a árvore do
repositório. Evite imagem hero, HTML em colunas ou linhas densas de badges.

## Elevação e profundidade

Não há elevação personalizada. A renderização nativa do GitHub fornece a única
camada visual; títulos e espaço em branco estabelecem prioridade.

## Formas

Use os componentes nativos do GitHub. Não adicione cards SVG, badges em pílula
ou navegação baseada em imagens.

## Componentes

| Elemento | Uso |
| --- | --- |
| Link Markdown | Navegar entre fontes canônicas, com rótulo descritivo. |
| Tabela GFM | Mostrar estado, finalidade e próximo destino de artefatos. |
| Bloco de código | Exibir a árvore documental ou comandos reproduzíveis. |
| Alerta GitHub | Destacar limites relevantes, não decorar a página. |
| Checklist | Indicar manutenção local e verificável, sem cerimônia artificial. |

## Faça e não faça

**Faça:** use links relativos, rótulos consistentes, status explícito e datas
quando relevantes. Mostre somente o que existe e conduza o visitante pela
jornada produto → técnico.

**Não faça:** prometa API, wireframes, interface, autenticação ou deploy antes
de existirem; replique conteúdo canônico; use imagens ou emojis para transportar
informação essencial; nem simule maturidade comercial com métricas, badges ou
processos que um único mantenedor não executa.
