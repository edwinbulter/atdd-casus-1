# 0012. Property tests with jqwik

Date: 2026-10-30
Status: Accepted

## Context
Incremental matching (ADR 0011) is only correct if it gives the same answer as checking everyone, for every order of registrations, updates and contradicting records. Example tests cover the cases someone thought of; the mistakes in incremental code are in the cases nobody thought of.

Options: hand-written random loops in JUnit (no shrinking, no statistics), or **jqwik**, a property-based testing engine on the JUnit platform.

## Decision
- jqwik (`net.jqwik:jqwik`, test scope), run by Surefire in the commit stage like any JUnit test.
- Property classes are named `*PropertyTests`, so Surefire picks them up.
- The generator is **biased** towards families that match: most registrations are members of one family (parents 1 and 2, children 3, 4 and 5), sometimes with something left out or changed (a child with another second parent, a missing partner, a child who turns 18 today); some registrations are random people.
- The test collects how often somebody matches, to check that the property tests something.
- `.jqwik-database` (failing samples) stays local (`.gitignore`).

## Consequences
1. **A failure comes with the smallest example.** The property generates random sequences of up to 30 registrations. When it finds a failure, jqwik does not show that messy sequence: it **shrinks** it, removing and simplifying registrations as long as the failure remains. What you see is the smallest sequence that still fails, for example "register Homer → register Bart → register Bart again without parents → wrong answer".
2. **500 tries cost a few seconds.** Each run tries 500 different random sequences. That adds a few seconds to the commit stage. More tries find more mistakes but make the pipeline slower; 500 is the balance.
3. **The generator must produce matches, or the test proves nothing.** After every registration the test compares two answers: the incremental one and "check everyone". When nobody matches, both answers are empty, so they are always equal, even when the incremental code is wrong: the test is green without proving anything.
   - With fully random people, a valid family (a partner, exactly three children who both name the parents, one child under 18) almost never appeared: only **2%** of the registrations had somebody who matched.
   - With the biased generator about **9%** of the registrations match, and, more important, there are many changes from "matches" to "does not match" and back. That is where incremental code goes wrong.

## Guard
`FamilyRegisterPropertyTests` in the commit stage. That it catches mistakes is proven by sabotage: each part of `People.affectedBy`, and removing the old record from the lookup lists, was left out once, and every time this test turned red (ADR 0011).
