---
title: 'Tornar Cabeçalho Flutuante e Transparente'
type: 'spec'
created: '2026-08-18'
status: 'done'
route: 'one-shot'
---

# Tornar Cabeçalho Flutuante e Transparente

## Intent
**Problem:** O cabeçalho possuía uma barra branca de fundo e uma sombra de contêiner, o que quebrava a estética de "pílula flutuante" da navegação no desktop. A navegação e o perfil não pareciam estar flutuando sobre o fundo cinza da aplicação.
**Approach:** Remover o `background-color` e `box-shadow` do contêiner principal `.app-header` no CSS. Para garantir que cliques na área transparente não bloqueiem a interação com o conteúdo da página, aplicamos `pointer-events: none` no cabeçalho e `pointer-events: auto` nos filhos.

## Suggested Review Order

- Remove o fundo e a sombra do Header para que a barra de navegação pareça flutuante
  [`Header.css:1`](../../frontend/src/components/Header/Header.css#L1)
