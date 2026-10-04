# Addendum — Aureus Product Brief

Este documento preserva contexto, decisões adiadas e diretrizes técnicas capturadas durante a elaboração do Product Brief, garantindo que não sejam esquecidas nas próximas fases de evolução do projeto (V2 e posteriores).

## 1. Autenticação e Segurança (OAuth 2.0 / Login)

- **Status:** Deferido para V2 (Não-Escopo da V1).
- **Contexto:** A modelagem de dados original (`database/aureus-database-erd.txt`) já contempla a entidade `usuarios` com `email` e `senha`, definindo uma arquitetura Single-Owner relacional onde todas as movimentações e contas apontam para um `usuario_id`.
- **Decisão V1:** Como a V1 será executada localmente para estudo pessoal do criador e validação das regras de consolidação, a autenticação foi removida do escopo inicial para evitar complexidade desnecessária. O sistema rodará em contexto de usuário padrão único local.
- **Diretrizes para V2:**
- Implementar autenticação moderna utilizando **OAuth 2.0 (Google Login)** juntamente com JWT ou sessão segura no Spring Security. - Manter a compatibilidade com a tabela `usuarios` já definida no ERD sem refatoração drástica das relações em `contas`, `categorias`, `despesas` e `receitas`.

## 2. Dark Mode e Personalização de Interface

- **Status:** Deferido para V2.
- **Contexto:** A filosofia de design definida (inspirada na simplicidade da Apple) foca primeiramente na clareza da grade de 24 meses e usabilidade direta.
- **Diretrizes para V2:**
- Estruturar o TailwindCSS utilizando variáveis CSS ou tokens de design nativos para habilitar Dark Mode sem poluição de classes nos componentes React. - Garantir contraste adequado para relatórios financeiros (destaque cromático para saldos positivos vs. negativos em ambientes escuros).

## 3. Infraestrutura Avançada (Docker Compose Completo, Kubernetes e CI/CD)

- **Status:** Deferido para fases avançadas de Portfólio.
- **Contexto:** O README do projeto cita ferramentas como Docker e Kubernetes.
- **Decisão V1:** Apenas o **PostgreSQL** será executado via container Docker na V1. O backend Java 21 e o frontend React 19 rodarão diretamente pelo código-fonte (via Maven/Gradle e Vite Dev Server) na máquina local do desenvolvedor.
- **Diretrizes Futuras:**
- Criar um `docker-compose.yaml` fullstack para empacotamento completo. - Especificar manifestos Kubernetes (`Deployment`, `Service`, `ConfigMap`, `Secret`) como vitrine de competências DevOps avançadas, demonstrando como a infraestrutura serve à aplicação.
