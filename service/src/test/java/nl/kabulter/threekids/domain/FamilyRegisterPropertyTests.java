package nl.kabulter.threekids.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
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
 * computed from scratch from the last record of each ID that was not deleted (ADR 0013, ADR 0014).
 */
class FamilyRegisterPropertyTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 30);
    private static final long IDS = 8;

    /** One step of a random sequence: a registration, or a delete of a few IDs. */
    sealed interface Step permits Register, Delete {
    }

    record Register(Person person) implements Step {
    }

    record Delete(List<Long> ids) implements Step {
    }

    @Property(tries = 500)
    void theAnswerEqualsCheckingEveryone(@ForAll("steps") List<Step> steps) {
        FamilyRegister register = new FamilyRegister();
        Map<Long, Person> lastRecords = new HashMap<>();
        Set<Long> deleted = new HashSet<>();
        for (Step step : steps) {
            switch (step) {
                case Delete(List<Long> ids) -> {
                    register.delete(ids, TODAY);
                    deleted.addAll(ids);
                    ids.forEach(lastRecords::remove);
                    lastRecords.replaceAll((id, record) -> withoutDeleted(record, deleted));
                }
                case Register(Person person) -> {
                    List<Person> answer = register.register(person, TODAY);
                    if (!deleted.contains(person.id())) {
                        lastRecords.put(person.id(), withoutDeleted(person, deleted));
                    }
                    Statistics.label("answer").collect(answer.isEmpty() ? "nobody matches" : "somebody matches");

                    assertThat(answer).containsExactlyInAnyOrderElementsOf(checkingEveryone(lastRecords));
                }
            }
        }
    }

    /** The oracle's own view of a deleted ID: it is not referred to any more (ADR 0013, ADR 0014). */
    private static Person withoutDeleted(Person record, Set<Long> deleted) {
        return new Person(record.id(), record.name(), record.birthDate(),
                deleted.contains(record.parent1()) ? null : record.parent1(),
                deleted.contains(record.parent2()) ? null : record.parent2(),
                deleted.contains(record.partner()) ? null : record.partner(),
                record.children().stream().filter(child -> !deleted.contains(child)).toList());
    }

    private static List<Person> checkingEveryone(Map<Long, Person> lastRecords) {
        People people = People.of(lastRecords.values());
        return everyone(lastRecords).stream()
                .filter(candidate -> ThreeKidFamily.matches(candidate, people, TODAY))
                .map(people::asKnown)
                .toList();
    }

    /** The people with a record, and everyone else a record refers to, as a person with only an ID (ADR 0010). */
    private static List<Person> everyone(Map<Long, Person> lastRecords) {
        Stream<Person> knownOnlyById = lastRecords.values().stream()
                .flatMap(person -> Stream.concat(
                        Stream.of(person.parent1(), person.parent2(), person.partner()), person.children().stream()))
                .filter(Objects::nonNull)
                .filter(id -> !lastRecords.containsKey(id))
                .distinct()
                .map(id -> new Person(id, null, null));
        return Stream.concat(lastRecords.values().stream(), knownOnlyById).toList();
    }

    /**
     * Registrations over a few IDs, and now and then a delete: mostly members of one family (parents 1 and 2, children
     * 3, 4 and 5) with some relations left out or changed, and some random records, so registrations often update,
     * contradict and complete each other, and families often match.
     */
    @Provide
    Arbitrary<List<Step>> steps() {
        Arbitrary<Step> register = Arbitraries.frequencyOf(
                        Tuple.of(3, familyMember()),
                        Tuple.of(1, randomPerson()))
                .map(Register::new);
        Arbitrary<Step> delete = Arbitraries.longs().between(1, IDS).list().ofMinSize(1).ofMaxSize(2)
                .map(Delete::new);
        return Arbitraries.frequencyOf(Tuple.of(10, register), Tuple.of(1, delete)).list().ofMinSize(1).ofMaxSize(30);
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
