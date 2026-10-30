# Backlog

One iteration per item, following the `atdd` skill: ⛔ scenario and expected values, seen red for the right reason, outside-in, ⛔ ADR if needed, local verification, ⛔ commit.

## Step 0

Done.

1. **Guardrails.** Maven modules `service` and `acceptance`; ArchUnit rules from the profile, each seen red by sabotage. ⛔ Approval before the walking skeleton.
2. **Walking skeleton.** Scenario "the service reports the version it was built from" in four layers against the Jib image; `Makefile`; GitHub Actions with a commit stage and an acceptance stage, seen red once, then green.

## Phase 1 – matching

1. **Done** (`MatchingScenarios.aSinglePersonWithoutAFamilyDoesNotMatch`): a single unknown person is registered → nobody matches (444, without a body).
2. **Done** (`MatchingScenarios.partnersWithThreeChildrenOneOfThemUnder18Match`, ADR 0005, ADR 0006): Homer and Marge, each with an own record naming the other as partner and the three children, and the three children with both parents; the last registration answers 200 with Homer and Marge and everything in their records. So far the pattern only checks a partner and exactly three children (also unit tests for two and four children).
   - **Split off, done with item 7:** Homer known only by ID (Marge names him as partner, the children as parent) also matches, with the derived partner and children. Decided: such a person can match (casus: "you might not know their name or birthdate yet"). Goes with item 5.
3. **Done** (`MatchingScenarios.aThreeKidFamilyWithOnlyAdultChildrenDoesNotMatch`, ADR 0007): the same family, all children 18 or older → nobody matches. Today comes from an injected clock, as the date in Europe/Amsterdam; a child who turns 18 today is no longer under 18.
4. **Done** (`MatchingScenarios.aThreeKidFamilyWhereOneChildHasAnotherSecondParentDoesNotMatch`): one child has a different second parent → nobody matches. Approved example: the family of scenario 2, but Lisa is registered with the parents Homer and Edna (6) → after Maggie, nobody matches (Homer: Lisa does not name Marge; Marge: Lisa is not her child, the child's own record wins). (Two and four children: done as unit tests with scenario 2.)
5. Bidirectional integrity: the parent names the child but the child does not name the parent, and the reverse → repaired, the family matches.
   - 5a, **done** (`IntegrityScenarios.childrenWithoutParentsGetTheirParentsFromTheParentsRecords`, ADR 0008): Homer and Marge name Bart, Lisa and Maggie as children; the three children are registered without parents → after Maggie, Homer and Marge match. Decided: an empty parent slot of a child is filled by a parent who names the child; a filled slot is never overwritten (the child's own record wins).
   - 5b, **done** (`IntegrityScenarios.parentsWhoDoNotListTheirChildrenGetThemFromTheChildrensRecords`, ADR 0009): Homer and Marge are registered as partners without listing children; Bart, Lisa and Maggie are registered with the parents Homer and Marge → after Maggie, Homer and Marge match. Decided: the answer shows the derived children ("everything we know"); the children are found with an index parent → children, built once per registration (O(n)).
6. **Done** (`UpdateScenarios`, `PeopleServiceTests.registeringTheSameRecordAgainGivesTheSameAnswer`, `.aNewRecordForAKnownIdReplacesTheOldOne`): update (idempotent on the ID): registering a known ID again replaces the old data; registering the same record twice gives the same state; a match can disappear.
   - Approved: 6a, the family of scenario 2 matches; Maggie is registered again with exactly the same record → Homer and Marge still match. 6b, the family of scenario 2 matches; Lisa is registered again with the parents Homer and Edna → nobody matches. Both are expected green at once; proven by sabotage.
7. **Done** (`IntegrityScenarios.aPersonKnownOnlyByIdMatchesWithEverythingKnownAboutThem`, ADR 0010): the response holds everything known about the matching people, also when name or birth date are unknown.
   - Approved (with the split-off item 2, "Homer known only by ID"): Marge is registered with partner Homer (1) and the three children, Homer has no record; the children are registered with the parents Homer and Marge → after Maggie, Marge and Homer match; Homer is shown with id 1, partner Marge, children Bart, Lisa and Maggie, no name or birth date. Decided: the partner link is symmetric (if A names B and B has no record or names no partner, B's partner is A; B's own record wins); people known only by ID are candidates too.

## Phase 2 – purge and ignore list (only if there is time)

1. **Done** (`DeletionScenarios.aDeletedPersonNoLongerCountsForMatching`, ADR 0013): delete IDs → they are gone and no longer count for matching, also not through references in other records. Deleting the same IDs again changes nothing. DELETE answers 204.
2. **Done** (`DeletionScenarios.aDeletedPersonWhoIsRegisteredAgainIsIgnored`, ADR 0014): a later registration of a deleted ID is ignored, for storage and for matching. Approved (8b): the family matches, Lisa is deleted; when Lisa is registered again with her old record → nobody matches. Decided: a new record that refers to a deleted ID is stored without that reference.

## Domain questions (⛔ decided per scenario, not up front)

- Is the partner link symmetric: if A names B, is B's partner A? **Decided** (scenario 7, ADR 0010): yes; B's own record wins.
- Can a person known only by ID match? **Decided:** yes (scenario 7, ADR 0010).
- Does a child known only by ID count toward "exactly 3"? **Decided** with scenario 4: no; it cannot name the partner as parent, so the family does not match until all three children are registered (`ThreeKidFamilyTests.aChildWithoutARecordPreventsAMatch`). Scenario 5 (auto-repair) may revisit this.
- Does a child without a birth date count as under 18? **Decided** with scenario 3: no, its age is unknown (`ThreeKidFamilyTests.aChildWithoutABirthDateDoesNotCountAsUnder18`). The same for a child without a record (`aChildWithoutARecordDoesNotCountAsUnder18`).
- Conflicting parents: a child's own record names two parents, and a third person names it as a child. What wins? **Decided** (scenario 4): the child's own record; the third person does not count it as a child.
- "All matching people": **decided**, only the people who match; the body is a JSON array of their records, in the format of the POST (scenario 2).
- Response body of 444: **decided**, no body (scenario 1).

## Performance (measure first)

- **Done:** `make perf` measured the plain filter (O(n) per registration, 3.8 ms with 10,000 people); decided to make matching incremental (ADR 0011): 12 µs with 100,000 people, guarded by a work test and a property test (ADR 0012).
- **Done:** a concurrency test: registrations and deletes from eight threads at once leave the register as if they had run one after the other (`FamilyRegisterConcurrencyTests`; without the lock it fails every time).
