# Three-Kid Family Challenge

The casus "Three-Kid Family Challenge" ([doc/casus.md](doc/casus.md); the original PDF is local only, not in Git), rebuilt from scratch with Dave Farley's ATDD way of working and automated quality assurance (AQA): design rules are enforced by tests, not by review. The goal and the approach are in [README.md](README.md); the scenario backlog is in [doc/backlog.md](doc/backlog.md). The original AQA brief is [doc/automated-quality-assurance.md](doc/automated-quality-assurance.md).

## ATDD-profiel

Werkwijze: skill `atdd` (`.claude/skills/atdd/`). This profile holds what is specific to this project.

> **Step 0 (walking skeleton) is done:** the commands below are verified, locally and in CI (seen red once: a failing commit stage skipped the acceptance stage). Commands marked *(not yet verified)* have not run.

**Language:** English, in the code (domain terms), the scenarios, the ADRs, the docs and the commit messages. The skills `atdd` and `uitleg` come from [banksim-mse](https://github.com/edwinbulter/banksim-mse) and stay in Dutch. `atdd` is a copy extended with the AQA guardrails (§4) and Farley's principles on cohesion, idempotency and measuring; it goes back to banksim-mse once the guardrails have run here. `uitleg` differs in one rule: it always explains in Dutch, whatever language this profile prescribes.

**Stack:**
- Java 21 and Spring Boot 4 (Maven wrapper, Jib);
- JUnit 5, jqwik (ADR 0012), ArchUnit and Testcontainers;
- GitHub Actions.

New libraries or tools (PIT, ...) only with an ADR.

**Commands**
- Commit stage: `make commit-stage` (`mvnw verify`: unit, integration and architecture tests; the acceptance module only compiles and runs `FourLayerTests`).
- Build the image: `make image` (Jib, local Docker daemon, `three-kids:<version>`; skips the tests).
- Acceptance tests: `make acceptance` (`image` + `acceptance-tests`).
- One scenario: `./mvnw -B -pl acceptance verify -DskipITs=false -Dapp.version=$(make -s version) -Dit.test=<ScenarioClass>` (build the image first).
- Which version runs: `make version` (short commit hash, `-dirty` with uncommitted changes); `GET /api/version` on the running service.
- Run the service: `make run` (http://localhost:8080); try it by hand with [doc/requests.http](doc/requests.http), top to bottom on a fresh service.
- Pipeline: `gh run list --limit 3` and `gh run watch <id> --exit-status`.
- Measure the time per registration: `make perf` (tag `perf`, excluded from the commit stage; sizes with `PERF_SIZES=1000,10000`). The README holds the results.

**Where things go**

| Change | Where | Test |
| --- | --- | --- |
| Scenario (layer 1) | `acceptance/src/test/java/.../scenarios/` | JUnit 5 against the container |
| DSL (layer 2) | `acceptance/src/test/java/.../dsl/` | via the scenarios |
| Protocol driver (layer 3) | `acceptance/src/test/java/.../drivers/` (interface), `.../drivers/http/` | via the scenarios |
| Domain (family graph, pattern) | `service/src/main/java/nl/kabulter/threekids/domain/` (no Spring) | JUnit, jqwik (`*PropertyTests`) |
| Performance | `FamilyRegisterWorkTests` (work per registration), `FamilyRegisterPropertyTests` (incremental = checking everyone), `RegistrationMeasurement` (`make perf`) | commit stage, except the measurement |
| Application | `.../application/` | JUnit with test doubles |
| API (controllers, DTOs) | `.../api/` | `@WebMvcTest`; `@SpringBootTest` with MockMvc |
| Guardrails | `service/src/test/java/.../architecture/` | `ArchitectureTests` (ArchUnit), `NoSynchronizedBlocksTests` (source scan) |
| Pipeline | `.github/workflows/ci.yml` | seen red once, then green |
| ADR | `doc/adr/NNNN-title.md` | – |

**Guardrails** (skill §4; each one seen red once by sabotage)

| Guardrail | Test | Where |
| --- | --- | --- |
| Layers `api → application → domain`, no cycles | `ArchitectureTests.layers`, `.noCycles` | `architecture/` |
| Domain knows no framework (no `org.springframework..`, `jakarta..`) | `ArchitectureTests.domainKnowsNoFramework` | `architecture/` |
| DTOs stay in `api` | `ArchitectureTests.dtosStayInApi` | `architecture/` |
| No `synchronized` in production code; use `ConcurrentHashMap`, locks or atomics | `ArchitectureTests.noSynchronizedMethods` + `NoSynchronizedBlocksTests` | `architecture/` |
| No `LocalDate.now()`, `LocalDateTime.now()`, `Instant.now()`: use the injected `Clock` | `ArchitectureTests.noSystemClock` | `architecture/` |
| Four layers: scenarios speak only the DSL; drivers make no assertions | `FourLayerTests` | `acceptance/.../architecture/` |
| The acceptance tests test the build that was just made | `ServiceVersionScenarios` | `acceptance/.../scenarios/` |
| Simplest construct; optimise only after a measurement (ADR): matching is incremental since ADR 0011, after `make perf` | `FamilyRegisterWorkTests`, `FamilyRegisterPropertyTests` | `domain/` |
| Mutations at the same time stay consistent: the register handles them one at a time behind a lock (ADR 0011) | `FamilyRegisterConcurrencyTests` | `domain/` |
| Mutations are idempotent: a POST of a known ID replaces, a DELETE of a deleted ID changes nothing | POST: `PeopleServiceTests.registeringTheSameRecordAgainGivesTheSameAnswer`, `.aNewRecordForAKnownIdReplacesTheOldOne`, `UpdateScenarios`; DELETE: `FamilyRegisterTests.deletingTheSameIdTwiceChangesNothing`, `DeletionScenarios` (deletes twice) | `domain/`, `application/`, `scenarios/` |

**Target environment:** a local Docker container and the GitHub Actions runner. No Kubernetes.

**Conventions**
- Commit message headings: **Seen red** (message and reason), **Sabotage** (if done) and **Not verified**.
- One ATDD step per small commit: the evaluation looks at the Git history.
- Never commit or push without permission. A push to `main` starts the pipeline.
- No commit prefix, unless the user names one.

**Pitfalls**
- The image sets `APP_VERSION`, which Spring binds to `app.version` before `application.yml`. Sabotage on the version therefore belongs in the code, not in the yml.
- Commit stage and image are separate: `make image` builds with `-DskipTests`. After a code change use `make acceptance`, never `make acceptance-tests` alone.
- HTTP status 444 is not in Spring's `HttpStatus`: use `ResponseEntity.status(444)`.
- "Under 18" changes as time passes without a POST. Never cache the age check; use the injected `Clock`.
- `DELETE /api/v1/people` carries a JSON body: check that the HTTP driver really sends it.

**Backlog and background**
- Scenario backlog and the domain questions to approve: [doc/backlog.md](doc/backlog.md).
- Progress: the status in `README.md`, the existing scenarios and the ADRs in `doc/adr/`.
- Explaining a part of the system: skill `uitleg` (`/uitleg <topic>`).
