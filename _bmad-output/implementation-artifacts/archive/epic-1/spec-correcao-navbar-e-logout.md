---
status: 'done'
baseline_commit: '75f54dbecfa66b59237a526a0d29914192ac0fbd'
---

# Spec: Correção Navbar e Logout

<frozen-after-approval>
</frozen-after-approval>

## Code Map
- `frontend/src/App.tsx`: Mover `<Navigation />` para dentro de `<Header>`.
- `frontend/src/components/Header/Header.tsx`: Atualizar para receber `children`, mudar layout interno e adicionar `referrerPolicy="no-referrer"` na imagem de perfil.
- `frontend/src/components/Header/Header.css`: Mudar `.app-header` para `display: grid; grid-template-columns: 1fr auto 1fr;` para centralizar os `children` (navbar) na versão desktop.
- `frontend/src/components/Navigation/Navigation.css`: No desktop (`min-width: 769px`), remover o `padding: 24px` que fazia a pill navigation ficar muito grande dentro do header.

## Story Tasks
- [x] Modificar `App.tsx` para passar `<Navigation />` como filho de `<Header>`.
- [x] Alterar a interface de `Header` em `Header.tsx` para aceitar `children` e organizar as divs em esquerda, centro e direita.
- [x] Em `Header.tsx`, adicionar `referrerPolicy="no-referrer"` na tag `<img>` da foto de perfil.
- [x] Atualizar `Header.css` com CSS Grid (`grid-template-columns: 1fr auto 1fr;`) garantindo que na esquerda fique vazio (ou futuro logo), centro a nav e direita o perfil.
- [x] Em `Navigation.css`, alterar o desktop breakpoint para remover o `padding: 24px` do contêiner da `.navigation` (para que não afaste a barra).

## Suggested Review Order

**Ajuste do Layout**

- Move a Navbar para dentro do Header, mantendo o App limpo
  [`App.tsx:24`](../../frontend/src/App.tsx#L24)

- Organiza o cabeçalho em três colunas no grid, preparando espaço para logo, navbar e perfil
  [`Header.tsx:61`](../../frontend/src/components/Header/Header.tsx#L61)

- Estilos para as colunas do Header usando display flex no centro e laterais
  [`Header.css:1`](../../frontend/src/components/Header/Header.css#L1)

- Remove margem/padding inferior desnecessário do main content para telas maiores
  [`index.css:50`](../../frontend/src/index.css#L50)

- Remove padding da nav list no desktop que antes sobrepunha os cliques na tela
  [`Navigation.css:67`](../../frontend/src/components/Navigation/Navigation.css#L67)

**Correção de Imagem do Perfil**

- Adiciona a flag de referrerPolicy para evitar erros 403 do Google na imagem de perfil
  [`Header.tsx:81`](../../frontend/src/components/Header/Header.tsx#L81)
