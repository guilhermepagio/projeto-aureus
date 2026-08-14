# UX Spine Review Rubric

## Pass 1: Mechanical Coverage

### 1. Flow Coverage (EXPERIENCE.md)
**Extract:**
- Flow 1: Visualizar Consolidação
- Flow 2: Registrar Despesa Variável Parcelada
- Flow 3: Alternar Contexto de Data

**Misses:**
- Flow 1 & 3: Lack named protagonist (e.g., "User" is generic). (EXPERIENCE.md: L64, L81)
- Flow 1: Missing failure path (e.g., network error loading grid). (EXPERIENCE.md: L63-L68)
- Flow 3: Missing failure path. (EXPERIENCE.md: L80-L85)

### 2. Token Completeness (DESIGN.md)
**Extract:**
- color, typography, shape, elevation tokens defined.
**Misses:**
- None. All color tokens have strict hex values.

### 3. Component Coverage
**Extract:**
- Pill Navigation, Bottom Navigation, FAB, Data Block Card, Form Input, Calculation Preview, Toast.

**Misses:**
- Modal / Bottom Sheet: Documented in EXPERIENCE.md behavior (L38) but missing visual specs in DESIGN.md.
- Empty State Block: Documented in EXPERIENCE.md behavior (L48) but missing visual specs in DESIGN.md.
- Global Filter Toggle: Documented in EXPERIENCE.md (L44) but missing visual specs in DESIGN.md.

### 4. State Coverage (EXPERIENCE.md)
**Extract:**
- Empty, Error, Success, Focus, Destructive Actions covered.

**Misses:**
- Cold-load / Loading State: Completely missing. (EXPERIENCE.md: L47-L51)
- Grid Empty State: Not defined what the Consolidação tab looks like with no data.

### 5. Visual Reference Coverage
**Extract:**
- None.

**Misses:**
- Missing links to Figma, wireframes, or mockup references in both spines.

---

## Pass 2: Judgment

### 6. Bloat & Overspecification
**Verdict:** Strong
- Files are highly concise, well-structured, and strictly define the contract without unnecessary implementation details.

### 7. Inheritance Discipline
**Verdict:** Strong
- Design tokens have a robust semantic taxonomy. Components appropriately reference these tokens (e.g., `color.status.negative` over raw hex). 

### 8. Shape Fit
**Verdict:** Adequate
- The architectural decisions correctly serve the "Registration on Mobile, Consolidation on Desktop" principle. However, missing loading states and missing component specs slightly weaken the contract.
