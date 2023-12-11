set positional-arguments := true
DOCKER_CONTAINER := "powerwall-export"
DOCKERFILE := justfile_directory() / "src/main/docker/Dockerfile.jvm"
DOCKER_IMAGE_TAG := `whoami` / DOCKER_CONTAINER + ":latest"
OS_NAME:=`uname -o | tr '[:upper:]' '[:lower:]'`

# show recipes
[private]
@help:
  just --list --list-prefix "  "

# Show proposed release notes
@changelog:
  git cliff --unreleased

# Use Docker to build/run
docker +args="build": check_tesla_env
  #!/usr/bin/env bash
  set -eo pipefail

  action=$1
  if [[ "$#" -ne "0" ]]; then shift; fi
  case "$action" in
    build)
      ./gradlew build
      docker build -t  "{{ DOCKER_IMAGE_TAG }}" -f "{{ DOCKERFILE }}" .
      ;;
    stop)
    	docker rm -f "{{ DOCKER_CONTAINER }}"
      ;;
    logs)
      docker logs -f "{{ DOCKER_CONTAINER }}"
      ;;
    run)
      just check_tesla_env
      docker run --name "{{ DOCKER_CONTAINER }}" \
          -p 9961:9961 \
          -e NODE_PORT=9961 \
          -e TESLA_ADDR="$TESLA_ADDR" \
          -e TESLA_EMAIL="$TESLA_EMAIL" \
          -e TESLA_PASSWORD="$TESLA_PASSWORD" \
          -e LOG_LEVEL=DEBUG \
          -d \
          "{{ DOCKER_IMAGE_TAG }}"
      ;;
    *)
      echo "Unknown action: $action"
      echo "Try: build | run | logs | stop"
      ;;
  esac

# # Do a release
# release version="patch" push="localonly": check_npm_env
#   #!/usr/bin/env bash
#   set -eo pipefail
#   npm_tag_name=$(npm version "{{ version }}" --git-tag-version=false)
#   modified_files=$(git diff --name-only)
#   for file in $modified_files; do
#     git add "$file"
#   done
#   git commit -m"chore(release): mark next version ${npm_tag_name/v/}"
#   git tag "${npm_tag_name/v/}"
#   if [[ "{{ push }}" == "push" ]]; then
#     git push --all
#     git push --tags
#   fi

# Cleanup
@clean:
  rm -rf node_modules
  ./gradlew clean
  -docker images | grep -e "^<none>" -e "{{ DOCKER_CONTAINER }}" | awk '{print $3}' | xargs -r docker rmi

@build:
  ./gradlew build

@dev: check_tesla_env
  ./gradlew -Dorg.gradle.console=plain quarkusDev


[private]
[no-cd]
[no-exit-message]
check_tesla_env:
  #!/usr/bin/env bash
  set -eo pipefail
  if [[ -z "$TESLA_ADDR" ]]; then echo "TESLA_ADDR not defined; abort"; exit 1; fi
  if [[ -z "$TESLA_EMAIL" ]]; then echo "TESLA_EMAIL not defined; abort"; exit 1; fi
  if [[ -z "$TESLA_PASSWORD" ]]; then echo "TESLA_PASSWORD not defined; abort"; exit 1; fi
