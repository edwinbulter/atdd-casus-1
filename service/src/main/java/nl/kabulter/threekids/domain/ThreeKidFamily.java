package nl.kabulter.threekids.domain;

/**
 * The pattern of the casus. So far it checks that a person has a partner and three children; the rules about the
 * children (their second parent, their age) come with the scenarios that need them (doc/backlog.md).
 */
public final class ThreeKidFamily {

    private static final int CHILDREN = 3;

    private ThreeKidFamily() {
    }

    public static boolean matches(Person person) {
        return person.partner() != null && person.children().size() == CHILDREN;
    }
}
