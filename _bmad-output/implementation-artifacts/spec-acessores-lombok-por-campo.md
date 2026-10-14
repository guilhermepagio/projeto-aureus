---
title: 'Acessores Lombok explícitos por campo'
type: 'refactor'
created: '2026-10-14'
status: 'done'
baseline_commit: 'f5922ef780a51a01b133285f1bb558047ce08e52'
review_loop_iteration: 0
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O backend contém getters e setters manuais que apenas leem ou atribuem campos, inclusive em entidades já anotadas com Lombok. Isso duplica código e dificulta identificar os métodos que efetivamente implementam comportamento.

**Approach:** Substituir somente acessores triviais por `@Getter` e `@Setter` diretamente nos campos correspondentes. Padronizar as oito classes de domínio nesse formato, conservando a API pública e toda lógica existente. O usuário autorizou continuar a implementação após a discussão do escopo no Party Mode.

## Boundaries & Constraints

**Always:** Preservar nomes, tipos e visibilidade dos métodos públicos; manter validação, persistência, serialização, construtores e builders; `TenantAwareEntity.usuarioId` continua somente com getter. Trabalhar sobre o estado atual dos arquivos, preservando alterações anteriores do usuário. O workspace está modificado e a continuação foi autorizada nesse contexto. O implementador é responsável apenas pelos oito arquivos diretamente em `domain/`, `security/JwtUtil.java` e pelas tarefas desta especificação. Outros agentes e o usuário podem estar trabalhando no repositório: não reverter edições alheias, adaptar-se a elas.

**Ask First:** Alterações que modifiquem o comportamento ou a API pública em vez de apenas a declaração dos acessores.

**Never:** Substituir métodos com cálculo, normalização, consulta ao contexto ou parsing por acessores gerados; criar setters antes inexistentes; mudar dependências, versões Java, testes existentes ou arquivos fora do escopo; usar `@Data` em entidades; executar commit, push ou operações remotas.

</frozen-after-approval>

## Code Map

- Base Java: `backend-api/src/main/java/com/guilhermepagio/aureus/backend/`.
- `domain/Categoria.java` e `domain/Conta.java`: três campos cada; seis acessores triviais em cada classe, além de anotações Lombok redundantes na classe.
- `domain/DespesaFixa.java` e `domain/ReceitaFixa.java`: sete campos e quatorze acessores triviais por classe.
- `domain/DespesaVariavel.java`: onze campos e vinte e dois acessores triviais; manter literalmente `getValorTotal()` e `getUltimaParcela()`, com seus comentários e anotações.
- `domain/ReceitaVariavel.java`: nove campos e dezoito acessores triviais; mesmos métodos calculados a preservar. Ambas as entidades variáveis já têm alterações locais: partir do conteúdo atual.
- `domain/Usuario.java`: getters/setters gerados na classe; mover anotações aos cinco campos, preservando `@Builder` e construtores.
- `domain/TenantAwareEntity.java`: mover somente `@Getter` ao campo `usuarioId`, sem setter.
- `security/JwtUtil.java`: `getExpiration()` apenas retorna `expiration`; substituí-lo por `@Getter` nesse campo final. `key` não ganha getter; `getSubject(String)` permanece explícito.
- `domain/dto/ConsolidacaoPorContaDTO.java`: somente leitura; setter de saldo histórico contém normalização de escala e nulos, portanto preservar integralmente. Demais DTOs não precisam de alterações.
- `security/TenantContext.java`: somente leitura; acessores operam sobre ThreadLocal, fora da substituição.
- `backend-api/pom.xml`: somente leitura; Java 21 e Lombok já configurados, incluindo annotation processor.
- `docs/architecture/testing.md` e `scripts/run-unit-tests.sh`: orientações existentes de validação. Maven wrapper disponível em `backend-api/mvnw.cmd`; JDK esperado em `C:/Program Files/Java/jdk-21`.

## Tasks & Acceptance

**Execution:**
- [x] `domain/{Categoria,Conta,DespesaFixa,ReceitaFixa,DespesaVariavel,ReceitaVariavel}.java` — substituir os 80 acessores triviais por anotações nos campos, removendo as anotações de acesso na classe.
- [x] `domain/{Usuario,TenantAwareEntity}.java` — mover anotações de acesso da classe aos campos, preservando restrições.
- [x] `security/JwtUtil.java` — gerar somente o getter do campo `expiration` por Lombok.
- [x] Executar compilação e testes existentes do backend, com Java 21; verificar o diff para preservar mudanças locais e métodos com lógica.

**Acceptance Criteria:**
- Given as classes refatoradas, when consumidores compilam, then nomes, tipos e visibilidade dos acessores continuam compatíveis.
- Given um campo sem setter anterior, when o código é refatorado, then nenhum setter novo é gerado.
- Given métodos com cálculo ou normalização, when a refatoração termina, then seus corpos e anotações permanecem intactos.
- Given a suíte existente do backend, when executada com Java 21, then todos os testes passam ou um impedimento externo é reportado precisamente.

## Spec Change Log

## Verification

**Commands:**
- Em `backend-api/`, `./mvnw.cmd -B clean test` com `JAVA_HOME` apontando para Java 21 — recompilação e suíte unitária/web MVC sem falhas.
- `git diff --check` nos arquivos alterados — sem erros de whitespace introduzidos.
- Inspecionar os métodos remanescentes e a API compilada para conservar os acessores públicos, a ausência de setter de tenant e a lógica financeira.
- A revisão principal executará os testes de integração existentes se Docker estiver disponível; se indisponível, reportar a limitação sem alterar configuração.

**Resultados observados em 2026-10-14:**
- `./mvnw.cmd -B clean test`, com Java 21: 205 testes executados, zero falhas, zero erros, zero ignorados.
- `javap -public`: 92 acessores de campos conferidos por nome, tipo e visibilidade, sem acessores extras. `setUsuarioId` e `JwtUtil.getKey` continuam ausentes.
- Métodos calculados das duas entidades variáveis comparados literalmente com o conteúdo anterior, incluindo comentários e anotações: intactos. O DTO de saldo histórico não foi editado.
- `git diff --check` restrito aos oito arquivos diretamente em `domain/` e a `security/JwtUtil.java`: passou.
- Integração (`./mvnw.cmd -B verify -Pintegration-test`) não executada: Docker indisponível, pipe `dockerDesktopLinuxEngine` inexistente.
- Limite da alteração: nove arquivos Java; demais diferenças do workspace são anteriores à tarefa, incluindo os imports `DecimalMax` nas entidades variáveis. Snapshot temporário anterior: `C:/Users/GUILHE~1/AppData/Local/Temp/aureus-lombok-bed70756-a549-4394-a106-46e950f65d51.json`.
- Revisão em três camadas concluída: nenhum defeito funcional ou lacuna de verificação identificado. Observações pertinentes de rastreabilidade e evidência incorporadas nesta especificação.

## Suggested Review Order

- Anotações por campo substituem acessores triviais e explicitam permissões de acesso.
  [Categoria.java:24](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Categoria.java#L24)
- O identificador do tenant mantém exclusivamente leitura pública.
  [TenantAwareEntity.java:8](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java#L8)
- Campos financeiros usam Lombok; métodos de cálculo permanecem explícitos.
  [ReceitaVariavel.java:40](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/ReceitaVariavel.java#L40)
- O getter de expiração é gerado sem expor a chave criptográfica.
  [JwtUtil.java:15](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtUtil.java#L15)
