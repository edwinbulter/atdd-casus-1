package nl.kabulter.threekids.acceptance.drivers;

import java.time.LocalDate;
import java.util.List;

/**
 * One person record as the service receives and returns it. Unknown values are {@code null}; no children is an empty list.
 */
public record PersonRecord(
        long id, String name, LocalDate birthDate, Long parent1, Long parent2, Long partner, List<Long> children) {

    public PersonRecord {
        children = List.copyOf(children);
    }
}
