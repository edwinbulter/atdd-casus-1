package nl.kabulter.threekids.acceptance.scenarios;

import static nl.kabulter.threekids.acceptance.dsl.Person.person;

import nl.kabulter.threekids.acceptance.dsl.Person;
import nl.kabulter.threekids.acceptance.dsl.Registry;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Registering a known ID again replaces the old record: the same record again changes nothing, a new one can end a
 * match.
 */
class UpdateScenarios {

    @AutoClose
    private final Registry registry = Registry.withNobodyRegistered();

    private final Person homer = person("Homer").withId(1).bornOn("1956-05-12");
    private final Person marge = person("Marge").withId(2).bornOn("1956-10-01");
    private final Person bart = person("Bart").withId(3).bornOn("2015-04-01");
    private final Person lisa = person("Lisa").withId(4).bornOn("2017-05-09");
    private final Person maggie = person("Maggie").withId(5).bornOn("2023-01-12");
    private final Person edna = person("Edna").withId(6);

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
    void registeringTheSameRecordAgainChangesNothing() {
        // Given the family of Homer and Marge matches

        // When Maggie is registered again with exactly the same record
        registry.registers(maggie);

        // Then Homer and Marge still match
        registry.answersThatTheMatchingPeopleAre(homer, marge);
    }

    @Test
    void aNewRecordForAKnownIdReplacesTheOldOneAndCanEndAMatch() {
        // Given the family of Homer and Marge matches

        // When Lisa is registered again, now with the parents Homer and Edna
        registry.registers(lisa.withParents(homer, edna));

        // Then nobody matches yet
        registry.answersThatNobodyMatchesYet();
    }
}
