set positional-arguments := true
DOCKER_CONTAINER := "powerwall-export"
DOCKER_IMAGE_TAG:= `whoami` / DOCKER_CONTAINER + ":latest"
OS_NAME:=`uname -o | tr '[:upper:]' '[:lower:]'`

# show recipes
[private]
@help:
  just --list --list-prefix "  "

# Show proposed release notes
@changelog:
  git cliff --unreleased

# Use Docker to build/run
docker +args="build":
  #!/usr/bin/env bash
  set -eo pipefail

  action=$1
  if [[ "$#" -ne "0" ]]; then shift; fi
  case "$action" in
    build)
      docker build -t  "{{ DOCKER_IMAGE_TAG }}" .
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
          -e NODE_NO_WARNINGS=1 \
          -e TESLA_ADDR="$TESLA_ADDR" \
          -e TESLA_EMAIL="$TESLA_EMAIL" \
          -e TESLA_PASSWORD="$TESLA_PASSWORD" \
          -d \
          "{{ DOCKER_IMAGE_TAG }}"
      ;;
    *)
      echo "Unknown action: $action"
      echo "Try: build | run | logs | stop"
      ;;
  esac

# Requires git@github.com:quotidian-ennui/gitscripts or git@github.com:mcwarman/gitscripts
# Do it locally w/o pushing changes so that we can at least test the changes locally
# first.
# Attempt to do a dependabot-merge
@dependabot:
  git dependabot-merge -f npm_and_yarn


# Do a release
release version="patch" push="localonly": check_npm_env
  #!/usr/bin/env bash
  set -eo pipefail
  npm_tag_name=$(npm version "{{ version }}" --git-tag-version=false)
  modified_files=$(git diff --name-only)
  for file in $modified_files; do
    git add "$file"
  done
  git commit -m"chore(release): mark next version ${npm_tag_name/v/}"
  git tag "${npm_tag_name/v/}"
  if [[ "{{ push }}" == "push" ]]; then
    git push --all
    git push --tags
  fi

# npm run lint
@lint: check_npm_env
  npm run lint

# npm run start
@start: check_tesla_env install
  NODE_NO_WARNINGS=1 npm run start

# NPM install
@install: check_npm_env
  npm install

# Cleanup
@clean:
  rm -rf node_modules
  -docker images | grep -e "^<none>" -e "{{ DOCKER_CONTAINER }}" | awk '{print $3}' | xargs -r docker rmi

[private]
[no-cd]
[no-exit-message]
check_npm_env:
  #!/usr/bin/env bash
  set -eo pipefail

  if [[ "{{ OS_NAME }}" == "msys" ]]; then echo "npm on windows git+bash, are you mad?; abort"; exit 1; fi
  which npm >/dev/null 2>&1 || { echo "npm not found; abort"; exit 1; }

[private]
[no-cd]
[no-exit-message]
check_tesla_env:
  #!/usr/bin/env bash
  set -eo pipefail
  if [[ -z "$TESLA_ADDR" ]]; then echo "TESLA_ADDR not defined; abort"; exit 1; fi
  if [[ -z "$TESLA_EMAIL" ]]; then echo "TESLA_EMAIL not defined; abort"; exit 1; fi
  if [[ -z "$TESLA_PASSWORD" ]]; then echo "TESLA_PASSWORD not defined; abort"; exit 1; fi
