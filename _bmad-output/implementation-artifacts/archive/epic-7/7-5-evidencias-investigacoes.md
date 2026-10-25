# Story 7.5 — Evidências de segurança e desempenho

## Protocolo registrado antes da coleta — 2026-10-25

Revisão de referência: `c389e76e518d66099fef384448ec27b7d1800233`. Escopo exclusivamente investigativo: CAP-4/6/7/9, SEG-12/13/17/18 e PERF-01–06/12–14; DW-2/160/139/117/147/153, DW-38 consolidado em DW-160. A10 permanece aberto; os demais não têm action item associado. Nenhuma mudança de produção está autorizada.

Q-4: JDK 21 e PostgreSQL 16 descartável, em banco exclusivo dos testes investigativos. Três tenants (`invest-a`, `invest-b`, `public`), sem dados reais. Por tenant, 100/1.000/10.000 movimentos **em cada uma das quatro famílias** e 10/100/1.000 contas e categorias. Fixture determinística de 2021–2028, incluindo fixos, parcelas que cruzam ambas as bordas da janela 2026-01–2027-12, valores em centavos e campos opcionais. Os vínculos de conta/categoria são obrigatórios no domínio e no schema vigente; não se relaxará NOT NULL para produzir vínculos nulos artificiais. A referência financeira será calculada em centavos, mês a mês, independentemente do serviço de produção.

Sequência: consolidação por conta (incluindo saldo anterior à grade) e categoria; seis listagens completas e serializadas; DTOs LAZY com 10/100/1.000 movimentos e pares compartilhados/distintos; por último, validação de titularidade isolada e criação revertida. Cada operação/carga terá uma amostra fria, três aquecimentos e dez amostras, sempre com sessões novas. Fixture, emissão do cookie de teste e validações ficam fora do intervalo medido; abertura/fechamento da sessão, transação, serialização e instrumentação SQL ficam dentro. A amostra fria significa primeira execução daquela operação/carga, não cache de disco do PostgreSQL limpo.

Métricas: nanossegundos e milissegundos, bytes UTF-8, statements preparados e SQL normalizado sem valores, entidades carregadas, bytes alocados pela thread de execução e heap usado observado antes/depois. Mediana e máximo das dez amostras; a fria permanece separada. Não se chamará heap observado de pico nem tempo MockMvc de latência de rede. Amostras sequenciais, sem paralelismo; não extrapolar para concorrência ou produção.

Sinais exploratórios pré-definidos: mediana ≥200 ms, resposta ≥1 MiB, alocação ≥32 MiB ou SQL crescendo com o número de relações distintas. Não são SLOs nem autorização de otimizar. Titularidade será medida separadamente e como razão perante a criação revertida, preservando 403/404 e rejeição sem mutação; não compara criação persistida nem mede COMMIT durável.

Segurança: replay de cópia em memória após logout com CSRF, token expirado, logout não entregue (modelo de falha de rede); contexto nulo/vazio/branco e tenants A/B/public; consultas HQL, ID, consulta nativa de proprietário, escrita e sessões novas/existentes; resolver efetivo da SessionFactory, Flyway/JPA reais e OAuth sintético sem tenant. Todos os contextos/sessões serão limpos em `finally`. Resultados internos serão separados dos endpoints HTTP. Nenhum JWT, segredo, cookie ou parâmetro SQL será persistido.

Publicação refinada pelos patches do step-04: a execução comum grava `backend-api/target/7-5-investigation-evidence/measurement.json`. Arquivar em `7-5-investigation-evidence/measurement.json` exige a flag explícita `-Dinvestigation.publish=true`. Ambas as suítes devem executar na mesma JVM, com `-DforkCount=1 -DreuseForks=true`, sem paralelismo JUnit. O estado pertence ao Store raiz de cada execução JUnit, possui `runId` próprio e é descartado no fim/falha; coleta incompleta falha explicitamente. Toda serialização, validação e staging antecedem o único move atômico do JSON, sem sidecar ou I/O posterior no publicador. O SHA é registrado posteriormente em `verification.json`. Regressões normais preservam o JSON arquivado, relatório e verificação anteriores. Ambiente, digest, revisão Git completa, hashes das fontes e comandos integram cada nova coleta.

## Resultados

Após os nove patches do step-04, a coleta focada autorizada passou: **225 testes unitários e 2 investigativos**, sem falhas ou skips. A regressão completa sem publicação passou: **225 unitários + 72 integrações**, sem falhas, erros ou skips; gerou uma coleta nova em target e preservou exatamente o JSON arquivado. Os **3 testes existentes do Header** já foram aprovados e suas fontes permanecem idênticas. A revisão independente foi concluída em três camadas; a conferência final dos nove patches pelo agente principal foi concluída em 2026-10-25. Este registro representa investigação, sem correção de produção e sem encerramento de A10.

### Observações de segurança — SEG-12/13/17/18

`SecurityInvestigationIT#caracterizaSessaoFallbackBootstrapEOAuthSemPublicarCredenciais` executa os cenários abaixo no PostgreSQL real descartável. O token copiado fica em memória e não é publicado.

| Caminho observado | Resultado demonstrado | Interpretação e limite |
|---|---|---|
| Logout HTTP com CSRF; replay da mesma cópia | Logout 200 e cookie vazio com Max-Age=0; replay 200. Sem CSRF, logout 403. Token expirado: 401 e limpeza do cookie. | Logout remove o cookie da resposta; não revoga uma cópia. TTL configurado de 86.400.000 ms, logo a janela residual pode se aproximar de 24 h após emissão. Não houve uso de token real nem demonstração de como roubá-lo. |
| Logout não entregue | A cópia continua aceita, 200. | Modelo explícito de ausência de entrega de POST; não é falha de rede medida pelo MockMvc. A limpeza local é verificada separadamente pelos testes existentes do Header. |
| Sessão nova, contexto nulo/vazio/branco | Tenant efetivo `public`; HQL retorna somente `public`; HQL com proprietário A explícito não retorna A; `find` por ID de A retorna ausente. | Não se demonstrou leitura financeira de A por fallback. É um caminho interno, sem autenticação HTTP envolvida. |
| Persistência interna sem tenant | `persist` + `flush` insere efetivamente com proprietário `public`; transação revertida e contagem inicial preservada. | Risco demonstrado de encaminhar uma escrita interna para `public` por ausência de contexto; isso não demonstra um endpoint anônimo capaz de escrever. |
| Sessão já aberta em A; contexto depois nulo/vazio/branco | HQL e escrita continuam em A; uma nova sessão passa a `public`. Todas as escritas são revertidas. | O tenant pertence à sessão aberta. `validateExistingCurrentSessions=true` não revalida cada uso de um EntityManager explícito nem troca seu tenant ao mudar ThreadLocal. |
| Consulta nativa de proprietário | Retorna `invest-a` para o ID de A, inclusive sem contexto; SQL normalizado registrado separadamente. | Esse caminho deliberadamente global fornece a comparação que rejeita vínculos alheios. Não retorna o DTO financeiro nem dispensa a validação do serviço. |
| Bootstrap Flyway/JPA e resolver | V1/V2 aplicadas e validadas, sem pendências; JPA com `ddl-auto=update`; inicialização sem tenant bem-sucedida. Resolver efetivo tem a classe configurada e é instância diferente do bean Spring. | Flyway opera via JDBC; `Usuario` é global. A diferença de identidade não demonstra defeito: o resolver vigente é stateless e não depende de injeção. Não se trocou configuração ou fallback. |
| OAuth sintético sem tenant | Usuário global persistido e redirect 302; snapshots das seis tabelas financeiras idênticos; contexto segue ausente. | Exercita o handler real com principal sintético. Não chama Google nem valida o intercâmbio externo OAuth. |
| Fronteira HTTP | Anônimo 401; A/B/public autenticados recebem somente seu próprio ID. Vínculos alheios 403; conta inexistente e recurso alheio nas mutações 404; snapshots das seis tabelas financeiras idênticos após todas as rejeições. | O fallback interno não ficou exposto por esses caminhos HTTP. A/B/public são identidades sintéticas assinadas apenas para o teste; não representam possibilidade de emitir esses subjects pelo login real. |

**Hipótese / encaminhamento:** a falta de contexto merece uma política futura explícita para operações financeiras internas. Separar bootstrap e usuário global de acessos financeiros antes de propor rejeição antecipada nesse limite; mudar universalmente o resolver para falhar sem tenant não está justificado por este ensaio. Testar o caminho consumidor concreto antes de qualquer alteração. DW-38 continua consolidado em DW-160.

### Método e limites de desempenho — PERF-01–06/12–14

São 32 séries: 8 operações × 3 cargas, 6 combinações do experimento LAZY e 2 operações de titularidade. Cada série executa 14 intervalos (1 fria + 3 aquecimentos + 10 amostras); o JSON preserva a fria e as dez amostras, SQL com frequências, sessões abertas/fechadas e sumários. As três cargas representam 1.200/12.000/120.000 movimentos no banco, respectivamente, mais contas/categorias dos três tenants. A medição principal usa A; B/public e um tenant vazio são verificados funcionalmente fora dos intervalos.

O oráculo enumera competências em **centavos inteiros**, sem chamar serviço ou DTO de produção, e compara cada linha, os totais de cada um dos 24 meses e o saldo histórico anterior a 2026. Inclui fixos sem data inicial (sem histórico anterior no contrato vigente), movimentos de 2021–2028, parcelas atravessando o início e o fim da grade, textos/datas opcionais e tenants com valores diferentes. Listagens e linhas não zeradas de consolidação conferem o conjunto exato de IDs e rejeitam duplicatas. Contas e categorias dos seletores são verificadas integralmente nas três cargas; não se aceita truncamento silencioso. Descrição e observações de **ambas** as relações aparecem nos DTOs verificados.

O JSON inclui JDK, SO, CPUs, heap máximo, PostgreSQL, parâmetros de memória do banco, Docker, imagem/digest, revisão Git canônica completa e hashes SHA-256 de todas as fontes de produção, fontes investigativas, configuração Maven e contextos relevantes. A revisão Git identifica o baseline; os hashes identificam as fontes de teste medidas antes do commit de implementação. `verification.json` registra o SHA-256 do JSON arquivado e a conferência das fontes. Os hashes brutos preservam os bytes medidos; `sourceContentLF` em `verification.json` também registra SHA-256 com CRLF normalizado para LF, conferido contra o índice Git, para verificar o mesmo conteúdo após checkouts com conversão de fim de linha.

Limites: execução sequencial e local, sem usuários reais, concorrência, navegador, rede HTTP, TLS, renderização ou disco frio controlado. A alocação é da thread síncrona, não de todas as threads da JVM; heap antes/depois inclui estado de outras operações/GC e **não é pico**. Tempos e alocações incluem a infraestrutura MockMvc, abertura/transação/fechamento de sessão e a instrumentação SQL: o StatementInspector executa regex e atualiza mapas dentro do intervalo, com custo proporcional às consultas. **Esse overhead não foi quantificado.** Contagens de SQL/entidades e bytes são observações úteis; latência e alocação instrumentadas não são previsões de produção. A primeira operação pode aproveitar caches anteriores da JVM/PostgreSQL. Não há SLO ou ganho implementado. A criação medida faz rollback e não inclui COMMIT durável; sua razão com titularidade não compara criação persistida.

### Métricas da coleta final

Coleta arquivada concluída em `2026-10-25T07:16:30.261262700Z`, pelo comando focado com publicação explícita. RunId `f902fd6f-ce68-433f-b069-df164a36f10e`. Java `21.0.11+10-LTS`, Windows 11 10.0, 16 CPUs; PostgreSQL 16.15, Docker 29.8.2. Imagem `postgres:16-alpine`, image ID `sha256:721873c34ceb9f8d8fc265984940dc982404c105f19ad51be9fdc5970a6080ea`; RepoDigest no JSON. SHA-256 do JSON: `2b5ca159cd4154dd9f8ac8a2d105ede9eda86c566447d2302bbac14b84bdcbf8`.

| Operação | N por família | ms mediana / máximo | SQL | Entidades | Alocação MiB mediana / máximo | Bytes UTF-8 |
|---|---:|---:|---:|---:|---:|---:|
| consolidacao-conta | 100 | 40.24 / 70.00 | 5 | 410 | 1.24 / 1.31 | 4456 |
| consolidacao-categoria | 100 | 16.84 / 22.16 | 3 | 210 | 0.69 / 0.80 | 2178 |
| listagem-contas | 100 | 6.32 / 8.31 | 1 | 10 | 0.20 / 0.23 | 743 |
| listagem-categorias | 100 | 5.86 / 7.42 | 1 | 10 | 0.20 / 0.23 | 743 |
| listagem-despesas-fixas | 100 | 35.69 / 49.17 | 21 | 120 | 0.90 / 0.95 | 28002 |
| listagem-receitas-fixas | 100 | 27.99 / 31.81 | 21 | 120 | 0.90 / 0.90 | 28002 |
| listagem-despesas-variaveis | 100 | 27.54 / 29.21 | 21 | 120 | 1.01 / 1.02 | 36964 |
| listagem-receitas-variaveis | 100 | 26.02 / 27.13 | 21 | 120 | 0.99 / 1.00 | 33264 |
| consolidacao-conta | 1000 | 38.45 / 44.75 | 5 | 4100 | 8.59 / 8.73 | 44805 |
| consolidacao-categoria | 1000 | 18.67 / 22.13 | 3 | 2100 | 4.21 / 4.22 | 22622 |
| listagem-contas | 1000 | 3.82 / 4.49 | 1 | 100 | 0.28 / 0.28 | 7585 |
| listagem-categorias | 1000 | 3.76 / 5.73 | 1 | 100 | 0.28 / 0.28 | 7585 |
| listagem-despesas-fixas | 1000 | 170.14 / 177.28 | 201 | 1200 | 7.47 / 7.51 | 284119 |
| listagem-receitas-fixas | 1000 | 155.02 / 185.17 | 201 | 1200 | 7.40 / 7.43 | 284119 |
| listagem-despesas-variaveis | 1000 | 148.27 / 151.88 | 201 | 1200 | 7.89 / 7.89 | 373803 |
| listagem-receitas-variaveis | 1000 | 145.86 / 152.79 | 201 | 1200 | 7.82 / 7.82 | 336803 |
| consolidacao-conta | 10000 | 263.55 / 337.37 | 5 | 41000 | 82.20 / 82.21 | 443219 |
| consolidacao-categoria | 10000 | 89.36 / 95.59 | 3 | 21000 | 39.28 / 39.28 | 218277 |
| listagem-contas | 10000 | 6.49 / 10.09 | 1 | 1000 | 1.26 / 1.26 | 77787 |
| listagem-categorias | 10000 | 5.81 / 7.07 | 1 | 1000 | 1.26 / 1.26 | 77787 |
| listagem-despesas-fixas | 10000 | 1453.72 / 1496.75 | 2001 | 12000 | 70.77 / 70.77 | 2889920 |
| listagem-receitas-fixas | 10000 | 1469.35 / 1532.46 | 2001 | 12000 | 70.77 / 70.77 | 2889920 |
| listagem-despesas-variaveis | 10000 | 1498.10 / 1585.49 | 2001 | 12000 | 75.53 / 75.53 | 3786812 |
| listagem-receitas-variaveis | 10000 | 1482.53 / 1508.57 | 2001 | 12000 | 74.87 / 74.87 | 3416812 |

Experimento controlado LAZY (mesmas cardinalidades de referências; varia o número de pares usados):

| N | Pares usados | ms mediana / máximo | SQL | Entidades carregadas |
|---:|---|---:|---:|---:|
| 10 | compartilhadas | 3.73 / 4.51 | 3 | 12 |
| 10 | distintas | 16.33 / 17.08 | 21 | 30 |
| 100 | compartilhadas | 5.20 / 7.06 | 3 | 102 |
| 100 | distintas | 135.15 / 142.24 | 201 | 300 |
| 1000 | compartilhadas | 14.10 / 17.83 | 3 | 1002 |
| 1000 | distintas | 1390.97 / 1466.68 | 2001 | 3000 |

Titularidade, medida por último (serviços; sem MockMvc; criação revertida):

| Operação | N por família | ms mediana / máximo | SQL | Entidades | Alocação MiB mediana / máximo | Bytes UTF-8 |
|---|---:|---:|---:|---:|---:|---:|
| titularidade-conta-categoria | 100 | 4.95 / 7.12 | 4 | 2 | 0.08 / 0.13 | 0 |
| criacao-revertida-despesa-variavel | 100 | 6.05 / 8.08 | 5 | 2 | 0.11 / 0.12 | 355 |

A razão entre medianas de titularidade e **criação revertida** foi **81.7%**. São execuções separadas: a primeira lê os vínculos/confirma proprietário; a segunda executa o serviço, serializa o DTO e faz rollback. **Não há COMMIT durável nem comparação com criação persistida.** A razão não estima economia realizável ou parcela causal exata. Os snapshots das seis tabelas financeiras permanecem intactos. Tempo e alocação incluem regex/mapas do SQL inspector, com overhead não quantificado. Heap antes/depois e amostras brutas constam do JSON; máximos não são percentis populacionais nem pico de heap.

## Alternativas de revogação — análise de engenharia

Os custos abaixo são qualitativos, inferidos do desenho de cada alternativa; não foram implementados nem medidos. A [OWASP JWT Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_Cheat_Sheet.html) descreve a ausência de invalidação antecipada automática e o uso de identificadores de token revogados. A [OWASP REST Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html) relaciona logout antecipado e denylist. A [RFC 7009](https://www.rfc-editor.org/rfc/rfc7009) explica revogação e propagação; não fornece um endpoint pronto para o JWT local do Aureus. Consulta às fontes: 2026-10-25.

| Alternativa | Efeito esperado e custo adicional | Limite / decisão pendente |
|---|---|---|
| Manter JWT de 24 h | Nenhuma leitura central de sessão por requisição; a cópia continua utilizável até expirar. | Aceitar formalmente a janela residual e seu risco; a investigação não aceita esse risco em nome do produto. |
| TTL menor, sem renovação automática | Reduz a janela máxima; aumenta a frequência de novo login OAuth. | Não revoga imediatamente; exige decisão de experiência de uso e duração. Renovação automática permanece fora do escopo. |
| Denylist por identificador único emitido pelo servidor | Acrescenta identificador à emissão, grava revogação até a expiração e consulta estado em cada requisição; exige retenção, disponibilidade e propagação. | Permite revogação individual após o pedido chegar ao servidor. Definir comportamento na indisponibilidade e compatibilidade dos tokens existentes. |
| Versão de sessão por usuário | Compara versão do JWT ao estado central, com leitura/cache por requisição e política de invalidação. | Revoga conjuntamente as sessões do usuário; cache cria janela de propagação. Requer alteração de contrato e persistência posterior. |
| Identificador opaco de sessão | Estado central por sessão, consulta em cada acesso e limpeza/expiração de registros. | Controle individual, com custo operacional e mudança arquitetural maior. Tabela de sessões não é solução obrigatória. |
| Rotação da chave de assinatura | Invalida conjuntamente os tokens assinados pela chave substituída. | Impacta todos os usuários e a operação; não fornece logout individual. Não é recomendação de rotação rotineira a cada logout. |

Qualquer solução depende de o pedido de revogação chegar ao servidor. Falha anterior à entrega não pode ser convertida em garantia de revogação por limpeza do estado local. Falha na resposta depois de o servidor processar o logout é outro caso: sem revogação atual, nem mesmo o processamento do logout invalida a cópia. A cópia usada na investigação permanece somente em memória.

Encaminhamento proposto: avaliar requisito de revogação individual e prazo máximo de invalidação com o responsável pelo produto; se aprovado, comparar denylist e versão por usuário com política de falha explícita. Não há autorização nesta história para escolher infraestrutura, renovar JWT ou alterar a produção.

## Observações, hipóteses e encaminhamento por DW

| Registro | Observação demonstrada | Hipótese / próximo passo condicionado | Estado atual |
|---|---|---|---|
| DW-2 / CAP-6 | Replay depois de logout 200, expiração 401; limpeza local não invalida cópia. | Decidir requisito/prazo de revogação, alcance individual ou por usuário e política de indisponibilidade. Custos acima são qualitativos. | Investigação executada; revisão concluída; decisão de produto pendente; open. |
| DW-160 / CAP-4, incluindo DW-38 | ID/HQL não trouxeram A na sessão nova sem tenant; escrita usa public e sessão existente mantém A. Bootstrap e OAuth global legítimos. | Definir o limite de rejeição de operações financeiras sem contexto a partir do consumidor concreto, preservando inicialização e usuário global. | Investigação executada; revisão concluída; decisão de política pendente; open. DW-38 segue consolidado. |
| DW-139 / CAP-7 / A10 | Cinco/três SQL, com 41.000/21.000 entidades carregadas na maior carga; há sinal de alocação e, por conta, de tempo. Saldo pré-grade e 24 meses equivalentes. | Uma futura comparação pode testar agregação no banco ou recorte temporal, incluindo cálculo histórico separado. Não remover dados anteriores à grade. | Investigação verificada; DW e A10 open por decisão de implementação pendente. SQL agregado/índices do compromisso original não foram implementados. |
| DW-117 / CAP-9 | As quatro listagens de movimentos grandes superam os sinais de tempo/bytes; 1.000 opções dos seletores continuam completas e ficam abaixo desses sinais no ensaio. | Avaliar paginação das movimentações conforme carga relevante, com acesso completo aos catálogos usados pelos formulários. O ensaio não demonstra necessidade de truncar cadastros/seletores. | Investigação executada; revisão concluída; decisão de implementação pendente; open. |
| DW-147 / CAP-9 | Listagem custa uma consulta-base + uma por conta distinta + uma por categoria distinta. Compartilhar pares limita a 3 SQL; variar pares produz 21/201/2.001. | Comparar carregamento em lote, plano de fetch ou projeção sob o mesmo DTO completo, preservando isolamento. Nenhuma estratégia escolhida ou implementada. | N+1 demonstrado; revisão concluída; encaminhamento pendente; open. |
| DW-153 / CAP-9 | Dois SELECTs por vínculo, quatro para conta+categoria; criação revertida usa cinco statements. 403/404 e snapshots das seis tabelas financeiras preservados. | Razão entre medianas de execuções separadas, sem criação persistida ou COMMIT durável; custo absoluto e prioridade menor devem orientar decisão futura. Não retirar a validação de proprietário. | Investigação executada por último; conferência concluída; decisão de implementação pendente; open. |

As consultas nativas de proprietário contadas são chamadas SQL lógicas reais; a investigação não conta pacotes TCP, mensagens do protocolo PostgreSQL ou roundtrips transacionais de abertura/commit separadamente. O ensaio controlado LAZY separa o número de relações distintas do número de movimentos, evitando inferir N+1 apenas da anotação LAZY. As alternativas de consulta são hipóteses a experimentar, sem estimativa de ganho antecipada.

## Verificação e rastreabilidade

Revisão-base canônica: `c389e76e518d66099fef384448ec27b7d1800233`. Em `backend-api/`, usar JDK 21 e Docker:

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-21'
$env:Path="$env:JAVA_HOME/bin;$env:Path"
./mvnw.cmd verify -Pintegration-test -DforkCount=1 -DreuseForks=true "-Dit.test=SecurityInvestigationIT,PerformanceInvestigationIT"
./mvnw.cmd verify -Pintegration-test -DforkCount=1 -DreuseForks=true
```

Esses comandos produzem uma nova coleta em `target/7-5-investigation-evidence/measurement.json` e preservam o arquivo arquivado. Para a publicação explicitamente autorizada desta entrega, executar o comando focado com `-Dinvestigation.publish=true`; depois atualizar relatório/verificação a partir desse JSON. Rodar somente uma das suítes ou dividir classes entre JVMs não constitui coleta válida: falta de seção gera falha explícita em vez de reaproveitar resultados de outra execução. O `runId` distingue a coleta arquivada da coleta posterior de regressão.

Em `frontend-web/`: `npm test -- src/components/Header/Header.test.tsx` — **3 testes aprovados**, incluindo sucesso, falha de rede com limpeza de queries/mutations/authStore e prevenção de disparos simultâneos. Esses testes reutilizados demonstram somente a limpeza do cliente, sem exclusão comprovada de cookie HttpOnly quando falta resposta.

Na raiz: `git diff --check`. Execução, conferência de todos os hashes, ausência de tokens nos artefatos e ausência de mudanças em produção estão registradas em `7-5-investigation-evidence/verification.json`; `git diff --check` passou. O JSON de medição anterior permanece intacto se uma das suítes investigativas falhar; a publicação ocorre somente quando ambas, incluindo sua limpeza final, concluem.

Matriz congelada: a linha **Sessão** é coberta por `investigateReplay` e pelos 3 testes existentes do Header; a linha **Tenant**, por `investigateMissingTenant`, `investigateExistingSession`, `investigateBootstrap`, `investigateOAuth` e `statusOf`; a linha **Dados**, por `seed`/`Oracle`, `validateAccounts`, `validateCategories`, `validateList`, `validateEmptyTenant` e `validateContracts` em `PerformanceInvestigationIT`. Os dois métodos `@Test` executam integralmente esses auxiliares, sem filtros internos, testes desabilitados ou skips.

Falhas de preparação corrigidas antes da coleta final: precedência da propriedade de banco herdada (o guard de banco próprio impediu limpeza); GET por ID inexistente para despesa variável removido da matriz HTTP; overrides globais de log retirados após interferirem na assertiva de console de SecurityIT. O teste mantém somente instrumentação local da SessionFactory. Dumps automáticos do MockMvc ficam desativados nas investigações para não persistir credenciais em caso de falha; logs locais das tentativas anteriores foram sanitizados.

`deferred-work.md` e A10 em `sprint-status.yaml` recebem data ISO, resultado investigativo, evidências e pendências. As três camadas do step-04 foram concluídas e nove patches aceitos aplicados; **a conferência final pelo agente principal foi concluída em 2026-10-25**. Nenhum registro é apresentado como correção entregue ou encerrado por esta execução.

### Conferência pós-patches — 2026-10-25

Os 6 testes de publicação/ciclo de vida passaram, incluindo falha pré-commit preservando os bytes anteriores. Foco autorizado: 225 unitários + 2 investigativos; regressão normal: 225 unitários + 72 integrações. Run arquivado `f902fd6f-ce68-433f-b069-df164a36f10e` e run de regressão `463fcb4c-3cb3-482c-894b-5436fb844697` são distintos; o SHA arquivado antes/depois da regressão é o mesmo: `2b5ca159cd4154dd9f8ac8a2d105ede9eda86c566447d2302bbac14b84bdcbf8`. As 74 fontes conferem em ambas as coletas. Não há sidecar; `verification.json` registra essa prova e os nove patches, com conferência final do agente principal concluída em 2026-10-25. Header não foi repetido: suas fontes permaneceram inalteradas e os 3 testes anteriores seguem como evidência.

### Code review fracionado — 2026-10-25

Revisão adicional de `e7738c9f2b24ea133fccd8149bcbf47049ec6021` em três grupos: coleta/publicação/integridade; segurança/tenant; medições/oráculo/rastreabilidade. As quatro camadas de cada grupo concluíram sem achados relevantes. Nenhum patch, decisão ou adiamento criado. Artefatos e séries conferidos sem repetir a suíte de integração; resultados existentes continuam vinculados às mesmas fontes. Ver `verification.json`, campo `codeReview`. Os seis DW e A10 permanecem abertos pelas decisões futuras registradas.
