# Backlog

One iteration per item, following the `atdd` skill: ⛔ scenario and expected values, seen red for the right reason, outside-in, ⛔ ADR if needed, local verification, ⛔ commit.

## Step 0

Done.

1. **Guardrails.** Maven modules `service` and `acceptance`; ArchUnit rules from the profile, each seen red by sabotage. ⛔ Approval before the walking skeleton.
2. **Walking skeleton.** Scenario "the service reports the version it was built from" in four layers against the Jib image; `Makefile`; GitHub Actions with a commit stage and an acceptance stage, seen red once, then green.

## Phase 1 – matching

1. **Done** (`MatchingScenarios.aSinglePersonWithoutAFamilyDoesNotMatch`): a single unknown person is registered → nobody matches (444, without a body).
2. A complete family through successive registrations: a parent with a partner and three children, both parents on every child, one child under 18 → the last registration matches (200 with the matching people). Introduces the in-memory store and the plain matcher.
3. The same family, all children 18 or older → nobody matches. Introduces the injected clock.
4. One child has a different second parent → nobody matches. Two and four children as unit tests ("exactly 3").
5. Bidirectional integrity: the parent names the child but the child does not name the parent, and the reverse → repaired, the family matches.
6. Update (idempotent on the ID): registering a known ID again replaces the old data; registering the same record twice gives the same state; a match can disappear.
7. The response holds everything known about the matching people, also when name or birth date are unknown.

## Phase 2 – purge and ignore list (only if there is time)

1. Delete IDs → they are gone and no longer count for matching. Deleting the same IDs again changes nothing.
2. A later registration of a deleted ID is ignored, for storage and for matching.

## Domain questions (⛔ decided per scenario, not up front)

- Is the partner link symmetric: if A names B, is B's partner A?
- Does a child known only by ID count toward "exactly 3"?
- Does a child without a birth date count as under 18?
- Conflicting parents: a child's own record names two parents, and a third person names it as a child. What wins?
- "All matching people": only the people who match, or also their partners and children?
- Response body of 444: **decided**, no body (scenario 1).

## Performance (measure first)

- A measurement outside the commit stage registers 10,000 and 100,000 people and reports the time per registration. A plain filter over everyone makes each registration O(n). Only if the numbers are too slow, an ADR proposes something smarter, and a test guards the result against the plain version.
- A concurrency test: parallel registrations keep the store consistent.
