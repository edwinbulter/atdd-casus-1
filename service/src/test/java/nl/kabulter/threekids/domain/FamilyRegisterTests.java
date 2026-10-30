package nl.kabulter.threekids.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class FamilyRegisterTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 30);

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
}
