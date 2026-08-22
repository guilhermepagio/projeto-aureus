---
title: "PRD: Aureus"
status: final
created: 2026-08-02
updated: 2026-08-13
---

# PRD: Aureus

## 0. Propósito do Documento

Este PRD especifica os requisitos funcionais e não-funcionais da V1 do **Aureus**, uma plataforma web de gestão de finanças pessoais. O documento serve como contrato entre as fases de planejamento e implementação dentro da abordagem **Spec-Driven Development (SDD)** adotada no projeto, sendo a fonte única da verdade para arquitetura, UX, backend e frontend.

O PRD foi construído sobre o Product Brief (`brief.md`), o Addendum do Brief (`addendum.md`), o ERD em DBML (`database/aureus-database-erd.txt`) e o Diagrama de Classes do domínio Java (`api/README.md`). Os termos utilizados seguem o Glossário (§3) de forma estrita.

## 1. Visão

O **Aureus** digitaliza um método de controle financeiro pessoal validado por mais de dois anos em planilha. O sistema permite registrar receitas e despesas — fixas ou variáveis, com ou sem parcelamento — e consolida tudo em um painel de grade mensal com visão de 24 meses, oferecendo projeção de saldo acumulado para tomada de decisão financeira.

A filosofia central é **forward-looking**: o Aureus existe para acompanhar o mês atual e planejar os próximos. Não há necessidade de rastrear histórico. Quando um valor fixo muda na realidade (aumento de salário, reajuste de internet), o usuário simplesmente atualiza o registro e segue — o sistema projeta o novo valor para frente sem versionar o passado.

O nome remete ao *aureus*, moeda de ouro da Roma Antiga, simbolizando valor e estabilidade. O projeto também funciona como portfólio profissional de engenharia, demonstrando domínio de Spec-Driven Development com abordagem AI-First, onde especificações rigorosas precedem e guiam toda a codificação.

## 2. Usuário-Alvo

### 2.1 Jobs To Be Done

- **Funcional:** Registrar todas as minhas movimentações financeiras (fixas e variáveis, receitas e despesas) num único lugar, com suporte nativo a parcelamentos, para saber exatamente quanto entra e sai por mês.
- **Funcional:** Visualizar uma projeção financeira de 24 meses que me mostre o impacto acumulado de cada decisão de compra antes de tomar a decisão.
- **Emocional:** Ter controle e tranquilidade financeira ao ver claramente se o saldo acumulado cresce ou aperta nos meses à frente.
- **Contextual:** Conferir faturas do cartão de crédito com facilidade, comparando os valores registrados no Aureus com os cobrados na fatura.

### 2.2 Não-Usuários (V1)

- Múltiplos usuários com perfis, permissões ou compartilhamento de dados — a V1 autentica contas individuais, mas permanece sem colaboração entre usuários.
- Contadores ou profissionais de finanças que precisam de relatórios fiscais ou contábeis formais.
- Usuários que necessitam de integração bancária automática (Open Banking).

### 2.3 Jornadas do Usuário

**UJ-1. Guilherme prepara o Aureus para começar a controlar suas finanças.**

> **Persona + contexto:** Guilherme, desenvolvedor que controla finanças pessoais há mais de dois anos em planilha. Acabou de rodar o Aureus localmente pela primeira vez.
>
> **Entry state:** Sistema vazio, sem dados. Rodando local.
>
> **Path:**
> 1. Abre o Aureus e navega até o cadastro de **Contas**. Registra "Conta Corrente", "Cartão de Crédito A" e "Cartão de Crédito B".
> 2. Navega até o cadastro de **Categorias**. Registra categorias macro: "Essencial", "Não Essencial", "Investimentos", "Lazer".
> 3. Com Contas e Categorias cadastradas, o sistema está pronto — as abas de Despesas e Receitas agora podem ser utilizadas.
>
> **Climax:** Guilherme vê que as Contas e Categorias aparecem como opções nos formulários de Despesas e Receitas. O sistema está operacional.
>
> **Resolution:** Sistema preparado para receber Movimentações. Guilherme pode ir para qualquer aba na ordem que quiser.
>
> **Edge case:** Se tentar cadastrar uma Despesa ou Receita sem ter Contas ou Categorias registradas, o sistema impede a operação (campos obrigatórios sem opções disponíveis).

**UJ-2. Guilherme registra uma compra parcelada no cartão de crédito.**

> **Persona + contexto:** Guilherme comprou um fone Air Pods Pro 2ª Gen em 3x no Cartão de Crédito A. Quer registrar para que o impacto apareça nos próximos meses do Painel de Consolidação.
>
> **Entry state:** Contas e Categorias já cadastradas.
>
> **Path:**
> 1. Abre a aba **Despesas Variáveis**.
> 2. Preenche o formulário:
>    - **Descrição:** "Fone de Ouvido Air Pods Pro 2nd Gen" *(obrigatório)*
>    - **Local Compra:** "Mercado Livre" *(opcional — facilita conferência de fatura)*
>    - **Data Compra:** 15/08/2026 *(opcional — data em que a compra foi computada no cartão)*
>    - **Valor Parcela:** R$ 124,00 *(obrigatório)*
>    - **Nº Parcelas:** 3 *(obrigatório)*
>    - **Valor Total:** R$ 372,00 *(calculado: Valor Parcela × Nº Parcelas)*
>    - **Primeira Parcela:** 01/09/2026 *(obrigatório — dia 1 do mês da fatura)*
>    - **Última Parcela:** 01/11/2026 *(calculado: Primeira Parcela + (Nº Parcelas − 1) meses)*
>    - **Categoria:** Não Essencial *(obrigatório)*
>    - **Conta:** Cartão de Crédito A *(obrigatório)*
>    - **Notas:** "Presente de aniversário do João" *(opcional)*
> 3. Salva o registro.
>
> **Climax:** O sistema exibe uma linha com todas as informações da compra registrada. Guilherme confere visualmente que está correto.
>
> **Resolution:** A Despesa Variável está registrada e será distribuída automaticamente nos meses set/out/nov no Painel de Consolidação.
>
> **Edge case:** Se esquecer de preencher Categoria ou Conta, o sistema impede o salvamento.

**UJ-3. Guilherme registra suas despesas e receitas fixas mensais.**

> **Persona + contexto:** Guilherme quer registrar o salário (Receita Fixa) e a internet (Despesa Fixa) — valores constantes todo mês.
>
> **Entry state:** Contas e Categorias já cadastradas.
>
> **Path:**
> 1. Abre a aba **Receitas Fixas**. Preenche: Descrição "Salário", Valor R$ 5.000, Categoria "Essencial", Conta "Conta Corrente". Salva.
> 2. Abre a aba **Despesas Fixas**. Preenche: Descrição "Internet", Valor R$ 120, Categoria "Essencial", Conta "Conta Corrente". Salva.
>
> **Climax:** Os registros fixos aparecem listados em suas abas. No Painel de Consolidação, R$ 5.000 e R$ 120 se repetem em todos os 24 meses.
>
> **Resolution:** Quando o salário aumentar ou a internet reajustar, Guilherme simplesmente edita o valor e o sistema projeta o novo valor para frente.

**UJ-4. Guilherme registra uma receita variável (PLR em parcelas).**

> **Persona + contexto:** Guilherme vai receber a PLR 2026 em 2 parcelas de R$ 3.000.
>
> **Entry state:** Contas e Categorias já cadastradas.
>
> **Path:**
> 1. Abre a aba **Receitas Variáveis**. Preenche: Descrição "PLR 2026", Valor Parcela R$ 3.000, Nº Parcelas 2, Primeira Parcela 01/09/2026, Categoria "Essencial", Conta "Conta Corrente". Salva.
> 2. O sistema calcula: Valor Total R$ 6.000, Última Parcela 01/10/2026.
>
> **Climax:** No Painel de Consolidação, a linha Conta Corrente no bloco de Receitas mostra R$ 8.000 em set e out (salário + PLR), e R$ 5.000 nos demais meses.
>
> **Resolution:** Guilherme tem visibilidade exata de quando a PLR impacta o fluxo de caixa.

**UJ-5. Guilherme consulta o Painel de Consolidação para planejar os próximos meses.**

> **Persona + contexto:** Guilherme tem diversas movimentações cadastradas e quer visualizar a projeção financeira.
>
> **Entry state:** Sistema com dados. Múltiplas movimentações fixas e variáveis registradas.
>
> **Path:**
> 1. Abre a aba **Consolidação**. Clica no botão "Mês Atual" para definir Agosto/2026 como primeiro mês (ou digita uma data manualmente).
> 2. Visualiza o **Bloco Receitas** — linhas por Conta mostrando o valor mensal. Conta Corrente mostra R$ 5.000 (fixo) em todos os meses, com R$ 8.000 em set e out (salário + PLR).
> 3. Visualiza o **Bloco Despesas** — linhas por Conta. Cartão A mostra R$ 124 em set/out/nov (fone parcelado) somado às Despesas Fixas daquela Conta. Internet fixa aparece constante todo mês.
> 4. Visualiza o **Bloco Categorias (R$)** — valor absoluto gasto por Categoria em cada mês (apenas Despesas).
> 5. Visualiza o **Bloco Categorias (%)** — percentual de cada Categoria sobre o total gasto no mês (apenas Despesas).
> 6. Visualiza o **Bloco Resumo** — Total Gasto no Mês, Sobra do Mês (Receitas − Despesas) e Sobra Retroativa Acumulada.
>
> **Climax:** Guilherme percebe que em novembro a sobra do mês fica apertada por causa das parcelas acumuladas. Com a visão dos 24 meses, decide adiar uma compra grande para dezembro quando as parcelas do fone terminam.
>
> **Resolution:** Decisão financeira tomada com confiança e visibilidade clara do impacto futuro.

**UJ-6. Guilherme entra no Aureus usando sua conta Google.**

> **Persona + contexto:** Guilherme acessa o Aureus pelo navegador e quer proteger seus dados financeiros sem criar e manter uma senha adicional.
>
> **Entry state:** Usuário não autenticado.
>
> **Path:**
> 1. Abre a tela de entrada e seleciona “Entrar com Google”.
> 2. É redirecionado ao Google para autenticação e consentimento, quando aplicável.
> 3. Após o retorno bem-sucedido, o Aureus identifica ou cria a conta local associada ao identificador estável do Google e abre o painel.
> 4. Ao acessar novamente, uma sessão autenticada permite consultar e alterar somente os dados financeiros vinculados à sua própria conta.
>
> **Climax:** Guilherme acessa seus dados sem cadastrar senha no Aureus.
>
> **Resolution:** A sessão é encerrada pelo comando “Sair” e o usuário volta à tela de entrada.
>
> **Edge cases:** O sistema não cria uma conta quando o consentimento é negado ou a resposta do provedor é inválida; erros de autenticação não revelam dados de outras contas.

## 3. Glossário

- **Conta** — Origem ou destino do dinheiro. Representa uma fonte financeira concreta do Usuário (ex: Conta Corrente, Cartão de Crédito A). Toda Movimentação é vinculada a exatamente uma Conta.
- **Categoria** — Classificação macro da Movimentação (ex: Essencial, Não Essencial, Investimentos, Lazer). Toda Movimentação é vinculada a exatamente uma Categoria. Na V1, Categorias são flat (sem hierarquia de subcategorias).
- **Movimentação** — Termo genérico para qualquer registro financeiro. Subdivide-se em Despesa e Receita, cada uma podendo ser Fixa ou Variável.
- **Despesa** — Movimentação de saída de dinheiro. Pode ser Fixa (valor recorrente constante) ou Variável (evento pontual, potencialmente parcelado).
- **Receita** — Movimentação de entrada de dinheiro. Pode ser Fixa (valor recorrente constante) ou Variável (evento pontual, potencialmente parcelado).
- **Fixa** — Qualificador de Movimentação cujo valor se repete identicamente em todos os meses do Painel de Consolidação. Quando o valor muda na realidade, o Usuário atualiza o registro in-place.
- **Variável** — Qualificador de Movimentação que possui parcelamento e aparece apenas nos meses correspondentes às parcelas. Inclui compras à vista (1 parcela), parceladas e financiamentos.
- **Parcelamento** — Conjunto de campos que define a distribuição temporal de uma Movimentação Variável: Valor Parcela, Nº Parcelas, Primeira Parcela e os campos calculados Valor Total e Última Parcela.
- **Painel de Consolidação** — Grade financeira de 24 meses que consolida todas as Movimentações em cinco blocos analíticos, permitindo projeção de saldo futuro.
- **Sobra do Mês** — Diferença entre o total de Receitas e o total de Despesas de um mês específico.
- **Sobra Retroativa Acumulada** — Soma cumulativa das Sobras do Mês, iniciando em zero no primeiro mês da grade selecionada. Fórmula: `Retroativa[mês] = Sobra[mês] + Retroativa[mês − 1]`, onde `Retroativa[primeiro mês] = Sobra[primeiro mês]`.
- **Usuário** — Pessoa que opera o sistema. Na V1, cada Usuário possui uma conta autenticada e só acessa suas próprias Movimentações, Contas e Categorias.
- **Provedor de Identidade** — Serviço externo que autentica o Usuário e fornece as afirmações de identidade autorizadas pelo fluxo OIDC. Na V1, o provedor é o Google.
- **Identidade Externa** — Vínculo entre um Usuário local e o identificador estável emitido pelo Provedor de Identidade. O e-mail exibido pelo provedor não é o identificador técnico do vínculo.
- **Sessão** — Estado autenticado mantido pelo Aureus após o login, com expiração e encerramento explícito.

## 4. Features

### 4.1 Gestão de Contas

**Descrição:** O Usuário cadastra Contas que representam origens e destinos do dinheiro (ex: Conta Corrente, Cartão de Crédito A). Contas são universais — uma vez cadastradas, ficam disponíveis como opção em todas as abas de Movimentação. O Usuário pode criar, visualizar, editar e excluir Contas. Realiza UJ-1.

**Requisitos Funcionais:**

#### FR-1: Criar Conta

O Usuário pode criar uma Conta informando Descrição (obrigatório) e Observações (opcional).

**Consequências (testáveis):**
- Conta criada aparece na listagem de Contas.
- Conta criada fica disponível como opção nos selects de Conta em todas as abas de Movimentação.

#### FR-2: Visualizar Contas

O Usuário pode visualizar todas as Contas cadastradas em formato de lista.

**Consequências (testáveis):**
- A listagem exibe Descrição e Observações de cada Conta.

#### FR-3: Editar Conta

O Usuário pode editar a Descrição e Observações de uma Conta existente.

**Consequências (testáveis):**
- Alterações refletem imediatamente na listagem e nos selects das abas de Movimentação.

#### FR-4: Excluir Conta

O Usuário pode excluir uma Conta existente.

**Consequências (testáveis):**
- Conta removida desaparece da listagem e dos selects.
- O sistema impede exclusão de Conta que possui Movimentações vinculadas, exibindo mensagem informativa. A edição de Descrição e Observações permanece permitida independentemente de vínculos.

### 4.2 Gestão de Categorias

**Descrição:** O Usuário cadastra Categorias macro para classificar Movimentações (ex: Essencial, Não Essencial, Investimentos, Lazer). Categorias são universais e flat (sem hierarquia na V1). CRUD completo. Realiza UJ-1.

**Requisitos Funcionais:**

#### FR-5: Criar Categoria

O Usuário pode criar uma Categoria informando Descrição (obrigatório) e Observações (opcional).

**Consequências (testáveis):**
- Categoria criada aparece na listagem e nos selects de Categoria em todas as abas de Movimentação.

#### FR-6: Visualizar Categorias

O Usuário pode visualizar todas as Categorias cadastradas em formato de lista.

**Consequências (testáveis):**
- A listagem exibe Descrição e Observações de cada Categoria.

#### FR-7: Editar Categoria

O Usuário pode editar Descrição e Observações de uma Categoria existente.

**Consequências (testáveis):**
- Alterações refletem imediatamente na listagem e nos selects.

#### FR-8: Excluir Categoria

O Usuário pode excluir uma Categoria existente.

**Consequências (testáveis):**
- Categoria removida desaparece da listagem e dos selects.
- O sistema impede exclusão de Categoria que possui Movimentações vinculadas, exibindo mensagem informativa. A edição de Descrição e Observações permanece permitida independentemente de vínculos.

### 4.3 Registro de Despesas Variáveis

**Descrição:** Aba dedicada para cadastro de Despesas de natureza pontual ou parcelada — compras no cartão (à vista ou parceladas), financiamentos, gastos esporádicos. Inclui campos específicos para conferência de fatura (Local Compra, Data Compra). Campos calculados (Valor Total, Última Parcela) são gerados automaticamente. CRUD completo. Realiza UJ-2.

**Requisitos Funcionais:**

#### FR-9: Criar Despesa Variável

O Usuário pode criar uma Despesa Variável preenchendo:
- **Obrigatórios:** Descrição, Valor Parcela, Nº Parcelas, Primeira Parcela, Categoria, Conta.
- **Opcionais:** Local Compra, Data Compra, Notas.
- **Calculados automaticamente:** Valor Total (`Valor Parcela × Nº Parcelas`), Última Parcela (`Primeira Parcela + (Nº Parcelas − 1) meses`).

**Consequências (testáveis):**
- Ao informar Valor Parcela e Nº Parcelas, o campo Valor Total é calculado e exibido em tempo real.
- Ao informar Primeira Parcela e Nº Parcelas, o campo Última Parcela é calculado e exibido em tempo real.
- Campos calculados não são editáveis pelo Usuário.
- Registro salvo aparece como linha na listagem da aba, exibindo todas as informações para conferência visual.

#### FR-10: Visualizar Despesas Variáveis

O Usuário pode visualizar todas as Despesas Variáveis em formato de lista com todos os campos visíveis.

#### FR-11: Editar Despesa Variável

O Usuário pode editar qualquer campo editável de uma Despesa Variável existente. Campos calculados são recalculados automaticamente.

#### FR-12: Excluir Despesa Variável

O Usuário pode excluir uma Despesa Variável existente.

**Consequências (testáveis):**
- Registro removido desaparece da listagem e deixa de impactar o Painel de Consolidação.

### 4.4 Registro de Despesas Fixas

**Descrição:** Aba dedicada para cadastro de Despesas recorrentes de valor constante mensal — internet, aluguel, streaming, salário de diarista. Sem parcelamento. Quando o valor muda na realidade, o Usuário edita o registro e o novo valor é projetado para frente. CRUD completo. Realiza UJ-3.

**Requisitos Funcionais:**

#### FR-13: Criar Despesa Fixa

O Usuário pode criar uma Despesa Fixa preenchendo:
- **Obrigatórios:** Descrição, Valor, Categoria, Conta.
- **Opcionais:** Notas.

**Consequências (testáveis):**
- Registro salvo aparece como linha na listagem da aba.
- O valor é projetado em todos os 24 meses do Painel de Consolidação.

#### FR-14: Visualizar Despesas Fixas

O Usuário pode visualizar todas as Despesas Fixas em formato de lista.

#### FR-15: Editar Despesa Fixa

O Usuário pode editar qualquer campo de uma Despesa Fixa existente.

**Consequências (testáveis):**
- O novo valor é projetado imediatamente em todos os meses do Painel de Consolidação, sem alterar histórico.

#### FR-16: Excluir Despesa Fixa

O Usuário pode excluir uma Despesa Fixa existente.

### 4.5 Registro de Receitas Variáveis

**Descrição:** Aba dedicada para cadastro de Receitas pontuais ou parceladas — PLR, 13º salário, freela, venda de bens. Mesma lógica de parcelamento das Despesas Variáveis, sem os campos Local Compra e Data Compra. CRUD completo. Realiza UJ-4.

**Requisitos Funcionais:**

#### FR-17: Criar Receita Variável

O Usuário pode criar uma Receita Variável preenchendo:
- **Obrigatórios:** Descrição, Valor Parcela, Nº Parcelas, Primeira Parcela, Categoria, Conta.
- **Opcionais:** Notas.
- **Calculados automaticamente:** Valor Total (`Valor Parcela × Nº Parcelas`), Última Parcela (`Primeira Parcela + (Nº Parcelas − 1) meses`).

**Consequências (testáveis):**
- Campos calculados funcionam de forma idêntica à Despesa Variável (FR-9).
- Registro salvo aparece como linha na listagem da aba.

#### FR-18: Visualizar Receitas Variáveis

O Usuário pode visualizar todas as Receitas Variáveis em formato de lista.

#### FR-19: Editar Receita Variável

O Usuário pode editar qualquer campo editável de uma Receita Variável existente.

#### FR-20: Excluir Receita Variável

O Usuário pode excluir uma Receita Variável existente.

### 4.6 Registro de Receitas Fixas

**Descrição:** Aba dedicada para cadastro de Receitas recorrentes de valor constante mensal — salário, renda de aluguel. Sem parcelamento. Mesma lógica forward-looking das Despesas Fixas. CRUD completo. Realiza UJ-3.

**Requisitos Funcionais:**

#### FR-21: Criar Receita Fixa

O Usuário pode criar uma Receita Fixa preenchendo:
- **Obrigatórios:** Descrição, Valor, Categoria, Conta.
- **Opcionais:** Notas.

**Consequências (testáveis):**
- Registro salvo aparece como linha na listagem da aba.
- O valor é projetado em todos os 24 meses do Painel de Consolidação.

#### FR-22: Visualizar Receitas Fixas

O Usuário pode visualizar todas as Receitas Fixas em formato de lista.

#### FR-23: Editar Receita Fixa

O Usuário pode editar qualquer campo de uma Receita Fixa existente.

#### FR-24: Excluir Receita Fixa

O Usuário pode excluir uma Receita Fixa existente.

### 4.7 Painel de Consolidação Mensal

**Descrição:** Coração analítico do Aureus. Grade financeira de 24 colunas (meses) organizada em cinco blocos sequenciais que consolidam todas as Movimentações. O Usuário define o mês inicial manualmente ou com um botão de atalho para o mês atual. Todas as Movimentações Fixas se repetem em todos os meses; Movimentações Variáveis aparecem apenas nos meses das suas parcelas. Valores na mesma Conta somam na mesma linha. Realiza UJ-5.

**Requisitos Funcionais:**

#### FR-25: Selecionar Mês Inicial da Grade

O Usuário pode definir o primeiro mês da grade de 24 meses de duas formas:
1. Informando uma data manualmente.
2. Clicando no botão "Mês Atual".

**Consequências (testáveis):**
- A grade exibe 24 colunas sequenciais a partir do mês selecionado.
- Ao clicar "Mês Atual", o primeiro mês é definido como o mês corrente do sistema.

#### FR-26: Bloco Receitas por Conta

O sistema exibe um bloco com uma linha por Conta, mostrando o total de Receitas daquela Conta em cada mês.

**Consequências (testáveis):**
- Receitas Fixas vinculadas à Conta repetem seu valor em todos os 24 meses.
- Receitas Variáveis vinculadas à Conta aparecem apenas nos meses das suas parcelas.
- Múltiplas Receitas na mesma Conta somam na mesma linha.
- Contas sem Receitas geram linha neste bloco com valores zerados em todos os meses.

#### FR-27: Bloco Despesas por Conta

O sistema exibe um bloco com uma linha por Conta, mostrando o total de Despesas daquela Conta em cada mês.

**Consequências (testáveis):**
- Despesas Fixas vinculadas à Conta repetem seu valor em todos os 24 meses.
- Despesas Variáveis vinculadas à Conta aparecem apenas nos meses das suas parcelas.
- Múltiplas Despesas na mesma Conta somam na mesma linha.
- Contas sem Despesas geram linha neste bloco com valores zerados em todos os meses.

#### FR-28: Bloco Categorias (R$)

O sistema exibe um bloco com uma linha por Categoria, mostrando o valor absoluto total de **Despesas** daquela Categoria em cada mês.

**Consequências (testáveis):**
- Apenas Despesas são consideradas (Receitas não entram neste bloco).
- A soma de todas as linhas deste bloco é igual ao Total Gasto do Mês.

#### FR-29: Bloco Categorias (%)

O sistema exibe um bloco com uma linha por Categoria, mostrando o percentual de representatividade daquela Categoria em relação ao total gasto no mês.

**Consequências (testáveis):**
- O percentual é calculado como: `(Despesas da Categoria no mês ÷ Total de Despesas no mês) × 100`.
- A soma dos percentuais de todas as Categorias em um mês é 100%.
- Quando o Total de Despesas no mês é zero, o sistema exibe 0% para todas as Categorias.

#### FR-30: Bloco Resumo Geral

O sistema exibe um bloco com três linhas consolidadas para cada mês:

1. **Total Gasto no Mês:** soma de todas as Despesas (Fixas + Variáveis) do mês.
2. **Sobra do Mês:** Total de Receitas do mês − Total Gasto do Mês. Pode ser negativo.
3. **Sobra Retroativa Acumulada:** soma cumulativa das Sobras do Mês, iniciando em zero no primeiro mês da grade.

**Consequências (testáveis):**
- `Sobra Retroativa[mês 1] = Sobra[mês 1]`
- `Sobra Retroativa[mês N] = Sobra[mês N] + Sobra Retroativa[mês N − 1]`
- A Sobra Retroativa pode ser negativa (indica projeção de déficit acumulado).

### 4.8 Dependência de Cadastro

**Descrição:** O sistema garante que Contas e Categorias existam antes que qualquer Movimentação possa ser registrada. Realiza UJ-1 (edge case).

**Requisitos Funcionais:**

#### FR-31: Impedir Cadastro sem Dependências

O sistema impede a criação de Despesas e Receitas quando não existem Contas ou Categorias cadastradas.

**Consequências (testáveis):**
- Os selects de Conta e Categoria nos formulários de Movimentação exibem apenas registros existentes.
- Se não há Contas ou Categorias cadastradas, o formulário exibe uma mensagem orientando o Usuário a cadastrá-las primeiro, com link direto para a tela de cadastro.

### 4.9 Navegação Principal

**Descrição:** A interface é organizada por uma barra de navegação centralizada no topo da página, levemente afastada da margem superior, com cantos arredondados. As abas são exibidas na seguinte ordem fixa: Consolidação, Despesas Variáveis, Despesas Fixas, Receitas Fixas, Receitas Variáveis. A gestão de Contas e Categorias é acessada por meio secundário dentro da interface. `[ASSUMPTION: Contas e Categorias são acessadas via menu ou botão secundário fora da barra principal de abas.]`

**Requisitos Funcionais:**

#### FR-32: Barra de Navegação por Abas

O sistema exibe uma barra de navegação horizontal centralizada no topo com cinco abas na ordem: Consolidação, Despesas Variáveis, Despesas Fixas, Receitas Fixas, Receitas Variáveis.

**Consequências (testáveis):**
- A barra possui cantos arredondados e está visualmente afastada da margem superior.
- Ao clicar em uma aba, o conteúdo correspondente é exibido.
- A aba ativa é visualmente diferenciada das demais.
- A ordem das abas é fixa e não pode ser alterada pelo Usuário.

### 4.10 Movimentação Variável à Vista (1 Parcela)

**Descrição:** Despesas e Receitas Variáveis aceitam Nº Parcelas = 1, representando compras à vista no cartão ou receitas únicas. Neste cenário, Valor Total é igual ao Valor Parcela e Última Parcela é igual à Primeira Parcela. A Movimentação aparece em um único mês no Painel de Consolidação.

**Requisitos Funcionais:**

#### FR-33: Aceitar Parcela Única

O Usuário pode informar Nº Parcelas = 1 em qualquer Movimentação Variável.

**Consequências (testáveis):**
- Valor Total calculado é igual ao Valor Parcela.
- Última Parcela calculada é igual à Primeira Parcela.
- No Painel de Consolidação, o valor aparece em um único mês.

### 4.11 Autenticação e Isolamento de Dados

**Descrição:** O Aureus autentica Usuários por meio do Google usando OAuth 2.0 com OpenID Connect. A autenticação é requisito para acessar as funcionalidades financeiras; cada registro financeiro pertence a um Usuário e não pode ser consultado ou alterado por outro.

#### FR-34: Entrar com Google

O Usuário pode iniciar autenticação selecionando “Entrar com Google”. O sistema usa um fluxo de autorização adequado a aplicações web no servidor e somente considera o login concluído após validar a resposta do Provedor de Identidade.

**Consequências (testáveis):**
- Usuário não autenticado é direcionado à tela de entrada ao tentar acessar uma área protegida.
- O sistema solicita somente os escopos de identidade necessários ao login na V1 (`openid`, `profile` e `email`).
- O sistema não coleta nem armazena a senha do Google.
- Consentimento negado, código inválido, resposta expirada ou falha de validação produzem erro controlado, sem criar sessão autenticada.

#### FR-35: Criar ou reconhecer conta local

Após um login Google válido, o sistema cria a conta local na primeira entrada ou reconhece a conta existente pela Identidade Externa estável do provedor.

**Consequências (testáveis):**
- O mesmo Usuário Google não gera contas locais duplicadas em novos logins.
- O vínculo técnico não depende de o e-mail poder ser alterado.
- O perfil local mantém, no mínimo, o identificador do provedor, e-mail e nome/imagem atuais quando fornecidos, além de datas de criação e última atualização.

#### FR-36: Isolar dados por Usuário

O sistema associa Contas, Categorias, Despesas e Receitas a um Usuário autenticado e aplica esse vínculo em toda leitura, criação, edição e exclusão.

**Consequências (testáveis):**
- Uma requisição autenticada só retorna dados do Usuário da sessão.
- IDs pertencentes a outro Usuário não permitem leitura, edição ou exclusão e não revelam se o registro existe.
- O Painel de Consolidação usa exclusivamente as Movimentações do Usuário autenticado.

#### FR-37: Encerrar sessão

O Usuário pode selecionar “Sair” para invalidar a sessão local e retornar à tela de entrada.

**Consequências (testáveis):**
- Após sair, endpoints e telas protegidos exigem nova autenticação.
- A aplicação não exibe novamente dados financeiros em cache após o encerramento.
- O encerramento local não é tratado como revogação global da conta Google.

## 5. Não-Objetivos (Explícito)

- O Aureus **não é** um aplicativo bancário e **não** se integra com bancos ou sistemas financeiros externos.
- O Aureus **não** rastreia histórico de alterações de valores fixos — é forward-looking por design.
- O Aureus **não** oferece login por senha, múltiplos provedores, recuperação de senha própria ou colaboração entre usuários na V1.
- O Aureus **não** categoriza automaticamente Movimentações — a classificação é decisão manual do Usuário.
- O Aureus **não** possui subcategorias na V1 — Categorias são flat/macro.
- O Aureus **não** gera relatórios exportáveis (PDF, Excel) na V1.
- O Aureus **não** possui notificações, alertas ou lembretes.
- O Aureus **não** possui Dark Mode ou temas visuais na V1.

## 6. Escopo MVP

### 6.1 No Escopo

- CRUD completo de Contas (descrição, observações).
- CRUD completo de Categorias macro (descrição, observações).
- 4 abas especializadas para registro de Movimentações:
  - Despesas Variáveis (com Local Compra, Data Compra, Parcelamento).
  - Despesas Fixas (valor recorrente mensal).
  - Receitas Variáveis (com Parcelamento).
  - Receitas Fixas (valor recorrente mensal).
- Cálculo automático em tempo real de Valor Total e Última Parcela.
- Painel de Consolidação com grade de 24 meses e 5 blocos analíticos.
- Seleção do mês inicial (manual ou botão "Mês Atual").
- Sobra Retroativa Acumulada com soma cumulativa.
- Autenticação por Google via OAuth 2.0/OpenID Connect e sessão protegida.
- Isolamento de todos os dados financeiros por Usuário autenticado.
- Execução local (backend via código-fonte, PostgreSQL via Docker).

### 6.2 Fora do Escopo para MVP

- **Autorização avançada** (perfis, papéis e compartilhamento entre usuários) — deferida para versão futura.
- **Dark Mode e temas visuais** — deferido para V2. Ver `addendum.md` do Brief.
- **Subcategorias** — deferido para versão futura. Categorias V1 são macro/flat. Ver `addendum.md` do PRD.
- **Deploy em nuvem** — deferido para fases avançadas de portfólio.
- **Docker Compose fullstack e Kubernetes** — deferido para demonstração DevOps em portfólio.
- **CI/CD automatizado** — deferido para fases avançadas.
- **Relatórios exportáveis** (PDF, Excel).
- **Integração bancária** (Open Banking).
- **Notificações e alertas**.

## 7. Métricas de Sucesso

Dado que a V1 é um projeto de portfólio/estudo pessoal com um único Usuário (o próprio desenvolvedor), as métricas são pragmáticas:

**Primária**
- **SM-1:** Adoção pessoal — Guilherme utiliza o Aureus como ferramenta principal de controle financeiro por pelo menos 3 meses consecutivos, substituindo a planilha. Valida FR-9 a FR-37.

**Secundárias**
- **SM-2:** Completude funcional — login Google, isolamento de dados, todas as 4 abas de registro e o Painel de Consolidação estão operacionais e corretos matematicamente. Valida FR-1 a FR-37.
- **SM-3:** Fidelidade ao controle existente — os resultados do Painel de Consolidação reproduzem com exatidão os mesmos números que a planilha produziria para o mesmo conjunto de dados. Valida FR-26 a FR-30.

**Contra-métricas (não otimizar)**
- **SM-C1:** Complexidade de interface — a adição de funcionalidades não deve aumentar o número de cliques para registrar uma Movimentação além do estritamente necessário. Contrabalança SM-1.
- **SM-C2:** Dependência externa — a indisponibilidade do Google não deve corromper ou expor dados; login novo pode ficar indisponível, mas sessões e dados existentes devem falhar de forma segura.

## 8. Questões em Aberto

As decisões de produto estão fechadas para o MVP. A arquitetura deve detalhar os valores de expiração/renovação de sessão, estratégia de armazenamento de sessão e política de exclusão/desvinculação da conta Google antes da implementação.

## 9. Índice de Assumptions

Todas as assumptions foram confirmadas ou corrigidas pelo Usuário e convertidas em requisitos definitivos:

| Assumption Original | Resolução |
|---|---|
| §4.1 FR-4 — Impedir exclusão de Conta com Movimentações | ✅ Confirmada. Edição permitida, exclusão bloqueada. |
| §4.2 FR-8 — Impedir exclusão de Categoria com Movimentações | ✅ Confirmada. Edição permitida, exclusão bloqueada. |
| §4.7 FR-26 — Contas sem Receitas omitidas do Painel | ❌ Corrigida → Contas sem Receitas **geram linhas zeradas**. |
| §4.7 FR-27 — Contas sem Despesas omitidas do Painel | ❌ Corrigida → Contas sem Despesas **geram linhas zeradas**. |
| §4.7 FR-29 — Exibir 0% quando total de Despesas é zero | ✅ Confirmada. |
| §4.8 FR-31 — Mensagem com link para cadastro de dependências | ✅ Confirmada. |
| Autenticação Google no MVP | ✅ Confirmada pelo pedido de atualização; OAuth 2.0/OIDC e isolamento por Usuário entram na V1. |
| Login por senha próprio | ❌ Fora do escopo; não haverá armazenamento de senhas na V1. |
