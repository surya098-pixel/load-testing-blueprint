package simulations;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import modules.RestApiChains;
import utilities.InjectionHelper;
import utilities.RunnerHelper;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * <b>Stateful chain simulation</b> — demonstrates the real-world correlation pattern:
 * extract a value from response N, use it in request N+1, and verify the round-trip.
 * <p>
 * Also demonstrates the <b>CSV feeder</b>: each virtual user gets an {@code actor}
 * value from {@code src/test/resources/data/users.csv}, cycling through in order.
 * <p>
 * <b>Recommended invocation:</b>
 * <pre>
 *   mvn gatling:test -Dgatling.simulationClass=simulations.StatefulChainSimulation \
 *     -DBASE_URL=https://httpbin.org \
 *     -DSEQUENCE="RAMP_USERS_1,CONSTANT_1" \
 *     -DRAMP_USERS_1=5 -DRAMP_USERS_1_DURATION=10 \
 *     -DCONSTANT_1=5 -DCONSTANT_1_DURATION=60
 * </pre>
 */
public class StatefulChainSimulation extends BaseSimulation {

    private final FeederBuilder<String> usersFeeder = csv("data/users.csv").circular();

    private final ScenarioBuilder scenario = scenario("Stateful chain — feed → GET → extract → POST → verify")
            .feed(usersFeeder)
            .exec(RestApiChains.statefulChain);

    {
        int users = RunnerHelper.userCount > 0 ? RunnerHelper.userCount : 5;
        long duration = RunnerHelper.maxDuration > 0 ? RunnerHelper.maxDuration : 60;

        setUp(
                scenario.injectOpen(
                        InjectionHelper.buildInjectionSteps().isEmpty()
                                ? java.util.List.of(rampUsers(users).during(Duration.ofSeconds(duration)))
                                : InjectionHelper.buildInjectionSteps()
                )
        )
                .protocols(httpProtocol)
                .assertions(
                        global().successfulRequests().percent().gte(99.0),
                        global().responseTime().percentile(95).lt(2500)
                );
    }
}
