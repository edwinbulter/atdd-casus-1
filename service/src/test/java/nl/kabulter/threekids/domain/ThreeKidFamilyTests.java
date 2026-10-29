package nl.kabulter.threekids.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ThreeKidFamilyTests {

    private static final LocalDate BORN = LocalDate.of(1956, 5, 12);

    @Test
    void aPersonWithAPartnerAndThreeChildrenMatches() {
        assertThat(ThreeKidFamily.matches(new Person(1, "Homer", BORN, null, null, 2L, List.of(3L, 4L, 5L)))).isTrue();
    }

    @Test
    void aPersonWithoutAPartnerDoesNotMatch() {
        assertThat(ThreeKidFamily.matches(new Person(1, "Homer", BORN, null, null, null, List.of(3L, 4L, 5L)))).isFalse();
    }

    @Test
    void aPersonWithoutChildrenDoesNotMatch() {
        assertThat(ThreeKidFamily.matches(new Person(1, "Homer", BORN, null, null, 2L, List.of()))).isFalse();
    }

    @Test
    void aPersonWithTwoChildrenDoesNotMatch() {
        assertThat(ThreeKidFamily.matches(new Person(1, "Homer", BORN, null, null, 2L, List.of(3L, 4L)))).isFalse();
    }

    @Test
    void aPersonWithFourChildrenDoesNotMatch() {
        assertThat(ThreeKidFamily.matches(new Person(1, "Homer", BORN, null, null, 2L, List.of(3L, 4L, 5L, 6L)))).isFalse();
    }
}
