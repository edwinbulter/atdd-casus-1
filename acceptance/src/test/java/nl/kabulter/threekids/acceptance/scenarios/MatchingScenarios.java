package nl.kabulter.threekids.acceptance.scenarios;

import static nl.kabulter.threekids.acceptance.dsl.Person.person;

import nl.kabulter.threekids.acceptance.dsl.Registry;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.Test;

class MatchingScenarios {

    @AutoClose
    private final Registry registry = Registry.withNobodyRegistered();

    @Test
    void aSinglePersonWithoutAFamilyDoesNotMatch() {
        // Given a fresh service with nobody registered

        // When Ada is registered, without parents, partner or children
        registry.registers(person("Ada").withId(42).bornOn("1815-12-10"));

        // Then nobody matches yet
        registry.answersThatNobodyMatchesYet();
    }
}
