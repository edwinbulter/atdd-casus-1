# 0006. Every acceptance scenario runs against a fresh service

Date: 2026-10-29
Status: Accepted

## Context
The answer to every registration depends on **all** people stored in the service: "does anyone match?". Unique IDs per scenario therefore do not isolate scenarios. After the scenario that registers a matching family, the scenario "a single person without a family does not match" would get 200 instead of 444 on the same service. Scenario 2 forces the choice.

Options:
1. **A fresh container per scenario.** Starting the image takes about 2 seconds.
2. **One container, scenarios in a fixed order.** Fast, but scenarios depend on each other.
3. **A reset endpoint.** Fast, but test-only code in the service, which the acceptance test model forbids.

## Decision
Every scenario gets its own container of the image under test. The DSL objects (`Registry`, `Operator`) start it through `HttpServiceDriver.onAFreshService()` and stop it when the scenario ends (JUnit `@AutoClose`; JUnit creates a test instance per scenario).

## Consequences
- Scenarios are independent and can run in any order.
- Each scenario costs a container start of about 2 seconds; the acceptance stage grows linearly with the number of scenarios.
- The acceptance scenarios say nothing about many stored people; that needs a separate measurement.

## Guard
- `MatchingScenarios` holds both a matching and a non-matching scenario; on a shared service, one of them fails depending on the order.
- The drivers only offer a service through `HttpServiceDriver.onAFreshService()`; there is no shared container.
