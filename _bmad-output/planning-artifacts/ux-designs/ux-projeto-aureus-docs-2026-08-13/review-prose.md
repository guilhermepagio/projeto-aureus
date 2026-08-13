# Revisão editorial — UX de documentação

Escopo: `DESIGN.md` e `EXPERIENCE.md`. A proposta está bem orientada: comunica
portfólio solo com sobriedade, estabelece a jornada produto → técnico e evita
processos artificiais. Os pontos abaixo evitam ruídos na execução.

| Severidade | Evidência | Impacto e recomendação |
| --- | --- | --- |
| Alta | `DESIGN.md` está integralmente em inglês, enquanto determina “Write in Brazilian Portuguese”. | O contrato destinado a orientar documentação em português cria uma exceção não explicada e prejudica a consistência. Traduzir o corpo do documento (ou declarar claramente que contratos internos são em inglês). |
| Alta | Os dois arquivos exibem literais como `{colors.system}`, `{typography.system}` e `{components.links}`. | Quem executar o contrato não saberá se são valores reais ou variáveis a resolver; no GitHub, eles aparecerão como texto bruto. Substituir pelos valores finais (“tema nativo do GitHub”, por exemplo) ou documentar o mecanismo de interpolação. |
| Média | `EXPERIENCE.md`: “ERD e classes são documentação técnica” e “modelos que citam `Usuario`”. | “Classes” é impreciso como nome de artefato e alterna com “modelo de domínio”; usar sempre “diagrama de classes” ou “modelo de domínio”. Padronizar também `Usuario`/`Usuário` conforme a nomenclatura canônica. |
| Média | `EXPERIENCE.md`: “**Clímax:**” em ambos os fluxos. | O termo é incomum em uma especificação de navegação e distrai do comportamento verificável. Trocar por “Ponto de decisão” ou descrever diretamente a ação. |
| Baixa | Há alternância entre “solo”, “único desenvolvedor” e “único mantenedor”. | Todas são compreensíveis, mas uma nomenclatura única reforça o posicionamento. Recomenda-se “mantido por um único desenvolvedor” para contexto e “mantenedor” somente para a responsabilidade de manutenção. |
| Baixa | `DESIGN.md`: “calm, direct, and deliberate” / “quality signal”; `EXPERIENCE.md`: “provar organização”. | A linguagem em inglês é mais idiomática que a tradução implícita; “provar” soa mais absoluto que o necessário. Preferir “evidenciar organização e pensamento de produto”. |

## Veredito

**Aprovável com ajustes.** Não há ambiguidade estrutural na jornada proposta;
porém, a tradução do `DESIGN.md` e a eliminação/definição dos placeholders são
necessárias antes de tratá-los como contratos de referência.
