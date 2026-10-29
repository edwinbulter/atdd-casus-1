# 0008. Missing relations are repaired when matching, not in the stored records

Date: 2026-10-29
Status: Accepted

## Context
The casus asks for bidirectional integrity: if A says B is a child, B must list A as a parent ("enforce or auto-repair"). Scenario 5a registers children without parents; their parents name them, and the family must match. Decided with the user: an empty parent slot of a child is filled by a parent who lists the child; a filled slot is never overwritten (the child's own record wins).

Options:
1. **Repair on write:** on each registration, update the stored records of the people it names (and of the people that name it). The store is always repaired, but a later update (posting an ID again) must undo old repairs, and one registration changes several records.
2. **Repair when matching:** the store keeps every record exactly as registered; `ThreeKidFamily` reads a child's parents as its own slots plus the parents who list it, within the empty slots.
3. **Enforce:** reject a registration that contradicts stored records. The casus allows it, but references to people not seen yet are normal, so most registrations would be incomplete for a while.

## Decision
Option 2. The store holds the records as registered. `ThreeKidFamily.matches` repairs locally: a child counts as a child of the person when it names the person or has an empty slot; it has the partner as parent when it names the partner, or the partner lists it and the empty slots are enough for every missing parent.

## Consequences
- A registration changes exactly one record; posting an ID again replaces it, and nothing derived has to be cleaned up.
- The repair costs a lookup of the partner's record per child, so matching stays O(n) per registration (ADR 0005).
- The answer returns the matching people's records as registered; repaired relations are not shown. If a scenario asks for derived relations in the answer, that needs its own decision.
- The reverse direction (a child names a parent who does not list the child) is not repaired yet (backlog 5b).

## Guard
`ThreeKidFamilyTests`: `childrenWithoutParentsAreRepairedFromBothParentsRecords`, `anEmptySecondSlotIsRepairedFromThePartnersRecord`, `anEmptySlotIsNotRepairedByAPartnerWhoDoesNotNameTheChild`, `oneEmptySlotCannotTakeBothParents` and `aChildWhoseOwnRecordNamesOtherParentsIsNotThePersonsChild`, each seen red by sabotage; `IntegrityScenarios` end to end.
