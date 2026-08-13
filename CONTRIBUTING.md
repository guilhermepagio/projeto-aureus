# Contribuindo com o Aureus

O Aureus é um projeto de portfólio pessoal, sem fins comerciais no momento,
desenvolvido e mantido por um único desenvolvedor. Este guia mantém a
documentação clara sem criar um processo de equipe artificial.

## Fonte de verdade

- `docs/` contém os documentos canônicos para leitura humana.
- `_bmad-output/` preserva histórico e artefatos de trabalho do BMad.
- Uma mudança de requisito começa em `docs/product/prd.md`.
- Uma mudança técnica atualiza o documento técnico correspondente e, quando
  alterar entendimento ou escopo, registra uma decisão em
  `docs/decisions/YYYY-MM-DD-slug.md`.
- Diagramas devem manter sua fonte editável e um resumo textual acessível.

## Fluxo de mudança

1. Explique o motivo e o impacto da mudança.
2. Atualize a fonte canônica em `docs/`.
3. Atualize índices, links, status e artefatos derivados.
4. Preserve o histórico do BMad; não crie cópias canônicas concorrentes.
5. Revise a sequência `README → PRD → técnico` antes de publicar.

Contribuições externas são bem-vindas quando respeitam esse fluxo. Como o
projeto tem um único mantenedor, não há SLA, equipe de revisão ou promessa de
integração automática.

## Branches e commits

Use nomes curtos e descritivos, por exemplo `docs/readme-inicial`,
`arch/modelo-dominio` ou `fix/typo-erd`.

Adote Conventional Commits:

```text
docs: reorganiza biblioteca documental
fix: corrige relação no modelo de dados
chore: atualiza configuração do BMad
```

Use verbos no presente, descrição em minúsculas e uma primeira linha de até 72
caracteres.

## Checklist

- [ ] A fonte canônica em `docs/` foi atualizada.
- [ ] Links relativos funcionam.
- [ ] Status está escrito por extenso.
- [ ] Diagramas têm fonte editável e explicação textual.
- [ ] O README ainda conduz do produto ao técnico.
