package nl.kabulter.threekids.acceptance.scenarios;

import static nl.kabulter.threekids.acceptance.dsl.Person.person;

import nl.kabulter.threekids.acceptance.dsl.Person;
import nl.kabulter.threekids.acceptance.dsl.Registry;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.Test;

class MatchingScenarios {

    @AutoClose
    private final Registry registry = Registry.withNobodyRegistered();

    private final Person homer = person("Homer").withId(1).bornOn("1956-05-12");
    private final Person marge = person("Marge").withId(2).bornOn("1956-10-01");
    private final Person bart = person("Bart").withId(3).bornOn("2015-04-01");
    private final Person lisa = person("Lisa").withId(4).bornOn("2017-05-09");
    private final Person maggie = person("Maggie").withId(5).bornOn("2023-01-12");
    private final Person edna = person("Edna").withId(6);

    @Test
    void aSinglePersonWithoutAFamilyDoesNotMatch() {
        // Given a fresh service with nobody registered

        // When Ada is registered, without parents, partner or children
        registry.registers(person("Ada").withId(42).bornOn("1815-12-10"));

        // Then nobody matches yet
        registry.answersThatNobodyMatchesYet();
    }

    @Test
    void partnersWithThreeChildrenOneOfThemUnder18Match() {
        // Given Homer and Marge are registered as partners, each with the children Bart, Lisa and Maggie
        registry.registers(homer.withPartner(marge).withChildren(bart, lisa, maggie));
        registry.registers(marge.withPartner(homer).withChildren(bart, lisa, maggie));

        // And Bart and Lisa are registered, each with the parents Homer and Marge
        registry.registers(bart.withParents(homer, marge));
        registry.registers(lisa.withParents(homer, marge));

        // When Maggie is registered with the parents Homer and Marge
        registry.registers(maggie.withParents(homer, marge));

        // Then Homer and Marge match, with everything known about them
        registry.answersThatTheMatchingPeopleAre(homer, marge);
    }

    @Test
    void aThreeKidFamilyWithOnlyAdultChildrenDoesNotMatch() {
        // Given Homer and Marge are registered as partners, each with the children Bart, Lisa and Maggie
        registry.registers(homer.withPartner(marge).withChildren(bart, lisa, maggie));
        registry.registers(marge.withPartner(homer).withChildren(bart, lisa, maggie));

        // And Bart and Lisa are adults, registered with the parents Homer and Marge
        registry.registers(bart.bornOn("1980-04-01").withParents(homer, marge));
        registry.registers(lisa.bornOn("1982-05-09").withParents(homer, marge));

        // When Maggie, also an adult, is registered with the parents Homer and Marge
        registry.registers(maggie.bornOn("1988-01-12").withParents(homer, marge));

        // Then nobody matches yet
        registry.answersThatNobodyMatchesYet();
    }

    @Test
    void aThreeKidFamilyWhereOneChildHasAnotherSecondParentDoesNotMatch() {
        // Given Homer and Marge are registered as partners, each with the children Bart, Lisa and Maggie
        registry.registers(homer.withPartner(marge).withChildren(bart, lisa, maggie));
        registry.registers(marge.withPartner(homer).withChildren(bart, lisa, maggie));

        // And Bart is registered with the parents Homer and Marge
        registry.registers(bart.withParents(homer, marge));

        // But Lisa is registered with the parents Homer and Edna
        registry.registers(lisa.withParents(homer, edna));

        // When Maggie is registered with the parents Homer and Marge
        registry.registers(maggie.withParents(homer, marge));

        // Then nobody matches yet: Lisa does not name Marge, and Lisa is not Marge's child
        registry.answersThatNobodyMatchesYet();
    }
}
