# Contratos e verificação de segurança

Companion de `SPEC.md`, derivado do recorte final aprovado. Os critérios abaixo orientam a execução futura; não são resultados de testes já executados.

## CAP-1 — Política pública e fallback (DW-13)

**Evidência atual:** `SecurityConfig.java` protege `/api/**`, mas termina em `anyRequest().permitAll()`. A dependência Actuator existe; sua exposição efetiva em produção não foi sondada.

- **SEG-01:** Inventariar as rotas públicas necessárias e registrar allowlist explícita, incluindo início/callback OAuth e política do Actuator. Decidir quais endpoints operacionais ficam públicos, protegidos ou indisponíveis; não presumir exposição de todos eles.
- **SEG-02:** Restringir o fallback conforme essa política, verificando chamadas anônimas/autenticadas à API, a rotas fora da allowlist e aos endpoints operacionais selecionados. Não aplicar `denyAll` indiscriminadamente sobre rotas OAuth legítimas.
- **SEG-03:** Demonstrar que início/callback OAuth continuam alcançáveis, `/api/auth/me` anônimo responde 401 e logout mantém seu contrato com CSRF. A allowlist não deve tornar dados financeiros públicos nem dispensar CSRF em mutações.

**Decisão pendente:** Q-1. A história precisa definir a política antes de implementar a restrição; a evidência do aceite deve identificar as rotas verificadas.

## CAP-2 — Emissão e expiração do cookie (DW-14, DW-36)

**Evidência atual:** `OAuth2LoginSuccessHandler.java` e `AuthController.java` já definem flags do cookie. `SecurityIT` cobre status e presença de Set-Cookie no logout, mas não todos os atributos; já há casos parciais de JWT expirado/inválido.

| Caso | Critérios de aceite futuros |
|---|---|
| **SEG-04 — DW-36: sucesso OAuth** | Cookie `AUREUS_SESSION` com `HttpOnly`, `Path=/`, `SameSite=Lax`; `Secure` conforme transporte seguro/inseguro; `Max-Age` em segundos coerente com `jwt.expiration / 1000`. |
| **SEG-05 — DW-14: logout** | Cookie de mesmo nome/caminho com valor vazio e `Max-Age=0`; assertivas de `HttpOnly`, `SameSite=Lax` e `Secure` conforme transporte, preservando status/CSRF do endpoint. |
| **SEG-06 — expiração existente** | Reutilizar a verificação de rejeição de JWT expirado/inválido e limpeza do cookie como evidência do contrato; completar apenas lacunas necessárias, sem declarar revogação antecipada. |

Os testes são focados em segurança/expiração. Extração de imagem, dados de perfil e avatar não fazem parte do aceite. A presença atual das flags não constitui uma vulnerabilidade comprovada.

## CAP-3 — Logout e sessão inválida durante uso (DW-26, DW-60)

**Evidência atual:** `Header.tsx` limpa `QueryClient` e `authStore` em `finally`, inclusive após erro de rede, e navega ao login. `App.tsx` consulta `/me` na inicialização; `apiClient.ts` lança `ApiError` para resposta não OK sem tratamento central de sessão inválida durante uso.

- **SEG-07 — DW-26:** Partindo de sessão autenticada e cache privado preenchido, acionar logout e verificar limpeza de estado/cache e retorno ao login tanto com resposta bem-sucedida quanto com falha de rede.
- **SEG-08 — DW-60:** Partindo de aplicação em uso com dados privados carregados, responder 401 a chamada autenticada e verificar invalidação da sessão local, limpeza de estado/cache privado e retorno ao login. Preservar os contratos de outros erros, incluindo 403/404.
- **SEG-09:** Registrar separadamente o que foi demonstrado no cliente e no servidor. Limpeza local com falha de rede não prova exclusão do cookie HttpOnly nem revogação do JWT; o backend já rejeita requisições sem sessão válida.

Não adicionar testes de aparência/avatar ou uma suíte genérica de navegação/E2E para satisfazer esses itens.

## CAP-4 — Contextos e resolver (DW-35, DW-37, DW-160)

**Evidência atual:** `JwtAuthenticationFilter.java` preenche os contextos e limpa `TenantContext`/`SecurityContextHolder` em `finally`. `CurrentTenantIdentifierResolverImpl.java` retorna `public` se o contexto é nulo/em branco e valida sessões existentes. `application.yaml` configura o resolver por nome de classe. Esses fatos, isoladamente, não comprovam vazamento ou falha de configuração.

- **SEG-10 — DW-35:** Verificar os dois contextos durante a requisição válida e sua limpeza após sucesso, token inválido/expirado, erro e exceção da cadeia. Reutilizar a mesma thread para outro usuário e requisição anônima, demonstrando que o contexto anterior não permanece.
- **SEG-11 — DW-37:** Verificar o contrato do resolver para tenant preenchido, nulo, vazio e somente espaços, incluindo validação de sessões existentes. O teste documenta o contrato atual; não autoriza substituir o fallback.
- **SEG-12 — DW-160, absorvendo DW-38:** Investigar consultas e persistência sem contexto, bootstrap e inicialização/ciclo de vida do resolver na configuração Hibernate vigente. Distinguir operação técnica legítima sem tenant de acesso a dados financeiros que deve estar isolado.
- **SEG-13 — DW-160:** Entregar evidência reproduzível, risco demonstrado ou não, necessidade de bootstrap e recomendação/decisão antes de alterar fallback/configuração. Nome de classe ou ausência de injeção Spring não justificam, sozinhos, migração para bean.

Reutilizar os testes cross-tenant existentes; não duplicá-los como nova entrega. DW-38 permanece origem consolidada de DW-160. Propagação assíncrona DW-63 está descartada por ausência de consumidor identificado.

## CAP-5 — Matriz residual CSRF e vínculos (DW-176)

**Evidência atual:** `SecurityIT` testa POST autenticado sem CSRF; `MultiTenancyIT` e `RestContractAndIntegrityIT` já exercitam isolamento e integridade. Falta delimitar os casos residuais do recorte aprovado.

- **SEG-14:** Inventariar a cobertura existente por operação/entidade antes de criar testes. A matriz de aceite identifica método, recurso/vínculo, usuários envolvidos, teste existente ou lacuna, resultado esperado e evidência.
- **SEG-15:** Completar os casos faltantes de PUT/DELETE autenticados sem token CSRF, demonstrando 403 e ausência de alteração/exclusão indevida.
- **SEG-16:** Completar somente vínculos cruzados de conta/categoria ainda não exercitados, preservando rejeição de vínculo alheio com 403 e recurso não acessível com 404, conforme contrato da operação. Verificar que a tentativa rejeitada não persiste alteração indevida.

Expansão genérica/exaustiva de validações de DTOs está excluída. Não encerrar apontamentos amplos de cobertura com base apenas nesse recorte de segurança.

## CAP-6 — Investigação de revogação (DW-2)

**Evidência atual:** JWT stateless em cookie; verificação de assinatura/expiração e logout por remoção do cookie. A remoção do cookie não demonstra invalidação de uma cópia do JWT antes de expirar.

- **SEG-17:** Descrever o contrato atual e investigar necessidade, risco e custo de revogação antes da expiração, incluindo as consequências do logout e da falha de rede.
- **SEG-18:** Comparar alternativas pertinentes com evidência e registrar decisão ou pendências explícitas. Se uma mudança for necessária, apresentar o escopo para decisão posterior, sem implementá-la como parte da investigação.

Renovação automática não foi aprovada. Refresh token, tabela de sessões ou outra arquitetura não são soluções obrigatórias.

## Referências de execução

Os caminhos abaixo são relativos à raiz do repositório e localizam a evidência estática; o futuro aceite precisa registrar revisão e resultados efetivamente executados.

- Backend: `backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/{SecurityConfig,OAuth2LoginSuccessHandler,JwtAuthenticationFilter,CurrentTenantIdentifierResolverImpl}.java`, `controller/AuthController.java` no mesmo pacote base e `backend-api/src/main/resources/application.yaml`.
- Frontend: `frontend-web/src/components/Header/Header.tsx`, `frontend-web/src/App.tsx`, `frontend-web/src/services/apiClient.ts`.
- Testes existentes: `backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java` e `backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/{MultiTenancyIT,RestContractAndIntegrityIT}.java`.

Cada história aplica também o protocolo de atualização/encerramento de `rastreabilidade.md`.
