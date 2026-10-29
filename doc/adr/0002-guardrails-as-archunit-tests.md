# 0002. Enforce the guardrails with ArchUnit tests in the commit stage

Date: 2026-10-29
Status: Accepted

## Context
Design rules are not checked by review afterwards but enforced by tests (automated quality assurance, see `doc/automated-quality-assurance.md`). Before the first feature we need rules for: the layers `api → application → domain` without cycles, a domain without Spring, DTOs that stay in `api`, no `synchronized`, and no system clock. Options:
- **ArchUnit:** rules in plain JUnit, on the compiled classes, in the same `mvnw verify`.
- **Java modules or Maven modules per layer:** the compiler enforces dependencies, but not `synchronized` or the clock, and it costs a module per layer for a small service.
- **Checkstyle or PMD:** source rules, but layer dependencies are hard to express.

## Decision
- ArchUnit (`archunit-junit5`), with the rules in `service/src/test/java/.../architecture/ArchitectureTests.java`, run by Surefire in the commit stage.
- ArchUnit sees a `synchronized` method but not a `synchronized` block. A plain JUnit test (`NoSynchronizedBlocksTests`) scans `src/main/java` for the keyword. No extra tool.
- ArchUnit fails a rule that checks no classes (`failOnEmptyShould`, default on). The rules about one layer allow an empty layer (`allowEmptyShould(true)`, `withOptionalLayers(true)`), because `domain` and `application` stay empty until the first scenario needs them. The rules about all classes stay strict, so a wrong root package turns them red.

## Consequences
- A violation fails the build with the class and line, before any review.
- Each rule has been seen red once, by a temporary sabotage class in the real package (not committed).
- The rules for one layer cannot detect that the layer pattern itself is mistyped; the sabotage run is the evidence that they match.
- The source scan also matches `synchronized` in a comment or string; that is accepted, it is cheap to rephrase.

## Guard
`ArchitectureTests` and `NoSynchronizedBlocksTests` in the commit stage. Weakening, excluding or removing a rule needs a new ADR (skill `atdd`, hard rules).
