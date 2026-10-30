package nl.kabulter.threekids.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import nl.kabulter.threekids.domain.Person;
import org.junit.jupiter.api.Test;

class PeopleServiceTests {

    private static final ZoneId AMSTERDAM = ZoneId.of("Europe/Amsterdam");
    private static final Clock TODAY = Clock.fixed(LocalDate.of(2026, 10, 29).atStartOfDay(AMSTERDAM).toInstant(), AMSTERDAM);

    private final PeopleService people = new PeopleService(TODAY);

    private final Person homer = new Person(1, "Homer", LocalDate.of(1956, 5, 12), null, null, 2L, List.of(3L, 4L, 5L));
    private final Person marge = new Person(2, "Marge", LocalDate.of(1956, 10, 1), null, null, 1L, List.of(3L, 4L, 5L));

    @Test
    void aSinglePersonWithoutAFamilyDoesNotMatch() {
        assertThat(people.register(new Person(42, "Ada Lovelace", LocalDate.of(1815, 12, 10)))).isEmpty();
    }

    @Test
    void partnersWithThreeChildrenMatchOnceTheFamilyIsRegistered() {
        people.register(homer);
        people.register(marge);
        people.register(child(3, "Bart", LocalDate.of(2015, 4, 1)));
        people.register(child(4, "Lisa", LocalDate.of(2017, 5, 9)));

        List<Person> matching = people.register(child(5, "Maggie", LocalDate.of(2023, 1, 12)));

        assertThat(matching).containsExactlyInAnyOrder(homer, marge);
    }

    @Test
    void partnersWithOnlyAdultChildrenDoNotMatch() {
        people.register(homer);
        people.register(marge);
        people.register(child(3, "Bart", LocalDate.of(1980, 4, 1)));
        people.register(child(4, "Lisa", LocalDate.of(1982, 5, 9)));

        List<Person> matching = people.register(child(5, "Maggie", LocalDate.of(1988, 1, 12)));

        assertThat(matching).isEmpty();
    }

    @Test
    void parentsWhoDoNotListTheirChildrenMatchWithTheChildrenFromTheChildrensRecords() {
        people.register(new Person(1, "Homer", LocalDate.of(1956, 5, 12), null, null, 2L, List.of()));
        people.register(new Person(2, "Marge", LocalDate.of(1956, 10, 1), null, null, 1L, List.of()));
        people.register(child(3, "Bart", LocalDate.of(2015, 4, 1)));
        people.register(child(4, "Lisa", LocalDate.of(2017, 5, 9)));

        List<Person> matching = people.register(child(5, "Maggie", LocalDate.of(2023, 1, 12)));

        assertThat(matching).containsExactlyInAnyOrder(homer, marge);
    }

    @Test
    void registeringTheSameRecordAgainGivesTheSameAnswer() {
        Person maggie = registerTheFamily();

        assertThat(people.register(maggie)).containsExactlyInAnyOrder(homer, marge);
    }

    @Test
    void aNewRecordForAKnownIdReplacesTheOldOne() {
        registerTheFamily();

        assertThat(people.register(new Person(4, "Lisa", LocalDate.of(2017, 5, 9), 1L, 6L, null, List.of()))).isEmpty();
    }

    /** Registers Homer, Marge and their three young children; returns the last one registered, Maggie. */
    private Person registerTheFamily() {
        people.register(homer);
        people.register(marge);
        people.register(child(3, "Bart", LocalDate.of(2015, 4, 1)));
        people.register(child(4, "Lisa", LocalDate.of(2017, 5, 9)));
        Person maggie = child(5, "Maggie", LocalDate.of(2023, 1, 12));
        assertThat(people.register(maggie)).containsExactlyInAnyOrder(homer, marge);
        return maggie;
    }

    private static Person child(long id, String name, LocalDate birthDate) {
        return new Person(id, name, birthDate, 1L, 2L, null, List.of());
    }
}
