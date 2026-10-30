package nl.kabulter.threekids.acceptance.drivers;

import java.util.List;

/**
 * How the DSL talks to the service. Implementations make no assertions about behaviour.
 */
public interface ServiceDriver extends AutoCloseable {

    String runningVersion();

    RegistrationAnswer register(PersonRecord person);

    void delete(List<Long> ids);

    @Override
    void close();
}
