import re

with open('epics.md', 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
in_fr_map = False

for line in lines:
    if line.startswith('### FR Coverage Map'):
        in_fr_map = True
    elif line.startswith('## Epic List'):
        in_fr_map = False
    
    if in_fr_map:
        # shift epics in FR map
        line = re.sub(r'Epic 4', 'Epic 5', line)
        line = re.sub(r'Epic 3', 'Epic 4', line)
        line = re.sub(r'Epic 2', 'Epic 3', line)
        new_lines.append(line)
        continue
        
    if line.startswith('## Epic 4'):
        line = line.replace('## Epic 4', '## Epic 5')
    elif line.startswith('## Epic 3'):
        line = line.replace('## Epic 3', '## Epic 4')
    elif line.startswith('## Epic 2'):
        line = line.replace('## Epic 2', '## Epic 3')
        
    if line.startswith('### Story 4.'):
        line = line.replace('### Story 4.', '### Story 5.')
    elif line.startswith('### Story 3.'):
        line = line.replace('### Story 3.', '### Story 4.')
    elif line.startswith('### Story 2.'):
        line = line.replace('### Story 2.', '### Story 3.')
        
    new_lines.append(line)

epic2_content = """
---

## Epic 2: Autenticação Fluida com Google One Tap

Evoluir a experiência de login para o Google One Tap, mantendo o usuário na tela de boas-vindas sem redirecionamento forçado, integrando a validação no backend existente.

### Story 2.1: Renderização do Prompt do Google One Tap via Clique

As a Usuário não autenticado,
I want que o prompt do Google One Tap seja acionado quando eu clicar no botão de login,
So that eu possa me autenticar rapidamente no contexto da página, apenas quando eu tiver a intenção explícita de entrar.

**Acceptance Criteria:**

**Given** que o usuário acessa a aplicação e visualiza a tela de boas-vindas com o botão "Entrar com Google"
**When** o usuário **clica no botão** "Entrar com Google"
**Then** o prompt nativo do Google One Tap deve ser exibido sobre a interface (sem recarregar a página)
**And** se o usuário fechar o prompt (ou se houver bloqueio do navegador), o foco do teclado deve retornar ao botão
**And** no caso de bloqueio nativo do One Tap, o sistema não deve travar, permitindo o fluxo normal ou exibindo mensagem de erro tratada.

### Story 2.2: Validação do Token e Transição de Estado (Backend + Integração)

As a Usuário,
I want que minhas credenciais do One Tap sejam validadas pela API de forma invisível,
So that eu seja logado na aplicação e direcionado ao painel, mantendo a compatibilidade com a segurança existente.

**Acceptance Criteria:**

**Given** que o usuário selecionou sua conta no prompt do One Tap com sucesso
**When** o frontend recebe a resposta contendo a credencial (token OIDC)
**Then** o frontend deve enviar este token (POST) para um novo endpoint de verificação (`/api/auth/onetap`)
**And** o backend deve validar a assinatura criptográfica do token, reconhecer o usuário e emitir o JWT padrão no cookie `HttpOnly` (reaproveitando a lógica do Epic 1)
**And** no sucesso da resposta, o frontend transita o usuário direto para a aba de "Consolidação"
**And** em caso de falha de validação, um Toast de erro é exibido (via `aria-live`) e o usuário continua na tela de login.
"""

# Insert Epic 2 right before Epic 3
final_lines = []
for line in new_lines:
    if line.startswith('## Epic 3'):
        final_lines.append(epic2_content + '\n')
    final_lines.append(line)
    
# Update frontmatter stepsCompleted if needed (I will just rewrite it)
# We can use regex to replace stepsCompleted: [...] with the new one
out_text = "".join(final_lines)
out_text = re.sub(r'stepsCompleted: \[.*?\]', 'stepsCompleted: ["step-01-validate-prerequisites", "step-02-design-epics", "step-03-create-stories", "step-04-final-validation"]', out_text)

with open('epics.md', 'w', encoding='utf-8') as f:
    f.write(out_text)

