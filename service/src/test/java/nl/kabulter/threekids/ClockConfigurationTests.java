package nl.kabulter.threekids;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class ClockConfigurationTests {

    @Test
    void todayIsTheDateInAmsterdam() {
        assertThat(new ClockConfiguration().clock().getZone()).isEqualTo(ZoneId.of("Europe/Amsterdam"));
    }
}
