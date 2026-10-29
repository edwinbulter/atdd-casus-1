package nl.kabulter.threekids;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The clock that decides what "today" is (ADR 0007).
 */
@Configuration
class ClockConfiguration {

    /** "Under 18" is decided on the date in the Netherlands, also when the container runs in UTC. */
    private static final ZoneId AMSTERDAM = ZoneId.of("Europe/Amsterdam");

    @Bean
    Clock clock() {
        return Clock.system(AMSTERDAM);
    }
}
