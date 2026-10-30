# 0009. The answer shows the children who name a person, found with an index built once per registration

Date: 2026-10-29
Status: Accepted

## Context
In scenario 5b the children are only written down on one side:

| Record | What it says |
| --- | --- |
| Homer | partner Marge, **no children** |
| Marge | partner Homer, **no children** |
| Bart, Lisa, Maggie | parents Homer and Marge |

By the bidirectional integrity of the casus, Bart, Lisa and Maggie are still Homer's and Marge's children; it is only recorded in the children's records. Homer and Marge must match. That raises two questions.

**1. What does the answer show?** The service returns the matching people.
- Without this decision it returns Homer's record as it was posted: Homer **without** children. That is wrong, because Homer matches precisely because he has three children.
- The casus asks to "respond with everything we know". Decided with the user: the answer shows Homer **with** the children Bart, Lisa and Maggie, taken from the children's records.

**2. How are those children found quickly?** To find Homer's children, the service must know which records say "parent: Homer", and those records can be anywhere.
- Searching all records again for every person costs, with 10,000 people, 10,000 × 10,000 = 100 million look-ups per registration: O(n²).
- Going through all records once and writing down a lookup list (Homer → Bart, Lisa, Maggie; Marge → Bart, Lisa, Maggie) costs about 2 × 10,000 steps per registration: O(n). After that, "who are Homer's children?" is one look-up.

## Decision
- The domain type `People` holds all records of one moment, plus that lookup list: an **index** from a parent to the IDs of the children who name that parent. `PeopleService` builds it once per registration and throws it away afterwards; nothing is kept up to date between registrations (simple, ADR 0005).
- A person's children are first the person's own list, then the children from the index, each child once, in the order of their IDs. So the answer is always the same.
- The service returns the matching people with these children (`People.asKnown`).
- The store still keeps every record exactly as it was posted (ADR 0008). Only the answer adds the children.

This amends ADR 0008 on one point: that ADR returned the records as posted; now the answer shows the derived children.

## Consequences
- Each registration goes through all records twice (build the index, then filter): still O(n), and still a plain stream.
- The answer for a person can differ from the person's own record: it also shows the children who name the person.
- Not shown: the derived **parents** of a matching person. If someone names Homer as a child but Homer names no parents, the answer does not show that parent. No scenario asks for it yet.

## Guard
- `PeopleTests`: the children who name the person, the own list first and no child twice, the order of the IDs, and `asKnown`; each seen red by sabotage.
- `PeopleServiceTests.parentsWhoDoNotListTheirChildrenMatchWithTheChildrenFromTheChildrensRecords` and `IntegrityScenarios.parentsWhoDoNotListTheirChildrenGetThemFromTheChildrensRecords`.
