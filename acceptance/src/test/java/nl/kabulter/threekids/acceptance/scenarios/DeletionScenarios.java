package nl.kabulter.threekids.acceptance.scenarios;

import static nl.kabulter.threekids.acceptance.dsl.Person.person;

import nl.kabulter.threekids.acceptance.dsl.Person;
import nl.kabulter.threekids.acceptance.dsl.Registry;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Phase 2: deleted IDs are gone, and are ignored forever, for storage and for matching.
 */
class DeletionScenarios {

    @AutoClose
    private final Registry registry = Registry.withNobodyRegistered();

    private final Person homer = person("Homer").withId(1).bornOn("1956-05-12");
    private final Person marge = person("Marge").withId(2).bornOn("1956-10-01");
    private final Person bart = person("Bart").withId(3).bornOn("2015-04-01");
    private final Person lisa = person("Lisa").withId(4).bornOn("2017-05-09");
    private final Person maggie = person("Maggie").withId(5).bornOn("2023-01-12");

    @BeforeEach
    void theFamilyOfHomerAndMargeMatches() {
        registry.registers(homer.withPartner(marge).withChildren(bart, lisa, maggie));
        registry.registers(marge.withPartner(homer).withChildren(bart, lisa, maggie));
        registry.registers(bart.withParents(homer, marge));
        registry.registers(lisa.withParents(homer, marge));
        registry.registers(maggie.withParents(homer, marge));
        registry.answersThatTheMatchingPeopleAre(homer, marge);
    }

    @Test
    void aDeletedPersonNoLongerCountsForMatching() {
        // Given the family of Homer and Marge matches

        // When Lisa is deleted, twice
        registry.deletes(lisa);
        registry.deletes(lisa);

        // And Ada is registered
        registry.registers(person("Ada").withId(42).bornOn("1815-12-10"));

        // Then nobody matches yet: Homer and Marge have only two children left
        registry.answersThatNobodyMatchesYet();
    }
}
