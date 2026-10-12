---
title: 'Story 6.4: Infraestrutura de Validação Contínua Local e Documentação Canônica de Testes'
type: 'feature'
created: '2026-10-06'
status: 'done'
baseline_commit: '07080d4330bb24c23c2d5ac77370a7b3a1c7711b'
review_loop_iteration: 0
context:
  - 'README.md'
  - 'CONTRIBUTING.md'
  - '_bmad-output/implementation-artifacts/epic-6-context.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O Aureus V1 possui três camadas robustas de testes (unitários de domínio/serviços, integração com Testcontainers e componentes frontend), mas carece de um ponto de entrada unificado para execução automatizada local e de documentação canônica centralizada que formalize a arquitetura de qualidade, matriz de cobertura e a decisão técnica do adiamento dos testes E2E.

**Approach:** Criar o script de validação contínua local (`scripts/validate.sh`) com suporte a execução sequencial com fail-fast, feedback visual e flag opcional para testes de integração, produzir a documentação técnica canônica (`docs/architecture/testing.md`) cobrindo as 3 camadas e o racional do Playwright pós-V1, e integrar referências no `README.md` e `CONTRIBUTING.md`.

## Boundaries & Constraints

**Always:** O script `scripts/validate.sh` deve ser portável para Linux/macOS (bash padrão), executável a partir de qualquer diretório de trabalho, com permissão de execução (`chmod +x`), e retornar código de saída zero apenas se todas as etapas passarem. O modo padrão do script deve rodar apenas testes que não exigem Docker ativo.

**Ask First:** Alterações no ciclo de build ou nos comandos padrão dos projetos (`backend-api/pom.xml` ou `frontend-web/package.json`).

**Never:** Não tornar a inicialização do Docker/Testcontainers obrigatória no fluxo padrão diário do desenvolvedor solo. Não introduzir dependências ou scaffolds de Playwright/Cypress na versão V1.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Execução padrão do script | `./scripts/validate.sh` (sem parâmetros) | Executa ordenadamente: 1. Backend unitário (`./mvnw test`), 2. Frontend lint (`npm run lint`), 3. Frontend build (`npm run build`), 4. Frontend unitário (`npm test`). Exibe banner de sucesso e tempo total. | Se qualquer etapa falhar, interrompe imediatamente (fail-fast) com código de saída 1 e destaca a etapa com erro. |
| Execução com suíte de integração | `./scripts/validate.sh --with-integration` ou `-i` | Executa a suíte padrão e adiciona a etapa de testes de integração com Testcontainers (`./mvnw verify` no backend). | Se o Docker não estiver ativo ou houver falha de integração, exibe erro explicativo e sai com código 1. |
| Ajuda e opções inválidas | `./scripts/validate.sh --help` ou `-h` | Exibe menu de ajuda com descrição das flags disponíveis (`--with-integration`, `-i`, `--help`, `-h`) e sai com código 0. Parâmetro inválido exibe erro e sai com código 1. | Exibe mensagem de uso e parâmetros aceitos. |
| Execução fora da raiz | Executado de dentro de `backend-api/` ou `scripts/` | Resolve dinamicamente o `PROJECT_ROOT` a partir do caminho físico do script e executa com sucesso. | Falha com mensagem amigável se não localizar os diretórios `backend-api` e `frontend-web`. |

</frozen-after-approval>

## Code Map

- `scripts/validate.sh` -- Script bash unificado de validação contínua local; orquestra testes do backend e frontend com suporte a flags (`--with-integration`, `--help`), cálculo de duração e saída colorida.
- `docs/architecture/testing.md` -- Documentação técnica canônica de testes do Aureus V1; detalha as 3 camadas, matriz de regras de negócio/segurança/UX, guia do desenvolvedor solo e registro formal do adiamento do Playwright.
- `README.md` -- Documento principal do repositório; atualizado para referenciar o guia de testes e o script de validação no índice de navegação e seção de desenvolvimento.
- `CONTRIBUTING.md` -- Guia de contribuição; atualizado para recomendar a execução do script `scripts/validate.sh` antes de abrir Pull Requests.
- `_bmad-output/implementation-artifacts/sprint-status.yaml` -- Rastreamento de progresso da sprint; atualização de status da Story 6.4.

## Tasks & Acceptance

**Execution:**
- [x] `scripts/validate.sh` -- Criar script executável com funções modulares, suporte a argumentos (`--with-integration`, `-i`, `--help`, `-h`), resolução de caminho relativo, colorização e fail-fast -- Fornece ferramenta canônica de validação local unificada (AC 1).
- [x] `docs/architecture/testing.md` -- Criar documento canônico detalhando a arquitetura de testes em 3 camadas, matriz qualitativa de cobertura, guia prático para desenvolvedor solo e registro do adiamento do Playwright para pós-V1 -- Fornece especificação canônica de qualidade e rastreabilidade técnica (AC 2).
- [x] `CONTRIBUTING.md` -- Adicionar instrução de validação contínua pré-PR com `./scripts/validate.sh` -- Alinha o fluxo de trabalho do desenvolvedor aos padrões de qualidade estabelecidos.
- [x] `README.md` -- Adicionar menção à documentação de testes e comandos de validação rápida -- Garante visibilidade e descoberta das ferramentas de qualidade no repositório.
- [x] `_bmad-output/implementation-artifacts/sprint-status.yaml` -- Atualizar status da story 6.4 para in-progress e registrar progresso.

**Acceptance Criteria:**
- Given as suítes de teste configuradas no backend e frontend, when o script `./scripts/validate.sh` for executado na raiz ou em subpastas, then executa ordenadamente os testes unitários do backend (`./mvnw test`) e a tríade do frontend (`npm run lint`, `npm run build`, `npm test`) sem requerer Docker.
- Given o script `./scripts/validate.sh`, when invocado com a flag `--with-integration` ou `-i`, then inclui adicionalmente a etapa de testes de integração com Testcontainers (`./mvnw verify`).
- Given a pasta `docs/architecture/`, when inspecionado o arquivo `testing.md`, then apresenta as 3 camadas de teste (unitário backend, integração backend e componentes frontend), matriz de cobertura com regras financeiras e segurança multi-tenant, guia de comandos e registro formal do adiamento do Playwright para pós-V1.
- Given os documentos `README.md` e `CONTRIBUTING.md`, when consultados por um desenvolvedor, then fornecem orientações claras sobre como validar as mudanças localmente e onde encontrar a documentação canônica de testes.

### Review Findings

- [x] [Review][Decision] Ordem de execução das etapas do frontend — Mantida a ordem 'npm test' antes de 'npm run build' visando fail-fast ágil (Decisão do usuário: Opção 1)
- [x] [Review][Patch] Adicionar exit 130 e exit 143 explícitos aos traps de sinais INT e TERM no validate.sh [scripts/validate.sh:140]
- [x] [Review][Patch] Adicionar checagem da presença do binário java nos pré-requisitos do validate.sh [scripts/validate.sh:96]
- [x] [Review][Patch] Adicionar checagem da presença de frontend-web/node_modules nos pré-requisitos do validate.sh [scripts/validate.sh:105]
- [x] [Review][Patch] Incluir flag batch mode (-B) nas chamadas do ./mvnw no validate.sh [scripts/validate.sh:172]
- [x] [Review][Patch] Documentar comandos para testes individuais/granulares no guia de testes [docs/architecture/testing.md:175]
- [x] [Review][Patch] Esclarecer isolamento de dados de teste (tenants únicos) e execução in-memory do MockMvc [docs/architecture/testing.md:71]
- [x] [Review][Patch] Sincronizar status da story no frontmatter para done após resolução de todos os achados [_bmad-output/implementation-artifacts/spec-6-4-infraestrutura-de-validacao-continua-local-e-documentacao-canonica-de-testes.md:5]
- [x] [Review][Defer] Otimizar execução do modo --with-integration para evitar repetição da suíte unitária [scripts/validate.sh:172] — deferred, improvement
- [x] [Review][Defer] Avaliar introdução de ferramentas de cobertura quantitativa (JaCoCo e Vitest coverage) [docs/architecture/testing.md:120] — deferred, future
- [x] [Review][Defer] Suporte a flags modulares (--frontend e --backend) no validate.sh [scripts/validate.sh:65] — deferred, improvement
- [x] [Review][Defer] Configuração de linter e ferramenta de análise estática no backend Maven [backend-api/pom.xml:153] — deferred, pre-existing

## Spec Change Log

- 2026-10-11: Code review concluído com 1 decisão confirmada pelo usuário (manutenção da ordem fail-fast de npm test antes de npm run build), 7 patches aplicados (traps específicos de sinais INT 130 e TERM 143 no validate.sh, validações de pré-requisitos para java e node_modules, inclusão da flag batch mode -B no Maven, documentação de comandos granulares/individuais e detalhamento do MockMvc in-memory e isolamento multi-tenant dinâmico em testing.md, sincronização de status para done), 4 itens deferidos e 3 dismiss.
- 2026-10-06: Implementação completa da Story 6.4 — criação de scripts/validate.sh (suporte a -i e -h, fail-fast, tempo), documentação canônica docs/architecture/testing.md, atualização do README.md e CONTRIBUTING.md, e sincronização do sprint-status.yaml.

## Design Notes

- **Resolução de Caminhos no Script**: O script deve calcular `PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"` garantindo funcionamento agnóstico ao CWD do chamador.
- **Fail-Fast**: Utilizar `set -eo pipefail` para garantir que falhas intermediárias encerrem a execução imediatamente, evitando falsos positivos.
- **Isolamento de Docker**: Por padrão, o desenvolvedor solo deve ser capaz de rodar `./scripts/validate.sh` em menos de 30 segundos sem subir containers Docker pesados. A verificação via Testcontainers fica reservada para a flag opcional `--with-integration`.

## Verification

**Commands:**
- `./scripts/validate.sh --help` -- expected: Exibe banner de ajuda e opções válidas sem erros.
- `./scripts/validate.sh` -- expected: Executa backend unitário, frontend lint, build e tests, concluindo com sucesso (código 0).
- `test -f docs/architecture/testing.md` -- expected: Arquivo existe e contém todas as seções canônicas exigidas.

## Suggested Review Order

**Automação e Execução Contínua Local**

- Script de validação contínua unificado com flags CLI, fail-fast e cálculo de tempo
  [`validate.sh:1`](../../scripts/validate.sh#L1)

- Verificação de pré-requisitos locais e orquestração de testes antes de build
  [`validate.sh:92`](../../scripts/validate.sh#L92)

**Documentação Canônica de Qualidade**

- Visão geral das três camadas de teste e filosofia de qualidade
  [`testing.md:20`](../../docs/architecture/testing.md#L20)

- Matriz qualitativa de regras financeiras, segurança multi-tenant e UX
  [`testing.md:122`](../../docs/architecture/testing.md#L122)

- Racional técnico e critérios formais do adiamento do Playwright para pós-V1
  [`testing.md:147`](../../docs/architecture/testing.md#L147)

- Guia prático de comandos do desenvolvedor solo e checklist pré-PR
  [`testing.md:173`](../../docs/architecture/testing.md#L173)

**Diretrizes e Descoberta no Repositório**

- Regra de validação pré-PR e referências no guia de contribuição
  [`CONTRIBUTING.md:8`](../../CONTRIBUTING.md#L8)

- Índice do projeto e seção dedicada de testes no README
  [`README.md:34`](../../README.md#L34)

