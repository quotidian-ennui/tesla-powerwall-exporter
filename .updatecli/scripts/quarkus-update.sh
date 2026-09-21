#!/usr/bin/env bash
# Note that this doesn't require parameters; since quarkus update will
# do the right thing, but updatecli will pass them in so we set the
# platform version to be what updatecli has suggested.
#
# If argument isn't parsed in then quarkus gradle plugin version will
# not be updated.
#
# If you have quarkus installed then quarkus update --help
# otherwise ./gradlew help --task quarkusUpdate
set -eo pipefail

readonly YQ_ADD_GRADLE_PROPERTY='.recipeList.[]."org.openrewrite.gradle.AddProperty" | select (. != null)'
readonly GRADLE_PROPERTIES="gradle.properties"

root=$(git rev-parse --show-toplevel)

log() {
  if [[ -n "$INTERACTIVE_MODE" ]]; then
    echo "$@"
  fi
}

fast_rewrite() {
  file="$1"
  yq -p yaml -o json -r "$YQ_ADD_GRADLE_PROPERTY" "$file" | jq -c | while read -r line; do
    value=$(jq -r '.value' <<<"$line")
    key=$(jq -r '.key' <<<"$line")
    log "Update $key to $value"
    yq -o props -o props ".$key = \"$value\"" -i "$(realpath "$root/$GRADLE_PROPERTIES")"
  done
}

generate_recipe() {
  if [[ -n "$INTERACTIVE_MODE" ]]; then
    ./gradlew "${gradle_props[@]}" quarkusUpdate "${quarkus_args[@]}" --no-rewrite
  else
    ./gradlew "${gradle_props[@]}" quarkusUpdate "${quarkus_args[@]}" --no-rewrite >/dev/null 2>&1
  fi
  recipe=$(find . -name "rewrite.yaml" | head -n1) || true
  echo "$recipe"
}

quarkus_args=()
gradle_props=("-Dorg.gradle.daemon=false")

if [[ -n "$1" ]]; then
  quarkus_args+=("--platformVersion")
  quarkus_args+=("$1")
  currentVersion="$(yq -p props -o yaml '.quarkusPlatformVersion' gradle.properties)"
  if [[ "$currentVersion" == "$1" ]]; then
    exit 0
  else
    if [[ "$DRY_RUN" == "true" ]]; then
      echo "Quarkus update from $currentVersion to $1"
    fi
  fi
fi

if [[ "$DRY_RUN" != "true" ]]; then
  echo "Quarkus updating to $1 using" "${quarkus_args[@]}"
  # fast_rewrite "$(realpath "$(generate_recipe)")"
  ./gradlew "${gradle_props[@]}" quarkusUpdate "${quarkus_args[@]}" --rewrite
fi
