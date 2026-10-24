# Story 7.4 — evidências de migração e avaliação de índices

Data: 2026-10-24. Escopo: CAP-8, PERF-01–03 e PERF-07–11; DW-155, DW-161 e A13.

Revisão-base: `dc416175d794645372abf9331201868e76ab6a90`, com as alterações locais desta implementação. Os SHA-256 do script, V1 e V2 medidos constam de `sha256_fontes` no [registro da coleta](7-4-index-evidence/measurement.json). Os patches das três camadas de revisão foram aplicados e verificados; a conferência final pelo agente principal está pendente. Este relatório não a substitui.

## Ambiente e procedimento reproduzível

PostgreSQL 16.15, imagem `postgres:16-alpine`, digest `sha256:721873c34ceb9f8d8fc265984940dc982404c105f19ad51be9fdc5970a6080ea`; Linux Alpine x86_64 sobre Docker Desktop 29.8.2, host Windows. `shared_buffers=128MB`, `work_mem=4MB`, `random_page_cost=4`, `effective_cache_size=4GB`, `jit=on`. Testes com Eclipse Adoptium, `java.runtime.version=21.0.11+10-LTS`, Spring Boot 4.1.1 e Hibernate 7.4.5.Final; fornecedor e versão confirmados nos XMLs Failsafe.

Na raiz do repositório:

```powershell
uv run --no-cache backend-api/scripts/evaluate-indexes.py --image 'postgres@sha256:721873c34ceb9f8d8fc265984940dc982404c105f19ad51be9fdc5970a6080ea'
```

O [script](../../backend-api/scripts/evaluate-indexes.py) inicia exclusivamente um contêiner novo com nome aleatório, sem volumes nem URL de banco externo, aplica V1, carrega dados sintéticos, cria os dois índices obrigatórios, altera um candidato por vez e restaura-o antes do próximo. Ao final aplica V2 em transação e remove o contêiner em `finally`. `--image` controla tanto o Docker run quanto a coleta de metadados; `imagem_referencia` registra o argumento efetivo. Não houve acesso a banco com dados reais.

A coleta publicada é preservada durante toda a reprodução. Protocolo e progresso são gravados em um arquivo privado `measurement.json.<aleatório>.pending`, no mesmo diretório, e somente um resultado completo é publicado por substituição atômica. O provisório é removido ao falhar. Uma execução com imagem deliberadamente inválida confirmou que a coleta publicada permaneceu byte a byte idêntica; uma reprodução com falha inicial não apaga a evidência anterior.

O protocolo é persistido no provisório **antes de subir o banco ou medir** e publicado em `protocolo_definido_antes_da_coleta` após sucesso:

- 40.000 registros por movimentação, quatro tabelas (160.000 no total); 100 tenants. `tenant-000` possui 16.000 registros (40%); os demais possuem 242/243. `distribuicao_observada` registra a distribuição efetiva.
- Cada tabela usa uma faixa distinta de IDs dos pais, com quatro contas/categorias vinculadas por tenant e uma sem vínculos. O pai presente de uma tabela não tem vínculos nas outras três; uma exclusão ainda precisa conferir as quatro FKs do mesmo tipo.
- Datas distribuídas por `2016-01-01 + (g*17 mod 3650)` dias; fixas com 5% de início nulo; variáveis com fim 330 dias após início. Tenant seletivo: cerca de 0,606%; vínculo seletivo: cerca de 0,152%; vínculo ausente: 0%. Há também leitura do tenant de 40%.
- Um aquecimento e cinco amostras por operação/estado. `VACUUM FULL` e `ANALYZE` antes de cada estado; leituras de listagem/período antes de escritas; INSERT/UPDATE de 1.000 linhas e DELETE sempre em transação com rollback. As sondagens/FKs, feitas após as escritas, incluem o efeito dos registros abortados de forma igual nos dois estados.
- O UPDATE rotaciona cada FK para outro dos quatro pais do tenant da própria linha, nos IDs 20001–21000. O controle `validacao_update_titularidade` confere 1.000 alvos e zero vínculos com proprietário diferente por tabela antes das medições. O lote sintético atravessa vários tenants, preservando a titularidade individual; não atribui pais de um tenant único a todos os registros.
- Critério prévio: sinal relevante é reduzir pelo menos 50% dos blocos **e** 1 ms por operação, ou 10% do espaço/escrita. Adicionar FK também exige frequência/volume real que justifique manutenção contínua; remover temporal exige equivalência do trabalho real e ausência de acesso distinto demonstrado. O critério orienta investigação, não cria SLA de produção.

Cada operação guarda SQL, um `EXPLAIN (ANALYZE, BUFFERS, WAL, FORMAT JSON)` representativo e cinco amostras de tempo, buffers, WAL e triggers. Os tempos nas tabelas abaixo são medianas de execução do servidor, em ms; blocos são `Shared Hit Blocks + Shared Read Blocks`. Não são latências HTTP ou da consolidação completa em Java. O DELETE com vínculo é rejeitado por `23503`: não produz plano completo de execução; seu custo é medido no servidor por `clock_timestamp()` com captura da exceção em subtransação. O script exige `23503` inclusive no aquecimento e interrompe a coleta para qualquer SQLSTATE diferente; o contrato também foi conferido com `00000`, `23505` e estado ausente. O DELETE sem vínculo possui plano completo e tempo de cada trigger.

## SQL revisado e limites da comparação

As quatro interfaces de repositório oferecem `findAllByOrderByDescricaoAsc()` e `findByUsuarioId()`. As formas SQL avaliadas para listagem e leitura da consolidação correspondem a esses predicados, com aliases/projeção simplificados; não representam captura de todo o tráfego Hibernate nem medem materialização de DTO/N+1. `ConsolidacaoService` agrega datas em memória, sem filtro SQL temporal. `ContaService.excluir` e `CategoriaService.excluir` fazem `existsById`, `deleteById` e `flush`; o DELETE avaliado é a etapa efetiva que aciona a integridade referencial, não o custo completo do serviço.

Não existem `existsByConta`/`existsByCategoria` no código revisado. Por isso `SELECT 1 ... WHERE conta_id/categoria_id = ... LIMIT 1` está marcado **hipotético**, separado dos DELETEs reais. As consultas de período global e período com tenant também são sondagens hipotéticas. Elas servem para investigar caminhos de acesso distintos; não alegam consumidores atuais.

Amostra de desenvolvimento, cache aquecido, sem concorrência nem frequência real de operações. As repetições reutilizam o estado físico após rollback dentro do mesmo estado; há variação de cache, WAL/checkpoint e tempo de escrita. Os custos não autorizam extrapolar ganhos de produção nem concluir que uma variação pequena de tempo foi causada pelo índice. O JSON preserva todas as amostras, os planos e as dependências para auditoria.

## Oito candidatos de FK

Todos estavam **ausentes** na V1; as oito constraints estavam presentes, validadas e independentes desses candidatos. Cada alteração experimental adiciona apenas um B-tree simples, sem tenant, permitindo à checagem referencial por ID usá-lo. Todos os oito ocuparam 288 KiB quando construídos sobre as 40.000 linhas (`catalogo_depois` do candidato). A configuração entregue mantém os oito ausentes.

| Candidato experimental | Sondagem ausente ms; blocos antes → depois | DELETE sem vínculos ms | DELETE rejeitado ms | INSERT 1.000 ms | UPDATE 1.000 ms |
|---|---|---|---|---|---|
| `idx_despesas_fixas_conta_id` | 3,248; 862 → 0,063; 5 | 14,937 → 11,869 | 4,183 → 3,205 | 34,628 → 34,337 | 38,716 → 40,624 |
| `idx_despesas_fixas_categoria_id` | 3,419; 862 → 0,058; 5 | 14,983 → 12,275 | 4,230 → 3,203 | 34,575 → 32,984 | 38,808 → 41,129 |
| `idx_receitas_fixas_conta_id` | 3,199; 862 → 0,055; 5 | 15,904 → 12,121 | 7,816 → 6,871 | 34,171 → 33,213 | 40,453 → 40,439 |
| `idx_receitas_fixas_categoria_id` | 3,362; 862 → 0,056; 5 | 14,498 → 11,581 | 7,790 → 6,148 | 34,861 → 33,433 | 39,595 → 40,705 |
| `idx_despesas_variaveis_conta_id` | 3,888; 959 → 0,060; 5 | 14,712 → 11,088 | 11,660 → 10,599 | 33,461 → 33,713 | 40,256 → 43,305 |
| `idx_despesas_variaveis_categoria_id` | 4,000; 959 → 0,067; 5 | 14,901 → 11,970 | 11,526 → 10,030 | 32,276 → 33,657 | 39,440 → 43,401 |
| `idx_receitas_variaveis_conta_id` | 3,578; 911 → 0,053; 5 | 13,925 → 10,543 | 14,083 → 13,266 | 33,027 → 33,520 | 39,630 → 42,618 |
| `idx_receitas_variaveis_categoria_id` | 3,746; 911 → 0,055; 5 | 14,380 → 11,024 | 14,198 → 13,284 | 32,079 → 33,335 | 39,096 → 43,154 |

**Recomendação por candidato:** para cada uma das oito linhas, preservar a ausência na V2 e condicionar uma futura adição ao perfil real de exclusões/volume e orçamento de escrita. O plano da sondagem ausente muda de `Limit → Seq Scan` para `Limit → Index Only Scan`; presente também melhora (1,230–1,570 ms → 0,073–0,081 ms). O sinal de leitura é relevante, mas a aplicação não executa essas sondagens. No DELETE real sem vínculos, indexar apenas uma das quatro tabelas reduz aproximadamente 18–25% do tempo total; as demais checagens continuam varrendo tabelas. O DELETE rejeitado tem trabalho diferente conforme a ordem dos triggers e não melhora consistentemente.

Há custo de manutenção e espaço: oito índices somariam cerca de 2,25 MiB nesta carga. Os pares de escrita acima e as cinco amostras no JSON mostram custos de manutenção e ruído; alguns tempos caem mesmo adicionando índice, impedindo atribuição causal simples. As medianas desta coleta substituem a rodada anterior, cujo UPDATE concentrava pais de outro tenant; somente a carga corrigida fundamenta os números apresentados. Não foi medida a combinação dos oito índices, nem a frequência de exclusões. Sua possível adição em carga maior é recomendação condicionada, não correção implementada. Isto mantém explícita a parte histórica de A13 que pedia adição de FKs.

## Quatro índices temporais sem tenant

Todos eram B-trees válidos, sem constraint/dependentes impeditivos, com definições distintas dos compostos por tenant. Cada experimento abaixo remove somente o índice da linha; os compostos permanecem. Espaço por índice: 384 KiB após reconstrução sobre as linhas vivas (`catalogo_pre_v2`, evitando confundir bloat das escritas abortadas com tamanho do índice). A configuração entregue preserva os quatro.

| Índice | Período global ms; blocos antes → sem índice | Listagem tenant ms | Consolidação/leitura tenant maior ms | INSERT 1.000 ms | UPDATE 1.000 ms |
|---|---|---|---|---|---|
| `idx_despesas_fixas_data_inicio` | 0,570; 190 → 3,293; 690 | 0,617 → 0,667 | 3,856 → 3,978 | 33,794 → 31,149 | 39,005 → 41,596 |
| `idx_receitas_fixas_data_inicio` | 0,597; 190 → 3,542; 690 | 0,659 → 0,628 | 3,885 → 4,097 | 34,669 → 32,556 | 40,111 → 39,285 |
| `idx_despesas_variaveis_datas` | 0,614; 190 → 3,580; 770 | 0,671 → 0,671 | 4,259 → 4,147 | 34,518 → 32,511 | 40,817 → 39,446 |
| `idx_receitas_variaveis_datas` | 0,615; 191 → 3,450; 728 | 0,657 → 0,663 | 4,210 → 4,072 | 32,496 → 32,403 | 40,134 → 37,864 |

**Recomendação por candidato:** preservar cada um dos quatro. A consulta global muda de `Bitmap Heap Scan → Bitmap Index Scan` para `Seq Scan`; portanto o índice temporal não é equivalente ao composto cujo primeiro campo é tenant. A consulta hipotética com tenant e período continua atendida pelo composto (medianas de 0,051–0,056 ms antes e 0,051–0,060 ms depois, conforme a linha). Há potencial economia de 1,5 MiB e manutenção, especialmente em despesas variáveis, mas ausência de consumidor global no código revisado não comprova redundância no banco alvo nem benefício suficiente da remoção. Não se remove nenhum mapeamento JPA temporal.

## Catálogo da V2, integridade e alinhamento JPA

`catalogo_v1`, `catalogo_pre_v2` e `catalogo_pos_v2` registram definição, validade/prontidão, unicidade, constraints, dependências nos dois sentidos e bytes. A aplicação da V2 no mesmo descartável preservou os quatro compostos e os quatro temporais; os únicos índices novos entregues são `idx_contas_usuario_id` e `idx_categorias_usuario_id`. As seis tabelas possuem índice válido liderado por `usuario_id`; nenhum simples foi duplicado nas quatro movimentações.

O inventário próprio de `pg_constraint`, em `fks_v1`, `fks_pre_v2`, `fks_pos_v2` e nos pares `fks_antes`/`fks_depois` de cada candidato, registra as oito FKs: tabela/nome, colunas de origem, tabela/colunas referenciadas, definição, ações UPDATE/DELETE, match, validade e adiamento. São `conta_id → contas(id)` e `categoria_id → categorias(id)` para cada uma das quatro movimentações. As oito têm ações `a` (NO ACTION), match `s` (SIMPLE), validade verdadeira e não são adiáveis. A coleta compara integralmente os inventários e falha se qualquer definição/estado mudar. O teste de upgrade também compara as oito definições e validades da V1 após V2 e após bootstrap JPA; a contagem isolada não é usada como prova de preservação.

Em `usuarios`, antes: `usuarios_google_subject_id_key` é UNIQUE válida/pronta, com constraint de mesmo nome, B-tree sobre `google_subject_id`; `idx_usuarios_google_subject_id` é um secundário B-tree sobre a mesma coluna, válido/pronto, sem constraint/dependentes, com somente dependência automática da coluna. Depois: o secundário não existe; a UNIQUE permanece. Não se promete aceleração de leitura.

A V2 repete a comprovação no próprio banco alvo: tabela/esquema, coluna única, ausência de expressão/predicado/INCLUDE, método, opclass, collation, ordenação, validade/prontidão, UNIQUE imediata/validada e dependências. Recusa índices de constraint, cluster, identidade de replicação ou dependências inesperadas. Na ausência de comprovação, preserva catálogo e emite NOTICE para inspeção; isso não declara a correção entregue naquele alvo. Os índices de tenant obrigatórios precisam ter a mesma collation da coluna. Os quatro compostos existentes também são conferidos quanto a definição, prefixo, collation e validade; nenhum é criado/substituído. Um índice obrigatório divergente/inválido, ou composto ausente/inutilizável, faz o upgrade falhar com rollback e Flyway ainda na V1. Os testes selecionam uma collation diferente da coluna no contêiner e comprovam a divergência no catálogo antes da tentativa de migração.

`Usuario` perde somente a declaração do secundário em `@Table`; `@Column(unique=true, nullable=false)` permanece. O bootstrap real verifica que `ddl-auto=update` não recria o secundário e existe exatamente um índice UNIQUE válido de `google_subject_id`.

## Verificação e rastreabilidade

A execução inicialmente vermelha de `IndexMigrationIT` teve 11 falhas esperadas pela ausência da V2/índices obrigatórios, sem erro de infraestrutura. Os 17 cenários anteriores foram preservados; a suíte agora possui 33 cenários. O banco pertence à classe e cada cenário usa esquema aleatório próprio, isolado de `AbstractIntegrationTest`.

O [registro da verificação](7-4-index-evidence/verification.json) preserva o resultado extraído dos XMLs Failsafe, os 33 nomes/casos de migração realmente executados, totais e hashes das fontes verificadas. As três linhas da matriz da spec correspondem aos testes de base limpa/upgrade, remoção comprovada com bootstrap e preservação dos 13 catálogos divergentes mais dependência de extensão.

Matriz coberta: base limpa e V1 existente, reaplicação sem pendências, checksum/histórico, seis tabelas e ausência de duplicação, valores/datas/vínculos preservados, rejeição de duplicatas e FKs, bootstrap JPA; preservação do secundário ausente/diferente/parcial/com INCLUDE/constraint/expressão/opclass/collation/cluster/dependência de extensão, UNIQUE ausente/inválida e secundário inválido; rollback quando um índice obrigatório preexistente diverge; índices inválidos/collation divergente em contas/categorias e compostos ausentes/inválidos/com collation divergente em cada uma das quatro movimentações. Em todos os 16 cenários novos, conferem-se exceção, catálogo/FKs, checksum/histórico e Flyway ainda na V1. Quantidade de parcelas e data_fim também são verificadas antes/depois da migração e JPA.

Em `backend-api/`, com `$env:JAVA_HOME='C:\Program Files\Java\jdk-21'`:

| Comando | Resultado em 2026-10-24 |
|---|---|
| `./mvnw.cmd verify -Pintegration-test -Dit.test=IndexMigrationIT` | BUILD SUCCESS; 219 unitários e 33 cenários de migração, zero falhas/erros/skips; 44,443 s |
| `./mvnw.cmd verify -Pintegration-test` | BUILD SUCCESS; 219 unitários e 70 integrações (incluindo os 33), zero falhas/erros/skips; 45,301 s |
| Na raiz: `git diff --check` | Sem erros |
| `git diff dc416175d794645372abf9331201868e76ab6a90 --exit-code -- backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql` | Sem diferença |

O blob Git da V1 continua `804b92a07f01c2fd9462c854836981ccd224d92c`. O arquivo do checkout Windows e a cópia compilada têm SHA-256 `d3084565f56325b7b22faabb99bd8759dc14a1924203065647c03fa91c8d9369`; o checkout conserva seu CRLF original, e o Git conserva o blob LF original. Nenhuma escrita foi feita na V1. `IndexMigrationIT` compara o checksum efetivamente registrado pelo Flyway antes/depois do upgrade, além de `validate` e reaplicação sem pendências.

A revisão em três camadas e a conferência final dos patches pelo agente principal foram concluídas em 2026-10-24. Os hashes das fontes e os XMLs finais confirmam 219 unitários e 70 integrações aprovados. DW-155 e DW-161 foram encerrados no recorte de implementação/investigação aprovado; A13 permanece aberto pela adição histórica dos índices de FK, ainda condicionada ao perfil real de uso. Nenhuma execução em banco real foi realizada.
