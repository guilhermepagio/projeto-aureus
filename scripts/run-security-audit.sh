#!/usr/bin/env bash
# ==============================================================================
# Aureus - Script Canônico de Auditoria de Segurança Local
# ==============================================================================
# Executa auditorias de vulnerabilidades conhecidas (CVEs) no ecossistema
# backend (Java / Spring Boot via OWASP Dependency-Check) e frontend
# (React / Node via npm audit).
#
# NOTA: Este script é intencionalmente independente do git pre-push, pois
# varreduras de CVEs requerem sincronização de bases remotas e consultas a feeds
# de vulnerabilidade que levam mais tempo que a suíte rápida de testes unitários.
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

# Configurações padrão
AUDIT_BACKEND=true
AUDIT_FRONTEND=true
SKIP_NVD_UPDATE=false

show_help() {
  cat << EOF
${BOLD}Uso:${NC} ./scripts/run-security-audit.sh [OPÇÕES]

${BOLD}Descrição:${NC}
  Audita vulnerabilidades e falhas de segurança (CVEs) nas dependências do projeto Aureus.
  - Frontend (React): Executa 'npm audit' para detectar e reportar pacotes vulneráveis.
  - Backend (Spring Boot): Executa OWASP Dependency-Check para correlacionar JARs com a base NVD da NIST.

${BOLD}Opções disponíveis:${NC}
  -f, --frontend-only    Executa apenas a auditoria de segurança do frontend (npm audit).
  -b, --backend-only     Executa apenas a auditoria de segurança do backend (Maven OWASP).
  --skip-update          Ignora o download/atualização da base remota NVD do OWASP (usa cache local).
  -h, --help             Exibe este menu de ajuda e encerra com código 0.

${BOLD}Variáveis de Ambiente Recomendadas:${NC}
  NVD_API_KEY            Chave de API do NIST NVD (obtenha gratuitamente em https://nvd.nist.gov/developers/request-an-api-key).
                         Sem a chave, a NIST limita requisições drasticamente, tornando o primeiro download do feed lento.

${BOLD}Exemplos:${NC}
  ./scripts/security-audit.sh                   # Auditoria completa (Frontend + Backend)
  ./scripts/security-audit.sh --frontend-only   # Auditoria rápida apenas do npm
  NVD_API_KEY=xxx ./scripts/security-audit.sh   # Auditoria backend com velocidade máxima de API

EOF
}

# Processamento de flags e argumentos
while [[ $# -gt 0 ]]; do
  case "$1" in
    -h|--help)
      show_help
      exit 0
      ;;
    -f|--frontend-only)
      AUDIT_FRONTEND=true
      AUDIT_BACKEND=false
      shift
      ;;
    -b|--backend-only)
      AUDIT_FRONTEND=false
      AUDIT_BACKEND=true
      shift
      ;;
    --skip-update)
      SKIP_NVD_UPDATE=true
      shift
      ;;
    *)
      echo -e "${RED}Erro: Opção desconhecida: '$1'${NC}" >&2
      echo "" >&2
      show_help >&2
      exit 1
      ;;
  esac
done

echo -e "${CYAN}${BOLD}==============================================================================${NC}"
echo -e "${CYAN}${BOLD}       Aureus - Auditoria de Segurança e Varredura de CVEs                    ${NC}"
echo -e "${CYAN}${BOLD}==============================================================================${NC}"

# Auto-detecção do JDK 21 caso instalado no diretório padrão do sistema
if [ -d "/c/Program Files/Java/jdk-21" ] || [ -d "C:/Program Files/Java/jdk-21" ]; then
  export JAVA_HOME="C:/Program Files/Java/jdk-21"
  if [ -d "/c/Program Files/Java/jdk-21/bin" ]; then
    export PATH="/c/Program Files/Java/jdk-21/bin:${PATH}"
  else
    export PATH="${JAVA_HOME}/bin:${PATH}"
  fi
elif [ -d "/mnt/c/Program Files/Java/jdk-21" ]; then
  export JAVA_HOME="/mnt/c/Program Files/Java/jdk-21"
  export PATH="${JAVA_HOME}/bin:${PATH}"
fi

# ------------------------------------------------------------------------------
# 1. Auditoria Frontend (React / npm)
# ------------------------------------------------------------------------------
if [ "${AUDIT_FRONTEND}" = true ]; then
  echo ""
  echo -e "${BLUE}${BOLD}[1/2] Auditando dependências do Frontend (npm audit)...${NC}"
  if [ ! -d "${FRONTEND_DIR}" ]; then
    echo -e "${YELLOW}Aviso: Diretório '${FRONTEND_DIR}' não encontrado. Pulando auditoria frontend.${NC}"
  else
    cd "${FRONTEND_DIR}"
    if npm audit; then
      echo -e "${GREEN}✓ Nenhuma vulnerabilidade de segurança encontrada no Frontend!${NC}"
    else
      EXIT_CODE=$?
      echo -e "${YELLOW}⚠️ Vulnerabilidades identificadas no npm audit.${NC}"
      echo -e "${YELLOW}Execute 'npm audit fix' dentro de '${FRONTEND_DIR}' para tentar resolução automática.${NC}"
      if [ "${AUDIT_BACKEND}" = false ]; then
        exit "${EXIT_CODE}"
      fi
    fi
    cd "${PROJECT_ROOT}"
  fi
fi

# ------------------------------------------------------------------------------
# 2. Auditoria Backend (Java / Spring Boot / OWASP)
# ------------------------------------------------------------------------------
if [ "${AUDIT_BACKEND}" = true ]; then
  echo ""
  echo -e "${BLUE}${BOLD}[2/2] Auditando dependências do Backend (OWASP Dependency-Check)...${NC}"
  if [ ! -d "${BACKEND_DIR}" ]; then
    echo -e "${YELLOW}Aviso: Diretório '${BACKEND_DIR}' não encontrado. Pulando auditoria backend.${NC}"
  else
    cd "${BACKEND_DIR}"
    
    if [ -z "${NVD_API_KEY:-}" ]; then
      echo -e "${YELLOW}ℹ️  Nota: A variável NVD_API_KEY não foi informada.${NC}"
      echo -e "${YELLOW}   O download e atualização de feeds CVE da NIST podem levar alguns minutos devido a rate-limits.${NC}"
      echo -e "${YELLOW}   Dica: Obtenha uma chave gratuita em: https://nvd.nist.gov/developers/request-an-api-key${NC}"
    else
      echo -e "${GREEN}✓ NVD_API_KEY detectada. Utilizando alta taxa de requisições da NIST.${NC}"
    fi

    MVN_CMD=("./mvnw" "-Psecurity-audit" "org.owasp:dependency-check-maven:check")
    if [ "${SKIP_NVD_UPDATE}" = true ]; then
      MVN_CMD+=("-DnvdValidForHours=720" "-DautoUpdate=false")
    fi

    echo -e "${CYAN}Executando OWASP Dependency-Check via Maven...${NC}"
    if "${MVN_CMD[@]}"; then
      echo -e "${GREEN}✓ Nenhuma vulnerabilidade de severidade crítica/alta (CVSS >= 8) identificada no Backend!${NC}"
      echo -e "${CYAN}Relatório detalhado gerado em: ${BACKEND_DIR}/target/dependency-check-report.html${NC}"
    else
      echo -e "${RED}❌ O OWASP Dependency-Check identificou potenciais vulnerabilidades no Backend.${NC}"
      echo -e "${YELLOW}Consulte o relatório em: ${BACKEND_DIR}/target/dependency-check-report.html${NC}"
      exit 1
    fi
    cd "${PROJECT_ROOT}"
  fi
fi

echo ""
echo -e "${GREEN}${BOLD}==============================================================================${NC}"
echo -e "${GREEN}${BOLD}       Auditoria de Segurança Concluída com Sucesso!                          ${NC}"
echo -e "${GREEN}${BOLD}==============================================================================${NC}"
