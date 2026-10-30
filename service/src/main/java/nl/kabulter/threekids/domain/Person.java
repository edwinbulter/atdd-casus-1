package nl.kabulter.threekids.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * One person record, as registered. Relations refer to other people by their ID and may be {@code null}
 * (unknown); a person without children has an empty list.
 */
public record Person(
        long id, String name, LocalDate birthDate, Long parent1, Long parent2, Long partner, List<Long> children) {

    public Person {
        children = children == null ? List.of() : List.copyOf(children);
    }

    /** Whether this record names the given person as parent1 or parent2. */
    public boolean namesAsParent(long parent) {
        return Long.valueOf(parent).equals(parent1) || Long.valueOf(parent).equals(parent2);
    }

    /** How many of parent1 and parent2 are still unknown. */
    public int emptyParentSlots() {
        return (parent1 == null ? 1 : 0) + (parent2 == null ? 1 : 0);
    }

    /** Whether this record lists the given person as a child. */
    public boolean namesAsChild(long child) {
        return children.contains(child);
    }

    /** This record without any reference to the given people, as if they never existed. */
    public Person withoutReferencesTo(Set<Long> gone) {
        return new Person(id, name, birthDate, unless(parent1, gone), unless(parent2, gone), unless(partner, gone),
                children.stream().filter(child -> !gone.contains(child)).toList());
    }

    private static Long unless(Long reference, Set<Long> gone) {
        return reference != null && gone.contains(reference) ? null : reference;
    }

    /** A person without known relations. */
    public Person(long id, String name, LocalDate birthDate) {
        this(id, name, birthDate, null, null, null, List.of());
    }
}
