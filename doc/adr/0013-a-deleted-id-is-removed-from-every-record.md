# 0013. A deleted ID is removed from every record that refers to it

Date: 2026-10-30
Status: Accepted

## Context
This ADR is about one question: **what happens to the references to someone who is deleted?**

Phase 2 of the casus: `DELETE /api/v1/people` with an array of IDs. The service must delete those IDs and ignore them, for storage **and** for matching. Decided with the user: a deleted ID counts **nowhere** any more: not as a partner, not as a parent, not as a child, and not as a person known only by ID (ADR 0010).

**The situation.** Take the family of Homer and Marge, and delete **Marge (id 2)**. Her own record can go, but other records still refer to her:

| Record | Refers to Marge as… |
| --- | --- |
| Homer | partner |
| Bart, Lisa, Maggie | parent |

**The problem if only her record goes.** Since ADR 0010 someone without a record can still match, through what the others say about them. If only Marge's record disappears, Homer still names her as partner, and the children still name her as parent. Marge would then **match as a person known only by ID**, while she is deleted. That is exactly what the casus forbids.

**Two ways to solve it:**
1. **Filter when reading.** Every record stays as it was posted, and every place that reads a reference (partner, parents, children, the lookup lists, the answer) skips deleted IDs. The risk: forget one place and the answer is wrong, without an error.
2. **Clean up when deleting.** At the DELETE, the records that refer to Marge are stored again **without** that reference: Homer then has no partner, and Bart, Lisa and Maggie only have Homer as parent. After that, ID 2 appears nowhere, and the rest of the code needs no knowledge of deletion.

## Decision
- **Way 2.** `People.delete(id)` removes the record, and stores every record that refers to the ID again via `Person.withoutReferenceTo(id)`. The lookup lists of ADR 0011 tell at once which records refer to the ID (who names it as partner, as parent, as child), so the clean-up costs the number of references, not the number of people stored.
- **A DELETE checks the affected people at once**, as a POST does. A DELETE can end a match (Lisa goes, Homer and Marge have two children left) but also **start** one (someone with four children loses one and has exactly three).
- **DELETE answers 204 No Content**, without a body. Who matches shows at the next POST.

This amends ADR 0008 on one point: the store keeps every record as it was posted, **except for references to deleted IDs**, which are removed.

## Consequences
- The pattern, the lookup lists and the answer need no knowledge of deletion at all.
- A delete costs the number of records that refer to the ID.
- Deleting the same ID twice changes nothing: the second time there is no record and no reference left.
- **Not yet (scenario 8b):** if Marge is posted again after the DELETE, or a new record names her as parent, that is not ignored yet.

## Guard
- `FamilyRegisterTests`: `aDeletedChildNoLongerCounts`, `aDeletedPersonDoesNotMatchAsAPersonKnownOnlyById`, `deletingAFourthChildCanMakeAFamilyMatch` and `deletingTheSameIdTwiceChangesNothing`; the first three seen red by sabotage (references left in place, no check after a delete, the record left in place).
- `FamilyRegisterTests.deletingAFourthChildCanMakeAFamilyMatchWhenTheParentsDoNotListTheirChildren`: the people the deleted record names are checked too, also when nobody refers to the deleted ID (seen red by sabotage: leaving them out of the people to check).
- `PeopleControllerTests.deletesTheIdsInTheBodyAndAnswers204` and `DeletionScenarios.aDeletedPersonNoLongerCountsForMatching` (the driver sends the IDs as a JSON body).
