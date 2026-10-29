package nl.kabulter.threekids.acceptance.dsl;

import static org.assertj.core.api.Assertions.assertThat;

import nl.kabulter.threekids.acceptance.drivers.ServiceDriver;
import nl.kabulter.threekids.acceptance.drivers.http.HttpServiceDriver;

/**
 * Someone who runs the service and wants to know which build is running.
 */
public class Operator implements AutoCloseable {

    private final ServiceDriver service = HttpServiceDriver.onAFreshService();
    private String reportedVersion;

    public void asksForTheRunningVersion() {
        reportedVersion = service.runningVersion();
    }

    public void seesVersion(String expected) {
        assertThat(reportedVersion).as("the version the service reports").isEqualTo(expected);
    }

    @Override
    public void close() {
        service.close();
    }
}
