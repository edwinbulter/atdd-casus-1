package nl.kabulter.threekids.acceptance.dsl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import nl.kabulter.threekids.acceptance.drivers.PersonRecord;

/**
 * A person as the scenarios describe it, built step by step. Relations refer to other people by their ID.
 */
public final class Person {

    private final String name;
    private long id;
    private LocalDate birthDate;
    private Long parent1;
    private Long parent2;
    private Long partner;
    private List<Long> children = List.of();

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

    public Person withParents(Person first, Person second) {
        this.parent1 = first.id;
        this.parent2 = second.id;
        return this;
    }

    public Person withPartner(Person partner) {
        this.partner = partner.id;
        return this;
    }

    public Person withChildren(Person... children) {
        this.children = Arrays.stream(children).map(child -> child.id).toList();
        return this;
    }

    /**
     * This person as the service knows it without a record of its own: only the ID, no name or birth date. Relations
     * can be added with the other methods.
     */
    public Person asKnownOnlyById() {
        return new Person(null).withId(id);
    }

    PersonRecord toRecord() {
        return new PersonRecord(id, name, birthDate, parent1, parent2, partner, children);
    }
}
