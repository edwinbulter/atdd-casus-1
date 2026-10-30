package nl.kabulter.threekids.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The registered people, and who matches the three-kid family pattern after each registration. Every registration
 * checks everyone (ADR 0005).
 */
public final class FamilyRegister {

    private final Map<Long, Person> records = new ConcurrentHashMap<>();

    /** Registers the person, replacing an earlier record with the same ID, and returns everyone who matches today. */
    public List<Person> register(Person person, LocalDate today) {
        records.put(person.id(), person);
        People everyone = People.of(records.values());
        return everyone.everyone().stream()
                .filter(candidate -> ThreeKidFamily.matches(candidate, everyone, today))
                .map(everyone::asKnown)
                .toList();
    }
}
