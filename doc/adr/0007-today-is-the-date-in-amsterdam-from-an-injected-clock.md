# 0007. "Today" is the date in Europe/Amsterdam, from an injected clock

Date: 2026-10-29
Status: Accepted

## Context
Scenario "a three-kid family with only adult children does not match" needs "under 18", and that depends on today's date. Today changes without any registration, so the age check cannot be cached, and tests need a fixed date to check the boundary (a child who turns 18 today). The container runs in UTC; between midnight in Amsterdam and midnight UTC the date differs.

Options for the date: `LocalDate.now()` (untestable, and the guardrail forbids it), or **an injected `java.time.Clock`**. Options for the zone: UTC (the container default), or **Europe/Amsterdam** (the casus is Dutch).

## Decision
- A `Clock` bean (`ClockConfiguration`) with `Clock.system(ZoneId.of("Europe/Amsterdam"))`; `PeopleService` gets it injected and computes `LocalDate.now(clock)` on every registration.
- The domain gets `today` as a parameter: `ThreeKidFamily.matches(person, people, today)`.
- A child is under 18 when its 18th birthday is after today: a child who turns 18 today is no longer under 18.

## Consequences
- Tests use `Clock.fixed(...)` and can check the boundary exactly.
- The age is computed again on every registration; nothing about age is stored.
- A family can stop matching as time passes; the service only notices on the next registration, which is what the casus asks ("after each record").
- Children born on 29 February turn 18 on 28 February in a non-leap year (`LocalDate.plusYears`); not tested.

## Guard
- `ArchitectureTests.noSystemClock` fails on `LocalDate.now()`, `LocalDateTime.now()` or `Instant.now()` (seen red by sabotage in `PeopleService`).
- `ClockConfigurationTests.todayIsTheDateInAmsterdam` fails with another zone (seen red with UTC).
- `ThreeKidFamilyTests.aChildWhoTurns18TodayIsNoLongerUnder18` and `aChildWhoTurns18TomorrowIsStillUnder18` guard the boundary.
