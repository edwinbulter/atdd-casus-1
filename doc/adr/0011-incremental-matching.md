# 0011. A registration only checks the people whose match it can change

Date: 2026-10-30
Status: Accepted

## Context
This ADR is about **how much work the service does for each registration**.

**How it was (ADR 0005).** After every POST the service checked **everyone** again: is this person now a parent with a partner and three children, one of them under 18? Simple, but the work grows with the number of people stored. Measured with `make perf` (README):
- 10,000 people stored: 10,000 checks per POST, about 4 ms;
- 100,000 people: 100,000 checks per POST, about 38 ms (estimated);
- 1 million people: about 0.4 s per POST (estimated).

The casus evaluates "what your application would do under load". Decided with the user: make matching incremental.

**The idea.** One POST changes **one record**. With 100,000 people stored, re-registering Bart cannot change whether the neighbour down the street matches. Only the people connected to Bart can change:
- **Bart himself**;
- **the people Bart names**: his parents and his partner, in his old and in his new record (after an update he may name others);
- **the people who refer to Bart**: who names Bart as partner, or lists Bart as a child.

That is usually only a handful of people. The service only checks them.

**One catch: time.** "Under 18" changes by itself, without any POST. If Homer and Marge match today because Maggie is 17, they no longer match tomorrow when she turns 18, even if nobody in their family is registered. The reverse cannot happen: someone who did not match yesterday, and whose family did not change, does not start matching today. Children only get older.

## Decision
- **Remember who matches.** `FamilyRegister` (domain) keeps the set of people who match now.
- **Per POST, check only:** the people the record can affect (`People.affectedBy(earlier, person)`), plus the people who match now. Those are checked again because time can end their match; they are all in the answer anyway, as the casus asks for every matching person. The check itself is the same `ThreeKidFamily.matches` as before.
- **Update the lookup lists instead of rebuilding them.** `People` keeps its lookup lists ("who names Homer as parent?", "who names Marge as partner?", "who lists Bart as a child?") up to date on every POST: the old record out, the new one in. This amends ADR 0009 and ADR 0010 on how the lists are built, not on what they mean.
- **One POST at a time.** So that two POSTs at the same moment do not mix up the lists, `FamilyRegister` works behind a lock (`ReentrantLock`), close to the state it guards; no `synchronized` (guardrail).

This replaces ADR 0005 on how matching works; keeping people in memory by ID stays.

## Consequences
- Measured: with 100,000 people stored a POST takes **12 µs** instead of about 38 ms, and it stays the same however many people are stored (README).
- The work of a POST grows with the size of the family and with the number of people who match, not with the number of people stored.
- More code than a plain filter. **The risk:** if `affectedBy` forgets someone, the answer is wrong and there is **no error**. Hence the guards below.
- POSTs are handled one at a time; throughput is bounded by one thread, but a POST is short.

## Guard
1. **Property test** `FamilyRegisterPropertyTests` (jqwik, ADR 0012): generates hundreds of random sequences of registrations and, after every POST, compares the fast answer with the old way, checking everyone. Each part of `affectedBy`, and removing the old record from the lookup lists, was left out once by sabotage: every time this test turned red.
2. **Time test** `FamilyRegisterTests.aMatchEndsWhenTheYoungestChildTurns18EvenWithoutARelatedRegistration`: a match ends when the youngest child turns 18, also without a related POST (seen red by sabotage).
3. **Work test** `FamilyRegisterWorkTests`: with 1,000 people stored, one POST checks at most 10 people (before this change: 1,001).
4. `make perf` measures the time itself; not in the pipeline, because timing on shared runners is unreliable.
