package nl.kabulter.threekids.acceptance.drivers.http;

import java.net.URI;
import java.time.Duration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;

/**
 * The service under test: the image of the current build, in its own container.
 */
final class ServiceContainer implements AutoCloseable {

    private static final int PORT = 8080;

    private final GenericContainer<?> container;

    private ServiceContainer(GenericContainer<?> container) {
        this.container = container;
    }

    static ServiceContainer start() {
        DockerImageName image = DockerImageName.parse(
                System.getProperty("image.name", "three-kids") + ":" + System.getProperty("app.version", "dev"));
        GenericContainer<?> container = new GenericContainer<>(image)
                .withExposedPorts(PORT)
                .waitingFor(anyHttpAnswer())
                .withStartupTimeout(Duration.ofSeconds(60));
        container.start();
        return new ServiceContainer(container);
    }

    /**
     * Ready when the HTTP server answers at all. A listening port is not enough: Docker Desktop opens it before the
     * service runs. Any status counts, so a missing endpoint shows up in the scenario, not as a start-up timeout.
     */
    private static WaitStrategy anyHttpAnswer() {
        return Wait.forHttp("/").forStatusCodeMatching(status -> true);
    }

    URI baseUri() {
        return URI.create("http://" + container.getHost() + ":" + container.getMappedPort(PORT));
    }

    @Override
    public void close() {
        container.stop();
    }
}
