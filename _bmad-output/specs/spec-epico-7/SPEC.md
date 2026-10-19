---
id: SPEC-epico-7
companions:
  - seguranca.md
  - desempenho.md
  - observabilidade.md
  - rastreabilidade.md
sources:
  - ../../planning-artifacts/backlog-triage-2026-10-14.md
---

# Épico 7 — Segurança, desempenho e observabilidade da aplicação

> Contrato canônico: este SPEC e seus quatro companions definem o escopo completo para planejamento, implementação futura e verificação. Derivados das decisões registradas em `.memlog.md` e das fontes ali citadas.

## Why

Proteger os dados financeiros dos usuários do Aureus, restringindo o fallback de autorização, verificando sessão e isolamento, removendo dados privados locais após perda da sessão e instrumentando logs estruturados com contexto de requisição e tenant para diagnóstico e troubleshooting. No banco de dados, garantir que todas as tabelas filtradas por `TenantContext` (`usuario_id`) possuam índice adequado via migração incremental Flyway V2, além de investigar custos de consultas e índices secundários antes de decidir outras otimizações.

## Capabilities

- **CAP-1**
  - **intent:** O sistema limita o acesso anônimo à política pública explícita e protege o restante dos recursos.
  - **success:** A matriz de autorização demonstra API protegida, rotas públicas e OAuth legítimos preservados, fallback restrito e política Actuator definida e verificada conforme `seguranca.md` (DW-13).
- **CAP-2**
  - **intent:** O usuário recebe e encerra uma sessão cujas propriedades de segurança são verificáveis.
  - **success:** Testes focados comprovam atributos e TTL do cookie OAuth e expiração do cookie no logout, conforme `seguranca.md` (DW-14, DW-36).
- **CAP-3**
  - **intent:** O usuário deixa de ter dados privados locais disponíveis ao sair ou quando a aplicação detecta sessão inválida durante o uso.
  - **success:** Logout com sucesso ou erro de rede e respostas 401 durante uso resultam em limpeza de estado/cache privado e retorno ao login, conforme `seguranca.md` (DW-26, DW-60).
- **CAP-4**
  - **intent:** Requisições de usuários distintos mantêm isolamento, e o comportamento sem contexto de tenant tem contrato demonstrado.
  - **success:** Testes verificam população/limpeza dos contextos e resolver; investigação documenta acesso/persistência sem contexto, configuração e bootstrap antes de decidir mudanças, conforme `seguranca.md` (DW-35, DW-37, DW-160, incluindo DW-38).
- **CAP-5**
  - **intent:** Requisições mutantes indevidas não contornam CSRF nem vinculam dados de outros usuários.
  - **success:** A matriz residual cobre PUT/DELETE sem token CSRF e vínculos cruzados faltantes, verificando rejeição e ausência de alteração indevida, conforme `seguranca.md` (DW-176).
- **CAP-6**
  - **intent:** A necessidade de invalidar sessões antes da expiração é decidida com evidência.
  - **success:** A investigação registra contrato atual, risco, alternativas, custo e decisão ou pendências sobre revogação; não declara implementação nem inclui renovação automática, conforme `seguranca.md` (DW-2).
- **CAP-7**
  - **intent:** O custo da consolidação é conhecido, e eventuais melhorias preservam os resultados financeiros.
  - **success:** Tempo, memória e volume de dados/consultas são medidos; a recomendação exige equivalência dos totais mensais e saldo histórico pré-grade antes de otimizar, conforme `desempenho.md` (DW-139, A10).
- **CAP-8**
  - **intent:** Todas as tabelas multi-tenant possuem índice com `usuario_id` na liderança, e as demais decisões de índices reduzem custo comprovado sem perder unicidade ou integridade.
  - **success:** Migração incremental Flyway V2 cria os índices obrigatórios `idx_contas_usuario_id` e `idx_categorias_usuario_id` (preservando `V1` imutável, onde as 4 tabelas de movimentações já possuem `usuario_id` liderando índices compostos); catálogo e planos orientam a avaliação das FKs e dos 4 índices de data sem `usuario_id` (DW-155); somente redundância confirmada permite remover o índice secundário em `usuarios`, preservando UNIQUE (DW-161, A13), conforme `desempenho.md`.
- **CAP-9**
  - **intent:** O custo das listagens e validações orienta mudanças que preservam opções dos seletores e titularidade dos dados.
  - **success:** Volume, tamanho de resposta, latência, consultas da conversão de DTOs e custo da validação de propriedade são registrados separadamente; recomendações preservam isolamento e contratos de acesso, conforme `desempenho.md` (DW-117, DW-147, DW-153).
- **CAP-10**
  - **intent:** Eventos de autenticação, falhas de autorização/validação, exceções e operações críticas produzem logs estruturados com contexto de requisição e tenant para diagnóstico e troubleshooting sem expor dados sensíveis.
  - **success:** O `MDC` popula e limpa `requestId` e `tenantId` em `finally`; filtros de segurança, `AuthController`, `GlobalExceptionHandler` e serviços críticos registram eventos padronizados (`INFO`/`WARN`/`ERROR`/`DEBUG`) sem vazar JWT, cookies ou valores financeiros, com testes de limpeza do `MDC` e contratos de log, conforme `observabilidade.md`.

## Constraints

- O corte final compreende os 16 DW ativos da triagem (8 DW de segurança, 1 planejamento condicional `DW-161` e 7 DW originalmente investigativos, sendo a criação de índice para `usuario_id` em `contas` e `categorias` de `DW-155` já aprovada para implementação em Flyway V2), os 2 action items abertos (`A10`, `A13`) e a capacidade de observabilidade (`CAP-10`); vínculos e exclusões estão em `rastreabilidade.md`. Aprovação de planejamento não é execução concluída.
- Manter a migração `V1__create_performance_indexes.sql` imutável; toda alteração de índices aprovada (`idx_contas_usuario_id`, `idx_categorias_usuario_id`, eventual remoção de redundância `DW-161` e decisões de `DW-155`) deve ocorrer via migração incremental Flyway (`V2+`). Nas 4 tabelas de movimentações, não criar índice simples duplicado em `(usuario_id)`, pois `usuario_id` já lidera os índices compostos da `V1`.
- Nas demais frentes de desempenho (`CAP-7`, `CAP-9` e FKs de `CAP-8`), investigações não autorizam automaticamente otimização ou mudança arquitetural antes de medição e decisão; `DW-161` conserva a pré-condição de confirmação no catálogo e preservação de `UNIQUE`.
- Na observabilidade (`CAP-10`), usar SLF4J/Logback nativo do Spring Boot sem stack externa, limpando o `MDC` em `finally` e proibindo logar JWT, cookies, dados pessoais de perfil ou valores/descrições financeiras privadas.
- Preservar OAuth legítimo, CSRF, cookies de sessão, isolamento por usuário, contratos 403/404 e resultados financeiros históricos. Requisitos verificáveis e pontos de intervenção ficam nos companions.
- Não impor refresh token, tabela de sessões, alteração do fallback `public`, JOIN FETCH, paginação ou SQL agregado antes das respectivas investigações/decisões.
- Toda história derivada declara suas capacidades, DW e action items; atualiza os registros correspondentes com data e evidência após testes/revisão e **antes de ser marcada `done`**, conforme o protocolo de `rastreabilidade.md`.
- Atendimento parcial mantém o registro aberto, com pendências explícitas. Investigação concluída não equivale a correção implementada; A10/A13 só encerram com todo o escopo atendido e verificado ou decisão explícita de mudança de escopo.
- Esta entrega produz apenas especificação e ajusta o nome da branch de planejamento. Não executa código, testes de aplicação, medições ou migrações nem altera estados de execução.

## Non-goals

- Editar migrações Flyway já aplicadas (`V1`) ou adicionar stack externa de observabilidade/telemetria (Prometheus, Grafana, ELK, OpenTelemetry Collector).
- Renovação automática de sessão e implementação de mecanismos de revogação sem decisão posterior.
- Reabrir DW-63, DW-27, DW-28 ou DW-92; criar tarefa independente para DW-38; tratar descartes ou consolidações como correções entregues.
- Funcionalidades/consistência fora do corte: limite monetário D150, filtro mensal D125 e histórico/vigência D106/D156.
- Refinamentos visuais, avatar/perfil, acessibilidade, reorganização de modelos, DRY e refatorações sem benefício concreto de segurança/desempenho/observabilidade.
- Testes genéricos/exaustivos de UI ou DTOs, suíte E2E, CI, cobertura/lint e desempenho de ferramentas de desenvolvimento; não alegar cobertura integral ou conformidade de acessibilidade.
- Criar histórias de execução, alterar o sprint ou implementar o Épico 7 nesta etapa.

## Success signal

- Na execução futura, os cenários de segurança e observabilidade (`MDC` e logs sem vazamento sensível) passam com evidência revisada, a migração Flyway V2 garante índices de `usuario_id` em `contas` e `categorias`, as investigações apresentam resultados reproduzíveis e decisões rastreáveis, e qualquer remoção de índice satisfaz sua pré-condição. Nenhuma história chega a `done` sem atualizar seus DW/action items; registros parcialmente atendidos continuam abertos.

## Open Questions

- **Q-1 — CAP-1:** Qual allowlist pública completa preserva OAuth/callback e qual política de exposição/autorização do Actuator será aplicada no ambiente de implantação? Resolver antes de restringir rotas; exposição efetiva em produção não foi sondada.
- **Q-2 — CAP-6 / DW-2:** Há necessidade de revogar JWT antes da expiração e qual contrato/custo sustenta a decisão? É objeto da investigação aprovada; renovação automática segue excluída.
- **Q-3 — CAP-4 / DW-160:** Quais operações de bootstrap precisam ocorrer sem tenant e o que a investigação de acesso/persistência sem contexto demonstra? Resolver antes de mudar fallback ou configuração.
- **Q-4 — CAP-7/8/9:** Quais ambiente, volumes/carga de referência e critério de custo relevante serão usados nas investigações de consultas e FKs? Definir no início das investigações; não há benchmark ou meta numérica aprovados que permitam decidir essas otimizações agora.
