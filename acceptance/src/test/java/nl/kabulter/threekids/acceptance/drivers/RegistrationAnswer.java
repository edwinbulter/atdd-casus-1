package nl.kabulter.threekids.acceptance.drivers;

/**
 * What the service answers to a registration, in domain terms.
 */
public record RegistrationAnswer(boolean somebodyMatches) {

    public static RegistrationAnswer nobodyMatchesYet() {
        return new RegistrationAnswer(false);
    }
}
