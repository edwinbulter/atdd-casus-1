package nl.kabulter.threekids.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;
import org.junit.jupiter.api.RepeatedTest;

/**
 * Registrations and deletes that run at the same time leave the register as if they had run one after the other: no
 * lost update, no broken lookup list, no exception (ADR 0011, one at a time behind a lock).
 */
class FamilyRegisterConcurrencyTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 30);
    private static final int FAMILIES = 200;
    private static final int THREADS = 8;
    private static final Duration DEADLINE = Duration.ofSeconds(10);

    @RepeatedTest(5)
    void registrationsAndDeletesAtTheSameTimeLeaveEveryFamilyConsistent() throws Exception {
        FamilyRegister register = new FamilyRegister();
        List<Callable<Object>> mutations = new ArrayList<>();
        for (long family = 0; family < FAMILIES; family++) {
            familyMembers(family).forEach(member -> mutations.add(() -> register.register(member, TODAY)));
            if (losesAChild(family)) {
                List<Long> child = List.of(firstChildOf(family));
                mutations.add(Executors.callable(() -> register.delete(child, TODAY)));
            }
        }
        Collections.shuffle(mutations, new Random(42));

        runAllAtOnce(mutations);

        List<Long> expected = LongStream.range(0, FAMILIES)
                .filter(family -> !losesAChild(family))
                .flatMap(family -> LongStream.of(parentOf(family), parentOf(family) + 1))
                .boxed()
                .toList();
        assertThat(register.register(new Person(1, "Ada", TODAY.minusYears(50)), TODAY))
                .extracting(Person::id)
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    /**
     * Starts every mutation on a pool of threads behind one gate, so they run together, and rethrows any failure. A
     * collection that is changed by two threads at once can loop forever, so the mutations get a deadline, and the
     * threads are daemons that do not keep the test run alive.
     */
    private static void runAllAtOnce(List<Callable<Object>> mutations) throws Exception {
        CountDownLatch gate = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(THREADS, Thread.ofPlatform().daemon().factory());
        try {
            List<Future<Object>> results = new ArrayList<>();
            for (Callable<Object> mutation : mutations) {
                results.add(pool.submit(() -> {
                    gate.await();
                    return mutation.call();
                }));
            }
            gate.countDown();
            long deadline = System.nanoTime() + DEADLINE.toNanos();
            for (Future<Object> result : results) {
                result.get(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
            }
        } finally {
            pool.shutdownNow();
        }
    }

    /** A family whose first child is deleted, so the parents have two children left and do not match. */
    private static boolean losesAChild(long family) {
        return family % 4 == 0;
    }

    private static long parentOf(long family) {
        return 10 + family * 10;
    }

    private static long firstChildOf(long family) {
        return parentOf(family) + 2;
    }

    private static List<Person> familyMembers(long family) {
        long father = parentOf(family);
        long mother = father + 1;
        List<Long> children = List.of(father + 2, father + 3, father + 4);
        List<Person> members = new ArrayList<>();
        members.add(new Person(father, "Father " + family, TODAY.minusYears(40), null, null, mother, children));
        members.add(new Person(mother, "Mother " + family, TODAY.minusYears(40), null, null, father, children));
        for (long child : children) {
            members.add(new Person(child, "Child " + child, TODAY.minusYears(10), father, mother, null, List.of()));
        }
        return members;
    }
}
