package nl.kabulter.threekids.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.Tuple;
import net.jqwik.api.statistics.Statistics;

/**
 * Whatever the register does to be fast (ADR 0011), after every registration its answer equals checking everyone,
 * computed from scratch from the last record of each ID.
 */
class FamilyRegisterPropertyTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 30);
    private static final long IDS = 8;

    @Property(tries = 500)
    void theAnswerEqualsCheckingEveryone(@ForAll("registrations") List<Person> registrations) {
        FamilyRegister register = new FamilyRegister();
        Map<Long, Person> lastRecords = new HashMap<>();
        for (Person person : registrations) {
            List<Person> answer = register.register(person, TODAY);
            lastRecords.put(person.id(), person);
            Statistics.label("answer").collect(answer.isEmpty() ? "nobody matches" : "somebody matches");

            assertThat(answer).containsExactlyInAnyOrderElementsOf(checkingEveryone(lastRecords));
        }
    }

    private static List<Person> checkingEveryone(Map<Long, Person> lastRecords) {
        People everyone = People.of(lastRecords.values());
        return everyone.everyone().stream()
                .filter(candidate -> ThreeKidFamily.matches(candidate, everyone, TODAY))
                .map(everyone::asKnown)
                .toList();
    }

    /**
     * Registrations over a few IDs: mostly members of one family (parents 1 and 2, children 3, 4 and 5) with some
     * relations left out or changed, and some random records, so registrations often update, contradict and complete
     * each other, and families often match.
     */
    @Provide
    Arbitrary<List<Person>> registrations() {
        return Arbitraries.frequencyOf(
                        Tuple.of(3, familyMember()),
                        Tuple.of(1, randomPerson()))
                .list().ofMinSize(1).ofMaxSize(30);
    }

    private static Arbitrary<Person> familyMember() {
        Arbitrary<Long> member = Arbitraries.longs().between(1, 5);
        Arbitrary<List<Long>> parents = Arbitraries.frequency(
                Tuple.of(6, Arrays.asList(1L, 2L)),
                Tuple.of(1, Arrays.asList(2L, 6L)),
                Tuple.of(1, Arrays.asList(1L, null)),
                Tuple.of(1, Arrays.<Long>asList(null, null)));
        Arbitrary<List<Long>> children = Arbitraries.frequency(
                Tuple.of(6, List.of(3L, 4L, 5L)),
                Tuple.of(1, List.of(3L, 4L)),
                Tuple.of(1, List.<Long>of()),
                Tuple.of(1, List.of(3L, 4L, 5L, 6L)));
        Arbitrary<Boolean> namesPartner = Arbitraries.frequency(Tuple.of(4, true), Tuple.of(1, false));
        return Combinators.combine(member, birthDate(), parents, children, namesPartner)
                .as((id, born, mumAndDad, kids, listsPartner) -> id <= 2
                        ? new Person(id, "Parent " + id, born, null, null, listsPartner ? 3 - id : null, kids)
                        : new Person(id, "Child " + id, born, mumAndDad.get(0), mumAndDad.get(1), null, List.of()));
    }

    private static Arbitrary<Person> randomPerson() {
        Arbitrary<Long> id = Arbitraries.longs().between(1, IDS);
        Arbitrary<Long> reference = id.injectNull(0.3);
        Arbitrary<List<Long>> children = id.list().ofMaxSize(4).uniqueElements();
        return Combinators.combine(id, birthDate(), reference, reference, reference, children)
                .as((personId, born, parent1, parent2, partner, kids) ->
                        new Person(personId, "Person " + personId, born, parent1, parent2, partner, kids));
    }

    private static Arbitrary<LocalDate> birthDate() {
        return Arbitraries.frequency(
                        Tuple.of(2, LocalDate.of(2015, 4, 1)),
                        Tuple.of(1, LocalDate.of(1980, 4, 1)),
                        Tuple.of(1, LocalDate.of(2008, 10, 30)))
                .injectNull(0.1);
    }
}
