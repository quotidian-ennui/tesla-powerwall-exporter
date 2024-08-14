set positional-arguments := true

LOCAL_DOCKER_CONTAINER:= "powerwall-export"
DOCKER_PUBLIC_IMAGE_LATEST:= "ghcr.io/quotidian-ennui/tesla-powerwall-exporter:latest"
DOCKERFILE:= justfile_directory() / "src/main/docker/Dockerfile.jvm"
DOCKERFILE_UBER:= justfile_directory() / "src/main/docker/Dockerfile.jvm-uber"
DOCKERFILE_NATIVE:= justfile_directory() / "src/main/docker/Dockerfile.native-micro"
DOCKERFILE_CGR:= justfile_directory() / "src/main/docker/Dockerfile.cgr"
DOCKER_IMAGE_TAG := `whoami` / LOCAL_DOCKER_CONTAINER + ":latest"
OS_NAME:=`uname -o | tr '[:upper:]' '[:lower:]'`
GRADLE_NATIVE_OPTS:="-Dquarkus.package.jar.enabled=false -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=docker"
GRADLE_UBER_OPTS:="-Dquarkus.package.jar.enabled=true -Dquarkus.package.jar.type=uber-jar"


# show recipes
[private]
@help:
  just --list --list-prefix "  "

# Show proposed release notes
changelog:
  #!/usr/bin/env bash
  tag=$(./gradlew -Dorg.gradle.console=plain printVersion 2>/dev/null | grep -Eo "[0-9]+\.[0-9]+\.[0-9]+.*$")
  git cliff --unreleased --tag "$tag"

# Use Docker to build/run
docker $action="help": check_tesla_env
  #!/usr/bin/env bash
  # shellcheck disable=SC2068
  # shellcheck disable=SC1083
  # shellcheck disable=SC2154
  set -eo pipefail

  docker_args=()
  docker_args+=("-p 9961:9961")
  docker_args+=("-e QUARKUS_HTTP_PORT=9961")
  docker_args+=("-e TESLA_ADDR=$TESLA_ADDR")
  docker_args+=("-e TESLA_BACKUP_ADDR=${TESLA_BACKUP_ADDR:-$TESLA_ADDR}")
  docker_args+=("-e TESLA_EMAIL=$TESLA_EMAIL")
  docker_args+=("-e TESLA_PASSWORD=$TESLA_PASSWORD")
  docker_args+=("-e LOG_LEVEL=DEBUG")
  if [[ "$#" -ne "0" ]]; then shift; fi
  case "$action" in
    build|jvm)
      ./gradlew build
      docker build --pull -t "{{ DOCKER_IMAGE_TAG }}" -f "{{ DOCKERFILE }}" .
      ;;
    uber)
      ./gradlew build {{ GRADLE_UBER_OPTS}}
      docker build --pull -t "{{ DOCKER_IMAGE_TAG }}" -f "{{ DOCKERFILE_UBER }}" .
      ;;
    native)
      ./gradlew build {{ GRADLE_NATIVE_OPTS }}
      docker build --pull -t  "{{ DOCKER_IMAGE_TAG }}" -f "{{ DOCKERFILE_NATIVE }}" .
      ;;
    chainguard|cgr)
      ./gradlew build {{ GRADLE_UBER_OPTS}}
      docker build --pull -t  "{{ DOCKER_IMAGE_TAG }}" -f "{{ DOCKERFILE_CGR }}" .
      ;;
    latest)
      just check_tesla_env
      docker pull "{{ DOCKER_PUBLIC_IMAGE_LATEST }}"
      docker run --rm -it --name "{{ LOCAL_DOCKER_CONTAINER }}" \
          ${docker_args[@]} \
          "{{ DOCKER_PUBLIC_IMAGE_LATEST }}"
      ;;
    latest-native)
      just check_tesla_env
      docker pull "{{ DOCKER_PUBLIC_IMAGE_LATEST }}-native"
      docker run --rm -it --name "{{ LOCAL_DOCKER_CONTAINER }}" \
          ${docker_args[@]} \
          "{{ DOCKER_PUBLIC_IMAGE_LATEST }}-native"
      ;;
    run|start)
      just check_tesla_env
      docker run --rm -it --name "{{ LOCAL_DOCKER_CONTAINER }}" \
          ${docker_args[@]} \
          "{{ DOCKER_IMAGE_TAG }}"
      ;;
    *)
      echo "Unknown action: $action"
      echo ""
      echo "just docker jvm | native | chainguard which match the Dockerfile files in src/main/docker/"
      echo "just docker run | start to start a previously built container"
      echo "just docker latest to run the public latest image"
      echo "just docker latest-native to run the public latest native image"
      echo ""
      exit 0
      ;;
  esac

# only native is useful, others here for completeness
# Publish a snapshot image to ghcr.io
publish $type="native": clean
  #!/usr/bin/env bash
  # shellcheck disable=SC1083
  # shellcheck disable=SC2154
  set -eo pipefail

  _giturl_to_base () {
    local url=$1
    url=${url%%.git}
    url=${url#*github.com:}
    url=${url#*github.com/}
    echo "$url"
  }

  gitRemote=$(git remote get-url origin 2>/dev/null | grep "github.com") || true
  imageName="ghcr.io/$(_giturl_to_base "$gitRemote")"
  imageTag="$(git rev-parse --short HEAD)-$type"
  if [[ "$#" -ne "0" ]]; then shift; fi
  case "$type" in
    jvm)
      ./gradlew build
      docker build --pull -t "$imageName:$imageTag" -f "{{ DOCKERFILE }}" .
      ;;
    native)
      ./gradlew build {{ GRADLE_NATIVE_OPTS }}
      docker build --pull -t  "$imageName:$imageTag" -f "{{ DOCKERFILE_NATIVE }}" .
      ;;
    chainguard)
      ./gradlew build {{ GRADLE_UBER_OPTS }}
      docker build --pull -t  "$imageName:$imageTag" -f "{{ DOCKERFILE_CGR }}" .
      ;;
    *)
      echo "Unknown type: $type"
      echo ""
      echo "Publish a docker image to ghcr.io using the current git ref"
      echo ""
      echo "just publish jvm | native | chainguard which match the Dockerfile files in src/main/docker/"
      exit 0
      ;;
  esac
  docker push "$imageName:$imageTag"

# Tag & release
release $push="localonly":
  #!/usr/bin/env bash
  # shellcheck disable=SC1083
  # shellcheck disable=SC2154
  set -eo pipefail

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
  -docker images | grep -e "<none>" -e "{{ LOCAL_DOCKER_CONTAINER }}" | awk '{print $3}' | xargs -r docker rmi

# Do a build perhaps in the style of jar|uber|native
build $style="uber":
  #!/usr/bin/env bash
  # shellcheck disable=SC1083
  # shellcheck disable=SC2154
  set -eo pipefail

  case "$style" in
    jar)
      ./gradlew build
      ;;
    uber)
      ./gradlew build {{ GRADLE_UBER_OPTS }}
      ;;
    native)
      ./gradlew build {{ GRADLE_NATIVE_OPTS }}
      ;;
    *)
      echo "Unknown build style: $style"
      echo "just build jar | uber | native"
      ;;
  esac

# ./gradlew spotlessApply
@fmt:
  ./gradlew -PdisableSpotlessJava=false spotlessApply

# ./gradlew check (with spotlessApply)
@check:
 ./gradlew -PdisableSpotlessJava=false spotlessApply check

# ./gradlew test
@test:
  ./gradlew test

# ./gradlew quarkusDev
@dev: check_tesla_env
  ./gradlew -Dorg.gradle.console=plain quarkusDev

# ./gradlew quarkusRun
@run: check_tesla_env
  ./gradlew -Dorg.gradle.daemon=false -Dorg.gradle.console=plain quarkusRun

[private]
[no-cd]
[no-exit-message]
check_tesla_env:
  #!/usr/bin/env bash
  set -eo pipefail
  if [[ -z "$TESLA_ADDR" ]]; then echo "TESLA_ADDR not defined; abort"; exit 1; fi
  if [[ -z "$TESLA_EMAIL" ]]; then echo "TESLA_EMAIL not defined; abort"; exit 1; fi
  if [[ -z "$TESLA_PASSWORD" ]]; then echo "TESLA_PASSWORD not defined; abort"; exit 1; fi
