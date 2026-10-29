# Backlog

One iteration per item, following the `atdd` skill: ⛔ scenario and expected values, seen red for the right reason, outside-in, ⛔ ADR if needed, local verification, ⛔ commit.

## Step 0

Done.

1. **Guardrails.** Maven modules `service` and `acceptance`; ArchUnit rules from the profile, each seen red by sabotage. ⛔ Approval before the walking skeleton.
2. **Walking skeleton.** Scenario "the service reports the version it was built from" in four layers against the Jib image; `Makefile`; GitHub Actions with a commit stage and an acceptance stage, seen red once, then green.

## Phase 1 – matching

1. **Done** (`MatchingScenarios.aSinglePersonWithoutAFamilyDoesNotMatch`): a single unknown person is registered → nobody matches (444, without a body).
2. **Done** (`MatchingScenarios.partnersWithThreeChildrenOneOfThemUnder18Match`, ADR 0005, ADR 0006): Homer and Marge, each with an own record naming the other as partner and the three children, and the three children with both parents; the last registration answers 200 with Homer and Marge and everything in their records. So far the pattern only checks a partner and exactly three children (also unit tests for two and four children).
   - **Next, split off:** Homer known only by ID (Marge names him as partner, the children as parent) also matches, with the derived partner and children. Decided: such a person can match (casus: "you might not know their name or birthdate yet"). Goes with item 5.
3. **Done** (`MatchingScenarios.aThreeKidFamilyWithOnlyAdultChildrenDoesNotMatch`, ADR 0007): the same family, all children 18 or older → nobody matches. Today comes from an injected clock, as the date in Europe/Amsterdam; a child who turns 18 today is no longer under 18.
4. One child has a different second parent → nobody matches. Approved example: the family of scenario 2, but Lisa is registered with the parents Homer and Edna (6) → after Maggie, nobody matches (Homer: Lisa does not name Marge; Marge: Lisa is not her child, the child's own record wins). (Two and four children: done as unit tests with scenario 2.)
5. Bidirectional integrity: the parent names the child but the child does not name the parent, and the reverse → repaired, the family matches.
6. Update (idempotent on the ID): registering a known ID again replaces the old data; registering the same record twice gives the same state; a match can disappear.
7. The response holds everything known about the matching people, also when name or birth date are unknown.

## Phase 2 – purge and ignore list (only if there is time)

1. Delete IDs → they are gone and no longer count for matching. Deleting the same IDs again changes nothing.
2. A later registration of a deleted ID is ignored, for storage and for matching.

## Domain questions (⛔ decided per scenario, not up front)

- Is the partner link symmetric: if A names B, is B's partner A?
- Can a person known only by ID match? **Decided:** yes (see item 2, next).
- Does a child known only by ID count toward "exactly 3"?
- Does a child without a birth date count as under 18? **Decided** with scenario 3: no, its age is unknown (`ThreeKidFamilyTests.aChildWithoutABirthDateDoesNotCountAsUnder18`). The same for a child without a record (`aChildWithoutARecordDoesNotCountAsUnder18`).
- Conflicting parents: a child's own record names two parents, and a third person names it as a child. What wins? **Decided** (scenario 4): the child's own record; the third person does not count it as a child.
- "All matching people": **decided**, only the people who match; the body is a JSON array of their records, in the format of the POST (scenario 2).
- Response body of 444: **decided**, no body (scenario 1).

## Performance (measure first)

- A measurement outside the commit stage registers 10,000 and 100,000 people and reports the time per registration. A plain filter over everyone makes each registration O(n). Only if the numbers are too slow, an ADR proposes something smarter, and a test guards the result against the plain version.
- A concurrency test: parallel registrations keep the store consistent.
