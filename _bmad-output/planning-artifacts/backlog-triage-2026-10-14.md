# Triagem do backlog — 2026-10-14

Guilherme · Mary, John, Winston, Amelia e Murat · revisão do checkout `1cecb9028d9532fed10f85a4488225f231f1bacb`.

## Corte vigente — segurança e desempenho (decisão de Guilherme)

**Substitui as decisões de “aguardando Guilherme” da triagem inicial.** Guilherme autorizou descartar o que não entregar segurança ou desempenho. Aplicação do filtro: desempenho da aplicação (API, banco e cliente), não tempo de ferramentas de desenvolvimento; segurança concreta de acesso, sessão e isolamento, não qualquer refatoração que toque um arquivo chamado security.

As classificações técnicas originais permanecem como fotografia da triagem. **Decisão de escopo é uma dimensão separada:** descartado não significa resolvido. Itens já entregues e duplicações continuam históricos. Nenhum épico foi aprovado/criado, nenhuma correção implementada e nenhum status de story/épico foi alterado.

### O que é estritamente necessário agora

**D013 é a correção de proteção mais claramente justificada:** fallback permite qualquer rota fora dos matchers protegidos. Restringir requer allowlist pública explícita e decisão sobre Actuator; não usar denyAll cegamente em rotas OAuth legítimas. A exposição efetiva em produção não foi sondada.

**D014, D026, D035, D036 e D037 são verificações focadas de segurança:** não representam cinco vulnerabilidades comprovadas. Podem compor o aceite da correção de sessão/isolamento, sem suíte genérica de UI. D060 trata remoção de dados privados locais após expiração, com backend já rejeitando acesso.

**Nenhuma otimização pode ser chamada de estritamente necessária por latência sem medição.** D139, D155 e D161 são os melhores candidatos estruturais: reduzir carga integral, avaliar índices de FKs e retirar índice redundante. Os demais permanecem investigação, não implementação automática nem promessa de aceleração.

### Escopo pequeno sugerido

| Objetivo | Itens centrais | Entrega/validação esperada | Situação |
|---|---|---|---|
| Proteger sessão e isolamento | D013, D014, D026, D035, D036, D037, D060 | Política de endpoints públicos; cookies/logout; cache privado; contexto por requisição. D002/D160 e correlatos exigem investigação antes de mudar arquitetura. | Manter; sem inclusão em épico automática |
| Medir e reduzir custo de consultas | D117, D139, D147, D153, D155, D161 | Medir consultas/latência/volume; decidir paginação/agregação/índices com equivalência dos totais históricos. | Investigar primeiro |
| Evitar trabalho dispensável de rede/login | D027, D028, D092 | Medir SQL de login e frequência de /me; verificar utilidade do cancelamento de query. | Investigar; prioridade inferior |

### Contagem das decisões vigentes

| Decisão | Registros de origem |
|---|---:|
| Resolvido — histórico | 50 |
| Investigar — segurança | 2 |
| Descartado por critério | 95 |
| Manter — segurança | 8 |
| Encerrado — inaplicável | 7 |
| Consolidado | 12 |
| Investigar — desempenho | 5 |
| Planejar — condicional | 1 |
| Duplicado — histórico | 25 |
| **Total** | **205** |

Contagens são de origens preservadas, não número de tarefas/épicos. Os representantes de investigação e segurança podem se fundir em critérios de aceite; consolidar não autoriza ampliar escopo.

### Perdas de escopo deliberadas

- **D150** (limite monetário), **D125** (filtro mensal de fixas), **D106/D156** (histórico/vigência) não foram corrigidos. São trabalho funcional/consistência e estão descartados pelo filtro, não declarados desnecessários ao produto em sentido absoluto.
- Acessibilidade, refinamentos visuais, reorganização de modelos, DRY, testes genéricos de UI, E2E/CI e ferramentas de cobertura/lint ficam fora deste recorte. Não há alegação de conformidade de acessibilidade nem cobertura integral.
- D027 foi refinado: chamada incondicional de save não comprova escrita SQL incondicional. D147 foi mantido como hipótese N+1, não como gargalo comprovado. Nenhuma medição foi executada.
- Resolved/obsoleto/duplicado não vira “descartado” indiscriminadamente. O histórico distingue entrega, inaplicabilidade e decisão de não executar.

### Decisão rastreável de todos os registros

| ID | Classificação técnica inicial | Decisão vigente | Justificativa / recorte |
|---|---|---|---|
| D001 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D002 | Necessita esclarecimento ou validação adicional | **Investigar — segurança** | Decidir necessidade de revogação/renovação de sessão. Investigar risco/contrato; não exigir entidade refresh token como solução automática. |
| D003 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D004 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D005 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D006 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D007 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D008 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D009 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D010 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D011 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D012 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D013 | Pendente confirmado | **Manter — segurança** | Corrigir fallback permissivo e definir allowlist pública/Actuator. A autorização de rotas não API é aberta; não há prova de exposição de todos os endpoints Actuator em produção. |
| D014 | Pendente confirmado | **Manter — segurança** | Manter somente testes de expiração e atributos do cookie de logout; testes de dados de perfil ficam descartados. Já há teste REST de status/Set-Cookie, faltam assertivas de segurança. |
| D015 | Obsoleto ou inaplicável | **Encerrado — inaplicável** | Premissa não corresponde ao projeto atual ou recomendação não é aplicável; preservar justificativa técnica original. |
| D016 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D017 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D018 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D019 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D020 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D021 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D022 | Pendente confirmado | **Consolidado** | Manter somente verificação de sessão/logout/cookies; E2E de navegador e testes genéricos de UI descartados neste corte. Representante(s): D014/D026/D036. |
| D023 | Pendente confirmado | **Descartado por critério** | Guard frontend controla navegação; autorização real está no backend. Teste de rota anônima é funcional, não prova isolamento de dados via API. |
| D024 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D025 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D026 | Pendente confirmado | **Manter — segurança** | Manter somente logout, limpeza de authStore/QueryClient e comportamento de falha de rede. Imagem/avatar/estado visual ficam descartados. Evita regressão na remoção local de dados privados; não comprova revogação do JWT no servidor. |
| D027 | Pendente confirmado | **Descartado por critério** | Medir SQL real no login antes de otimizar. save() é chamado sempre, mas não prova UPDATE em toda autenticação: detecção de alterações pode evitar escrita. Não prometer ganho sem tracing. |
| D028 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Medir frequência/latência de /me; há leitura por chamada. Decidir cache só se custo relevante e mantendo frescor/isolamento de perfil. |
| D029 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D030 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D031 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D032 | Pendente confirmado | **Descartado por critério** | catch amplo dificulta diagnóstico, mas o fluxo atual limpa cookie e rejeita JWT inválido. Sem bypass demonstrado, estreitar catch sozinho não entrega segurança/desempenho comprovados. |
| D033 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D034 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D035 | Pendente confirmado | **Manter — segurança** | Testar set/clear de TenantContext e SecurityContextHolder em sucesso, falha e reuso de thread. Protege isolamento; implementação de finally já existe, não se presume vazamento atual. |
| D036 | Pendente confirmado | **Manter — segurança** | Testar emissão OAuth de cookie HttpOnly, SameSite, Secure condicionado ao transporte e TTL. Flags já existem; ganho é prevenir regressão de segurança, não corrigir vulnerabilidade comprovada. |
| D037 | Pendente confirmado | **Manter — segurança** | Testar contrato do resolver com tenant preenchido/nulo/vazio; vincular à investigação D160. Não alterar fallback automaticamente nem duplicar integração cross-tenant existente. |
| D038 | Necessita esclarecimento ou validação adicional | **Consolidado** | Consolidar validação da configuração do resolver em D160; nome de classe e ausência de bean não comprovam falha por si. |
| D039 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D040 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D041 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D042 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D043 | Obsoleto ou inaplicável | **Encerrado — inaplicável** | Premissa não corresponde ao projeto atual ou recomendação não é aplicável; preservar justificativa técnica original. |
| D044 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D037; não é uma tarefa independente. |
| D045 | Pendente confirmado | **Descartado por critério** | referrerPolicy já existe; teste isolado de atributo visual não é necessário ao recorte de proteção de sessão escolhido. |
| D046 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D047 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D039; não é uma tarefa independente. |
| D048 | Pendente confirmado | **Descartado por critério** | Auditoria pode ajudar investigação de incidentes, mas pedido é ultimo_acesso/evento sem ameaça, alerta ou requisito definido. Sem benefício de segurança concreto justificado, não incluir. |
| D049 | Pendente confirmado | **Descartado por critério** | Tratamento amigável de perfil OAuth incompleto é robustez de login; falha observada não demonstra emissão indevida de sessão ou ganho de desempenho. |
| D050 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D051 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D052 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D040; não é uma tarefa independente. |
| D053 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D054 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D055 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D056 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D021; não é uma tarefa independente. |
| D057 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D009; não é uma tarefa independente. |
| D058 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D059 | Pendente confirmado | **Consolidado** | Consolidar verificação de segurança dos fluxos auth; não abrir suíte geral OAuth/E2E por esse registro. Representante(s): D014/D026/D036. |
| D060 | Pendente confirmado | **Manter — segurança** | Limpar estado/cache privado e tratar sessão expirada após 401 em chamadas durante uso. Reduz permanência de dados privados na UI; backend já bloqueia API, portanto não é correção de bypass de autorização. |
| D061 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Corrida de cadastro pode falhar em UNIQUE, mas não há evidência de identidade indevida ou gargalo de desempenho; melhoria de concorrência/disponibilidade fora do corte. |
| D062 | Necessita esclarecimento ou validação adicional | **Consolidado** | Manter apenas cenários de segurança concretos já individualizados; descartar pedido genérico sem critério. Representante(s): D035/D036/D037. |
| D063 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Identificar consumidor assíncrono real de dados por tenant. Sem esse fluxo, propagação de contexto não é requisito necessário. |
| D064 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D065 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D066 | Obsoleto ou inaplicável | **Encerrado — inaplicável** | Premissa não corresponde ao projeto atual ou recomendação não é aplicável; preservar justificativa técnica original. |
| D067 | Necessita esclarecimento ou validação adicional | **Consolidado** | Manter recorte de filtros/cookies/logout; integração financeira já existe e expansão genérica não entra. Representante(s): D014/D035/D036/D037. |
| D068 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D012; não é uma tarefa independente. |
| D069 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D070 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D071 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D042; não é uma tarefa independente. |
| D072 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D073 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D074 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D075 | Pendente confirmado | **Consolidado** | Mesmo objetivo de paginação por volume, recorte Contas. Representante(s): D117. |
| D076 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D077 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D078 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D074; não é uma tarefa independente. |
| D079 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D080 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D075; não é uma tarefa independente. |
| D081 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D073; não é uma tarefa independente. |
| D082 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D083 | Pendente confirmado | **Descartado por critério** | Isolamento cross-tenant já é testado; pendência residual é invalidação funcional de cache CRUD, não segregação de cache entre usuários. |
| D084 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D085 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D086 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D087 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D088 | Pendente confirmado | **Consolidado** | Mesmo objetivo de paginação por volume, recorte Categorias. Representante(s): D117. |
| D089 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D090 | Pendente confirmado | **Descartado por critério** | Isolamento/FK backend já possuem testes; pendência residual de UI e cache CRUD fora do corte. |
| D091 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D092 | Pendente confirmado | **Descartado por critério** | Encaminhar AbortSignal da query pode interromper requisição obsoleta no cliente. Medir relevância; não garante cancelamento de trabalho já iniciado no servidor. |
| D093 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D094 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D095 | Obsoleto ou inaplicável | **Encerrado — inaplicável** | Premissa não corresponde ao projeto atual ou recomendação não é aplicável; preservar justificativa técnica original. |
| D096 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D097 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D098 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D099 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D100 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D101 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D095; não é uma tarefa independente. |
| D102 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D103 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D104 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D105 | Pendente confirmado | **Consolidado** | Mesmo objetivo de paginação por volume, recorte movimentações fixas. Representante(s): D117. |
| D106 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Política de término/exclusão preserva histórico financeiro, sem ganho demonstrado de segurança ou desempenho. Descartado deste backlog por critério de escopo, não por ausência de valor funcional. |
| D107 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D108 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D109 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D104; não é uma tarefa independente. |
| D110 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D106; não é uma tarefa independente. |
| D111 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D105; não é uma tarefa independente. |
| D112 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D113 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D114 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D115 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D116 | Obsoleto ou inaplicável | **Encerrado — inaplicável** | Premissa não corresponde ao projeto atual ou recomendação não é aplicável; preservar justificativa técnica original. |
| D117 | Pendente confirmado | **Investigar — desempenho** | Consolidar paginação de Contas, Categorias e movimentações. Listas sem limite confirmadas; necessidade depende do volume/latência. Seletores não podem perder opções ao paginar. |
| D118 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D119 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D120 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D121 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D122 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D123 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D124 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D125 | Pendente confirmado | **Descartado por critério** | Filtro mensal de fixas é correção funcional. Filtrar no cliente não reduz a carga da API; ganho de desempenho não demonstrado. Descartado pelo critério escolhido, não resolvido. |
| D126 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D127 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D128 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D129 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D130 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D131 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D132 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D133 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D134 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D135 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D136 | Pendente confirmado | **Consolidado** | Avaliar laços por conta no benchmark de consolidação; sem tarefa isolada de refatoração. Representante(s): D139. |
| D137 | Pendente confirmado | **Consolidado** | Micro-otimização de map lookups só entra se perfil mostrar custo relevante. Representante(s): D139. |
| D138 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D139 | Pendente confirmado | **Investigar — desempenho** | Medir leitura integral e agregação da consolidação e escolher SQL/corte por período se o custo justificar. Preservar cálculo de saldo histórico, que usa período anterior à grade. |
| D140 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D134; não é uma tarefa independente. |
| D141 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D133; não é uma tarefa independente. |
| D142 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D139; não é uma tarefa independente. |
| D143 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D134; não é uma tarefa independente. |
| D144 | Obsoleto ou inaplicável | **Encerrado — inaplicável** | Premissa não corresponde ao projeto atual ou recomendação não é aplicável; preservar justificativa técnica original. |
| D145 | Obsoleto ou inaplicável | **Encerrado — inaplicável** | Premissa não corresponde ao projeto atual ou recomendação não é aplicável; preservar justificativa técnica original. |
| D146 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D147 | Necessita esclarecimento ou validação adicional | **Investigar — desempenho** | Contar consultas na conversão DTO antes de adotar JOIN FETCH. LAZY e acesso ao ID não comprovam N+1. |
| D148 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D149 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D150 | Pendente confirmado | **Descartado por critério** | Divergência monetária DTO/JPA é defeito funcional de validação, sem ganho demonstrado de segurança/desempenho. Descartado por decisão de escopo, não corrigido. |
| D151 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D152 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D153 | Pendente confirmado | **Investigar — desempenho** | Medir custo das duas consultas de validação de relacionamento; reduzir roundtrips preservando propriedade do tenant e respostas 403/404. |
| D154 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D155 | Pendente confirmado | **Investigar — desempenho** | Validar planos/catálogo e volume para índices de FKs e usuario_id de contas/categorias. Benefício de busca/delete plausível, mas índices também custam escrita e espaço. |
| D156 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Tratamento de legado sem dataInicio é consistência de saldo/histórico; fora do filtro de desempenho/segurança. Não foi resolvido. |
| D157 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D158 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D151; não é uma tarefa independente. |
| D159 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D160 | Necessita esclarecimento ou validação adicional | **Investigar — segurança** | Investigar acesso/persistência sem contexto e requisitos de bootstrap antes de trocar fallback public. Risco de isolamento ainda não demonstrado. |
| D161 | Pendente confirmado | **Planejar — condicional** | Conferir catálogo e remover índice secundário redundante de google_subject_id preservando UNIQUE. Reduz espaço/manutenção se ambos existirem como no DDL; nenhuma melhoria de leitura garantida. |
| D162 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D139; não é uma tarefa independente. |
| D163 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D164 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D165 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D135; não é uma tarefa independente. |
| D166 | Pendente confirmado | **Consolidado** | Uso de dataFim indexada só entra na otimização medida, preservando invariantes de vigência. Representante(s): D139. |
| D167 | Pendente confirmado | **Descartado por critério** | ddl-auto update/Flyway exige governança de schema e confiabilidade. Sem conexão de implantação/privilégios que demonstre problema de segurança ou desempenho, não tratar alteração como estritamente necessária neste filtro. |
| D168 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D169 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D170 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D171 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D172 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D173 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D174 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D154; não é uma tarefa independente. |
| D175 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D176 | Necessita esclarecimento ou validação adicional | **Manter — segurança** | Manter só matriz de segurança CSRF PUT/DELETE sem token e vínculos cruzados não exercitados. Expansão exaustiva de DTOs/validação genérica fica descartada. |
| D177 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D178 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D179 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| D180 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D181 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D182 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D183 | Pendente confirmado | **Descartado por critério** | Otimiza tempo do script de desenvolvimento, não desempenho da aplicação. Fora do critério adotado. |
| D184 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D185 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D186 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D187 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D188 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D189 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| D190 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| A01 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| A02 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| A03 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| A04 | Necessita esclarecimento ou validação adicional | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| A05 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| A06 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| A07 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| A08 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| A09 | Já resolvido, com evidência | **Resolvido — histórico** | Sem trabalho remanescente neste apontamento; preservar evidência original, sem descartar como se nunca tivesse sido entregue. |
| A10 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D139; não é uma tarefa independente. |
| A11 | Pendente confirmado | **Descartado por critério** | Não há benefício específico demonstrado de segurança ou desempenho da aplicação. Melhoria funcional, visual, acessibilidade, refatoração, documentação ou cobertura genérica fora do recorte autorizado. Descartar do escopo não significa resolver a falha ou desqualificar seu valor em outro objetivo. |
| A12 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D163; não é uma tarefa independente. |
| A13 | Pendente confirmado | **Consolidado** | Migração corretiva é eventual entrega das decisões de índices; não criar V2 nesta etapa. Representante(s): D155/D161. |
| A14 | Duplicado | **Duplicado — histórico** | Consultar decisão vigente de D168; não é uma tarefa independente. |
| A15 | Pendente confirmado | **Descartado por critério** | CI previne regressões indiretamente, mas não é correção específica de segurança nem desempenho da aplicação. Testes de segurança preservados não exigem GitHub Actions para existir. |

### Evidências dos representantes mantidos e em investigação

- **D013:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44); [backend-api/pom.xml:147](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/pom.xml:147).
- **D014:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:87](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:87).
- **D026:** [frontend-web/src/components/Header/Header.tsx:13](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Header/Header.tsx:13); [frontend-web/src/App.test.tsx:59](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.test.tsx:59).
- **D035:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:57](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:57); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31).
- **D036:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31).
- **D037:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206).
- **D060:** [frontend-web/src/services/apiClient.ts:176](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/services/apiClient.ts:176); [frontend-web/src/App.tsx:22](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.tsx:22).
- **D027:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41).
- **D028:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29).
- **D092:** [frontend-web/src/hooks/useCategorias.ts:14](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/hooks/useCategorias.ts:14); [frontend-web/src/services/apiClient.ts:140](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/services/apiClient.ts:140).
- **D117:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java:23).
- **D139:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:74](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:74); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262).
- **D147:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43).
- **D153:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:52](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:52).
- **D155:** [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74); [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:24](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:24).
- **D161:** [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:4](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:4); [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74).
- **D002:** [_bmad-output/planning-artifacts/decisions/2026-08-13-autenticacao-v1.md:15](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/planning-artifacts/decisions/2026-08-13-autenticacao-v1.md:15); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44).
- **D038:** [backend-api/src/main/resources/application.yaml:8](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/application.yaml:8); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206).
- **D063:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:57](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:57).
- **D160:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9).
- **D176:** [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:132](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:132); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:89](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:89).

### Divergências entre as perspectivas

🏗️ **Winston:** “Eu preservaria as otimizações de banco, mas não implementaria corte temporal sem proteger o saldo histórico. O índice redundante é candidato mais direto que micro-otimizar mapas.”

📋 **John:** “Candidato não é obrigação. Não temos benchmark nem carga de referência para chamar paginação, SQL agregado ou JOIN FETCH de urgência. Sem o resultado da medição, permanecem investigação.”

🧪 **Murat:** “Cookies, logout e tenant merecem testes focados. Uma nova suíte E2E ou teste de avatar não é necessária para demonstrar esses contratos.”

💻 **Amelia:** “Não vou chamar catch específico, novo service ou constante compartilhada de correção de segurança sem caminho de falha. O código atual já captura JWT inválido e limpa contexto em finally.”

📊 **Mary:** “Os descartes abaixo seguem o critério de Guilherme, inclusive quando há defeito funcional. Preservar evidência evita que alguém leia descartado como corrigido.”

### Sincronização autorizada do backlog — 2026-10-14

Guilherme liberou a edição dos dois arquivos após a triagem. As decisões deste relatório foram sincronizadas:

- `deferred-work.md`: 21 entradas ativas DW (7 mantidas por segurança, 5 investigações de segurança e 9 de desempenho); inventário D e transcrição numerada integral do arquivo anterior preservados como histórico.
- `sprint-status.yaml`: 2 action items ativos, A10/A13, com `status: open` e investigação de desempenho; 13 no histórico com decisão e `status_at_triage`. A10 deixa de afirmar entrega SQL inexistente, preservando o `done` anterior como histórico.
- Epics, stories e retrospectivas em `development_status` permanecem idênticos. Nenhum código, teste, dependência, spec ou épico alterado; nenhum commit/push ou operação remota.
- IDs de triagem D/A, IDs originais dos action items, descrições, owners e refs preservados. As origens D abaixo apontam às linhas correspondentes da transcrição numerada; números originais permanecem nela. Hashes ao final identificam as fontes **anteriores à organização**, não os arquivos atuais.
- Validação estrutural YAML, conservação dos 35 estados de planejamento, 15 action items e 190 origens D; nenhuma suíte da aplicação foi executada.

---

## Triagem inicial — evidências históricas preservadas

As classificações, estimativas e agrupamentos a seguir registram a análise inicial. **As decisões de escopo vigentes estão no quadro acima**; estimativas dos itens descartados não representam planejamento autorizado.

## Pendentes confirmados: opções para decisão

**Decisões iniciais substituídas pelo corte vigente de segurança/desempenho no início deste relatório.** Prioridades e agrupamentos abaixo são sugestões, sem aprovação de escopo, encerramento ou inclusão. P1 = impacto potencial alto/proteção ou integridade; P2 = fluxo/contrato/teste relevante; P3 = melhoria, escala sem medida ou trabalho pós-V1. Esforço em dias de trabalho de uma pessoa, incluindo verificação futura, sem estimativa formal nem soma automática (há sobreposição).

| Objetivo sugerido | Benefício e limite de escopo | Pendentes representantes | Decisão |
|---|---|---|---|
| G1 — Sessão protegida e autenticação verificável | Reduzir falhas de sessão, fechar fallback permissivo e provar contratos OAuth/logout. Refatorações de perfil/escritas são opcionais; não introduzir refresh token sem esclarecer D002. | D013, D014, D022, D023, D024, D025, D026, D027, D029, D032, D033, D035, D036, D037, D045, D049, D059, D060, D157 | **consultar corte vigente** |
| G2 — Completar jornadas e recuperação acessível | Retomar fluxo após login/cadastro, representar pausa/retry, corrigir menus e estados assistivos. Pode ser dividido em jornada de dependências e acessibilidade de navegação se ficarem independentes. | D016, D019, D041, D065, D070, D079, D092, D099, D100, D125, D151, D168, D170, D177, D185, D186, D187, D188, D189, D190, A01 | **consultar corte vigente** |
| G3 — Confiança de entrega e regressão dos fluxos | Cobrir submissões/guards/cache e composição do painel; automatizar validação. E2E é alternativa de fase pós-V1, não pré-requisito automático para todo o grupo; lint/cobertura/flags são incrementos opcionais. | D012, D069, D072, D083, D090, D098, D108, D126, D132, D180, D181, D182, D183, D184, A11, A15 | **consultar corte vigente** |
| G4 — Consolidação e consultas com custo previsível | Evitar leitura integral desnecessária, corrigir índices redundantes e medir volume. Separar paginação CRUD de otimização de projeção; preservar histórico pré-grade e seletores. | D075, D088, D105, D117, D134, D136, D137, D139, D153, D155, D161, D166, A13 | **consultar corte vigente** |
| G5 — Contratos financeiros e persistência consistentes | Alinhar limite monetário, payloads REST e evolução Flyway. Pode ser dividido em contratos REST/validação e governança de schema; abstrações de código só entram se contribuírem para esse objetivo. | D077, D085, D091, D135, D148, D150, D152, D154, D159, D167, D173, D175 | **consultar corte vigente** |
| G6 — Modelos e auditoria coerentes | Reduzir divergência entre diagramas/DBML/JPA; auditoria runtime é opção distinta e depende de política de dados. Primeiro esclarecer D006. | D003, D004, D005, D048, D119 | **consultar corte vigente** |
| G7 — Consistência da interface e manutenção dos componentes | Consolidar padrões visuais e componentes repetidos. Apenas itens de UI/manutenção com benefício comum; não é épico de diversos. Cada melhoria pode permanecer isolada no backlog. | D011, D018, D020, D042, D055, D102, D130, D171, D178 | **consultar corte vigente** |

### Impacto, prioridade, esforço e dependências por pendente

| ID | Descrição / impacto e motivo da prioridade | Tipo | Prioridade sugerida | Esforço | Dependências | Decisão |
|---|---|---|---|---|---|---|
| D013 (G1) | Fix permissive authorization fallback and protect Actuator endpoints — anyRequest().permitAll() permanece e Actuator é dependência. Confirma fallback permissivo; exposição concreta de cada endpoint operacional depende da configuração de implantação, não foi sondada. | Defeito de proteção | P1 | 1–2 dias | Definir allowlist pública e política Actuator | consultar corte vigente |
| D150 (G5) | Alinhar anotação de valor máximo @Max(9999999) na entidade JPA com @DecimalMax(9999999.99) do DTO [backend/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:46] — pre-existing entity annotation. — Entidade fixa limita a 9999999 com @Max; DTO aceita 9999999.99. Faixa final aceita na API pode falhar na validação JPA. Divergência estática confirmada; não foi gravado dado de borda. | Defeito de validação | P1 | 0,5–1 dia | Validar mesma regra em ReceitaFixa e teste do limite | consultar corte vigente |
| D014 (G1) | Implement backend unit tests for AuthController — Há integração para status de /me e logout, porém não suíte unitária AuthController nem assertiva completa de perfil e atributos do cookie de logout. Entrega parcial. | Lacuna de teste | P2 | 1–2 dias | Fixtures de perfil e política de cookie | consultar corte vigente |
| D022 (G1) | Ausência de testes end-to-end e componentes para o Auth Fetch e Logout — Parte backend existe; faltam testes de componentes de auth/logout e suíte ponta a ponta. E2E foi adiado formalmente pós-V1, não resolvido. | Lacuna de teste | P2 | 2–4 dias | Definir recorte componente agora; E2E depende A11 | consultar corte vigente |
| D023 (G1) | Ausência de verificação contra bypass de rota protegida — AuthStore está sempre mockado autenticado no App.test; ausência de casos de rota anônima/carregando permite regressão do guard. | Lacuna de teste | P2 | 1 dia | Fixtures anônimas e carregando | consultar corte vigente |
| D024 (G1) | Missing verification for Google profile picture extraction — picture é mapeado e salvo no handler; os testes security atuais não simulam sucesso OAuth nem assertam esse mapeamento. | Lacuna de teste | P2 | 0,5–1 dia | Mock OAuth2User | consultar corte vigente |
| D025 (G1) | Missing verification for profile picture inclusion in /me response — SecurityIT verifica /me anônimo/autenticado e logout; não verifica fotoPerfil no /me com usuário persistido nem consumo real no frontend. | Lacuna de teste | P2 | 1–2 dias | Fixture Usuario e mock /me | consultar corte vigente |
| D026 (G1) | Missing verification for Header logout and profile UI — Header real aparece no App.test mas não há assertivas de imagem, referrerPolicy, logout, cache limpo ou estado pendente. | Lacuna de teste | P2 | 1–2 dias | Mock apiClient, authStore e QueryClient | consultar corte vigente |
| D032 (G1) | Overly broad exception catching in JwtAuthenticationFilter. — catch Exception permanece no trecho de validação/autenticação. Evita erro JWT escapar, mas também oculta falha inesperada. | Débito de diagnóstico | P2 | 0,5–1 dia | Separar erros esperados e inesperados | consultar corte vigente |
| D033 (G1) | Empty Response Body on 401 Unauthorized in JwtAuthenticationFilter. — 401 vazio continua no entry point e /me. Filtro hoje limpa cookie e segue cadeia; descrição antiga da escrita de 401 no próprio filtro mudou. | Débito de contrato | P2 | 1–2 dias | Padronizar entry point e CSRF | consultar corte vigente |
| D035 (G1) | Missing verification for Tenant context population. — Código faz set/clear, mas SecurityIT verifica respostas HTTP, sem assertiva direta de limpeza de TenantContext entre requisições/falhas. | Lacuna de teste | P2 | 1 dia | Testes do filtro com exceção e reuso de thread | consultar corte vigente |
| D036 (G1) | Missing verification for Session cookie SameSite security attribute. — sameSite Lax está implementado; falta assertiva de emissão de cookie no sucesso OAuth. | Lacuna de teste | P2 | 0,5 dia | Mock OAuth e resposta | consultar corte vigente |
| D037 (G1) | Missing verification for Hibernate tenant resolution. — Integração comprova isolamento em cenários exercitados; falta teste direto do contrato resolver com tenant nulo/vazio/preenchido e validateExistingCurrentSessions. | Lacuna de teste | P2 | 0,5–1 dia | Definir comportamento nulo; D160 | consultar corte vigente |
| D049 (G1) | OAuth2LoginSuccessHandler missing error handling — Claims sub/email são usados sem validação explícita; perfil incompleto pode falhar no save. Falta tratamento controlado dentro do handler. | Lacuna de robustez | P2 | 1–2 dias | Definir claims obrigatórias e mensagem de falha | consultar corte vigente |
| D059 (G1) | Missing automated tests for auth flows — SecurityIT cobre JWT/CSRF e status auth, mas não login Google completo e frontend logout; registro amplo parcialmente atendido. | Lacuna de teste | P2 | 2–4 dias | Definir fronteiras do fluxo OAuth | consultar corte vigente |
| D060 (G1) | Missing frontend interception of 401 errors during API access — apiClient converte 401 em ApiError; não há interceptor que limpe sessão e redirecione após 401 durante uso. App consulta auth apenas no efeito inicial. | Lacuna funcional | P2 | 1–2 dias | Definir expiração no meio do fluxo e evitar loops | consultar corte vigente |
| D157 (G1) | Padronizar tratamento de erros dos filtros de segurança (Spring Security) no formato ApiErrorResponse — EntryPoint 401 e filtros CSRF não estão ligados a ApiErrorResponse; contrato global MVC não alcança todos esses ramos. | Débito de contrato | P2 | 1–2 dias | D033; definir corpo 401/403 | consultar corte vigente |
| D019 (G2) | Redirecionamento de login não preserva o estado de rota prévia (`location.state`) [frontend/src/App.tsx:47] — ProtectedRoute redireciona sem state.from e OAuth retorna ao frontendUrl fixo configurado. Rota anterior não é preservada. | Lacuna funcional | P2 | 1–3 dias | Definir retorno seguro após OAuth | consultar corte vigente |
| D041 (G2) | Unrecoverable Profile Image Error State — imgError fica true após onError e nenhum efeito o reseta quando profileImage muda. Uma nova imagem permanece ocultada. | Defeito UI | P2 | 0,5–1 dia | Teste de troca de profileImage | consultar corte vigente |
| D065 (G2) | Omissão de acessibilidade no dropdown do Header (Esc): Falta listener para a tecla Escape. — Header fecha por pointerdown, sem listener Escape. | Defeito de acessibilidade | P2 | 0,5–1 dia | Definir foco de retorno | consultar corte vigente |
| D070 (G2) | Corrigir a11y: adicionar aria-live e role="status" ao estado de carregamento e aria-controls ao botão de perfil. — Carregando do guard não tem role/status/aria-live; perfil tem aria-expanded/haspopup mas não aria-controls. Remanescente verificado. | Defeito de acessibilidade | P2 | 0,5–1 dia | ID estável do menu | consultar corte vigente |
| D079 (G2) | Tratamento de erros de validação server-side não mapeados no formulário. — Formulário gerencia erro local obrigatório; mutações não mapeiam fieldErrors do servidor aos campos. Toast de mensagem existe, não substitui associação por campo. | Lacuna de UX | P2 | 1–2 dias | Mapeamento ApiError.fieldErrors | consultar corte vigente |
| D099 (G2) | Missing `role="status"` on loading state. — Loading de RequiresDependencies não possui role=status. | Defeito de acessibilidade | P2 | 0,5 dia | Validar anúncios assistivos | consultar corte vigente |
| D100 (G2) | Missing `role="alert"` on error state. — Falha do guard não possui role=alert. | Defeito de acessibilidade | P2 | 0,5 dia | Validar anúncios assistivos | consultar corte vigente |
| D168 (G2) | Integrar ErrorBoundary com QueryErrorResetBoundary do TanStack Query para limpeza automática de queries em cache no retry — ErrorBoundary tem onReset opcional, mas App não liga reset ao TanStack Query. Integração solicitada está ausente; loop concreto com cache inválido precisa caso de teste, não chamar todo erro de query de crash. | Melhoria de resiliência | P2 | 1–2 dias | Definir reset/refetch/removal adequados | consultar corte vigente |
| D170 (G2) | Exibir estado de loading/erro para contas e categorias em ContaCategoriaFields [frontend/src/components/MovimentacaoForm/ContaCategoriaFields.tsx:30] — deferred, pre-existing — Campos usam apenas data dos hooks; não exibem loading/erro próprios. Guard cobre entrada, mas falhas/revalidação em formulário aberto merecem estado definido. | Lacuna de resiliência | P2 | 1–2 dias | Decidir responsabilidade entre guard e campos | consultar corte vigente |
| D177 (G2) | Suporte a tecla Escape e semântica WAI-ARIA (aria-haspopup, role="menu", role="menuitem") no componente ActionMenu [frontend-web/src/components/ui/ActionMenu.tsx:45] — deferred, pre-existing — ActionMenu só fecha por mousedown; sem Escape e sem aria-haspopup/menu/menuitem. | Defeito de acessibilidade | P2 | 1–2 dias | Definir navegação de teclado e foco | consultar corte vigente |
| D185 (G2) | Distinguir consultas pausadas sem dados de cadastros vazios. — Guard só lê isLoading/isError e data; consulta paused sem dados pode seguir ao estado vazio indevido. Estado paused não é representado. | Defeito de estado UI | P2 | 1 dia | Fixture offline/paused; validar comportamento runtime em tarefa futura | consultar corte vigente |
| D186 (G2) | Indicar recuperação em andamento e evitar cliques repetidos. — Botão refetch não lê isFetching e não é desabilitado durante recuperação. Cliques repetidos possíveis; não implica requisições duplicadas por deduplicação interna. | Lacuna de UX | P2 | 0,5–1 dia | Definir feedback e retry por dependência | consultar corte vigente |
| D188 (G2) | Melhorar anúncios e hierarquia de títulos dos estados de dependências. — Além dos anúncios ausentes D099/D100, estados usam h3 como primeiro título da área. Validar hierarquia na página completa antes de escolher nível final. | Defeito de acessibilidade | P2 | 0,5–1 dia | D099/D100; contexto de headings | consultar corte vigente |
| D189 (G2) | Completar retorno ao fluxo original após cadastrar dependências. — Guard envia state.from, destinos não consomem esse estado após cadastro. Jornada não retorna automaticamente à movimentação original. | Lacuna funcional | P2 | 1–2 dias | Retorno seguro e caso faltam duas dependências | consultar corte vigente |
| A15 (G3) | Configurar GitHub Actions Workflow para Validação Contínua — Não há .github/workflows no checkout; script local existe. Não foi consultado estado remoto; action pede workflow versionado ainda ausente. | Melhoria de automação | P2 | 1–3 dias | Escolher JDK/Node/runner, Docker e comandos atuais | consultar corte vigente |
| D012 (G3) | Add automated tests for client-side routing — O teste nominal de navegação só verifica texto na raiz; outro testa ErrorBoundary em /categorias. Faltam navegação real e renderização saudável das rotas não raiz. | Lacuna de teste | P2 | 1–2 dias | Fixtures de autenticação e dependências | consultar corte vigente |
| D072 (G3) | Adicionar testes de unidade e integração (Backend e Frontend) para Contas — Backend já tem unidade e integração; frontend testa abertura e exclusão, mas não submissão criar/editar Conta nem invalidação real. Pendência parcial. | Lacuna de teste | P2 | 1–2 dias | Fixtures hooks reais | consultar corte vigente |
| D083 (G3) | Ausência de testes verificando isolamento cross-tenant e invalidação de cache. — Isolamento cross-tenant já exercitado em backend; invalidação real de cache Contas/Categorias não é testada com QueryClient nos testes de página (hooks mockados). | Lacuna de teste | P2 | 1–2 dias | QueryClient e hooks reais | consultar corte vigente |
| D090 (G3) | Testes automatizados ausentes para Categoria. — Isolamento/FK têm integração; falta cobertura frontend Categoria e invalidação real do cache. Não considerar tudo ausente nem tudo resolvido. | Lacuna de teste | P2 | 1–3 dias | Fixtures Categoria e QueryClient | consultar corte vigente |
| D098 (G3) | Add automated tests to verify dependency guards block access to transaction routes. — Não há teste que remova contas/categorias e confirme bloqueio das quatro rotas. App.test só testa raiz/falha de categorias. | Lacuna de teste | P2 | 1–2 dias | Fixtures dependências vazias/falhas | consultar corte vigente |
| D108 (G3) | Missing frontend component tests for DespesasFixasPage and ReceitasFixasPage. — Wrappers dos formulários fixos têm testes; páginas DespesasFixasPage/ReceitasFixasPage não têm suíte dedicada de listagem, filtro e ações. | Lacuna de teste | P2 | 2–3 dias | Mocks de dados e mês | consultar corte vigente |
| D125 (G2) | Filtro mensal de lançamentos fixos — Entrega parcial: entidades fixas já têm dataInicio, mas DespesasFixasPage:29 e ReceitasFixasPage:29 retornam a lista inteira sem considerar mês; tipos/hooks frontend nem incluem dataInicio. A ausência de datas no backend foi resolvida; o filtro mensal que motivou o registro continua pendente. | Lacuna funcional | P2 | 2–3 dias | Confirmar contrato temporal D106/D156; alinhar tipos, formulários e testes D108 | consultar corte vigente |
| D126 (G3) | Missing test coverage for React Query mutation cache invalidations and global month filter logic. — Filtro global e selectedMonth têm testes de página; invalidação real de queries após mutação não está exercitada, pois hooks são mockados. Remanescente parcial. | Lacuna de teste | P2 | 1–3 dias | QueryClient real e cliente HTTP controlado | consultar corte vigente |
| D132 (G3) | Adicionar testes de unidade para ConsolidacaoPage, ConsolidacaoGrid e ConsolidacaoToolbar garantindo que as 24 colunas são renderizadas e o seletor funciona. — Grid e Toolbar têm testes; falta suíte de ConsolidacaoPage para composição, scroll e colapso integrados. Remanescente parcial, não recriar testes existentes. | Lacuna de teste | P2 | 1–2 dias | Fixtures componentes/refs | consultar corte vigente |
| D180 (G3) | Suíte de testes unitários para ContaFormModal e submissão em ContasPage [frontend-web/src/pages/Contas/components/ContaFormModal.tsx:1] — deferred, pre-existing — ContasPage.test testa abrir formulário e confirmar exclusão, não submissão criar/editar ContaFormModal nem validação. | Lacuna de teste | P2 | 1–2 dias | Mocks create/update e usuário | consultar corte vigente |
| A13 (G4) | Criar migração Flyway V2 para limpeza de índice redundante em usuarios e adição de índices em FKs de movimentações — Não há V2 no diretório de migrações; redundância e falta de índices FK continuam. A ação reúne D161 e parte D155; D155 inclui também usuario_id de contas/categorias. | Débito de banco | P2 | 1–3 dias | Validar catálogo e plano de migração | consultar corte vigente |
| D139 (G4) | Fetches all transactions regardless of requested period: Unfiltered Database Fetch fetches all transactions without date filtering at DB level. — Repositories recebem apenas usuarioId, sem período/SQL de agregação. Todos os registros do tenant são carregados para projetar e calcular histórico. | Débito de desempenho | P2 | 4–8 dias | Benchmark, preservar saldo pré-grade; índices D155 | consultar corte vigente |
| D155 (G4) | Criar índices adicionais no banco para chaves estrangeiras (conta_id, categoria_id) e usuario_id em contas/categorias — V1 indexa datas e usuário nas movimentações, não conta_id/categoria_id nem usuario_id em contas/categorias; nenhuma V2 encontrada. Ganho precisa EXPLAIN em ambiente autorizado. | Débito de banco | P2 | 1–3 dias | Desenho de índices e migração futura; nenhum SQL executado | consultar corte vigente |
| D154 (G5) | Padronizar payload de erro com ApiErrorResponse na exclusão de recurso inexistente (DELETE /{id}) — DELETE ausente retorna notFound().build(), sem ApiErrorResponse. Há handler ResourceNotFound, mas este ramo não o usa. | Débito de contrato | P2 | 1–2 dias | Definir uniformidade todos controllers | consultar corte vigente |
| D167 (G5) | Coexistência de hibernate.ddl-auto: update e migrações Flyway em application.yaml — ddl-auto update e Flyway enabled coexistem; não foi encontrado profile de produção/CI com validate. Alteração futura requer migrações completas e bootstrap reproduzível. | Débito de configuração | P2 | 2–4 dias | Auditar DDL real, baseline e integrações | consultar corte vigente |
| D027 (G1) | Unnecessary Database Writes on Login — save(usuario) continua incondicional; reduzir escritas depende de comparar atributos. Não há medida de custo ou falha. | Melhoria opcional | P3 | 0,5–1 dia | Definir atualização de perfil | consultar corte vigente |
| D029 (G1) | Direct Repository Access in AuthController — AuthController ainda injeta UsuarioRepository diretamente. APIs financeiras foram extraídas, auth não. | Débito técnico | P3 | 0,5–1 dia | Definir serviço de perfil | consultar corte vigente |
| D045 (G1) | Profile image referrer policy is unverified — referrerPolicy existe na imagem, porém os testes atuais não o verificam. | Lacuna de teste | P3 | 0,5 dia | Mesmo fixture de Header; D026 | consultar corte vigente |
| A01 (G2) | Corrigir ordem das abas na barra de navegacao (Receitas Variaveis antes de Receitas Fixas) — Action está done, mas links de Receitas Fixas precedem Variáveis. Pendência contra ordem registrada confirmada; validar se alteração posterior foi intencional antes de aplicar. | Desvio de compromisso UI | P3 | 0,5 dia | Confirmar ordem atual desejada com Guilherme | consultar corte vigente |
| D016 (G2) | Configure global defaults for React QueryClient — QueryClient continua sem defaultOptions. Defaults não são defeito por si; política de freshness/retry é melhoria a decidir. | Melhoria opcional | P3 | 0,5–1 dia | Definir expectativa de atualização e retry | consultar corte vigente |
| D092 (G2) | useCategorias.ts lacks AbortSignal. — apiClient suporta signal, mas fetchCategorias não recebe/encaminha signal da query. Timeout já existe; cancelamento da query não. | Débito de cancelamento | P3 | 0,5 dia | Teste cancelamento TanStack Query | consultar corte vigente |
| D151 (G2) | Adicionar ordenação alfabética (OrderByDescricaoAsc) na listagem de Contas e Categorias [backend/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:28] — pre-existing query pattern. — Contas/Categorias services usam findAll sem OrderByDescricaoAsc. Ordenação da consolidação por categoria não resolve a listagem CRUD. | Melhoria opcional | P3 | 0,5–1 dia | Definir ordenação e locale | consultar corte vigente |
| D187 (G2) | Retirar console.error da renderização de RequiresDependencies. — console.error continua no ramo de renderização, repetindo log a cada render em erro. | Débito técnico | P3 | 0,5 dia | Definir logging fora da renderização | consultar corte vigente |
| D190 (G2) | Avaliar abertura direta dos formulários nos links Cadastrar Conta e Cadastrar Categoria. — Links vão às listagens, exigindo clicar para abrir formulário. Abertura direta não implementada; decidir se melhora jornada sem surpreender. | Melhoria opcional | P3 | 1 dia | D189; definir parâmetro/estado para abrir modal | consultar corte vigente |
| A11 (G3) | Implementar suíte de testes E2E automatizados para o Painel de Consolidação — Não há suíte E2E de navegador; documento decide pós-V1 e lista CI/Mock OIDC/regressão entre navegadores como marcos. Adiado não é resolvido. | Lacuna de teste adiada | P3 | 5–10 dias | CI e estratégia Mock OIDC; decisão de fase | consultar corte vigente |
| D069 (G3) | Adicionar teste automatizado E2E ou de artefatos para verificar se os estilos do Tailwind são processados e aplicados com sucesso na build. — Build compila CSS mas nenhuma assertiva inspeciona CSS gerado/aplicação visual Tailwind. Não confundir build com verificação visual. | Lacuna de teste | P3 | 1–2 dias | Definir assertiva de artefato ou navegador | consultar corte vigente |
| D181 (G3) | Suporte a flags para validação seletiva/modular (--frontend e --backend) no script de validação [scripts/validate.sh:65] — deferred, improvement — Script canônico aceita somente help e with-integration, sem frontend/backend. Nome atual é run-unit-tests.sh, não validate.sh. | Melhoria opcional | P3 | 0,5–1 dia | Definir composição de flags | consultar corte vigente |
| D182 (G3) | Configuração de ferramenta de análise estática e linter no backend (Checkstyle, Spotless ou SpotBugs no Maven) [backend-api/pom.xml:153] — deferred, pre-existing — Plugins Maven têm compilação/testes/auditoria dependências, sem Checkstyle/Spotless/SpotBugs. Auditoria de dependências não substitui lint de código. | Melhoria de qualidade | P3 | 1–3 dias | Escolher ferramenta e baseline sem ruído | consultar corte vigente |
| D183 (G3) | Otimizar execução do modo --with-integration no script de validação para evitar execução duplicada da suíte unitária [scripts/validate.sh:172] — deferred, improvement — Modo integração roda mvnw test e depois mvnw verify -Pintegration-test; fase test se repete no verify. | Melhoria de produtividade | P3 | 0,5–1 dia | Preservar fail-fast e testes unitários ao otimizar | consultar corte vigente |
| D184 (G3) | Avaliar introdução de ferramentas de cobertura quantitativa de testes (JaCoCo no Maven e cobertura Vitest no frontend) pós-V1 [docs/architecture/testing.md:120] — deferred, future — Não há JaCoCo nem script/provider de cobertura frontend. Avaliação futura permanece, sem percentual mínimo definido. | Melhoria opcional | P3 | 1–2 dias | Definir utilidade/métrica e fase pós-V1 | consultar corte vigente |
| D075 (G4) | Adicionar suporte à paginação no backend e listagem de Contas — Listagem retorna List completa sem Page/Pageable e frontend consome coleção. Risco de volume, sem benchmark de lentidão. | Melhoria de escala | P3 | 2–4 dias | Contrato paginação; seletores precisam de busca completa | consultar corte vigente |
| D088 (G4) | Listagem de Categorias não possui paginação. — Categoria lista coleção inteira sem paginação. Ausência confirmada, lentidão não medida. | Melhoria de escala | P3 | 2–3 dias | Preservar seletores de dependências | consultar corte vigente |
| D105 (G4) | Missing Pagination for /api/despesas-fixas and /api/receitas-fixas. — Endpoints fixos retornam List sem paginação; ausência confirmada, sem volume de referência. | Melhoria de escala | P3 | 2–4 dias | Contrato paginação/listas e projeção | consultar corte vigente |
| D117 (G4) | Unbounded findAll queries in backend list endpoints — findAll/listas ordenadas continuam sem limite nos services. Abrange listagens variáveis além de D075/D088/D105. | Débito de escala | P3 | 3–5 dias | Projetar paginação transversal e seletores | consultar corte vigente |
| D134 (G4) | O ConsolidacaoService possui o valor de 24 meses hardcoded em vários locais. — 24/23 e colSpan 25 continuam dispersos. Horizonte de 24 meses é requisito; centralizar constantes pode reduzir drift, sem tornar horizonte configurável. | Débito técnico | P3 | 1–2 dias | Preservar contrato de 24 meses | consultar corte vigente |
| D136 (G4) | O(N*M) loop instead of O(N) in ConsolidacaoService for 24-month projection. — Por conta continua percorrendo cada coleção em cada um dos 24 meses; por categoria usa limites de sobreposição. Alegação global está parcialmente resolvida. Com horizonte fixo ambos são lineares em N com fatores diferentes, não declarar mudança assintótica automaticamente. | Débito de desempenho | P3 | 2–4 dias | Benchmark e equivalência de totais | consultar corte vigente |
| D137 (G4) | Redundant map lookups in ConsolidacaoService 24-month loop. — Maps de início/fim ainda são consultados dentro dos laços mensais por conta. Micro-otimização sem custo medido. | Melhoria opcional | P3 | 0,5–1 dia | Benchmark; avaliar junto D136 | consultar corte vigente |
| D153 (G4) | Optimize foreign key validation queries in transaction services to avoid two roundtrips (findOwnerUsuarioId + findById) — Consulta nativa de proprietário seguida de findById permanece. Combinar sem revelar outro tenant exige cuidado. Sem benchmark, não é urgência comprovada. | Melhoria de desempenho | P3 | 1–2 dias | Preservar distinção 403/404; D152 | consultar corte vigente |
| D161 (G4) | Consolidar índice B-tree duplicado em usuarios(google_subject_id) — UNIQUE e índice secundário da mesma coluna coexistem em V1. Não há migração de remoção; custo redundante confirmado no DDL, catálogo real não consultado. | Débito de banco | P3 | 0,5–1 dia | Migração corretiva após validar catálogo e nomes | consultar corte vigente |
| D166 (G4) | Utilizar a coluna persistida dataFim no ConsolidacaoService ao invés de recalcular a data final das parcelas em memória — Consolidação ainda recalcula fim com plusMonths em vez de ler dataFim persistida. Qual fonte deve prevalecer exige invariante consistente. | Débito técnico | P3 | 1–2 dias | Definir fonte de vigência e validar legados | consultar corte vigente |
| D077 (G5) | Endpoint criar Conta retorna 200 OK em vez de 201 Created. — POST Conta retorna DTO direto e 200; ainda não 201. Contrato atual é testado como 200, portanto mudança exige intenção explícita. | Débito de contrato | P3 | 0,5–1 dia | Definir contrato HTTP e ajustar consumidores/testes | consultar corte vigente |
| D085 (G5) | Endpoint criar Categoria retorna 200 OK em vez de 201 Created. — POST Categoria retorna DTO/200, sem 201. Melhoria de contrato; testes atuais esperam 200. | Débito de contrato | P3 | 0,5–1 dia | Contrato HTTP; D091 | consultar corte vigente |
| D091 (G5) | Missing Location header in Categoria creation. — POST Categoria não envia Location. Definir 201/URI do recurso junto com eventual GET por ID. | Melhoria de contrato | P3 | 0,5–1 dia | D085/D175 | consultar corte vigente |
| D135 (G5) | Manual usuarioId == null check instead of Spring Security filters in ConsolidacaoController. — ConsolidacaoController mantém if usuarioId==null e 401 vazio apesar do filtro proteger /api. Remoção é refatoração; não significa endpoint desprotegido. | Débito técnico | P3 | 0,5 dia | Garantir testes com filtros ativos | consultar corte vigente |
| D148 (G5) | Extrair cálculo de vigência temporal de parcelas em helper compartilhado [backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:46] — duplicado entre DespesaVariavelService e ReceitaVariavelService. — preencherDataFim permanece em ambos services, chamando lógica duplicada nas entidades. Extração de helper ainda possível. | Débito técnico | P3 | 1–2 dias | D173; preservar testes de datas | consultar corte vigente |
| D152 (G5) | Centralize duplicate validarEObterConta and validarEObterCategoria across transaction services into shared validation component — validarEObterConta/Categoria continua repetido nos quatro services financeiros. Extração deve preservar checagem de proprietário. | Débito técnico | P3 | 1–2 dias | Testes TenantValidation e REST | consultar corte vigente |
| D159 (G5) | Padronizar retorno dos métodos criar entre controllers (ResponseEntity vs DTO direto) — Criar retorna DTO direto em cadastros e ResponseEntity nos financeiros. Todos hoje retornam 200; diferença de assinatura Java não é defeito HTTP. | Melhoria opcional | P3 | 0,5–1 dia | Decidir junto D077/D085 | consultar corte vigente |
| D173 (G5) | Extração de abstração/interface comum de parcelamento para DespesaVariavel e ReceitaVariavel [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/] — deferred, pre-existing — DespesaVariavel e ReceitaVariavel mantêm fórmulas próprias de valor total/última parcela. Abstração é opcional; preservar regras específicas de cada entidade. | Débito técnico | P3 | 1–2 dias | Testes de domínio e D148 | consultar corte vigente |
| D175 (G5) | Implementar endpoints REST de busca por ID (GET /{id}) para as demais entidades de domínio (Categoria, Despesa e Receita) [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/] — deferred, pre-existing — Somente ContaController expõe GET /{id}; services têm buscarPorId. Ausência é confirmada; não foi identificado fluxo UI bloqueado. | Melhoria opcional | P3 | 1–2 dias | Definir necessidade do consumidor e contrato 404 | consultar corte vigente |
| D003 (G6) | Add audit attributes to domain classes (Conta, Categoria, Despesa, Receita) — Conta e Categoria no diagrama continuam sem criadoEm/atualizadoEm previstos no DBML. A implementação também não audita via classe base. Separar sincronização documental de introdução de auditoria em produção. | Débito documental | P3 | 1–2 dias | Escolher modelo canônico; D006 | consultar corte vigente |
| D004 (G6) | Sync Despesa attributes between class diagram and DBML — Despesa continua sem localCompra, dataCompra e observacoes no diagrama (DBML os contém). | Débito documental | P3 | 0,5–1 dia | Decisão sobre fonte canônica; D006 | consultar corte vigente |
| D005 (G6) | Sync Receita attributes between class diagram and DBML — Receita continua sem observacoes no diagrama; DBML contém o campo. | Débito documental | P3 | 0,5 dia | Decisão sobre fonte canônica; D006 | consultar corte vigente |
| D048 (G6) | OAuth2LoginSuccessHandler missing audit logging — Handler não atualiza ultimo_acesso nem registra evento auditável. DBML prevê ultimo_acesso; decidir retenção antes de ampliar dados. | Melhoria opcional | P3 | 1–3 dias | Política de auditoria e privacidade | consultar corte vigente |
| D119 (G6) | Missing Auditing fields (createdAt, updatedAt) across entities — Entidades atuais não têm createdAt/updatedAt e classe base só define usuarioId. Amplo escopo de auditoria não se confunde com atualização do diagrama D003. | Débito técnico / auditoria | P3 | 2–4 dias | Definir eventos, clock, migração e backfill | consultar corte vigente |
| D011 (G7) | Add semantic states colors to CSS tokens — Tokens de texto, superfície e borda já existem; tokens semânticos dedicados de sucesso/erro continuam ausentes. Escopo remanescente é parcial, não toda a descrição antiga. | Débito de design | P3 | 0,5–1 dia | Definir paleta semântica | consultar corte vigente |
| D018 (G7) | Interface de carregamento (Loading) não estlizada [frontend/src/App.tsx] — Carregando... continua como div simples. Falta estilo específico; estética não constitui falha funcional. | Melhoria opcional | P3 | 0,5 dia | Definir experiência de carregamento | consultar corte vigente |
| D020 (G7) | Rotas hardcoded ao invés de usar constantes centralizadas [frontend/src/App.tsx] — Rotas permanecem literais em App e Navigation. Centralização é refatoração opcional, sem defeito demonstrado. | Débito técnico | P3 | 0,5–1 dia | Inventário de destinos | consultar corte vigente |
| D042 (G7) | Hardcoded Avatar Fallback — Fallback continua U; /me sequer fornece nome. Personalização precisa ampliar contrato ou adotar outra referência disponível. | Melhoria opcional | P3 | 1–2 dias | Definir dado para inicial e privacidade | consultar corte vigente |
| D055 (G7) | Header CSS hardcoded z-index — Header usa z-[100] e outros componentes usam z-index independentes. Escala compartilhada é melhoria de design, nenhum overlap específico reproduzido. | Melhoria opcional | P3 | 1 dia | Definir camadas UI | consultar corte vigente |
| D102 (G7) | Repetitive `<ProtectedRoute><RequiresDependencies>` wrappers in App.tsx. — Quatro rotas repetem ProtectedRoute/RequiresDependencies. Refatoração opcional, guard atual existe. | Débito técnico | P3 | 0,5–1 dia | Cobrir D098 antes da refatoração | consultar corte vigente |
| D130 (G7) | Duplicated toggle switch and SVG icon markup. — Switch Ver Todos permanece repetido entre páginas. Não há defeito funcional demonstrado; extração seria manutenção. | Débito técnico | P3 | 1–2 dias | Preservar teclado e semântica cobertos | consultar corte vigente |
| D171 (G7) | Adicionar contador visual de caracteres (0/300) em ObservacoesField [frontend/src/components/MovimentacaoForm/ObservacoesField.tsx:37] — deferred, pre-existing — Textarea tem maxLength=300, sem contador visual. Ausência não é falha de validação. | Melhoria opcional | P3 | 0,5 dia | Decidir UX do contador | consultar corte vigente |
| D178 (G7) | Unificação do componente DeleteConfirmModal compartilhado entre as páginas em vez de implementações duplicadas [frontend-web/src/pages/] — deferred, pre-existing — DeleteConfirmModal continua específico e semelhante em cada página. Extração pode reduzir deriva de comportamento, não há defeito só pela repetição. | Débito técnico | P3 | 1–2 dias | Preservar mutações e reset de erro | consultar corte vigente |

## Resumo das classificações

| Classificação | Deferred-work | Action items | Total de registros |
|---|---:|---:|---:|
| Pendente confirmado | 91 | 4 | 95 |
| Já resolvido, com evidência | 43 | 7 | 50 |
| Duplicado | 22 | 3 | 25 |
| Obsoleto ou inaplicável | 7 | 0 | 7 |
| Necessita esclarecimento ou validação adicional | 27 | 1 | 28 |
| **Total** | **190** | **15** | **205** |

Contagem por registro de origem: duplicados ficam na sua própria classe e não são contados outra vez como pendentes. Duplicados herdam a situação substantiva do representante. Itens compostos com entrega parcial estão explicitados no inventário; não se presume encerramento dos subitens. O quadro development_status é inventariado separadamente ao final como estado de planejamento, sem tratá-lo como 35 novos problemas.

## Método, evidência e limites

- Inspeção estática de código, configurações, assertivas de testes, modelos, specs arquivadas e retrospectivas; histórico Git somente local. Nenhum teste, build, migração, aplicação, banco, rede ou operação remota foi executado. “Resolvido” significa evidência de implementação/cobertura no checkout, não certificação de execução desta sessão ou implantação.
- Ausências de testes foram conferidas na árvore `backend-api/src/test` e nos arquivos `.test.*` de `frontend-web/src`, além da leitura de assertivas. O arquivo citado mostra o contrato/suíte atual; a ausência não é provada por uma linha isolada. Não se equipara existência de suíte a cobertura total.
- Caminhos históricos `frontend/src`, `backend/src`, `frontend/...` e `scripts/validate.sh` foram confrontados com `frontend-web`, `backend-api` e `scripts/run-unit-tests.sh`. Specs 4–6 foram localizadas em `archive/epic-*`; referências originais são preservadas abaixo, sem repará-las.
- A linha física 353 de deferred-work contém separadores `\n` literais: D133 e D134 foram tratados como dois apontamentos, preservando referência à mesma linha física (a origem exata consta no inventário).
- D001 inclui inspeção visual da imagem PNG, que não tem linhas; a comparação textual cita DBML:19. Modelo conceitual/DBML ainda diverge do físico, tratado separadamente.
- Skill bmad-party-mode aplicada em modo session com o elenco pedido. Configuração lida com Python em modo sem bytecode após uv falhar antes da execução por cache somente leitura. Nenhum hook de ativação configurado. Memória e lembrança HTML não foram escritas, respeitando a única saída autorizada.
- Estado inicial de `git status --short`: limpo. Na triagem inicial, a única saída foi este relatório. Após autorização posterior, os dois backlogs foram organizados conforme a seção de sincronização; código, testes, dependências e configurações permanecem sem alterações.

## Discussão das perspectivas e divergências que afetam decisão

📊 **Mary:** “A01 e A10 mostram por que precisamos ler o código: `done` não encerra a ordem das abas nem transforma índices em agregação SQL. Já D001 ficou para trás: a imagem contém os campos OAuth.”

💻 **Amelia:** “A10 aponta para D139. Não proponho reimplementar testes de FK ou isolamento já presentes. Em A04, formulários comuns existem, mas os hooks continuam próprios; encerrar tudo exige esclarecer a intenção da ação.”

🏗️ **Winston:** “Priorizaria planejar D155/A13 e D167 antes de ampliar persistência. Para D139, não basta filtrar a janela de 24 meses: o saldo histórico precisa dos meses anteriores. Uma otimização ingênua muda os números.”

📋 **John:** “Discordo de promover toda otimização a urgência. Sem volume e tempo medidos, D136/D137 ficam P3; D139 é P2 sugerido pelo custo estrutural e compromisso anterior, condicionado a benchmark para dimensionar. Auditoria, paginação e GET por ID também precisam benefício confirmado.”

🧪 **Murat:** “Quero os testes dos contratos de sessão e submissão primeiro. E2E ausente é lacuna real, mas a decisão pós-V1 é explícita. Também não aceito chamar `data-testid` de requisito de qualidade: precisamos provar comportamento, não decorar DOM.”

📋 **John:** “Para E2E, mantenho P3 até haver CI e estratégia de autenticação controlada. Guilherme pode escolher antecipar, mas o relatório não revoga essa decisão por ele.”

🏗️ **Winston:** “Quanto ao reset de queries D168, a integração está ausente, porém QueryErrorResetBoundary não implica automaticamente limpar dados inválidos em cache. A solução precisa demonstrar o cenário e escolher reset/refetch adequado.”

📊 **Mary:** “D106/D156 são decisões de negócio: preservar histórico após excluir recorrência e tratar data desconhecida precisam regra humana. A equipe não vai inventar data inicial nem política de exclusão.”

**Divergências preservadas:** segurança/integridade (D013/D150) têm P1 sugerido por impacto; otimizações sem medição ficam condicionadas; cobertura E2E não é requisito imediato; A04 pode estar parcialmente entregue conforme interpretação de hooks; D168 confirma ausência da integração, sem afirmar que o mecanismo sugerido sozinho resolve todo erro de cache. Essas prioridades históricas não decidem inclusão; aplicar o corte vigente acima.

## Mapa explícito de duplicações

Cada linha conserva o registro original. Duplicados que apontam para resolvidos/obsoletos não viram pendências. Sobreposições parciais (D072/D180, D083/D126, D022/D059, A13/D155/D161 e D099/D100/D188) permanecem explícitas, sem soma de esforço duplicada.

| Registro duplicado | Representante | Situação substantiva |
|---|---|---|
| D044 | D037 | Pendente confirmado |
| D047 | D039 | Já resolvido |
| D052 | D040 | Já resolvido |
| D056 | D021 | Já resolvido |
| D057 | D009 | Já resolvido |
| D068 | D012 | Pendente confirmado |
| D071 | D042 | Pendente confirmado |
| D078 | D074 | Já resolvido |
| D080 | D075 | Pendente confirmado |
| D081 | D073 | Necessita esclarecimento |
| D101 | D095 | Obsoleto/inaplicável |
| D109 | D104 | Já resolvido |
| D110 | D106 | Necessita esclarecimento |
| D111 | D105 | Pendente confirmado; escopo geral também D117 |
| D140 | D134 | Pendente confirmado |
| D141 | D133 | Já resolvido |
| D142 | D139 | Pendente confirmado |
| D143 | D134 | Pendente confirmado |
| D158 | D151 | Pendente confirmado |
| D162 | D139 | Pendente confirmado |
| D165 | D135 | Pendente confirmado |
| D174 | D154 | Pendente confirmado |
| A10 | D139 | Pendente confirmado apesar de done |
| A12 | D163 | Já resolvido |
| A14 | D168 | Pendente confirmado |

## Inventário completo com origem e evidência

IDs D seguem a ordem física dos apontamentos; IDs A seguem action_items. Cada entrada mantém descrição original e source_spec/ref (ou cabeçalho da revisão), além do ID original e status de action item. Relações de sobreposição não significam duplicação exata: registros compostos foram mantidos quando o escopo diferia.

### D001 — Regenerate aureus-erd.png

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:576](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:576)
- **Referência original:** `none`
- **Seção de origem:** Registros iniciais sem seção
- **Evidência alegada no registro (histórica):** The static image is out of sync with the updated DBML after adding OAuth2 fields.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Documentação.
- **Evidência atual verificável:** [_bmad-output/planning-artifacts/technical/data-model/aureus-erd.dbml:19](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/planning-artifacts/technical/data-model/aureus-erd.dbml:19).
- **Justificativa / limite:** A imagem foi inspecionada visualmente e já mostra provider_issuer, provider_subject, imagem_url e ultimo_acesso presentes no DBML. A alegação original de falta dos campos OAuth não persiste; alinhamento global com JPA é outra questão (D006).
- **Relações:** —.

### D002 — Add session/refresh token entity to data and domain models

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:579](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:579)
- **Referência original:** `none`
- **Seção de origem:** Registros iniciais sem seção
- **Evidência alegada no registro (histórica):** Required for full OAuth2 implementation, but out of scope for the immediate structural sync.
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Proposta arquitetural.
- **Evidência atual verificável:** [_bmad-output/planning-artifacts/decisions/2026-08-13-autenticacao-v1.md:15](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/planning-artifacts/decisions/2026-08-13-autenticacao-v1.md:15); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44).
- **Justificativa / limite:** Não há entidade persistida de sessão/refresh, mas o fluxo vigente é stateless com JWT em cookie. Falta decidir renovação/revogação e se o requisito ainda existe; OAuth não exige por si só esta entidade.
- **Relações:** D007/D008.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D003 — Add audit attributes to domain classes (Conta, Categoria, Despesa, Receita)

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:582](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:582)
- **Referência original:** `none`
- **Seção de origem:** Registros iniciais sem seção
- **Evidência alegada no registro (histórica):** These classes are missing criadoEm/atualizadoEm which exist in the DBML.
- **Classificação:** Pendente confirmado. **Natureza:** Débito documental.
- **Evidência atual verificável:** [_bmad-output/planning-artifacts/technical/domain-model/class-diagram.md:13](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/planning-artifacts/technical/domain-model/class-diagram.md:13); [_bmad-output/planning-artifacts/technical/data-model/aureus-erd.dbml:19](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/planning-artifacts/technical/data-model/aureus-erd.dbml:19).
- **Justificativa / limite:** Conta e Categoria no diagrama continuam sem criadoEm/atualizadoEm previstos no DBML. A implementação também não audita via classe base. Separar sincronização documental de introdução de auditoria em produção.
- **Relações:** D119/D048.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D004 — Sync Despesa attributes between class diagram and DBML

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:585](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:585)
- **Referência original:** `none`
- **Seção de origem:** Registros iniciais sem seção
- **Evidência alegada no registro (histórica):** Class diagram is missing localCompra, dataCompra, and observacoes that are defined in DBML.
- **Classificação:** Pendente confirmado. **Natureza:** Débito documental.
- **Evidência atual verificável:** [_bmad-output/planning-artifacts/technical/domain-model/class-diagram.md:13](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/planning-artifacts/technical/domain-model/class-diagram.md:13); [_bmad-output/planning-artifacts/technical/data-model/aureus-erd.dbml:19](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/planning-artifacts/technical/data-model/aureus-erd.dbml:19).
- **Justificativa / limite:** Despesa continua sem localCompra, dataCompra e observacoes no diagrama (DBML os contém).
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D005 — Sync Receita attributes between class diagram and DBML

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:588](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:588)
- **Referência original:** `none`
- **Seção de origem:** Registros iniciais sem seção
- **Evidência alegada no registro (histórica):** Class diagram is missing observacoes defined in DBML.
- **Classificação:** Pendente confirmado. **Natureza:** Débito documental.
- **Evidência atual verificável:** [_bmad-output/planning-artifacts/technical/domain-model/class-diagram.md:13](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/planning-artifacts/technical/domain-model/class-diagram.md:13); [_bmad-output/planning-artifacts/technical/data-model/aureus-erd.dbml:19](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/planning-artifacts/technical/data-model/aureus-erd.dbml:19).
- **Justificativa / limite:** Receita continua sem observacoes no diagrama; DBML contém o campo.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D006 — Resolve structural discrepancy for TipoMovimento

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:591](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:591)
- **Referência original:** `none`
- **Seção de origem:** Registros iniciais sem seção
- **Evidência alegada no registro (histórica):** DBML stores fixed/variable directly on transactions, while class diagram abstracts it via ContextoFinanceiro.
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Divergência de modelos.
- **Evidência atual verificável:** [_bmad-output/planning-artifacts/technical/domain-model/class-diagram.md:13](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/planning-artifacts/technical/domain-model/class-diagram.md:13); [_bmad-output/planning-artifacts/technical/data-model/aureus-erd.dbml:19](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/planning-artifacts/technical/data-model/aureus-erd.dbml:19); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:50](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:50); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavel.java:130](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavel.java:130).
- **Justificativa / limite:** TipoMovimento via ContextoFinanceiro permanece no modelo conceitual; DBML usa discriminador e JPA usa quatro entidades/tabelas. Não é comprovado defeito de execução. Falta definir se diagramas são conceituais ou devem refletir o esquema físico.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D007 — Story 1.2 - Autenticação via Google (OAuth 2.0) e Tratamento de Erros

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:594](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:594)
- **Referência original:** `none`
- **Seção de origem:** Registros iniciais sem seção
- **Evidência alegada no registro (histórica):** Separado do Epic 1 completo para manter o foco na criação do esqueleto visual primeiro.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Funcionalidade.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44).
- **Justificativa / limite:** OAuth Google possui handlers e configuração ativa. Esta entrega existe no código; não implica cobertura de todas as falhas (D049/D061).
- **Relações:** —.

### D008 — Story 1.3 - Proteção de Sessão e Isolamento de Dados

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:597](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:597)
- **Referência original:** `none`
- **Seção de origem:** Registros iniciais sem seção
- **Evidência alegada no registro (histórica):** Separado do Epic 1 completo para manter o foco na criação do esqueleto visual primeiro.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Funcionalidade.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:33](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:33); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:57](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:57); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206).
- **Justificativa / limite:** Filtro popula contexto a partir do JWT, limpa em finally e testes REST exercitam vínculos de outro tenant. A proteção implementada é verificável; lacunas específicas ficam em D035/D037.
- **Relações:** —.

### D009 — Story 1.4 - Encerrar Sessão (Logout) e Limpeza de Estado

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:600](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:600)
- **Referência original:** `none`
- **Seção de origem:** Registros iniciais sem seção
- **Evidência alegada no registro (histórica):** Separado do Epic 1 completo para manter o foco na criação do esqueleto visual primeiro.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Funcionalidade.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:42](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:42); [frontend-web/src/components/Header/Header.tsx:13](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Header/Header.tsx:13).
- **Justificativa / limite:** Endpoint expira cookie; Header limpa QueryClient e authStore e navega ao login. Não equivale a revogação de JWT roubado nem a cobertura completa.
- **Relações:** —; origens duplicadas: D057.

### D010 — Add Navigation icons

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:603](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:603)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-1-esqueleto-visual-barra-navegacao.md`
- **Seção de origem:** Registros iniciais sem seção
- **Evidência alegada no registro (histórica):** The UX design requires a bottom navigation bar with 5 icons, but the current implementation lacks icons.
- **Classificação:** Já resolvido, com evidência. **Natureza:** UI.
- **Evidência atual verificável:** [frontend-web/src/components/Navigation/Navigation.tsx:55](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Navigation/Navigation.tsx:55).
- **Justificativa / limite:** Os cinco links têm ícones lucide; existem ícones mobile.
- **Relações:** —.

### D011 — Add semantic states colors to CSS tokens

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:606](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:606)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-1-esqueleto-visual-barra-navegacao.md`
- **Seção de origem:** Registros iniciais sem seção
- **Evidência alegada no registro (histórica):** Essential color tokens such as text colors, secondary backgrounds, and semantic states (success/error) are missing.
- **Classificação:** Pendente confirmado. **Natureza:** Débito de design.
- **Evidência atual verificável:** [frontend-web/src/index.css:3](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/index.css:3).
- **Justificativa / limite:** Tokens de texto, superfície e borda já existem; tokens semânticos dedicados de sucesso/erro continuam ausentes. Escopo remanescente é parcial, não toda a descrição antiga.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D012 — Add automated tests for client-side routing

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:609](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:609)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-1-esqueleto-visual-barra-navegacao.md`
- **Seção de origem:** Registros iniciais sem seção
- **Evidência alegada no registro (histórica):** Unverified navigation link paths and client-side routing paths without component integration tests.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [frontend-web/src/App.test.tsx:59](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.test.tsx:59).
- **Justificativa / limite:** O teste nominal de navegação só verifica texto na raiz; outro testa ErrorBoundary em /categorias. Faltam navegação real e renderização saudável das rotas não raiz.
- **Relações:** D068/D098; origens duplicadas: D068.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D013 — Fix permissive authorization fallback and protect Actuator endpoints

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:612](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:612)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-encerrar-sessao.md`
- **Seção de origem:** Registros iniciais sem seção
- **Evidência alegada no registro (histórica):** SecurityConfig currently uses `.anyRequest().permitAll()`, which inadvertently exposes unmatched endpoints, including Spring Boot Actuator, without authentication.
- **Classificação:** Pendente confirmado. **Natureza:** Defeito de proteção.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44); [backend-api/pom.xml:147](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/pom.xml:147).
- **Justificativa / limite:** anyRequest().permitAll() permanece e Actuator é dependência. Confirma fallback permissivo; exposição concreta de cada endpoint operacional depende da configuração de implantação, não foi sondada.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D014 — Implement backend unit tests for AuthController

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:615](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:615)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-encerrar-sessao.md`
- **Seção de origem:** Registros iniciais sem seção
- **Evidência alegada no registro (histórica):** Review caught that the logout endpoint lacks test coverage.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:87](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:87).
- **Justificativa / limite:** Há integração para status de /me e logout, porém não suíte unitária AuthController nem assertiva completa de perfil e atributos do cookie de logout. Entrega parcial.
- **Relações:** D025/D022.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D015 — Implement frontend unit tests for Navigation component

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:618](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:618)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-encerrar-sessao.md`
- **Seção de origem:** Registros iniciais sem seção
- **Evidência alegada no registro (histórica):** Review caught that the handleLogout logic in the frontend lacks test coverage.
- **Classificação:** Obsoleto ou inaplicável. **Natureza:** Lacuna histórica.
- **Evidência atual verificável:** [frontend-web/src/components/Navigation/Navigation.tsx:55](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Navigation/Navigation.tsx:55); [frontend-web/src/components/Header/Header.tsx:13](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Header/Header.tsx:13).
- **Justificativa / limite:** handleLogout migrou para Header; Navigation apenas contém links. Testar logout em Navigation não se aplica. A cobertura de Header continua em D026.
- **Relações:** D026/D012.

### D016 — Configure global defaults for React QueryClient

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:621](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:621)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-encerrar-sessao.md`
- **Seção de origem:** Registros iniciais sem seção
- **Evidência alegada no registro (histórica):** The QueryClient in main.tsx uses out-of-the-box settings, missing optimized defaults like staleTime and refetchOnWindowFocus.
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria opcional.
- **Evidência atual verificável:** [frontend-web/src/main.tsx:8](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/main.tsx:8).
- **Justificativa / limite:** QueryClient continua sem defaultOptions. Defaults não são defeito por si; política de freshness/retry é melhoria a decidir.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D017 — Configure secure attribute for session cookie based on environment

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:624](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:624)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-encerrar-sessao.md`
- **Seção de origem:** Registros iniciais sem seção
- **Evidência alegada no registro (histórica):** AuthController hardcodes secure(false) for the AUREUS_SESSION cookie, which must be enabled dynamically for production.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Configuração de segurança.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:42](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:42); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61).
- **Justificativa / limite:** secure(false) não existe nesses fluxos; secure(request.isSecure()) usado em login/logout. Funcionamento atrás de proxy TLS exige validação de ambiente fora desta triagem.
- **Relações:** —.

### D018 — Interface de carregamento (Loading) não estlizada [frontend/src/App.tsx]

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:629](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:629)
- **Referência original:** `Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria opcional.
- **Evidência atual verificável:** [frontend-web/src/App.tsx:22](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.tsx:22).
- **Justificativa / limite:** Carregando... continua como div simples. Falta estilo específico; estética não constitui falha funcional.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D019 — Redirecionamento de login não preserva o estado de rota prévia (`location.state`) [frontend/src/App.tsx:47]

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:630](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:630)
- **Referência original:** `Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna funcional.
- **Evidência atual verificável:** [frontend-web/src/App.tsx:22](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.tsx:22); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61).
- **Justificativa / limite:** ProtectedRoute redireciona sem state.from e OAuth retorna ao frontendUrl fixo configurado. Rota anterior não é preservada.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D020 — Rotas hardcoded ao invés de usar constantes centralizadas [frontend/src/App.tsx]

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:631](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:631)
- **Referência original:** `Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Classificação:** Pendente confirmado. **Natureza:** Débito técnico.
- **Evidência atual verificável:** [frontend-web/src/App.tsx:73](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.tsx:73); [frontend-web/src/components/Navigation/Navigation.tsx:55](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Navigation/Navigation.tsx:55).
- **Justificativa / limite:** Rotas permanecem literais em App e Navigation. Centralização é refatoração opcional, sem defeito demonstrado.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D021 — Ausência de Error Boundary genérico para falhas do React [frontend/src/App.tsx]

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:632](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:632)
- **Referência original:** `Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Classificação:** Já resolvido, com evidência. **Natureza:** Resiliência.
- **Evidência atual verificável:** [frontend-web/src/App.tsx:73](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.tsx:73); [frontend-web/src/App.test.tsx:59](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.test.tsx:59); [frontend-web/src/components/ErrorBoundary/ErrorBoundary.tsx:48](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/ErrorBoundary/ErrorBoundary.tsx:48).
- **Justificativa / limite:** App envolve rotas em ErrorBoundary e teste provoca erro de renderização para verificar fallback.
- **Relações:** —; origens duplicadas: D056.

### D022 — Ausência de testes end-to-end e componentes para o Auth Fetch e Logout

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:633](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:633)
- **Referência original:** `Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [frontend-web/src/App.test.tsx:59](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.test.tsx:59); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:87](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:87); [docs/architecture/testing.md:149](/home/guilhermepagio/developer/workspace/projeto-aureus/docs/architecture/testing.md:149).
- **Justificativa / limite:** Parte backend existe; faltam testes de componentes de auth/logout e suíte ponta a ponta. E2E foi adiado formalmente pós-V1, não resolvido.
- **Relações:** D014/D026/A11.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D023 — Ausência de verificação contra bypass de rota protegida

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:634](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:634)
- **Referência original:** `Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [frontend-web/src/App.test.tsx:59](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.test.tsx:59); [frontend-web/src/App.tsx:22](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.tsx:22).
- **Justificativa / limite:** AuthStore está sempre mockado autenticado no App.test; ausência de casos de rota anônima/carregando permite regressão do guard.
- **Relações:** D012.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D024 — Missing verification for Google profile picture extraction

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:635](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:635)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-correcao-botao-sair.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** No assertion checks that the picture attribute from OAuth2User is correctly mapped and saved to the user entity.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31).
- **Justificativa / limite:** picture é mapeado e salvo no handler; os testes security atuais não simulam sucesso OAuth nem assertam esse mapeamento.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D025 — Missing verification for profile picture inclusion in /me response

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:638](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:638)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-correcao-botao-sair.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** No test covers this endpoint or its consumption in the frontend.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:87](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:87).
- **Justificativa / limite:** SecurityIT verifica /me anônimo/autenticado e logout; não verifica fotoPerfil no /me com usuário persistido nem consumo real no frontend.
- **Relações:** D014/D026.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D026 — Missing verification for Header logout and profile UI

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:641](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:641)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-correcao-botao-sair.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** No frontend test checks that Header displays the user profile image or that logout triggers the flow.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [frontend-web/src/components/Header/Header.tsx:13](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Header/Header.tsx:13); [frontend-web/src/App.test.tsx:59](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.test.tsx:59).
- **Justificativa / limite:** Header real aparece no App.test mas não há assertivas de imagem, referrerPolicy, logout, cache limpo ou estado pendente.
- **Relações:** D045/D046.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D027 — Unnecessary Database Writes on Login

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:644](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:644)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-correcao-botao-sair.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** OAuth2LoginSuccessHandler saves the user entity unconditionally on every login.
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria opcional.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41).
- **Justificativa / limite:** save(usuario) continua incondicional; reduzir escritas depende de comparar atributos. Não há medida de custo ou falha.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D028 — Inefficient Profile Fetching

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:647](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:647)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-correcao-botao-sair.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** AuthController.me() queries the database on every check.
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Otimização proposta.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29).
- **Justificativa / limite:** /me consulta findById em cada verificação. Falta medir frequência/custo e decidir frescor; cache não deve ser imposto sem requisito.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D029 — Direct Repository Access in AuthController

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:650](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:650)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-correcao-botao-sair.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** Bypasses the service layer.
- **Classificação:** Pendente confirmado. **Natureza:** Débito técnico.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29).
- **Justificativa / limite:** AuthController ainda injeta UsuarioRepository diretamente. APIs financeiras foram extraídas, auth não.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D030 — Missing Strongly Typed DTO in AuthController

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:653](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:653)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-correcao-botao-sair.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** /api/auth/me endpoint returns a loosely typed Map<String, Object>.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Contrato API.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29).
- **Justificativa / limite:** /me retorna UserProfileResponseDTO, não Map.
- **Relações:** —.

### D031 — Hardcoded frontend redirect URI in OAuth2LoginSuccessHandler.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:656](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:656)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** The URL `http://localhost:5173/` is hardcoded instead of being loaded from application properties.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Configuração.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41); [backend-api/src/main/resources/application.yaml:24](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/application.yaml:24).
- **Justificativa / limite:** frontendUrl vem de @Value e FRONTEND_URL em application.yaml.
- **Relações:** —.

### D032 — Overly broad exception catching in JwtAuthenticationFilter.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:659](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:659)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** The filter catches generic `Exception` instead of specific JWT exceptions, potentially hiding system errors.
- **Classificação:** Pendente confirmado. **Natureza:** Débito de diagnóstico.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:33](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:33).
- **Justificativa / limite:** catch Exception permanece no trecho de validação/autenticação. Evita erro JWT escapar, mas também oculta falha inesperada.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D033 — Empty Response Body on 401 Unauthorized in JwtAuthenticationFilter.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:662](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:662)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** The filter sets status 401 but writes no JSON body, making frontend error handling harder.
- **Classificação:** Pendente confirmado. **Natureza:** Débito de contrato.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29).
- **Justificativa / limite:** 401 vazio continua no entry point e /me. Filtro hoje limpa cookie e segue cadeia; descrição antiga da escrita de 401 no próprio filtro mudou.
- **Relações:** D157.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D034 — Potential Token and Cookie Expiration Mismatch.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:665](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:665)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** The `AUREUS_SESSION` cookie uses a hardcoded 86400 maxAge which might drift from the JWT expiration property.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Segurança.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61).
- **Justificativa / limite:** maxAge usa jwtUtil.getExpiration()/1000; já não há TTL independente fixo.
- **Relações:** —.

### D035 — Missing verification for Tenant context population.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:668](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:668)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** There are no unit or integration tests verifying that `JwtAuthenticationFilter` populates and clears the `TenantContext`.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:57](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:57); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31).
- **Justificativa / limite:** Código faz set/clear, mas SecurityIT verifica respostas HTTP, sem assertiva direta de limpeza de TenantContext entre requisições/falhas.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D036 — Missing verification for Session cookie SameSite security attribute.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:671](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:671)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** There are no tests verifying that `OAuth2LoginSuccessHandler` emits a cookie with `SameSite=Lax`.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31).
- **Justificativa / limite:** sameSite Lax está implementado; falta assertiva de emissão de cookie no sucesso OAuth.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D037 — Missing verification for Hibernate tenant resolution.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:674](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:674)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** There are no tests verifying that `CurrentTenantIdentifierResolverImpl` correctly returns the value from `TenantContext`.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206).
- **Justificativa / limite:** Integração comprova isolamento em cenários exercitados; falta teste direto do contrato resolver com tenant nulo/vazio/preenchido e validateExistingCurrentSessions.
- **Relações:** —; origens duplicadas: D044.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D038 — Loss of Spring Context in Tenant Resolver

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:677](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:677)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** application.yaml changed tenant_identifier_resolver to a fully qualified class name, bypassing Spring context.
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Hipótese arquitetural.
- **Evidência atual verificável:** [backend-api/src/main/resources/application.yaml:8](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/application.yaml:8); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206).
- **Justificativa / limite:** Resolver por nome de classe permanece, porém é sem estado e consulta TenantContext estático; perder injeção Spring não demonstra falha. Falta demonstrar dependência real de bean e verificar inicialização do Hibernate.
- **Relações:** D037.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D039 — Security Vulnerability via URL Token Transmission

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:681](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:681)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** OAuth2LoginSuccessHandler removed Cookie import, potentially exposing JWT.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Segurança.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61).
- **Justificativa / limite:** JWT é emitido em Set-Cookie HttpOnly, não em URL. Redirect usa apenas frontendUrl.
- **Relações:** —; origens duplicadas: D047.

### D040 — Missing JWT Validation Error Handling

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:685](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:685)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** JwtUtil removed JwtException catch, potentially leading to 500s instead of 401s.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Segurança.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:33](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:33); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31).
- **Justificativa / limite:** Exceção de validação é capturada no filtro e tokens expirados/malformados têm casos 401 em SecurityIT. Não é necessário catch em JwtUtil para este contrato.
- **Relações:** —; origens duplicadas: D052.

### D041 — Unrecoverable Profile Image Error State

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:689](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:689)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** Header.tsx uses setImgError(true) but does not reset it if the profileImage prop changes.
- **Classificação:** Pendente confirmado. **Natureza:** Defeito UI.
- **Evidência atual verificável:** [frontend-web/src/components/Header/Header.tsx:13](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Header/Header.tsx:13); [frontend-web/src/components/Header/Header.tsx:69](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Header/Header.tsx:69).
- **Justificativa / limite:** imgError fica true após onError e nenhum efeito o reseta quando profileImage muda. Uma nova imagem permanece ocultada.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D042 — Hardcoded Avatar Fallback

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:693](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:693)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** Header.tsx renders a hardcoded "U" instead of a dynamic initial.
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria opcional.
- **Evidência atual verificável:** [frontend-web/src/components/Header/Header.tsx:69](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Header/Header.tsx:69); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29).
- **Justificativa / limite:** Fallback continua U; /me sequer fornece nome. Personalização precisa ampliar contrato ou adotar outra referência disponível.
- **Relações:** —; origens duplicadas: D071.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D043 — Non-Semantic DOM Structure

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:697](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:697)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** Header.tsx includes an empty div for header-left just to satisfy CSS grid.
- **Classificação:** Obsoleto ou inaplicável. **Natureza:** Hipótese de revisão.
- **Evidência atual verificável:** [frontend-web/src/components/Header/Header.tsx:55](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Header/Header.tsx:55).
- **Justificativa / limite:** Div à esquerda já contém marca Aureus; premissa de div vazio não corresponde ao código atual. Div de layout por si só não comprova violação semântica.
- **Relações:** —.

### D044 — Tenant identifier resolver configuration lacks verification

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:701](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:701)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** No tests exist to assert tenant context is actively resolved and applied.
- **Classificação:** Duplicado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206).
- **Justificativa / limite:** Sobreposição na verificação do resolver; integração já cobre isolamento, lacuna direta fica em D037.
- **Relações:** D037.

### D045 — Profile image referrer policy is unverified

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:705](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:705)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** No component tests check that the img tag receives the referrerPolicy attribute.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [frontend-web/src/components/Header/Header.tsx:69](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Header/Header.tsx:69); [frontend-web/src/App.test.tsx:59](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.test.tsx:59).
- **Justificativa / limite:** referrerPolicy existe na imagem, porém os testes atuais não o verificam.
- **Relações:** D026.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D046 — Application navigation layout composition is unverified

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:709](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:709)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** No tests exist to verify that Navigation is properly rendered within Header.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [frontend-web/src/App.test.tsx:59](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.test.tsx:59); [frontend-web/src/App.tsx:73](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.tsx:73).
- **Justificativa / limite:** App.test renderiza App real e verifica texto da Navigation; App a compõe dentro de Header. Cobertura estrutural básica existe; não cobre logout nem todos os caminhos.
- **Relações:** —.

### D047 — OAuth2LoginSuccessHandler Cookie import removed

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:712](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:712)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** Bypassing standard cookie manipulation or HTTP-only setup for JWTs raises transmission security concerns.
- **Classificação:** Duplicado. **Natureza:** Segurança.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61).
- **Justificativa / limite:** Mesma suspeita de transmissão derivada de remoção de import; ResponseCookie preserva HttpOnly.
- **Relações:** D039.

### D048 — OAuth2LoginSuccessHandler missing audit logging

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:716](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:716)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** Successful authentications do not update a last_login timestamp or record audit events.
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria opcional.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41); [_bmad-output/planning-artifacts/technical/data-model/aureus-erd.dbml:19](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/planning-artifacts/technical/data-model/aureus-erd.dbml:19).
- **Justificativa / limite:** Handler não atualiza ultimo_acesso nem registra evento auditável. DBML prevê ultimo_acesso; decidir retenção antes de ampliar dados.
- **Relações:** D003/D119.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D049 — OAuth2LoginSuccessHandler missing error handling

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:720](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:720)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** Missing logic for when OAuth2 providers return incomplete profiles without required claims.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de robustez.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41).
- **Justificativa / limite:** Claims sub/email são usados sem validação explícita; perfil incompleto pode falhar no save. Falta tratamento controlado dentro do handler.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D050 — UsuarioRepository missing @Repository annotation

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:724](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:724)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** Removal of this annotation disables automatic persistence exception translation.
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Hipótese de revisão.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/repository/UsuarioRepository.java:9](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/repository/UsuarioRepository.java:9).
- **Justificativa / limite:** Interface estende JpaRepository; ausência de @Repository não comprova perda de tradução de exceções em proxy Spring Data. Falta reproduzir contrato alegado; não classificar como defeito apenas pelo import/anotação.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D051 — UsuarioRepository missing google_subject_id index

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:728](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:728)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** findByGoogleSubjectId is on the critical login path, but there's no DB migration creating an index for it.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Banco de dados.
- **Evidência atual verificável:** [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:4](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:4); [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74).
- **Justificativa / limite:** google_subject_id é UNIQUE em V1 e ainda há índice secundário explícito. Falta de índice está resolvida; redundância é D161.
- **Relações:** —.

### D052 — JwtUtil JwtException import removed

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:732](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:732)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** Suggests explicit handling of token validation errors like expiration or malformed signatures was removed.
- **Classificação:** Duplicado. **Natureza:** Segurança.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:33](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:33); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D040.

### D053 — Tenant isolation missing tests

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:736](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:736)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** multitenancy setup in application.yaml lacks test verification for cross-tenant data isolation.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/AbstractIntegrationTest.java:25](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/AbstractIntegrationTest.java:25).
- **Justificativa / limite:** MultiTenancyIT exercita leitura/alteração/exclusão e vínculos cruzados com Postgres Testcontainers. Existência e assertivas verificadas estaticamente, não reexecutadas.
- **Relações:** —.

### D054 — application.yaml OAuth2 hardcoded secrets risk

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:740](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:740)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** security.oauth2.client block does not clearly use placeholder variables for secrets.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Configuração.
- **Evidência atual verificável:** [backend-api/src/main/resources/application.yaml:24](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/application.yaml:24).
- **Justificativa / limite:** Credenciais OAuth usam GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET; defaults fake não são credenciais reais. Segurança da implantação não auditada.
- **Relações:** —.

### D055 — Header CSS hardcoded z-index

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:744](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:744)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** z-index: 100 used without a centralized scale, risking overlaps with other components.
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria opcional.
- **Evidência atual verificável:** [frontend-web/src/components/Header/Header.tsx:55](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Header/Header.tsx:55); [frontend-web/src/components/ui/Modal.tsx:205](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/ui/Modal.tsx:205).
- **Justificativa / limite:** Header usa z-[100] e outros componentes usam z-index independentes. Escala compartilhada é melhoria de design, nenhum overlap específico reproduzido.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D056 — Entire React application unmounts on route rendering error [frontend/src/App.tsx:68]

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:749](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:749)
- **Referência original:** `Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- **Classificação:** Duplicado. **Natureza:** Resiliência.
- **Evidência atual verificável:** [frontend-web/src/App.tsx:73](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.tsx:73); [frontend-web/src/App.test.tsx:59](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.test.tsx:59); [frontend-web/src/components/ErrorBoundary/ErrorBoundary.tsx:48](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/ErrorBoundary/ErrorBoundary.tsx:48).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D021.

### D057 — Missing Backend Logout Endpoint (belongs to Story 1.4)

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:752](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:752)
- **Referência original:** `Deferred from: code review of spec-1-2-autenticacao-via-google-oauth-2-0-e-tratamento-de-erros.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-2-autenticacao-via-google-oauth-2-0-e-tratamento-de-erros.md (2026-08-18)
- **Classificação:** Duplicado. **Natureza:** Funcionalidade.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:42](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:42); [frontend-web/src/components/Header/Header.tsx:13](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Header/Header.tsx:13).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D009.

### D058 — Incomplete CSRF Setup on Frontend

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:753](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:753)
- **Referência original:** `Deferred from: code review of spec-1-2-autenticacao-via-google-oauth-2-0-e-tratamento-de-erros.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-2-autenticacao-via-google-oauth-2-0-e-tratamento-de-erros.md (2026-08-18)
- **Classificação:** Já resolvido, com evidência. **Natureza:** Segurança.
- **Evidência atual verificável:** [frontend-web/src/services/apiClient.ts:101](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/services/apiClient.ts:101); [frontend-web/src/services/apiClient.test.ts:143](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/services/apiClient.test.ts:143).
- **Justificativa / limite:** Cliente central injeta X-XSRF-TOKEN em mutações e testes verificam POST/PUT/DELETE; Header usa esse cliente.
- **Relações:** —.

### D059 — Missing automated tests for auth flows

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:754](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:754)
- **Referência original:** `Deferred from: code review of spec-1-2-autenticacao-via-google-oauth-2-0-e-tratamento-de-erros.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-2-autenticacao-via-google-oauth-2-0-e-tratamento-de-erros.md (2026-08-18)
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:87](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:87); [frontend-web/src/App.test.tsx:59](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.test.tsx:59); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41).
- **Justificativa / limite:** SecurityIT cobre JWT/CSRF e status auth, mas não login Google completo e frontend logout; registro amplo parcialmente atendido.
- **Relações:** D022/D024/D026.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D060 — Missing frontend interception of 401 errors during API access

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:755](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:755)
- **Referência original:** `Deferred from: code review of spec-1-2-autenticacao-via-google-oauth-2-0-e-tratamento-de-erros.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-2-autenticacao-via-google-oauth-2-0-e-tratamento-de-erros.md (2026-08-18)
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna funcional.
- **Evidência atual verificável:** [frontend-web/src/services/apiClient.ts:176](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/services/apiClient.ts:176); [frontend-web/src/App.tsx:22](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.tsx:22).
- **Justificativa / limite:** apiClient converte 401 em ApiError; não há interceptor que limpe sessão e redirecione após 401 durante uso. App consulta auth apenas no efeito inicial.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D061 — Concurrent OAuth logins for same new user could cause DataIntegrityViolationException

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:756](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:756)
- **Referência original:** `Deferred from: code review of spec-1-2-autenticacao-via-google-oauth-2-0-e-tratamento-de-erros.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-2-autenticacao-via-google-oauth-2-0-e-tratamento-de-erros.md (2026-08-18)
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Risco de concorrência.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41); [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:4](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:4).
- **Justificativa / limite:** find-then-save continua e UNIQUE pode rejeitar corrida. Precisa reprodução controlada com dois callbacks e política de retry; não foi executado teste concorrente.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D062 — Missing Automated Tests

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:759](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:759)
- **Referência original:** `Deferred from: code review of spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md (2026-08-18)
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Lacuna de teste mal delimitada.
- **Evidência atual verificável:** [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206).
- **Justificativa / limite:** Apontamento genérico na revisão 1.3 não define cenários. Há testes atuais; faltam critérios para afirmar cobertura completa ou duplicação exata.
- **Relações:** D035/D036/D037/D053.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D063 — Lack of Asynchronous Thread Context Propagation

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:760](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:760)
- **Referência original:** `Deferred from: code review of spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md (2026-08-18)
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Requisito condicional.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:57](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:57).
- **Justificativa / limite:** ThreadLocal não propaga para outras threads. Nenhum fluxo assíncrono específico foi identificado como consumidor; falta cenário que exija propagação. Não trocar por InheritableThreadLocal sem contrato.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D064 — Vazamento de contexto no TenantContext: Usando ThreadLocal padrão.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:763](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:763)
- **Referência original:** `Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)
- **Classificação:** Já resolvido, com evidência. **Natureza:** Segurança.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:57](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:57).
- **Justificativa / limite:** finally limpa TenantContext e SecurityContextHolder, inclusive se cadeia lança exceção. ThreadLocal padrão sozinho não implica vazamento. Teste direto permanece em D035.
- **Relações:** —.

### D065 — Omissão de acessibilidade no dropdown do Header (Esc): Falta listener para a tecla Escape.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:764](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:764)
- **Referência original:** `Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)
- **Classificação:** Pendente confirmado. **Natureza:** Defeito de acessibilidade.
- **Evidência atual verificável:** [frontend-web/src/components/Header/Header.tsx:13](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Header/Header.tsx:13).
- **Justificativa / limite:** Header fecha por pointerdown, sem listener Escape.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D066 — Preenchimento extra no mobile para rodapé: O .main-content tem padding inferior de 80px herdado da antiga navegação.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:765](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:765)
- **Referência original:** `Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)
- **Classificação:** Obsoleto ou inaplicável. **Natureza:** Layout.
- **Evidência atual verificável:** [frontend-web/src/App.tsx:22](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.tsx:22); [frontend-web/src/components/Navigation/Navigation.tsx:55](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Navigation/Navigation.tsx:55).
- **Justificativa / limite:** Navegação mobile atual é fixed bottom-0 com 64px + safe area; padding 80px reserva espaço real. Não é herança sem função. Ajuste fino depende validação visual, não remover automaticamente.
- **Relações:** —.

### D067 — Ausência de testes de integração e endpoints backend: verification-gap acusou falta de testes para controllers e filtros criados.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:766](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:766)
- **Referência original:** `Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)`
- **Seção de origem:** Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Lacuna parcialmente atendida.
- **Evidência atual verificável:** [backend-api/src/test/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaControllerTest.java:1](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaControllerTest.java:1); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:87](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:87).
- **Justificativa / limite:** Controllers financeiros/filtro JWT têm testes atuais; AuthController não tem unitários e OAuth não é exercitado. Registro amplo não pode ser encerrado em bloco.
- **Relações:** D014/D024/D035.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D068 — Adicionar testes de navegação para certificar que rotas não-raiz renderizam o conteúdo esperado.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:767](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:767)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-uso-tailwind.md`
- **Seção de origem:** Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** Surfaced by verification-gap review; App.test.tsx apenas verifica a rota /.
- **Classificação:** Duplicado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [frontend-web/src/App.test.tsx:59](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.test.tsx:59).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D012.

### D069 — Adicionar teste automatizado E2E ou de artefatos para verificar se os estilos do Tailwind são processados e aplicados com sucesso na build.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:770](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:770)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-uso-tailwind.md`
- **Seção de origem:** Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** Surfaced by verification-gap review; O projeto atual não valida o CSS final processado.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [frontend-web/package.json:6](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/package.json:6); [frontend-web/src/index.css:3](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/index.css:3); [scripts/run-unit-tests.sh:199](/home/guilhermepagio/developer/workspace/projeto-aureus/scripts/run-unit-tests.sh:199).
- **Justificativa / limite:** Build compila CSS mas nenhuma assertiva inspeciona CSS gerado/aplicação visual Tailwind. Não confundir build com verificação visual.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D070 — Corrigir a11y: adicionar aria-live e role="status" ao estado de carregamento e aria-controls ao botão de perfil.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:773](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:773)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-uso-tailwind.md`
- **Seção de origem:** Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** Surfaced by blind-hunter review.
- **Classificação:** Pendente confirmado. **Natureza:** Defeito de acessibilidade.
- **Evidência atual verificável:** [frontend-web/src/App.tsx:22](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.tsx:22); [frontend-web/src/components/Header/Header.tsx:69](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Header/Header.tsx:69).
- **Justificativa / limite:** Carregando do guard não tem role/status/aria-live; perfil tem aria-expanded/haspopup mas não aria-controls. Remanescente verificado.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D071 — Utilizar a inicial do usuário logado no avatar padrão em vez de 'U' fixo.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:776](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:776)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-uso-tailwind.md`
- **Seção de origem:** Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** Surfaced by blind-hunter review.
- **Classificação:** Duplicado. **Natureza:** Melhoria opcional.
- **Evidência atual verificável:** [frontend-web/src/components/Header/Header.tsx:69](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Header/Header.tsx:69); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D042.

### D072 — Adicionar testes de unidade e integração (Backend e Frontend) para Contas

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:779](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:779)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** Nenhuma cobertura de teste foi adicionada na implementação da story 2.1.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [frontend-web/src/pages/Contas/ContasPage.test.tsx:81](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/Contas/ContasPage.test.tsx:81); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:89](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:89); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206).
- **Justificativa / limite:** Backend já tem unidade e integração; frontend testa abertura e exclusão, mas não submissão criar/editar Conta nem invalidação real. Pendência parcial.
- **Relações:** D180/D083.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D073 — Adicionar métodos equals() e hashCode() na entidade Conta

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:782](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:782)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** Conta não possui implementação customizada recomendada pelo JPA.
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Recomendação de modelagem.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Conta.java:20](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Conta.java:20); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9).
- **Justificativa / limite:** Conta não customiza equals/hashCode; não foi localizado uso que dependa de igualdade entre instâncias. Falta identificar cenário e estratégia de ID, sem presumir obrigação JPA.
- **Relações:** —; origens duplicadas: D081.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D074 — Implementar Focus Trap acessível e completo no Modal

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:785](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:785)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** Componente genérico Modal.tsx não possui restrição de foco para teclado.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Acessibilidade.
- **Evidência atual verificável:** [frontend-web/src/components/ui/Modal.tsx:128](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/ui/Modal.tsx:128).
- **Justificativa / limite:** Modal contém trap Tab/Shift+Tab, Escape, foco inicial e restauração. Casos verificáveis também em Modal.test.tsx; não houve avaliação assistiva real.
- **Relações:** —; origens duplicadas: D078.

### D075 — Adicionar suporte à paginação no backend e listagem de Contas

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:788](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:788)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** O endpoint /api/contas retorna todos os registros de uma vez.
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria de escala.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:42](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:42); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23).
- **Justificativa / limite:** Listagem retorna List completa sem Page/Pageable e frontend consome coleção. Risco de volume, sem benchmark de lentidão.
- **Relações:** —; origens duplicadas: D080.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D076 — Adicionar tratamento e exibição de erro da API com parsing estruturado no frontend

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:791](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:791)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)
- **Evidência alegada no registro (histórica):** useContas.ts lança erros genéricos sem extrair mensagens do body de erro.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Contrato cliente.
- **Evidência atual verificável:** [frontend-web/src/services/apiClient.ts:176](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/services/apiClient.ts:176); [frontend-web/src/services/apiClient.test.ts:143](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/services/apiClient.test.ts:143).
- **Justificativa / limite:** apiClient extrai message/errors/fieldErrors e hooks propagam error.message ao toast. Mapeamento por campo no formulário é D079.
- **Relações:** —.

### D077 — Endpoint criar Conta retorna 200 OK em vez de 201 Created.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:796](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:796)
- **Referência original:** `Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md
- **Classificação:** Pendente confirmado. **Natureza:** Débito de contrato.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:42](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:42).
- **Justificativa / limite:** POST Conta retorna DTO direto e 200; ainda não 201. Contrato atual é testado como 200, portanto mudança exige intenção explícita.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D078 — Modal genérico não possui Focus Trap acessível.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:797](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:797)
- **Referência original:** `Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md
- **Classificação:** Duplicado. **Natureza:** Acessibilidade.
- **Evidência atual verificável:** [frontend-web/src/components/ui/Modal.tsx:128](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/ui/Modal.tsx:128).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D074.

### D079 — Tratamento de erros de validação server-side não mapeados no formulário.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:798](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:798)
- **Referência original:** `Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de UX.
- **Evidência atual verificável:** [frontend-web/src/pages/Contas/components/ContaFormModal.tsx:32](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/Contas/components/ContaFormModal.tsx:32); [frontend-web/src/services/apiClient.ts:176](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/services/apiClient.ts:176).
- **Justificativa / limite:** Formulário gerencia erro local obrigatório; mutações não mapeiam fieldErrors do servidor aos campos. Toast de mensagem existe, não substitui associação por campo.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D080 — Endpoint de listagem de contas não é paginado.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:799](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:799)
- **Referência original:** `Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md
- **Classificação:** Duplicado. **Natureza:** Melhoria de escala.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:42](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:42); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D075.

### D081 — Entidade Conta sem implementação customizada de equals() e hashCode().

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:800](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:800)
- **Referência original:** `Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md
- **Classificação:** Duplicado. **Natureza:** Recomendação de modelagem.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Conta.java:20](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Conta.java:20); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D073.

### D082 — Modal não se adapta dinamicamente para Bottom Sheet em mobile.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:801](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:801)
- **Referência original:** `Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Melhoria UX condicional.
- **Evidência atual verificável:** [frontend-web/src/components/ui/Modal.tsx:205](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/ui/Modal.tsx:205).
- **Justificativa / limite:** Modal é centralizado em todos breakpoints; bottom sheet não está implementado. Falta validar se é requisito atual e impacto em mobile, antes de rotular defeito.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D083 — Ausência de testes verificando isolamento cross-tenant e invalidação de cache.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:802](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:802)
- **Referência original:** `Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206); [frontend-web/src/pages/Contas/ContasPage.test.tsx:81](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/Contas/ContasPage.test.tsx:81).
- **Justificativa / limite:** Isolamento cross-tenant já exercitado em backend; invalidação real de cache Contas/Categorias não é testada com QueryClient nos testes de página (hooks mockados).
- **Relações:** D072/D126.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D084 — Ausência de testes para contrato de violação de FK na deleção.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:803](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:803)
- **Referência original:** `Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md
- **Classificação:** Já resolvido, com evidência. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:89](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:89).
- **Justificativa / limite:** RestContractAndIntegrityIT tenta excluir Conta/Categoria vinculadas e verifica 400 com payload estruturado.
- **Relações:** —.

### D085 — Endpoint criar Categoria retorna 200 OK em vez de 201 Created.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:806](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:806)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-2/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
- **Evidência alegada no registro (histórica):** Pre-existing pattern from Conta Controller; low impact for now.
- **Classificação:** Pendente confirmado. **Natureza:** Débito de contrato.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java:34](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java:34).
- **Justificativa / limite:** POST Categoria retorna DTO/200, sem 201. Melhoria de contrato; testes atuais esperam 200.
- **Relações:** D077.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D086 — Entidade Categoria sem métodos equals() e hashCode().

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:809](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:809)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-2/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
- **Evidência alegada no registro (histórica):** Pre-existing pattern; low impact for current detached entity usage.
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Recomendação de modelagem.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Categoria.java:20](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Categoria.java:20).
- **Justificativa / limite:** Categoria sem equals/hashCode customizados. Falta uso problemático concreto para justificar mudança de semântica de identidade.
- **Relações:** D073.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D087 — O endpoint de exclusão assume 400 apenas como FK violation.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:812](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:812)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-2/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
- **Evidência alegada no registro (histórica):** Out of scope to overhaul error parsing; validation details might be masked.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Tratamento de erro.
- **Evidência atual verificável:** [frontend-web/src/pages/Categorias/components/DeleteConfirmModal.tsx:14](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/Categorias/components/DeleteConfirmModal.tsx:14); [frontend-web/src/hooks/useCategorias.ts:14](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/hooks/useCategorias.ts:14); [frontend-web/src/services/apiClient.ts:176](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/services/apiClient.ts:176).
- **Justificativa / limite:** Fluxo atual não decide toda mensagem por status 400: hook usa error.message derivado de payload/erro. Toast é genérico no sentido visual, não máscara fixa FK.
- **Relações:** —.

### D088 — Listagem de Categorias não possui paginação.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:815](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:815)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-2/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
- **Evidência alegada no registro (histórica):** Frontend and backend fetch all records at once, risky for large tenants.
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria de escala.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java:34](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java:34); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java:23).
- **Justificativa / limite:** Categoria lista coleção inteira sem paginação. Ausência confirmada, lentidão não medida.
- **Relações:** D075/D105.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D089 — Falta de tratamento de timeout para requisições frontend.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:818](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:818)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-2/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
- **Evidência alegada no registro (histórica):** Network hangs indefinitely instead of aborting.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Resiliência.
- **Evidência atual verificável:** [frontend-web/src/services/apiClient.ts:140](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/services/apiClient.ts:140).
- **Justificativa / limite:** Cliente central tem AbortController, timeout e erro 408; teste correspondente em apiClient.test.ts:298.
- **Relações:** —.

### D090 — Testes automatizados ausentes para Categoria.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:821](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:821)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-2/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
- **Evidência alegada no registro (histórica):** Multi-tenant isolation, FK constraint errors, and frontend cache invalidation have no automated tests.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:89](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:89); [frontend-web/src/hooks/useCategorias.ts:68](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/hooks/useCategorias.ts:68).
- **Justificativa / limite:** Isolamento/FK têm integração; falta cobertura frontend Categoria e invalidação real do cache. Não considerar tudo ausente nem tudo resolvido.
- **Relações:** D083.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D091 — Missing Location header in Categoria creation.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:826](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:826)
- **Referência original:** `Deferred from: code review (2026-08-20) - code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) - code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria de contrato.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java:34](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java:34).
- **Justificativa / limite:** POST Categoria não envia Location. Definir 201/URI do recurso junto com eventual GET por ID.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D092 — useCategorias.ts lacks AbortSignal.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:827](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:827)
- **Referência original:** `Deferred from: code review (2026-08-20) - code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) - code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
- **Classificação:** Pendente confirmado. **Natureza:** Débito de cancelamento.
- **Evidência atual verificável:** [frontend-web/src/hooks/useCategorias.ts:14](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/hooks/useCategorias.ts:14); [frontend-web/src/services/apiClient.ts:140](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/services/apiClient.ts:140).
- **Justificativa / limite:** apiClient suporta signal, mas fetchCategorias não recebe/encaminha signal da query. Timeout já existe; cancelamento da query não.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D093 — Optimistic locking (missing @Version).

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:828](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:828)
- **Referência original:** `Deferred from: code review (2026-08-20) - code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) - code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Melhoria de concorrência.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Conta.java:20](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Conta.java:20); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Categoria.java:20](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Categoria.java:20).
- **Justificativa / limite:** Nenhum @Version nas entidades avaliadas. Falta decisão de produto sobre conflito de edição e cenário concorrente; não assumir lost update como defeito sem contrato.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D094 — Improve UX by checking both accounts and categories simultaneously to avoid a "waterfall" of empty states.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:829](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:829)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-3-restricao-granular-de-cadastro-sem-dependencias.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) - code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
- **Evidência alegada no registro (histórica):** A user missing both will first see the warning for accounts, fix it, and then unexpectedly face a second block for categories.
- **Classificação:** Já resolvido, com evidência. **Natureza:** UX.
- **Evidência atual verificável:** [frontend-web/src/components/RequiresDependencies.tsx:55](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:55).
- **Justificativa / limite:** Estado vazio atual informa simultaneamente conta/categoria e oferece ambos links quando faltam ambos.
- **Relações:** —.

### D095 — Add data-testid attributes to loading, error, and empty states.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:832](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:832)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-3-restricao-granular-de-cadastro-sem-dependencias.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) - code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
- **Evidência alegada no registro (histórica):** Automated testing will be brittle without semantic data-testid attributes.
- **Classificação:** Obsoleto ou inaplicável. **Natureza:** Sugestão de teste.
- **Evidência atual verificável:** [frontend-web/src/components/RequiresDependencies.tsx:13](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:13); [frontend-web/src/components/RequiresDependencies.tsx:55](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:55).
- **Justificativa / limite:** data-testid não é requisito funcional nem condição necessária de testes robustos: roles/textos podem ser seletores. Ausência de testes do guard é D098; adicionar IDs por padrão não resolve essa lacuna.
- **Relações:** —; origens duplicadas: D101.

### D096 — Add a Retry button to the error state.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:835](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:835)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-3-restricao-granular-de-cadastro-sem-dependencias.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) - code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
- **Evidência alegada no registro (histórica):** Currently, the user is forced to manually refresh the entire application if the initial fetch fails.
- **Classificação:** Já resolvido, com evidência. **Natureza:** UX.
- **Evidência atual verificável:** [frontend-web/src/components/RequiresDependencies.tsx:37](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:37).
- **Justificativa / limite:** Há botão Tentar novamente disparando os dois refetch.
- **Relações:** —.

### D097 — Improve error state UI to match the polished, card-based layouts of the empty states.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:838](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:838)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-3-restricao-granular-de-cadastro-sem-dependencias.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) - code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
- **Evidência alegada no registro (histórica):** The error state UI is a simple line of text, which feels visually inconsistent.
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Melhoria visual.
- **Evidência atual verificável:** [frontend-web/src/components/RequiresDependencies.tsx:37](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:37); [frontend-web/src/components/RequiresDependencies.tsx:55](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:55).
- **Justificativa / limite:** Estados erro e vazio já compartilham estrutura p-6/max-w-lg/text-center; falta critério visual para declarar inconsistência remanescente ou necessidade de card.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D098 — Add automated tests to verify dependency guards block access to transaction routes.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:841](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:841)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-3-restricao-granular-de-cadastro-sem-dependencias.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) - code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
- **Evidência alegada no registro (histórica):** The test suite would not fail if a developer removed the RequiresDependencies wrapper, allowing users to access transaction pages without required data.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [frontend-web/src/App.test.tsx:59](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.test.tsx:59); [frontend-web/src/App.tsx:73](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.tsx:73).
- **Justificativa / limite:** Não há teste que remova contas/categorias e confirme bloqueio das quatro rotas. App.test só testa raiz/falha de categorias.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D099 — Missing `role="status"` on loading state.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:846](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:846)
- **Referência original:** `Deferred from: code review of spec-2-3-restricao-granular-de-cadastro-sem-dependencias (2026-08-20)`
- **Seção de origem:** Deferred from: code review of spec-2-3-restricao-granular-de-cadastro-sem-dependencias (2026-08-20)
- **Classificação:** Pendente confirmado. **Natureza:** Defeito de acessibilidade.
- **Evidência atual verificável:** [frontend-web/src/components/RequiresDependencies.tsx:13](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:13).
- **Justificativa / limite:** Loading de RequiresDependencies não possui role=status.
- **Relações:** D188.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D100 — Missing `role="alert"` on error state.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:847](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:847)
- **Referência original:** `Deferred from: code review of spec-2-3-restricao-granular-de-cadastro-sem-dependencias (2026-08-20)`
- **Seção de origem:** Deferred from: code review of spec-2-3-restricao-granular-de-cadastro-sem-dependencias (2026-08-20)
- **Classificação:** Pendente confirmado. **Natureza:** Defeito de acessibilidade.
- **Evidência atual verificável:** [frontend-web/src/components/RequiresDependencies.tsx:37](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:37).
- **Justificativa / limite:** Falha do guard não possui role=alert.
- **Relações:** D188.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D101 — Missing `data-testid` attributes on conditional UI states.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:848](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:848)
- **Referência original:** `Deferred from: code review of spec-2-3-restricao-granular-de-cadastro-sem-dependencias (2026-08-20)`
- **Seção de origem:** Deferred from: code review of spec-2-3-restricao-granular-de-cadastro-sem-dependencias (2026-08-20)
- **Classificação:** Duplicado. **Natureza:** Sugestão de teste.
- **Evidência atual verificável:** [frontend-web/src/components/RequiresDependencies.tsx:13](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:13); [frontend-web/src/components/RequiresDependencies.tsx:55](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:55).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D095.

### D102 — Repetitive `<ProtectedRoute><RequiresDependencies>` wrappers in App.tsx.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:849](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:849)
- **Referência original:** `Deferred from: code review of spec-2-3-restricao-granular-de-cadastro-sem-dependencias (2026-08-20)`
- **Seção de origem:** Deferred from: code review of spec-2-3-restricao-granular-de-cadastro-sem-dependencias (2026-08-20)
- **Classificação:** Pendente confirmado. **Natureza:** Débito técnico.
- **Evidência atual verificável:** [frontend-web/src/App.tsx:73](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.tsx:73).
- **Justificativa / limite:** Quatro rotas repetem ProtectedRoute/RequiresDependencies. Refatoração opcional, guard atual existe.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D103 — Scope creep / missing `aria-hidden="true"`: Unrequested additions of `lucide-react` icons in Header.tsx.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:850](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:850)
- **Referência original:** `Deferred from: code review of spec-2-3-restricao-granular-de-cadastro-sem-dependencias (2026-08-20)`
- **Seção de origem:** Deferred from: code review of spec-2-3-restricao-granular-de-cadastro-sem-dependencias (2026-08-20)
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Apontamento composto.
- **Evidência atual verificável:** [frontend-web/src/components/Header/Header.tsx:69](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Header/Header.tsx:69).
- **Justificativa / limite:** Escopo da introdução de ícones é julgamento histórico, não defeito atual. Ícones lucide exigem conferir SVG produzido para concluir aria-hidden; não foi renderizado navegador nem inspecionada dependência. Separar intenção de escopo e saída DOM.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D104 — Missing Relationship Ownership Validation (verify if related conta/categoria belong to user).

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:851](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:851)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
- **Seção de origem:** Deferred from: code review of spec-2-3-restricao-granular-de-cadastro-sem-dependencias (2026-08-20)
- **Evidência alegada no registro (histórica):** Not specific to this story, applies globally to existing entities too.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Segurança.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:52](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:52); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206).
- **Justificativa / limite:** Services verificam dono de conta/categoria antes de associar; MockMvc testa 403 para vínculo de outro tenant.
- **Relações:** —; origens duplicadas: D109.

### D105 — Missing Pagination for /api/despesas-fixas and /api/receitas-fixas.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:854](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:854)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
- **Seção de origem:** Deferred from: code review of spec-2-3-restricao-granular-de-cadastro-sem-dependencias (2026-08-20)
- **Evidência alegada no registro (histórica):** Table grows large over time, missing in other controllers as well.
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria de escala.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29).
- **Justificativa / limite:** Endpoints fixos retornam List sem paginação; ausência confirmada, sem volume de referência.
- **Relações:** D075/D117; origens duplicadas: D111.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D106 — Clarify if fixed items need an end date (dataFim) or active flag to prevent history mutation on deletion.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:857](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:857)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
- **Seção de origem:** Deferred from: code review of spec-2-3-restricao-granular-de-cadastro-sem-dependencias (2026-08-20)
- **Evidência alegada no registro (histórica):** Currently deletion removes the item entirely, which might break historical monthly projections. Needs product definition.
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Decisão de produto.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:80](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:80); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43).
- **Justificativa / limite:** Fixas têm dataInicio, não dataFim/soft-delete. Exclusão física permanece. Falta política de histórico/vigência para definir comportamento esperado; não implementar preservação automaticamente.
- **Relações:** —; origens duplicadas: D110.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D107 — Missing backend integration tests for new endpoints.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:860](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:860)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
- **Seção de origem:** Deferred from: code review of spec-2-3-restricao-granular-de-cadastro-sem-dependencias (2026-08-20)
- **Evidência alegada no registro (histórica):** Project currently lacks test infrastructure, deferring per Epic 1 retro items.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:89](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:89).
- **Justificativa / limite:** Integração atual testa endpoints fixos, vínculos e integridade. Expansão exaustiva é D176.
- **Relações:** —.

### D108 — Missing frontend component tests for DespesasFixasPage and ReceitasFixasPage.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:863](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:863)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
- **Seção de origem:** Deferred from: code review of spec-2-3-restricao-granular-de-cadastro-sem-dependencias (2026-08-20)
- **Evidência alegada no registro (histórica):** Project currently lacks test infrastructure, deferring per Epic 1 retro items.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [frontend-web/src/pages/DespesasFixas/DespesasFixasPage.tsx:1](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/DespesasFixas/DespesasFixasPage.tsx:1); [frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx:273](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx:273).
- **Justificativa / limite:** Wrappers dos formulários fixos têm testes; páginas DespesasFixasPage/ReceitasFixasPage não têm suíte dedicada de listagem, filtro e ações.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D109 — Falta de validação de propriedade (Tenant) nos relacionamentos informados (conta/categoria)

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:868](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:868)
- **Referência original:** `Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md
- **Classificação:** Duplicado. **Natureza:** Segurança.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:52](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:52); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D104.

### D110 — Mutação de histórico financeiro em exclusões (falta de dataFim/soft-delete)

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:869](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:869)
- **Referência original:** `Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md
- **Classificação:** Duplicado. **Natureza:** Decisão de produto.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:80](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:80); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D106.

### D111 — Falta de paginação nos endpoints `GET` de listagem

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:870](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:870)
- **Referência original:** `Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md
- **Classificação:** Duplicado. **Natureza:** Melhoria de escala.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29).
- **Justificativa / limite:** Mesmo problema de listagens sem paginação; este registro amplia escopo para endpoints GET em geral (D075/D088/D117).
- **Relações:** D105.

### D112 — Falta de cobertura de testes automatizados (integração e UI)

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:871](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:871)
- **Referência original:** `Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Lacuna parcialmente atendida.
- **Evidência atual verificável:** [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206); [frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx:273](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx:273).
- **Justificativa / limite:** Integração e formulários fixos hoje têm testes, páginas fixas ainda não. Registro genérico requer separar cenários para eventual encerramento.
- **Relações:** D107/D108.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D113 — Controllers return JPA Entities directly instead of using DTOs, risking Mass Assignment

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:872](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:872)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md
- **Evidência alegada no registro (histórica):** Found in DespesaVariavelController and ReceitaVariavelController (Pre-existing project pattern)
- **Classificação:** Já resolvido, com evidência. **Natureza:** Arquitetura API.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/DespesaVariavelRequestDTO.java:32](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/DespesaVariavelRequestDTO.java:32).
- **Justificativa / limite:** Controllers e services variáveis usam Request/Response DTOs; não expõem entidades como contrato de entrada/saída.
- **Relações:** —.

### D114 — Duplicated date logic (preencherDataFim) in controllers instead of centralizing in service or lifecycle hook

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:875](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:875)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md
- **Evidência alegada no registro (histórica):** Found repeated in both variable controllers
- **Classificação:** Já resolvido, com evidência. **Natureza:** Arquitetura.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelService.java:43).
- **Justificativa / limite:** preencherDataFim foi retirado dos controllers e colocado nos services; helper ainda duplica entre services (D148).
- **Relações:** —.

### D115 — Missing DB indexes for dataInicio and dataFim which will be heavily queried in Epic 4

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:878](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:878)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md
- **Evidência alegada no registro (histórica):** Not explicitly requested in the spec, but necessary for future performance
- **Classificação:** Já resolvido, com evidência. **Natureza:** Banco.
- **Evidência atual verificável:** [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74).
- **Justificativa / limite:** V1 cria índices simples e compostos para data_inicio/data_fim nas tabelas variáveis e data_inicio nas fixas.
- **Relações:** —.

### D116 — Missing class-level validation to ensure dataFim >= dataInicio

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:881](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:881)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md
- **Evidência alegada no registro (histórica):** Although the controller calculates it, programmatic API changes could introduce inconsistencies
- **Classificação:** Obsoleto ou inaplicável. **Natureza:** Validação.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/DespesaVariavelRequestDTO.java:32](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/DespesaVariavelRequestDTO.java:32); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavel.java:130](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavel.java:130).
- **Justificativa / limite:** API não aceita dataFim no request; service calcula por quantidade >=1. Validação cruzada de campo livre é inaplicável ao contrato atual. Escritas programáticas fora do service exigiriam requisito separado.
- **Relações:** —.

### D117 — Unbounded findAll queries in backend list endpoints

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:884](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:884)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md
- **Evidência alegada no registro (histórica):** Pre-existing technical debt across the project (Epic 2, etc.)
- **Classificação:** Pendente confirmado. **Natureza:** Débito de escala.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java:23).
- **Justificativa / limite:** findAll/listas ordenadas continuam sem limite nos services. Abrange listagens variáveis além de D075/D088/D105.
- **Relações:** D075/D088/D105.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D118 — DRY violation in frontend form/list pages for variable expenses and revenues

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:887](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:887)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md
- **Evidência alegada no registro (histórica):** Boilerplate is duplicated between the two modules
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Refatoração parcialmente entregue.
- **Evidência atual verificável:** [frontend-web/src/components/MovimentacaoForm/MovimentacaoVariavelFormModal.tsx:1](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/MovimentacaoForm/MovimentacaoVariavelFormModal.tsx:1); [frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx:273](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx:273); [frontend-web/src/pages/DespesasFixas/DespesasFixasPage.tsx:1](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/DespesasFixas/DespesasFixasPage.tsx:1).
- **Justificativa / limite:** Formulários foram unificados; hooks e páginas continuam específicos e semelhantes. Falta definir quanto da duplicação de listagem deve ser removido; abstrair tudo não é requisito automático.
- **Relações:** A04/D178.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D119 — Missing Auditing fields (createdAt, updatedAt) across entities

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:890](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:890)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md
- **Evidência alegada no registro (histórica):** Technical debt from prior architecture setup
- **Classificação:** Pendente confirmado. **Natureza:** Débito técnico / auditoria.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Conta.java:20](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Conta.java:20); [_bmad-output/planning-artifacts/technical/data-model/aureus-erd.dbml:19](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/planning-artifacts/technical/data-model/aureus-erd.dbml:19).
- **Justificativa / limite:** Entidades atuais não têm createdAt/updatedAt e classe base só define usuarioId. Amplo escopo de auditoria não se confunde com atualização do diagrama D003.
- **Relações:** D003/D048.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D120 — Lack of global @ControllerAdvice for standard constraint violations

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:893](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:893)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md
- **Evidência alegada no registro (histórica):** Pre-existing pattern where controllers individually catch DataIntegrityViolationException
- **Classificação:** Já resolvido, com evidência. **Natureza:** Contrato de erro.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:175](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:175).
- **Justificativa / limite:** GlobalExceptionHandler usa RestControllerAdvice e trata validação/constraint/ResourceNotFound/AccessDenied.
- **Relações:** —.

### D121 — Concurrent deletion causes unhandled 500 EmptyResultDataAccessException

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:896](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:896)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
- **Seção de origem:** Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md
- **Evidência alegada no registro (histórica):** Pre-existing basic CRUD pattern does not handle race conditions gracefully
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Risco de concorrência.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:175](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:175).
- **Justificativa / limite:** existsById seguido de deleteById permanece; handler genérico ainda existe. Falta reproduzir corrida na versão atual e confirmar se delete ausente lança exceção; não afirmar 500 por inferência de versão antiga.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D122 — Missing automated verification tests (dataFim, relational errors, API parsing)

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:901](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:901)
- **Referência original:** `Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)`
- **Seção de origem:** Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Lacuna parcialmente atendida.
- **Evidência atual verificável:** [backend-api/src/test/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelServiceTest.java:106](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelServiceTest.java:106); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206); [frontend-web/src/services/apiClient.test.ts:143](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/services/apiClient.test.ts:143).
- **Justificativa / limite:** Testes de datas/FK/parsing existem. A frase não define matriz completa e não autoriza declarar toda verificação encerrada. Especificar casos residuais.
- **Relações:** D176.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D123 — Missing DTOs, Service Layer, and Global Exception Handler

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:902](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:902)
- **Referência original:** `Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)`
- **Seção de origem:** Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- **Classificação:** Já resolvido, com evidência. **Natureza:** Arquitetura.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:175](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:175).
- **Justificativa / limite:** DTO, service e handler global implementados, com fronteiras e tratamentos atuais verificáveis.
- **Relações:** —.

### D124 — Fetcher swallows non-JSON 500 errors

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:903](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:903)
- **Referência original:** `Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)`
- **Seção de origem:** Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- **Classificação:** Já resolvido, com evidência. **Natureza:** Cliente HTTP.
- **Evidência atual verificável:** [frontend-web/src/services/apiClient.ts:176](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/services/apiClient.ts:176); [frontend-web/src/services/apiClient.test.ts:143](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/services/apiClient.test.ts:143).
- **Justificativa / limite:** Respostas não JSON/HTML recebem fallback por status e testes cobrem ambos.
- **Relações:** —.

### D125 — Fixed transactions lack start/end dates in the domain model, preventing month-based filtering.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:904](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:904)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-4-sincronizacao-de-visao-mensal-invalidacao-de-cache-e-filtro.md`
- **Seção de origem:** Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- **Evidência alegada no registro (histórica):** Fixed transaction components had to bypass the month filtering logic completely because `dataInicio` does not exist on the current entity.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna funcional (entrega parcial).
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:80](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:80); [frontend-web/src/pages/DespesasFixas/DespesasFixasPage.tsx:1](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/DespesasFixas/DespesasFixasPage.tsx:1).
- **Justificativa / limite:** Entrega parcial: entidades fixas já têm dataInicio, mas DespesasFixasPage:29 e ReceitasFixasPage:29 retornam a lista inteira sem considerar mês; tipos/hooks frontend nem incluem dataInicio. A ausência de datas no backend foi resolvida; o filtro mensal que motivou o registro continua pendente.
- **Relações:** D106/D108/D156.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

- **Evidência complementar:** [frontend-web/src/pages/DespesasFixas/DespesasFixasPage.tsx:29](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/DespesasFixas/DespesasFixasPage.tsx:29).
- **Evidência complementar:** [frontend-web/src/pages/ReceitasFixas/ReceitasFixasPage.tsx:29](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/ReceitasFixas/ReceitasFixasPage.tsx:29).
- **Evidência complementar:** [frontend-web/src/hooks/useDespesasFixas.ts:7](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/hooks/useDespesasFixas.ts:7).

### D126 — Missing test coverage for React Query mutation cache invalidations and global month filter logic.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:907](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:907)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-4-sincronizacao-de-visao-mensal-invalidacao-de-cache-e-filtro.md`
- **Seção de origem:** Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- **Evidência alegada no registro (histórica):** No component or hook tests assert that `['consolidacao']` is invalidated or that `isGlobalFilterActive` works correctly.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [frontend-web/src/pages/DespesasVariaveis/DespesasVariaveisPage.test.tsx:138](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/DespesasVariaveis/DespesasVariaveisPage.test.tsx:138); [frontend-web/src/hooks/useCategorias.ts:68](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/hooks/useCategorias.ts:68); [frontend-web/src/pages/Contas/ContasPage.test.tsx:81](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/Contas/ContasPage.test.tsx:81).
- **Justificativa / limite:** Filtro global e selectedMonth têm testes de página; invalidação real de queries após mutação não está exercitada, pois hooks são mockados. Remanescente parcial.
- **Relações:** D083/D090.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D127 — `epic-3-context.md` was translated to English, losing technical specificity and causing context fragmentation with Portuguese PRDs.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:910](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:910)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-4-sincronizacao-de-visao-mensal-invalidacao-de-cache-e-filtro.md`
- **Seção de origem:** Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- **Evidência alegada no registro (histórica):** The document omits explicit constraints like `ON DELETE RESTRICT` for foreign keys that were present in the Portuguese original.
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Documentação histórica.
- **Evidência atual verificável:** [_bmad-output/implementation-artifacts/archive/epic-3/epic-3-context.md:16](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/epic-3-context.md:16); [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:24](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:24).
- **Justificativa / limite:** Contexto arquivado continua em inglês e omite ON DELETE RESTRICT explícito; não há original comparável para provar perda pela tradução. Banco/integração já protegem vínculos. Decidir se contexto arquivado deve ser mantido como guia atual.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D128 — monthStore currentMonth is initialized at module load and can become stale.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:913](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:913)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-4-sincronizacao-de-visao-mensal-invalidacao-de-cache-e-filtro.md`
- **Seção de origem:** Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- **Evidência alegada no registro (histórica):** initialized at top-level instead of inside the store or getter.
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Comportamento temporal.
- **Evidência atual verificável:** [frontend-web/src/store/monthStore.ts:11](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/store/monthStore.ts:11); [frontend-web/src/components/Consolidacao/ConsolidacaoToolbar.tsx:22](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Consolidacao/ConsolidacaoToolbar.tsx:22).
- **Justificativa / limite:** Mês inicial é calculado no módulo; seletor permite escolher e resetar para mês atual. Falta decidir se aplicação aberta deve acompanhar virada de mês ou preservar seleção intencional.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D129 — No UI exists to change the selected month.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:916](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:916)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-4-sincronizacao-de-visao-mensal-invalidacao-de-cache-e-filtro.md`
- **Seção de origem:** Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- **Evidência alegada no registro (histórica):** month picker belongs to Epic 4 (Seleção de mês).
- **Classificação:** Já resolvido, com evidência. **Natureza:** UI.
- **Evidência atual verificável:** [frontend-web/src/components/Consolidacao/ConsolidacaoToolbar.tsx:22](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Consolidacao/ConsolidacaoToolbar.tsx:22); [frontend-web/src/components/Consolidacao/ConsolidacaoToolbar.test.tsx:25](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Consolidacao/ConsolidacaoToolbar.test.tsx:25).
- **Justificativa / limite:** Toolbar navega meses, abre seletor e volta ao mês atual; testes de interação existem.
- **Relações:** —.

### D130 — Duplicated toggle switch and SVG icon markup.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:919](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:919)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-4-sincronizacao-de-visao-mensal-invalidacao-de-cache-e-filtro.md`
- **Seção de origem:** Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- **Evidência alegada no registro (histórica):** identical markup across pages instead of reusable UI components.
- **Classificação:** Pendente confirmado. **Natureza:** Débito técnico.
- **Evidência atual verificável:** [frontend-web/src/pages/DespesasFixas/DespesasFixasPage.tsx:1](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/DespesasFixas/DespesasFixasPage.tsx:1); [frontend-web/src/pages/DespesasVariaveis/DespesasVariaveisPage.test.tsx:138](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/DespesasVariaveis/DespesasVariaveisPage.test.tsx:138).
- **Justificativa / limite:** Switch Ver Todos permanece repetido entre páginas. Não há defeito funcional demonstrado; extração seria manutenção.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D131 — useContas and useCategorias do not invalidate ['consolidacao'].

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:922](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:922)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-4-sincronizacao-de-visao-mensal-invalidacao-de-cache-e-filtro.md`
- **Seção de origem:** Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- **Evidência alegada no registro (histórica):** deleting accounts/categories could affect consolidation cache but wasn't updated here.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Cache.
- **Evidência atual verificável:** [frontend-web/src/hooks/useCategorias.ts:68](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/hooks/useCategorias.ts:68); [frontend-web/src/hooks/useContas.ts:1](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/hooks/useContas.ts:1).
- **Justificativa / limite:** Hooks Contas e Categorias invalidam consolidacao nas mutações. Cobertura do comportamento permanece D083/D126.
- **Relações:** —.

### D132 — Adicionar testes de unidade para ConsolidacaoPage, ConsolidacaoGrid e ConsolidacaoToolbar garantindo que as 24 colunas são renderizadas e o seletor funciona.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:925](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:925)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-4-1-selecao-de-mes-navegacao-temporal-e-grid-com-suporte-a-gesto.md`
- **Seção de origem:** Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- **Evidência alegada no registro (histórica):** Foi identificado que `App.test.tsx` apenas testa o título, e a nova navegação de meses do Painel de Consolidação ficou sem testes de regressão de renderização.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [frontend-web/src/components/Consolidacao/ConsolidacaoGrid.test.tsx:50](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Consolidacao/ConsolidacaoGrid.test.tsx:50); [frontend-web/src/components/Consolidacao/ConsolidacaoToolbar.test.tsx:25](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Consolidacao/ConsolidacaoToolbar.test.tsx:25); [frontend-web/src/pages/Consolidacao/ConsolidacaoPage.tsx:50](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/Consolidacao/ConsolidacaoPage.tsx:50).
- **Justificativa / limite:** Grid e Toolbar têm testes; falta suíte de ConsolidacaoPage para composição, scroll e colapso integrados. Remanescente parcial, não recriar testes existentes.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D133 — O hook useConsolidacao utiliza fetch diretamente, possivelmente contornando clientes de API centralizados como Axios se existirem.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:928](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:928)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-4-2-bloco-de-consolidacao-por-conta-receitas-e-despesas.md`
- **Seção de origem:** Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- **Evidência alegada no registro (histórica):** O código usa window.fetch e extrai CSRF token manualmente.
- **Classificação:** Já resolvido, com evidência. **Natureza:** Cliente HTTP.
- **Evidência atual verificável:** [frontend-web/src/hooks/useConsolidacao.ts:18](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/hooks/useConsolidacao.ts:18).
- **Justificativa / limite:** useConsolidacao usa apiClient.get com params, não fetch direto.
- **Relações:** —; origens duplicadas: D141.

### D134 — O ConsolidacaoService possui o valor de 24 meses hardcoded em vários locais.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:928](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:928)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-4-2-bloco-de-consolidacao-por-conta-receitas-e-despesas.md`
- **Seção de origem:** Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- **Evidência alegada no registro (histórica):** Loops e ArrayLists inicializados com capacidade 24 explicitamente.
- **Classificação:** Pendente confirmado. **Natureza:** Débito técnico.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:223](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:223); [frontend-web/src/components/Consolidacao/ConsolidacaoGrid.tsx:172](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Consolidacao/ConsolidacaoGrid.tsx:172).
- **Justificativa / limite:** 24/23 e colSpan 25 continuam dispersos. Horizonte de 24 meses é requisito; centralizar constantes pode reduzir drift, sem tornar horizonte configurável.
- **Relações:** —; origens duplicadas: D140, D143.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D135 — Manual usuarioId == null check instead of Spring Security filters in ConsolidacaoController.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:929](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:929)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-4-3-bloco-analitico-de-categorias.md`
- **Seção de origem:** Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- **Evidência alegada no registro (histórica):** Controller manually rejects unauthorized access.
- **Classificação:** Pendente confirmado. **Natureza:** Débito técnico.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoController.java:28](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoController.java:28); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44).
- **Justificativa / limite:** ConsolidacaoController mantém if usuarioId==null e 401 vazio apesar do filtro proteger /api. Remoção é refatoração; não significa endpoint desprotegido.
- **Relações:** D033; origens duplicadas: D165.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D136 — O(N*M) loop instead of O(N) in ConsolidacaoService for 24-month projection.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:932](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:932)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-4-3-bloco-analitico-de-categorias.md`
- **Seção de origem:** Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- **Evidência alegada no registro (histórica):** The logic iterates through all transactions 24 times instead of calculating index offsets.
- **Classificação:** Pendente confirmado. **Natureza:** Débito de desempenho.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:114](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:114); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262).
- **Justificativa / limite:** Por conta continua percorrendo cada coleção em cada um dos 24 meses; por categoria usa limites de sobreposição. Alegação global está parcialmente resolvida. Com horizonte fixo ambos são lineares em N com fatores diferentes, não declarar mudança assintótica automaticamente.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D137 — Redundant map lookups in ConsolidacaoService 24-month loop.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:935](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:935)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-4-3-bloco-analitico-de-categorias.md`
- **Seção de origem:** Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- **Evidência alegada no registro (histórica):** start and end dates are repeatedly retrieved from maps for every transaction inside the inner loop.
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria opcional.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:114](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:114).
- **Justificativa / limite:** Maps de início/fim ainda são consultados dentro dos laços mensais por conta. Micro-otimização sem custo medido.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D138 — BlocoCategorias loads independently causing layout shifts.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:938](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:938)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-4-3-bloco-analitico-de-categorias.md`
- **Seção de origem:** Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- **Evidência alegada no registro (histórica):** The block is rendered immediately and manages its own loading state below accounts, causing visual snap.
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Risco visual.
- **Evidência atual verificável:** [frontend-web/src/components/Consolidacao/ConsolidacaoGrid.tsx:172](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Consolidacao/ConsolidacaoGrid.tsx:172).
- **Justificativa / limite:** Conta/categoria usam queries independentes e categoria tem estado próprio. Layout shift real depende de latência/renderização; medir em navegador antes de definir defeito.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D139 — Fetches all transactions regardless of requested period: Unfiltered Database Fetch fetches all transactions without date filtering at DB level.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:943](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:943)
- **Referência original:** `Deferred from: code review (spec-4-3-bloco-analitico-de-categorias.md)`
- **Seção de origem:** Deferred from: code review (spec-4-3-bloco-analitico-de-categorias.md)
- **Classificação:** Pendente confirmado. **Natureza:** Débito de desempenho.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:74](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:74); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262).
- **Justificativa / limite:** Repositories recebem apenas usuarioId, sem período/SQL de agregação. Todos os registros do tenant são carregados para projetar e calcular histórico.
- **Relações:** D162/A10; origens duplicadas: D142, D162, A10.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D140 — Hardcoded 24-month horizon magic numbers: Magic numbers used across frontend and backend.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:944](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:944)
- **Referência original:** `Deferred from: code review (spec-4-3-bloco-analitico-de-categorias.md)`
- **Seção de origem:** Deferred from: code review (spec-4-3-bloco-analitico-de-categorias.md)
- **Classificação:** Duplicado. **Natureza:** Débito técnico.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:223](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:223); [frontend-web/src/components/Consolidacao/ConsolidacaoGrid.tsx:172](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Consolidacao/ConsolidacaoGrid.tsx:172).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D134.

### D141 — Raw fetch and manual CSRF token instead of centralized client: Hook bypasses standard API clients.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:945](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:945)
- **Referência original:** `Deferred from: code review (spec-4-3-bloco-analitico-de-categorias.md)`
- **Seção de origem:** Deferred from: code review (spec-4-3-bloco-analitico-de-categorias.md)
- **Classificação:** Duplicado. **Natureza:** Cliente HTTP.
- **Evidência atual verificável:** [frontend-web/src/hooks/useConsolidacao.ts:18](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/hooks/useConsolidacao.ts:18).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D133.

### D142 — Inefficient in-memory filtering for DespesaVariavel in ConsolidacaoService [ConsolidacaoService.java:165] — pre-existing query pattern across consolidacao services.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:949](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:949)
- **Referência original:** `Deferred from: code review of spec-4-3-bloco-analitico-de-categorias.md (2026-09-05)`
- **Seção de origem:** Deferred from: code review of spec-4-3-bloco-analitico-de-categorias.md (2026-09-05)
- **Classificação:** Duplicado. **Natureza:** Débito de desempenho.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:74](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:74); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262).
- **Justificativa / limite:** Mesmo carregamento integral e corte em memória, explicitado para DespesaVariavel.
- **Relações:** D139.

### D143 — Hardcoded column count (24) in ConsolidacaoGrid.tsx — standard 24-month design layout for Epic 4.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:950](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:950)
- **Referência original:** `Deferred from: code review of spec-4-3-bloco-analitico-de-categorias.md (2026-09-05)`
- **Seção de origem:** Deferred from: code review of spec-4-3-bloco-analitico-de-categorias.md (2026-09-05)
- **Classificação:** Duplicado. **Natureza:** Débito técnico.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:223](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:223); [frontend-web/src/components/Consolidacao/ConsolidacaoGrid.tsx:172](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Consolidacao/ConsolidacaoGrid.tsx:172).
- **Justificativa / limite:** Mesma dispersão de constante, com origem específica no grid; 24 meses é requisito vigente.
- **Relações:** D134.

### D144 — Duplicated month abbreviations between ConsolidacaoPage and ConsolidacaoGrid — non-blocking cosmetic refactoring.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:951](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:951)
- **Referência original:** `Deferred from: code review of spec-4-3-bloco-analitico-de-categorias.md (2026-09-05)`
- **Seção de origem:** Deferred from: code review of spec-4-3-bloco-analitico-de-categorias.md (2026-09-05)
- **Classificação:** Obsoleto ou inaplicável. **Natureza:** Refatoração cosmética.
- **Evidência atual verificável:** [frontend-web/src/pages/Consolidacao/ConsolidacaoPage.tsx:50](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/Consolidacao/ConsolidacaoPage.tsx:50); [frontend-web/src/components/Consolidacao/ConsolidacaoToolbar.tsx:22](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Consolidacao/ConsolidacaoToolbar.tsx:22).
- **Justificativa / limite:** ConsolidacaoPage atual só compõe toolbar/grid e não contém abreviações. A duplicação nomeada entre Page e Grid não persiste; eventual Toolbar/Grid é outro recorte a avaliar.
- **Relações:** —.

### D145 — Fragmented design system colors in ConsolidacaoPage.tsx — pre-existing hex usage in pages.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:952](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:952)
- **Referência original:** `Deferred from: code review of spec-4-3-bloco-analitico-de-categorias.md (2026-09-05)`
- **Seção de origem:** Deferred from: code review of spec-4-3-bloco-analitico-de-categorias.md (2026-09-05)
- **Classificação:** Obsoleto ou inaplicável. **Natureza:** Refatoração cosmética.
- **Evidência atual verificável:** [frontend-web/src/pages/Consolidacao/ConsolidacaoPage.tsx:50](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/Consolidacao/ConsolidacaoPage.tsx:50).
- **Justificativa / limite:** ConsolidacaoPage atual não contém cores hex; cores existem em filhos, mas premissa específica desta página perdeu validade. Não ampliar escopo automaticamente.
- **Relações:** —.

### D146 — Divergência na agregação entre contas e categorias para lançamentos com conta nula [backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:140] — pre-existing logic in category consolidation where null accounts are assigned to synthetic 'Sem Categoria'.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:955](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:955)
- **Referência original:** `Deferred from: code review of spec-4-4-bloco-de-resumo-geral-com-sobra-historica-acumulada.md (2026-09-06)`
- **Seção de origem:** Deferred from: code review of spec-4-4-bloco-de-resumo-geral-com-sobra-historica-acumulada.md (2026-09-06)
- **Classificação:** Já resolvido, com evidência. **Natureza:** Consistência financeira.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:118](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:118); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262); [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:24](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:24).
- **Justificativa / limite:** Conta nula tem linha Sem Conta; categoria é agrupada pela categoria, não pela existência de conta. FKs atuais impedem conta nula em novas movimentações. Não confundir agrupamento legítimo com divergência de total.
- **Relações:** A09.

### D147 — Otimizar consultas de listagem com carregamento antecipado (JOIN FETCH) para evitar consultas N+1 na conversão de DTOs [backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaFixaService.java:35] — pre-existing lazy fetch pattern.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:958](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:958)
- **Referência original:** `Deferred from: code review of spec-5-1-backend-service-layer-e-dtos-records-para-entidades.md (2026-09-15)`
- **Seção de origem:** Deferred from: code review of spec-5-1-backend-service-layer-e-dtos-records-para-entidades.md (2026-09-15)
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Hipótese de desempenho.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43).
- **Justificativa / limite:** Listagem usa relações LAZY e DTO, sem JOIN FETCH. Falta contar SQL/medir N+1; acesso ao ID de proxy não prova consulta adicional. Decidir apenas com tracing/benchmark seguro.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D148 — Extrair cálculo de vigência temporal de parcelas em helper compartilhado [backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:46] — duplicado entre DespesaVariavelService e ReceitaVariavelService.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:959](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:959)
- **Referência original:** `Deferred from: code review of spec-5-1-backend-service-layer-e-dtos-records-para-entidades.md (2026-09-15)`
- **Seção de origem:** Deferred from: code review of spec-5-1-backend-service-layer-e-dtos-records-para-entidades.md (2026-09-15)
- **Classificação:** Pendente confirmado. **Natureza:** Débito técnico.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelService.java:43).
- **Justificativa / limite:** preencherDataFim permanece em ambos services, chamando lógica duplicada nas entidades. Extração de helper ainda possível.
- **Relações:** D114/D173.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D149 — Normalizar retorno genérico ResponseEntity<?> para ResponseEntity<*ResponseDTO> via RestControllerAdvice na Story 5.2 [backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:40] — dependente da Story 5.2.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:960](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:960)
- **Referência original:** `Deferred from: code review of spec-5-1-backend-service-layer-e-dtos-records-para-entidades.md (2026-09-15)`
- **Seção de origem:** Deferred from: code review of spec-5-1-backend-service-layer-e-dtos-records-para-entidades.md (2026-09-15)
- **Classificação:** Já resolvido, com evidência. **Natureza:** Contrato API.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:175](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:175).
- **Justificativa / limite:** Controller fixo usa ResponseEntity<DespesaFixaResponseDTO>; erros saem por handler global.
- **Relações:** —.

### D150 — Alinhar anotação de valor máximo @Max(9999999) na entidade JPA com @DecimalMax(9999999.99) do DTO [backend/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:46] — pre-existing entity annotation.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:961](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:961)
- **Referência original:** `Deferred from: code review of spec-5-1-backend-service-layer-e-dtos-records-para-entidades.md (2026-09-15)`
- **Seção de origem:** Deferred from: code review of spec-5-1-backend-service-layer-e-dtos-records-para-entidades.md (2026-09-15)
- **Classificação:** Pendente confirmado. **Natureza:** Defeito de validação.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:50](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:50); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/DespesaFixaRequestDTO.java:1](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/DespesaFixaRequestDTO.java:1).
- **Justificativa / limite:** Entidade fixa limita a 9999999 com @Max; DTO aceita 9999999.99. Faixa final aceita na API pode falhar na validação JPA. Divergência estática confirmada; não foi gravado dado de borda.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D151 — Adicionar ordenação alfabética (OrderByDescricaoAsc) na listagem de Contas e Categorias [backend/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:28] — pre-existing query pattern.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:962](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:962)
- **Referência original:** `Deferred from: code review of spec-5-1-backend-service-layer-e-dtos-records-para-entidades.md (2026-09-15)`
- **Seção de origem:** Deferred from: code review of spec-5-1-backend-service-layer-e-dtos-records-para-entidades.md (2026-09-15)
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria opcional.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java:23).
- **Justificativa / limite:** Contas/Categorias services usam findAll sem OrderByDescricaoAsc. Ordenação da consolidação por categoria não resolve a listagem CRUD.
- **Relações:** D158; origens duplicadas: D158.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D152 — Centralize duplicate validarEObterConta and validarEObterCategoria across transaction services into shared validation component

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:965](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:965)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-16)
- **Evidência alegada no registro (histórica):** Review surfaced identical helper methods across all 4 transaction services
- **Classificação:** Pendente confirmado. **Natureza:** Débito técnico.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:52](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:52).
- **Justificativa / limite:** validarEObterConta/Categoria continua repetido nos quatro services financeiros. Extração deve preservar checagem de proprietário.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D153 — Optimize foreign key validation queries in transaction services to avoid two roundtrips (findOwnerUsuarioId + findById)

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:968](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:968)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-16)
- **Evidência alegada no registro (histórica):** Currently performs native check then findById, which could be combined
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria de desempenho.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:52](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:52).
- **Justificativa / limite:** Consulta nativa de proprietário seguida de findById permanece. Combinar sem revelar outro tenant exige cuidado. Sem benchmark, não é urgência comprovada.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D154 — Padronizar payload de erro com ApiErrorResponse na exclusão de recurso inexistente (DELETE /{id})

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:971](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:971)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-16)
- **Evidência alegada no registro (histórica):** service.excluir(id) retorna false e controller responde ResponseEntity.notFound().build() sem body
- **Classificação:** Pendente confirmado. **Natureza:** Débito de contrato.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:42](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:42); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java:34](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java:34); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:175](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:175).
- **Justificativa / limite:** DELETE ausente retorna notFound().build(), sem ApiErrorResponse. Há handler ResourceNotFound, mas este ramo não o usa.
- **Relações:** D174; origens duplicadas: D174.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D155 — Criar índices adicionais no banco para chaves estrangeiras (conta_id, categoria_id) e usuario_id em contas/categorias

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:974](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:974)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-16)
- **Evidência alegada no registro (histórica):** V1__create_performance_indexes.sql foca em datas e google_subject_id; chaves estrangeiras não indexadas causam seq scan em deletes
- **Classificação:** Pendente confirmado. **Natureza:** Débito de banco.
- **Evidência atual verificável:** [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74); [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:24](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:24).
- **Justificativa / limite:** V1 indexa datas e usuário nas movimentações, não conta_id/categoria_id nem usuario_id em contas/categorias; nenhuma V2 encontrada. Ganho precisa EXPLAIN em ambiente autorizado.
- **Relações:** A13/D161.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D156 — Alinhar consistência de cálculo histórico no ConsolidacaoService para movimentações sem dataInicio

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:977](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:977)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-16)
- **Evidência alegada no registro (histórica):** Transações sem dataInicio são incluídas na projeção futura de 24 meses mas ignoradas no saldo histórico pré-grade
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Decisão sobre legado.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:173](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:173); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:80](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:80).
- **Justificativa / limite:** Fixas sem dataInicio entram na projeção desde o primeiro mês e ficam fora do histórico. Diferença é real; falta definir desde quando contar legados sem data, evitando saldo arbitrário.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D157 — Padronizar tratamento de erros dos filtros de segurança (Spring Security) no formato ApiErrorResponse

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:980](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:980)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-16)
- **Evidência alegada no registro (histórica):** 401 Unauthorized e 403 CSRF nos filtros de segurança são gerados antes do RestControllerAdvice
- **Classificação:** Pendente confirmado. **Natureza:** Débito de contrato.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29).
- **Justificativa / limite:** EntryPoint 401 e filtros CSRF não estão ligados a ApiErrorResponse; contrato global MVC não alcança todos esses ramos.
- **Relações:** D033.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D158 — Aplicar ordenação alfabética (OrderByDescricaoAsc) em CategoriaService.listar()

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:983](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:983)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-16)
- **Evidência alegada no registro (histórica):** CategoriaService executa findAll() em vez do método ordenado do repositório
- **Classificação:** Duplicado. **Natureza:** Melhoria opcional.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java:23).
- **Justificativa / limite:** Mesma ordenação, recorte específico de Categoria; conservar esse escopo no representante.
- **Relações:** D151.

### D159 — Padronizar retorno dos métodos criar entre controllers (ResponseEntity vs DTO direto)

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:986](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:986)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-16)
- **Evidência alegada no registro (histórica):** Movimentações retornam ResponseEntity<T> enquanto Conta e Categoria retornam DTO puro
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria opcional.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:42](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:42); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java:34](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java:34); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29).
- **Justificativa / limite:** Criar retorna DTO direto em cadastros e ResponseEntity nos financeiros. Todos hoje retornam 200; diferença de assinatura Java não é defeito HTTP.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D160 — Tratar fail-closed estrito para tenant não autenticado no CurrentTenantIdentifierResolverImpl

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:989](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:989)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-16)
- **Evidência alegada no registro (histórica):** Resolver retorna string 'public' quando contexto é nulo, com risco de persistência inadvertida se não autenticado
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Risco de isolamento.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9).
- **Justificativa / limite:** Fallback public permanece; rotas financeiras são autenticadas e testes cruzados existem. Falta validar operações sem contexto e bootstrap para decidir fail-closed sem quebrar login/infra. Não há exploit demonstrado.
- **Relações:** D037/D038.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D161 — Consolidar índice B-tree duplicado em usuarios(google_subject_id)

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:992](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:992)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-16)
- **Evidência alegada no registro (histórica):** A coluna google_subject_id possui constraint UNIQUE na tabela usuarios (criando índice único automaticamente); um índice secundário não-único idx_usuarios_google_subject_id foi criado na migração V1.
- **Classificação:** Pendente confirmado. **Natureza:** Débito de banco.
- **Evidência atual verificável:** [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:4](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:4); [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74).
- **Justificativa / limite:** UNIQUE e índice secundário da mesma coluna coexistem em V1. Não há migração de remoção; custo redundante confirmado no DDL, catálogo real não consultado.
- **Relações:** A13/D155.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D162 — Otimizar consultas do ConsolidacaoService para utilizar os índices de data diretamente no banco via JPQL/SQL ao invés de carga em memória

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:995](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:995)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-16)
- **Evidência alegada no registro (histórica):** findByUsuarioId carrega todo o histórico e calcula vigência de datas em laços Java na memória da aplicação, sem usar índices compostos para corte por data no banco.
- **Classificação:** Duplicado. **Natureza:** Débito de desempenho.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:74](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:74); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D139.

### D163 — Configurar isolamento de banco de dados/Testcontainers ou profile específico para BackendApplicationTests sem exigir container Postgres ativo na porta 5432

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1004](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1004)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-27)
- **Evidência alegada no registro (histórica):** Teste @SpringBootTest falha com conexão recusada se Postgres local não estiver ativo; alocado para infraestrutura de testes no Épico 6 (Story 6.2/6.4)
- **Classificação:** Já resolvido, com evidência. **Natureza:** Infra de teste.
- **Evidência atual verificável:** [backend-api/src/test/java/com/guilhermepagio/aureus/backend/AbstractIntegrationTest.java:25](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/AbstractIntegrationTest.java:25); [backend-api/pom.xml:185](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/pom.xml:185).
- **Justificativa / limite:** BackendApplicationIT usa base Testcontainers com propriedades dinâmicas; antiga dependência do Postgres local foi substituída.
- **Relações:** —; origens duplicadas: A12.

### D164 — Adicionar testes MockMvc verificando HTTP 403 Forbidden nos controllers de movimentação quando tenant for inválido

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1007](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1007)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-27)
- **Evidência alegada no registro (histórica):** Isolamento de tenant testado na Service Layer e no ExceptionHandler; cobertura de integração REST de endpoints alocada na Story 6.2
- **Classificação:** Já resolvido, com evidência. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206).
- **Justificativa / limite:** MultiTenancyIT inclui MockMvc 403 para vínculos cruzados, inclusive despesa fixa/variável. Não comprova exaustividade de toda matriz (D176).
- **Relações:** —.

### D165 — Eliminar checagem manual de autenticação (usuarioId == null) em ConsolidacaoController delegando para o filtro Spring Security

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1010](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1010)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-27)
- **Evidência alegada no registro (histórica):** Padrão manual pré-existente do Épico 4 que retorna 401 vazio
- **Classificação:** Duplicado. **Natureza:** Débito técnico.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoController.java:28](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoController.java:28); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D135.

### D166 — Utilizar a coluna persistida dataFim no ConsolidacaoService ao invés de recalcular a data final das parcelas em memória

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1013](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1013)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-27)
- **Evidência alegada no registro (histórica):** ConsolidacaoService calcula data final em memória com plusMonths() em vez de ler a coluna dataFim indexada
- **Classificação:** Pendente confirmado. **Natureza:** Débito técnico.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:74](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:74); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262).
- **Justificativa / limite:** Consolidação ainda recalcula fim com plusMonths em vez de ler dataFim persistida. Qual fonte deve prevalecer exige invariante consistente.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D167 — Coexistência de hibernate.ddl-auto: update e migrações Flyway em application.yaml

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1016](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1016)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-27)
- **Evidência alegada no registro (histórica):** Configuração de desenvolvimento permissiva que deve ser ajustada para validate em ambientes de produção/CI
- **Classificação:** Pendente confirmado. **Natureza:** Débito de configuração.
- **Evidência atual verificável:** [backend-api/src/main/resources/application.yaml:8](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/application.yaml:8); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/AbstractIntegrationTest.java:25](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/AbstractIntegrationTest.java:25).
- **Justificativa / limite:** ddl-auto update e Flyway enabled coexistem; não foi encontrado profile de produção/CI com validate. Alteração futura requer migrações completas e bootstrap reproduzível.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D168 — Integrar ErrorBoundary com QueryErrorResetBoundary do TanStack Query para limpeza automática de queries em cache no retry

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1019](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1019)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-3-centralizacao-cliente-http-api-e-resiliencia-no-frontend.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-27)
- **Evidência alegada no registro (histórica):** Caso um erro de renderização seja provocado por dados inválidos em cache, o reset do ErrorBoundary sem reset de query pode reexecutar a renderização com o mesmo dado com erro.
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria de resiliência.
- **Evidência atual verificável:** [frontend-web/src/App.tsx:73](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.tsx:73); [frontend-web/src/components/ErrorBoundary/ErrorBoundary.tsx:48](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/ErrorBoundary/ErrorBoundary.tsx:48).
- **Justificativa / limite:** ErrorBoundary tem onReset opcional, mas App não liga reset ao TanStack Query. Integração solicitada está ausente; loop concreto com cache inválido precisa caso de teste, não chamar todo erro de query de crash.
- **Relações:** A14; origens duplicadas: A14.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D169 — Tratar entrada com formato inválido de ano e mês no parsing de selectedMonth em ConsolidacaoToolbar

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1022](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1022)
- **Referência original:** `_bmad-output/implementation-artifacts/spec-5-4-abstracao-e-unificacao-dos-formularios-de-movimentacoes-financeiras.md`
- **Seção de origem:** Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-27)
- **Evidência alegada no registro (histórica):** ConsolidacaoToolbar assume formato numérico válido ao quebrar a string selectedMonth por hífen sem validação de NaN
- **Classificação:** Já resolvido, com evidência. **Natureza:** Robustez.
- **Evidência atual verificável:** [frontend-web/src/components/Consolidacao/ConsolidacaoToolbar.tsx:22](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Consolidacao/ConsolidacaoToolbar.tsx:22).
- **Justificativa / limite:** Parsing já verifica NaN e mês fora de 0..11, com fallback. Parsing estrito de sufixos/ano é possível melhoria diferente da ausência de validação NaN registrada.
- **Relações:** —.

### D170 — Exibir estado de loading/erro para contas e categorias em ContaCategoriaFields [frontend/src/components/MovimentacaoForm/ContaCategoriaFields.tsx:30] — deferred, pre-existing

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1028](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1028)
- **Referência original:** `Deferred from: code review of spec-5-4-abstracao-e-unificacao-dos-formularios-de-movimentacoes-financeiras.md (2026-09-29)`
- **Seção de origem:** Deferred from: code review of spec-5-4-abstracao-e-unificacao-dos-formularios-de-movimentacoes-financeiras.md (2026-09-29)
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de resiliência.
- **Evidência atual verificável:** [frontend-web/src/components/MovimentacaoForm/ContaCategoriaFields.tsx:30](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/MovimentacaoForm/ContaCategoriaFields.tsx:30); [frontend-web/src/components/RequiresDependencies.tsx:13](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:13).
- **Justificativa / limite:** Campos usam apenas data dos hooks; não exibem loading/erro próprios. Guard cobre entrada, mas falhas/revalidação em formulário aberto merecem estado definido.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D171 — Adicionar contador visual de caracteres (0/300) em ObservacoesField [frontend/src/components/MovimentacaoForm/ObservacoesField.tsx:37] — deferred, pre-existing

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1029](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1029)
- **Referência original:** `Deferred from: code review of spec-5-4-abstracao-e-unificacao-dos-formularios-de-movimentacoes-financeiras.md (2026-09-29)`
- **Seção de origem:** Deferred from: code review of spec-5-4-abstracao-e-unificacao-dos-formularios-de-movimentacoes-financeiras.md (2026-09-29)
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria opcional.
- **Evidência atual verificável:** [frontend-web/src/components/MovimentacaoForm/ObservacoesField.tsx:24](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/MovimentacaoForm/ObservacoesField.tsx:24).
- **Justificativa / limite:** Textarea tem maxLength=300, sem contador visual. Ausência não é falha de validação.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D172 — Configuração do maven-failsafe-plugin e execução de testes de integração com Testcontainers [backend-api/pom.xml:149] — resolved in Story 6.2

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1032](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1032)
- **Referência original:** `Deferred from: code review of spec-6-1-testes-unitarios-de-dominio-e-servicos-financeiros-backend.md (2026-10-04)`
- **Seção de origem:** Deferred from: code review of spec-6-1-testes-unitarios-de-dominio-e-servicos-financeiros-backend.md (2026-10-04)
- **Classificação:** Já resolvido, com evidência. **Natureza:** Infra de teste.
- **Evidência atual verificável:** [backend-api/pom.xml:185](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/pom.xml:185); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/AbstractIntegrationTest.java:25](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/AbstractIntegrationTest.java:25).
- **Justificativa / limite:** Failsafe está configurado para integration-test/verify e profile ativa ITs; Testcontainers na classe base.
- **Relações:** —.

### D173 — Extração de abstração/interface comum de parcelamento para DespesaVariavel e ReceitaVariavel [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/] — deferred, pre-existing

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1033](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1033)
- **Referência original:** `Deferred from: code review of spec-6-1-testes-unitarios-de-dominio-e-servicos-financeiros-backend.md (2026-10-04)`
- **Seção de origem:** Deferred from: code review of spec-6-1-testes-unitarios-de-dominio-e-servicos-financeiros-backend.md (2026-10-04)
- **Classificação:** Pendente confirmado. **Natureza:** Débito técnico.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavel.java:130](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavel.java:130).
- **Justificativa / limite:** DespesaVariavel e ReceitaVariavel mantêm fórmulas próprias de valor total/última parcela. Abstração é opcional; preservar regras específicas de cada entidade.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D174 — Padronizar retorno de 404 em ContaController.excluir para retornar ApiErrorResponse estruturado via ResourceNotFoundException [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:58] — deferred, pre-existing

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1036](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1036)
- **Referência original:** `Deferred from: code review of spec-6-2-testes-de-integracao-rest-e-isolamento-multi-tenancy-backend.md (2026-10-05)`
- **Seção de origem:** Deferred from: code review of spec-6-2-testes-de-integracao-rest-e-isolamento-multi-tenancy-backend.md (2026-10-05)
- **Classificação:** Duplicado. **Natureza:** Débito de contrato.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:42](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:42); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java:34](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java:34); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:175](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:175).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D154.

### D175 — Implementar endpoints REST de busca por ID (GET /{id}) para as demais entidades de domínio (Categoria, Despesa e Receita) [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/] — deferred, pre-existing

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1037](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1037)
- **Referência original:** `Deferred from: code review of spec-6-2-testes-de-integracao-rest-e-isolamento-multi-tenancy-backend.md (2026-10-05)`
- **Seção de origem:** Deferred from: code review of spec-6-2-testes-de-integracao-rest-e-isolamento-multi-tenancy-backend.md (2026-10-05)
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria opcional.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java:34](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java:34); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:42](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:42).
- **Justificativa / limite:** Somente ContaController expõe GET /{id}; services têm buscarPorId. Ausência é confirmada; não foi identificado fluxo UI bloqueado.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D176 — Expansão exaustiva de testes de DTOs, CSRF em verbos PUT/DELETE e integridade com outras entidades financeiras [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/] — deferred, pre-existing

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1038](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1038)
- **Referência original:** `Deferred from: code review of spec-6-2-testes-de-integracao-rest-e-isolamento-multi-tenancy-backend.md (2026-10-05)`
- **Seção de origem:** Deferred from: code review of spec-6-2-testes-de-integracao-rest-e-isolamento-multi-tenancy-backend.md (2026-10-05)
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Expansão de testes sem limite.
- **Evidência atual verificável:** [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:132](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:132); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:89](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:89).
- **Justificativa / limite:** Há validação DTO, CSRF POST e FK com despesa fixa; não há matriz completa PUT/DELETE sem CSRF e demais vínculos. Falta definir risco/casos para expansão, evitando meta exaustiva ilimitada.
- **Relações:** D122/D164.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D177 — Suporte a tecla Escape e semântica WAI-ARIA (aria-haspopup, role="menu", role="menuitem") no componente ActionMenu [frontend-web/src/components/ui/ActionMenu.tsx:45] — deferred, pre-existing

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1041](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1041)
- **Referência original:** `Deferred from: code review of spec-6-3-testes-de-componentes-e-fluxos-criticos-no-frontend.md (2026-10-05)`
- **Seção de origem:** Deferred from: code review of spec-6-3-testes-de-componentes-e-fluxos-criticos-no-frontend.md (2026-10-05)
- **Classificação:** Pendente confirmado. **Natureza:** Defeito de acessibilidade.
- **Evidência atual verificável:** [frontend-web/src/components/ui/ActionMenu.tsx:16](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/ui/ActionMenu.tsx:16).
- **Justificativa / limite:** ActionMenu só fecha por mousedown; sem Escape e sem aria-haspopup/menu/menuitem.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D178 — Unificação do componente DeleteConfirmModal compartilhado entre as páginas em vez de implementações duplicadas [frontend-web/src/pages/] — deferred, pre-existing

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1042](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1042)
- **Referência original:** `Deferred from: code review of spec-6-3-testes-de-componentes-e-fluxos-criticos-no-frontend.md (2026-10-05)`
- **Seção de origem:** Deferred from: code review of spec-6-3-testes-de-componentes-e-fluxos-criticos-no-frontend.md (2026-10-05)
- **Classificação:** Pendente confirmado. **Natureza:** Débito técnico.
- **Evidência atual verificável:** [frontend-web/src/pages/DespesasVariaveis/components/DeleteConfirmModal.tsx:11](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/DespesasVariaveis/components/DeleteConfirmModal.tsx:11); [frontend-web/src/pages/ReceitasVariaveis/components/DeleteConfirmModal.tsx:11](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/ReceitasVariaveis/components/DeleteConfirmModal.tsx:11).
- **Justificativa / limite:** DeleteConfirmModal continua específico e semelhante em cada página. Extração pode reduzir deriva de comportamento, não há defeito só pela repetição.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D179 — Ajuste das dependências do useEffect (deleteMutation) em DeleteConfirmModal para eliminar warning do linter react-hooks/exhaustive-deps [frontend-web/src/pages/DespesasVariaveis/components/DeleteConfirmModal.tsx:18 e frontend-web/src/pages/ReceitasVariaveis/components/DeleteConfirmModal.tsx:14] — deferred, pre-existing

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1043](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1043)
- **Referência original:** `Deferred from: code review of spec-6-3-testes-de-componentes-e-fluxos-criticos-no-frontend.md (2026-10-05)`
- **Seção de origem:** Deferred from: code review of spec-6-3-testes-de-componentes-e-fluxos-criticos-no-frontend.md (2026-10-05)
- **Classificação:** Já resolvido, com evidência. **Natureza:** Débito técnico.
- **Evidência atual verificável:** [frontend-web/src/pages/DespesasVariaveis/components/DeleteConfirmModal.tsx:11](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/DespesasVariaveis/components/DeleteConfirmModal.tsx:11); [frontend-web/src/pages/ReceitasVariaveis/components/DeleteConfirmModal.tsx:11](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/ReceitasVariaveis/components/DeleteConfirmModal.tsx:11).
- **Justificativa / limite:** Ambos extraem reset e dependem de [isOpen, reset], não do objeto deleteMutation. Warning apontado foi removido no código; linter não reexecutado.
- **Relações:** —.

### D180 — Suíte de testes unitários para ContaFormModal e submissão em ContasPage [frontend-web/src/pages/Contas/components/ContaFormModal.tsx:1] — deferred, pre-existing

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1046](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1046)
- **Referência original:** `Deferred from: code review of spec-6-3-testes-de-componentes-e-fluxos-criticos-no-frontend.md (2026-10-06)`
- **Seção de origem:** Deferred from: code review of spec-6-3-testes-de-componentes-e-fluxos-criticos-no-frontend.md (2026-10-06)
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [frontend-web/src/pages/Contas/components/ContaFormModal.tsx:32](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/Contas/components/ContaFormModal.tsx:32); [frontend-web/src/pages/Contas/ContasPage.test.tsx:81](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/Contas/ContasPage.test.tsx:81).
- **Justificativa / limite:** ContasPage.test testa abrir formulário e confirmar exclusão, não submissão criar/editar ContaFormModal nem validação.
- **Relações:** D072.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D181 — Suporte a flags para validação seletiva/modular (--frontend e --backend) no script de validação [scripts/validate.sh:65] — deferred, improvement

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1049](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1049)
- **Referência original:** `Deferred from: code review of spec-6-4-infraestrutura-de-validacao-continua-local-e-documentacao-canonica-de-testes.md (2026-10-06)`
- **Seção de origem:** Deferred from: code review of spec-6-4-infraestrutura-de-validacao-continua-local-e-documentacao-canonica-de-testes.md (2026-10-06)
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria opcional.
- **Evidência atual verificável:** [scripts/run-unit-tests.sh:65](/home/guilhermepagio/developer/workspace/projeto-aureus/scripts/run-unit-tests.sh:65).
- **Justificativa / limite:** Script canônico aceita somente help e with-integration, sem frontend/backend. Nome atual é run-unit-tests.sh, não validate.sh.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D182 — Configuração de ferramenta de análise estática e linter no backend (Checkstyle, Spotless ou SpotBugs no Maven) [backend-api/pom.xml:153] — deferred, pre-existing

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1050](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1050)
- **Referência original:** `Deferred from: code review of spec-6-4-infraestrutura-de-validacao-continua-local-e-documentacao-canonica-de-testes.md (2026-10-06)`
- **Seção de origem:** Deferred from: code review of spec-6-4-infraestrutura-de-validacao-continua-local-e-documentacao-canonica-de-testes.md (2026-10-06)
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria de qualidade.
- **Evidência atual verificável:** [backend-api/pom.xml:147](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/pom.xml:147).
- **Justificativa / limite:** Plugins Maven têm compilação/testes/auditoria dependências, sem Checkstyle/Spotless/SpotBugs. Auditoria de dependências não substitui lint de código.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D183 — Otimizar execução do modo --with-integration no script de validação para evitar execução duplicada da suíte unitária [scripts/validate.sh:172] — deferred, improvement

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1051](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1051)
- **Referência original:** `Deferred from: code review of spec-6-4-infraestrutura-de-validacao-continua-local-e-documentacao-canonica-de-testes.md (2026-10-06)`
- **Seção de origem:** Deferred from: code review of spec-6-4-infraestrutura-de-validacao-continua-local-e-documentacao-canonica-de-testes.md (2026-10-06)
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria de produtividade.
- **Evidência atual verificável:** [scripts/run-unit-tests.sh:199](/home/guilhermepagio/developer/workspace/projeto-aureus/scripts/run-unit-tests.sh:199).
- **Justificativa / limite:** Modo integração roda mvnw test e depois mvnw verify -Pintegration-test; fase test se repete no verify.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D184 — Avaliar introdução de ferramentas de cobertura quantitativa de testes (JaCoCo no Maven e cobertura Vitest no frontend) pós-V1 [docs/architecture/testing.md:120] — deferred, future

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1052](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1052)
- **Referência original:** `Deferred from: code review of spec-6-4-infraestrutura-de-validacao-continua-local-e-documentacao-canonica-de-testes.md (2026-10-06)`
- **Seção de origem:** Deferred from: code review of spec-6-4-infraestrutura-de-validacao-continua-local-e-documentacao-canonica-de-testes.md (2026-10-06)
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria opcional.
- **Evidência atual verificável:** [backend-api/pom.xml:147](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/pom.xml:147); [frontend-web/package.json:6](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/package.json:6); [docs/architecture/testing.md:149](/home/guilhermepagio/developer/workspace/projeto-aureus/docs/architecture/testing.md:149).
- **Justificativa / limite:** Não há JaCoCo nem script/provider de cobertura frontend. Avaliação futura permanece, sem percentual mínimo definido.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D185 — Distinguir consultas pausadas sem dados de cadastros vazios.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1056](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1056)
- **Referência original:** `archive/avulsas/2026-10/spec-padronizar-verde-receitas.md`
- **Seção de origem:** Deferred from: code review of spec-6-4-infraestrutura-de-validacao-continua-local-e-documentacao-canonica-de-testes.md (2026-10-06)
- **Evidência alegada no registro (histórica):** RequiresDependencies verifica isLoading, mas não trata estado pending/paused; preexistente.
- **Classificação:** Pendente confirmado. **Natureza:** Defeito de estado UI.
- **Evidência atual verificável:** [frontend-web/src/components/RequiresDependencies.tsx:13](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:13); [frontend-web/src/components/RequiresDependencies.tsx:55](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:55).
- **Justificativa / limite:** Guard só lê isLoading/isError e data; consulta paused sem dados pode seguir ao estado vazio indevido. Estado paused não é representado.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D186 — Indicar recuperação em andamento e evitar cliques repetidos.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1059](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1059)
- **Referência original:** `archive/avulsas/2026-10/spec-padronizar-verde-receitas.md`
- **Seção de origem:** Deferred from: code review of spec-6-4-infraestrutura-de-validacao-continua-local-e-documentacao-canonica-de-testes.md (2026-10-06)
- **Evidência alegada no registro (histórica):** Tentar novamente dispara refetch sem desabilitação nem indicação de isFetching; preexistente.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de UX.
- **Evidência atual verificável:** [frontend-web/src/components/RequiresDependencies.tsx:13](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:13); [frontend-web/src/components/RequiresDependencies.tsx:37](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:37).
- **Justificativa / limite:** Botão refetch não lê isFetching e não é desabilitado durante recuperação. Cliques repetidos possíveis; não implica requisições duplicadas por deduplicação interna.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D187 — Retirar console.error da renderização de RequiresDependencies.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1062](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1062)
- **Referência original:** `archive/avulsas/2026-10/spec-padronizar-verde-receitas.md`
- **Seção de origem:** Deferred from: code review of spec-6-4-infraestrutura-de-validacao-continua-local-e-documentacao-canonica-de-testes.md (2026-10-06)
- **Evidência alegada no registro (histórica):** O registro ocorre em cada renderização do ramo de falha; preexistente.
- **Classificação:** Pendente confirmado. **Natureza:** Débito técnico.
- **Evidência atual verificável:** [frontend-web/src/components/RequiresDependencies.tsx:37](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:37).
- **Justificativa / limite:** console.error continua no ramo de renderização, repetindo log a cada render em erro.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D188 — Melhorar anúncios e hierarquia de títulos dos estados de dependências.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1065](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1065)
- **Referência original:** `archive/avulsas/2026-10/spec-padronizar-verde-receitas.md`
- **Seção de origem:** Deferred from: code review of spec-6-4-infraestrutura-de-validacao-continua-local-e-documentacao-canonica-de-testes.md (2026-10-06)
- **Evidência alegada no registro (histórica):** Carregamento e falha não têm status/alert e o primeiro título da área é h3; preexistente.
- **Classificação:** Pendente confirmado. **Natureza:** Defeito de acessibilidade.
- **Evidência atual verificável:** [frontend-web/src/components/RequiresDependencies.tsx:13](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:13); [frontend-web/src/components/RequiresDependencies.tsx:37](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:37); [frontend-web/src/components/RequiresDependencies.tsx:55](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:55).
- **Justificativa / limite:** Além dos anúncios ausentes D099/D100, estados usam h3 como primeiro título da área. Validar hierarquia na página completa antes de escolher nível final.
- **Relações:** D099/D100.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D189 — Completar retorno ao fluxo original após cadastrar dependências.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1068](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1068)
- **Referência original:** `archive/avulsas/2026-10/spec-padronizar-verde-receitas.md`
- **Seção de origem:** Deferred from: code review of spec-6-4-infraestrutura-de-validacao-continua-local-e-documentacao-canonica-de-testes.md (2026-10-06)
- **Evidência alegada no registro (histórica):** RequiresDependencies envia state.from, mas os destinos não o consomem; preexistente.
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna funcional.
- **Evidência atual verificável:** [frontend-web/src/components/RequiresDependencies.tsx:55](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:55); [frontend-web/src/pages/Contas/ContasPage.tsx:1](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/Contas/ContasPage.tsx:1); [frontend-web/src/pages/Categorias/components/CategoriaFormModal.tsx:1](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/Categorias/components/CategoriaFormModal.tsx:1).
- **Justificativa / limite:** Guard envia state.from, destinos não consomem esse estado após cadastro. Jornada não retorna automaticamente à movimentação original.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### D190 — Avaliar abertura direta dos formulários nos links Cadastrar Conta e Cadastrar Categoria.

- **Origem:** [_bmad-output/implementation-artifacts/deferred-work.md:1071](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/deferred-work.md:1071)
- **Referência original:** `archive/avulsas/2026-10/spec-padronizar-verde-receitas.md`
- **Seção de origem:** Deferred from: code review of spec-6-4-infraestrutura-de-validacao-continua-local-e-documentacao-canonica-de-testes.md (2026-10-06)
- **Evidência alegada no registro (histórica):** Os links abrem listagens e exigem outra ação para iniciar cadastro; preexistente.
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria opcional.
- **Evidência atual verificável:** [frontend-web/src/components/RequiresDependencies.tsx:55](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:55); [frontend-web/src/pages/Contas/ContasPage.tsx:1](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/Contas/ContasPage.tsx:1).
- **Justificativa / limite:** Links vão às listagens, exigindo clicar para abrir formulário. Abertura direta não implementada; decidir se melhora jornada sem surpreender.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### A01 — Corrigir ordem das abas na barra de navegacao (Receitas Variaveis antes de Receitas Fixas)

- **Origem:** [_bmad-output/implementation-artifacts/sprint-status.yaml:113](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:113)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-1/epic-1-retro-08-19-2026.md`
- **ID original / status registrado:** `epic-1-retro-item-1-corrigir-ordem-das-abas-na-barra-de-nave` / `done`
- **Classificação:** Pendente confirmado. **Natureza:** Desvio de compromisso UI.
- **Evidência atual verificável:** [frontend-web/src/components/Navigation/Navigation.tsx:55](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Navigation/Navigation.tsx:55).
- **Justificativa / limite:** Action está done, mas links de Receitas Fixas precedem Variáveis. Pendência contra ordem registrada confirmada; validar se alteração posterior foi intencional antes de aplicar.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### A02 — Criar testes automatizados para o pacote security no backend

- **Origem:** [_bmad-output/implementation-artifacts/sprint-status.yaml:125](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:125)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-1/epic-1-retro-08-19-2026.md`
- **ID original / status registrado:** `epic-1-retro-item-2-criar-testes-automatizados-para-o-pacote` / `done`
- **Classificação:** Já resolvido, com evidência. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:87](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:87); [_bmad-output/implementation-artifacts/archive/epic-6/epic-6-retro-2026-10-11.md:108](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-6/epic-6-retro-2026-10-11.md:108).
- **Justificativa / limite:** SecurityIT implementa testes automatizados JWT válido/inválido/expirado, CSRF e auth. Atende ação geral de criar testes; não implica testar todas classes security. Lacunas específicas D024/D035/D036/D037 permanecem.
- **Relações:** —.

### A03 — Commitar as alterações pendentes da revisão de código da Story 2.3

- **Origem:** [_bmad-output/implementation-artifacts/sprint-status.yaml:136](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:136)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/epic-2-retro-2026-08-20.md`
- **ID original / status registrado:** `epic-2-retro-item-3-commitar-as-alterações-pendentes-da-revi` / `done`
- **Classificação:** Já resolvido, com evidência. **Natureza:** Registro histórico.
- **Evidência atual verificável:** [frontend-web/src/components/RequiresDependencies.tsx:13](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:13).
- **Justificativa / limite:** Histórico Git local contém commit 05e1320 fix(epic-2): aplicar correcoes do code review da story 2.3; patch consultado na validação da triagem. Estado atual também preserva melhoria simultânea de dependências. Não foi feito commit nesta sessão.
- **Relações:** —.
- **Evidência Git adicional:** commit local `05e1320`, título `fix(epic-2): aplicar correcoes do code review da story 2.3`; não há arquivo/linha de texto que substitua essa evidência de commit.

### A04 — Refatorar os componentes de formulários e hooks de Despesas e Receitas (tanto fixas quanto variáveis) para utilizar abstrações comuns, reduzindo duplicação de código.

- **Origem:** [_bmad-output/implementation-artifacts/sprint-status.yaml:147](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:147)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/epic-3-retro-2026-08-23.md`
- **ID original / status registrado:** `epic-3-retro-item-4-refatorar-os-componentes-de-formulários` / `done`
- **Classificação:** Necessita esclarecimento ou validação adicional. **Natureza:** Refatoração parcialmente entregue.
- **Evidência atual verificável:** [frontend-web/src/components/MovimentacaoForm/MovimentacaoVariavelFormModal.tsx:1](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/MovimentacaoForm/MovimentacaoVariavelFormModal.tsx:1); [frontend-web/src/hooks/useCategorias.ts:14](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/hooks/useCategorias.ts:14); [frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx:273](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx:273).
- **Justificativa / limite:** Formulários fixos/variáveis usam componentes comuns; quatro hooks financeiros ainda repetem estrutura. Ação menciona formulários E hooks. Falta decidir se padronização sobre apiClient atende intenção ou se abstração comum de hooks é obrigatória. done não basta.
- **Relações:** D118/D130.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### A05 — Adicionar cobertura de testes unitários para os Domain Models e Controllers desenvolvidos (DespesaFixa, ReceitaFixa, DespesaVariavel, ReceitaVariavel).

- **Origem:** [_bmad-output/implementation-artifacts/sprint-status.yaml:160](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:160)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/epic-3-retro-2026-08-23.md`
- **ID original / status registrado:** `epic-3-retro-item-5-adicionar-cobertura-de-testes-unitários` / `done`
- **Classificação:** Já resolvido, com evidência. **Natureza:** Lacuna de teste.
- **Evidência atual verificável:** [backend-api/src/test/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavelTest.java:1](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavelTest.java:1); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaControllerTest.java:1](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaControllerTest.java:1).
- **Justificativa / limite:** Há testes de domínio variáveis e controllers das quatro entidades, além de services. Fixas não possuem cálculo de parcelamento a exigir mesma suíte de domínio. Não afirmar cobertura total quantitativa.
- **Relações:** —.

### A06 — Criar uma camada de serviço (Service Layer) no backend para encapsular e remover a regra de negócio dos Controllers.

- **Origem:** [_bmad-output/implementation-artifacts/sprint-status.yaml:172](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:172)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/epic-3-retro-2026-08-23.md`
- **ID original / status registrado:** `epic-3-retro-item-6-criar-uma-camada-de-serviço-service-laye` / `done`
- **Classificação:** Já resolvido, com evidência. **Natureza:** Arquitetura.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43).
- **Justificativa / limite:** Regras financeiras estão nos services e controllers delegam. AuthController direto no repository é recorte independente D029.
- **Relações:** —.

### A07 — Refatorar as APIs REST para utilizar DTOs (Data Transfer Objects), de preferência usando records do Java, isolando as entidades do domínio.

- **Origem:** [_bmad-output/implementation-artifacts/sprint-status.yaml:184](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:184)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/epic-3-retro-2026-08-23.md`
- **ID original / status registrado:** `epic-3-retro-item-7-refatorar-as-apis-rest-para-utilizar-dto` / `done`
- **Classificação:** Já resolvido, com evidência. **Natureza:** Contrato API.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:29); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/DespesaVariavelRequestDTO.java:32](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/DespesaVariavelRequestDTO.java:32); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29).
- **Justificativa / limite:** APIs financeiras usam records Request/Response e /me usa UserProfileResponseDTO. Entidades não são contrato REST desses fluxos.
- **Relações:** —.

### A08 — Centralizar cliente HTTP/API no Frontend para padronizar chamadas e tratamento de erros

- **Origem:** [_bmad-output/implementation-artifacts/sprint-status.yaml:196](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:196)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-4/epic-4-retro-2026-09-14.md`
- **ID original / status registrado:** `epic-4-retro-item-8-centralizar-cliente-http-api-no-frontend` / `done`
- **Classificação:** Já resolvido, com evidência. **Natureza:** Cliente HTTP.
- **Evidência atual verificável:** [frontend-web/src/hooks/useConsolidacao.ts:18](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/hooks/useConsolidacao.ts:18); [frontend-web/src/services/apiClient.ts:176](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/services/apiClient.ts:176); [frontend-web/src/hooks/useCategorias.ts:14](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/hooks/useCategorias.ts:14).
- **Justificativa / limite:** Hooks e auth/logout usam apiClient central com parsing, CSRF e timeout.
- **Relações:** —.

### A09 — Padronizar regra de negócio para movimentações sem conta associada

- **Origem:** [_bmad-output/implementation-artifacts/sprint-status.yaml:207](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:207)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-4/epic-4-retro-2026-09-14.md`
- **ID original / status registrado:** `epic-4-retro-item-9-padronizar-regra-de-negócio-para-movimen` / `done`
- **Classificação:** Já resolvido, com evidência. **Natureza:** Regra financeira.
- **Evidência atual verificável:** [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:24](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:24); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:118](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:118); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262).
- **Justificativa / limite:** Conta é obrigatória em modelo/DDL/DTO de novas movimentações; consolidação tem fallback Sem Conta para legado. Atende alternativa explícita da retro de tornar conta obrigatória. Verificar dados legados exigiria autorização separada.
- **Relações:** D146.

### A10 — Otimizar agregação de transações no Backend substituindo carregamento em memória por queries SQL e índices

- **Origem:** [_bmad-output/implementation-artifacts/sprint-status.yaml:80](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:80)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-4/epic-4-retro-2026-09-14.md`
- **ID original / status registrado:** `epic-4-retro-item-10-otimizar-agregação-de-transações-no-back` / `done`
- **Classificação:** Duplicado. **Natureza:** Débito de desempenho.
- **Evidência atual verificável:** [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:74](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:74); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262).
- **Justificativa / limite:** Ação marcada done, mas SQL de agregação/corte por período não existe nos repositories atuais. Índices V1 e melhoria parcial por categoria não entregam a substituição pedida.
- **Relações:** D139.

### A11 — Implementar suíte de testes E2E automatizados para o Painel de Consolidação

- **Origem:** [_bmad-output/implementation-artifacts/sprint-status.yaml:218](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:218)
- **Referência original:** `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-4/epic-4-retro-2026-09-14.md`
- **ID original / status registrado:** `epic-4-retro-item-11-implementar-suíte-de-testes-e2e-automati` / `open`
- **Classificação:** Pendente confirmado. **Natureza:** Lacuna de teste adiada.
- **Evidência atual verificável:** [docs/architecture/testing.md:149](/home/guilhermepagio/developer/workspace/projeto-aureus/docs/architecture/testing.md:149); [frontend-web/package.json:6](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/package.json:6).
- **Justificativa / limite:** Não há suíte E2E de navegador; documento decide pós-V1 e lista CI/Mock OIDC/regressão entre navegadores como marcos. Adiado não é resolvido.
- **Relações:** D022/D059.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### A12 — Configurar Testcontainers e isolamento de banco de dados para a suíte BackendApplicationTests

- **Origem:** [_bmad-output/implementation-artifacts/sprint-status.yaml:230](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:230)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-5/epic-5-retro-2026-10-03.md`
- **ID original / status registrado:** `epic-5-retro-item-12-configurar-testcontainers-e-isolamento-d` / `done`
- **Classificação:** Duplicado. **Natureza:** Infra de teste.
- **Evidência atual verificável:** [backend-api/src/test/java/com/guilhermepagio/aureus/backend/AbstractIntegrationTest.java:25](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/AbstractIntegrationTest.java:25); [backend-api/pom.xml:185](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/pom.xml:185).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D163.

### A13 — Criar migração Flyway V2 para limpeza de índice redundante em usuarios e adição de índices em FKs de movimentações

- **Origem:** [_bmad-output/implementation-artifacts/sprint-status.yaml:95](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:95)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-5/epic-5-retro-2026-10-03.md`
- **ID original / status registrado:** `epic-5-retro-item-13-criar-migração-flyway-v2-para-limpeza-de` / `open`
- **Classificação:** Pendente confirmado. **Natureza:** Débito de banco.
- **Evidência atual verificável:** [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:4](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:4); [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74).
- **Justificativa / limite:** Não há V2 no diretório de migrações; redundância e falta de índices FK continuam. A ação reúne D161 e parte D155; D155 inclui também usuario_id de contas/categorias.
- **Relações:** D155/D161.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

### A14 — Integrar ErrorBoundary com QueryErrorResetBoundary do TanStack Query para reset de cache em retentativas

- **Origem:** [_bmad-output/implementation-artifacts/sprint-status.yaml:240](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:240)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-5/epic-5-retro-2026-10-03.md`
- **ID original / status registrado:** `epic-5-retro-item-14-integrar-errorboundary-com-queryerrorres` / `open`
- **Classificação:** Duplicado. **Natureza:** Melhoria de resiliência.
- **Evidência atual verificável:** [frontend-web/src/App.tsx:73](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/App.tsx:73); [frontend-web/src/components/ErrorBoundary/ErrorBoundary.tsx:48](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/ErrorBoundary/ErrorBoundary.tsx:48).
- **Justificativa / limite:** Mesmo objetivo; conservar esta origem e consultar a análise do representante.
- **Relações:** D168.

### A15 — Configurar GitHub Actions Workflow para Validação Contínua

- **Origem:** [_bmad-output/implementation-artifacts/sprint-status.yaml:250](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:250)
- **Referência original:** `_bmad-output/implementation-artifacts/archive/epic-6/epic-6-retro-2026-10-11.md`
- **ID original / status registrado:** `epic-6-retro-item-15-configurar-github-actions-workflow-para` / `open`
- **Classificação:** Pendente confirmado. **Natureza:** Melhoria de automação.
- **Evidência atual verificável:** [scripts/run-unit-tests.sh:199](/home/guilhermepagio/developer/workspace/projeto-aureus/scripts/run-unit-tests.sh:199); [_bmad-output/implementation-artifacts/archive/epic-6/epic-6-retro-2026-10-11.md:108](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-6/epic-6-retro-2026-10-11.md:108).
- **Justificativa / limite:** Não há .github/workflows no checkout; script local existe. Não foi consultado estado remoto; action pede workflow versionado ainda ausente.
- **Relações:** —.
- **Decisão:** consultar corte vigente — incluir em épico, manter no backlog, descartar ou investigar mais.

## Pontos que precisam de esclarecimento ou validação adicional

Nenhum destes itens entra automaticamente em épico. A pergunta/validação faltante está indicada para cada origem.

| ID | O que falta esclarecer ou validar | Evidência disponível | Decisão |
|---|---|---|---|
| D002 | Não há entidade persistida de sessão/refresh, mas o fluxo vigente é stateless com JWT em cookie. Falta decidir renovação/revogação e se o requisito ainda existe; OAuth não exige por si só esta entidade. | [_bmad-output/planning-artifacts/decisions/2026-08-13-autenticacao-v1.md:15](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/planning-artifacts/decisions/2026-08-13-autenticacao-v1.md:15); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44) | consultar corte vigente |
| D006 | TipoMovimento via ContextoFinanceiro permanece no modelo conceitual; DBML usa discriminador e JPA usa quatro entidades/tabelas. Não é comprovado defeito de execução. Falta definir se diagramas são conceituais ou devem refletir o esquema físico. | [_bmad-output/planning-artifacts/technical/domain-model/class-diagram.md:13](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/planning-artifacts/technical/domain-model/class-diagram.md:13); [_bmad-output/planning-artifacts/technical/data-model/aureus-erd.dbml:19](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/planning-artifacts/technical/data-model/aureus-erd.dbml:19); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:50](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:50); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavel.java:130](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavel.java:130) | consultar corte vigente |
| D028 | /me consulta findById em cada verificação. Falta medir frequência/custo e decidir frescor; cache não deve ser imposto sem requisito. | [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29) | consultar corte vigente |
| D038 | Resolver por nome de classe permanece, porém é sem estado e consulta TenantContext estático; perder injeção Spring não demonstra falha. Falta demonstrar dependência real de bean e verificar inicialização do Hibernate. | [backend-api/src/main/resources/application.yaml:8](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/application.yaml:8); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206) | consultar corte vigente |
| D050 | Interface estende JpaRepository; ausência de @Repository não comprova perda de tradução de exceções em proxy Spring Data. Falta reproduzir contrato alegado; não classificar como defeito apenas pelo import/anotação. | [backend-api/src/main/java/com/guilhermepagio/aureus/backend/repository/UsuarioRepository.java:9](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/repository/UsuarioRepository.java:9) | consultar corte vigente |
| D061 | find-then-save continua e UNIQUE pode rejeitar corrida. Precisa reprodução controlada com dois callbacks e política de retry; não foi executado teste concorrente. | [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41); [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:4](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:4) | consultar corte vigente |
| D062 | Apontamento genérico na revisão 1.3 não define cenários. Há testes atuais; faltam critérios para afirmar cobertura completa ou duplicação exata. | [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206) | consultar corte vigente |
| D063 | ThreadLocal não propaga para outras threads. Nenhum fluxo assíncrono específico foi identificado como consumidor; falta cenário que exija propagação. Não trocar por InheritableThreadLocal sem contrato. | [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:57](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:57) | consultar corte vigente |
| D067 | Controllers financeiros/filtro JWT têm testes atuais; AuthController não tem unitários e OAuth não é exercitado. Registro amplo não pode ser encerrado em bloco. | [backend-api/src/test/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaControllerTest.java:1](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaControllerTest.java:1); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:87](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:87) | consultar corte vigente |
| D073 | Conta não customiza equals/hashCode; não foi localizado uso que dependa de igualdade entre instâncias. Falta identificar cenário e estratégia de ID, sem presumir obrigação JPA. | [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Conta.java:20](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Conta.java:20); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9) | consultar corte vigente |
| D082 | Modal é centralizado em todos breakpoints; bottom sheet não está implementado. Falta validar se é requisito atual e impacto em mobile, antes de rotular defeito. | [frontend-web/src/components/ui/Modal.tsx:205](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/ui/Modal.tsx:205) | consultar corte vigente |
| D086 | Categoria sem equals/hashCode customizados. Falta uso problemático concreto para justificar mudança de semântica de identidade. | [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Categoria.java:20](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Categoria.java:20) | consultar corte vigente |
| D093 | Nenhum @Version nas entidades avaliadas. Falta decisão de produto sobre conflito de edição e cenário concorrente; não assumir lost update como defeito sem contrato. | [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Conta.java:20](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Conta.java:20); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Categoria.java:20](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Categoria.java:20) | consultar corte vigente |
| D097 | Estados erro e vazio já compartilham estrutura p-6/max-w-lg/text-center; falta critério visual para declarar inconsistência remanescente ou necessidade de card. | [frontend-web/src/components/RequiresDependencies.tsx:37](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:37); [frontend-web/src/components/RequiresDependencies.tsx:55](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/RequiresDependencies.tsx:55) | consultar corte vigente |
| D103 | Escopo da introdução de ícones é julgamento histórico, não defeito atual. Ícones lucide exigem conferir SVG produzido para concluir aria-hidden; não foi renderizado navegador nem inspecionada dependência. Separar intenção de escopo e saída DOM. | [frontend-web/src/components/Header/Header.tsx:69](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Header/Header.tsx:69) | consultar corte vigente |
| D106 | Fixas têm dataInicio, não dataFim/soft-delete. Exclusão física permanece. Falta política de histórico/vigência para definir comportamento esperado; não implementar preservação automaticamente. | [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:80](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:80); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43) | consultar corte vigente |
| D112 | Integração e formulários fixos hoje têm testes, páginas fixas ainda não. Registro genérico requer separar cenários para eventual encerramento. | [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206); [frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx:273](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx:273) | consultar corte vigente |
| D118 | Formulários foram unificados; hooks e páginas continuam específicos e semelhantes. Falta definir quanto da duplicação de listagem deve ser removido; abstrair tudo não é requisito automático. | [frontend-web/src/components/MovimentacaoForm/MovimentacaoVariavelFormModal.tsx:1](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/MovimentacaoForm/MovimentacaoVariavelFormModal.tsx:1); [frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx:273](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx:273); [frontend-web/src/pages/DespesasFixas/DespesasFixasPage.tsx:1](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/pages/DespesasFixas/DespesasFixasPage.tsx:1) | consultar corte vigente |
| D121 | existsById seguido de deleteById permanece; handler genérico ainda existe. Falta reproduzir corrida na versão atual e confirmar se delete ausente lança exceção; não afirmar 500 por inferência de versão antiga. | [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:175](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:175) | consultar corte vigente |
| D122 | Testes de datas/FK/parsing existem. A frase não define matriz completa e não autoriza declarar toda verificação encerrada. Especificar casos residuais. | [backend-api/src/test/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelServiceTest.java:106](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelServiceTest.java:106); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206); [frontend-web/src/services/apiClient.test.ts:143](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/services/apiClient.test.ts:143) | consultar corte vigente |
| D127 | Contexto arquivado continua em inglês e omite ON DELETE RESTRICT explícito; não há original comparável para provar perda pela tradução. Banco/integração já protegem vínculos. Decidir se contexto arquivado deve ser mantido como guia atual. | [_bmad-output/implementation-artifacts/archive/epic-3/epic-3-context.md:16](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/epic-3-context.md:16); [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:24](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:24) | consultar corte vigente |
| D128 | Mês inicial é calculado no módulo; seletor permite escolher e resetar para mês atual. Falta decidir se aplicação aberta deve acompanhar virada de mês ou preservar seleção intencional. | [frontend-web/src/store/monthStore.ts:11](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/store/monthStore.ts:11); [frontend-web/src/components/Consolidacao/ConsolidacaoToolbar.tsx:22](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Consolidacao/ConsolidacaoToolbar.tsx:22) | consultar corte vigente |
| D138 | Conta/categoria usam queries independentes e categoria tem estado próprio. Layout shift real depende de latência/renderização; medir em navegador antes de definir defeito. | [frontend-web/src/components/Consolidacao/ConsolidacaoGrid.tsx:172](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/Consolidacao/ConsolidacaoGrid.tsx:172) | consultar corte vigente |
| D147 | Listagem usa relações LAZY e DTO, sem JOIN FETCH. Falta contar SQL/medir N+1; acesso ao ID de proxy não prova consulta adicional. Decidir apenas com tracing/benchmark seguro. | [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43) | consultar corte vigente |
| D156 | Fixas sem dataInicio entram na projeção desde o primeiro mês e ficam fora do histórico. Diferença é real; falta definir desde quando contar legados sem data, evitando saldo arbitrário. | [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:173](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:173); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:80](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:80) | consultar corte vigente |
| D160 | Fallback public permanece; rotas financeiras são autenticadas e testes cruzados existem. Falta validar operações sem contexto e bootstrap para decidir fail-closed sem quebrar login/infra. Não há exploit demonstrado. | [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9) | consultar corte vigente |
| D176 | Há validação DTO, CSRF POST e FK com despesa fixa; não há matriz completa PUT/DELETE sem CSRF e demais vínculos. Falta definir risco/casos para expansão, evitando meta exaustiva ilimitada. | [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:132](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:132); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:31); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:89](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java:89) | consultar corte vigente |
| A04 | Formulários fixos/variáveis usam componentes comuns; quatro hooks financeiros ainda repetem estrutura. Ação menciona formulários E hooks. Falta decidir se padronização sobre apiClient atende intenção ou se abstração comum de hooks é obrigatória. done não basta. | [frontend-web/src/components/MovimentacaoForm/MovimentacaoVariavelFormModal.tsx:1](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/MovimentacaoForm/MovimentacaoVariavelFormModal.tsx:1); [frontend-web/src/hooks/useCategorias.ts:14](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/hooks/useCategorias.ts:14); [frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx:273](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx:273) | consultar corte vigente |

### Validações futuras também necessárias para dimensionar pendentes

- D013: conferir superfície Actuator e allowlist no ambiente de implantação; fallback permissivo já confirmado estaticamente.
- D017: validar HTTPS e forwarded headers do proxy, sem reabrir automaticamente secure(false), que já foi removido.
- D139/D155/A13: benchmark, plano EXPLAIN e catálogo de índices em base autorizada; nenhuma consulta de dados nesta sessão.
- D150: reproduzir limite 9.999.999,99 em teste isolado; divergência DTO/JPA já está explícita.
- D168/D185: reproduzir reset com cache inválido e query paused sem dados em fixtures, antes de escolher implementação.
- A01: confirmar se a ordem atual resultou de decisão posterior deliberada; o registro pede Variáveis antes de Fixas, enquanto o código faz o inverso.

## Inventário de development_status (contexto, não novos apontamentos)

Os estados abaixo foram preservados apenas para rastreabilidade. Não são evidência de resolução dos D/A e não entram na contagem de 205 registros de trabalho. Story 3.3 foi absorvida conforme a chave, e os seis épicos/retrospectivas estão registrados como done. Nenhum status foi alterado.

| ID contextual | Registro original | Estado registrado | Origem |
|---|---|---|---|
| S01 | `epic-1` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:39](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:39) |
| S02 | `1-1-esqueleto-visual-e-barra-de-navegação` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:40](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:40) |
| S03 | `1-2-autenticação-via-google-oauth-2-0-e-tratamento-de-erros` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:41](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:41) |
| S04 | `1-3-proteção-de-sessão-e-isolamento-de-dados` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:42](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:42) |
| S05 | `1-4-encerrar-sessão-logout-e-limpeza-de-estado` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:43](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:43) |
| S06 | `epic-1-retrospective` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:44](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:44) |
| S07 | `epic-2` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:46](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:46) |
| S08 | `2-1-gestão-de-contas-crud-com-proteção-de-vínculo` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:47](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:47) |
| S09 | `2-2-gestão-de-categorias-crud-com-proteção-de-vínculo` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:48](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:48) |
| S10 | `2-3-restrição-granular-de-cadastro-sem-dependências` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:49](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:49) |
| S11 | `epic-2-retrospective` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:50](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:50) |
| S12 | `epic-3` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:52](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:52) |
| S13 | `3-1-lançamentos-fixos-despesas-e-receitas-com-vigência` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:53](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:53) |
| S14 | `3-2-lançamentos-variáveis-e-parcelados` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:54](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:54) |
| S15 | `3-3-lançamentos-variáveis-à-vista-parcela-única-descartada-absor` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:55](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:55) |
| S16 | `3-4-sincronização-de-visão-mensal-invalidação-de-cache-e-filtro` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:56](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:56) |
| S17 | `epic-3-retrospective` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:57](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:57) |
| S18 | `epic-4` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:59](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:59) |
| S19 | `4-1-seleção-de-mês-navegação-temporal-e-grid-com-suporte-a-gesto` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:60](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:60) |
| S20 | `4-2-bloco-de-consolidação-por-conta-receitas-e-despesas` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:61](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:61) |
| S21 | `4-3-bloco-analítico-de-categorias-valores-e-percentuais-com-prot` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:62](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:62) |
| S22 | `4-4-bloco-de-resumo-geral-com-sobra-histórica-acumulada` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:63](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:63) |
| S23 | `epic-4-retrospective` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:64](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:64) |
| S24 | `epic-5` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:66](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:66) |
| S25 | `5-1-backend-service-layer-e-dtos-records-para-entidades-financei` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:67](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:67) |
| S26 | `5-2-tratamento-global-de-erros-validação-de-tenant-em-relacionam` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:68](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:68) |
| S27 | `5-3-centralização-do-cliente-http-api-e-resiliência-no-frontend` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:69](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:69) |
| S28 | `5-4-abstração-e-unificação-dos-formulários-de-movimentações-fina` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:70](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:70) |
| S29 | `epic-5-retrospective` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:71](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:71) |
| S30 | `epic-6` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:73](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:73) |
| S31 | `6-1-testes-unitários-de-domínio-e-serviços-financeiros-backend` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:74](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:74) |
| S32 | `6-2-testes-de-integração-rest-e-isolamento-multi-tenancy-backend` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:75](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:75) |
| S33 | `6-3-testes-de-componentes-e-fluxos-críticos-no-frontend-frontend` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:76](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:76) |
| S34 | `6-4-infraestrutura-de-validação-contínua-local-e-documentação-ca` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:77](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:77) |
| S35 | `epic-6-retrospective` | done | [_bmad-output/implementation-artifacts/sprint-status.yaml:78](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/sprint-status.yaml:78) |

**Total contextual: 35 registros.**

## Registro de integridade das fontes anteriores à organização

- HEAD local: `1cecb9028d9532fed10f85a4488225f231f1bacb`.
- SHA-256 deferred-work.md: `ed7d4d1eaaba588044726181c84c210853d5f68de1927a27eb2c39200c9f4304`.
- SHA-256 sprint-status.yaml: `6aa78e49a4f43829b65a3544b57c95e76f762ba972aae9c1229e597c156bd9e4`.
- Aplicar as decisões vigentes de descarte, manutenção e investigação registradas no início deste relatório. Não foram criados épicos, stories ou specs; nenhuma correção foi implementada. Alterações de backlog e planejamento ficam aguardando decisões posteriores.

## Validação interativa — decisões de planejamento

### Grupo 1 — acesso, cookies e logout

- Decisão explícita de Guilherme: “aprovo todos para planejamento”, referente aos quatro itens apresentados neste grupo, não aos 21 itens ativos.
- Aprovados: DW-13 (D013), DW-14 (D014), DW-26 (D026), DW-36 (D036).
- DW-13: planejar restrição do fallback público e política Actuator preservando rotas OAuth necessárias. Não foi comprovada exposição de todos os endpoints Actuator em produção.
- DW-14/DW-36: planejar testes focados dos atributos/expiração dos cookies; comportamento já existe e testes específicos faltam.
- DW-26: planejar testes de limpeza local de cache/estado no logout, inclusive falha de rede; não equivale a revogação de sessão no servidor.
- Verificação: inspeção estática de SecurityConfig:44, AuthController:42, OAuth2LoginSuccessHandler:61, SecurityIT:87 e Header:23; nenhuma suíte executada.
- Status de execução dos quatro: open, sem implementação. Aprovação registrada nas entradas DW e em backlog_triage.planning_approvals no sprint-status.
- Os outros 17 DW aguardam apresentação e decisão nos próximos grupos.

### Cobertura dos action items do sprint

- A10: otimização da consolidação, ativo como investigação, vinculado a DW-139. Será apresentado no grupo de desempenho; não aprovado por esta decisão do grupo 1.
- A13: revisão/migração de índices, ativo como investigação, vinculado a DW-155 e DW-161. Será apresentado no grupo de desempenho; não aprovado por esta decisão do grupo 1.
- Os outros 13 action items foram preservados em action_items_history como resolvidos, descartados ou duplicados conforme o corte anterior; nenhum foi apagado.
- Não criar tarefa duplicada a partir de A10/A13: as mesmas histórias futuras devem atualizar tanto os respectivos DW quanto o action item associado, somente quando seu escopo estiver integralmente atendido.

### Grupo 2 — isolamento e requisições indevidas

Decisão explícita de Guilherme: planejar DW-35, DW-37 e o recorte de DW-176; investigar DW-160 incluindo DW-38; descartar DW-63.

- Planejamento aprovado: DW-35, DW-37, DW-176 (somente testes CSRF PUT/DELETE sem token e vínculos cruzados faltantes). Nenhuma implementação iniciada.
- Investigação aprovada: DW-160 incluindo configuração do resolver de DW-38. Mudança do fallback não aprovada antes da evidência.
- DW-38 consolidado em DW-160; DW-63 descartado por ausência de fluxo assíncrono consumidor. Ambos preservados como registros históricos, fora das entradas canônicas abertas DW. Descarte não foi marcado como implementação concluída.
- Evidência estática: JwtAuthenticationFilter:57 limpa contexto; CurrentTenantIdentifierResolverImpl:10 retorna public se contexto ausente; SecurityIT:77 testa POST sem CSRF; MultiTenancyIT contém casos cross-tenant. Busca em backend-api/src/main não identificou consumidores @Async/Executor/CompletableFuture/parallelStream. Nenhum teste executado.
- Após grupos 1 e 2: 7 registros aprovados para planejamento, 1 investigação aprovada (DW-160, absorvendo DW-38), 1 consolidado e 1 descartado. Foram decididas 10 das 21 origens iniciais; faltam 11 para apresentação.
- Ledger atual: 19 entradas canônicas open (8 mantidas por segurança, 2 investigações de segurança, 9 investigações de desempenho). Aprovação de planejamento e conclusão de execução continuam distintas.

### Grupo 3 — expiração e revogação de sessão

Guilherme aprovou o encaminhamento apresentado (“perfeito, podemos continuar”): planejar DW-60 e investigar DW-2 com foco em revogação, sem aprovar renovação automática.

- DW-60: planejar tratamento de 401 durante uso, limpeza de cache/estado privado e retorno ao login; o backend já bloqueia requisições sem sessão válida.
- DW-2: investigar necessidade/custo de revogar JWT antes da expiração; nenhuma escolha automática de refresh token ou persistência de sessão.
- Evidência estática: App consulta /me no efeito inicial; apiClient lança ApiError para respostas não OK sem tratamento central de sessão; JwtUtil valida assinatura/expiração sem mecanismo de revogação; logout remove cookie. Nenhuma suíte executada.
- Progresso: 12 das 21 origens iniciais decididas; 8 para planejamento, 2 investigações aprovadas, 1 consolidado e 1 descartado. Faltam 9 origens de desempenho. Todos os trabalhos mantidos continuam open, sem implementação.

### Grupo 4 — consolidação e índices (A10/A13 do sprint)

Guilherme aprovou o encaminhamento apresentado (“sim”):

- DW-139/A10: investigar tempo, memória e volume carregado antes de aprovar otimização; preservar saldo histórico pré-grade e totais mensais.
- DW-155/A13: investigar catálogo e planos/consultas para definir índices úteis, considerando custo de manutenção.
- DW-161/A13: planejamento aprovado condicionalmente à conferência do banco antes de remover índice; preservar a constraint UNIQUE. Nenhum SQL/migração executado.
- A10 e A13 permanecem open. Aprovação da investigação ou de parte de A13 não encerra o action item; o escopo total deve ser atendido e verificado para fechamento.
- Progresso: 15 das 21 origens iniciais decididas; 8 para planejamento, 1 para planejamento condicional, 4 investigações aprovadas, 1 consolidado e 1 descartado. Faltam 6 origens de desempenho.

### Grupo 5 — paginação e consultas extras

Guilherme aprovou (“aprovo”) investigar DW-117, DW-147 e DW-153, com DW-153 em prioridade menor e implementação condicionada aos resultados.

- DW-117: medir volume, tamanho das respostas e latência antes de paginar; preservar opções completas nos seletores dos formulários.
- DW-147: contar consultas SQL reais ao converter DTOs das movimentações. Refinamento da evidência inicial: DespesaVariavelResponseDTO.fromEntity delega a ContaResponseDTO/CategoriaResponseDTO, que leem descrição e observações de entidades LAZY; portanto o caminho não se limita a acessar ID de proxy. A existência/magnitude de N+1 permanece sem medição.
- DW-153: medir custo da consulta de proprietário seguida de findById; não remover checagem de titularidade e preservar contratos de acesso. Prioridade menor.
- Progresso: 18 das 21 origens iniciais decididas; 8 para planejamento, 1 para planejamento condicional, 7 investigações aprovadas, 1 consolidado e 1 descartado. Faltam DW-27, DW-28 e DW-92. Nenhuma medição, teste ou implementação executada.

### Grupo 6 — login, perfil e cancelamento

Guilherme aprovou (“sim”) descartar DW-27, DW-28 e DW-92 deste planejamento. As alegações originais e evidências ficam preservadas; nenhum item foi marcado como correção entregue.

- DW-27: save() não comprova escrita SQL incondicional/desnecessária; perfil deve continuar atualizado no login.
- DW-28: chamada de /me identificada na inicialização, sem polling no cliente atual; cache adicional não justificado por custo medido.
- DW-92: cancelamento de consulta pode economizar transferência, mas não foi identificado impacto relevante para este escopo.

## Resultado final da validação interativa — 21 origens decididas

Todas as decisões abaixo foram aprovadas por Guilherme nos grupos 1 a 6. Não houve implementação, teste de aplicação, medição, migração, criação de spec, épico ou história.

| Encaminhamento aprovado | IDs | Quantidade |
|---|---|---:|
| Planejar correções/verificações de segurança | DW-13, DW-14, DW-26, DW-35, DW-36, DW-37, DW-60, DW-176 | 8 |
| Planejar com pré-condição | DW-161 — conferir catálogo e preservar UNIQUE antes de remover índice redundante; vinculado a A13 | 1 |
| Investigar antes de decidir implementação | DW-2, DW-160, DW-117, DW-139, DW-147, DW-153, DW-155 | 7 |
| Consolidado em outro registro | DW-38 em DW-160 | 1 |
| Descartados deste planejamento | DW-63, DW-27, DW-28, DW-92 | 4 |
| **Total das origens iniciais** | | **21** |

- **A10 do sprint:** investigação DW-139 aprovada; manter open até resolver e verificar todo o escopo aprovado.
- **A13 do sprint:** investigação DW-155 e planejamento condicional DW-161 aprovados; concluir apenas a remoção do índice redundante não encerra a ação inteira.
- **Conclusão obrigatória de futuras histórias:** declarar os DW e action items que resolve; atualizar backlog/deferred-work com data e evidência após testes/revisão e antes de marcar a história done. Não encerrar registro parcialmente atendido. História de investigação só pode afirmar investigação concluída, nunca correção implementada; eventual mudança de escopo do registro precisa decisão explícita.
- **Próxima etapa:** gerar planejamento apenas a partir destas decisões quando solicitado; não tratar investigações como otimizações ou mudanças arquiteturais já aprovadas.

## Conferência final da organização e próximo fluxo

- Conferidos: 190 origens D, 15 action items A (2 ativos e 13 históricos), 21 decisões finais (8 planejamento, 1 condicional, 7 investigações, 1 consolidado, 4 descartados), 16 DW abertos e 35 estados de desenvolvimento preservados.
- Corrigidas quatro referências ativas de specs arquivadas: DW-160, DW-153, DW-155 e DW-161. O caminho antigo foi preservado em source_spec_original e na transcrição histórica.
- Entradas reagrupadas conforme as decisões finais: planejamento, condicional, investigação de segurança, investigação de desempenho; registros retirados em seção histórica própria. Não há item ativo sem decisão.
- Vínculos A10 → DW-139 e A13 → DW-155/DW-161 conferidos; regra de fechamento com evidência permanece obrigatória.
- Esta branch planning/organizacao-backlog é exclusiva da organização. O próximo planejamento deve começar em NOVA branch planning/epico-seguranca-desempenho, antes de escrever SPEC/arquitetura/épico. Concluir/versionar a organização primeiro para não transportar alterações pendentes; nenhum commit/push foi autorizado ou executado nesta etapa.
- BMad Help local: próximo passo recomendado é [SPC] bmad-spec para consolidar o escopo validado; em seguida [CA] bmad-architecture para conferir a arquitetura brownfield, [CE] bmad-create-epics-and-stories e [SP] bmad-sprint-planning. Não refazer descoberta do produto nem tratar investigações como implementação aprovada.

## Autorização de versionamento da organização

Após a conferência final, Guilherme autorizou commit exclusivamente de deferred-work.md, sprint-status.yaml e deste relatório na branch planning/organizacao-backlog, sem push. A próxima etapa de planejamento deve criar planning/epico-seguranca-desempenho antes de escrever os novos documentos. As observações anteriores de ausência de autorização de commit referem-se às etapas anteriores a esta decisão.
