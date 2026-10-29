package nl.kabulter.threekids.acceptance.drivers;

import java.util.List;

/**
 * What the service answers to a registration, in domain terms: the people that match now.
 */
public record RegistrationAnswer(List<PersonRecord> matchingPeople) {

    public RegistrationAnswer {
        matchingPeople = List.copyOf(matchingPeople);
    }

    public static RegistrationAnswer nobodyMatchesYet() {
        return new RegistrationAnswer(List.of());
    }
}
