# 📜 Guia de Contribuição

Este guia contém as regras para contribuição com o projeto, mantendo o projeto simples de acompanhar para os visitantes.

---

# 🔀 Nomenclatura de Branches

Utilize o padrão abaixo para identificar de forma clara o que cada branche está implementando.

Prefixo | Uso | Exemplo
-- | -- | --
arch/ | Arquitetura, Banco de Dados, Estrutura | arch/10-diagrama-classes
design/ | Wireframes, UI, Estilos | design/12-tela-login
docs/ | Planejamento, Textos Gerais | docs/05-readme-inicial
fix/ | Correções de erros | fix/15-typo-erd

---

# 🏷️ Tipos de Commit

Este projeto segue a convenção do **Angular (Conventional Commits)**. O objetivo é manter um histórico de alterações legível, facilitar a automação de releases e simplificar a identificação de mudanças.

Utilize os prefixos abaixo para categorizar suas alterações:

| Prefixo (Type) | Descrição | Exemplo de Uso |
| :--- | :--- | :--- |
| **feat** | Adiciona uma nova funcionalidade (feature) ao projeto. | `feat: adiciona filtro de busca na home` |
| **fix** | Corrige um erro (bug) no código. | `fix: corrige erro de validação no formulário` |
| **docs** | Alterações que afetam apenas a documentação. | `docs: atualiza instruções no README` |
| **style** | Alterações de formatação (espaços, ponto e vírgula, lint) que não afetam a lógica do código. | `style: remove espaços em branco desnecessários` |
| **refactor** | Alteração de código que não adiciona funcionalidade nem corrige bugs (melhoria estrutural). | `refactor: simplifica lógica do serviço de auth` |
| **perf** | Uma mudança de código que melhora o desempenho. | `perf: diminui tempo de carregamento das imagens` |
| **test** | Adição de testes ausentes ou correção de testes existentes. | `test: adiciona testes unitários para o header` |
| **build** | Mudanças que afetam o sistema de build ou dependências externas (npm, gulp, webpack). | `build: atualiza versão do moment.js` |
| **ci** | Mudanças nos arquivos e scripts de configuração de CI (GitHub Actions, Jenkins, Travis, etc.). | `ci: migra pipeline do Travis para o CircleCI` |
| **chore** | Outras mudanças que não modificam arquivos src ou de teste (tarefas utilitárias). | `chore: atualiza .gitignore` |
| **revert** | Reverte um commit anterior. | `revert: reverte commit a1b2c3` |

---

## 📝 Formato da Mensagem

A mensagem do commit deve seguir estritamente a estrutura:

```text
<tipo>(<escopo opcional>): <descrição>

[Corpo opcional]

[Rodapé opcional]
```

### Regras de Ouro 🌟
1.  **Imperativo:** Use verbos no imperativo ("adiciona" ao invés de "adicionado").
2.  **Minúsculas:** Comece a descrição com letra minúscula.
3.  **Sem Ponto:** Não use ponto final `.` no final da primeira linha.
4.  **Comprimento:** Tente manter a primeira linha com no máximo 72 caracteres.

---

## 🔍 Exemplos Práticos

**1. Nova funcionalidade simples**
> `feat: adiciona filtro de busca na listagem de usuários`

**2. Correção de bug com escopo**
> `fix(auth): corrige erro de validação no login`

**3. Alteração de estilo/formatação**
> `style: remove espaços em branco desnecessários`

**4. Breaking Change (Mudança que quebra compatibilidade)**
Adicione um `!` após o tipo ou explique no rodapé.
> `feat!: altera estrutura de retorno da API de produtos`

---

## 💥 Breaking Changes

Caso o commit inclua mudanças que quebrem a compatibilidade com versões anteriores (Breaking Changes), deve-se adicionar a flag `BREAKING CHANGE:` no rodapé do commit:

```text
feat: atualiza biblioteca de gráficos

BREAKING CHANGE: Método render() agora aceita apenas objetos de configuração.
```

---