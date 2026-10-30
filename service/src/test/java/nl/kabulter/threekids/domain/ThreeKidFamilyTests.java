package nl.kabulter.threekids.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ThreeKidFamilyTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 29);
    private static final LocalDate PARENT_BORN = LocalDate.of(1956, 5, 12);
    private static final LocalDate YOUNG = LocalDate.of(2015, 4, 1);
    private static final LocalDate ADULT = LocalDate.of(1980, 4, 1);

    private final Map<Long, Person> records = new HashMap<>();

    @Test
    void aPersonWithAPartnerAndThreeChildrenMatches() {
        Person homer = register(parent(2L, List.of(3L, 4L, 5L)));
        registerChildren(YOUNG, YOUNG, YOUNG);

        assertThat(matches(homer)).isTrue();
    }

    @Test
    void aPersonWithoutAPartnerDoesNotMatch() {
        Person homer = register(parent(null, List.of(3L, 4L, 5L)));
        registerChildren(YOUNG, YOUNG, YOUNG);

        assertThat(matches(homer)).isFalse();
    }

    @Test
    void aPersonWithoutChildrenDoesNotMatch() {
        Person homer = register(parent(2L, List.of()));

        assertThat(matches(homer)).isFalse();
    }

    @Test
    void aPersonWithTwoChildrenDoesNotMatch() {
        Person homer = register(parent(2L, List.of(3L, 4L)));
        registerChildren(YOUNG, YOUNG);

        assertThat(matches(homer)).isFalse();
    }

    @Test
    void aPersonWithFourChildrenDoesNotMatch() {
        Person homer = register(parent(2L, List.of(3L, 4L, 5L, 6L)));
        registerChildren(YOUNG, YOUNG, YOUNG, YOUNG);

        assertThat(matches(homer)).isFalse();
    }

    @Test
    void aPersonWhoseChildrenAreAllAdultsDoesNotMatch() {
        Person homer = register(parent(2L, List.of(3L, 4L, 5L)));
        registerChildren(ADULT, ADULT, ADULT);

        assertThat(matches(homer)).isFalse();
    }

    @Test
    void oneChildUnder18IsEnough() {
        Person homer = register(parent(2L, List.of(3L, 4L, 5L)));
        registerChildren(ADULT, ADULT, YOUNG);

        assertThat(matches(homer)).isTrue();
    }

    @Test
    void aChildWhoTurns18TodayIsNoLongerUnder18() {
        Person homer = register(parent(2L, List.of(3L, 4L, 5L)));
        registerChildren(ADULT, ADULT, TODAY.minusYears(18));

        assertThat(matches(homer)).isFalse();
    }

    @Test
    void aChildWhoTurns18TomorrowIsStillUnder18() {
        Person homer = register(parent(2L, List.of(3L, 4L, 5L)));
        registerChildren(ADULT, ADULT, TODAY.minusYears(18).plusDays(1));

        assertThat(matches(homer)).isTrue();
    }

    @Test
    void aChildWithoutABirthDateDoesNotCountAsUnder18() {
        Person homer = register(parent(2L, List.of(3L, 4L, 5L)));
        registerChildren(ADULT, ADULT, null);

        assertThat(matches(homer)).isFalse();
    }

    @Test
    void aChildWithoutARecordDoesNotCountAsUnder18() {
        Person homer = register(parent(2L, List.of(3L, 4L, 5L)));
        registerChildren(ADULT, ADULT);

        assertThat(matches(homer)).isFalse();
    }

    @Test
    void aChildThatDoesNotNameThePartnerPreventsAMatch() {
        Person homer = register(parent(2L, List.of(3L, 4L, 5L)));
        registerChildren(YOUNG, YOUNG, YOUNG);
        register(new Person(4, "Lisa", YOUNG, 1L, 6L, null, List.of()));

        assertThat(matches(homer)).isFalse();
    }

    @Test
    void aChildWhoseOwnRecordNamesOtherParentsIsNotThePersonsChild() {
        Person marge = register(new Person(2, "Marge", PARENT_BORN, null, null, 1L, List.of(3L, 4L, 5L)));
        registerChildren(YOUNG, YOUNG, YOUNG);
        register(new Person(4, "Lisa", YOUNG, 1L, 6L, null, List.of()));

        assertThat(matches(marge)).isFalse();
    }

    @Test
    void aChildWithoutARecordPreventsAMatch() {
        Person homer = register(parent(2L, List.of(3L, 4L, 5L)));
        registerChildren(YOUNG, YOUNG);

        assertThat(matches(homer)).isFalse();
    }

    @Test
    void childrenWithoutParentsAreRepairedFromBothParentsRecords() {
        Person homer = register(parent(2L, List.of(3L, 4L, 5L)));
        register(new Person(2, "Marge", PARENT_BORN, null, null, 1L, List.of(3L, 4L, 5L)));
        registerChildrenWithParents(null, null);

        assertThat(matches(homer)).isTrue();
    }

    @Test
    void anEmptySecondSlotIsRepairedFromThePartnersRecord() {
        Person homer = register(parent(2L, List.of(3L, 4L, 5L)));
        register(new Person(2, "Marge", PARENT_BORN, null, null, 1L, List.of(3L, 4L, 5L)));
        registerChildrenWithParents(1L, null);

        assertThat(matches(homer)).isTrue();
    }

    @Test
    void anEmptySlotIsNotRepairedByAPartnerWhoDoesNotNameTheChild() {
        Person homer = register(parent(2L, List.of(3L, 4L, 5L)));
        register(new Person(2, "Marge", PARENT_BORN, null, null, 1L, List.of()));
        registerChildrenWithParents(1L, null);

        assertThat(matches(homer)).isFalse();
    }

    @Test
    void oneEmptySlotCannotTakeBothParents() {
        Person homer = register(parent(2L, List.of(3L, 4L, 5L)));
        register(new Person(2, "Marge", PARENT_BORN, null, null, 1L, List.of(3L, 4L, 5L)));
        registerChildrenWithParents(6L, null);

        assertThat(matches(homer)).isFalse();
    }

    @Test
    void aPersonWhoDoesNotListTheChildrenGetsThemFromTheChildrensRecords() {
        Person homer = register(parent(2L, List.of()));
        register(new Person(2, "Marge", PARENT_BORN, null, null, 1L, List.of()));
        registerChildren(YOUNG, YOUNG, YOUNG);

        assertThat(matches(homer)).isTrue();
    }

    @Test
    void aPersonKnownOnlyByIdMatchesThroughThePartnerWhoNamesThem() {
        register(new Person(2, "Marge", PARENT_BORN, null, null, 1L, List.of(3L, 4L, 5L)));
        registerChildren(YOUNG, YOUNG, YOUNG);

        assertThat(matches(new Person(1, null, null))).isTrue();
    }

    private void registerChildrenWithParents(Long parent1, Long parent2) {
        for (long id = 3; id <= 5; id++) {
            register(new Person(id, "Child " + id, YOUNG, parent1, parent2, null, List.of()));
        }
    }

    private static Person parent(Long partner, List<Long> children) {
        return new Person(1, "Homer", PARENT_BORN, null, null, partner, children);
    }

    /** Registers children with the IDs 3, 4, 5, ... and the given birth dates, each with parents 1 and 2. */
    private void registerChildren(LocalDate... birthDates) {
        for (int i = 0; i < birthDates.length; i++) {
            register(new Person(3 + i, "Child " + (3 + i), birthDates[i], 1L, 2L, null, List.of()));
        }
    }

    private Person register(Person person) {
        records.put(person.id(), person);
        return person;
    }

    private boolean matches(Person person) {
        return ThreeKidFamily.matches(person, People.of(records.values()), TODAY);
    }
}
