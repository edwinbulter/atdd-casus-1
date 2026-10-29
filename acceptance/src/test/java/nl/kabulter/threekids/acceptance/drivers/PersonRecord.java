package nl.kabulter.threekids.acceptance.drivers;

import java.time.LocalDate;

/**
 * One person record as the service receives it.
 */
public record PersonRecord(long id, String name, LocalDate birthDate) {
}
