#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIRECTORY="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_DIRECTORY="$(cd -- "${SCRIPT_DIRECTORY}/.." && pwd)"
ENV_FILE="${WORKSPACE_DIRECTORY}/.env"
ENV_EXAMPLE_FILE="${WORKSPACE_DIRECTORY}/.env.example"

log() {
  printf '==> %s\n' "$*"
}

fail() {
  printf '[irm-dev] ERROR: %s\n' "$*" >&2
  exit 1
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || fail "Missing required command: $1"
}

version_at_least() {
  local actual_version="$1"
  local minimum_version="$2"

  node -e '
    const parse = (value) => value.replace(/^v/, "").split(".").map(Number)
    const [actual, minimum] = process.argv.slice(1).map(parse)
    const length = Math.max(actual.length, minimum.length)
    for (let index = 0; index < length; index += 1) {
      const difference = (actual[index] ?? 0) - (minimum[index] ?? 0)
      if (difference !== 0) process.exit(difference > 0 ? 0 : 1)
    }
  ' "${actual_version}" "${minimum_version}"
}

ensure_node() {
  local minimum_node_version
  local preferred_node_version
  local current_node_version=""
  local nvm_script

  preferred_node_version="$(tr -d '[:space:]' < "${WORKSPACE_DIRECTORY}/.nvmrc")"

  if command -v node >/dev/null 2>&1; then
    minimum_node_version="$(node -e '
      const { readFileSync } = require("node:fs")
      const manifest = JSON.parse(readFileSync(process.argv[1], "utf8"))
      process.stdout.write(manifest.engines.node.replace(/^>=/, ""))
    ' "${WORKSPACE_DIRECTORY}/package.json")"
    current_node_version="$(node --version)"

    if version_at_least "${current_node_version}" "${minimum_node_version}"; then
      return
    fi
  fi

  nvm_script="${NVM_DIR:-${HOME}/.nvm}/nvm.sh"
  if [[ ! -s "${nvm_script}" ]]; then
    fail "Node.js ${preferred_node_version} is required. Install it or install nvm, then rerun this script."
  fi

  log "Activating Node.js ${preferred_node_version} with nvm"
  set +u
  # shellcheck disable=SC1090
  source "${nvm_script}"
  set -u

  if ! nvm use "${preferred_node_version}" >/dev/null 2>&1; then
    log "Installing Node.js ${preferred_node_version}"
    nvm install "${preferred_node_version}"
    nvm use "${preferred_node_version}" >/dev/null
  fi
}

ensure_java() {
  local java_major=""

  if command -v java >/dev/null 2>&1; then
    java_major="$(java -XshowSettings:properties -version 2>&1 \
      | awk -F= '/java.specification.version/ { gsub(/[[:space:]]/, "", $2); print $2; exit }')"
  fi

  if [[ "${java_major}" != "21" ]] \
      && [[ "$(uname -s)" == "Darwin" ]] \
      && [[ -x /usr/libexec/java_home ]]; then
    if JAVA_HOME_21="$(/usr/libexec/java_home -v 21 2>/dev/null)"; then
      export JAVA_HOME="${JAVA_HOME_21}"
      export PATH="${JAVA_HOME}/bin:${PATH}"
      java_major="21"
    fi
  fi

  [[ "${java_major}" == "21" ]] || fail "Java 21 is required. Current Java specification version: ${java_major:-not found}."
}

create_local_environment() {
  local database_password
  local root_password
  local jwt_secret
  local temporary_env

  if docker volume inspect irm_mysql-data >/dev/null 2>&1; then
    fail "${ENV_FILE} is missing but the irm_mysql-data volume already exists. Restore the matching .env, or explicitly remove the old volume before generating new database credentials."
  fi

  read -r database_password root_password jwt_secret < <(
    node -e '
      const { randomBytes } = require("node:crypto")
      console.log([24, 24, 48].map((size) => randomBytes(size).toString("hex")).join(" "))
    '
  )

  temporary_env="$(mktemp "${WORKSPACE_DIRECTORY}/.env.tmp.XXXXXX")"
  chmod 600 "${temporary_env}"

  while IFS= read -r line || [[ -n "${line}" ]]; do
    case "${line}" in
      MYSQL_ROOT_PASSWORD=*) printf 'MYSQL_ROOT_PASSWORD=%s\n' "${root_password}" ;;
      DB_PASSWORD=*) printf 'DB_PASSWORD=%s\n' "${database_password}" ;;
      JWT_SECRET=*) printf 'JWT_SECRET=%s\n' "${jwt_secret}" ;;
      *) printf '%s\n' "${line}" ;;
    esac
  done < "${ENV_EXAMPLE_FILE}" > "${temporary_env}"

  mv "${temporary_env}" "${ENV_FILE}"
  log "Created .env with generated local-only credentials"
}

validate_local_environment() {
  local required_name

  for required_name in DB_URL DB_USERNAME DB_PASSWORD MYSQL_ROOT_PASSWORD JWT_SECRET; do
    if ! grep -Eq "^${required_name}=.+" "${ENV_FILE}"; then
      fail "${ENV_FILE} must define a non-empty ${required_name}."
    fi
  done

  if grep -Eq '^(DB_PASSWORD|MYSQL_ROOT_PASSWORD|JWT_SECRET)=replace-' "${ENV_FILE}"; then
    fail "${ENV_FILE} still contains placeholder credentials. Remove it to generate safe local values, or replace the placeholders manually."
  fi

  if [[ -n "${SERVER_PORT:-}" && "${SERVER_PORT}" != "7001" ]]; then
    fail "SERVER_PORT=${SERVER_PORT} conflicts with the Vite /api proxy. Local API development uses port 7001."
  fi
}

install_node_dependencies() {
  if [[ ! -x "${WORKSPACE_DIRECTORY}/node_modules/.bin/nx" ]] \
      || [[ ! -f "${WORKSPACE_DIRECTORY}/node_modules/.package-lock.json" ]] \
      || [[ "${WORKSPACE_DIRECTORY}/package-lock.json" -nt "${WORKSPACE_DIRECTORY}/node_modules/.package-lock.json" ]]; then
    log "Installing Node.js dependencies with npm ci"
    npm ci
  fi
}

port_is_listening() {
  local port="$1"

  if command -v lsof >/dev/null 2>&1; then
    lsof -nP -iTCP:"${port}" -sTCP:LISTEN >/dev/null 2>&1
  elif command -v nc >/dev/null 2>&1; then
    nc -z 127.0.0.1 "${port}" >/dev/null 2>&1
  else
    return 1
  fi
}

env_value() {
  local key="$1"

  awk -F= -v key="${key}" '
    $1 == key {
      sub(/^[^=]*=/, "")
      print
      exit
    }
  ' "${ENV_FILE}"
}

find_available_port() {
  local candidate="$1"

  while port_is_listening "${candidate}"; do
    ((candidate += 1))
    [[ "${candidate}" -le 65535 ]] || fail "Unable to find an available local port."
  done

  printf '%s\n' "${candidate}"
}

compose_published_port() {
  local service="$1"
  local container_port="$2"
  local published_address

  published_address="$(docker compose port "${service}" "${container_port}" 2>/dev/null | head -n 1)"
  [[ -n "${published_address}" ]] || return 1
  printf '%s\n' "${published_address##*:}"
}

select_host_port() {
  local service="$1"
  local container_port="$2"
  local requested_port="$3"
  local fallback_port="$4"
  local published_port

  if published_port="$(compose_published_port "${service}" "${container_port}")"; then
    printf '%s\n' "${published_port}"
  elif ! port_is_listening "${requested_port}"; then
    printf '%s\n' "${requested_port}"
  else
    find_available_port "${fallback_port}"
  fi
}

configure_infrastructure_ports() {
  local requested_mysql_port="${MYSQL_PORT:-$(env_value MYSQL_PORT)}"
  local requested_smtp_port="${MAILPIT_SMTP_PORT:-$(env_value MAILPIT_SMTP_PORT)}"
  local requested_mailpit_ui_port="${MAILPIT_UI_PORT:-$(env_value MAILPIT_UI_PORT)}"
  local mysql_database="${MYSQL_DATABASE:-$(env_value MYSQL_DATABASE)}"

  requested_mysql_port="${requested_mysql_port:-3306}"
  requested_smtp_port="${requested_smtp_port:-1025}"
  requested_mailpit_ui_port="${requested_mailpit_ui_port:-8025}"
  mysql_database="${mysql_database:-internship_application}"

  DEVELOPMENT_MYSQL_PORT="$(select_host_port mysql 3306 "${requested_mysql_port}" 13306)"
  DEVELOPMENT_SMTP_PORT="$(select_host_port mailpit 1025 "${requested_smtp_port}" 11025)"
  DEVELOPMENT_MAILPIT_UI_PORT="$(select_host_port mailpit 8025 "${requested_mailpit_ui_port}" 18025)"

  export MYSQL_PORT="${DEVELOPMENT_MYSQL_PORT}"
  export MAILPIT_SMTP_PORT="${DEVELOPMENT_SMTP_PORT}"
  export MAILPIT_UI_PORT="${DEVELOPMENT_MAILPIT_UI_PORT}"
  export DB_URL="jdbc:mysql://localhost:${DEVELOPMENT_MYSQL_PORT}/${mysql_database}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  export MAIL_ENABLED="true"
  export SMTP_HOST="localhost"
  export SMTP_PORT="${DEVELOPMENT_SMTP_PORT}"
}

start_infrastructure() {
  local running_compose_apps

  running_compose_apps="$(docker compose ps --services --status running 2>/dev/null \
    | awk '/^(api|web)$/ { print }')"
  if [[ -n "${running_compose_apps}" ]]; then
    log "Stopping Compose web/api containers so local hot-reload ports are available"
    docker compose stop api web
  fi

  port_is_listening 4200 && fail "Port 4200 is already in use. Stop the existing frontend process and rerun."
  port_is_listening 7001 && fail "Port 7001 is already in use. Stop the existing API process and rerun."

  configure_infrastructure_ports
  log "Starting MySQL and Mailpit..."
  if ! docker compose --progress quiet up -d --wait mysql mailpit; then
    docker compose ps >&2 || true
    docker compose logs --tail=100 mysql mailpit >&2 || true
    fail "Local infrastructure failed to become ready."
  fi
}

run_applications() {
  exec "${WORKSPACE_DIRECTORY}/node_modules/.bin/nx" run-many \
    -t serve \
    -p web,api \
    --parallel=2
}

main() {
  cd "${WORKSPACE_DIRECTORY}"

  log "IRM development environment"

  ensure_node
  require_command npm
  require_command docker
  require_command pgrep
  ensure_java

  local minimum_npm_version
  minimum_npm_version="$(node -e '
    const { readFileSync } = require("node:fs")
    const manifest = JSON.parse(readFileSync(process.argv[1], "utf8"))
    process.stdout.write(manifest.engines.npm.replace(/^>=/, ""))
  ' "${WORKSPACE_DIRECTORY}/package.json")"
  version_at_least "$(npm --version)" "${minimum_npm_version}" \
    || fail "npm ${minimum_npm_version} or newer is required. Current version: $(npm --version)."

  docker compose version >/dev/null 2>&1 || fail "Docker Compose v2 is required."
  docker info >/dev/null 2>&1 || fail "Docker Desktop/daemon is not running."

  [[ -f "${ENV_EXAMPLE_FILE}" ]] || fail "Missing ${ENV_EXAMPLE_FILE}."
  [[ -f "${ENV_FILE}" ]] || create_local_environment
  validate_local_environment
  install_node_dependencies
  start_infrastructure

  printf '\n'
  log "Infrastructure is ready"
  printf '    MySQL:   localhost:%s\n' "${DEVELOPMENT_MYSQL_PORT}"
  printf '    Mailpit: http://localhost:%s (SMTP %s)\n' \
    "${DEVELOPMENT_MAILPIT_UI_PORT}" "${DEVELOPMENT_SMTP_PORT}"
  printf '\n'
  log "Starting web and API with hot reload..."
  printf '    Web: http://localhost:4200\n'
  printf '    API: http://localhost:7001\n'
  printf '    Ctrl+C stops web/API; MySQL and Mailpit remain available.\n'

  run_applications
}

main "$@"
