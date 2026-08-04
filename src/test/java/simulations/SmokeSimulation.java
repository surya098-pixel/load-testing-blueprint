package simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import modules.RestApiChains;
import utilities.RunnerHelper;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * <b>Smoke test</b> — smallest possible run, just proves the system is up and the
 * scripts work. Ship this in CI on every PR.
 * <p>
 * <b>Recommended invocation:</b>
 * <pre>
 *   mvn gatling:test -Dgatling.simulationClass=simulations.SmokeSimulation \
 *     -DBASE_URL=https://httpbin.org \
 *     -DUSERS=1 -DMAX_DURATION=30
 * </pre>
 */
public class SmokeSimulation extends BaseSimulation {

    private final ScenarioBuilder scenario = scenario("Smoke — single user, full journey")
            .feed(feeder())
            .exec(RestApiChains.browseAndPost);

    private static java.util.Iterator<java.util.Map<String, Object>> feeder() {
        return listFeeder(
                java.util.List.of(java.util.Map.of("actor", "smoke-user"))
        ).circular().apply(null);
    }

    {
        int users = RunnerHelper.userCount > 0 ? RunnerHelper.userCount : 1;
        long duration = RunnerHelper.maxDuration > 0 ? RunnerHelper.maxDuration : 30;

        setUp(
                scenario.injectOpen(constantUsersPerSec(users).during(java.time.Duration.ofSeconds(duration)))
        )
                .protocols(httpProtocol)
                .assertions(
                        global().successfulRequests().percent().gte(99.0),
                        global().responseTime().percentile(95).lt(2000)
                );
    }
}
