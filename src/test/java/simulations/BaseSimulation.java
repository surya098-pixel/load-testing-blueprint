package simulations;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import utilities.RunnerHelper;

import static io.gatling.javaapi.http.HttpDsl.http;

/**
 * Shared setup for every simulation — base URL, common headers, auth injection.
 * Subclasses just supply the scenario and injection profile.
 */
public abstract class BaseSimulation extends Simulation {

    protected final HttpProtocolBuilder httpProtocol = buildProtocol();

    private HttpProtocolBuilder buildProtocol() {
        var protocol = http
                .baseUrl(RunnerHelper.baseUrl)
                .acceptHeader("application/json")
                .acceptEncodingHeader("gzip, deflate")
                .userAgentHeader("gatling/load-testing-blueprint");

        if (RunnerHelper.authToken != null && !RunnerHelper.authToken.isBlank()) {
            protocol = protocol.authorizationHeader("Bearer " + RunnerHelper.authToken);
        }
        return protocol;
    }
}
