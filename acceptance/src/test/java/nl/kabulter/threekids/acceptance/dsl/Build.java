package nl.kabulter.threekids.acceptance.dsl;

/**
 * The build under test. Its version comes from the build ({@code make version}), not from the service.
 */
public final class Build {

    private Build() {
    }

    public static String currentVersion() {
        String version = System.getProperty("app.version");
        if (version == null || version.isBlank()) {
            throw new IllegalStateException("app.version is not set: run the scenarios with make acceptance");
        }
        return version;
    }
}
