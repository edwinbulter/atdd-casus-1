package nl.kabulter.threekids.acceptance.scenarios;

import static nl.kabulter.threekids.acceptance.dsl.Person.person;

import nl.kabulter.threekids.acceptance.dsl.Person;
import nl.kabulter.threekids.acceptance.dsl.Registry;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.Test;

/**
 * Bidirectional integrity: when a parent names a child and the child does not name the parent, or the reverse, the
 * missing side is repaired.
 */
class IntegrityScenarios {

    @AutoClose
    private final Registry registry = Registry.withNobodyRegistered();

    private final Person homer = person("Homer").withId(1).bornOn("1956-05-12");
    private final Person marge = person("Marge").withId(2).bornOn("1956-10-01");
    private final Person bart = person("Bart").withId(3).bornOn("2015-04-01");
    private final Person lisa = person("Lisa").withId(4).bornOn("2017-05-09");
    private final Person maggie = person("Maggie").withId(5).bornOn("2023-01-12");

    @Test
    void childrenWithoutParentsGetTheirParentsFromTheParentsRecords() {
        // Given Homer and Marge are registered as partners, each with the children Bart, Lisa and Maggie
        registry.registers(homer.withPartner(marge).withChildren(bart, lisa, maggie));
        registry.registers(marge.withPartner(homer).withChildren(bart, lisa, maggie));

        // And Bart and Lisa are registered without parents
        registry.registers(bart);
        registry.registers(lisa);

        // When Maggie is registered without parents
        registry.registers(maggie);

        // Then Homer and Marge match: the children's parents are repaired from Homer's and Marge's records
        registry.answersThatTheMatchingPeopleAre(homer, marge);
    }
}
