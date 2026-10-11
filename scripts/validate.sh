#!/usr/bin/env bash
# ==============================================================================
# Aureus - Script Canônico de Validação Contínua Local
# ==============================================================================
# Executa ordenadamente a suíte de testes e validações de qualidade locais do
# backend e frontend com suporte a fail-fast e execução agnóstica de diretório.
# ==============================================================================

set -eo pipefail

# Cores e estilização para terminal interativo
if [ -t 1 ] && [ -z "${NO_COLOR:-}" ]; then
  BOLD=$'\033[1m'
  RED=$'\033[0;31m'
  GREEN=$'\033[0;32m'
  YELLOW=$'\033[1;33m'
  BLUE=$'\033[0;34m'
  CYAN=$'\033[0;36m'
  NC=$'\033[0m'
else
  BOLD=''
  RED=''
  GREEN=''
  YELLOW=''
  BLUE=''
  CYAN=''
  NC=''
fi

# Resolução dinâmica da raiz do repositório
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

BACKEND_DIR="${PROJECT_ROOT}/backend-api"
FRONTEND_DIR="${PROJECT_ROOT}/frontend-web"

# Exibe o menu de ajuda
show_help() {
  cat << EOF
${BOLD}Uso:${NC} ./scripts/validate.sh [OPÇÕES]

${BOLD}Descrição:${NC}
  Script unificado de validação contínua local para o projeto Aureus.
  Executa a suíte de testes, verificação de tipos e linting do backend e frontend
  com suporte a fail-fast (interrompe imediatamente na primeira falha).

${BOLD}Opções disponíveis:${NC}
  -i, --with-integration    Inclui a suíte de testes de integração com Testcontainers
                            (requer Docker em execução na máquina local).
  -h, --help                Exibe este menu de ajuda e encerra com código 0.

${BOLD}Fluxo padrão (sem parâmetros):${NC}
  Executa apenas testes rápidos que não dependem do Docker (~20-30s):
    1. Backend: Testes unitários de domínio e serviços (./mvnw test)
    2. Frontend: Análise estática / Linter (npm run lint)
    3. Frontend: Testes unitários e de componentes Vitest (npm test)
    4. Frontend: Checagem de tipos TypeScript e Build (npm run build)

${BOLD}Exemplos:${NC}
  ./scripts/validate.sh                    # Validação rápida de rotina
  ./scripts/validate.sh --with-integration # Validação completa pré-PR (com Testcontainers)
EOF
}

# Processamento de argumentos de linha de comando
RUN_INTEGRATION=false

while [ $# -gt 0 ]; do
  case "$1" in
    -h|--help)
      show_help
      exit 0
      ;;
    -i|--with-integration)
      RUN_INTEGRATION=true
      shift
      ;;
    *)
      echo -e "${RED}Erro: Opção desconhecida ou inválida: '$1'${NC}" >&2
      echo "" >&2
      show_help >&2
      exit 1
      ;;
  esac
done

# Verificação de integridade dos diretórios do projeto
if [ ! -d "${BACKEND_DIR}" ] || [ ! -d "${FRONTEND_DIR}" ]; then
  echo -e "${RED}Erro: Estrutura de diretórios do projeto incompleta a partir de '${PROJECT_ROOT}'.${NC}" >&2
  echo -e "${RED}Esperado backend em:  ${BACKEND_DIR}${NC}" >&2
  echo -e "${RED}Esperado frontend em: ${FRONTEND_DIR}${NC}" >&2
  exit 1
fi

# Verificação de pré-requisitos de ferramentas locais
if ! command -v java >/dev/null 2>&1; then
  echo -e "${RED}${BOLD}❌ Erro: 'java' não encontrado no PATH.${NC}" >&2
  exit 1
fi

if ! command -v node >/dev/null 2>&1; then
  echo -e "${RED}${BOLD}❌ Erro: 'node' não encontrado no PATH.${NC}" >&2
  exit 1
fi

if ! command -v npm >/dev/null 2>&1; then
  echo -e "${RED}${BOLD}❌ Erro: 'npm' não encontrado no PATH.${NC}" >&2
  exit 1
fi

if [ ! -d "${FRONTEND_DIR}/node_modules" ]; then
  echo -e "${RED}${BOLD}❌ Erro: 'node_modules' não encontrado em '${FRONTEND_DIR}'.${NC}" >&2
  echo -e "${YELLOW}Dica: Execute 'npm install' no diretório '${FRONTEND_DIR}' antes de validar.${NC}" >&2
  exit 1
fi

if [ ! -x "${BACKEND_DIR}/mvnw" ]; then
  echo -e "${RED}${BOLD}❌ Erro: '${BACKEND_DIR}/mvnw' não encontrado ou sem permissão de execução.${NC}" >&2
  exit 1
fi

# Verificação de pré-requisitos para o modo com integração
if [ "${RUN_INTEGRATION}" = true ]; then
  if ! docker info >/dev/null 2>&1; then
    echo -e "${RED}${BOLD}❌ Erro: O serviço do Docker não está em execução ou não foi encontrado.${NC}" >&2
    echo -e "${RED}A suíte de integração com Testcontainers requer o daemon do Docker ativo.${NC}" >&2
    echo -e "${YELLOW}Dica: Inicie o Docker Desktop/Daemon ou execute sem a flag '--with-integration'.${NC}" >&2
    exit 1
  fi
fi

START_TIME=$(date +%s)
CURRENT_STEP=""

# Tratamento de erro e fail-fast
on_exit() {
  local exit_code=$?
  if [ "${exit_code}" -ne 0 ]; then
    local end_time
    end_time=$(date +%s)
    local elapsed=$((end_time - START_TIME))
    echo -e "\n${RED}${BOLD}======================================================================${NC}"
    echo -e "${RED}${BOLD}❌ FALHA NA VALIDAÇÃO LOCAL (Código de saída: ${exit_code})${NC}"
    if [ -n "${CURRENT_STEP}" ]; then
      echo -e "${RED}Etapa interrompida: ${BOLD}${CURRENT_STEP}${NC}"
    fi
    echo -e "${RED}Tempo decorrido até a falha: ${elapsed}s${NC}"
    echo -e "${RED}${BOLD}======================================================================${NC}\n"
  fi
}
trap on_exit EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

# Definição das etapas
TOTAL_STEPS=4
if [ "${RUN_INTEGRATION}" = true ]; then
  TOTAL_STEPS=5
fi

run_step() {
  local step_num="$1"
  local step_title="$2"
  local work_dir="$3"
  shift 3
  local cmd=("$@")

  CURRENT_STEP="[${step_num}/${TOTAL_STEPS}] ${step_title}"
  echo -e "\n${BLUE}${BOLD}==> ${CURRENT_STEP}${NC}"
  echo -e "${CYAN}Diretório:${NC} ${work_dir}"
  echo -e "${CYAN}Comando:${NC}   ${cmd[*]}\n"

  (cd "${work_dir}" && "${cmd[@]}")
  echo -e "${GREEN}✓ Concluído com sucesso: ${step_title}${NC}"
}

# Cabeçalho de início
echo -e "${BOLD}${CYAN}======================================================================${NC}"
echo -e "${BOLD}${CYAN}🏛️  Aureus — Validação Contínua Local${NC}"
echo -e "${CYAN}Modo:${NC} $([ "${RUN_INTEGRATION}" = true ] && echo "${YELLOW}Completo com Integração (Testcontainers)${NC}" || echo "${GREEN}Padrão / Rápido (Sem Docker)${NC}")"
echo -e "${CYAN}Raiz do Projeto:${NC} ${PROJECT_ROOT}"
echo -e "${BOLD}${CYAN}======================================================================${NC}"

# Execução das etapas sequenciais
run_step 1 "Backend: Testes Unitários de Domínio e Serviços" "${BACKEND_DIR}" ./mvnw -B test
run_step 2 "Frontend: Análise Estática e Linter" "${FRONTEND_DIR}" npm run lint
run_step 3 "Frontend: Testes Unitários e Componentes" "${FRONTEND_DIR}" npm test
run_step 4 "Frontend: Checagem de Tipos e Build" "${FRONTEND_DIR}" npm run build

if [ "${RUN_INTEGRATION}" = true ]; then
  run_step 5 "Backend: Testes de Integração e Isolamento Multi-Tenant" "${BACKEND_DIR}" ./mvnw -B verify
fi

# Cálculo do tempo decorrido
END_TIME=$(date +%s)
TOTAL_DURATION=$((END_TIME - START_TIME))
MINUTES=$((TOTAL_DURATION / 60))
SECONDS=$((TOTAL_DURATION % 60))

if [ "${MINUTES}" -gt 0 ]; then
  DURATION_FORMATTED="${MINUTES}m ${SECONDS}s (${TOTAL_DURATION}s)"
else
  DURATION_FORMATTED="${SECONDS}s"
fi

# Banner final de sucesso
CURRENT_STEP=""
echo -e "\n${GREEN}${BOLD}======================================================================${NC}"
echo -e "${GREEN}${BOLD}✅ TODAS AS ETAPAS FORAM CONCLUÍDAS COM SUCESSO!${NC}"
echo -e "${GREEN}Modo executado:   $([ "${RUN_INTEGRATION}" = true ] && echo "Completo (Unitários + Integração)" || echo "Padrão (Unitários rápidos)")${NC}"
echo -e "${GREEN}Tempo total:      ${DURATION_FORMATTED}${NC}"
echo -e "${GREEN}${BOLD}======================================================================${NC}\n"

exit 0
