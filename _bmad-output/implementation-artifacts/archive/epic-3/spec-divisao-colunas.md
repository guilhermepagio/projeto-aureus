---
title: 'Modernização e Padronização das Listagens e Modais'
type: 'spec'
created: '2026-08-21'
status: 'done'
route: 'one-shot'
---

# Modernização e Padronização das Listagens e Modais

## Intent
**Problem:** O usuário solicitava melhorias de layout e padronização visual abrangente, incluindo tabelas e modais. As colunas sofriam distorção de largura devido ao `w-full` em telas grandes, o menu de ação estava muito carregado e quebrando o layout, os modais de cadastro possuíam proporções assimétricas e a nomenclatura de "Parcela/Valor" estava inconsistente pelo sistema.
**Approach:** 
1. Refatoração do `<ActionMenu>` para usar `createPortal`, isolando-o do contexto de empilhamento das tabelas e permitindo fluidez vertical.
2. Padronização de cores financeiras (remoção de vermelhos/azuis e `font-semibold` nas células), adotando `text-gray-900` para sobriedade.
3. Absorção de espaço excedente usando `w-full min-w-[380px]` nas colunas "Descrição" e "Observações" das abas Fixas, prevenindo estiramento indesejado de outras colunas.
4. Ajuste proporcional e empilhamento lógico nos modais (1/2 esquerdo para formulários, 1/2 direito para Observações com `min-h-[160px]` esticável).
5. Padronização ubíqua das nomenclaturas para `Valor Parcela` e `Qtd. Parcelas` em todas as tabelas e cabeçalhos do formulário.

## Suggested Review Order

1. [ActionMenu.tsx](../../frontend/src/components/ui/ActionMenu.tsx) — Portal implementation para o z-index.
2. [DespesasFixasPage.tsx](../../frontend/src/pages/DespesasFixas/DespesasFixasPage.tsx) — Fixas com Descrição expansiva.
3. [DespesaFixaFormModal.tsx](../../frontend/src/pages/DespesasFixas/components/DespesaFixaFormModal.tsx) — Empilhamento de Categoria/Conta e layout 50/50.
4. [ReceitaFixaFormModal.tsx](../../frontend/src/pages/ReceitasFixas/components/ReceitaFixaFormModal.tsx) — Empilhamento de Categoria/Conta e layout 50/50.
5. [DespesaVariavelFormModal.tsx](../../frontend/src/pages/DespesasVariaveis/components/DespesaVariavelFormModal.tsx) — Inversão de localização Categoria/Conta x Local/Data.
6. [ReceitaVariavelFormModal.tsx](../../frontend/src/pages/ReceitasVariaveis/components/ReceitaVariavelFormModal.tsx) — Move Categoria/Conta para a esquerda.
