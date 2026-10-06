# 🤝 Contribuindo com o Aureus

O Aureus é um projeto de portfólio pessoal, sem fins comerciais no momento, desenvolvido e mantido por um único desenvolvedor. Este guia mantém a documentação clara sem criar um processo de equipe artificial.

## Fonte de verdade

- `_bmad-output/` centraliza a documentação viva, planejamento, requisitos e artefatos de trabalho do BMad.
- `docs/architecture/testing.md` formaliza a arquitetura canônica de testes automatizados, matriz de cobertura e decisões de engenharia de qualidade.

## Validação local contínua (Pré-PR)

Antes de abrir um Pull Request para a branch `main`, execute sempre a validação contínua local para garantir que os testes unitários, linter, tipagem e build estejam 100% íntegros:

```bash
# Validação padrão e rápida (~20-25s, sem dependência de Docker):
./scripts/validate.sh
```

Caso a alteração envolva regras de persistência relacional, migrações Flyway, entidades JPA ou filtros de segurança, execute também a suíte de integração com Testcontainers (requer o serviço do Docker ativo):

```bash
# Validação completa com testes de integração:
./scripts/validate.sh --with-integration
```

Para mais detalhes sobre a arquitetura de 3 camadas, comandos pontuais e matriz de cobertura, consulte [`docs/architecture/testing.md`](./docs/architecture/testing.md).

## Fluxo de mudança

Como o projeto tem um único mantenedor, não há SLA, equipe de revisão ou promessa de integração automática.
O repositório adota proteção na branch `main`, de modo que todas as alterações passam obrigatoriamente por Pull Request.
O título do Pull Request acompanha o próprio nome da branch (ex.: `feature/epico-5`, `planning/epico-6`). Para detalhamento do escopo e requisitos de cada épico, consulte a documentação correspondente em `_bmad-output/`.

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