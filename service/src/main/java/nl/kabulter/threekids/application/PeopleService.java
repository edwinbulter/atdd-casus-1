package nl.kabulter.threekids.application;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nl.kabulter.threekids.domain.Person;
import nl.kabulter.threekids.domain.ThreeKidFamily;
import org.springframework.stereotype.Service;

/**
 * Registers people and tells who matches the three-kid family pattern after each registration. People are kept in
 * memory by ID, and every registration checks everyone (ADR 0005).
 */
@Service
public class PeopleService {

    private final Map<Long, Person> people = new ConcurrentHashMap<>();

    public List<Person> register(Person person) {
        people.put(person.id(), person);
        return people.values().stream().filter(ThreeKidFamily::matches).toList();
    }
}
