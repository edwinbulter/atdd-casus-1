package nl.kabulter.threekids.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class FamilyRegisterTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 30);

    private static final Person ADA = new Person(42, "Ada", TODAY.minusYears(50));

    private final FamilyRegister register = new FamilyRegister();

    @Test
    void aMatchEndsWhenTheYoungestChildTurns18EvenWithoutARelatedRegistration() {
        List<Long> children = List.of(3L, 4L, 5L);
        register.register(new Person(1, "Homer", TODAY.minusYears(40), null, null, 2L, children), TODAY);
        register.register(new Person(2, "Marge", TODAY.minusYears(40), null, null, 1L, children), TODAY);
        register.register(new Person(3, "Bart", TODAY.minusYears(30), 1L, 2L, null, List.of()), TODAY);
        register.register(new Person(4, "Lisa", TODAY.minusYears(25), 1L, 2L, null, List.of()), TODAY);
        LocalDate maggieTurns18 = TODAY.plusDays(1);
        assertThat(register.register(new Person(5, "Maggie", maggieTurns18.minusYears(18), 1L, 2L, null, List.of()), TODAY))
                .hasSize(2);

        List<Person> answer = register.register(new Person(42, "Ada", TODAY.minusYears(50)), maggieTurns18);

        assertThat(answer).isEmpty();
    }

    @Test
    void theFamilyMatches() {
        registerTheFamily();

        assertThat(register.register(ADA, TODAY)).extracting(Person::id).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void aDeletedChildNoLongerCounts() {
        registerTheFamily();

        register.delete(List.of(4L), TODAY);

        assertThat(register.register(ADA, TODAY)).isEmpty();
    }

    @Test
    void aDeletedPersonDoesNotMatchAsAPersonKnownOnlyById() {
        registerTheFamily();

        register.delete(List.of(2L), TODAY);

        assertThat(register.register(ADA, TODAY)).isEmpty();
    }

    @Test
    void deletingTheSameIdTwiceChangesNothing() {
        registerTheFamily();

        register.delete(List.of(99L), TODAY);
        register.delete(List.of(99L), TODAY);

        assertThat(register.register(ADA, TODAY)).extracting(Person::id).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void deletingAFourthChildCanMakeAFamilyMatch() {
        List<Long> children = List.of(3L, 4L, 5L, 6L);
        register.register(new Person(1, "Homer", TODAY.minusYears(40), null, null, 2L, children), TODAY);
        register.register(new Person(2, "Marge", TODAY.minusYears(40), null, null, 1L, children), TODAY);
        for (long child : children) {
            register.register(new Person(child, "Child " + child, TODAY.minusYears(10), 1L, 2L, null, List.of()), TODAY);
        }

        register.delete(List.of(6L), TODAY);

        assertThat(register.register(ADA, TODAY)).extracting(Person::id).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void aDeletedIdThatIsRegisteredAgainIsIgnored() {
        registerTheFamily();
        register.delete(List.of(4L), TODAY);

        assertThat(register.register(new Person(4, "Lisa", TODAY.minusYears(8), 1L, 2L, null, List.of()), TODAY))
                .isEmpty();
    }

    @Test
    void aNewRecordThatRefersToADeletedIdIsStoredWithoutThatReference() {
        registerTheFamily();
        register.delete(List.of(2L), TODAY);

        registerTheFamily();

        assertThat(register.register(ADA, TODAY)).isEmpty();
    }

    @Test
    void aDeletedPersonCannotComeBackThroughTheOldReferencesOfOthers() {
        registerTheFamily();
        Person ned = new Person(6, "Ned", TODAY.minusYears(45), null, null, 2L, List.of());
        register.register(ned, TODAY);
        register.delete(List.of(2L), TODAY);

        assertThat(register.register(ned, TODAY)).isEmpty();
    }

    private void registerTheFamily() {
        List<Long> children = List.of(3L, 4L, 5L);
        register.register(new Person(1, "Homer", TODAY.minusYears(40), null, null, 2L, children), TODAY);
        register.register(new Person(2, "Marge", TODAY.minusYears(40), null, null, 1L, children), TODAY);
        register.register(new Person(3, "Bart", TODAY.minusYears(10), 1L, 2L, null, List.of()), TODAY);
        register.register(new Person(4, "Lisa", TODAY.minusYears(8), 1L, 2L, null, List.of()), TODAY);
        register.register(new Person(5, "Maggie", TODAY.minusYears(3), 1L, 2L, null, List.of()), TODAY);
    }
}
