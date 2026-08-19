---
sources: ["../../../docs/product/prd.md", "../../../docs/product/brief.md"]
tokens:
  color:
    brand:
      primary: "#0D7377"
      secondary: "#D4A843"
    surface:
      background: "#FAFAFA"
      card: "#FFFFFF"
      teal-light: "#E8F4F4"
      amber-light: "#FDF6E8"
    text:
      primary: "#1A1A2E"
      secondary: "#6B7280"
      muted: "#9CA3AF"
    border:
      default: "#E5E7EB"
      light: "#F3F4F6"
    status:
      positive: "#16A34A"
      positive-bg: "#F0FDF4"
      negative: "#DC2626"
      negative-bg: "#FEF2F2"
  typography:
    family: "'Plus Jakarta Sans', sans-serif"
    weights:
      regular: 400
      medium: 500
      semibold: 600
      bold: 700
  shape:
    radius-sm: "8px"
    radius-md: "12px"
    radius-pill: "9999px"
  elevation:
    shadow-sm: "0 1px 3px rgba(0,0,0,0.08)"
    shadow-up: "0 -4px 6px -1px rgba(0,0,0,0.05)"
---

# Aureus Design Spine

## Visual References
- [Wireframe: Consolidação Desktop](.working/consolidacao-desktop.html)
- [Wireframe: Consolidação Mobile](.working/consolidacao-mobile.html)
- [Wireframe: Registro de Despesas Mobile](.working/registro-despesas-mobile.html)

## Brand & Style
Aureus is a minimal, modern, and serious personal finance web application. It draws aesthetic inspiration from clean, pixel-perfect platforms (like Duolingo's structural simplicity) but maintains a premium, trustworthy financial tone. The name connects to gold, reflected in the amber accent, while the primary deep teal provides a grounded, secure base.

## Colors
- **Brand Primary (`color.brand.primary`)**: Deep Teal. Used for primary actions, active navigation states, and key highlights.
- **Brand Secondary (`color.brand.secondary`)**: Warm Amber. Used for informational highlights, specific data categories (like Category headers), and the logo dot.
- **Surface (`color.surface.*`)**: Clean white cards on an off-white background to establish depth without heavy shadows. Light teal and amber variations are used for block headers in the consolidation grid.
- **Text (`color.text.*`)**: High contrast charcoal for primary text, cool grays for secondary labels and placeholders.
- **Status (`color.status.*`)**: Semantic green for positive balances/income, semantic red for negative balances/expenses. Accompanied by very light tinted backgrounds for emphasis.

## Typography
Plus Jakarta Sans is the singular typeface. It is modern, slightly rounded, and highly legible.
- Headings use `bold` (700).
- Labels and secondary information use `semibold` (600) or `medium` (500).
- Numeric data in the consolidation grid and lists MUST use tabular figures (`font-variant-numeric: tabular-nums`) to align vertically.

## Layout & Spacing
- Responsive, fluid layout based on flexbox/grid.
- Desktop constraints allow wide horizontal scaling for the 24-month consolidation grid.
- Mobile constraints wrap content into stacked cards and utilize a bottom navigation bar.

## Elevation & Depth
Flat design with subtle depth.
- `elevation.shadow-sm` is used on cards and dropdowns to separate them from the background.
- `elevation.shadow-up` is used on the mobile bottom navigation bar, modals, and bottom sheets.

## Shapes
Corners are uniformly rounded to soften the UI.
- Buttons, cards, and modals use `shape.radius-md` (12px).
- Inputs and smaller elements use `shape.radius-sm` (8px).
- The desktop navigation bar is a floating pill (`shape.radius-pill`).

## Components

| Component | Visual Specification |
| :--- | :--- |
| **Pill Navigation** | Floating horizontal bar, white bg, pill shape. Active item gets `color.brand.primary` background and white text. (Desktop only) |
| **Bottom Navigation** | Fixed to bottom, white bg, top border. 5 icons with labels. Active item is `color.brand.primary`, others are `color.text.muted`. (Mobile only) |
| **Floating Action Button (FAB)** | Circular or pill button fixed to bottom-right (desktop) or above bottom nav (mobile) in `color.brand.primary` with white "+" icon. |
| **Data Block Card** | White card, `radius-md`, `shadow-sm`. Header has tinted background. **Required breakdown:** "Receitas por Conta" (positive bg), "Despesas por Conta" (negative bg), "Categorias (R$)" (amber bg), "Categorias (%)" (amber bg), and "Resumo Geral" (teal bg). |
| **Form Input** | White bg, `radius-sm`, 1px `color.border.default` border. On focus: border becomes primary, ring/box-shadow of `teal-light`. Error state: `color.status.negative` border. |
| **Modal / Bottom Sheet** | White surface overlay covering content. Desktop: Centered modal with `radius-md`. Mobile: Bottom sheet docked to bottom, `radius-md` top corners, heavy top shadow (`shadow-up`). Black 50% opacity backdrop. |
| **Empty State Block** | Centered alignment, dashed border (`color.border.default`), muted gray icon (`color.text.muted`) sized at 48px, bold title, secondary text description. |
| **Global Filter Toggle** | Small icon button located in the list header (e.g., funnel icon). Active state uses `color.brand.primary` tint, inactive uses `color.text.secondary`. |
| **Calculation Preview** | Inline block within forms. Uses `teal-light` background, `color.brand.primary` text, and prominent typography to show real-time totals. |
| **Toast** | Floating dark notification (`color.text.primary` bg, white text), `radius-sm`, semantic left border (e.g., green for success). |

## Do's and Don'ts
- **Do** align monetary values right and use tabular lining.
- **Don't** use colored gradients or heavy drop shadows. Keep it flat and crisp.
- **Don't** rely solely on color for errors. Always pair red borders with an error message text.
