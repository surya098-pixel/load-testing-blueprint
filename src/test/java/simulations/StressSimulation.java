package simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import modules.RestApiChains;
import utilities.InjectionHelper;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * <b>Stress test</b> — push past expected capacity to find the breaking point.
 * Assertions are relaxed on purpose: we WANT to see when things start failing.
 * <p>
 * <b>Recommended invocation:</b>
 * <pre>
 *   mvn gatling:test -Dgatling.simulationClass=simulations.StressSimulation \
 *     -DBASE_URL=https://httpbin.org \
 *     -DSEQUENCE="RAMP_USERS_PER_SEC_1,CONSTANT_1,RAMP_USERS_PER_SEC_2,CONSTANT_2" \
 *     -DRAMP_USERS_PER_SEC_1=1 -DRAMP_USERS_PER_SEC_1_TO=50 -DRAMP_USERS_PER_SEC_1_DURATION=60 \
 *     -DCONSTANT_1=50 -DCONSTANT_1_DURATION=180 \
 *     -DRAMP_USERS_PER_SEC_2=50 -DRAMP_USERS_PER_SEC_2_TO=200 -DRAMP_USERS_PER_SEC_2_DURATION=120 \
 *     -DCONSTANT_2=200 -DCONSTANT_2_DURATION=180
 * </pre>
 */
public class StressSimulation extends BaseSimulation {

    private final ScenarioBuilder scenario = scenario("Stress — climbing steps until failure")
            .exec(RestApiChains.browseAndPost);

    {
        setUp(
                scenario.injectOpen(InjectionHelper.buildInjectionSteps())
        )
                .protocols(httpProtocol)
                .maxDuration(java.time.Duration.ofMinutes(30));
        // No assertions — we want the run to complete even as errors climb, so we can
        // see the breaking point in the Gatling report.
    }
}
