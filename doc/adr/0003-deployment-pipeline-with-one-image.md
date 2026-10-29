# 0003. A deployment pipeline that builds one image and tests exactly that image

Date: 2026-10-29
Status: Accepted

## Context
The walking skeleton needs a target environment and a pipeline. The casus asks for no deployment; the profile names a local Docker container and the GitHub Actions runner, no Kubernetes. The acceptance scenarios must run against the real, packaged service, and must be able to tell which build they test.

Options for the image: a `Dockerfile` (needs a Docker build step and a base image to maintain), Spring Boot buildpacks (slow, large), or **Jib** (Maven plugin, no Dockerfile, reproducible layers). Options for starting it in the tests: a shell script with `docker run`, Docker Compose, or **Testcontainers** (starts and stops the container from the test, maps a free port).

## Decision
- **Jib** builds `three-kids:<version>` on a distroless Java 21 base (non-root, pinned by digest). `<version>` is the short commit hash (`make version`), with `-dirty` for uncommitted work, and the image passes it to the service as `APP_VERSION`.
- **Testcontainers** starts that image for the acceptance scenarios.
- **GitHub Actions, two stages.** The commit stage runs `make commit-stage` and builds the image once; the acceptance stage starts only when the commit stage is green, loads exactly that image and runs `make acceptance-tests`.
- Locally and in CI the same `make` targets run.

## Consequences
- The scenario `ServiceVersionScenarios` checks that the running service reports the version of the build under test, so an old image cannot pass unnoticed.
- `make image` skips the tests; locally `make acceptance` builds a fresh image first. `make acceptance-tests` alone tests whatever image carries the current version.
- Acceptance tests need a Docker daemon.
- The image goes from one job to the next as an artifact (`docker save`/`docker load`); there is no registry.

## Guard
- `ServiceVersionScenarios` fails when the service reports another version than the build under test (seen red by sabotage).
- In `.github/workflows/ci.yml` the acceptance stage `needs` the commit stage; seen red once with a failing commit stage and a skipped acceptance stage.
