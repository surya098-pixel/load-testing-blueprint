package simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import modules.RestApiChains;
import utilities.InjectionHelper;
import utilities.RunnerHelper;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * <b>Load test</b> — sustained expected traffic. Answers "does the system hold under
 * normal production volume?".
 * <p>
 * <b>Recommended invocation:</b>
 * <pre>
 *   mvn gatling:test -Dgatling.simulationClass=simulations.LoadSimulation \
 *     -DBASE_URL=https://httpbin.org \
 *     -DSEQUENCE="RAMP_USERS_PER_SEC_1,CONSTANT_1" \
 *     -DRAMP_USERS_PER_SEC_1=1 -DRAMP_USERS_PER_SEC_1_TO=20 -DRAMP_USERS_PER_SEC_1_DURATION=60 \
 *     -DCONSTANT_1=20 -DCONSTANT_1_DURATION=300
 * </pre>
 */
public class LoadSimulation extends BaseSimulation {

    private final ScenarioBuilder scenario = scenario("Sustained load")
            .exec(RestApiChains.browseAndPost);

    {
        setUp(
                scenario.injectOpen(InjectionHelper.buildInjectionSteps())
        )
                .protocols(httpProtocol)
                .assertions(
                        global().successfulRequests().percent().gte(
                                RunnerHelper.successRateFailUnder > 0 ? RunnerHelper.successRateFailUnder : 99.0),
                        global().responseTime().percentile(95).lt(
                                (int) (RunnerHelper.p95FailUnderMs > 0 ? RunnerHelper.p95FailUnderMs : 2000))
                );
    }
}
