package nl.kabulter.threekids.acceptance.dsl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import nl.kabulter.threekids.acceptance.drivers.RegistrationAnswer;
import nl.kabulter.threekids.acceptance.drivers.ServiceDriver;
import nl.kabulter.threekids.acceptance.drivers.http.HttpServiceDriver;

/**
 * The service as a register of people: people are registered one by one, and each registration says who matches.
 */
public final class Registry implements AutoCloseable {

    private final ServiceDriver service;
    private RegistrationAnswer lastAnswer;

    private Registry(ServiceDriver service) {
        this.service = service;
    }

    public static Registry withNobodyRegistered() {
        return new Registry(HttpServiceDriver.onAFreshService());
    }

    public void registers(Person person) {
        lastAnswer = service.register(person.toRecord());
    }

    public void answersThatNobodyMatchesYet() {
        assertThat(lastAnswer).as("the answer to the last registration").isEqualTo(RegistrationAnswer.nobodyMatchesYet());
    }

    public void answersThatTheMatchingPeopleAre(Person... people) {
        assertThat(lastAnswer.matchingPeople()).as("the matching people in the answer to the last registration")
                .containsExactlyInAnyOrderElementsOf(Arrays.stream(people).map(Person::toRecord).toList());
    }

    @Override
    public void close() {
        service.close();
    }
}
