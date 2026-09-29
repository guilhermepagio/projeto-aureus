---
title: 'Story 5.4: Abstração e Unificação dos Formulários de Movimentações Financeiras (DRY)'
type: 'refactor'
created: '2026-09-29'
status: 'done'
baseline_commit: '48bf07245595e8ab248cb0d1e4205b8234c46791'
review_loop_iteration: 0
context:
  - '_bmad-output/implementation-artifacts/epic-5-context.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Os formulários e modais de movimentações financeiras (`DespesasFixas`, `ReceitasFixas`, `DespesasVariaveis`, `ReceitasVariaveis`) possuem centenas de linhas de código duplicadas para campos comuns (descrição, valor com máscara monetária, selects de conta e categoria, parcelamento com cálculo de valor total e última parcela, e observações). Além disso, o componente genérico `Modal` carece de confinamento de foco por teclado (`Focus Trap`), suporte à restauração de foco ao fechar e padronização semântica de atributos ARIA (`aria-labelledby`, `aria-describedby`).

**Approach:** Implementar `Focus Trap` e atributos semânticos ARIA no componente genérico `Modal` para garantir total acessibilidade por teclado e leitores de tela. Criar componentes base reutilizáveis e modais genéricos unificados para movimentações fixas e variáveis (`MovimentacaoFixaFormModal`, `MovimentacaoVariavelFormModal`, subcomponentes de campos e seletores), eliminando a duplicação de código nas 4 páginas financeiras e preservando seus contratos públicos e mutações de dados.

## Boundaries & Constraints

**Always:**
- O componente `Modal` deve manter o foco restrito dentro de seus limites enquanto aberto (`Focus Trap`), alternando entre elementos focáveis com `Tab` e `Shift+Tab`.
- Ao abrir o `Modal`, o foco deve ser posicionado no primeiro elemento interativo focável ou com autoFocus; ao fechar (via botão Fechar, tecla `Escape` ou ação de cancelamento), o foco deve ser devolvido ao elemento acionador original.
- O diálogo do `Modal` deve possuir `role="dialog"`, `aria-modal="true"`, `aria-labelledby` associado ao título e suporte opcional a `aria-describedby`.
- Os campos de formulário e seletores devem padronizar atributos de acessibilidade (`htmlFor`, `id`, `aria-invalid`, `aria-describedby` para exibição de erros).
- A refatoração das telas de Despesas Fixas, Receitas Fixas, Despesas Variáveis e Receitas Variáveis deve manter 100% de paridade funcional com o comportamento atual, sem regressão visual ou nas regras de validação e payloads de mutação da API.
- Manter a compatibilidade com todos os testes existentes da suíte do frontend.

**Ask First:**
- Modificações na API pública de contratos ou rotas backend (`/api/*`).
- Remoção ou alteração da assinatura pública de props dos modais existentes das páginas (`isOpen`, `onClose`, `despesaToEdit`, etc.).

**Never:**
- Não instalar novas bibliotecas externas para modais ou formulários (usar React nativo e Tailwind já configurados no projeto).
- Não quebrar o funcionamento do `Escape` ou o bloqueio de scroll de fundo (`document.body.style.overflow = 'hidden'`) já presente no `Modal`.
- Não misturar regras de negócio ou mutações de receitas e despesas dentro de componentes puramente visuais.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Abertura e foco inicial no Modal | Usuário aciona botão "+ Nova Despesa" | Modal abre com `role="dialog"`, `aria-modal="true"`, `aria-labelledby` apontando para o título e foco no primeiro campo interativo | Elemento gatilho armazenado em ref para restauração |
| Navegação por Tab no Modal (Focus Trap) | Usuário pressiona `Tab` no último elemento interativo do modal | Foco salta ciclicamente para o primeiro elemento interativo dentro do modal, sem vazar para a página | Previne `Tab` padrão e foca o primeiro elemento |
| Navegação por Shift+Tab no Modal | Usuário pressiona `Shift+Tab` no primeiro elemento interativo do modal | Foco salta para o último elemento interativo dentro do modal | Previne `Shift+Tab` padrão e foca o último elemento |
| Fechamento por tecla Escape | Usuário pressiona `Escape` com modal aberto e `!disableClose` | Modal fecha e o foco do teclado retorna ao elemento acionador original | Se `disableClose` (ex: salvando), evento `Escape` é ignorado |
| Submissão com campos inválidos | Usuário submete formulário com campos obrigatórios vazios ou valor zero | Erros são exibidos sob cada campo com `aria-invalid="true"` e mensagem vinculada por `aria-describedby` | Submissão bloqueada até resolução |
| Edição de movimentação fixa/variável | Modal aberto passando objeto existente (`despesaToEdit` ou `receitaToEdit`) | Campos pré-populados com valores existentes, formatação de moeda e datas preservadas | Previews de valor total e última parcela calculados automaticamente |

</frozen-after-approval>

## Code Map

- `frontend/src/components/ui/Modal.tsx` -- Adição de Focus Trap (confinamento cíclico de Tab/Shift+Tab), foco inicial, restauração de foco no fechamento, `aria-labelledby` e `aria-describedby`
- `frontend/src/components/ui/Modal.test.tsx` -- Testes unitários para `Modal` (renderização, foco inicial, Focus Trap por Tab/Shift+Tab, fechamento por ESC e restauração de foco)
- `frontend/src/components/MovimentacaoForm/FormField.tsx` -- Componente base para renderização acessível de label, container do input e mensagem de erro vinculada por `aria-describedby`
- `frontend/src/components/MovimentacaoForm/ContaCategoriaFields.tsx` -- Componente unificado para seletores acessíveis de Conta e Categoria com integração a `useContas` e `useCategorias`
- `frontend/src/components/MovimentacaoForm/ValorInput.tsx` -- Componente de input monetário com máscara de formatação (`formatCurrency`/`parseCurrency`), acessibilidade e mensagens de validação
- `frontend/src/components/MovimentacaoForm/ObservacoesField.tsx` -- Componente de textarea para observações com limitação de caracteres e layout flexível
- `frontend/src/components/MovimentacaoForm/ParcelamentoFields.tsx` -- Componente de parcelamento para lançamentos variáveis (valor da parcela, qtd de parcelas, preview do total, MonthPicker de início e preview da última parcela)
- `frontend/src/components/MovimentacaoForm/MovimentacaoFixaFormModal.tsx` -- Modal e formulário unificado para movimentações fixas (despesas e receitas fixas)
- `frontend/src/components/MovimentacaoForm/MovimentacaoVariavelFormModal.tsx` -- Modal e formulário unificado para movimentações variáveis (despesas e receitas variáveis, incluindo suporte condicional a local e data de compra)
- `frontend/src/pages/DespesasFixas/components/DespesaFixaFormModal.tsx` -- Refatoração para delegar ao `MovimentacaoFixaFormModal` mantendo contratos de props e mutações
- `frontend/src/pages/ReceitasFixas/components/ReceitaFixaFormModal.tsx` -- Refatoração para delegar ao `MovimentacaoFixaFormModal` mantendo contratos de props e mutações
- `frontend/src/pages/DespesasVariaveis/components/DespesaVariavelFormModal.tsx` -- Refatoração para delegar ao `MovimentacaoVariavelFormModal` mantendo contratos de props e mutações
- `frontend/src/pages/ReceitasVariaveis/components/ReceitaVariavelFormModal.tsx` -- Refatoração para delegar ao `MovimentacaoVariavelFormModal` mantendo contratos de props e mutações
- `frontend/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx` -- Testes de renderização e validação dos componentes compartilhados de movimentações

## Tasks & Acceptance

**Execution:**
- [x] `frontend/src/components/ui/Modal.tsx` -- Implementar Focus Trap com ciclo de Tab/Shift+Tab, captura e restauração de foco ativo, e vincular `aria-labelledby` e `aria-describedby` -- Garante acessibilidade total por teclado nos modais
- [x] `frontend/src/components/ui/Modal.test.tsx` -- Criar suíte de testes unitários para o `Modal` validando Focus Trap, tecla Escape e restauração de foco -- Validação automatizada de acessibilidade do modal
- [x] `frontend/src/components/MovimentacaoForm/` -- Criar componentes base de formulário (`FormField`, `ContaCategoriaFields`, `ValorInput`, `ObservacoesField`, `ParcelamentoFields`) -- Modularização e eliminação de código duplicado
- [x] `frontend/src/components/MovimentacaoForm/MovimentacaoFixaFormModal.tsx` e `frontend/src/components/MovimentacaoForm/MovimentacaoVariavelFormModal.tsx` -- Implementar modais genéricos unificados para movimentações fixas e variáveis -- Centraliza layout, estados, cálculo de parcelas e validações
- [x] `frontend/src/pages/DespesasFixas/components/DespesaFixaFormModal.tsx` e `frontend/src/pages/ReceitasFixas/components/ReceitaFixaFormModal.tsx` -- Refatorar para consumir `MovimentacaoFixaFormModal` -- Elimina duplicação nas movimentações fixas
- [x] `frontend/src/pages/DespesasVariaveis/components/DespesaVariavelFormModal.tsx` e `frontend/src/pages/ReceitasVariaveis/components/ReceitaVariavelFormModal.tsx` -- Refatorar para consumir `MovimentacaoVariavelFormModal` -- Elimina duplicação nas movimentações variáveis
- [x] `frontend/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx` -- Implementar testes unitários para os formulários unificados e cálculos de parcelas -- Garante que os formulários compartilhados validam e calculam corretamente

### Review Findings

- [x] [Review][Patch] Confinamento de foco no Shift+Tab tratando foco no modalRef.current ou externo [frontend/src/components/ui/Modal.tsx:135]
- [x] [Review][Patch] Proteção contra roubo indevido de foco em portais aninhados com role="dialog" [frontend/src/components/ui/Modal.tsx:125]
- [x] [Review][Patch] Restauração segura de foco checando document.contains antes de chamar focus [frontend/src/components/ui/Modal.tsx:158]
- [x] [Review][Patch] Suporte a initialFocusRef opcional e filtro de fieldset[disabled] e hidden em getFocusableElements [frontend/src/components/ui/Modal.tsx:18]
- [x] [Review][Patch] event.stopPropagation no Escape de MonthPicker e DatePicker evitando fechamento do modal pai [frontend/src/components/ui/MonthPicker.tsx:112]
- [x] [Review][Patch] Validação defensiva em calculateUltimaParcela para formatos e meses inválidos [frontend/src/components/MovimentacaoForm/parcelamentoUtils.ts:16]
- [x] [Review][Patch] Repasse de required e ariaRequired no helper de FormField [frontend/src/components/MovimentacaoForm/FormField.tsx:43]
- [x] [Review][Patch] Envelopamento de MonthPicker, localCompra e dataCompra em FormField [frontend/src/components/MovimentacaoForm/ParcelamentoFields.tsx:110]
- [x] [Review][Patch] Adição de focus-visible:ring-2 nos botões e role="alert" no container de erros [frontend/src/components/MovimentacaoForm/MovimentacaoFixaFormModal.tsx:142]
- [x] [Review][Patch] Limpeza reativa de erro individual de campo em onChange [frontend/src/components/MovimentacaoForm/MovimentacaoFixaFormModal.tsx:50]
- [x] [Review][Patch] Expansão de testes cobrindo wrappers das páginas e cálculos defensivos (110 testes passando) [frontend/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx:280]
- [x] [Review][Defer] Tratar parsing de selectedMonth com formato não numérico em ConsolidacaoToolbar [frontend/src/components/Consolidacao/ConsolidacaoToolbar.tsx:27] — deferred, pre-existing

**Acceptance Criteria:**
- Given qualquer componente `Modal` aberto, when o usuário navegar usando `Tab` ou `Shift+Tab`, then o foco deve permanecer confinado dentro do modal em ciclo fechado
- Given qualquer componente `Modal` aberto, when o usuário pressionar a tecla `Escape` e `disableClose` não estiver ativo, then o modal deve fechar e restaurar o foco para o elemento HTML que disparou sua abertura
- Given os diálogos de modal e seletores de formulário, when inspecionados via árvore de acessibilidade, then devem conter atributos semânticos padronizados (`role="dialog"`, `aria-modal="true"`, `aria-labelledby`, `aria-describedby` e `aria-invalid` em campos com erro)
- Given as páginas de Despesas Fixas, Receitas Fixas, Despesas Variáveis e Receitas Variáveis, when o usuário abrir seus respectivos formulários de criação ou edição, then os campos comuns (descrição, valor, conta, categoria, data/mês e observações) devem ser renderizados via componentes base compartilhados mantendo comportamento e contratos intactos

## Spec Change Log

## Design Notes

A unificação dos formulários preserva estritamente os contratos de componentes existentes (`DespesaFixaFormModal`, `ReceitaFixaFormModal`, etc.), permitindo que as páginas continuem instanciando-os da mesma forma. Os formulários compartilhados concentram a validação dos campos obrigatórios comuns, o parsing/formatação de valores monetários e o cálculo dinâmico das parcelas e meses de término, evitando qualquer redundância nos controladores de tela. O `Modal` utiliza `useId` e listeners de teclado com captura limpa de nós focáveis (`button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])`), garantindo conformidade com WCAG 2.1 AA.

## Verification

**Commands:**
- `npm test -- --run` no diretório `frontend` -- expected: todos os testes unitários novos e existentes passando com sucesso
- `npm run lint` no diretório `frontend` -- expected: nenhum erro de lint
- `npm run build` no diretório `frontend` -- expected: compilação TypeScript e Vite sem erros de tipos ou dependências

## Suggested Review Order

**Acessibilidade e Focus Trap no Modal**

- Implementação do confinamento cíclico de foco e atributos semânticos ARIA no diálogo
  [`Modal.tsx:71`](../../frontend/src/components/ui/Modal.tsx#L71)

- Interrupção de propagação da tecla Escape para proteger o modal pai
  [`MonthPicker.tsx:112`](../../frontend/src/components/ui/MonthPicker.tsx#L112)

**Componentes Base Compartilhados de Formulário (DRY)**

- Componente base para renderização acessível de rótulos e erros com aria-describedby
  [`FormField.tsx:21`](../../frontend/src/components/MovimentacaoForm/FormField.tsx#L21)

- Dropdowns compartilhados de Conta e Categoria com integração direta aos hooks
  [`ContaCategoriaFields.tsx:29`](../../frontend/src/components/MovimentacaoForm/ContaCategoriaFields.tsx#L29)

- Input monetário com máscara BRL e validação de valores
  [`ValorInput.tsx:23`](../../frontend/src/components/MovimentacaoForm/ValorInput.tsx#L23)

- Funções puras de cálculo de parcelas com validações defensivas contra NaN
  [`parcelamentoUtils.ts:16`](../../frontend/src/components/MovimentacaoForm/parcelamentoUtils.ts#L16)

- Bloco de parcelamento com preview dinâmico de valor total e mês final
  [`ParcelamentoFields.tsx:39`](../../frontend/src/components/MovimentacaoForm/ParcelamentoFields.tsx#L39)

**Modais Unificados e Delegação nas Páginas**

- Modal genérico de movimentações fixas unificando despesas e receitas
  [`MovimentacaoFixaFormModal.tsx:32`](../../frontend/src/components/MovimentacaoForm/MovimentacaoFixaFormModal.tsx#L32)

- Modal genérico de movimentações variáveis com suporte condicional a compras
  [`MovimentacaoVariavelFormModal.tsx:39`](../../frontend/src/components/MovimentacaoForm/MovimentacaoVariavelFormModal.tsx#L39)

- DespesaFixaFormModal delegando ao modal unificado com contratos de props preservados
  [`DespesaFixaFormModal.tsx:17`](../../frontend/src/pages/DespesasFixas/components/DespesaFixaFormModal.tsx#L17)

- DespesaVariavelFormModal delegando ao modal unificado com contratos de props preservados
  [`DespesaVariavelFormModal.tsx:18`](../../frontend/src/pages/DespesasVariaveis/components/DespesaVariavelFormModal.tsx#L18)

**Suíte de Testes Automatizados**

- Suíte de 13 testes do Modal cobrindo Focus Trap, Escape e restauração
  [`Modal.test.tsx:17`](../../frontend/src/components/ui/Modal.test.tsx#L17)

- Suíte de 23 testes dos formulários compartilhados, wrappers e cálculos de parcelas
  [`MovimentacaoForm.test.tsx:26`](../../frontend/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx#L26)
