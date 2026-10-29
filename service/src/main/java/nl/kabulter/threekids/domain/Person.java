package nl.kabulter.threekids.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * One person record, as registered. Relations refer to other people by their ID and may be {@code null}
 * (unknown); a person without children has an empty list.
 */
public record Person(
        long id, String name, LocalDate birthDate, Long parent1, Long parent2, Long partner, List<Long> children) {

    public Person {
        children = children == null ? List.of() : List.copyOf(children);
    }

    /** A person without known relations. */
    public Person(long id, String name, LocalDate birthDate) {
        this(id, name, birthDate, null, null, null, List.of());
    }
}
