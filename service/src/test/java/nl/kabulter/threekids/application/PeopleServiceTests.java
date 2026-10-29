package nl.kabulter.threekids.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import nl.kabulter.threekids.domain.Person;
import org.junit.jupiter.api.Test;

class PeopleServiceTests {

    private final PeopleService people = new PeopleService();

    @Test
    void aSinglePersonWithoutAFamilyDoesNotMatch() {
        assertThat(people.register(new Person(42, "Ada Lovelace", LocalDate.of(1815, 12, 10)))).isEmpty();
    }
}
