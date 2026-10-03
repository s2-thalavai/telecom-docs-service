# Telecom docs service – common tasks. Run `make` or `make help`.
APP_NAME  := telecom-docs-service
VERSION   := 1.0.0
IMAGE     := $(APP_NAME):$(VERSION)
MVN       ?= mvn
KUBECTL   ?= kubectl
BASE_URL  ?= http://localhost:8080
NAMESPACE := telecom-docs

.DEFAULT_GOAL := help
.PHONY: help test docs open-docs verify package openapi run clean docker-build up down nuke logs docs-static \
	    smoke k8s-deploy k8s-status k8s-port-forward k8s-delete

help: ## Show this help
	@grep -E '^[a-zA-Z0-9_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'

# ---------------- Build, tests, docs ----------------
test: ## Unit + documentation tests (writes target/generated-snippets)
	$(MVN) -B test

docs: ## Build the HTML docs -> target/generated-docs/index.html
	$(MVN) -B prepare-package

open-docs: ## Build the docs and open them in a browser
	./scripts/open-docs.sh

verify: ## Everything: tests, docs, packaged jar, coverage check, integration tests
	$(MVN) -B clean verify

package: ## Build the jar (docs included at /docs)
	$(MVN) -B clean package

openapi: ## Optional: also generate docs/openapi3.yaml + Swagger UI (see docs/openapi-integration.md)
	$(MVN) -B -Popenapi clean verify

run: ## Run locally (H2 + sample data); docs at /docs/index.html
	./scripts/run-dev.sh

clean: ## Remove build output
	$(MVN) -B clean

# ---------------- Docker ----------------
docker-build: ## Build the image (runs the documentation tests inside the build)
	docker build -t $(IMAGE) .

env-file:
	@test -f .env || (cp .env.example .env && echo "Created .env from .env.example")

up: env-file ## Start MySQL + app with Docker Compose (prod profile)
	docker compose up -d --build
	@echo "API: $(BASE_URL)/api/v1   Docs: $(BASE_URL)/docs/index.html"

down: ## Stop Compose (keeps the DB volume)
	docker compose down

nuke: ## Stop Compose and delete the DB volume
	docker compose --profile docs down -v

logs: ## Follow app logs
	docker compose logs -f app

docs-static: docs ## Serve only the static HTML docs with nginx on http://localhost:8082
	docker compose --profile docs up -d docs

smoke: ## Check a running instance against the documented behaviour
	@set -a; [ -f .env ] && . ./.env; set +a; BASE_URL=$(BASE_URL) ./scripts/smoke-test.sh

# ---------------- Kubernetes ----------------
k8s-deploy: ## Build, load into local cluster, apply k8s/
	./scripts/k8s-deploy.sh

k8s-status: ## Pods, services, ingress
	$(KUBECTL) -n $(NAMESPACE) get pods,svc,ingress

k8s-port-forward: ## Forward localhost:8080 to the service
	$(KUBECTL) -n $(NAMESPACE) port-forward svc/$(APP_NAME) 8080:80

k8s-delete: ## Delete all K8s resources (including DB data)
	./scripts/k8s-teardown.sh
