package nl.kabulter.threekids.application;

import java.util.List;
import nl.kabulter.threekids.domain.Person;
import org.springframework.stereotype.Service;

/**
 * Registers people and tells who matches the three-kid family pattern after each registration.
 */
@Service
public class PeopleService {

    public List<Person> register(Person person) {
        return List.of();
    }
}
