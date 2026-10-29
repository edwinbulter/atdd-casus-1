package nl.kabulter.threekids.domain;

import java.time.LocalDate;

/**
 * One person record, as registered.
 */
public record Person(long id, String name, LocalDate birthDate) {
}
