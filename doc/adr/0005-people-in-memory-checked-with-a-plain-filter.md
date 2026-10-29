# 0005. People are kept in memory, and every registration checks everyone with a plain filter

Date: 2026-10-29
Status: Accepted

## Context
Scenario "partners with three children, one of them under 18, match" needs the service to remember people between registrations, and to tell after each registration who matches. The casus forbids filtering in SQL ("all filtering in Java"). The guardrails ask for the simplest construct, no smart algorithms up front, and concurrency through Java's concurrent data structures, not `synchronized` (skill `atdd` §4).

Options for the store: a database with the filter in Java (an extra component, and the data would be loaded into memory anyway), or **a map in memory**. Options for matching: **check every stored person after each registration**, or re-check only the family of the registered person (incremental, with indexes).

## Decision
- `PeopleService` keeps people in a `ConcurrentHashMap<Long, Person>`, by ID. Registering a known ID replaces the old record.
- After each registration it filters all stored people with `ThreeKidFamily.matches`, a pure domain function, as a stream.
- No incremental matching or indexes until a measurement shows the plain filter is too slow (backlog, "Performance").

## Consequences
- Simple and readable: the rule is in one place in the domain, the service only stores and filters.
- Every registration costs O(n) in the number of stored people. A measurement decides whether that is acceptable.
- Data is lost when the service stops; the casus asks for nothing else.
- `put` and the filter are not one atomic step: a registration that runs in parallel may or may not be in the answer of another one. Each answer is a consistent view of a moment; no lock is needed for that.
- The store lives in a singleton bean: integration tests that share a Spring context share the people.

## Guard
- `ArchitectureTests`: no `synchronized`; the domain knows no Spring.
- `PeopleServiceTests.partnersWithThreeChildrenMatchOnceTheFamilyIsRegistered` fails when the service only checks the registered person instead of everyone (seen red by sabotage).
- A performance measurement (backlog) before any optimisation; an optimisation needs a new ADR that replaces this one.
