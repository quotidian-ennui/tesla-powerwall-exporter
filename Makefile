.DEFAULT_TARGET: help
.PHONY:  check_env lint start help docker docker-run
.SILENT: lint start check_env upgrade docker docker-run
DOCKER_IMAGE_TAG:=$(USER)/powerwall-export:latest

help:
	@grep -E '^[a-zA-Z_-]+.*:.*?## .*$$' $(word 1,$(MAKEFILE_LIST)) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

check_env:
ifndef WSL_DISTRO_NAME
	$(error Not running node under windows, switch to WSL2)
endif
	@which npm

lint: check_env   ## npm run eslint
	npm run lint

start: check_env   ## npm run start
	npm run start

update: check_env   ## npm upgrade
	npm update

docker:  ## docker build
	docker build . --tag $(DOCKER_IMAGE_TAG)

docker-run: ## run via docker
ifndef TESLA_PASSWORD
	$(error No Password for the Tesla gateway defined)
endif
	docker run -it --rm \
		-e 9961:9961 \
		-e TESLA_ADDR=$(TESLA_ADDR) \
		-e TESLA_EMAIL=$(TESLA_EMAIL) \
		-e TESLA_PASSWORD=$(TESLA_PASSWORD) \
		$(DOCKER_IMAGE_TAG)