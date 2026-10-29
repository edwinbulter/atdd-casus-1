package nl.kabulter.threekids.acceptance.dsl;

import java.time.LocalDate;
import nl.kabulter.threekids.acceptance.drivers.PersonRecord;

/**
 * A person as the scenarios describe it, built step by step.
 */
public final class Person {

    private final String name;
    private long id;
    private LocalDate birthDate;

    private Person(String name) {
        this.name = name;
    }

    public static Person person(String name) {
        return new Person(name);
    }

    public Person withId(long id) {
        this.id = id;
        return this;
    }

    public Person bornOn(String isoDate) {
        this.birthDate = LocalDate.parse(isoDate);
        return this;
    }

    PersonRecord toRecord() {
        return new PersonRecord(id, name, birthDate);
    }
}
