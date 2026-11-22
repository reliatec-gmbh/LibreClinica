SHELL := /bin/bash
REGISTRY ?= ghcr.io
IMAGE_NAME ?= mutexio/libreclinica
DEFAULT_BRANCH ?= lc-develop
BRANCH_NAME ?= $(shell git rev-parse --abbrev-ref HEAD 2>/dev/null)
PLATFORMS ?= linux/amd64,linux/arm64
HOST_ARCH := $(shell uname -m)
NATIVE_PLATFORM ?= linux/$(shell echo $(HOST_ARCH) | sed -e 's/^x86_64$$/amd64/' -e 's/^aarch64$$/arm64/')
PUSH ?= true

.PHONY: ci help

ci:
	@set -euo pipefail; \
	if [ -z "$(BRANCH_NAME)" ]; then \
		echo "Unable to determine branch name"; \
		exit 1; \
	fi; \
	if [ -z "$(IMAGE_NAME)" ]; then \
		echo "Set IMAGE_NAME to continue"; \
		exit 1; \
	fi; \
	TAGS="--tag $(REGISTRY)/$(IMAGE_NAME):$(BRANCH_NAME)"; \
	if [ "$(BRANCH_NAME)" = "$(DEFAULT_BRANCH)" ]; then \
		TAGS="$$TAGS --tag $(REGISTRY)/$(IMAGE_NAME):latest"; \
	fi; \
	if [ "$(PUSH)" = "true" ]; then \
		BUILD_FLAG="--push"; \
		BUILD_PLATFORMS="$(PLATFORMS)"; \
	else \
		BUILD_FLAG="--load"; \
		BUILD_PLATFORMS="$(NATIVE_PLATFORM)"; \
	fi; \
	docker buildx build --platform $$BUILD_PLATFORMS $$TAGS $$BUILD_FLAG .

help:
	@echo "Targets:"
	@echo "  ci    Build $(PLATFORMS) and push $(REGISTRY)/$(IMAGE_NAME) (requires docker login ghcr.io)"
	@echo "        PUSH=false builds the native platform only and loads it into Docker"
	@echo "  help  Show this help"
