# 🤝 Contribuindo com o Aureus

O Aureus é um projeto de portfólio pessoal, sem fins comerciais no momento, desenvolvido e mantido por um único desenvolvedor. Este guia mantém a documentação clara sem criar um processo de equipe artificial.

## Fonte de verdade

- `docs/` contém os documentos canônicos para leitura humana.
- `_bmad-output/` preserva histórico e artefatos de trabalho do BMad.

## Fluxo de mudança

Como o projeto tem um único mantenedor, não há SLA, equipe de revisão ou promessa de integração automática.
O repositório adota proteção na branch `main`, de modo que todas as alterações passam obrigatoriamente por Pull Request.

## Nomenclatura de branches

Para manter o fluxo enxuto e produtivo de um desenvolvedor solo, as branches são divididas em apenas 3 categorias:

| Prefixo | Uso | Exemplo de Branch |
| :--- | :--- | :--- |
| **`planning/`** | Concepção, especificação e planejamento de épicos (PRD, sprint status, arquitetura, design de testes). | `planning/epico-5` |
| **`feature/`** | Construção de código em geral (desenvolvimento do épico, suítes de testes, novas telas, refactors, CI/CD). | `feature/epico-5` |
| **`hotfix/`** | Correções urgentes e emergenciais aplicadas diretamente a partir da `main`. | `hotfix/corrige-trava-saldo` |

## Padrão de commits

Dentro de qualquer branch, os commits seguem a convenção do *Conventional Commits* para manter o histórico granular, legível e auditável:

| Prefixo | Uso | Exemplo de Commit |
| :--- | :--- | :--- |
| **`feat`** | Nova funcionalidade ou capacidade de negócio/técnica. | `feat: adiciona validacao jwt no login` |
| **`fix`** | Correção de bug ou falha. | `fix: corrige desalinhamento do botao na home` |
| **`test`** | Criação, ajuste ou configuração de testes automatizados. | `test: garante cobertura de falha no login` |
| **`docs`** | Alterações exclusivas de documentação ou artefatos de planejamento. | `docs: detalha epico 5 no sprint status` |
| **`refactor`** | Mudança estrutural sem alterar comportamento. | `refactor: extrai validacao para servico` |
| **`chore`** | Atualizações de ferramentas, BMad, build ou dependências. | `chore: arquiva artefatos da story` |

Use verbos no presente, descrição em minúsculas e uma primeira linha de até 72 caracteres.