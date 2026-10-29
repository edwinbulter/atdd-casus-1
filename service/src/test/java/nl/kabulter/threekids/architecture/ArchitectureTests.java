package nl.kabulter.threekids.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The guardrails from the ATDD profile (CLAUDE.md, "Guardrails"). Each rule has been seen red once by sabotage.
 *
 * <p>ArchUnit fails a rule that checks no classes. The rules about one layer allow that, because a layer may still be
 * empty; the rules about all classes do not, so a wrong root package turns them red.
 */
@AnalyzeClasses(packages = "nl.kabulter.threekids", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTests {

    @ArchTest
    static final ArchRule layers = layeredArchitecture().consideringOnlyDependenciesInLayers()
            .layer("api").definedBy("..api..")
            .layer("application").definedBy("..application..")
            .layer("domain").definedBy("..domain..")
            .whereLayer("api").mayNotBeAccessedByAnyLayer()
            .whereLayer("application").mayOnlyBeAccessedByLayers("api")
            .whereLayer("domain").mayOnlyBeAccessedByLayers("api", "application")
            .withOptionalLayers(true);

    @ArchTest
    static final ArchRule noCycles = slices().matching("nl.kabulter.threekids.(*)..").should().beFreeOfCycles()
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule domainKnowsNoFramework = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule dtosStayInApi = noClasses().that().resideOutsideOfPackage("..api..")
            .should().dependOnClassesThat().resideInAPackage("..api..");

    @ArchTest
    static final ArchRule noSynchronizedMethods = noMethods()
            .that().areDeclaredInClassesThat().resideInAnyPackage("..api..", "..application..", "..domain..")
            .should().haveModifier(JavaModifier.SYNCHRONIZED)
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule noSystemClock = noClasses()
            .should().callMethod(LocalDate.class, "now")
            .orShould().callMethod(LocalDateTime.class, "now")
            .orShould().callMethod(Instant.class, "now");
}
