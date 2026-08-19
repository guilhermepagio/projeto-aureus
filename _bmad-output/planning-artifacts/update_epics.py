import re

with open('epics.md', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace Epic 4 -> Epic 5
content = re.sub(r'Epic 4', 'Epic 5', content)
content = re.sub(r'Story 4\.', 'Story 5.', content)

# Replace Epic 3 -> Epic 4
content = re.sub(r'Epic 3', 'Epic 4', content)
content = re.sub(r'Story 3\.', 'Story 4.', content)

# Replace Epic 2 -> Epic 3
content = re.sub(r'Epic 2', 'Epic 3', content)
content = re.sub(r'Story 2\.', 'Story 3.', content)

# But wait! I replaced "Epic 2" in the "Epic List" section earlier.
# The "Epic List" was:
# * **Epic 1: ...
# * **Epic 2: Autenticação Fluida com Google One Tap** ...
# * **Epic 3: Configuração Financeira Básica...
# * **Epic 4: Lançamentos Financeiros...
# * **Epic 5: Consolidação e Projeção Mensal...
# So the earlier replacement might mess up the Epic List if I'm not careful.
