# Verification Gap Review

**Goal:** Find changed behavior that could break without reliable verification catching it. Ask one question — "if the behavior this change is supposed to produce broke where it's actually used, would verification fail?" Do not hunt for correctness bugs, but report genuine problems you notice while tracing verification.

The main verification gap shapes are:

1. **Regression gap:** the changed code regresses where it's used, and no test covering that use would fail.
2. **Missing-adoption gap:** a place that should now use the new behavior doesn't; it handles the same case its own way, or not at all, and no test would flag the omission.
3. **Broken-verification gap:** a test appears to cover the changed behavior, but would not actually protect it because it is skipped, flaky, not run in the normal verification path, or too weak to observe the regression.

## Evidence Rules

- Read a test before claiming what it covers, runs, asserts, or misses.
- Before claiming no test exists, search the whole repo by the symbol under test and by import references; expected file locations are not enough.
- Never assert what you did not verify. If a finding cannot be grounded, drop it.
- In a finding, say what you actually checked — "none of the tests I read cover this" — and show how far you looked. Say a test doesn't exist anywhere only when the symbol/import-reference search actually shows that.
- Do not assign severity, confidence, priority, or ranking.

## Review Sequence

### Step 1: Screen for behavioral change

Screen each part of the change separately. If a part is non-behavioral, skip it. Call a part non-behavioral only when the changed code does not alter return values, thrown errors, caller-visible side effects, or observable state (including iteration order and emitted messages). Once a part meets that test, move on; do not inspect callers or tests for extra confirmation.

Common non-behavioral examples: formatting, comments, whitespace; pure renames; trivial getters/setters and pass-throughs; type-only or compiler-enforced changes with no runtime effect; etc.

Only outcomes produced by deterministic code are worth automatically testing; tests are useless on static source text and brittle on LLM output. Skip those parts.

If every part is skipped, output the clean result (see Output Format).

### Step 2: Find the behavior that changed

Identify what behavior changed compared to the previous version: output, side effect, branch, error path, schema/event shape, config default, validation/authorization rule, external contract, etc. If the change affects more than one behavior, handle each separately.

Treat broad-impact changes as behavioral even when no single changed line looks important: dependency, toolchain, build/config, data-file, etc.

### Step 3: Trace where that behavior is used

Trace the changed behavior to the places that observe it. Start with direct callers and registered entry points (routes, commands, DI), contract consumers (schemas, events, APIs, database readers), and reverse-dependency info if already available.

Follow a path only while the changed behavior is reachable and unverified. Stop when a test at that boundary would fail, the consumer does not observe the changed behavior, or the next hop is guesswork (dynamic dispatch, reflection, outside-repo consumers, etc.). Prefer the nearest observable boundary, often one to three hops away, especially across contract, integration, or service edges. If there are more than five similar consumers, group obvious repeats and check representative paths; expand only when a consumer observes the behavior differently.

### Step 4: Qualify the consumer, then check its test

For each consumer, name the smallest realistic regression this consumer would observe: invert the branch, drop the default, omit the field, return the old error code, skip the integration call, etc. This is the Demonstration. If no such regression exists, drop the path; untested downstream code is not a finding.

A `Missing-adoption gap` qualifies not by the adoption failure alone but by a supersession signal: the change gives clear evidence the new behavior is meant to replace the local one — PR intent, naming or docs, a replaced sibling site, deleted duplicate logic, or a test defining the new rule — and the local site shares the same observable contract. Without a supersession signal and a shared observable contract, it is a refactor suggestion, not a verification-gap finding. Once both hold, check whether any test for that site would flag the non-adoption; missing coverage of the non-adoption is the gap itself, not a disqualifier.

Find and read the relevant test. Ask whether the Demonstration would make an assertion fail.

- If yes, the behavior is verified. No finding.
- For a regression-style Demonstration: if no test runs the path, the test is skipped/flaky/not run normally, or the test runs the code without checking the changed result, report a `Regression gap` or `Broken-verification gap`.
- For a qualifying Missing-adoption case: if none of the site tests you found assert it adopts the new behavior, report a `Missing-adoption gap`.

A test counts only if it runs normally and an assertion observes the changed output, branch, or contract. These do not count: no execution; source-text assertions that match a file's wording instead of running it; success/no-throw/snapshot-only checks; mock/log-call checks; human-only checks; tests that mock away the integration; e2e tests that pass through without checking the changed output; stale assertions or fixtures.

For example, `expect(x ?? DEFAULT).toBe(DEFAULT)` passes when `x` is missing.

Common patterns:

- **Caller-path gap** — helper test covers the branch, but caller values skip it.
- **Contract drift** — payload/schema/event changes must be verified at the consumer.
- **Migration compatibility** — tests only create new-format rows or fresh schemas.
- **Phantom exception** — handled partial-failure path has no test.
- **Missing-adoption gap** — sibling site should use the new rule/helper and does not.
- **Removed verification** — deleted test or weakened assertion leaves behavior unpinned; removing a source-text assertion is not this, since it never counted.

### Step 5: Confirm each finding is real

Before writing a finding, re-open the specific tests or search results the finding relies on. Verify the Demonstration would not make any test you checked fail, or that the absence claim is backed by the symbol/import-reference search. Do not claim more than you verified; drop any finding you cannot ground.

Explain why the test misses the bug using what the test sets up and checks.

Do not report: compiler/type-checker-enforced cases; behavior already verified by an integration, contract, or e2e test; implementation-detail or mock-only tests; low coverage or a missing test file by itself; legacy untested code the change did not affect.

Report genuine problems you noticed while tracing verification, even if they are not verification gaps. Put them under `Other findings` in the output. This permits reporting what you already reached, not extra hunting.

## OUTPUT FORMAT

Emit each verification-gap finding as one block. No general advice, no severity or confidence.

```markdown
### <one-line title naming the gap>

- **Changed surface:** the exact behavior or contract that changed — `file:line`.
- **Impacted consumer or site:** named concretely with `file:line` (e.g. "the `createInvoice` mutation used by the billing dashboard at `billing/dashboard.ts:88`," not "callers of this function").
- **Existing test evidence:**
  - `Regression gap`: what the relevant test actually asserts, with `file:line`; or, if none, the symbol/import-reference searches run and their result.
  - `Missing-adoption gap`: tests for the impacted site, and whether any assert it adopts the new behavior.
  - `Broken-verification gap`: the apparent test or verification path, and why it does not count.
- **Missing verification:** the precise assertion or check that's absent.
- **Demonstration:**
  - `Regression gap` / `Broken-verification gap`: the concrete regression that would ship undetected, and why the tests you checked would not fail.
  - `Missing-adoption gap`: the case the site mishandles by not adopting the new behavior, and that none of the tests you read assert adoption.
- **Consequence:** the concrete thing that ships wrong — a regression the checked evidence would not catch, or a site that should use the new behavior and doesn't.
- **Suggested test shape:** (optional) the kind of test that would close the gap, fit to the repo's own way of verifying — don't impose a generic test pyramid.
```

If you noticed genuine non-gap problems while tracing verification, append:

```markdown
## Other findings

- <description only; no severity, confidence, priority, or ranking>
```

When you find no verification gaps and no other findings, output exactly this single line, not an empty response:

`No verification gaps found.`

## CONTENT SOURCE

Review the content supplied under "Review content:" in the message that launched you. If none is supplied, stop with exactly: `No verification gaps found.`


Review content:

diff --git a/_bmad-output/implementation-artifacts/sprint-status.yaml b/_bmad-output/implementation-artifacts/sprint-status.yaml
index 9bf68ba..41c2136 100644
--- a/_bmad-output/implementation-artifacts/sprint-status.yaml
+++ b/_bmad-output/implementation-artifacts/sprint-status.yaml
@@ -29,9 +29,8 @@
 # - Dev moves story to 'review', then runs code-review (fresh context, different LLM recommended)
 # - Retrospective appends its action items to action_items; the status view surfaces open ones
 generated: 08-17-2026 18:28
-last_updated: 09-16-2026 01:42
+last_updated: 09-16-2026 10:55
 project: projeto-aureus
-project_key: NOKEY
 tracking_system: file-system
 story_location: _bmad-output/implementation-artifacts
 development_status:
@@ -64,7 +63,7 @@ development_status:
 
   epic-5: in-progress
   5-1-backend-service-layer-e-dtos-records-para-entidades: done
-  5-2-tratamento-global-erros-validacao-tenant-e-indices: backlog
+  5-2-tratamento-global-erros-validacao-tenant-e-indices: done
   5-3-centralizacao-cliente-http-api-e-resiliencia-frontend: backlog
   5-4-abstracao-e-unificacao-formularios-movimentacoes-dry: backlog
   epic-5-retrospective: optional
@@ -135,14 +134,14 @@ action_items:
     epic: 4
     action: "Padronizar regra de negócio para movimentações sem conta associada"
     owner: "Arquiteto / Desenvolvedor Backend"
-    status: open
+    status: done
     ref: "/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-4/epic-4-retro-2026-09-14.md"
   - id: "epic-4-retro-item-10-otimizar-agregação-de-transações-no-back"
     epic: 4
     action: "Otimizar agregação de transações no Backend substituindo carregamento
       em memória por queries SQL e índices"
     owner: "Desenvolvedor Backend"
-    status: open
+    status: done
     ref: "/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-4/epic-4-retro-2026-09-14.md"
   - id: "epic-4-retro-item-11-implementar-suíte-de-testes-e2e-automati"
     epic: 4
diff --git a/backend/pom.xml b/backend/pom.xml
index 15ed783..f34753d 100644
--- a/backend/pom.xml
+++ b/backend/pom.xml
@@ -97,6 +97,14 @@
 			<version>0.12.5</version>
 			<scope>runtime</scope>
 		</dependency>
+		<dependency>
+			<groupId>org.flywaydb</groupId>
+			<artifactId>flyway-core</artifactId>
+		</dependency>
+		<dependency>
+			<groupId>org.flywaydb</groupId>
+			<artifactId>flyway-database-postgresql</artifactId>
+		</dependency>
 	</dependencies>
 
 	<build>
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java
index 466bec7..04cb14c 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java
@@ -2,7 +2,6 @@ package com.guilhermepagio.aureus.backend.controller;
 
 import java.util.List;
 
-import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.DeleteMapping;
 import org.springframework.web.bind.annotation.GetMapping;
@@ -41,21 +40,15 @@ public class CategoriaController {
 
     @PutMapping("/{id}")
     public ResponseEntity<CategoriaResponseDTO> atualizar(final @PathVariable Long id, final @Valid @RequestBody CategoriaRequestDTO dto) {
-        return categoriaService.atualizar(id, dto)
-                .map(ResponseEntity::ok)
-                .orElse(ResponseEntity.notFound().build());
+        return ResponseEntity.ok(categoriaService.atualizar(id, dto));
     }
 
     @DeleteMapping("/{id}")
     public ResponseEntity<Void> excluir(final @PathVariable Long id) {
-        try {
-            boolean excluido = categoriaService.excluir(id);
-            if (!excluido) {
-                return ResponseEntity.notFound().build();
-            }
-            return ResponseEntity.noContent().build();
-        } catch (final DataIntegrityViolationException e) {
-            return ResponseEntity.badRequest().build();
+        boolean excluido = categoriaService.excluir(id);
+        if (!excluido) {
+            return ResponseEntity.notFound().build();
         }
+        return ResponseEntity.noContent().build();
     }
 }
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java
index 4a95aec..97cd575 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java
@@ -2,7 +2,6 @@ package com.guilhermepagio.aureus.backend.controller;
 
 import java.util.List;
 
-import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.DeleteMapping;
 import org.springframework.web.bind.annotation.GetMapping;
@@ -41,21 +40,15 @@ public class ContaController {
 
     @PutMapping("/{id}")
     public ResponseEntity<ContaResponseDTO> atualizar(final @PathVariable Long id, final @Valid @RequestBody ContaRequestDTO dto) {
-        return contaService.atualizar(id, dto)
-                .map(ResponseEntity::ok)
-                .orElse(ResponseEntity.notFound().build());
+        return ResponseEntity.ok(contaService.atualizar(id, dto));
     }
 
     @DeleteMapping("/{id}")
     public ResponseEntity<Void> excluir(final @PathVariable Long id) {
-        try {
-            boolean excluido = contaService.excluir(id);
-            if (!excluido) {
-                return ResponseEntity.notFound().build();
-            }
-            return ResponseEntity.noContent().build();
-        } catch (final DataIntegrityViolationException e) {
-            return ResponseEntity.badRequest().build();
+        boolean excluido = contaService.excluir(id);
+        if (!excluido) {
+            return ResponseEntity.notFound().build();
         }
+        return ResponseEntity.noContent().build();
     }
 }
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java
index e6c599f..7adc26f 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java
@@ -1,9 +1,7 @@
 package com.guilhermepagio.aureus.backend.controller;
 
 import java.util.List;
-import java.util.Map;
 
-import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.DeleteMapping;
 import org.springframework.web.bind.annotation.GetMapping;
@@ -36,35 +34,21 @@ public class DespesaFixaController {
     }
 
     @PostMapping
-    public ResponseEntity<?> criar(final @Valid @RequestBody DespesaFixaRequestDTO dto) {
-        try {
-            return ResponseEntity.ok(despesaFixaService.criar(dto));
-        } catch (DataIntegrityViolationException e) {
-            return ResponseEntity.badRequest().body(Map.of("message", "Erro de integridade relacional. Verifique os vínculos informados."));
-        }
+    public ResponseEntity<DespesaFixaResponseDTO> criar(final @Valid @RequestBody DespesaFixaRequestDTO dto) {
+        return ResponseEntity.ok(despesaFixaService.criar(dto));
     }
 
     @PutMapping("/{id}")
-    public ResponseEntity<?> atualizar(final @PathVariable Long id, final @Valid @RequestBody DespesaFixaRequestDTO dto) {
-        try {
-            return despesaFixaService.atualizar(id, dto)
-                    .map(ResponseEntity::ok)
-                    .orElse(ResponseEntity.notFound().build());
-        } catch (DataIntegrityViolationException e) {
-            return ResponseEntity.badRequest().body(Map.of("message", "Erro de integridade relacional. Verifique os vínculos informados."));
-        }
+    public ResponseEntity<DespesaFixaResponseDTO> atualizar(final @PathVariable Long id, final @Valid @RequestBody DespesaFixaRequestDTO dto) {
+        return ResponseEntity.ok(despesaFixaService.atualizar(id, dto));
     }
 
     @DeleteMapping("/{id}")
-    public ResponseEntity<?> excluir(final @PathVariable Long id) {
-        try {
-            boolean excluido = despesaFixaService.excluir(id);
-            if (!excluido) {
-                return ResponseEntity.notFound().build();
-            }
-            return ResponseEntity.noContent().build();
-        } catch (final DataIntegrityViolationException e) {
-            return ResponseEntity.badRequest().body(Map.of("message", "Não é possível excluir esta despesa porque ela está em uso."));
+    public ResponseEntity<Void> excluir(final @PathVariable Long id) {
+        boolean excluido = despesaFixaService.excluir(id);
+        if (!excluido) {
+            return ResponseEntity.notFound().build();
         }
+        return ResponseEntity.noContent().build();
     }
 }
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaVariavelController.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaVariavelController.java
index c4f7b03..e31f73b 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaVariavelController.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaVariavelController.java
@@ -1,9 +1,7 @@
 package com.guilhermepagio.aureus.backend.controller;
 
 import java.util.List;
-import java.util.Map;
 
-import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.DeleteMapping;
 import org.springframework.web.bind.annotation.GetMapping;
@@ -36,35 +34,21 @@ public class DespesaVariavelController {
     }
 
     @PostMapping
-    public ResponseEntity<?> criar(final @Valid @RequestBody DespesaVariavelRequestDTO dto) {
-        try {
-            return ResponseEntity.ok(despesaVariavelService.criar(dto));
-        } catch (DataIntegrityViolationException e) {
-            return ResponseEntity.badRequest().body(Map.of("message", "Erro de integridade relacional. Verifique os vínculos informados."));
-        }
+    public ResponseEntity<DespesaVariavelResponseDTO> criar(final @Valid @RequestBody DespesaVariavelRequestDTO dto) {
+        return ResponseEntity.ok(despesaVariavelService.criar(dto));
     }
 
     @PutMapping("/{id}")
-    public ResponseEntity<?> atualizar(final @PathVariable Long id, final @Valid @RequestBody DespesaVariavelRequestDTO dto) {
-        try {
-            return despesaVariavelService.atualizar(id, dto)
-                    .map(ResponseEntity::ok)
-                    .orElse(ResponseEntity.notFound().build());
-        } catch (DataIntegrityViolationException e) {
-            return ResponseEntity.badRequest().body(Map.of("message", "Erro de integridade relacional. Verifique os vínculos informados."));
-        }
+    public ResponseEntity<DespesaVariavelResponseDTO> atualizar(final @PathVariable Long id, final @Valid @RequestBody DespesaVariavelRequestDTO dto) {
+        return ResponseEntity.ok(despesaVariavelService.atualizar(id, dto));
     }
 
     @DeleteMapping("/{id}")
-    public ResponseEntity<?> excluir(final @PathVariable Long id) {
-        try {
-            boolean excluido = despesaVariavelService.excluir(id);
-            if (!excluido) {
-                return ResponseEntity.notFound().build();
-            }
-            return ResponseEntity.noContent().build();
-        } catch (final DataIntegrityViolationException e) {
-            return ResponseEntity.badRequest().body(Map.of("message", "Erro ao excluir o registro."));
+    public ResponseEntity<Void> excluir(final @PathVariable Long id) {
+        boolean excluido = despesaVariavelService.excluir(id);
+        if (!excluido) {
+            return ResponseEntity.notFound().build();
         }
+        return ResponseEntity.noContent().build();
     }
 }
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ReceitaFixaController.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ReceitaFixaController.java
index 5fe18ef..f613608 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ReceitaFixaController.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ReceitaFixaController.java
@@ -1,9 +1,7 @@
 package com.guilhermepagio.aureus.backend.controller;
 
 import java.util.List;
-import java.util.Map;
 
-import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.DeleteMapping;
 import org.springframework.web.bind.annotation.GetMapping;
@@ -36,35 +34,21 @@ public class ReceitaFixaController {
     }
 
     @PostMapping
-    public ResponseEntity<?> criar(final @Valid @RequestBody ReceitaFixaRequestDTO dto) {
-        try {
-            return ResponseEntity.ok(receitaFixaService.criar(dto));
-        } catch (DataIntegrityViolationException e) {
-            return ResponseEntity.badRequest().body(Map.of("message", "Erro de integridade relacional. Verifique os vínculos informados."));
-        }
+    public ResponseEntity<ReceitaFixaResponseDTO> criar(final @Valid @RequestBody ReceitaFixaRequestDTO dto) {
+        return ResponseEntity.ok(receitaFixaService.criar(dto));
     }
 
     @PutMapping("/{id}")
-    public ResponseEntity<?> atualizar(final @PathVariable Long id, final @Valid @RequestBody ReceitaFixaRequestDTO dto) {
-        try {
-            return receitaFixaService.atualizar(id, dto)
-                    .map(ResponseEntity::ok)
-                    .orElse(ResponseEntity.notFound().build());
-        } catch (DataIntegrityViolationException e) {
-            return ResponseEntity.badRequest().body(Map.of("message", "Erro de integridade relacional. Verifique os vínculos informados."));
-        }
+    public ResponseEntity<ReceitaFixaResponseDTO> atualizar(final @PathVariable Long id, final @Valid @RequestBody ReceitaFixaRequestDTO dto) {
+        return ResponseEntity.ok(receitaFixaService.atualizar(id, dto));
     }
 
     @DeleteMapping("/{id}")
-    public ResponseEntity<?> excluir(final @PathVariable Long id) {
-        try {
-            boolean excluido = receitaFixaService.excluir(id);
-            if (!excluido) {
-                return ResponseEntity.notFound().build();
-            }
-            return ResponseEntity.noContent().build();
-        } catch (final DataIntegrityViolationException e) {
-            return ResponseEntity.badRequest().body(Map.of("message", "Não é possível excluir esta receita porque ela está em uso."));
+    public ResponseEntity<Void> excluir(final @PathVariable Long id) {
+        boolean excluido = receitaFixaService.excluir(id);
+        if (!excluido) {
+            return ResponseEntity.notFound().build();
         }
+        return ResponseEntity.noContent().build();
     }
 }
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ReceitaVariavelController.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ReceitaVariavelController.java
index 24a0f77..740b947 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ReceitaVariavelController.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ReceitaVariavelController.java
@@ -1,9 +1,7 @@
 package com.guilhermepagio.aureus.backend.controller;
 
 import java.util.List;
-import java.util.Map;
 
-import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.DeleteMapping;
 import org.springframework.web.bind.annotation.GetMapping;
@@ -36,35 +34,21 @@ public class ReceitaVariavelController {
     }
 
     @PostMapping
-    public ResponseEntity<?> criar(final @Valid @RequestBody ReceitaVariavelRequestDTO dto) {
-        try {
-            return ResponseEntity.ok(receitaVariavelService.criar(dto));
-        } catch (DataIntegrityViolationException e) {
-            return ResponseEntity.badRequest().body(Map.of("message", "Erro de integridade relacional. Verifique os vínculos informados."));
-        }
+    public ResponseEntity<ReceitaVariavelResponseDTO> criar(final @Valid @RequestBody ReceitaVariavelRequestDTO dto) {
+        return ResponseEntity.ok(receitaVariavelService.criar(dto));
     }
 
     @PutMapping("/{id}")
-    public ResponseEntity<?> atualizar(final @PathVariable Long id, final @Valid @RequestBody ReceitaVariavelRequestDTO dto) {
-        try {
-            return receitaVariavelService.atualizar(id, dto)
-                    .map(ResponseEntity::ok)
-                    .orElse(ResponseEntity.notFound().build());
-        } catch (DataIntegrityViolationException e) {
-            return ResponseEntity.badRequest().body(Map.of("message", "Erro de integridade relacional. Verifique os vínculos informados."));
-        }
+    public ResponseEntity<ReceitaVariavelResponseDTO> atualizar(final @PathVariable Long id, final @Valid @RequestBody ReceitaVariavelRequestDTO dto) {
+        return ResponseEntity.ok(receitaVariavelService.atualizar(id, dto));
     }
 
     @DeleteMapping("/{id}")
-    public ResponseEntity<?> excluir(final @PathVariable Long id) {
-        try {
-            boolean excluido = receitaVariavelService.excluir(id);
-            if (!excluido) {
-                return ResponseEntity.notFound().build();
-            }
-            return ResponseEntity.noContent().build();
-        } catch (final DataIntegrityViolationException e) {
-            return ResponseEntity.badRequest().body(Map.of("message", "Erro ao excluir o registro."));
+    public ResponseEntity<Void> excluir(final @PathVariable Long id) {
+        boolean excluido = receitaVariavelService.excluir(id);
+        if (!excluido) {
+            return ResponseEntity.notFound().build();
         }
+        return ResponseEntity.noContent().build();
     }
 }
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java
index 3dcf1cd..9cc0f97 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java
@@ -11,6 +11,7 @@ import jakarta.persistence.FetchType;
 import jakarta.persistence.GeneratedValue;
 import jakarta.persistence.GenerationType;
 import jakarta.persistence.Id;
+import jakarta.persistence.Index;
 import jakarta.persistence.JoinColumn;
 import jakarta.persistence.ManyToOne;
 import jakarta.persistence.Table;
@@ -25,7 +26,9 @@ import lombok.NoArgsConstructor;
 import lombok.Setter;
 
 @Entity
-@Table(name = "despesas_fixas")
+@Table(name = "despesas_fixas", indexes = {
+    @Index(name = "idx_despesas_fixas_data_inicio", columnList = "data_inicio")
+})
 @Getter
 @Setter
 @NoArgsConstructor
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavel.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavel.java
index 1efee0f..3b46806 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavel.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavel.java
@@ -11,6 +11,7 @@ import jakarta.persistence.FetchType;
 import jakarta.persistence.GeneratedValue;
 import jakarta.persistence.GenerationType;
 import jakarta.persistence.Id;
+import jakarta.persistence.Index;
 import jakarta.persistence.JoinColumn;
 import jakarta.persistence.ManyToOne;
 import jakarta.persistence.Table;
@@ -27,7 +28,9 @@ import lombok.NoArgsConstructor;
 import lombok.Setter;
 
 @Entity
-@Table(name = "despesas_variaveis")
+@Table(name = "despesas_variaveis", indexes = {
+    @Index(name = "idx_despesas_variaveis_datas", columnList = "data_inicio, data_fim")
+})
 @Getter
 @Setter
 @NoArgsConstructor
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/ReceitaFixa.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/ReceitaFixa.java
index f6511d6..1105507 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/ReceitaFixa.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/ReceitaFixa.java
@@ -11,6 +11,7 @@ import jakarta.persistence.FetchType;
 import jakarta.persistence.GeneratedValue;
 import jakarta.persistence.GenerationType;
 import jakarta.persistence.Id;
+import jakarta.persistence.Index;
 import jakarta.persistence.JoinColumn;
 import jakarta.persistence.ManyToOne;
 import jakarta.persistence.Table;
@@ -25,7 +26,9 @@ import lombok.NoArgsConstructor;
 import lombok.Setter;
 
 @Entity
-@Table(name = "receitas_fixas")
+@Table(name = "receitas_fixas", indexes = {
+    @Index(name = "idx_receitas_fixas_data_inicio", columnList = "data_inicio")
+})
 @Getter
 @Setter
 @NoArgsConstructor
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/ReceitaVariavel.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/ReceitaVariavel.java
index ffd4ef3..d3f5f1a 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/ReceitaVariavel.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/ReceitaVariavel.java
@@ -11,6 +11,7 @@ import jakarta.persistence.FetchType;
 import jakarta.persistence.GeneratedValue;
 import jakarta.persistence.GenerationType;
 import jakarta.persistence.Id;
+import jakarta.persistence.Index;
 import jakarta.persistence.JoinColumn;
 import jakarta.persistence.ManyToOne;
 import jakarta.persistence.Table;
@@ -27,7 +28,9 @@ import lombok.NoArgsConstructor;
 import lombok.Setter;
 
 @Entity
-@Table(name = "receitas_variaveis")
+@Table(name = "receitas_variaveis", indexes = {
+    @Index(name = "idx_receitas_variaveis_datas", columnList = "data_inicio, data_fim")
+})
 @Getter
 @Setter
 @NoArgsConstructor
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/Usuario.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/Usuario.java
index c2c752a..6ad50de 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/Usuario.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/domain/Usuario.java
@@ -5,6 +5,7 @@ import jakarta.persistence.Entity;
 import jakarta.persistence.GeneratedValue;
 import jakarta.persistence.GenerationType;
 import jakarta.persistence.Id;
+import jakarta.persistence.Index;
 import jakarta.persistence.Table;
 import lombok.AllArgsConstructor;
 import lombok.Builder;
@@ -13,7 +14,9 @@ import lombok.NoArgsConstructor;
 import lombok.Setter;
 
 @Entity
-@Table(name = "usuarios")
+@Table(name = "usuarios", indexes = {
+    @Index(name = "idx_usuarios_google_subject_id", columnList = "google_subject_id")
+})
 @Getter
 @Setter
 @NoArgsConstructor
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/repository/CategoriaRepository.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/repository/CategoriaRepository.java
index ff84cd5..e6ba390 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/repository/CategoriaRepository.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/repository/CategoriaRepository.java
@@ -1,9 +1,17 @@
 package com.guilhermepagio.aureus.backend.repository;
 
 import java.util.List;
+import java.util.Optional;
+
 import org.springframework.data.jpa.repository.JpaRepository;
+import org.springframework.data.jpa.repository.Query;
+import org.springframework.data.repository.query.Param;
+
 import com.guilhermepagio.aureus.backend.domain.Categoria;
 
 public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
     List<Categoria> findByUsuarioIdOrderByDescricaoAsc(String usuarioId);
+
+    @Query(value = "SELECT usuario_id FROM categorias WHERE id = :id", nativeQuery = true)
+    Optional<String> findOwnerUsuarioId(@Param("id") Long id);
 }
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/repository/ContaRepository.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/repository/ContaRepository.java
index d20230e..db1e18c 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/repository/ContaRepository.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/repository/ContaRepository.java
@@ -1,11 +1,17 @@
 package com.guilhermepagio.aureus.backend.repository;
 
-import org.springframework.data.jpa.repository.JpaRepository;
-
 import java.util.List;
+import java.util.Optional;
+
+import org.springframework.data.jpa.repository.JpaRepository;
+import org.springframework.data.jpa.repository.Query;
+import org.springframework.data.repository.query.Param;
 
 import com.guilhermepagio.aureus.backend.domain.Conta;
 
 public interface ContaRepository extends JpaRepository<Conta, Long> {
     List<Conta> findByUsuarioId(String usuarioId);
+
+    @Query(value = "SELECT usuario_id FROM contas WHERE id = :id", nativeQuery = true)
+    Optional<String> findOwnerUsuarioId(@Param("id") Long id);
 }
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java
index 0613fb7..7b60dff 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java
@@ -9,6 +9,7 @@ import org.springframework.transaction.annotation.Transactional;
 import com.guilhermepagio.aureus.backend.domain.Categoria;
 import com.guilhermepagio.aureus.backend.domain.dto.CategoriaRequestDTO;
 import com.guilhermepagio.aureus.backend.domain.dto.CategoriaResponseDTO;
+import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
 import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
 
 @Service
@@ -42,13 +43,13 @@ public class CategoriaService {
     }
 
     @Transactional
-    public Optional<CategoriaResponseDTO> atualizar(Long id, CategoriaRequestDTO dto) {
-        return categoriaRepository.findById(id)
-                .map(categoria -> {
-                    categoria.setDescricao(dto.descricao());
-                    categoria.setObservacoes(dto.observacoes());
-                    return CategoriaResponseDTO.fromEntity(categoriaRepository.save(categoria));
-                });
+    public CategoriaResponseDTO atualizar(Long id, CategoriaRequestDTO dto) {
+        Categoria categoria = categoriaRepository.findById(id)
+                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + id));
+        categoria.setDescricao(dto.descricao());
+        categoria.setObservacoes(dto.observacoes());
+        Categoria salva = categoriaRepository.save(categoria);
+        return CategoriaResponseDTO.fromEntity(salva);
     }
 
     @Transactional
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java
index c3954b6..17dea4f 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java
@@ -35,6 +35,7 @@ import lombok.RequiredArgsConstructor;
 public class ConsolidacaoService {
 
     public static final Long SEM_CATEGORIA_ID = -1L;
+    public static final Long SEM_CONTA_ID = -1L;
 
     private final ContaRepository contaRepository;
     private final CategoriaRepository categoriaRepository;
@@ -59,10 +60,14 @@ public class ConsolidacaoService {
         Map<Long, LinhaConsolidacaoDTO> despesasMap = new LinkedHashMap<>();
         
         for (Conta c : contas) {
-            receitasMap.put(c.getId(), criarLinha(c));
-            despesasMap.put(c.getId(), criarLinha(c));
+            receitasMap.put(c.getId(), criarLinha(c.getId(), c.getDescricao()));
+            despesasMap.put(c.getId(), criarLinha(c.getId(), c.getDescricao()));
         }
 
+        // Linha sintética para "Sem Conta"
+        receitasMap.put(SEM_CONTA_ID, criarLinha(SEM_CONTA_ID, "Sem Conta"));
+        despesasMap.put(SEM_CONTA_ID, criarLinha(SEM_CONTA_ID, "Sem Conta"));
+
         List<ReceitaFixa> receitasFixas = receitaFixaRepository.findByUsuarioId(usuarioId);
         List<ReceitaVariavel> receitasVariaveis = receitaVariavelRepository.findByUsuarioId(usuarioId);
         List<DespesaFixa> despesasFixas = despesaFixaRepository.findByUsuarioId(usuarioId);
@@ -107,36 +112,36 @@ public class ConsolidacaoService {
             YearMonth currentMonth = startMonth.plusMonths(i);
 
             for (ReceitaFixa rf : receitasFixas) {
-                if (rf.getConta() == null) continue;
+                Long contaId = rf.getConta() != null ? rf.getConta().getId() : SEM_CONTA_ID;
                 YearMonth inicio = inicioReceitaFixa.get(rf.getId());
                 if (inicio == null || !inicio.isAfter(currentMonth)) {
-                    somarValor(receitasMap, rf.getConta().getId(), i, rf.getValor());
+                    somarValor(receitasMap, contaId, i, rf.getValor());
                 }
             }
 
             for (ReceitaVariavel rv : receitasVariaveis) {
-                if (rv.getConta() == null) continue;
+                Long contaId = rv.getConta() != null ? rv.getConta().getId() : SEM_CONTA_ID;
                 YearMonth inicio = inicioReceitaVariavel.get(rv.getId());
                 YearMonth fim = fimReceitaVariavel.get(rv.getId());
                 if (inicio != null && fim != null && !currentMonth.isBefore(inicio) && !currentMonth.isAfter(fim)) {
-                    somarValor(receitasMap, rv.getConta().getId(), i, rv.getValorParcela());
+                    somarValor(receitasMap, contaId, i, rv.getValorParcela());
                 }
             }
 
             for (DespesaFixa df : despesasFixas) {
-                if (df.getConta() == null) continue;
+                Long contaId = df.getConta() != null ? df.getConta().getId() : SEM_CONTA_ID;
                 YearMonth inicio = inicioDespesaFixa.get(df.getId());
                 if (inicio == null || !inicio.isAfter(currentMonth)) {
-                    somarValor(despesasMap, df.getConta().getId(), i, df.getValor());
+                    somarValor(despesasMap, contaId, i, df.getValor());
                 }
             }
 
             for (DespesaVariavel dv : despesasVariaveis) {
-                if (dv.getConta() == null) continue;
+                Long contaId = dv.getConta() != null ? dv.getConta().getId() : SEM_CONTA_ID;
                 YearMonth inicio = inicioDespesaVariavel.get(dv.getId());
                 YearMonth fim = fimDespesaVariavel.get(dv.getId());
                 if (inicio != null && fim != null && !currentMonth.isBefore(inicio) && !currentMonth.isAfter(fim)) {
-                    somarValor(despesasMap, dv.getConta().getId(), i, dv.getValorParcela());
+                    somarValor(despesasMap, contaId, i, dv.getValorParcela());
                 }
             }
         }
@@ -145,7 +150,6 @@ public class ConsolidacaoService {
         BigDecimal totalDespesasHistoricas = BigDecimal.ZERO;
 
         for (ReceitaFixa rf : receitasFixas) {
-            if (rf.getConta() == null || !receitasMap.containsKey(rf.getConta().getId())) continue;
             if (rf.getDataInicio() != null && rf.getValor() != null) {
                 YearMonth inicio = YearMonth.from(rf.getDataInicio());
                 if (inicio.isBefore(startMonth)) {
@@ -156,7 +160,6 @@ public class ConsolidacaoService {
         }
 
         for (ReceitaVariavel rv : receitasVariaveis) {
-            if (rv.getConta() == null || !receitasMap.containsKey(rv.getConta().getId())) continue;
             if (rv.getDataInicio() != null && rv.getQuantidadeParcelas() != null && rv.getQuantidadeParcelas() > 0 && rv.getValorParcela() != null) {
                 YearMonth inicio = YearMonth.from(rv.getDataInicio());
                 if (inicio.isBefore(startMonth)) {
@@ -169,7 +172,6 @@ public class ConsolidacaoService {
         }
 
         for (DespesaFixa df : despesasFixas) {
-            if (df.getConta() == null || !despesasMap.containsKey(df.getConta().getId())) continue;
             if (df.getDataInicio() != null && df.getValor() != null) {
                 YearMonth inicio = YearMonth.from(df.getDataInicio());
                 if (inicio.isBefore(startMonth)) {
@@ -180,7 +182,6 @@ public class ConsolidacaoService {
         }
 
         for (DespesaVariavel dv : despesasVariaveis) {
-            if (dv.getConta() == null || !despesasMap.containsKey(dv.getConta().getId())) continue;
             if (dv.getDataInicio() != null && dv.getQuantidadeParcelas() != null && dv.getQuantidadeParcelas() > 0 && dv.getValorParcela() != null) {
                 YearMonth inicio = YearMonth.from(dv.getDataInicio());
                 if (inicio.isBefore(startMonth)) {
@@ -230,16 +231,19 @@ public class ConsolidacaoService {
         );
     }
 
-    private LinhaConsolidacaoDTO criarLinha(Conta conta) {
+    private LinhaConsolidacaoDTO criarLinha(Long id, String descricao) {
         List<BigDecimal> valores = new ArrayList<>(24);
         for (int i = 0; i < 24; i++) {
             valores.add(BigDecimal.ZERO);
         }
-        return new LinhaConsolidacaoDTO(conta.getId(), conta.getDescricao(), valores);
+        return new LinhaConsolidacaoDTO(id, descricao, valores);
     }
 
     private void somarValor(Map<Long, LinhaConsolidacaoDTO> map, Long contaId, int index, BigDecimal valor) {
         LinhaConsolidacaoDTO linha = map.get(contaId);
+        if (linha == null) {
+            linha = map.get(SEM_CONTA_ID);
+        }
         if (linha != null && valor != null) {
             BigDecimal atual = linha.getValoresMensais().get(index);
             linha.getValoresMensais().set(index, atual.add(valor));
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java
index b01148d..2371d86 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java
@@ -9,6 +9,7 @@ import org.springframework.transaction.annotation.Transactional;
 import com.guilhermepagio.aureus.backend.domain.Conta;
 import com.guilhermepagio.aureus.backend.domain.dto.ContaRequestDTO;
 import com.guilhermepagio.aureus.backend.domain.dto.ContaResponseDTO;
+import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
 import com.guilhermepagio.aureus.backend.repository.ContaRepository;
 
 @Service
@@ -42,13 +43,13 @@ public class ContaService {
     }
 
     @Transactional
-    public Optional<ContaResponseDTO> atualizar(Long id, ContaRequestDTO dto) {
-        return contaRepository.findById(id)
-                .map(conta -> {
-                    conta.setDescricao(dto.descricao());
-                    conta.setObservacoes(dto.observacoes());
-                    return ContaResponseDTO.fromEntity(contaRepository.save(conta));
-                });
+    public ContaResponseDTO atualizar(Long id, ContaRequestDTO dto) {
+        Conta conta = contaRepository.findById(id)
+                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + id));
+        conta.setDescricao(dto.descricao());
+        conta.setObservacoes(dto.observacoes());
+        Conta salva = contaRepository.save(conta);
+        return ContaResponseDTO.fromEntity(salva);
     }
 
     @Transactional
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaFixaService.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaFixaService.java
index e180ff2..12e2563 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaFixaService.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaFixaService.java
@@ -3,7 +3,7 @@ package com.guilhermepagio.aureus.backend.service;
 import java.util.List;
 import java.util.Optional;
 
-import org.springframework.dao.DataIntegrityViolationException;
+import org.springframework.security.access.AccessDeniedException;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
@@ -12,9 +12,11 @@ import com.guilhermepagio.aureus.backend.domain.Conta;
 import com.guilhermepagio.aureus.backend.domain.DespesaFixa;
 import com.guilhermepagio.aureus.backend.domain.dto.DespesaFixaRequestDTO;
 import com.guilhermepagio.aureus.backend.domain.dto.DespesaFixaResponseDTO;
+import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
 import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
 import com.guilhermepagio.aureus.backend.repository.ContaRepository;
 import com.guilhermepagio.aureus.backend.repository.DespesaFixaRepository;
+import com.guilhermepagio.aureus.backend.security.TenantContext;
 
 @Service
 public class DespesaFixaService {
@@ -43,12 +45,38 @@ public class DespesaFixaService {
         return despesaFixaRepository.findById(id).map(DespesaFixaResponseDTO::fromEntity);
     }
 
+    private Conta validarEObterConta(Long contaId) {
+        if (contaId == null) {
+            throw new ResourceNotFoundException("Conta não encontrada: null");
+        }
+        String ownerUsuarioId = contaRepository.findOwnerUsuarioId(contaId)
+                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + contaId));
+        String currentTenant = TenantContext.getTenantId();
+        if (currentTenant != null && !ownerUsuarioId.equals(currentTenant)) {
+            throw new AccessDeniedException("Acesso negado: a conta informada não pertence ao usuário autenticado");
+        }
+        return contaRepository.findById(contaId)
+                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + contaId));
+    }
+
+    private Categoria validarEObterCategoria(Long categoriaId) {
+        if (categoriaId == null) {
+            throw new ResourceNotFoundException("Categoria não encontrada: null");
+        }
+        String ownerUsuarioId = categoriaRepository.findOwnerUsuarioId(categoriaId)
+                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + categoriaId));
+        String currentTenant = TenantContext.getTenantId();
+        if (currentTenant != null && !ownerUsuarioId.equals(currentTenant)) {
+            throw new AccessDeniedException("Acesso negado: a categoria informada não pertence ao usuário autenticado");
+        }
+        return categoriaRepository.findById(categoriaId)
+                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + categoriaId));
+    }
+
     @Transactional
     public DespesaFixaResponseDTO criar(DespesaFixaRequestDTO dto) {
-        Conta conta = contaRepository.findById(dto.conta().id())
-                .orElseThrow(() -> new DataIntegrityViolationException("Conta não encontrada: " + dto.conta().id()));
-        Categoria categoria = categoriaRepository.findById(dto.categoria().id())
-                .orElseThrow(() -> new DataIntegrityViolationException("Categoria não encontrada: " + dto.categoria().id()));
+        Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
+        Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);
 
         DespesaFixa despesa = new DespesaFixa();
         despesa.setDescricao(dto.descricao());
@@ -63,24 +91,22 @@ public class DespesaFixaService {
     }
 
     @Transactional
-    public Optional<DespesaFixaResponseDTO> atualizar(Long id, DespesaFixaRequestDTO dto) {
-        return despesaFixaRepository.findById(id)
-                .map(existente -> {
-                    Conta conta = contaRepository.findById(dto.conta().id())
-                            .orElseThrow(() -> new DataIntegrityViolationException("Conta não encontrada: " + dto.conta().id()));
-                    Categoria categoria = categoriaRepository.findById(dto.categoria().id())
-                            .orElseThrow(() -> new DataIntegrityViolationException("Categoria não encontrada: " + dto.categoria().id()));
-
-                    existente.setDescricao(dto.descricao());
-                    existente.setValor(dto.valor());
-                    existente.setConta(conta);
-                    existente.setCategoria(categoria);
-                    existente.setObservacoes(dto.observacoes());
-                    existente.setDataInicio(dto.dataInicio());
-
-                    DespesaFixa salva = despesaFixaRepository.saveAndFlush(existente);
-                    return DespesaFixaResponseDTO.fromEntity(salva);
-                });
+    public DespesaFixaResponseDTO atualizar(Long id, DespesaFixaRequestDTO dto) {
+        DespesaFixa existente = despesaFixaRepository.findById(id)
+                .orElseThrow(() -> new ResourceNotFoundException("Despesa fixa não encontrada: " + id));
+
+        Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
+        Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);
+
+        existente.setDescricao(dto.descricao());
+        existente.setValor(dto.valor());
+        existente.setConta(conta);
+        existente.setCategoria(categoria);
+        existente.setObservacoes(dto.observacoes());
+        existente.setDataInicio(dto.dataInicio());
+
+        DespesaFixa salva = despesaFixaRepository.saveAndFlush(existente);
+        return DespesaFixaResponseDTO.fromEntity(salva);
     }
 
     @Transactional
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java
index 90a0169..582cad7 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java
@@ -3,7 +3,7 @@ package com.guilhermepagio.aureus.backend.service;
 import java.util.List;
 import java.util.Optional;
 
-import org.springframework.dao.DataIntegrityViolationException;
+import org.springframework.security.access.AccessDeniedException;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
@@ -12,9 +12,11 @@ import com.guilhermepagio.aureus.backend.domain.Conta;
 import com.guilhermepagio.aureus.backend.domain.DespesaVariavel;
 import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelRequestDTO;
 import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelResponseDTO;
+import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
 import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
 import com.guilhermepagio.aureus.backend.repository.ContaRepository;
 import com.guilhermepagio.aureus.backend.repository.DespesaVariavelRepository;
+import com.guilhermepagio.aureus.backend.security.TenantContext;
 
 @Service
 public class DespesaVariavelService {
@@ -50,12 +52,38 @@ public class DespesaVariavelService {
         }
     }
 
+    private Conta validarEObterConta(Long contaId) {
+        if (contaId == null) {
+            throw new ResourceNotFoundException("Conta não encontrada: null");
+        }
+        String ownerUsuarioId = contaRepository.findOwnerUsuarioId(contaId)
+                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + contaId));
+        String currentTenant = TenantContext.getTenantId();
+        if (currentTenant != null && !ownerUsuarioId.equals(currentTenant)) {
+            throw new AccessDeniedException("Acesso negado: a conta informada não pertence ao usuário autenticado");
+        }
+        return contaRepository.findById(contaId)
+                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + contaId));
+    }
+
+    private Categoria validarEObterCategoria(Long categoriaId) {
+        if (categoriaId == null) {
+            throw new ResourceNotFoundException("Categoria não encontrada: null");
+        }
+        String ownerUsuarioId = categoriaRepository.findOwnerUsuarioId(categoriaId)
+                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + categoriaId));
+        String currentTenant = TenantContext.getTenantId();
+        if (currentTenant != null && !ownerUsuarioId.equals(currentTenant)) {
+            throw new AccessDeniedException("Acesso negado: a categoria informada não pertence ao usuário autenticado");
+        }
+        return categoriaRepository.findById(categoriaId)
+                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + categoriaId));
+    }
+
     @Transactional
     public DespesaVariavelResponseDTO criar(DespesaVariavelRequestDTO dto) {
-        Conta conta = contaRepository.findById(dto.conta().id())
-                .orElseThrow(() -> new DataIntegrityViolationException("Conta não encontrada: " + dto.conta().id()));
-        Categoria categoria = categoriaRepository.findById(dto.categoria().id())
-                .orElseThrow(() -> new DataIntegrityViolationException("Categoria não encontrada: " + dto.categoria().id()));
+        Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
+        Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);
 
         DespesaVariavel despesa = new DespesaVariavel();
         despesa.setDescricao(dto.descricao());
@@ -74,28 +102,26 @@ public class DespesaVariavelService {
     }
 
     @Transactional
-    public Optional<DespesaVariavelResponseDTO> atualizar(Long id, DespesaVariavelRequestDTO dto) {
-        return despesaVariavelRepository.findById(id)
-                .map(existente -> {
-                    Conta conta = contaRepository.findById(dto.conta().id())
-                            .orElseThrow(() -> new DataIntegrityViolationException("Conta não encontrada: " + dto.conta().id()));
-                    Categoria categoria = categoriaRepository.findById(dto.categoria().id())
-                            .orElseThrow(() -> new DataIntegrityViolationException("Categoria não encontrada: " + dto.categoria().id()));
-
-                    existente.setDescricao(dto.descricao());
-                    existente.setLocalCompra(dto.localCompra());
-                    existente.setDataCompra(dto.dataCompra());
-                    existente.setValorParcela(dto.valorParcela());
-                    existente.setQuantidadeParcelas(dto.quantidadeParcelas());
-                    existente.setDataInicio(dto.dataInicio());
-                    preencherDataFim(existente);
-                    existente.setConta(conta);
-                    existente.setCategoria(categoria);
-                    existente.setObservacoes(dto.observacoes());
-
-                    DespesaVariavel salva = despesaVariavelRepository.saveAndFlush(existente);
-                    return DespesaVariavelResponseDTO.fromEntity(salva);
-                });
+    public DespesaVariavelResponseDTO atualizar(Long id, DespesaVariavelRequestDTO dto) {
+        DespesaVariavel existente = despesaVariavelRepository.findById(id)
+                .orElseThrow(() -> new ResourceNotFoundException("Despesa variável não encontrada: " + id));
+
+        Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
+        Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);
+
+        existente.setDescricao(dto.descricao());
+        existente.setLocalCompra(dto.localCompra());
+        existente.setDataCompra(dto.dataCompra());
+        existente.setValorParcela(dto.valorParcela());
+        existente.setQuantidadeParcelas(dto.quantidadeParcelas());
+        existente.setDataInicio(dto.dataInicio());
+        preencherDataFim(existente);
+        existente.setConta(conta);
+        existente.setCategoria(categoria);
+        existente.setObservacoes(dto.observacoes());
+
+        DespesaVariavel salva = despesaVariavelRepository.saveAndFlush(existente);
+        return DespesaVariavelResponseDTO.fromEntity(salva);
     }
 
     @Transactional
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaFixaService.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaFixaService.java
index d98c30f..4c6bf36 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaFixaService.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaFixaService.java
@@ -3,7 +3,7 @@ package com.guilhermepagio.aureus.backend.service;
 import java.util.List;
 import java.util.Optional;
 
-import org.springframework.dao.DataIntegrityViolationException;
+import org.springframework.security.access.AccessDeniedException;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
@@ -12,9 +12,11 @@ import com.guilhermepagio.aureus.backend.domain.Conta;
 import com.guilhermepagio.aureus.backend.domain.ReceitaFixa;
 import com.guilhermepagio.aureus.backend.domain.dto.ReceitaFixaRequestDTO;
 import com.guilhermepagio.aureus.backend.domain.dto.ReceitaFixaResponseDTO;
+import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
 import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
 import com.guilhermepagio.aureus.backend.repository.ContaRepository;
 import com.guilhermepagio.aureus.backend.repository.ReceitaFixaRepository;
+import com.guilhermepagio.aureus.backend.security.TenantContext;
 
 @Service
 public class ReceitaFixaService {
@@ -43,12 +45,38 @@ public class ReceitaFixaService {
         return receitaFixaRepository.findById(id).map(ReceitaFixaResponseDTO::fromEntity);
     }
 
+    private Conta validarEObterConta(Long contaId) {
+        if (contaId == null) {
+            throw new ResourceNotFoundException("Conta não encontrada: null");
+        }
+        String ownerUsuarioId = contaRepository.findOwnerUsuarioId(contaId)
+                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + contaId));
+        String currentTenant = TenantContext.getTenantId();
+        if (currentTenant != null && !ownerUsuarioId.equals(currentTenant)) {
+            throw new AccessDeniedException("Acesso negado: a conta informada não pertence ao usuário autenticado");
+        }
+        return contaRepository.findById(contaId)
+                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + contaId));
+    }
+
+    private Categoria validarEObterCategoria(Long categoriaId) {
+        if (categoriaId == null) {
+            throw new ResourceNotFoundException("Categoria não encontrada: null");
+        }
+        String ownerUsuarioId = categoriaRepository.findOwnerUsuarioId(categoriaId)
+                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + categoriaId));
+        String currentTenant = TenantContext.getTenantId();
+        if (currentTenant != null && !ownerUsuarioId.equals(currentTenant)) {
+            throw new AccessDeniedException("Acesso negado: a categoria informada não pertence ao usuário autenticado");
+        }
+        return categoriaRepository.findById(categoriaId)
+                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + categoriaId));
+    }
+
     @Transactional
     public ReceitaFixaResponseDTO criar(ReceitaFixaRequestDTO dto) {
-        Conta conta = contaRepository.findById(dto.conta().id())
-                .orElseThrow(() -> new DataIntegrityViolationException("Conta não encontrada: " + dto.conta().id()));
-        Categoria categoria = categoriaRepository.findById(dto.categoria().id())
-                .orElseThrow(() -> new DataIntegrityViolationException("Categoria não encontrada: " + dto.categoria().id()));
+        Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
+        Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);
 
         ReceitaFixa receita = new ReceitaFixa();
         receita.setDescricao(dto.descricao());
@@ -63,24 +91,22 @@ public class ReceitaFixaService {
     }
 
     @Transactional
-    public Optional<ReceitaFixaResponseDTO> atualizar(Long id, ReceitaFixaRequestDTO dto) {
-        return receitaFixaRepository.findById(id)
-                .map(existente -> {
-                    Conta conta = contaRepository.findById(dto.conta().id())
-                            .orElseThrow(() -> new DataIntegrityViolationException("Conta não encontrada: " + dto.conta().id()));
-                    Categoria categoria = categoriaRepository.findById(dto.categoria().id())
-                            .orElseThrow(() -> new DataIntegrityViolationException("Categoria não encontrada: " + dto.categoria().id()));
-
-                    existente.setDescricao(dto.descricao());
-                    existente.setValor(dto.valor());
-                    existente.setConta(conta);
-                    existente.setCategoria(categoria);
-                    existente.setObservacoes(dto.observacoes());
-                    existente.setDataInicio(dto.dataInicio());
-
-                    ReceitaFixa salva = receitaFixaRepository.saveAndFlush(existente);
-                    return ReceitaFixaResponseDTO.fromEntity(salva);
-                });
+    public ReceitaFixaResponseDTO atualizar(Long id, ReceitaFixaRequestDTO dto) {
+        ReceitaFixa existente = receitaFixaRepository.findById(id)
+                .orElseThrow(() -> new ResourceNotFoundException("Receita fixa não encontrada: " + id));
+
+        Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
+        Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);
+
+        existente.setDescricao(dto.descricao());
+        existente.setValor(dto.valor());
+        existente.setConta(conta);
+        existente.setCategoria(categoria);
+        existente.setObservacoes(dto.observacoes());
+        existente.setDataInicio(dto.dataInicio());
+
+        ReceitaFixa salva = receitaFixaRepository.saveAndFlush(existente);
+        return ReceitaFixaResponseDTO.fromEntity(salva);
     }
 
     @Transactional
diff --git a/backend/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelService.java b/backend/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelService.java
index cf229b7..ec42354 100644
--- a/backend/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelService.java
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelService.java
@@ -3,7 +3,7 @@ package com.guilhermepagio.aureus.backend.service;
 import java.util.List;
 import java.util.Optional;
 
-import org.springframework.dao.DataIntegrityViolationException;
+import org.springframework.security.access.AccessDeniedException;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
@@ -12,9 +12,11 @@ import com.guilhermepagio.aureus.backend.domain.Conta;
 import com.guilhermepagio.aureus.backend.domain.ReceitaVariavel;
 import com.guilhermepagio.aureus.backend.domain.dto.ReceitaVariavelRequestDTO;
 import com.guilhermepagio.aureus.backend.domain.dto.ReceitaVariavelResponseDTO;
+import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
 import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
 import com.guilhermepagio.aureus.backend.repository.ContaRepository;
 import com.guilhermepagio.aureus.backend.repository.ReceitaVariavelRepository;
+import com.guilhermepagio.aureus.backend.security.TenantContext;
 
 @Service
 public class ReceitaVariavelService {
@@ -50,12 +52,38 @@ public class ReceitaVariavelService {
         }
     }
 
+    private Conta validarEObterConta(Long contaId) {
+        if (contaId == null) {
+            throw new ResourceNotFoundException("Conta não encontrada: null");
+        }
+        String ownerUsuarioId = contaRepository.findOwnerUsuarioId(contaId)
+                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + contaId));
+        String currentTenant = TenantContext.getTenantId();
+        if (currentTenant != null && !ownerUsuarioId.equals(currentTenant)) {
+            throw new AccessDeniedException("Acesso negado: a conta informada não pertence ao usuário autenticado");
+        }
+        return contaRepository.findById(contaId)
+                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + contaId));
+    }
+
+    private Categoria validarEObterCategoria(Long categoriaId) {
+        if (categoriaId == null) {
+            throw new ResourceNotFoundException("Categoria não encontrada: null");
+        }
+        String ownerUsuarioId = categoriaRepository.findOwnerUsuarioId(categoriaId)
+                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + categoriaId));
+        String currentTenant = TenantContext.getTenantId();
+        if (currentTenant != null && !ownerUsuarioId.equals(currentTenant)) {
+            throw new AccessDeniedException("Acesso negado: a categoria informada não pertence ao usuário autenticado");
+        }
+        return categoriaRepository.findById(categoriaId)
+                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + categoriaId));
+    }
+
     @Transactional
     public ReceitaVariavelResponseDTO criar(ReceitaVariavelRequestDTO dto) {
-        Conta conta = contaRepository.findById(dto.conta().id())
-                .orElseThrow(() -> new DataIntegrityViolationException("Conta não encontrada: " + dto.conta().id()));
-        Categoria categoria = categoriaRepository.findById(dto.categoria().id())
-                .orElseThrow(() -> new DataIntegrityViolationException("Categoria não encontrada: " + dto.categoria().id()));
+        Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
+        Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);
 
         ReceitaVariavel receita = new ReceitaVariavel();
         receita.setDescricao(dto.descricao());
@@ -72,26 +100,24 @@ public class ReceitaVariavelService {
     }
 
     @Transactional
-    public Optional<ReceitaVariavelResponseDTO> atualizar(Long id, ReceitaVariavelRequestDTO dto) {
-        return receitaVariavelRepository.findById(id)
-                .map(existente -> {
-                    Conta conta = contaRepository.findById(dto.conta().id())
-                            .orElseThrow(() -> new DataIntegrityViolationException("Conta não encontrada: " + dto.conta().id()));
-                    Categoria categoria = categoriaRepository.findById(dto.categoria().id())
-                            .orElseThrow(() -> new DataIntegrityViolationException("Categoria não encontrada: " + dto.categoria().id()));
-
-                    existente.setDescricao(dto.descricao());
-                    existente.setValorParcela(dto.valorParcela());
-                    existente.setQuantidadeParcelas(dto.quantidadeParcelas());
-                    existente.setDataInicio(dto.dataInicio());
-                    preencherDataFim(existente);
-                    existente.setConta(conta);
-                    existente.setCategoria(categoria);
-                    existente.setObservacoes(dto.observacoes());
-
-                    ReceitaVariavel salva = receitaVariavelRepository.saveAndFlush(existente);
-                    return ReceitaVariavelResponseDTO.fromEntity(salva);
-                });
+    public ReceitaVariavelResponseDTO atualizar(Long id, ReceitaVariavelRequestDTO dto) {
+        ReceitaVariavel existente = receitaVariavelRepository.findById(id)
+                .orElseThrow(() -> new ResourceNotFoundException("Receita variável não encontrada: " + id));
+
+        Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
+        Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);
+
+        existente.setDescricao(dto.descricao());
+        existente.setValorParcela(dto.valorParcela());
+        existente.setQuantidadeParcelas(dto.quantidadeParcelas());
+        existente.setDataInicio(dto.dataInicio());
+        preencherDataFim(existente);
+        existente.setConta(conta);
+        existente.setCategoria(categoria);
+        existente.setObservacoes(dto.observacoes());
+
+        ReceitaVariavel salva = receitaVariavelRepository.saveAndFlush(existente);
+        return ReceitaVariavelResponseDTO.fromEntity(salva);
     }
 
     @Transactional
diff --git a/backend/src/main/resources/application.yaml b/backend/src/main/resources/application.yaml
index 73726e9..75c6c17 100644
--- a/backend/src/main/resources/application.yaml
+++ b/backend/src/main/resources/application.yaml
@@ -12,6 +12,10 @@ spring:
       hibernate:
         dialect: org.hibernate.dialect.PostgreSQLDialect
         "[tenant_identifier_resolver]": com.guilhermepagio.aureus.backend.security.CurrentTenantIdentifierResolverImpl
+  flyway:
+    enabled: true
+    baseline-on-migrate: true
+    baseline-version: '0'
   security:
     oauth2:
       client:
diff --git a/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/CategoriaControllerTest.java b/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/CategoriaControllerTest.java
index 5d7593d..3518279 100644
--- a/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/CategoriaControllerTest.java
+++ b/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/CategoriaControllerTest.java
@@ -44,7 +44,9 @@ public class CategoriaControllerTest {
 
     @BeforeEach
     public void setup() {
-        mockMvc = MockMvcBuilders.standaloneSetup(categoriaController).build();
+        mockMvc = MockMvcBuilders.standaloneSetup(categoriaController)
+                .setControllerAdvice(new com.guilhermepagio.aureus.backend.exception.GlobalExceptionHandler())
+                .build();
     }
 
     @Test
@@ -86,7 +88,7 @@ public class CategoriaControllerTest {
     public void deveAtualizarCategoriaExistente() throws Exception {
         CategoriaRequestDTO request = new CategoriaRequestDTO("Mercado", "Atualizada");
         CategoriaResponseDTO response = new CategoriaResponseDTO(1L, "Mercado", "Atualizada");
-        when(categoriaService.atualizar(eq(1L), any(CategoriaRequestDTO.class))).thenReturn(Optional.of(response));
+        when(categoriaService.atualizar(eq(1L), any(CategoriaRequestDTO.class))).thenReturn(response);
 
         mockMvc.perform(put("/api/categorias/1")
                 .contentType(MediaType.APPLICATION_JSON)
@@ -98,7 +100,8 @@ public class CategoriaControllerTest {
     @Test
     public void deveRetornar404AoAtualizarCategoriaInexistente() throws Exception {
         CategoriaRequestDTO request = new CategoriaRequestDTO("Nova", "");
-        when(categoriaService.atualizar(eq(999L), any(CategoriaRequestDTO.class))).thenReturn(Optional.empty());
+        when(categoriaService.atualizar(eq(999L), any(CategoriaRequestDTO.class)))
+                .thenThrow(new com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException("Categoria não encontrada: 999"));
 
         mockMvc.perform(put("/api/categorias/999")
                 .contentType(MediaType.APPLICATION_JSON)
diff --git a/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ContaControllerTest.java b/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ContaControllerTest.java
index 274f268..f7a1aee 100644
--- a/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ContaControllerTest.java
+++ b/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ContaControllerTest.java
@@ -44,7 +44,9 @@ public class ContaControllerTest {
 
     @BeforeEach
     public void setup() {
-        mockMvc = MockMvcBuilders.standaloneSetup(contaController).build();
+        mockMvc = MockMvcBuilders.standaloneSetup(contaController)
+                .setControllerAdvice(new com.guilhermepagio.aureus.backend.exception.GlobalExceptionHandler())
+                .build();
     }
 
     @Test
@@ -86,7 +88,7 @@ public class ContaControllerTest {
     public void deveAtualizarContaExistente() throws Exception {
         ContaRequestDTO request = new ContaRequestDTO("Nubank PJ", "Atualizada");
         ContaResponseDTO response = new ContaResponseDTO(1L, "Nubank PJ", "Atualizada");
-        when(contaService.atualizar(eq(1L), any(ContaRequestDTO.class))).thenReturn(Optional.of(response));
+        when(contaService.atualizar(eq(1L), any(ContaRequestDTO.class))).thenReturn(response);
 
         mockMvc.perform(put("/api/contas/1")
                 .contentType(MediaType.APPLICATION_JSON)
@@ -98,7 +100,8 @@ public class ContaControllerTest {
     @Test
     public void deveRetornar404AoAtualizarContaInexistente() throws Exception {
         ContaRequestDTO request = new ContaRequestDTO("Nova", "");
-        when(contaService.atualizar(eq(999L), any(ContaRequestDTO.class))).thenReturn(Optional.empty());
+        when(contaService.atualizar(eq(999L), any(ContaRequestDTO.class)))
+                .thenThrow(new com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException("Conta não encontrada: 999"));
 
         mockMvc.perform(put("/api/contas/999")
                 .contentType(MediaType.APPLICATION_JSON)
diff --git a/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaControllerTest.java b/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaControllerTest.java
index 228eaa9..6936289 100644
--- a/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaControllerTest.java
+++ b/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaControllerTest.java
@@ -46,7 +46,9 @@ public class DespesaFixaControllerTest {
 
     @BeforeEach
     public void setup() {
-        mockMvc = MockMvcBuilders.standaloneSetup(despesaFixaController).build();
+        mockMvc = MockMvcBuilders.standaloneSetup(despesaFixaController)
+                .setControllerAdvice(new com.guilhermepagio.aureus.backend.exception.GlobalExceptionHandler())
+                .build();
     }
 
     @Test
@@ -121,7 +123,7 @@ public class DespesaFixaControllerTest {
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(json))
                 .andExpect(status().isBadRequest())
-                .andExpect(jsonPath("$.message").value("Erro de integridade relacional. Verifique os vínculos informados."));
+                .andExpect(jsonPath("$.status").value(400));
     }
 
     @Test
@@ -140,7 +142,7 @@ public class DespesaFixaControllerTest {
         CategoriaResponseDTO categoria = new CategoriaResponseDTO(2L, "Moradia", "Aluguel");
         DespesaFixaResponseDTO response = new DespesaFixaResponseDTO(10L, "Aluguel", new BigDecimal("1300.00"), conta, categoria, "Obs", LocalDate.of(2024, 1, 1));
 
-        when(despesaFixaService.atualizar(eq(10L), any(DespesaFixaRequestDTO.class))).thenReturn(Optional.of(response));
+        when(despesaFixaService.atualizar(eq(10L), any(DespesaFixaRequestDTO.class))).thenReturn(response);
 
         mockMvc.perform(put("/api/despesas-fixas/10")
                 .contentType(MediaType.APPLICATION_JSON)
@@ -162,7 +164,8 @@ public class DespesaFixaControllerTest {
         }
         """;
 
-        when(despesaFixaService.atualizar(eq(999L), any(DespesaFixaRequestDTO.class))).thenReturn(Optional.empty());
+        when(despesaFixaService.atualizar(eq(999L), any(DespesaFixaRequestDTO.class)))
+                .thenThrow(new com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException("Despesa fixa não encontrada: 999"));
 
         mockMvc.perform(put("/api/despesas-fixas/999")
                 .contentType(MediaType.APPLICATION_JSON)
@@ -208,7 +211,7 @@ public class DespesaFixaControllerTest {
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(json))
                 .andExpect(status().isBadRequest())
-                .andExpect(jsonPath("$.message").value("Erro de integridade relacional. Verifique os vínculos informados."));
+                .andExpect(jsonPath("$.status").value(400));
     }
 
     @Test
@@ -235,6 +238,6 @@ public class DespesaFixaControllerTest {
 
         mockMvc.perform(delete("/api/despesas-fixas/10"))
                 .andExpect(status().isBadRequest())
-                .andExpect(jsonPath("$.message").value("Não é possível excluir esta despesa porque ela está em uso."));
+                .andExpect(jsonPath("$.status").value(400));
     }
 }
diff --git a/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/DespesaVariavelControllerTest.java b/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/DespesaVariavelControllerTest.java
index 1b264dc..6ea2c1d 100644
--- a/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/DespesaVariavelControllerTest.java
+++ b/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/DespesaVariavelControllerTest.java
@@ -46,7 +46,9 @@ public class DespesaVariavelControllerTest {
 
     @BeforeEach
     public void setup() {
-        mockMvc = MockMvcBuilders.standaloneSetup(despesaVariavelController).build();
+        mockMvc = MockMvcBuilders.standaloneSetup(despesaVariavelController)
+                .setControllerAdvice(new com.guilhermepagio.aureus.backend.exception.GlobalExceptionHandler())
+                .build();
     }
 
     @Test
@@ -137,7 +139,7 @@ public class DespesaVariavelControllerTest {
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(json))
                 .andExpect(status().isBadRequest())
-                .andExpect(jsonPath("$.message").value("Erro de integridade relacional. Verifique os vínculos informados."));
+                .andExpect(jsonPath("$.status").value(400));
     }
 
     @Test
@@ -163,7 +165,7 @@ public class DespesaVariavelControllerTest {
             conta, categoria, "Obs"
         );
 
-        when(despesaVariavelService.atualizar(eq(10L), any(DespesaVariavelRequestDTO.class))).thenReturn(Optional.of(response));
+        when(despesaVariavelService.atualizar(eq(10L), any(DespesaVariavelRequestDTO.class))).thenReturn(response);
 
         mockMvc.perform(put("/api/despesas-variaveis/10")
                 .contentType(MediaType.APPLICATION_JSON)
@@ -188,7 +190,8 @@ public class DespesaVariavelControllerTest {
         }
         """;
 
-        when(despesaVariavelService.atualizar(eq(999L), any(DespesaVariavelRequestDTO.class))).thenReturn(Optional.empty());
+        when(despesaVariavelService.atualizar(eq(999L), any(DespesaVariavelRequestDTO.class)))
+                .thenThrow(new com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException("Despesa variável não encontrada: 999"));
 
         mockMvc.perform(put("/api/despesas-variaveis/999")
                 .contentType(MediaType.APPLICATION_JSON)
@@ -239,7 +242,7 @@ public class DespesaVariavelControllerTest {
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(json))
                 .andExpect(status().isBadRequest())
-                .andExpect(jsonPath("$.message").value("Erro de integridade relacional. Verifique os vínculos informados."));
+                .andExpect(jsonPath("$.status").value(400));
     }
 
     @Test
@@ -266,6 +269,6 @@ public class DespesaVariavelControllerTest {
 
         mockMvc.perform(delete("/api/despesas-variaveis/10"))
                 .andExpect(status().isBadRequest())
-                .andExpect(jsonPath("$.message").value("Erro ao excluir o registro."));
+                .andExpect(jsonPath("$.status").value(400));
     }
 }
diff --git a/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ReceitaFixaControllerTest.java b/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ReceitaFixaControllerTest.java
index 6c50908..204bd87 100644
--- a/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ReceitaFixaControllerTest.java
+++ b/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ReceitaFixaControllerTest.java
@@ -46,7 +46,9 @@ public class ReceitaFixaControllerTest {
 
     @BeforeEach
     public void setup() {
-        mockMvc = MockMvcBuilders.standaloneSetup(receitaFixaController).build();
+        mockMvc = MockMvcBuilders.standaloneSetup(receitaFixaController)
+                .setControllerAdvice(new com.guilhermepagio.aureus.backend.exception.GlobalExceptionHandler())
+                .build();
     }
 
     @Test
@@ -121,7 +123,7 @@ public class ReceitaFixaControllerTest {
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(json))
                 .andExpect(status().isBadRequest())
-                .andExpect(jsonPath("$.message").value("Erro de integridade relacional. Verifique os vínculos informados."));
+                .andExpect(jsonPath("$.status").value(400));
     }
 
     @Test
@@ -140,7 +142,7 @@ public class ReceitaFixaControllerTest {
         CategoriaResponseDTO categoria = new CategoriaResponseDTO(2L, "Trabalho", "Salário");
         ReceitaFixaResponseDTO response = new ReceitaFixaResponseDTO(10L, "Salário", new BigDecimal("5500.00"), conta, categoria, "Obs", LocalDate.of(2024, 1, 1));
 
-        when(receitaFixaService.atualizar(eq(10L), any(ReceitaFixaRequestDTO.class))).thenReturn(Optional.of(response));
+        when(receitaFixaService.atualizar(eq(10L), any(ReceitaFixaRequestDTO.class))).thenReturn(response);
 
         mockMvc.perform(put("/api/receitas-fixas/10")
                 .contentType(MediaType.APPLICATION_JSON)
@@ -162,7 +164,8 @@ public class ReceitaFixaControllerTest {
         }
         """;
 
-        when(receitaFixaService.atualizar(eq(999L), any(ReceitaFixaRequestDTO.class))).thenReturn(Optional.empty());
+        when(receitaFixaService.atualizar(eq(999L), any(ReceitaFixaRequestDTO.class)))
+                .thenThrow(new com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException("Receita fixa não encontrada: 999"));
 
         mockMvc.perform(put("/api/receitas-fixas/999")
                 .contentType(MediaType.APPLICATION_JSON)
@@ -208,7 +211,7 @@ public class ReceitaFixaControllerTest {
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(json))
                 .andExpect(status().isBadRequest())
-                .andExpect(jsonPath("$.message").value("Erro de integridade relacional. Verifique os vínculos informados."));
+                .andExpect(jsonPath("$.status").value(400));
     }
 
     @Test
@@ -235,6 +238,6 @@ public class ReceitaFixaControllerTest {
 
         mockMvc.perform(delete("/api/receitas-fixas/10"))
                 .andExpect(status().isBadRequest())
-                .andExpect(jsonPath("$.message").value("Não é possível excluir esta receita porque ela está em uso."));
+                .andExpect(jsonPath("$.status").value(400));
     }
 }
diff --git a/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ReceitaVariavelControllerTest.java b/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ReceitaVariavelControllerTest.java
index 6879df0..c3f24ba 100644
--- a/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ReceitaVariavelControllerTest.java
+++ b/backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ReceitaVariavelControllerTest.java
@@ -46,7 +46,9 @@ public class ReceitaVariavelControllerTest {
 
     @BeforeEach
     public void setup() {
-        mockMvc = MockMvcBuilders.standaloneSetup(receitaVariavelController).build();
+        mockMvc = MockMvcBuilders.standaloneSetup(receitaVariavelController)
+                .setControllerAdvice(new com.guilhermepagio.aureus.backend.exception.GlobalExceptionHandler())
+                .build();
     }
 
     @Test
@@ -132,7 +134,7 @@ public class ReceitaVariavelControllerTest {
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(json))
                 .andExpect(status().isBadRequest())
-                .andExpect(jsonPath("$.message").value("Erro de integridade relacional. Verifique os vínculos informados."));
+                .andExpect(jsonPath("$.status").value(400));
     }
 
     @Test
@@ -156,7 +158,7 @@ public class ReceitaVariavelControllerTest {
             conta, categoria, "Obs"
         );
 
-        when(receitaVariavelService.atualizar(eq(10L), any(ReceitaVariavelRequestDTO.class))).thenReturn(Optional.of(response));
+        when(receitaVariavelService.atualizar(eq(10L), any(ReceitaVariavelRequestDTO.class))).thenReturn(response);
 
         mockMvc.perform(put("/api/receitas-variaveis/10")
                 .contentType(MediaType.APPLICATION_JSON)
@@ -179,7 +181,8 @@ public class ReceitaVariavelControllerTest {
         }
         """;
 
-        when(receitaVariavelService.atualizar(eq(999L), any(ReceitaVariavelRequestDTO.class))).thenReturn(Optional.empty());
+        when(receitaVariavelService.atualizar(eq(999L), any(ReceitaVariavelRequestDTO.class)))
+                .thenThrow(new com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException("Receita variável não encontrada: 999"));
 
         mockMvc.perform(put("/api/receitas-variaveis/999")
                 .contentType(MediaType.APPLICATION_JSON)
@@ -227,7 +230,7 @@ public class ReceitaVariavelControllerTest {
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(json))
                 .andExpect(status().isBadRequest())
-                .andExpect(jsonPath("$.message").value("Erro de integridade relacional. Verifique os vínculos informados."));
+                .andExpect(jsonPath("$.status").value(400));
     }
 
     @Test
@@ -254,6 +257,6 @@ public class ReceitaVariavelControllerTest {
 
         mockMvc.perform(delete("/api/receitas-variaveis/10"))
                 .andExpect(status().isBadRequest())
-                .andExpect(jsonPath("$.message").value("Erro ao excluir o registro."));
+                .andExpect(jsonPath("$.status").value(400));
     }
 }
diff --git a/backend/src/test/java/com/guilhermepagio/aureus/backend/service/CategoriaServiceTest.java b/backend/src/test/java/com/guilhermepagio/aureus/backend/service/CategoriaServiceTest.java
index 61d3f30..46ddb36 100644
--- a/backend/src/test/java/com/guilhermepagio/aureus/backend/service/CategoriaServiceTest.java
+++ b/backend/src/test/java/com/guilhermepagio/aureus/backend/service/CategoriaServiceTest.java
@@ -91,22 +91,23 @@ public class CategoriaServiceTest {
         when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existente));
         when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));
 
-        Optional<CategoriaResponseDTO> response = categoriaService.atualizar(1L, dto);
+        CategoriaResponseDTO response = categoriaService.atualizar(1L, dto);
 
-        assertTrue(response.isPresent());
-        assertEquals(1L, response.get().id());
-        assertEquals("Supermercado", response.get().descricao());
-        assertEquals("Atualizada", response.get().observacoes());
+        assertNotNull(response);
+        assertEquals(1L, response.id());
+        assertEquals("Supermercado", response.descricao());
+        assertEquals("Atualizada", response.observacoes());
     }
 
     @Test
-    public void deveRetornarVazioAoAtualizarCategoriaInexistente() {
+    public void deveLancarExcecaoAoAtualizarCategoriaInexistente() {
         CategoriaRequestDTO dto = new CategoriaRequestDTO("Nova", "Obs");
         when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());
 
-        Optional<CategoriaResponseDTO> response = categoriaService.atualizar(999L, dto);
-
-        assertTrue(response.isEmpty());
+        org.junit.jupiter.api.Assertions.assertThrows(
+            com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException.class,
+            () -> categoriaService.atualizar(999L, dto)
+        );
     }
 
     @Test
diff --git a/backend/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java b/backend/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java
index 6d5ad28..1d92c04 100644
--- a/backend/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java
+++ b/backend/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java
@@ -419,4 +419,104 @@ public class ConsolidacaoServiceTest {
         assertTrue(dto.getDespesas().stream().noneMatch(d -> d.getContaId().equals(2L)));
         assertTrue(dto.getDespesas().stream().noneMatch(d -> d.getContaId().equals(3L)));
     }
+
+    @Test
+    void testMovimentacoesSemContaAgrupadasSobLinhaSinteticaSemConta() {
+        when(contaRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
+
+        // Receita Fixa sem conta
+        ReceitaFixa rf = new ReceitaFixa();
+        rf.setId(1L);
+        rf.setConta(null);
+        rf.setDataInicio(LocalDate.of(2024, 1, 1));
+        rf.setValor(new BigDecimal("250.00"));
+        when(receitaFixaRepository.findByUsuarioId("user1")).thenReturn(List.of(rf));
+
+        // Despesa Fixa sem conta
+        DespesaFixa df = new DespesaFixa();
+        df.setId(2L);
+        df.setConta(null);
+        df.setDataInicio(LocalDate.of(2024, 1, 1));
+        df.setValor(new BigDecimal("100.00"));
+        when(despesaFixaRepository.findByUsuarioId("user1")).thenReturn(List.of(df));
+
+        when(receitaVariavelRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
+        when(despesaVariavelRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
+
+        ConsolidacaoPorContaDTO dto = consolidacaoService.calcularConsolidacaoPorConta("user1", "2024-01");
+
+        // Receitas deve conter a linha "Sem Conta" com id -1
+        assertEquals(1, dto.getReceitas().size());
+        LinhaConsolidacaoDTO linhaRecSemConta = dto.getReceitas().get(0);
+        assertEquals(ConsolidacaoService.SEM_CONTA_ID, linhaRecSemConta.getContaId());
+        assertEquals("Sem Conta", linhaRecSemConta.getContaDescricao());
+        assertEquals(0, new BigDecimal("250.00").compareTo(linhaRecSemConta.getValoresMensais().get(0)));
+
+        // Despesas deve conter a linha "Sem Conta" com id -1
+        assertEquals(1, dto.getDespesas().size());
+        LinhaConsolidacaoDTO linhaDespSemConta = dto.getDespesas().get(0);
+        assertEquals(ConsolidacaoService.SEM_CONTA_ID, linhaDespSemConta.getContaId());
+        assertEquals("Sem Conta", linhaDespSemConta.getContaDescricao());
+        assertEquals(0, new BigDecimal("100.00").compareTo(linhaDespSemConta.getValoresMensais().get(0)));
+    }
+
+    @Test
+    void testSaldoHistoricoPreGradeComputaMovimentacoesSemConta() {
+        when(contaRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
+
+        // Receita Fixa retroativa sem conta: 2 meses antes de 2024-03 = 2 * 300.00 = 600.00
+        ReceitaFixa rf = new ReceitaFixa();
+        rf.setId(1L);
+        rf.setConta(null);
+        rf.setDataInicio(LocalDate.of(2024, 1, 1));
+        rf.setValor(new BigDecimal("300.00"));
+        when(receitaFixaRepository.findByUsuarioId("user1")).thenReturn(List.of(rf));
+
+        // Despesa Fixa retroativa sem conta: 2 meses antes de 2024-03 = 2 * 100.00 = 200.00
+        DespesaFixa df = new DespesaFixa();
+        df.setId(2L);
+        df.setConta(null);
+        df.setDataInicio(LocalDate.of(2024, 1, 1));
+        df.setValor(new BigDecimal("100.00"));
+        when(despesaFixaRepository.findByUsuarioId("user1")).thenReturn(List.of(df));
+
+        when(receitaVariavelRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
+        when(despesaVariavelRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
+
+        ConsolidacaoPorContaDTO dto = consolidacaoService.calcularConsolidacaoPorConta("user1", "2024-03");
+
+        // 600.00 - 200.00 = 400.00
+        assertEquals(0, new BigDecimal("400.00").compareTo(dto.getSaldoHistoricoPreGrade()));
+    }
+
+    @Test
+    void testConsistenciaTotalDespesasEntreContaECategoriaComLancamentoSemContaESemCategoria() {
+        when(contaRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
+        when(categoriaRepository.findByUsuarioIdOrderByDescricaoAsc("user1")).thenReturn(Collections.emptyList());
+
+        DespesaFixa df = new DespesaFixa();
+        df.setId(1L);
+        df.setConta(null);
+        df.setCategoria(null);
+        df.setDataInicio(LocalDate.of(2024, 1, 1));
+        df.setValor(new BigDecimal("150.00"));
+
+        when(despesaFixaRepository.findByUsuarioId("user1")).thenReturn(List.of(df));
+        when(despesaVariavelRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
+        when(receitaFixaRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
+        when(receitaVariavelRepository.findByUsuarioId("user1")).thenReturn(Collections.emptyList());
+
+        ConsolidacaoPorContaDTO porConta = consolidacaoService.calcularConsolidacaoPorConta("user1", "2024-01");
+        ConsolidacaoPorCategoriaDTO porCategoria = consolidacaoService.calcularConsolidacaoPorCategoria("user1", "2024-01");
+
+        // Ambas devem ter exatamente 1 linha ("Sem Conta" e "Sem Categoria") com os mesmos totais mensais
+        assertEquals(1, porConta.getDespesas().size());
+        assertEquals(1, porCategoria.getDespesas().size());
+
+        for (int i = 0; i < 24; i++) {
+            BigDecimal valorConta = porConta.getDespesas().get(0).getValoresMensais().get(i);
+            BigDecimal valorCategoria = porCategoria.getDespesas().get(0).getValoresMensais().get(i);
+            assertEquals(0, valorConta.compareTo(valorCategoria));
+        }
+    }
 }
\ No newline at end of file
diff --git a/backend/src/test/java/com/guilhermepagio/aureus/backend/service/ContaServiceTest.java b/backend/src/test/java/com/guilhermepagio/aureus/backend/service/ContaServiceTest.java
index 3b381cc..84ac98c 100644
--- a/backend/src/test/java/com/guilhermepagio/aureus/backend/service/ContaServiceTest.java
+++ b/backend/src/test/java/com/guilhermepagio/aureus/backend/service/ContaServiceTest.java
@@ -91,22 +91,23 @@ public class ContaServiceTest {
         when(contaRepository.findById(1L)).thenReturn(Optional.of(existente));
         when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> invocation.getArgument(0));
 
-        Optional<ContaResponseDTO> response = contaService.atualizar(1L, dto);
+        ContaResponseDTO response = contaService.atualizar(1L, dto);
 
-        assertTrue(response.isPresent());
-        assertEquals(1L, response.get().id());
-        assertEquals("Nubank PJ", response.get().descricao());
-        assertEquals("Atualizada", response.get().observacoes());
+        assertNotNull(response);
+        assertEquals(1L, response.id());
+        assertEquals("Nubank PJ", response.descricao());
+        assertEquals("Atualizada", response.observacoes());
     }
 
     @Test
-    public void deveRetornarVazioAoAtualizarContaInexistente() {
+    public void deveLancarExcecaoAoAtualizarContaInexistente() {
         ContaRequestDTO dto = new ContaRequestDTO("Nova", "Obs");
         when(contaRepository.findById(999L)).thenReturn(Optional.empty());
 
-        Optional<ContaResponseDTO> response = contaService.atualizar(999L, dto);
-
-        assertTrue(response.isEmpty());
+        org.junit.jupiter.api.Assertions.assertThrows(
+            com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException.class,
+            () -> contaService.atualizar(999L, dto)
+        );
     }
 
     @Test
diff --git a/backend/src/test/java/com/guilhermepagio/aureus/backend/service/DespesaFixaServiceTest.java b/backend/src/test/java/com/guilhermepagio/aureus/backend/service/DespesaFixaServiceTest.java
index 4f4025a..4cafb85 100644
--- a/backend/src/test/java/com/guilhermepagio/aureus/backend/service/DespesaFixaServiceTest.java
+++ b/backend/src/test/java/com/guilhermepagio/aureus/backend/service/DespesaFixaServiceTest.java
@@ -95,7 +95,9 @@ public class DespesaFixaServiceTest {
         Categoria categoria = new Categoria(2L, "Serviços", "Internet");
         DespesaFixa salva = new DespesaFixa(100L, "Internet", new BigDecimal("150.00"), conta, categoria, "Vivo Fibra", LocalDate.of(2024, 1, 1));
 
+        when(contaRepository.findOwnerUsuarioId(1L)).thenReturn(Optional.of("user1"));
         when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
+        when(categoriaRepository.findOwnerUsuarioId(2L)).thenReturn(Optional.of("user1"));
         when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
         when(despesaFixaRepository.saveAndFlush(any(DespesaFixa.class))).thenAnswer(invocation -> {
             DespesaFixa d = invocation.getArgument(0);
@@ -128,9 +130,9 @@ public class DespesaFixaServiceTest {
             LocalDate.of(2024, 1, 1)
         );
 
-        when(contaRepository.findById(999L)).thenReturn(Optional.empty());
+        when(contaRepository.findOwnerUsuarioId(999L)).thenReturn(Optional.empty());
 
-        assertThrows(DataIntegrityViolationException.class, () -> despesaFixaService.criar(dto));
+        assertThrows(com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException.class, () -> despesaFixaService.criar(dto));
     }
 
     @Test
@@ -145,10 +147,11 @@ public class DespesaFixaServiceTest {
         );
 
         Conta conta = new Conta(1L, "Nubank", "Principal");
+        when(contaRepository.findOwnerUsuarioId(1L)).thenReturn(Optional.of("user1"));
         when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
-        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());
+        when(categoriaRepository.findOwnerUsuarioId(999L)).thenReturn(Optional.empty());
 
-        assertThrows(DataIntegrityViolationException.class, () -> despesaFixaService.criar(dto));
+        assertThrows(com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException.class, () -> despesaFixaService.criar(dto));
     }
 
     @Test
@@ -169,23 +172,25 @@ public class DespesaFixaServiceTest {
         );
 
         when(despesaFixaRepository.findById(100L)).thenReturn(Optional.of(existente));
+        when(contaRepository.findOwnerUsuarioId(3L)).thenReturn(Optional.of("user1"));
         when(contaRepository.findById(3L)).thenReturn(Optional.of(contaNova));
+        when(categoriaRepository.findOwnerUsuarioId(4L)).thenReturn(Optional.of("user1"));
         when(categoriaRepository.findById(4L)).thenReturn(Optional.of(categoriaNova));
         when(despesaFixaRepository.saveAndFlush(any(DespesaFixa.class))).thenAnswer(invocation -> invocation.getArgument(0));
 
-        Optional<DespesaFixaResponseDTO> response = despesaFixaService.atualizar(100L, dto);
+        DespesaFixaResponseDTO response = despesaFixaService.atualizar(100L, dto);
 
-        assertTrue(response.isPresent());
-        assertEquals("Internet 500MB", response.get().descricao());
-        assertEquals(new BigDecimal("170.00"), response.get().valor());
-        assertEquals(3L, response.get().conta().id());
-        assertEquals(4L, response.get().categoria().id());
-        assertEquals("Upgrade", response.get().observacoes());
-        assertEquals(LocalDate.of(2024, 2, 1), response.get().dataInicio());
+        assertNotNull(response);
+        assertEquals("Internet 500MB", response.descricao());
+        assertEquals(new BigDecimal("170.00"), response.valor());
+        assertEquals(3L, response.conta().id());
+        assertEquals(4L, response.categoria().id());
+        assertEquals("Upgrade", response.observacoes());
+        assertEquals(LocalDate.of(2024, 2, 1), response.dataInicio());
     }
 
     @Test
-    public void deveRetornarVazioAoAtualizarDespesaFixaInexistente() {
+    public void deveLancarExcecaoAoAtualizarDespesaFixaInexistente() {
         DespesaFixaRequestDTO dto = new DespesaFixaRequestDTO(
             "Inexistente",
             new BigDecimal("100.00"),
@@ -197,9 +202,7 @@ public class DespesaFixaServiceTest {
 
         when(despesaFixaRepository.findById(999L)).thenReturn(Optional.empty());
 
-        Optional<DespesaFixaResponseDTO> response = despesaFixaService.atualizar(999L, dto);
-
-        assertTrue(response.isEmpty());
+        assertThrows(com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException.class, () -> despesaFixaService.atualizar(999L, dto));
     }
 
     @Test
@@ -215,9 +218,9 @@ public class DespesaFixaServiceTest {
         );
 
         when(despesaFixaRepository.findById(100L)).thenReturn(Optional.of(existente));
-        when(contaRepository.findById(999L)).thenReturn(Optional.empty());
+        when(contaRepository.findOwnerUsuarioId(999L)).thenReturn(Optional.empty());
 
-        assertThrows(DataIntegrityViolationException.class, () -> despesaFixaService.atualizar(100L, dto));
+        assertThrows(com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException.class, () -> despesaFixaService.atualizar(100L, dto));
     }
 
     @Test
diff --git a/backend/src/test/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelServiceTest.java b/backend/src/test/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelServiceTest.java
index 9ad08db..4821676 100644
--- a/backend/src/test/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelServiceTest.java
+++ b/backend/src/test/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelServiceTest.java
@@ -21,6 +21,7 @@ import org.mockito.Mock;
 import org.mockito.junit.jupiter.MockitoExtension;
 import org.springframework.dao.DataIntegrityViolationException;
 
+import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
 import com.guilhermepagio.aureus.backend.domain.Categoria;
 import com.guilhermepagio.aureus.backend.domain.Conta;
 import com.guilhermepagio.aureus.backend.domain.DespesaVariavel;
@@ -107,7 +108,9 @@ public class DespesaVariavelServiceTest {
         Conta conta = new Conta(1L, "Nubank", "Principal");
         Categoria categoria = new Categoria(2L, "Móveis", "Casa");
 
+        when(contaRepository.findOwnerUsuarioId(1L)).thenReturn(Optional.of("user1"));
         when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
+        when(categoriaRepository.findOwnerUsuarioId(2L)).thenReturn(Optional.of("user1"));
         when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
         when(despesaVariavelRepository.saveAndFlush(any(DespesaVariavel.class))).thenAnswer(invocation -> {
             DespesaVariavel d = invocation.getArgument(0);
@@ -149,7 +152,9 @@ public class DespesaVariavelServiceTest {
         Conta conta = new Conta(1L, "Nubank", "Principal");
         Categoria categoria = new Categoria(2L, "Alimentação", "Refeição");
 
+        when(contaRepository.findOwnerUsuarioId(1L)).thenReturn(Optional.of("user1"));
         when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
+        when(categoriaRepository.findOwnerUsuarioId(2L)).thenReturn(Optional.of("user1"));
         when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
         when(despesaVariavelRepository.saveAndFlush(any(DespesaVariavel.class))).thenAnswer(invocation -> {
             DespesaVariavel d = invocation.getArgument(0);
@@ -171,9 +176,9 @@ public class DespesaVariavelServiceTest {
             new IdReferenceDTO(999L), new IdReferenceDTO(2L), null
         );
 
-        when(contaRepository.findById(999L)).thenReturn(Optional.empty());
+        when(contaRepository.findOwnerUsuarioId(999L)).thenReturn(Optional.empty());
 
-        assertThrows(DataIntegrityViolationException.class, () -> despesaVariavelService.criar(dto));
+        assertThrows(ResourceNotFoundException.class, () -> despesaVariavelService.criar(dto));
     }
 
     @Test
@@ -184,10 +189,11 @@ public class DespesaVariavelServiceTest {
         );
 
         Conta conta = new Conta(1L, "Nubank", "Principal");
+        when(contaRepository.findOwnerUsuarioId(1L)).thenReturn(Optional.of("user1"));
         when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
-        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());
+        when(categoriaRepository.findOwnerUsuarioId(999L)).thenReturn(Optional.empty());
 
-        assertThrows(DataIntegrityViolationException.class, () -> despesaVariavelService.criar(dto));
+        assertThrows(ResourceNotFoundException.class, () -> despesaVariavelService.criar(dto));
     }
 
     @Test
@@ -215,26 +221,28 @@ public class DespesaVariavelServiceTest {
         );
 
         when(despesaVariavelRepository.findById(100L)).thenReturn(Optional.of(existente));
+        when(contaRepository.findOwnerUsuarioId(3L)).thenReturn(Optional.of("user1"));
         when(contaRepository.findById(3L)).thenReturn(Optional.of(contaNova));
+        when(categoriaRepository.findOwnerUsuarioId(4L)).thenReturn(Optional.of("user1"));
         when(categoriaRepository.findById(4L)).thenReturn(Optional.of(categoriaNova));
         when(despesaVariavelRepository.saveAndFlush(any(DespesaVariavel.class))).thenAnswer(invocation -> invocation.getArgument(0));
 
-        Optional<DespesaVariavelResponseDTO> response = despesaVariavelService.atualizar(100L, dto);
-
-        assertTrue(response.isPresent());
-        assertEquals("Notebook Gamer", response.get().descricao());
-        assertEquals(LocalDate.of(2024, 2, 1), response.get().dataInicio());
-        assertEquals(LocalDate.of(2024, 5, 1), response.get().dataFim()); // 2024-02-01 + (4 - 1) meses = 2024-05-01
-        assertEquals(3L, response.get().conta().id());
-        assertEquals(4L, response.get().categoria().id());
-        assertEquals(new BigDecimal("1200.00"), response.get().valorParcela());
-        assertEquals(4, response.get().quantidadeParcelas());
-        assertEquals("Kabum Tech", response.get().localCompra());
-        assertEquals("Reparcelado", response.get().observacoes());
+        DespesaVariavelResponseDTO response = despesaVariavelService.atualizar(100L, dto);
+
+        assertNotNull(response);
+        assertEquals("Notebook Gamer", response.descricao());
+        assertEquals(LocalDate.of(2024, 2, 1), response.dataInicio());
+        assertEquals(LocalDate.of(2024, 5, 1), response.dataFim()); // 2024-02-01 + (4 - 1) meses = 2024-05-01
+        assertEquals(3L, response.conta().id());
+        assertEquals(4L, response.categoria().id());
+        assertEquals(new BigDecimal("1200.00"), response.valorParcela());
+        assertEquals(4, response.quantidadeParcelas());
+        assertEquals("Kabum Tech", response.localCompra());
+        assertEquals("Reparcelado", response.observacoes());
     }
 
     @Test
-    public void deveRetornarVazioAoAtualizarDespesaVariavelInexistente() {
+    public void deveLancarExcecaoAoAtualizarDespesaVariavelInexistente() {
         DespesaVariavelRequestDTO dto = new DespesaVariavelRequestDTO(
             "Compra", null, null, new BigDecimal("100.00"), 1, LocalDate.of(2024, 1, 1),
             new IdReferenceDTO(1L), new IdReferenceDTO(2L), null
@@ -242,9 +250,7 @@ public class DespesaVariavelServiceTest {
 
         when(despesaVariavelRepository.findById(999L)).thenReturn(Optional.empty());
 
-        Optional<DespesaVariavelResponseDTO> response = despesaVariavelService.atualizar(999L, dto);
-
-        assertTrue(response.isEmpty());
+        assertThrows(ResourceNotFoundException.class, () -> despesaVariavelService.atualizar(999L, dto));
     }
 
     @Test
@@ -261,9 +267,9 @@ public class DespesaVariavelServiceTest {
         );
 
         when(despesaVariavelRepository.findById(100L)).thenReturn(Optional.of(existente));
-        when(contaRepository.findById(999L)).thenReturn(Optional.empty());
+        when(contaRepository.findOwnerUsuarioId(999L)).thenReturn(Optional.empty());
 
-        assertThrows(DataIntegrityViolationException.class, () -> despesaVariavelService.atualizar(100L, dto));
+        assertThrows(ResourceNotFoundException.class, () -> despesaVariavelService.atualizar(100L, dto));
     }
 
     @Test
diff --git a/backend/src/test/java/com/guilhermepagio/aureus/backend/service/ReceitaFixaServiceTest.java b/backend/src/test/java/com/guilhermepagio/aureus/backend/service/ReceitaFixaServiceTest.java
index 4a7875c..d8df271 100644
--- a/backend/src/test/java/com/guilhermepagio/aureus/backend/service/ReceitaFixaServiceTest.java
+++ b/backend/src/test/java/com/guilhermepagio/aureus/backend/service/ReceitaFixaServiceTest.java
@@ -21,6 +21,7 @@ import org.mockito.Mock;
 import org.mockito.junit.jupiter.MockitoExtension;
 import org.springframework.dao.DataIntegrityViolationException;
 
+import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
 import com.guilhermepagio.aureus.backend.domain.Categoria;
 import com.guilhermepagio.aureus.backend.domain.Conta;
 import com.guilhermepagio.aureus.backend.domain.ReceitaFixa;
@@ -95,7 +96,9 @@ public class ReceitaFixaServiceTest {
         Categoria categoria = new Categoria(2L, "Trabalho", "Salário");
         ReceitaFixa salva = new ReceitaFixa(100L, "Salário", new BigDecimal("5000.00"), conta, categoria, "Mensal", LocalDate.of(2024, 1, 1));
 
+        when(contaRepository.findOwnerUsuarioId(1L)).thenReturn(Optional.of("user1"));
         when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
+        when(categoriaRepository.findOwnerUsuarioId(2L)).thenReturn(Optional.of("user1"));
         when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
         when(receitaFixaRepository.saveAndFlush(any(ReceitaFixa.class))).thenAnswer(invocation -> {
             ReceitaFixa r = invocation.getArgument(0);
@@ -128,9 +131,9 @@ public class ReceitaFixaServiceTest {
             LocalDate.of(2024, 1, 1)
         );
 
-        when(contaRepository.findById(999L)).thenReturn(Optional.empty());
+        when(contaRepository.findOwnerUsuarioId(999L)).thenReturn(Optional.empty());
 
-        assertThrows(DataIntegrityViolationException.class, () -> receitaFixaService.criar(dto));
+        assertThrows(ResourceNotFoundException.class, () -> receitaFixaService.criar(dto));
     }
 
     @Test
@@ -145,10 +148,11 @@ public class ReceitaFixaServiceTest {
         );
 
         Conta conta = new Conta(1L, "Nubank", "Principal");
+        when(contaRepository.findOwnerUsuarioId(1L)).thenReturn(Optional.of("user1"));
         when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
-        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());
+        when(categoriaRepository.findOwnerUsuarioId(999L)).thenReturn(Optional.empty());
 
-        assertThrows(DataIntegrityViolationException.class, () -> receitaFixaService.criar(dto));
+        assertThrows(ResourceNotFoundException.class, () -> receitaFixaService.criar(dto));
     }
 
     @Test
@@ -169,23 +173,25 @@ public class ReceitaFixaServiceTest {
         );
 
         when(receitaFixaRepository.findById(100L)).thenReturn(Optional.of(existente));
+        when(contaRepository.findOwnerUsuarioId(3L)).thenReturn(Optional.of("user1"));
         when(contaRepository.findById(3L)).thenReturn(Optional.of(contaNova));
+        when(categoriaRepository.findOwnerUsuarioId(4L)).thenReturn(Optional.of("user1"));
         when(categoriaRepository.findById(4L)).thenReturn(Optional.of(categoriaNova));
         when(receitaFixaRepository.saveAndFlush(any(ReceitaFixa.class))).thenAnswer(invocation -> invocation.getArgument(0));
 
-        Optional<ReceitaFixaResponseDTO> response = receitaFixaService.atualizar(100L, dto);
+        ReceitaFixaResponseDTO response = receitaFixaService.atualizar(100L, dto);
 
-        assertTrue(response.isPresent());
-        assertEquals("Consultoria", response.get().descricao());
-        assertEquals(new BigDecimal("6000.00"), response.get().valor());
-        assertEquals(3L, response.get().conta().id());
-        assertEquals(4L, response.get().categoria().id());
-        assertEquals("Novo contrato", response.get().observacoes());
-        assertEquals(LocalDate.of(2024, 2, 1), response.get().dataInicio());
+        assertNotNull(response);
+        assertEquals("Consultoria", response.descricao());
+        assertEquals(new BigDecimal("6000.00"), response.valor());
+        assertEquals(3L, response.conta().id());
+        assertEquals(4L, response.categoria().id());
+        assertEquals("Novo contrato", response.observacoes());
+        assertEquals(LocalDate.of(2024, 2, 1), response.dataInicio());
     }
 
     @Test
-    public void deveRetornarVazioAoAtualizarReceitaFixaInexistente() {
+    public void deveLancarExcecaoAoAtualizarReceitaFixaInexistente() {
         ReceitaFixaRequestDTO dto = new ReceitaFixaRequestDTO(
             "Inexistente",
             new BigDecimal("100.00"),
@@ -197,9 +203,7 @@ public class ReceitaFixaServiceTest {
 
         when(receitaFixaRepository.findById(999L)).thenReturn(Optional.empty());
 
-        Optional<ReceitaFixaResponseDTO> response = receitaFixaService.atualizar(999L, dto);
-
-        assertTrue(response.isEmpty());
+        assertThrows(ResourceNotFoundException.class, () -> receitaFixaService.atualizar(999L, dto));
     }
 
     @Test
@@ -215,9 +219,9 @@ public class ReceitaFixaServiceTest {
         );
 
         when(receitaFixaRepository.findById(100L)).thenReturn(Optional.of(existente));
-        when(contaRepository.findById(999L)).thenReturn(Optional.empty());
+        when(contaRepository.findOwnerUsuarioId(999L)).thenReturn(Optional.empty());
 
-        assertThrows(DataIntegrityViolationException.class, () -> receitaFixaService.atualizar(100L, dto));
+        assertThrows(ResourceNotFoundException.class, () -> receitaFixaService.atualizar(100L, dto));
     }
 
     @Test
diff --git a/backend/src/test/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelServiceTest.java b/backend/src/test/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelServiceTest.java
index 7a98bf1..642cdd4 100644
--- a/backend/src/test/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelServiceTest.java
+++ b/backend/src/test/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelServiceTest.java
@@ -21,6 +21,7 @@ import org.mockito.Mock;
 import org.mockito.junit.jupiter.MockitoExtension;
 import org.springframework.dao.DataIntegrityViolationException;
 
+import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
 import com.guilhermepagio.aureus.backend.domain.Categoria;
 import com.guilhermepagio.aureus.backend.domain.Conta;
 import com.guilhermepagio.aureus.backend.domain.ReceitaVariavel;
@@ -105,7 +106,9 @@ public class ReceitaVariavelServiceTest {
         Conta conta = new Conta(1L, "Nubank", "Principal");
         Categoria categoria = new Categoria(2L, "Vendas", "Bens");
 
+        when(contaRepository.findOwnerUsuarioId(1L)).thenReturn(Optional.of("user1"));
         when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
+        when(categoriaRepository.findOwnerUsuarioId(2L)).thenReturn(Optional.of("user1"));
         when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
         when(receitaVariavelRepository.saveAndFlush(any(ReceitaVariavel.class))).thenAnswer(invocation -> {
             ReceitaVariavel r = invocation.getArgument(0);
@@ -142,7 +145,9 @@ public class ReceitaVariavelServiceTest {
         Conta conta = new Conta(1L, "Nubank", "Principal");
         Categoria categoria = new Categoria(2L, "Renda", "Extra");
 
+        when(contaRepository.findOwnerUsuarioId(1L)).thenReturn(Optional.of("user1"));
         when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
+        when(categoriaRepository.findOwnerUsuarioId(2L)).thenReturn(Optional.of("user1"));
         when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
         when(receitaVariavelRepository.saveAndFlush(any(ReceitaVariavel.class))).thenAnswer(invocation -> {
             ReceitaVariavel r = invocation.getArgument(0);
@@ -164,9 +169,9 @@ public class ReceitaVariavelServiceTest {
             new IdReferenceDTO(999L), new IdReferenceDTO(2L), null
         );
 
-        when(contaRepository.findById(999L)).thenReturn(Optional.empty());
+        when(contaRepository.findOwnerUsuarioId(999L)).thenReturn(Optional.empty());
 
-        assertThrows(DataIntegrityViolationException.class, () -> receitaVariavelService.criar(dto));
+        assertThrows(ResourceNotFoundException.class, () -> receitaVariavelService.criar(dto));
     }
 
     @Test
@@ -177,10 +182,11 @@ public class ReceitaVariavelServiceTest {
         );
 
         Conta conta = new Conta(1L, "Nubank", "Principal");
+        when(contaRepository.findOwnerUsuarioId(1L)).thenReturn(Optional.of("user1"));
         when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
-        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());
+        when(categoriaRepository.findOwnerUsuarioId(999L)).thenReturn(Optional.empty());
 
-        assertThrows(DataIntegrityViolationException.class, () -> receitaVariavelService.criar(dto));
+        assertThrows(ResourceNotFoundException.class, () -> receitaVariavelService.criar(dto));
     }
 
     @Test
@@ -206,25 +212,27 @@ public class ReceitaVariavelServiceTest {
         );
 
         when(receitaVariavelRepository.findById(50L)).thenReturn(Optional.of(existente));
+        when(contaRepository.findOwnerUsuarioId(3L)).thenReturn(Optional.of("user1"));
         when(contaRepository.findById(3L)).thenReturn(Optional.of(contaNova));
+        when(categoriaRepository.findOwnerUsuarioId(4L)).thenReturn(Optional.of("user1"));
         when(categoriaRepository.findById(4L)).thenReturn(Optional.of(categoriaNova));
         when(receitaVariavelRepository.saveAndFlush(any(ReceitaVariavel.class))).thenAnswer(invocation -> invocation.getArgument(0));
 
-        Optional<ReceitaVariavelResponseDTO> response = receitaVariavelService.atualizar(50L, dto);
-
-        assertTrue(response.isPresent());
-        assertEquals("Projeto Renovado", response.get().descricao());
-        assertEquals(LocalDate.of(2024, 3, 1), response.get().dataInicio());
-        assertEquals(LocalDate.of(2024, 6, 1), response.get().dataFim()); // 2024-03-01 + (4 - 1) = 2024-06-01
-        assertEquals(3L, response.get().conta().id());
-        assertEquals(4L, response.get().categoria().id());
-        assertEquals(new BigDecimal("1500.00"), response.get().valorParcela());
-        assertEquals(4, response.get().quantidadeParcelas());
-        assertEquals("Aditivo contratual", response.get().observacoes());
+        ReceitaVariavelResponseDTO response = receitaVariavelService.atualizar(50L, dto);
+
+        assertNotNull(response);
+        assertEquals("Projeto Renovado", response.descricao());
+        assertEquals(LocalDate.of(2024, 3, 1), response.dataInicio());
+        assertEquals(LocalDate.of(2024, 6, 1), response.dataFim()); // 2024-03-01 + (4 - 1) = 2024-06-01
+        assertEquals(3L, response.conta().id());
+        assertEquals(4L, response.categoria().id());
+        assertEquals(new BigDecimal("1500.00"), response.valorParcela());
+        assertEquals(4, response.quantidadeParcelas());
+        assertEquals("Aditivo contratual", response.observacoes());
     }
 
     @Test
-    public void deveRetornarVazioAoAtualizarReceitaVariavelInexistente() {
+    public void deveLancarExcecaoAoAtualizarReceitaVariavelInexistente() {
         ReceitaVariavelRequestDTO dto = new ReceitaVariavelRequestDTO(
             "Renda", new BigDecimal("100.00"), 1, LocalDate.of(2024, 1, 1),
             new IdReferenceDTO(1L), new IdReferenceDTO(2L), null
@@ -232,9 +240,7 @@ public class ReceitaVariavelServiceTest {
 
         when(receitaVariavelRepository.findById(999L)).thenReturn(Optional.empty());
 
-        Optional<ReceitaVariavelResponseDTO> response = receitaVariavelService.atualizar(999L, dto);
-
-        assertTrue(response.isEmpty());
+        assertThrows(ResourceNotFoundException.class, () -> receitaVariavelService.atualizar(999L, dto));
     }
 
     @Test
@@ -251,9 +257,9 @@ public class ReceitaVariavelServiceTest {
         );
 
         when(receitaVariavelRepository.findById(100L)).thenReturn(Optional.of(existente));
-        when(contaRepository.findById(999L)).thenReturn(Optional.empty());
+        when(contaRepository.findOwnerUsuarioId(999L)).thenReturn(Optional.empty());
 
-        assertThrows(DataIntegrityViolationException.class, () -> receitaVariavelService.atualizar(100L, dto));
+        assertThrows(ResourceNotFoundException.class, () -> receitaVariavelService.atualizar(100L, dto));
     }
 
     @Test


--- /dev/null
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/exception/ApiErrorResponse.java
@@ -0,0 +1,21 @@
package com.guilhermepagio.aureus.backend.exception;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ApiErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    Map<String, String> fieldErrors,
    List<ValidationErrorItem> errors
) {
    public ApiErrorResponse(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path, null, null);
    }

    public record ValidationErrorItem(String field, String message, String defaultMessage) {}
}


--- /dev/null
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/exception/ResourceNotFoundException.java
@@ -0,0 +1,8 @@
package com.guilhermepagio.aureus.backend.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}


--- /dev/null
+++ b/backend/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java
@@ -0,0 +1,147 @@
package com.guilhermepagio.aureus.backend.exception;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        List<ApiErrorResponse.ValidationErrorItem> errors = new ArrayList<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
            errors.add(new ApiErrorResponse.ValidationErrorItem(
                fieldError.getField(),
                fieldError.getDefaultMessage(),
                fieldError.getDefaultMessage()
            ));
        }

        String path = request != null ? request.getRequestURI() : "";
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Erro de validação nos campos informados",
            path,
            fieldErrors,
            errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violação de integridade relacional: {}", ex.getMessage());
        String path = request != null ? request.getRequestURI() : "";
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Não é possível realizar a operação devido a vínculos ativos ou restrições de integridade.",
            path,
            null,
            null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            path,
            null,
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.FORBIDDEN.value(),
            HttpStatus.FORBIDDEN.getReasonPhrase(),
            ex.getMessage(),
            path,
            null,
            null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            path,
            null,
            null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Corpo da requisição inválido ou mal formatado.",
            path,
            null,
            null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Erro interno inesperado no servidor: ", ex);
        String path = request != null ? request.getRequestURI() : "";
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "Ocorreu um erro interno inesperado no servidor.",
            path,
            null,
            null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}


--- /dev/null
+++ b/backend/src/main/resources/db/migration/V1__create_performance_indexes.sql
@@ -0,0 +1,5 @@
CREATE INDEX IF NOT EXISTS idx_usuarios_google_subject_id ON usuarios (google_subject_id);
CREATE INDEX IF NOT EXISTS idx_despesas_fixas_data_inicio ON despesas_fixas (data_inicio);
CREATE INDEX IF NOT EXISTS idx_receitas_fixas_data_inicio ON receitas_fixas (data_inicio);
CREATE INDEX IF NOT EXISTS idx_despesas_variaveis_datas ON despesas_variaveis (data_inicio, data_fim);
CREATE INDEX IF NOT EXISTS idx_receitas_variaveis_datas ON receitas_variaveis (data_inicio, data_fim);


--- /dev/null
+++ b/backend/src/test/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandlerTest.java
@@ -0,0 +1,156 @@
package com.guilhermepagio.aureus.backend.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    public void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
    }

    @Test
    public void deveTratarMethodArgumentNotValidExceptionComStatus400EItensDeValidacao() throws NoSuchMethodException {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "testObject");
        bindingResult.addError(new FieldError("testObject", "descricao", "A descrição é obrigatória"));
        bindingResult.addError(new FieldError("testObject", "valor", "O valor deve ser maior que zero"));

        MethodParameter parameter = new MethodParameter(
            this.getClass().getDeclaredMethod("setUp"), -1
        );
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleMethodArgumentNotValid(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
        assertEquals("/api/test", body.path());
        assertNotNull(body.timestamp());

        // Field errors map
        assertNotNull(body.fieldErrors());
        assertEquals(2, body.fieldErrors().size());
        assertEquals("A descrição é obrigatória", body.fieldErrors().get("descricao"));
        assertEquals("O valor deve ser maior que zero", body.fieldErrors().get("valor"));

        // Errors list (compatible with frontend errorData.errors[0].defaultMessage)
        assertNotNull(body.errors());
        assertEquals(2, body.errors().size());
        assertEquals("descricao", body.errors().get(0).field());
        assertEquals("A descrição é obrigatória", body.errors().get(0).defaultMessage());
        assertEquals("A descrição é obrigatória", body.errors().get(0).message());
    }

    @Test
    public void deveTratarDataIntegrityViolationExceptionComStatus400ESemVazarDetalhesInternos() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
            "ERROR: update or delete on table \"contas\" violates foreign key constraint \"fk_despesas_conta\" on table \"despesas_fixas\""
        );

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
        assertEquals("/api/test", body.path());
        assertFalse(body.message().contains("violates foreign key constraint"));
        assertFalse(body.message().contains("fk_despesas_conta"));
        assertTrue(body.message().contains("vínculos ativos") || body.message().contains("integridade"));
    }

    @Test
    public void deveTratarResourceNotFoundExceptionComStatus404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Conta não encontrada: 999");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleResourceNotFound(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.status());
        assertEquals("Not Found", body.error());
        assertEquals("Conta não encontrada: 999", body.message());
        assertEquals("/api/test", body.path());
    }

    @Test
    public void deveTratarAccessDeniedExceptionComStatus403() {
        AccessDeniedException ex = new AccessDeniedException("Acesso negado: a conta informada não pertence ao usuário autenticado");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleAccessDenied(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(403, body.status());
        assertEquals("Forbidden", body.error());
        assertEquals("Acesso negado: a conta informada não pertence ao usuário autenticado", body.message());
        assertEquals("/api/test", body.path());
    }

    @Test
    public void deveTratarIllegalArgumentExceptionComStatus400() {
        IllegalArgumentException ex = new IllegalArgumentException("usuarioId não pode ser nulo");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleIllegalArgument(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
        assertEquals("usuarioId não pode ser nulo", body.message());
        assertEquals("/api/test", body.path());
    }

    @Test
    public void deveTratarExceptionGenericaComStatus500ESemExporStacktrace() {
        NullPointerException ex = new NullPointerException("Null reference at internal layer");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.status());
        assertEquals("Internal Server Error", body.error());
        assertFalse(body.message().contains("Null reference"));
        assertFalse(body.message().contains("NullPointerException"));
        assertEquals("Ocorreu um erro interno inesperado no servidor.", body.message());
        assertEquals("/api/test", body.path());
    }
}


--- /dev/null
+++ b/backend/src/test/java/com/guilhermepagio/aureus/backend/service/TenantValidationTest.java
@@ -0,0 +1,284 @@
package com.guilhermepagio.aureus.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.DespesaFixa;
import com.guilhermepagio.aureus.backend.domain.DespesaVariavel;
import com.guilhermepagio.aureus.backend.domain.ReceitaFixa;
import com.guilhermepagio.aureus.backend.domain.ReceitaVariavel;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.IdReferenceDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaFixaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaVariavelRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaFixaRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaVariavelRepository;
import com.guilhermepagio.aureus.backend.security.TenantContext;

@ExtendWith(MockitoExtension.class)
public class TenantValidationTest {

    private static final String TENANT_CORRENTE = "usuario-autenticado-123";
    private static final String OUTRO_TENANT = "outro-usuario-456";

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private DespesaFixaRepository despesaFixaRepository;

    @Mock
    private ReceitaFixaRepository receitaFixaRepository;

    @Mock
    private DespesaVariavelRepository despesaVariavelRepository;

    @Mock
    private ReceitaVariavelRepository receitaVariavelRepository;

    @InjectMocks
    private DespesaFixaService despesaFixaService;

    @InjectMocks
    private ReceitaFixaService receitaFixaService;

    @InjectMocks
    private DespesaVariavelService despesaVariavelService;

    @InjectMocks
    private ReceitaVariavelService receitaVariavelService;

    @BeforeEach
    public void setUp() {
        TenantContext.setTenantId(TENANT_CORRENTE);
    }

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
    }

    // --- DespesaFixa Tenant Validation ---

    @Test
    public void despesaFixaDeveNegarAcessoQuandoContaPertenceAOutroTenant() {
        DespesaFixaRequestDTO dto = new DespesaFixaRequestDTO(
            "Aluguel", new BigDecimal("1200.00"),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> despesaFixaService.criar(dto));
        assertEquals("Acesso negado: a conta informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void despesaFixaDeveNegarAcessoQuandoCategoriaPertenceAOutroTenant() {
        DespesaFixaRequestDTO dto = new DespesaFixaRequestDTO(
            "Aluguel", new BigDecimal("1200.00"),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        Conta conta = new Conta(10L, "Nubank", "Obs");
        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(TENANT_CORRENTE));
        when(contaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findOwnerUsuarioId(20L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> despesaFixaService.criar(dto));
        assertEquals("Acesso negado: a categoria informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void despesaFixaDeveLancar404QuandoContaNaoExiste() {
        DespesaFixaRequestDTO dto = new DespesaFixaRequestDTO(
            "Aluguel", new BigDecimal("1200.00"),
            new IdReferenceDTO(999L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        when(contaRepository.findOwnerUsuarioId(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> despesaFixaService.criar(dto));
        assertEquals("Conta não encontrada: 999", ex.getMessage());
    }

    @Test
    public void despesaFixaDeveLancar404AoAtualizarIdInexistente() {
        DespesaFixaRequestDTO dto = new DespesaFixaRequestDTO(
            "Aluguel", new BigDecimal("1200.00"),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        when(despesaFixaRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> despesaFixaService.atualizar(999L, dto));
        assertEquals("Despesa fixa não encontrada: 999", ex.getMessage());
    }

    // --- ReceitaFixa Tenant Validation ---

    @Test
    public void receitaFixaDeveNegarAcessoQuandoContaPertenceAOutroTenant() {
        ReceitaFixaRequestDTO dto = new ReceitaFixaRequestDTO(
            "Salário", new BigDecimal("5000.00"),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> receitaFixaService.criar(dto));
        assertEquals("Acesso negado: a conta informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void receitaFixaDeveNegarAcessoQuandoCategoriaPertenceAOutroTenant() {
        ReceitaFixaRequestDTO dto = new ReceitaFixaRequestDTO(
            "Salário", new BigDecimal("5000.00"),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        Conta conta = new Conta(10L, "Nubank", "Obs");
        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(TENANT_CORRENTE));
        when(contaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findOwnerUsuarioId(20L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> receitaFixaService.criar(dto));
        assertEquals("Acesso negado: a categoria informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void receitaFixaDeveLancar404AoAtualizarIdInexistente() {
        ReceitaFixaRequestDTO dto = new ReceitaFixaRequestDTO(
            "Salário", new BigDecimal("5000.00"),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        when(receitaFixaRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> receitaFixaService.atualizar(999L, dto));
        assertEquals("Receita fixa não encontrada: 999", ex.getMessage());
    }

    // --- DespesaVariavel Tenant Validation ---

    @Test
    public void despesaVariavelDeveNegarAcessoQuandoContaPertenceAOutroTenant() {
        DespesaVariavelRequestDTO dto = new DespesaVariavelRequestDTO(
            "Celular", "Loja X", LocalDate.of(2024, 1, 1),
            new BigDecimal("200.00"), 10, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs"
        );

        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> despesaVariavelService.criar(dto));
        assertEquals("Acesso negado: a conta informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void despesaVariavelDeveNegarAcessoQuandoCategoriaPertenceAOutroTenant() {
        DespesaVariavelRequestDTO dto = new DespesaVariavelRequestDTO(
            "Celular", "Loja X", LocalDate.of(2024, 1, 1),
            new BigDecimal("200.00"), 10, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs"
        );

        Conta conta = new Conta(10L, "Nubank", "Obs");
        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(TENANT_CORRENTE));
        when(contaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findOwnerUsuarioId(20L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> despesaVariavelService.criar(dto));
        assertEquals("Acesso negado: a categoria informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void despesaVariavelDeveLancar404AoAtualizarIdInexistente() {
        DespesaVariavelRequestDTO dto = new DespesaVariavelRequestDTO(
            "Celular", "Loja X", LocalDate.of(2024, 1, 1),
            new BigDecimal("200.00"), 10, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs"
        );

        when(despesaVariavelRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> despesaVariavelService.atualizar(999L, dto));
        assertEquals("Despesa variável não encontrada: 999", ex.getMessage());
    }

    // --- ReceitaVariavel Tenant Validation ---

    @Test
    public void receitaVariavelDeveNegarAcessoQuandoContaPertenceAOutroTenant() {
        ReceitaVariavelRequestDTO dto = new ReceitaVariavelRequestDTO(
            "Freelance", new BigDecimal("1000.00"), 2, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs"
        );

        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> receitaVariavelService.criar(dto));
        assertEquals("Acesso negado: a conta informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void receitaVariavelDeveNegarAcessoQuandoCategoriaPertenceAOutroTenant() {
        ReceitaVariavelRequestDTO dto = new ReceitaVariavelRequestDTO(
            "Freelance", new BigDecimal("1000.00"), 2, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs"
        );

        Conta conta = new Conta(10L, "Nubank", "Obs");
        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(TENANT_CORRENTE));
        when(contaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findOwnerUsuarioId(20L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> receitaVariavelService.criar(dto));
        assertEquals("Acesso negado: a categoria informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void receitaVariavelDeveLancar404AoAtualizarIdInexistente() {
        ReceitaVariavelRequestDTO dto = new ReceitaVariavelRequestDTO(
            "Freelance", new BigDecimal("1000.00"), 2, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs"
        );

        when(receitaVariavelRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> receitaVariavelService.atualizar(999L, dto));
        assertEquals("Receita variável não encontrada: 999", ex.getMessage());
    }
}

