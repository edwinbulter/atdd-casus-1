package nl.kabulter.threekids.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class PeopleTests {

    private static final LocalDate BORN = LocalDate.of(2015, 4, 1);

    private final Person homer = new Person(1, "Homer", LocalDate.of(1956, 5, 12), null, null, 2L, List.of());

    @Test
    void aPersonsChildrenIncludeTheChildrenWhoNameThePerson() {
        People people = People.of(List.of(homer, child(3, 1L, 2L), child(4, 2L, 1L), child(5, 2L, null)));

        assertThat(people.childrenOf(homer)).containsExactly(3L, 4L);
    }

    @Test
    void theOwnListComesFirstAndNoChildIsCountedTwice() {
        Person homerWithList = new Person(1, "Homer", homer.birthDate(), null, null, 2L, List.of(4L, 3L));
        People people = People.of(List.of(homerWithList, child(3, 1L, 2L), child(5, 1L, 2L)));

        assertThat(people.childrenOf(homerWithList)).containsExactly(4L, 3L, 5L);
    }

    @Test
    void theChildrenWhoNameThePersonAreInTheOrderOfTheirIds() {
        People people = People.of(List.of(child(5, 1L, 2L), homer, child(3, 1L, 2L), child(4, 1L, 2L)));

        assertThat(people.childrenOf(homer)).containsExactly(3L, 4L, 5L);
    }

    @Test
    void aPersonAsKnownHasTheChildrenWhoNameThePerson() {
        People people = People.of(List.of(homer, child(3, 1L, 2L)));

        assertThat(people.asKnown(homer).children()).containsExactly(3L);
    }

    @Test
    void everyoneIncludesThePeopleKnownOnlyById() {
        Person marge = new Person(2, "Marge", LocalDate.of(1956, 10, 1), null, null, 1L, List.of(3L));
        People people = People.of(List.of(marge, child(3, 1L, 2L)));

        assertThat(people.everyone()).containsExactlyInAnyOrder(
                marge, child(3, 1L, 2L), new Person(1, null, null));
    }

    @Test
    void aPersonWithoutAPartnerInTheOwnRecordHasThePersonWhoNamesThemAsPartner() {
        Person homerWithoutPartner = new Person(1, "Homer", homer.birthDate());
        People people = People.of(List.of(homerWithoutPartner, partnerNaming(1)));

        assertThat(people.partnerOf(homerWithoutPartner)).isEqualTo(2L);
    }

    @Test
    void aPartnerInTheOwnRecordWins() {
        Person homerWithOtherPartner = new Person(1, "Homer", homer.birthDate(), null, null, 7L, List.of());
        People people = People.of(List.of(homerWithOtherPartner, partnerNaming(1)));

        assertThat(people.partnerOf(homerWithOtherPartner)).isEqualTo(7L);
    }

    @Test
    void aPersonAsKnownHasThePartnerWhoNamesThem() {
        Person homerOnlyById = new Person(1, null, null);
        People people = People.of(List.of(partnerNaming(1)));

        assertThat(people.asKnown(homerOnlyById).partner()).isEqualTo(2L);
    }

    private static Person partnerNaming(long partner) {
        return new Person(2, "Marge", LocalDate.of(1956, 10, 1), null, null, partner, List.of());
    }

    private static Person child(long id, Long parent1, Long parent2) {
        return new Person(id, "Child " + id, BORN, parent1, parent2, null, List.of());
    }
}
