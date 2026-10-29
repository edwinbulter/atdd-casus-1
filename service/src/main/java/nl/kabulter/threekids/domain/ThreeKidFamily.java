package nl.kabulter.threekids.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The pattern of the casus: a person with a partner and exactly three children, all three of whom have that partner
 * as a parent too, and at least one of whom is under 18 today.
 *
 * <ul>
 *   <li>A child's parents are the parents in its own record. An empty parent slot is repaired by a parent who lists
 *       the child; a filled slot is never overwritten, so when the records conflict, the child's own record wins.</li>
 *   <li>A person's children are the people in the person's own list who have the person as a parent. A child without
 *       a record is not counted.</li>
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
        Person partner = people.get(person.partner());
        List<Person> children = person.children().stream()
                .map(people::get)
                .filter(Objects::nonNull)
                .filter(child -> child.namesAsParent(person.id()) || child.emptyParentSlots() > 0)
                .toList();
        return children.size() == CHILDREN
                && children.stream().allMatch(child -> hasBothParents(child, person, person.partner(), partner))
                && children.stream().anyMatch(child -> isUnder18(child, today));
    }

    /**
     * Whether the child has both the person and the partner as parents, after repair. The person lists the child;
     * the partner can only fill an empty slot when the partner's record lists the child too.
     */
    private static boolean hasBothParents(Person child, Person person, long partnerId, Person partner) {
        boolean namesPerson = child.namesAsParent(person.id());
        boolean namesPartner = child.namesAsParent(partnerId);
        boolean partnerListsChild = partner != null && partner.namesAsChild(child.id());
        int missing = (namesPerson ? 0 : 1) + (namesPartner ? 0 : 1);
        return (namesPartner || partnerListsChild) && missing <= child.emptyParentSlots();
    }

    private static boolean isUnder18(Person child, LocalDate today) {
        return child.birthDate() != null && child.birthDate().plusYears(ADULT_AGE).isAfter(today);
    }
}
