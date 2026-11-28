#!/usr/bin/env bash
set -euo pipefail

fail() {
    printf 'Error: %s\n' "$1" >&2
    exit 1
}

if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
    cat <<'USAGE'
Usage: ./scripts/arcadedb-studio.sh

Start ArcadeDB Studio for Bistra's existing local database using Docker.
The image version is read from the project's pom.xml.

Environment:
  BISTRA_STORAGE_DATA_PATH   Data root (default: <repository>/.dump/data).
                            Relative paths are resolved from the repository root.
  ARCADEDB_STUDIO_PORT       Local HTTP port (default: 2480).
  ARCADEDB_STUDIO_PASSWORD   Studio root password (prompts if unset).

Open http://127.0.0.1:2480, log in as root, and select database bistra.
Stop Bistra before starting Studio. Press Ctrl+C to stop Studio before
running Bistra commands again. Studio edits change the application's data.
USAGE
    exit 0
fi
[[ $# -eq 0 ]] || fail 'Unknown argument. Use --help for usage.'

project_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd -P)"
cd "$project_root"

data_root="${BISTRA_STORAGE_DATA_PATH:-$project_root/.dump/data}"
[[ -d "$data_root/.arcadedb" ]] || fail "No local database found. Run './bistra user list' first, or set BISTRA_STORAGE_DATA_PATH."
data_root="$(cd -- "$data_root" && pwd -P)"
database_directory="$(cd -- "$data_root/.arcadedb" && pwd -P)"
[[ "$database_directory" == "${data_root%/}/"* ]] || fail 'The database directory resolves outside the configured data root.'
[[ -r "$database_directory/configuration.json" ]] || fail 'The database configuration is missing or unreadable.'

port="${ARCADEDB_STUDIO_PORT:-2480}"
[[ "$port" =~ ^[0-9]{1,5}$ ]] || fail 'ARCADEDB_STUDIO_PORT must be a number between 1 and 65535.'
(( 10#$port >= 1 && 10#$port <= 65535 )) || fail 'ARCADEDB_STUDIO_PORT must be between 1 and 65535.'
port="$((10#$port))"

arcadedb_version="$(sed -n 's/.*<arcadedb.version>\([^<]*\)<\/arcadedb.version>.*/\1/p' "$project_root/pom.xml")"
[[ -n "$arcadedb_version" ]] || fail 'Cannot determine the ArcadeDB version from pom.xml.'
command -v docker >/dev/null 2>&1 || fail 'Install Docker first.'
docker info >/dev/null 2>&1 || fail 'Start the Docker engine and retry.'

export LC_ALL=C
password="${ARCADEDB_STUDIO_PASSWORD:-}"
if [[ -n "$password" ]]; then
    [[ "$password" =~ ^[!-~]{8,128}$ ]] || fail 'Use 8 to 128 ASCII letters, digits, or punctuation for the Studio password.'
else
    [[ -t 0 ]] || fail 'Set ARCADEDB_STUDIO_PASSWORD when running without a terminal.'
    while :; do
        if ! IFS= read -r -s -p 'Studio root password: ' password; then
            printf '\n' >&2
            fail 'Password input ended.'
        fi
        printf '\n'
        if [[ "$password" =~ ^[!-~]{8,128}$ ]]; then
            break
        fi
        printf 'Use 8 to 128 ASCII letters, digits, or punctuation.\n' >&2
    done
fi

printf 'Starting Studio at http://127.0.0.1:%s (user: root, database: bistra).\n' "$port"
printf 'Press Ctrl+C to stop Studio before running Bistra commands again.\n'

# Pass the password through the environment rather than Docker's command arguments.
# Replacing the shell lets Docker forward Ctrl+C and remove the stopped container.
exec env "arcadedb.server.rootPassword=$password" docker run --rm --init \
    --name bistra-studio \
    --memory 1g \
    --publish "127.0.0.1:$port:2480" \
    --mount "type=bind,source=$database_directory,target=/home/arcadedb/databases/bistra" \
    --env arcadedb.server.rootPassword \
    "arcadedata/arcadedb:$arcadedb_version"
