package nl.kabulter.threekids.acceptance.drivers;

/**
 * How the DSL talks to the service. Implementations make no assertions about behaviour.
 */
public interface ServiceDriver extends AutoCloseable {

    String runningVersion();

    RegistrationAnswer register(PersonRecord person);

    @Override
    void close();
}
