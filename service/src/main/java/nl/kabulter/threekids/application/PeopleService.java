package nl.kabulter.threekids.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import nl.kabulter.threekids.domain.FamilyRegister;
import nl.kabulter.threekids.domain.Person;
import org.springframework.stereotype.Service;

/**
 * Registers people and tells who matches the three-kid family pattern after each registration, today according to
 * the injected clock (ADR 0007). The matching people are returned with everything known about them.
 */
@Service
public class PeopleService {

    private final FamilyRegister register = new FamilyRegister();
    private final Clock clock;

    public PeopleService(Clock clock) {
        this.clock = clock;
    }

    public List<Person> register(Person person) {
        return register.register(person, LocalDate.now(clock));
    }

    public void delete(List<Long> ids) {
        register.delete(ids, LocalDate.now(clock));
    }
}
