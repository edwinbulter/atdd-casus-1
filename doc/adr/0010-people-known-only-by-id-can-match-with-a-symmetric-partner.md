# 0010. People known only by ID can match, and the partner link is symmetric

Date: 2026-10-30
Status: Accepted

## Context
Scenario 7: Marge names Homer as her partner and Bart, Lisa and Maggie as her children; the children name Homer and Marge as parents. Homer himself has no record. The casus asks to "respond with everything we know about the matching people (you might not know their name or birthdate yet)": a person without a record can match.

Two questions follow:
- **Who is a candidate?** So far only people with a record. Homer is only an ID in other records.
- **Who is Homer's partner?** Only Marge's record says so. Bidirectional integrity for parents and children was decided in ADR 0008 and ADR 0009; the partner link needs the same.

Decided with the user: people known only by ID are candidates, and the partner link is symmetric, with the own record winning.

## Decision
- `People.everyone()` returns the people with a record, plus everyone else that a record refers to (as parent, partner or child), as a person with only an ID.
- `People.partnerOf(person)`: the partner in the person's own record; without one, the person who names this person as partner. When several people name the same person, the one with the lowest ID counts.
- `People.asKnown(person)` shows that partner, next to the children of ADR 0009.
- A second index, from a person to the person who names them as partner, is built in the same way as the children index: once per registration, O(n).

## Consequences
- Homer matches and is shown with id 1, partner Marge and the children Bart, Lisa and Maggie, without name or birth date.
- The number of candidates grows with the people referred to, not only the people registered; still O(n) per registration.
- When two people name the same person as partner and that person names nobody, the lowest ID wins; this choice has no scenario or unit test yet.

## Guard
- `PeopleTests`: `everyoneIncludesThePeopleKnownOnlyById`, `aPersonWithoutAPartnerInTheOwnRecordHasThePersonWhoNamesThemAsPartner`, `aPartnerInTheOwnRecordWins`, `aPersonAsKnownHasThePartnerWhoNamesThem`; each seen red by sabotage.
- `ThreeKidFamilyTests.aPersonKnownOnlyByIdMatchesThroughThePartnerWhoNamesThem`, `PeopleServiceTests.aPersonKnownOnlyByIdMatchesWithWhatTheOthersSayAboutThem` and `IntegrityScenarios.aPersonKnownOnlyByIdMatchesWithEverythingKnownAboutThem`.
