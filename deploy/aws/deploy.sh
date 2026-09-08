#!/usr/bin/env bash
set -Eeuo pipefail

cd /opt/shop

exec 9>/opt/shop/deploy.lock
flock -w 300 9

new_version="${1:-}"

if [[ ! "$new_version" =~ ^[0-9a-f]{40}$ ]]; then
  echo "A full Git commit SHA is required."
  exit 2
fi

if [[ ! -f .env ]]; then
  echo "/opt/shop/.env is missing."
  exit 2
fi

candidate="$(mktemp /opt/shop/candidate.XXXXXX.env)"
trap 'rm -f "$candidate"' EXIT

printf 'APP_VERSION=%s\n' "$new_version" > "$candidate"

compose_files=(-f compose.yaml)

if [[ -f compose.https.yaml ]]; then
  compose_files+=(-f compose.https.yaml)
fi

dc() {
  local version_file="$1"
  shift

  docker compose \
    --env-file /opt/shop/.env \
    --env-file "$version_file" \
    "${compose_files[@]}" \
    "$@"
}

verify_web() {
  curl \
    --fail \
    --silent \
    --show-error \
    --max-time 15 \
    http://127.0.0.1:8088/healthz > /dev/null

  curl \
    --fail \
    --silent \
    --show-error \
    --max-time 15 \
    http://127.0.0.1:8088/ > /dev/null
}

echo "Validating release: $new_version"
dc "$candidate" config --quiet

echo "Pulling images"
dc "$candidate" pull backend frontend

echo "Starting release"
if dc "$candidate" up \
    -d \
    --wait \
    --wait-timeout 180 \
    --force-recreate \
    backend frontend \
    && verify_web; then

  if [[ -f current.env ]]; then
    cp current.env previous.env
  fi

  mv "$candidate" current.env

  echo "Deployment succeeded: $new_version"
  exit 0
fi

echo "Deployment failed."
dc "$candidate" ps || true

if [[ -f current.env ]]; then
  echo "Attempting rollback."

  if dc /opt/shop/current.env up \
      -d \
      --wait \
      --wait-timeout 180 \
      --force-recreate \
      backend frontend \
      && verify_web; then
    echo "Rollback succeeded."
  else
    echo "Rollback failed. Inspect EC2 logs."
  fi
else
  echo "First deployment failed; no previous release exists."
  dc "$candidate" stop frontend backend || true
fi

exit 1