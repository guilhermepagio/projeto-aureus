# Validação — estrutura e consistência

**Veredito:** aprovado com ajustes antes de aplicar a reorganização. A jornada
`README → PRD → técnico` é clara, tem escopo correto (organização do
repositório, sem wireframes) e comunica bem o contexto de portfólio solo.

## Achados

| Severidade | Evidência | Impacto | Ajuste recomendado |
| --- | --- | --- | --- |
| Alta | `EXPERIENCE.md` define `docs/` como fonte canônica, mas o front matter de ambos os contratos ainda aponta Brief e PRD em `_bmad-output`; o texto também diz apenas que o fluxo BMad “deve ser adaptado”. | A migração pode criar duas versões concorrentes ou deixar o README apontando para documentos que o BMad continuará sobrescrevendo em outro local. | Antes da mudança, definir um plano explícito de migração: origem → destino, qual cópia é descontinuada/arquivada, como links antigos serão tratados e qual configuração/processo BMad passa a gravar ou sincronizar `docs/`. |
| Média | A árvore reserva `docs/technical/domain-model/README.md`, mas não nomeia nem posiciona o atual diagrama de classes, embora o contrato diga que ele será uma documentação técnica acompanhada de fonte editável, propósito e premissas. | O artefato pode ficar perdido em `api/` ou ser copiado sem uma fonte única, enfraquecendo a rota técnica prometida. | Declarar o destino e a fonte do modelo de domínio (por exemplo, arquivo-fonte e imagem/SVG), e exigir que o README do domínio exponha resumo textual, status e a divergência de autenticação. |
| Média | `docs/decisions/README.md` é previsto e o fluxo pede registrar decisões, mas não há convenção para um registro individual nem uma primeira decisão obrigatória. | A rastreabilidade dependerá de memória pessoal; a divergência PRD sem autenticação × modelos com `Usuario` pode persistir sem dono nem conclusão visível. | Criar uma convenção leve para decisões (`YYYY-MM-DD-slug.md`, contexto, decisão, impacto, status e links) e abrir a primeira decisão sobre autenticação antes de marcar os diagramas como disponíveis. |
| Baixa | O estado `Em revisão` é previsto para os documentos técnicos, porém a tabela de superfícies não diferencia o estado esperado do ERD e do modelo de domínio. | Um visitante pode interpretar a rota técnica como definitiva, apesar da ressalva aparecer somente mais abaixo. | No futuro índice técnico, mostrar o status de cada artefato e a decisão pendente junto ao respectivo link. |

## Pontos validados

- A arquitetura de informação atende à prioridade definida: proposta do produto,
  PRD e então documentação técnica.
- O posicionamento “portfólio pessoal, solo e não comercial” é transparente sem
  simular uma operação comercial ou equipe inexistente.
- A exclusão de wireframes desta etapa está explícita e evita expansão indevida
  de escopo.
- Os requisitos de acessibilidade preservam conteúdo textual, links descritivos,
  hierarquia de títulos e diagramas com explicação alternativa.

## Condição para seguir

Aplicar a reorganização após registrar o plano de migração/canonicidade e o
destino do diagrama de classes. A decisão de autenticação pode permanecer em
revisão, desde que fique visível na rota técnica e seja registrada em
`docs/decisions/`.
