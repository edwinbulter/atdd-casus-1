package nl.kabulter.threekids.domain;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The registered people, and who matches the three-kid family pattern after each registration (ADR 0011). A
 * registration checks only the people whose match it can change, plus the people who matched before: they can stop
 * matching as time passes, and they are in the answer anyway. Registrations run one at a time behind a lock.
 */
public final class FamilyRegister {

    /** The pattern a candidate is checked against; {@link ThreeKidFamily#matches} unless a test counts the checks. */
    @FunctionalInterface
    public interface Pattern {
        boolean matches(Person person, People people, LocalDate today);
    }

    private final Lock lock = new ReentrantLock();
    private final People people = new People();
    private final Set<Long> matching = new TreeSet<>();
    private final Pattern pattern;

    public FamilyRegister() {
        this(ThreeKidFamily::matches);
    }

    public FamilyRegister(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Registers the person, replacing an earlier record with the same ID, and returns everyone who matches today. The
     * registration of a deleted ID is ignored silently (ADR 0014).
     */
    public List<Person> register(Person person, LocalDate today) {
        lock.lock();
        try {
            check(people.put(person), today);
            return matching.stream().map(id -> people.asKnown(people.recordOrId(id))).toList();
        } finally {
            lock.unlock();
        }
    }

    /** Deletes the people with these IDs and ignores them from now on, for storage and for matching. */
    public void delete(Collection<Long> ids, LocalDate today) {
        lock.lock();
        try {
            Set<Long> affected = new HashSet<>();
            ids.forEach(id -> affected.addAll(people.delete(id)));
            check(affected, today);
        } finally {
            lock.unlock();
        }
    }

    /** Checks the affected people and the people who matched, and keeps who matches now. Call with the lock held. */
    private void check(Set<Long> affected, LocalDate today) {
        Set<Long> candidates = new HashSet<>(matching);
        candidates.addAll(affected);
        for (long id : candidates) {
            if (pattern.matches(people.recordOrId(id), people, today)) {
                matching.add(id);
            } else {
                matching.remove(id);
            }
        }
    }
}
