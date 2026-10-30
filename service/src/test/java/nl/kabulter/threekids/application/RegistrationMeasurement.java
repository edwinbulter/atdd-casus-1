package nl.kabulter.threekids.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nl.kabulter.threekids.domain.Person;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Measures the time per registration in-process, without HTTP: not a test, and not part of the commit stage (tag
 * {@code perf}, run with {@code make perf}). The population is families of five: two partners who name each other
 * and three children who name both parents. In ten families a child is under 18, so twenty people match.
 */
@Tag("perf")
class RegistrationMeasurement {

    private static final ZoneId AMSTERDAM = ZoneId.of("Europe/Amsterdam");
    private static final Clock TODAY = Clock.fixed(LocalDate.of(2026, 10, 30).atStartOfDay(AMSTERDAM).toInstant(), AMSTERDAM);
    private static final int FAMILY = 5;
    private static final int LAST = 100;
    private static final int YOUNG_FAMILIES = 10;

    @Test
    void timePerRegistration() {
        System.out.printf("%n%10s %12s %18s %22s%n", "people", "total (s)", "average (µs)", "last 100 average (µs)");
        for (int size : sizes()) {
            PeopleService people = new PeopleService(TODAY);
            List<Person> population = population(size);
            long start = System.nanoTime();
            long lastStart = start;
            int matching = 0;
            for (int i = 0; i < population.size(); i++) {
                if (i == population.size() - LAST) {
                    lastStart = System.nanoTime();
                }
                matching = people.register(population.get(i)).size();
            }
            long end = System.nanoTime();
            System.out.printf("%10d %12.1f %18.0f %22.0f   (%d matching)%n", size, (end - start) / 1e9,
                    (end - start) / 1e3 / size, (end - lastStart) / 1e3 / LAST, matching);
        }
    }

    private static int[] sizes() {
        return Arrays.stream(System.getProperty("perf.sizes", "1000,5000,10000").split(","))
                .mapToInt(size -> Integer.parseInt(size.trim()))
                .toArray();
    }

    private static List<Person> population(int size) {
        List<Person> population = new ArrayList<>();
        for (int family = 0; family * FAMILY < size; family++) {
            long first = (long) family * FAMILY + 1;
            long second = first + 1;
            List<Long> children = List.of(first + 2, first + 3, first + 4);
            LocalDate youngest = family < YOUNG_FAMILIES ? LocalDate.of(2020, 1, 1) : LocalDate.of(1990, 1, 1);
            population.add(new Person(first, "Parent " + first, LocalDate.of(1960, 1, 1), null, null, second, children));
            population.add(new Person(second, "Parent " + second, LocalDate.of(1961, 1, 1), null, null, first, children));
            population.add(new Person(first + 2, "Child " + (first + 2), LocalDate.of(1985, 1, 1), first, second, null, List.of()));
            population.add(new Person(first + 3, "Child " + (first + 3), LocalDate.of(1987, 1, 1), first, second, null, List.of()));
            population.add(new Person(first + 4, "Child " + (first + 4), youngest, first, second, null, List.of()));
        }
        return population.subList(0, size);
    }
}
