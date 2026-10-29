# Three-Kid Family Challenge

A Spring Boot service for the casus "[Three-Kid Family Challenge](doc/casus.md)", built with **acceptance test-driven development (ATDD)** as described by Dave Farley in *Modern Software Engineering* and *Continuous Delivery*, and with **automated quality assurance**: design rules are enforced by tests in the pipeline, not by review afterwards.

> **Status:** step 0 in progress. The guardrails (ArchUnit rules and a source scan, [ADR 0002](doc/adr/0002-guardrails-as-archunit-tests.md)) run in `./mvnw verify`; each one has been seen red once. Next: the walking skeleton.

## What the service will do

The service receives person records, one per request, and after **each** record decides immediately whether **any** stored person matches this pattern:

1. the person has a partner;
2. the person has **exactly 3 children**, and all 3 list that partner as mother or father;
3. **at least one** of those children is **under 18**.

| Request | Body | Response |
| --- | --- | --- |
| `POST /api/v1/people` | One person: `id`, `name`, `birthDate`, `parent1`, `parent2`, `partner`, `children` (references as `{ "id": n }`) | **200 OK** with all people that currently match. **444 No Response** if nobody matches yet |
| `DELETE /api/v1/people` (phase 2) | An array of IDs | The IDs are deleted and ignored forever, for storage and for matching |

## How it is built

- **Executable examples.** Every behaviour starts as a scenario in domain language with concrete values, approved before the code is built.
- **Double-loop TDD.** A failing acceptance scenario drives red → green → refactor per class, from the controller inwards.
- **Four-layer acceptance tests.** Scenarios → DSL → protocol driver → the real Docker image.
- **Guardrails first.** Before the first feature, ArchUnit rules enforce the layers, a domain without Spring, DTOs that stay in the API, no `synchronized` and no system clock. Each rule has been seen red once. The full list is in the profile in [`CLAUDE.md`](CLAUDE.md).
- **Simple first, measure before optimising.** Matching is a plain filter over all people in memory. It only gets smarter when a measurement shows it must, recorded as an ADR.
- **Small commits.** One iteration is one small commit, with the test that was seen red and what is not verified in the message.
- **The pipeline is the referee.** Commit stage (unit, integration, architecture tests) and an acceptance stage against exactly the image built once.

The way of working is captured as [Claude Code skills](.claude/skills/): [`atdd`](.claude/skills/atdd/README.md) (the procedure, stop points and guardrails) and [`uitleg`](.claude/skills/uitleg/SKILL.md) (explains a part of the system).

## Tech stack

Java 21, Spring Boot 4, Maven, Jib, JUnit 5, ArchUnit, Testcontainers and GitHub Actions.

## Time spent

To be filled in at submission.
