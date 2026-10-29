package nl.kabulter.threekids.acceptance.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The four-layer acceptance test model (ADR 0004): scenarios speak only the DSL, and drivers make no assertions.
 * Runs in the commit stage; the scenarios themselves only run in the acceptance stage.
 */
@AnalyzeClasses(packages = "nl.kabulter.threekids.acceptance")
class FourLayerTests {

    @ArchTest
    static final ArchRule scenariosSpeakOnlyTheDsl = noClasses().that().resideInAPackage("..scenarios..")
            .should().dependOnClassesThat().resideInAnyPackage("..drivers..", "java.net..", "tools.jackson..");

    @ArchTest
    static final ArchRule driversMakeNoAssertions = noClasses().that().resideInAPackage("..drivers..")
            .should().dependOnClassesThat().resideInAnyPackage("org.assertj..", "org.junit..", "..dsl..", "..scenarios..");
}
