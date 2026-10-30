package nl.kabulter.threekids.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * ADR 0011: the work of one registration does not grow with the number of people stored. It counts how often the
 * pattern is checked; the time itself is measured with make perf. The stored families do not match (their children
 * are adults): the people who match are checked again on every registration, because they can stop matching as time
 * passes, and they are all in the answer anyway.
 */
class FamilyRegisterWorkTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 30);
    private static final int FAMILIES = 200;

    private int checks;
    private final FamilyRegister register = new FamilyRegister((person, people, today) -> {
        checks++;
        return ThreeKidFamily.matches(person, people, today);
    });

    @Test
    void aRegistrationChecksOnlyAFewPeopleHoweverManyAreStored() {
        for (int family = 0; family < FAMILIES; family++) {
            registerFamily(family * 5L + 1);
        }
        checks = 0;

        register.register(new Person(FAMILIES * 5L + 1, "Newcomer", TODAY.minusYears(30)), TODAY);

        assertThat(checks).as("people checked for one registration among %d stored", FAMILIES * 5).isLessThanOrEqualTo(10);
    }

    private void registerFamily(long first) {
        long second = first + 1;
        List<Long> children = List.of(first + 2, first + 3, first + 4);
        register.register(new Person(first, "Parent", TODAY.minusYears(40), null, null, second, children), TODAY);
        register.register(new Person(second, "Parent", TODAY.minusYears(40), null, null, first, children), TODAY);
        for (long child : children) {
            register.register(new Person(child, "Child", TODAY.minusYears(20), first, second, null, List.of()), TODAY);
        }
    }
}
