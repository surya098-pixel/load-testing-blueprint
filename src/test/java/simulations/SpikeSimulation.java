package simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import modules.RestApiChains;
import utilities.InjectionHelper;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * <b>Spike test</b> — sudden burst of traffic to simulate a viral event / campaign launch.
 * <p>
 * <b>Recommended invocation:</b>
 * <pre>
 *   mvn gatling:test -Dgatling.simulationClass=simulations.SpikeSimulation \
 *     -DBASE_URL=https://httpbin.org \
 *     -DSEQUENCE="CONSTANT_1,STRESS_PEAK_1,CONSTANT_2" \
 *     -DCONSTANT_1=10 -DCONSTANT_1_DURATION=60 \
 *     -DSTRESS_PEAK_1=500 -DSTRESS_PEAK_1_DURATION=30 \
 *     -DCONSTANT_2=10 -DCONSTANT_2_DURATION=60
 * </pre>
 */
public class SpikeSimulation extends BaseSimulation {

    private final ScenarioBuilder scenario = scenario("Spike — normal, sudden peak, back to normal")
            .exec(RestApiChains.browseAndPost);

    {
        setUp(
                scenario.injectOpen(InjectionHelper.buildInjectionSteps())
        )
                .protocols(httpProtocol)
                .assertions(
                        // After a spike we want to verify recovery — most requests should still succeed.
                        global().successfulRequests().percent().gte(90.0)
                );
    }
}
