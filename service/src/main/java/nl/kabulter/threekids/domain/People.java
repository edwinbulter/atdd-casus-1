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
 * All registered people at one moment, as the pattern reads them. Built once per registration: the index from parent
 * to the children who name that parent costs one pass over the records (ADR 0005, ADR 0009).
 */
public final class People {

    private final Map<Long, Person> byId;
    private final Map<Long, List<Long>> childrenNamingParent;

    private People(Map<Long, Person> byId, Map<Long, List<Long>> childrenNamingParent) {
        this.byId = byId;
        this.childrenNamingParent = childrenNamingParent;
    }

    public static People of(Collection<Person> records) {
        Map<Long, Person> byId = records.stream()
                .collect(Collectors.toUnmodifiableMap(Person::id, Function.identity()));
        Map<Long, List<Long>> childrenNamingParent = records.stream()
                .sorted(Comparator.comparingLong(Person::id))
                .flatMap(child -> Stream.of(child.parent1(), child.parent2())
                        .filter(Objects::nonNull)
                        .map(parent -> Map.entry(parent, child.id())))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toUnmodifiableList())));
        return new People(byId, childrenNamingParent);
    }

    /** The person's children: first the person's own list, then the children whose records name the person. */
    public List<Long> childrenOf(Person person) {
        Set<Long> children = new LinkedHashSet<>(person.children());
        children.addAll(childrenNamingParent.getOrDefault(person.id(), List.of()));
        return List.copyOf(children);
    }

    /** The person with everything known: the own record, with the children from {@link #childrenOf}. */
    public Person asKnown(Person person) {
        return new Person(person.id(), person.name(), person.birthDate(),
                person.parent1(), person.parent2(), person.partner(), childrenOf(person));
    }

    /** The record with this ID, or {@code null} when the person is only known by ID. */
    public Person get(long id) {
        return byId.get(id);
    }
}
