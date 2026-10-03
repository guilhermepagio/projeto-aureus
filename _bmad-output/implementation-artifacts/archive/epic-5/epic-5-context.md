# Epic 5 Context: Saneamento Arquitetural e Resolução de Débitos Técnicos (Hardening)

<!-- Compiled from planning artifacts. Edit freely. Regenerate with compile-epic-context if planning docs change. -->

## Goal

Resolver de forma definitiva os débitos técnicos e pendências arquiteturais acumulados nos Épicos 1 a 4, desacoplando o backend por meio de Service Layer e DTOs Records, estabelecendo tratamento centralizado de exceções, garantindo validação estrita de tenant nos relacionamentos financeiros, unificando o cliente de API no frontend e eliminando a duplicação dos formulários de movimentações. O épico assegura a robustez e a manutenibilidade do sistema, servindo como base estrutural obrigatória para a automação de testes do Épico 6.

## Stories

- Story 5.1: Backend Service Layer e DTOs (Records) para Entidades Financeiras
- Story 5.2: Tratamento Global de Erros, Validação de Tenant em Relacionamentos e Índices
- Story 5.3: Centralização do Cliente HTTP/API e Resiliência no Frontend
- Story 5.4: Abstração e Unificação dos Formulários de Movimentações Financeiras (DRY)

## Requirements & Constraints

- **Desacoplamento e DTOs Imutáveis**: Os controllers REST não devem acessar repositories diretamente nem expor entidades JPA em parâmetros ou retornos; todas as operações devem transitar via DTOs imutáveis (Java Records) com Bean Validation (`@Valid`, `@NotNull`, `@Positive`, etc.), preservando os contratos JSON consumidos pelo cliente.
- **Tratamento Global de Exceções**: A API deve interceptar erros de forma padronizada via `@RestControllerAdvice`, convertendo falhas de validação (`MethodArgumentNotValidException`) e violações de chave estrangeira/integridade (`DataIntegrityViolationException`) em `400 Bad Request` amigáveis com detalhamento de campos, recursos ausentes em `404 Not Found` e erros não tratados em `500 Internal Server Error` sem exposição de stacktrace.
- **Validação de Multi-Tenancy em Relacionamentos**: Ao criar ou atualizar receitas e despesas vinculadas a uma Conta e/ou Categoria, a camada de serviço deve validar se os registros referenciados pertencem ao usuário autenticado, rejeitando violações com `403 Forbidden` ou `404 Not Found`.
- **Integridade da Consolidação**: Padronizar o tratamento de movimentações financeiras sem conta vinculada no cálculo da consolidação de 24 meses, assegurando coerência e evitando divergências entre agregações de contas e categorias.
- **Performance de Consultas**: Criar migração de banco de dados adicionando índices em colunas críticas de consulta frequente: `google_subject_id` na tabela de usuários e `data_inicio`/`data_fim` nas tabelas de movimentações.
- **Centralização de Comunicação HTTP**: Todas as requisições frontend devem passar por um wrapper/cliente HTTP centralizado responsável por configurar URL base, cabeçalho de CSRF para mutações (`POST`, `PUT`, `DELETE`), headers padrão, controle de timeout via `AbortSignal` e tratamento de erros estruturados da API.
- **Resiliência contra Falhas de Renderização**: A raiz da aplicação frontend deve ser protegida por um `ErrorBoundary` global com interface de fallback amigável e ação para recarregar ou tentar novamente, impedindo telas brancas por exceções em componentes.
- **Acessibilidade e Usabilidade em Modais**: O componente genérico de modal deve implementar Focus Trap (confinamento de foco por teclado), suporte a fechamento via tecla `Escape`, retorno automático do foco ao elemento de disparo e atributos ARIA semânticos (`role="dialog"`, `aria-labelledby`, `aria-describedby`).
- **Redução de Duplicação em Formulários (DRY)**: Formulários e modais de movimentações financeiras (despesas e receitas, fixas e variáveis) devem compartilhar componentes e campos comuns (descrição, valor, conta, categoria, data/mês, observações).

## Technical Decisions

- **Modularidade Backend**: Estruturação estrita `Controller -> Service -> Repository`. Classes de serviço (`ContaService`, `CategoriaService`, `DespesaFixaService`, `ReceitaFixaService`, `DespesaVariavelService`, `ReceitaVariavelService`) centralizam regras de negócio, isolamento transacional (`@Transactional`) e operações de persistência.
- **Java Records para Contratos de API**: DTOs de Request e Response imutáveis substituem entidades `@Entity` nas camadas expostas, protegendo contra Mass Assignment e vazamento de dados internos.
- **Validação Cruzada de Propriedade de Dados**: Regra de segurança em nível de serviço que valida se `contaId` e `categoriaId` informados no payload pertencem ao `usuario_id` extraído do contexto de autenticação da sessão.
- **Versionamento de Banco via Flyway**: Todos os índices novos devem ser declarados em script SQL versionado pelo Flyway, mantendo compatibilidade com o histórico de migrações existente.
- **Fetcher/API Client Centralizado**: Módulo frontend unificado que substitui chamadas diretas a `window.fetch`, automatizando a injeção do token CSRF obtido via cookie/header e o tratamento de responses de erro estruturadas.
- **Componentização Compartilhada de Movimentações**: Extração de blocos reutilizáveis para os campos recorrentes dos formulários de transações, mantendo a especificidade apenas de campos exclusivos (ex.: parcelamento ou local de compra).

## UX & Interaction Patterns

- **Comportamento Acessível de Janelas Modais**: Foco inicial direcionado para o primeiro campo interativo do modal; ciclo de tabulação restrito ao diálogo aberto; fechamento rápido com tecla `Escape`; restauração do foco do navegador para o gatilho original após o fechamento.
- **Recuperação de Falha de UI (Fallback)**: Exibição de componente visual informativo quando ocorre erro de renderização, com orientação clara e botão "Tentar Novamente" sem necessidade de recarregar manualmente a aba do navegador.
- **Apresentação de Erros de Validação da API**: O frontend consome os payloads de erro estruturados retornados pelo `@RestControllerAdvice` para exibir mensagens de validação e alertas pontuais e claros para o usuário.

## Cross-Story Dependencies

- **Pré-requisitos**: Depende das entidades, telas e contratos desenvolvidos ao longo dos Épicos 1 (Autenticação/Shell), 2 (Contas e Categorias), 3 (Lançamentos) e 4 (Consolidação).
- **Interdependência Interna**:
  - Story 5.1 e Story 5.2 estabelecem a Service Layer, Records e o `@RestControllerAdvice`, padronizando as respostas de erro e contratos consumidos pelo frontend.
  - Story 5.3 depende das respostas estruturadas da Story 5.2 para implementar o tratamento de erros no cliente HTTP centralizado.
  - Story 5.4 constrói os componentes compartilhados de formulário e o Modal com Focus Trap consumindo os hooks refatorados na Story 5.3.
- **Bloqueador para o Épico 6**: O Épico 5 é o alicerce obrigatório para o Épico 6 (Estratégia de Testes), que implementará testes unitários sobre a Service Layer e testes de integração REST/multitenancy sobre os endpoints protegidos e DTOs criados aqui.
