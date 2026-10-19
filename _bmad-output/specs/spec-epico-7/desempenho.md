# Investigações e condições de desempenho

Companion de `SPEC.md`, derivado das decisões aprovadas. Nenhuma medição foi executada na triagem ou nesta especificação; não há ganho de latência comprovado nem meta numérica aprovada.

## Evidência e decisão

- **PERF-01:** Cada investigação registra revisão do código, ambiente/configuração, volume e distribuição dos dados por usuário, operação medida, comandos/procedimento, métricas/rastros e limites da amostra. Definir carga de referência e critério de custo relevante antes da decisão de otimizar (Q-4).
- **PERF-02:** Concluir com resultado e recomendação sustentados pelos dados, inclusive quando não houver custo relevante demonstrado. Isso conclui o escopo investigativo atendido; não prova implementação nem autoriza encerrar compromissos de correção pendentes.
- **PERF-03:** Uma mudança derivada de investigação exige evidência e decisão de implementação registradas. Havendo implementação futura aprovada, comparar antes/depois sob condições comparáveis e verificar equivalência funcional/isolamento. Não impor paginação, SQL agregado, JOIN FETCH ou novos índices como resultado antecipado.

## CAP-7 — Consolidação (DW-139 / A10)

**Evidência atual:** `ConsolidacaoService.java` carrega conjuntos de movimentações por usuário e agrega em memória. A existência dessa leitura integral não mede seu custo. O saldo histórico usa período anterior à grade.

- **PERF-04:** Medir tempo, memória, número de consultas e volume carregado da consolidação por conta/categoria e do resumo/saldo histórico, com volume e distribuição temporal identificados.
- **PERF-05:** Avaliar alternativas somente se o custo justificar. SQL agregado e corte por período são candidatos, não soluções aprovadas.
- **PERF-06:** Exigir equivalência dos totais mensais, precisão monetária e saldo histórico pré-grade. A janela de 24 meses não autoriza descartar transações anteriores necessárias ao acumulado; preservar isolamento por usuário.

**Entrega aprovada:** relatório de investigação e encaminhamento. A10 mantém o compromisso original de otimização; relatório/recomendação não o encerra automaticamente. A ausência de justificativa para otimizar precisa de decisão explícita sobre eventual mudança de escopo, conforme `rastreabilidade.md`.

## CAP-8 — Índices (DW-155, DW-161 / A13)

### DW-155 — Índices obrigatórios de tenant (`usuario_id`) e investigação de FKs

**Evidência atual:** o `TenantContext` injeta `usuario_id` em todas as consultas das 6 tabelas multi-tenant. Em `V1__create_performance_indexes.sql`, as 4 tabelas de movimentações (`despesas_fixas`, `receitas_fixas`, `despesas_variaveis`, `receitas_variaveis`) já possuem `usuario_id` como primeira coluna de índices compostos, mas `contas` e `categorias` não possuem nenhum índice em `usuario_id`.

- **PERF-07:** Criar obrigatoriamente na migração incremental Flyway V2 (sem editar a `V1` já aplicada) os índices `idx_contas_usuario_id` em `contas(usuario_id)` e `idx_categorias_usuario_id` em `categorias(usuario_id)`. Não criar índice simples duplicado em `(usuario_id)` nas 4 tabelas de movimentações, pois o prefixo esquerdo dos índices compostos da `V1` já cobre filtros por `usuario_id`.
- **PERF-08:** Conferir catálogo, consultas, planos e volume para as FKs (`conta_id`, `categoria_id`) nas tabelas de movimentações e avaliar a utilidade/redundância dos 4 índices de data da `V1` que não possuem `usuario_id` (`idx_despesas_fixas_data_inicio`, `idx_receitas_fixas_data_inicio`, `idx_despesas_variaveis_datas`, `idx_receitas_variaveis_datas`). Apresentar recomendação fundamentada por candidato para essas FKs e índices secundários.

### DW-161 — Remoção condicionada de redundância

**Evidência atual:** a V1 declara `usuarios.google_subject_id` como UNIQUE e também cria `idx_usuarios_google_subject_id`. A duplicação efetiva no banco alvo ainda deve ser conferida.

- **PERF-09:** Antes de qualquer remoção, inspecionar catálogo, definição dos índices e dependências. Confirmar que o secundário é efetivamente redundante e identificar o índice/constraint que garante UNIQUE.
- **PERF-10:** Somente com a pré-condição comprovada, planejar/executar futuramente a remoção do secundário redundante e demonstrar preservação da unicidade e integridade. Registrar catálogo antes/depois e verificação de rejeição de duplicatas; não prometer aceleração de leitura.
- **PERF-11:** Se a redundância não for confirmada ou o índice sustentar uma constraint, não remover. Registrar evidência e encaminhamento; não marcar a correção como implementada nem encerrar o registro por presunção de inaplicabilidade.

**Entrega planejada e condicional:** a migração incremental Flyway V2 (mantendo a `V1` imutável) entregará os índices obrigatórios `idx_contas_usuario_id` e `idx_categorias_usuario_id`, além das alterações de índices que tiverem sua condição/evidência confirmada em `DW-161` e `DW-155`. Não rodar ou criar a migração na etapa de planejamento.

**A13:** cobre DW-155 e DW-161. A criação dos índices de `usuario_id` em `contas` e `categorias` é parte obrigatória da entrega, mas encerrar `DW-155` e `A13` exige também concluir a avaliação das FKs e a conferência de `DW-161`.

## CAP-9 — Listagens e consultas extras

| Origem | Investigação e critério de aceite | Condição para mudança futura |
|---|---|---|
| **PERF-12 — DW-117** | Medir volume, bytes das respostas e latência das listagens de Contas, Categorias e movimentações; registrar necessidade demonstrada ou não de paginação. | Preservar isolamento e acesso a todas as opções nos seletores dos formulários; não truncar opções ao paginar listas. |
| **PERF-13 — DW-147** | Contar SQL real na listagem/conversão de DTOs em volumes distintos e relações compartilhadas/distintas; identificar se existe N+1 e sua magnitude. | Escolher estratégia de carregamento somente com evidência, preservando conteúdo/contrato dos DTOs e isolamento. |
| **PERF-14 — DW-153** | Medir roundtrips e custo de consulta de proprietário seguida de `findById`, com prioridade menor que as demais investigações aprovadas. | Preservar checagem de titularidade e contratos 403/404; nenhuma redução de consultas pode liberar vínculo de outro usuário. |

**Refinamento obrigatório de DW-147:** `DespesaVariavelResponseDTO.fromEntity` delega a `ContaResponseDTO`/`CategoriaResponseDTO`, que leem descrição/observações das relações LAZY. O caminho não se limita a acessar ID de proxy. A magnitude de N+1 permanece sem medição; nem LAZY nem essa leitura, sozinhos, comprovam um gargalo.

## Dependências e limites

- A medição precede a escolha da otimização; decisões de consulta e índices devem considerar os mesmos padrões de acesso quando relacionados. Isso não transforma os registros em tarefas duplicadas.
- A conferência específica de DW-161 é obrigatória mesmo que DW-155 tenha produzido recomendação favorável; A13 depende do atendimento integral dos dois recortes.
- Preservar saldo histórico não autoriza corrigir a política de histórico/vigência D106/D156, descartada deste corte.
- DW-27 (login), DW-28 (`/me`) e DW-92 (cancelamento) permanecem descartados. `save()` não prova UPDATE incondicional, `/me` foi identificado na inicialização sem polling, e cancelamento no cliente não garante interrupção de trabalho no servidor.

Referências de execução na raiz do repositório: `backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/{ConsolidacaoService,DespesaVariavelService,ContaService,CategoriaService}.java`, DTOs em `domain/dto/` no mesmo pacote base e `backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql`.

Cada história aplica também o protocolo de atualização/encerramento de `rastreabilidade.md`.
