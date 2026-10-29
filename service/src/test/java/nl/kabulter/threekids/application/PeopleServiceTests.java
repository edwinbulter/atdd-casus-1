package nl.kabulter.threekids.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import nl.kabulter.threekids.domain.Person;
import org.junit.jupiter.api.Test;

class PeopleServiceTests {

    private final PeopleService people = new PeopleService();

    @Test
    void aSinglePersonWithoutAFamilyDoesNotMatch() {
        assertThat(people.register(new Person(42, "Ada Lovelace", LocalDate.of(1815, 12, 10)))).isEmpty();
    }

    @Test
    void partnersWithThreeChildrenMatchOnceTheFamilyIsRegistered() {
        Person homer = new Person(1, "Homer", LocalDate.of(1956, 5, 12), null, null, 2L, List.of(3L, 4L, 5L));
        Person marge = new Person(2, "Marge", LocalDate.of(1956, 10, 1), null, null, 1L, List.of(3L, 4L, 5L));
        people.register(homer);
        people.register(marge);
        people.register(child(3, "Bart", LocalDate.of(2015, 4, 1)));
        people.register(child(4, "Lisa", LocalDate.of(2017, 5, 9)));

        List<Person> matching = people.register(child(5, "Maggie", LocalDate.of(2023, 1, 12)));

        assertThat(matching).containsExactlyInAnyOrder(homer, marge);
    }

    private static Person child(long id, String name, LocalDate birthDate) {
        return new Person(id, name, birthDate, 1L, 2L, null, List.of());
    }
}
