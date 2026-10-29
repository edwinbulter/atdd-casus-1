package nl.kabulter.threekids.domain;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

/**
 * The pattern of the casus. So far it checks that a person has a partner and three children, at least one of them
 * under 18 today; the rule about the children's second parent comes with the scenario that needs it (doc/backlog.md).
 * A child without a record or without a birth date does not count as under 18: its age is unknown.
 */
public final class ThreeKidFamily {

    private static final int CHILDREN = 3;
    private static final int ADULT_AGE = 18;

    private ThreeKidFamily() {
    }

    public static boolean matches(Person person, Map<Long, Person> people, LocalDate today) {
        return person.partner() != null
                && person.children().size() == CHILDREN
                && person.children().stream()
                        .map(people::get)
                        .filter(Objects::nonNull)
                        .anyMatch(child -> isUnder18(child, today));
    }

    private static boolean isUnder18(Person child, LocalDate today) {
        return child.birthDate() != null && child.birthDate().plusYears(ADULT_AGE).isAfter(today);
    }
}
