package utilities;

import io.gatling.javaapi.core.OpenInjectionStep;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Dynamic Gatling injection funnel built from CLI system properties.
 * <p>
 * <b>Two modes.</b>
 * <ol>
 *   <li><b>Simple override</b>: pass {@code -DUSERS=N -DMAX_DURATION=S} and the funnel
 *       becomes {@code constantUsersPerSec(N).during(S)}. Ignores SEQUENCE.</li>
 *   <li><b>SEQUENCE DSL</b>: comma-separated tokens that map to Gatling injection steps.
 *       Each token names a further property that carries its magnitude.</li>
 * </ol>
 *
 * <h3>SEQUENCE tokens</h3>
 * <pre>
 *   -DSEQUENCE="NOTHING_1,RAMP_1,CONSTANT_1,SPIKE_1"
 *   -DNOTHING_1=5                       # nothingFor(5s)
 *   -DAT_ONCE_1=100                     # atOnceUsers(100)
 *   -DRAMP_USERS_1=100                  # rampUsers(100)
 *   -DRAMP_USERS_1_DURATION=60         #   .during(60s)
 *   -DRAMP_USERS_PER_SEC_1=1           # rampUsersPerSec(1)
 *   -DRAMP_USERS_PER_SEC_1_TO=50       #   .to(50)
 *   -DRAMP_USERS_PER_SEC_1_DURATION=120#   .during(120s)
 *   -DCONSTANT_1=20                    # constantUsersPerSec(20)
 *   -DCONSTANT_1_DURATION=300          #   .during(300s)
 *   -DSTRESS_PEAK_1=500                # stressPeakUsers(500)
 *   -DSTRESS_PEAK_1_DURATION=60        #   .during(60s)
 * </pre>
 *
 * The suffix ({@code _1}, {@code _2}, ...) lets you use the same step type more than
 * once in one funnel — e.g. {@code CONSTANT_1} for warmup and {@code CONSTANT_2} for
 * sustained load.
 */
public final class InjectionHelper {

    private InjectionHelper() {}

    public static List<OpenInjectionStep> buildInjectionSteps() {
        List<OpenInjectionStep> steps = new ArrayList<>();
        List<String> dslLog = new ArrayList<>();

        // Mode 1: simple override
        String usersProp = System.getProperty("USERS");
        String maxDurProp = System.getProperty("MAX_DURATION");
        if (isSet(usersProp) && isSet(maxDurProp)) {
            int users = Integer.parseInt(usersProp.trim());
            long duration = Long.parseLong(maxDurProp.trim());
            steps.add(constantUsersPerSec(users).during(Duration.ofSeconds(duration)));
            dslLog.add(String.format("constantUsersPerSec(%d).during(%d)", users, duration));
            printPlan(dslLog);
            return steps;
        }

        // Mode 2: SEQUENCE parser
        String sequence = System.getProperty("SEQUENCE", "").trim();
        if (sequence.isEmpty()) {
            System.out.println("[InjectionHelper] No USERS / SEQUENCE provided — simulation will run with an empty funnel.");
            return steps;
        }

        for (String rawToken : sequence.split(",")) {
            String tokenKey = stripQuotes(rawToken.trim());
            String tokenType = tokenKey.toUpperCase();

            try {
                if (tokenType.startsWith("NOTHING")) {
                    int seconds = intProp(tokenKey);
                    steps.add(nothingFor(Duration.ofSeconds(seconds)));
                    dslLog.add("nothingFor(" + seconds + ")");

                } else if (tokenType.startsWith("AT_ONCE")) {
                    int users = intProp(tokenKey);
                    steps.add(atOnceUsers(users));
                    dslLog.add("atOnceUsers(" + users + ")");

                } else if (tokenType.startsWith("RAMP_USERS_PER_SEC")) {
                    int from = intProp(tokenKey);
                    int to = intProp(tokenKey + "_TO");
                    int duration = intProp(tokenKey + "_DURATION");
                    steps.add(rampUsersPerSec(from).to(to).during(Duration.ofSeconds(duration)));
                    dslLog.add(String.format("rampUsersPerSec(%d).to(%d).during(%d)", from, to, duration));

                } else if (tokenType.startsWith("CONSTANT")) {
                    int users = intProp(tokenKey);
                    int duration = intProp(tokenKey + "_DURATION");
                    steps.add(constantUsersPerSec(users).during(Duration.ofSeconds(duration)));
                    dslLog.add(String.format("constantUsersPerSec(%d).during(%d)", users, duration));

                } else if (tokenType.startsWith("RAMP_USERS")) {
                    int users = intProp(tokenKey);
                    int duration = intProp(tokenKey + "_DURATION");
                    steps.add(rampUsers(users).during(Duration.ofSeconds(duration)));
                    dslLog.add(String.format("rampUsers(%d).during(%d)", users, duration));

                } else if (tokenType.startsWith("STRESS_PEAK")) {
                    int users = intProp(tokenKey);
                    int duration = intProp(tokenKey + "_DURATION");
                    steps.add(stressPeakUsers(users).during(Duration.ofSeconds(duration)));
                    dslLog.add(String.format("stressPeakUsers(%d).during(%d)", users, duration));

                } else {
                    System.out.println("[InjectionHelper] Unknown token, skipping: " + rawToken);
                }
            } catch (Exception e) {
                System.out.println("[InjectionHelper] Failed to parse token '" + rawToken + "': " + e.getMessage());
            }
        }

        printPlan(dslLog);
        return steps;
    }

    private static boolean isSet(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static int intProp(String key) {
        return Integer.parseInt(System.getProperty(key, "0").trim());
    }

    private static String stripQuotes(String s) {
        if (s.startsWith("\"")) s = s.substring(1);
        if (s.endsWith("\"")) s = s.substring(0, s.length() - 1);
        return s;
    }

    private static void printPlan(List<String> dsl) {
        System.out.println("========== Injection Plan ==========");
        for (String s : dsl) System.out.println("  " + s);
        System.out.println("====================================");
    }
}
