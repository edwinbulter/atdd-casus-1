package nl.kabulter.threekids.domain;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * The registered people, as the pattern reads them, with indexes that are kept up to date on every {@link #put}
 * (ADR 0011): from a parent to the children who name that parent (ADR 0009), from a person to the people who name
 * them as partner (ADR 0010), and from a child to the people who list it as a child. Not thread-safe: the
 * {@link FamilyRegister} guards it.
 */
public final class People {

    private final Map<Long, Person> byId = new HashMap<>();
    private final Map<Long, NavigableSet<Long>> childrenNamingParent = new HashMap<>();
    private final Map<Long, NavigableSet<Long>> partnerNaming = new HashMap<>();
    private final Map<Long, NavigableSet<Long>> listingAsChild = new HashMap<>();
    private final Set<Long> deleted = new HashSet<>();

    /** The people with these records, as if they were registered in this order. */
    public static People of(Collection<Person> records) {
        People people = new People();
        records.forEach(people::put);
        return people;
    }

    /**
     * Stores the record without references to deleted IDs, replacing an earlier record with the same ID; returns that
     * earlier record, or null. The caller ignores the record of a deleted ID ({@link #isDeleted}).
     */
    public Person put(Person person) {
        Person stored = person.withoutReferencesTo(deleted);
        Person earlier = byId.put(stored.id(), stored);
        if (earlier != null) {
            index(earlier, false);
        }
        index(stored, true);
        return earlier;
    }

    /** Whether this ID was deleted: it is ignored forever, for storage and for matching. */
    public boolean isDeleted(long id) {
        return deleted.contains(id);
    }

    /**
     * The people whose match can change when {@code earlier} is replaced by {@code person}: the person, the people
     * the old or new record names, and the people who name the person as partner or list the person as a child.
     */
    public Set<Long> affectedBy(Person earlier, Person person) {
        Set<Long> affected = new HashSet<>();
        affected.add(person.id());
        Stream.of(earlier, person)
                .filter(Objects::nonNull)
                .flatMap(record -> Stream.of(record.parent1(), record.parent2(), record.partner()))
                .filter(Objects::nonNull)
                .forEach(affected::add);
        affected.addAll(partnerNaming.getOrDefault(person.id(), new TreeSet<>()));
        affected.addAll(listingAsChild.getOrDefault(person.id(), new TreeSet<>()));
        return affected;
    }

    /**
     * Deletes the person with this ID: the record goes, and every record that refers to the person is stored again,
     * which removes the reference ({@link #put}); from now on the ID is ignored. Returns the people whose match can
     * change.
     */
    public Set<Long> delete(long id) {
        if (!deleted.add(id)) {
            return Set.of();
        }
        Set<Long> affected = new HashSet<>();
        Person record = byId.remove(id);
        if (record != null) {
            index(record, false);
            affected.addAll(affectedBy(record, record));
        }
        Set<Long> referring = new TreeSet<>();
        referring.addAll(childrenNamingParent.getOrDefault(id, new TreeSet<>()));
        referring.addAll(partnerNaming.getOrDefault(id, new TreeSet<>()));
        referring.addAll(listingAsChild.getOrDefault(id, new TreeSet<>()));
        for (long other : referring) {
            Person earlier = put(byId.get(other));
            affected.addAll(affectedBy(earlier, byId.get(other)));
        }
        affected.remove(id);
        return affected;
    }

    /** Everyone the pattern considers: the people with a record, and the people only known by ID from others. */
    public List<Person> everyone() {
        List<Person> records = byId.values().stream().sorted(Comparator.comparingLong(Person::id)).toList();
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

    /** The person's partner: the own record wins; without one, the person with the lowest ID who names this person. */
    public Long partnerOf(Person person) {
        if (person.partner() != null) {
            return person.partner();
        }
        NavigableSet<Long> naming = partnerNaming.get(person.id());
        return naming == null ? null : naming.first();
    }

    /** The person's children: first the person's own list, then the children whose records name the person. */
    public List<Long> childrenOf(Person person) {
        Set<Long> children = new LinkedHashSet<>(person.children());
        children.addAll(childrenNamingParent.getOrDefault(person.id(), new TreeSet<>()));
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

    /** The record with this ID, or a person with only this ID. */
    public Person recordOrId(long id) {
        Person record = byId.get(id);
        return record != null ? record : new Person(id, null, null);
    }

    private void index(Person person, boolean add) {
        Stream.of(person.parent1(), person.parent2()).filter(Objects::nonNull).distinct()
                .forEach(parent -> update(childrenNamingParent, parent, person.id(), add));
        if (person.partner() != null) {
            update(partnerNaming, person.partner(), person.id(), add);
        }
        person.children().forEach(child -> update(listingAsChild, child, person.id(), add));
    }

    private static void update(Map<Long, NavigableSet<Long>> index, long key, long value, boolean add) {
        if (add) {
            index.computeIfAbsent(key, ignored -> new TreeSet<>()).add(value);
            return;
        }
        NavigableSet<Long> values = index.get(key);
        values.remove(value);
        if (values.isEmpty()) {
            index.remove(key);
        }
    }
}
