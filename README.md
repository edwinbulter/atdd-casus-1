# Three-Kid Family Challenge

A Spring Boot service for the casus "[Three-Kid Family Challenge](doc/casus.md)", built with **acceptance test-driven development (ATDD)** as described by Dave Farley in *Modern Software Engineering* and *Continuous Delivery*, and with **automated quality assurance**: design rules are enforced by tests in the pipeline, not by review afterwards.

> **Status:** step 0, the walking skeleton. The service reports the version it was built from (`GET /api/version`), checked by an acceptance scenario against the Docker image of that build. The guardrails ([ADR 0002](doc/adr/0002-guardrails-as-archunit-tests.md)) run in the commit stage; each one has been seen red once. The pipeline in GitHub Actions is green, and has been seen red once (a failing commit stage skips the acceptance stage). Phase 1 has started: registering a single person without a family answers **444** without a body; partners with three children answer **200** with the matching people, kept in memory and checked with a plain filter ([ADR 0005](doc/adr/0005-people-in-memory-checked-with-a-plain-filter.md)). The pattern checks a partner, exactly three children who all name that partner as parent, and at least one child under 18 today, the date in Europe/Amsterdam from an injected clock ([ADR 0007](doc/adr/0007-today-is-the-date-in-amsterdam-from-an-injected-clock.md)). Bidirectional integrity: an empty parent slot of a child is repaired from a parent who lists the child, when matching; when records conflict, the child's own record wins ([ADR 0008](doc/adr/0008-relations-are-repaired-when-matching-not-when-storing.md)). A parent who does not list the children gets them from the children's records, and the answer shows them ([ADR 0009](doc/adr/0009-the-answer-shows-the-children-who-name-a-person.md)). Posting a known ID again replaces the old record: the same record again changes nothing, a new one can end a match. A person known only by ID can match too, with a symmetric partner link, and is shown with everything known about them ([ADR 0010](doc/adr/0010-people-known-only-by-id-can-match-with-a-symmetric-partner.md)). Phase 1 is complete, and matching is incremental (see Performance). Next: phase 2 (delete and ignore list).

## Getting started

```bash
make help            # all targets
make commit-stage    # unit, integration and architecture tests
make acceptance      # build the image of this commit and run the scenarios against it (needs Docker)
make run             # the service on http://localhost:8080
```

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

## Performance

Measured first, then optimised. Measured with `make perf`: in-process (the application service, without HTTP), on an Apple M1 Max. The population consists of families of five (two partners, three children); in ten families a child is under 18, so 20 people match. Each row registers that many people one by one into an empty service.

| People registered | Check everyone ([ADR 0005](doc/adr/0005-people-in-memory-checked-with-a-plain-filter.md)): total, average of the last 100 | Incremental ([ADR 0011](doc/adr/0011-incremental-matching.md)): total, average of the last 100 |
| ---: | ---: | ---: |
| 1,000 | 0.5 s, 597 µs | 0.1 s, 52 µs |
| 10,000 | 18.9 s, 3,824 µs | 0.2 s, 14 µs |
| 100,000 | not measured (estimated ~30 minutes, ~38 ms) | 1.2 s, 12 µs |

Checking everyone, the work of a registration grew with the number of people stored. Incremental, a registration only checks the people whose match it can change, plus the people who matched (they can stop matching as time passes, and they are all in the answer anyway); the time stays the same however many people are stored (the first row includes the warm-up of the JVM). Two limits remain:
- the answer contains all matching people, as the casus asks, so building it grows with the number of people that match;
- registrations run one at a time behind a lock, so throughput is bounded by one thread; HTTP and JSON add their own cost per request.

The pipeline does not measure time, which is unreliable on shared runners. It guards the design instead: `FamilyRegisterWorkTests` counts the people checked per registration, and the property test `FamilyRegisterPropertyTests` (jqwik, [ADR 0012](doc/adr/0012-property-tests-with-jqwik.md)) checks that the incremental answer equals checking everyone.

## Tech stack

Java 21, Spring Boot 4, Maven, Jib, JUnit 5, jqwik, ArchUnit, Testcontainers and GitHub Actions.

## Time spent

To be filled in at submission.
