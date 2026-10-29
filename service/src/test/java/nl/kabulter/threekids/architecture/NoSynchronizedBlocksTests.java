package nl.kabulter.threekids.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * ArchUnit sees {@code synchronized} on a method, but not a {@code synchronized} block. This test scans the sources.
 */
class NoSynchronizedBlocksTests {

    private static final Path MAIN_SOURCES = Path.of("src/main/java");
    private static final Pattern SYNCHRONIZED = Pattern.compile("\\bsynchronized\\b");

    @Test
    void noProductionSourceUsesSynchronized() throws IOException {
        assertThat(MAIN_SOURCES).isDirectory();
        try (Stream<Path> files = Files.walk(MAIN_SOURCES)) {
            List<Path> offenders = files
                    .filter(file -> file.toString().endsWith(".java"))
                    .filter(NoSynchronizedBlocksTests::mentionsSynchronized)
                    .toList();
            assertThat(offenders).as("sources that use synchronized").isEmpty();
        }
    }

    private static boolean mentionsSynchronized(Path file) {
        try {
            return SYNCHRONIZED.matcher(Files.readString(file)).find();
        } catch (IOException e) {
            throw new IllegalStateException("cannot read " + file, e);
        }
    }
}
