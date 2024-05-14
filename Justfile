set positional-arguments := true
DOCKER_CONTAINER:= "powerwall-export"
DOCKERFILE:= justfile_directory() / "src/main/docker/Dockerfile.jvm"
DOCKERFILE_NATIVE:= justfile_directory() / "src/main/docker/Dockerfile.native"
DOCKERFILE_CGR:= justfile_directory() / "src/main/docker/Dockerfile.cgr"

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
docker action="build":
  #!/usr/bin/env bash
  set -eo pipefail

  action="{{ action }}"
  if [[ "$#" -ne "0" ]]; then shift; fi
  case "$action" in
    build)
      ./gradlew build
      docker build --pull -t "{{ DOCKER_IMAGE_TAG }}" -f "{{ DOCKERFILE }}" .
      ;;
    native)
      ./gradlew build -Dquarkus.package.jar.enabled=false -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=docker
      docker build --pull -t  "{{ DOCKER_IMAGE_TAG }}" -f "{{ DOCKERFILE_NATIVE }}" .
      ;;
    chainguard|cgr)
      ./gradlew build -Dquarkus.package.jar.enabled=true -Dquarkus.package.jar.type=uber-jar
      docker build --pull -t  "{{ DOCKER_IMAGE_TAG }}" -f "{{ DOCKERFILE_CGR }}" .
      ;;
    run|start)
      just check_tesla_env
      docker run --rm --name "{{ DOCKER_CONTAINER }}" \
          -p 9961:9961 \
          -e QUARKUS_HTTP_PORT=9961 \
          -e TESLA_ADDR="$TESLA_ADDR" \
          -e TESLA_EMAIL="$TESLA_EMAIL" \
          -e TESLA_PASSWORD="$TESLA_PASSWORD" \
          -e LOG_LEVEL=DEBUG \
          "{{ DOCKER_IMAGE_TAG }}"
      ;;
    *)
      echo "Unknown action: $action"
      echo "Try: build | run | native | chainguard"
      ;;
  esac

# Tag & release
release push="localonly":
  #!/usr/bin/env bash
  set -eo pipefail

  push="{{ push }}"
  tag=$(./gradlew -Dorg.gradle.console=plain releaseVersion 2>/dev/null | grep -Eo "[0-9]+\.[0-9]+\.[0-9]+$")
  echo "Release: $tag"
  git tag -a "$tag" -m "release: $tag"
  case "$push" in
    push|github)
      git push --all
      git push --tags
      ;;
    *)
      ;;
  esac

# Cleanup
@clean:
  rm -rf node_modules build bin
  -docker images | grep -e "^<none>" -e "{{ DOCKER_CONTAINER }}" | awk '{print $3}' | xargs -r docker rmi

# ./gradlew build
@build:
  ./gradlew build

# ./gradlew quarkusDev
@dev: check_tesla_env
  ./gradlew -Dorg.gradle.console=plain quarkusDev

# ./gradlew quarkusRun
@run: check_tesla_env
  ./gradlew -Dorg.gradle.console=plain build quarkusRun

[private]
[no-cd]
[no-exit-message]
check_tesla_env:
  #!/usr/bin/env bash
  set -eo pipefail
  if [[ -z "$TESLA_ADDR" ]]; then echo "TESLA_ADDR not defined; abort"; exit 1; fi
  if [[ -z "$TESLA_EMAIL" ]]; then echo "TESLA_EMAIL not defined; abort"; exit 1; fi
  if [[ -z "$TESLA_PASSWORD" ]]; then echo "TESLA_PASSWORD not defined; abort"; exit 1; fi
