# Revisão de acessibilidade — contratos de UX

**Escopo:** `DESIGN.md` e `EXPERIENCE.md`  
**Resultado:** aprovado com ajustes antes de usar estes contratos como modelo dos
documentos públicos.

## Achados

| Severidade | Evidência | Impacto e recomendação |
| --- | --- | --- |
| Média | Ambos os arquivos iniciam o conteúdo em `##` (`Brand & Style` e `Foundation`), sem um `#` visível. | A hierarquia de títulos fica incompleta para leitores de tela, navegação por cabeçalhos e leitura isolada da página. Incluir um único `#` descritivo após o front matter: por exemplo, “Design da documentação do Aureus” e “Experiência da documentação do Aureus”. |
| Média | Há placeholders literais no texto, como `{colors.system}`, `{typography.headings}`, `{spacing.system}` e `{components.links}`. | Quem lê a página recebe termos sem significado e um leitor de tela os anuncia como caracteres. Substituir pelos valores finais (“tema nativo claro/escuro do GitHub”, “títulos Markdown”, “links Markdown nativos” etc.). |
| Baixa | As tabelas de Arquitetura da informação, Voz e tom, Padrões de componente e Estados têm três colunas. | São tabelas semanticamente corretas e com cabeçalhos, mas a leitura em mobile pode exigir rolagem horizontal. Manter apenas comparações compactas; complementar a informação decisiva em frases curtas antes/depois da tabela, sobretudo nos futuros índices públicos. |
| Baixa | O ERD futuro é nomeado como `aureus-erd.png`, e o contrato só estabelece que imagens “recebem texto alternativo significativo quando publicadas”. | O requisito é bom, porém fácil de perder na implementação. No README de cada diagrama, exigir: descrição curta da relação principal, link para a fonte editável DBML e um texto alternativo específico ao inserir a imagem. Não usar “ERD do Aureus” como alt. |

## Verificações aprovadas

- Status são definidos por palavras (“Disponível”, “Planejado”, “Em revisão” e “Arquivado”) e não dependem exclusivamente de cor.
- Há diretriz explícita para conteúdo essencial em Markdown, links descritivos, títulos sequenciais e alternativas textuais para diagramas.
- A escolha de GFM nativo, sem cards HTML, imagens hero ou badges, preserva zoom, teclado, leitor de tela e os temas claro/escuro do GitHub.
- O bloco de árvore usa texto, que é apropriado para estrutura de diretórios e acessível quando mantido curto.
- A sequência principal README → PRD → técnico está presente em prosa e não depende somente da tabela.

## Parecer

Os contratos estabelecem uma base acessível e particularmente adequada para um
repositório solo. Corrigir o nível inicial de título e os placeholders antes de
derivar `README.md`, `CONTRIBUTING.md` e os índices em `docs/`; os demais pontos
podem ser verificados durante essa derivação.
