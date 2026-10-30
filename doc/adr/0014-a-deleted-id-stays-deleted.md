# 0014. A deleted ID stays deleted: its registration is ignored, and new references to it are removed

Date: 2026-10-30
Status: Accepted

## Context
ADR 0013 removes a deleted ID from every record at the moment of the DELETE. The casus asks more: deleted IDs must be remembered **forever**. "If they show up later, silently ignore them for both storage and matching." They can show up later in two ways:
- **The ID itself is posted again.** Example: Lisa is deleted, and later Lisa's old record is posted again.
- **A new record refers to it.** Example: Marge is deleted, and later Bart is posted again with the parents Homer and Marge.

Decided with the user: both are ignored. The registration of a deleted ID changes nothing, and a new record is stored without its references to deleted IDs.

## Decision
- `People` remembers the deleted IDs.
- `People.put` ignores the registration of a deleted ID silently, at the door: nothing is stored or checked, and the answer is who matches now (200 or 444, as for any POST).
- `People.put` stores every record without its references to deleted IDs (`Person.withoutReferencesTo`). The DELETE of ADR 0013 uses the same `put` to clean up the records that referred to the ID, so the clean-up is in one place.

## Consequences
- A deleted ID appears nowhere, whatever is posted later.
- The set of deleted IDs grows forever, as the casus asks; it costs one entry per deleted ID.
- Posting a deleted ID answers like any POST; the client is not told that it was ignored ("silently").

## Guard
- `FamilyRegisterTests`: `aDeletedIdThatIsRegisteredAgainIsIgnored`, `aNewRecordThatRefersToADeletedIdIsStoredWithoutThatReference` and `aDeletedPersonCannotComeBackThroughTheOldReferencesOfOthers`; seen red by sabotage.
- `FamilyRegisterPropertyTests` now also deletes, with its own description of a deleted ID; it caught every sabotage of ignoring and cleaning up (ADR 0012).
- `DeletionScenarios.aDeletedPersonWhoIsRegisteredAgainIsIgnored`.
