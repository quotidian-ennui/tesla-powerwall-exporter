MAKEFLAGS+= --silent --always-make
.DEFAULT_TARGET: help
USER?=$(shell whoami)

DOCKER_CONTAINER=powerwall-export
DOCKER_IMAGE_TAG:=$(USER)/$(DOCKER_CONTAINER):latest

OS_NAME:=$(shell uname -o | tr '[:upper:]' '[:lower:]' )

help:
	@grep -E '^[a-zA-Z_-]+.*:.*?## .*$$' $(word 1,$(MAKEFILE_LIST)) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

# only care if we're in msys (i.e. windows git-bash)
check_env:
ifeq ($(OS_NAME),msys)
	$(error Not running node under windows, switch to WSL2)
endif
	@which npm

lint: check_env   ## npm run eslint
	npm run lint

start: check_env   ## npm run start
	npm run start

update: check_env   ## npm upgrade
	npm update

changelog:  ## show git changelog
	@git cliff

docker:  ## docker build
	docker build . --tag $(DOCKER_IMAGE_TAG)

docker-run: ## run via docker
ifndef TESLA_PASSWORD
	$(error No Password for the Tesla gateway defined)
endif
	@docker run --name $(DOCKER_CONTAINER) \
		-p 9961:9961 \
		-e NODE_PORT=9961 \
		-e NODE_NO_WARNINGS=1 \
		-e TESLA_ADDR=$(TESLA_ADDR) \
		-e TESLA_EMAIL=$(TESLA_EMAIL) \
		-e TESLA_PASSWORD=$(TESLA_PASSWORD) \
		-d \
		$(DOCKER_IMAGE_TAG)

docker-logs: ## Get logs
	@docker logs $(DOCKER_CONTAINER)

docker-rm:  ## remove docker container
	@docker rm -f $(DOCKER_CONTAINER)
