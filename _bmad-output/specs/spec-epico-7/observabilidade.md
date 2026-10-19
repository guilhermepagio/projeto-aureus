# Observabilidade e logs de troubleshooting

Companion de `SPEC.md`, derivado da decisão de 2026-10-19. Define os contratos de correlação, instrumentação de logs e proteção de dados sensíveis no backend para diagnóstico e troubleshooting.

## CAP-10 — Correlação por requisição e logs operacionais

**Evidência atual:** apenas `GlobalExceptionHandler.java` declara `Logger` no `backend-api`, registrando dois eventos (`warn` em integridade relacional e `error` em exceção genérica). Filtros de autenticação, login/logout OAuth, rejeições 401/403/404 e serviços de negócio não registram trilha de diagnóstico nem contexto no `MDC`.

### Correlação e ciclo de vida (`MDC`)

- **OBS-01:** Preencher o `MDC` (SLF4J) no início do processamento da requisição com um identificador curto de requisição (`requestId`) e, após resolução da autenticação/tenant, com `tenantId` (ou marcador explícito de requisição anônima).
- **OBS-02:** Garantir limpeza integral do `MDC` (`remove`/`clear`) em bloco `finally` ao término da requisição, inclusive após falha de autenticação, token inválido/expirado ou exceção não tratada, em conjunto com a verificação de `TenantContext` e `SecurityContextHolder` (`DW-35`).
- **OBS-03:** Configurar o padrão de saída do Logback (`application.yaml`) para exibir `requestId` e `tenantId` presentes no `MDC` sem exigir infraestrutura externa de coleta.

### Pontos de instrumentação e níveis de severidade

| Componente | Evento e nível | Conteúdo obrigatório |
|---|---|---|
| **Autenticação e sessão (`OAuth2LoginSuccessHandler`, `AuthController`, `JwtAuthenticationFilter`)** | `INFO` em login OAuth e logout concluídos; `DEBUG`/`WARN` em token JWT ausente/inválido/expirado | Rota, resultado da operação e motivo técnico da rejeição (sem expor o token ou cookie). |
| **Erros e rejeições (`GlobalExceptionHandler`)** | `WARN` em violações de domínio/integridade, acesso negado (`403`) e entradas inválidas relevantes; `DEBUG` em `404`; `ERROR` (com stack trace) em falhas `500` | Código HTTP, tipo da exceção/regra acionada, método e URI da requisição. |
| **Serviços financeiros e consolidação (`*Service`)** | `INFO`/`DEBUG` em operações mutantes (criação, atualização, exclusão) e medição de tempo/volume na consolidação | Entidade, operação, IDs técnicos (`id`, `contaId`, `categoriaId`) e, na consolidação, contagem de itens/duração em `DEBUG`. |

### Proteção de dados sensíveis e verificação

- **OBS-04:** Proibir o registro em log de tokens JWT, valores de cookies (`AUREUS_SESSION`, `XSRF-TOKEN`), valores monetários (`valor`, `valorParcela`, saldos), descrições pessoais de lançamentos/contas/categorias e dados pessoais de perfil (`email`, `nome`, `fotoPerfil`).
- **OBS-05:** Verificar por testes automatizados que o `MDC` é populado durante a requisição e limpo ao final (sem vazamento entre threads reutilizadas) e que os fluxos de erro/autenticação não vazam segredos ou valores financeiros nas mensagens registradas.

## Referências de execução

- Backend: `backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/{JwtAuthenticationFilter,OAuth2LoginSuccessHandler}.java`, `controller/AuthController.java`, `exception/GlobalExceptionHandler.java`, `service/{ConsolidacaoService,ContaService,CategoriaService,DespesaFixaService,ReceitaFixaService,DespesaVariavelService,ReceitaVariavelService}.java` e `backend-api/src/main/resources/application.yaml`.
