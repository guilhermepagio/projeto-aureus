# 🤝 Contribuindo com o Aureus

O Aureus é um projeto de portfólio pessoal, sem fins comerciais no momento, desenvolvido e mantido por um único desenvolvedor. Este guia mantém a documentação clara sem criar um processo de equipe artificial.

## Fonte de verdade

- `docs/` contém os documentos canônicos para leitura humana.
- `_bmad-output/` preserva histórico e artefatos de trabalho do BMad.

## Fluxo de mudança

Como o projeto tem um único mantenedor, não há SLA, equipe de revisão ou promessa de integração automática.

## Branches e commits

O projeto não utiliza *Issues* do GitHub para o acompanhamento do backlog. Todas as tarefas derivam diretamente do PRD (Product Requirements Document).

Portanto, as branches seguem a nomenclatura: `<tipo-alteracao>/<escopo>-<descricao-curta>`.

Para facilitar, adote a tabela abaixo tanto para o prefixo da branch quanto para o seu respectivo *Conventional Commit*:

| Prefixo | Uso | Exemplo de Branch | Exemplo de Commit |
| :--- | :--- | :--- | :--- |
| **`feat`** | Nova funcionalidade ou requisito do PRD. | `feat/auth-adiciona-jwt` | `feat: adiciona validacao jwt no login` |
| **`fix`** | Correção de bug ou falha. | `fix/ui-alinhamento-botoes` | `fix: corrige desalinhamento do botao na home` |
| **`docs`** | Alterações exclusivas de documentação. | `docs/prd-atualiza-requisitos` | `docs: atualiza arquitetura no prd` |
| **`refactor`** | Mudança estrutural sem alterar comportamento. | `refactor/api-limpeza-controlador` | `refactor: extrai validacao para servico` |
| **`test`** | Criação ou ajuste de testes automatizados. | `test/auth-cobertura-login` | `test: garante cobertura de falha no login` |
| **`chore`** | Atualizações de ferramentas, BMad ou configs. | `chore/bmad-atualiza-skill` | `chore: atualiza dependencias do projeto` |

Use verbos no presente, descrição em minúsculas e uma primeira linha de até 72 caracteres.