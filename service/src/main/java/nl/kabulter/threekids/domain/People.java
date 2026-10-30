package nl.kabulter.threekids.domain;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * All registered people at one moment, as the pattern reads them. Built once per registration: the indexes from a
 * parent to the children who name that parent, and from a person to the person who names them as partner, cost one
 * pass over the records each (ADR 0005, ADR 0009, ADR 0010).
 */
public final class People {

    private final List<Person> records;
    private final Map<Long, Person> byId;
    private final Map<Long, List<Long>> childrenNamingParent;
    private final Map<Long, Long> partnerNaming;

    private People(List<Person> records) {
        this.records = records;
        this.byId = records.stream().collect(Collectors.toUnmodifiableMap(Person::id, Function.identity()));
        this.childrenNamingParent = records.stream()
                .flatMap(child -> Stream.of(child.parent1(), child.parent2())
                        .filter(Objects::nonNull)
                        .map(parent -> Map.entry(parent, child.id())))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toUnmodifiableList())));
        this.partnerNaming = records.stream()
                .filter(person -> person.partner() != null)
                .collect(Collectors.toUnmodifiableMap(Person::partner, Person::id, (first, later) -> first));
    }

    public static People of(Collection<Person> records) {
        return new People(records.stream().sorted(Comparator.comparingLong(Person::id)).toList());
    }

    /** Everyone the pattern considers: the people with a record, and the people only known by ID from others. */
    public List<Person> everyone() {
        Stream<Person> knownOnlyById = records.stream()
                .flatMap(person -> Stream.concat(
                        Stream.of(person.parent1(), person.parent2(), person.partner()), person.children().stream()))
                .filter(Objects::nonNull)
                .filter(id -> !byId.containsKey(id))
                .distinct()
                .sorted()
                .map(id -> new Person(id, null, null));
        return Stream.concat(records.stream(), knownOnlyById).toList();
    }

    /** The person's partner: the own record wins; without one, the person who names this person as partner. */
    public Long partnerOf(Person person) {
        return person.partner() != null ? person.partner() : partnerNaming.get(person.id());
    }

    /** The person's children: first the person's own list, then the children whose records name the person. */
    public List<Long> childrenOf(Person person) {
        Set<Long> children = new LinkedHashSet<>(person.children());
        children.addAll(childrenNamingParent.getOrDefault(person.id(), List.of()));
        return List.copyOf(children);
    }

    /** The person with everything known: the own record, with the partner and the children the others name. */
    public Person asKnown(Person person) {
        return new Person(person.id(), person.name(), person.birthDate(),
                person.parent1(), person.parent2(), partnerOf(person), childrenOf(person));
    }

    /** The record with this ID, or {@code null} when the person is only known by ID. */
    public Person get(long id) {
        return byId.get(id);
    }
}
