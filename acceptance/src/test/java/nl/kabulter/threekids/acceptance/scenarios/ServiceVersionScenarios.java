package nl.kabulter.threekids.acceptance.scenarios;

import nl.kabulter.threekids.acceptance.dsl.Build;
import nl.kabulter.threekids.acceptance.dsl.Operator;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.Test;

class ServiceVersionScenarios {

    @AutoClose
    private final Operator operator = new Operator();

    @Test
    void theServiceReportsTheVersionItWasBuiltFrom() {
        // Given the service has been built from the current working tree
        String builtVersion = Build.currentVersion();

        // When an operator asks the service which version it runs
        operator.asksForTheRunningVersion();

        // Then the service reports exactly that version
        operator.seesVersion(builtVersion);
    }
}
