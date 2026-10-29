package nl.kabulter.threekids.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The pattern of the casus: a person with a partner and exactly three children, all three of whom name that partner
 * as a parent too, and at least one of whom is under 18 today.
 *
 * <ul>
 *   <li>A person's children are the people in the person's own list whose record names the person as a parent: when
 *       the records conflict, the child's own record wins. A child without a record is not counted.</li>
 *   <li>A child without a birth date does not count as under 18: its age is unknown.</li>
 * </ul>
 */
public final class ThreeKidFamily {

    private static final int CHILDREN = 3;
    private static final int ADULT_AGE = 18;

    private ThreeKidFamily() {
    }

    public static boolean matches(Person person, Map<Long, Person> people, LocalDate today) {
        if (person.partner() == null) {
            return false;
        }
        List<Person> children = person.children().stream()
                .map(people::get)
                .filter(Objects::nonNull)
                .filter(child -> child.namesAsParent(person.id()))
                .toList();
        return children.size() == CHILDREN
                && children.stream().allMatch(child -> child.namesAsParent(person.partner()))
                && children.stream().anyMatch(child -> isUnder18(child, today));
    }

    private static boolean isUnder18(Person child, LocalDate today) {
        return child.birthDate() != null && child.birthDate().plusYears(ADULT_AGE).isAfter(today);
    }
}
