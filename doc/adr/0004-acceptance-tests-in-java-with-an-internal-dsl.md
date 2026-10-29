# 0004. Acceptance tests in Java with an internal DSL, in four layers

Date: 2026-10-29
Status: Accepted

## Context
The way of working (skill `atdd`) uses the four-layer acceptance test model: scenarios in domain language, a DSL, protocol drivers and the real system. The service only has an HTTP API; there is no UI. Options: Gherkin with Cucumber (an extra tool and glue layer), Playwright in TypeScript (a second language and toolchain for an API-only service), or **JUnit 5 in Java with an internal DSL**.

## Decision
- A Maven module `acceptance` with JUnit 5, run by Failsafe only with `-DskipITs=false` (`make acceptance-tests`); in the commit stage it only compiles.
- Layer 1, `scenarios/`: one test method per scenario, with Given/When/Then comments, no URLs, JSON or HTTP.
- Layer 2, `dsl/`: domain methods with the assertions (for example `Operator.seesVersion`).
- Layer 3, `drivers/`: the interface `ServiceDriver` without assertions; `drivers/http/` implements it with the JDK `HttpClient` and starts the image (ADR 0003).

## Consequences
- One language and one build for the service and its acceptance tests.
- Scenarios read as Java, not as plain text; the Given/When/Then comments carry the example.
- A second driver (for example in-process) can be added behind `ServiceDriver` without changing the scenarios.

## Guard
- `FourLayerTests` (ArchUnit, commit stage): scenarios do not use drivers, `java.net` or Jackson; drivers do not use AssertJ, JUnit, the DSL or the scenarios. Both rules seen red by sabotage.
- Failsafe only picks up `**/scenarios/**/*Scenarios.java`.
