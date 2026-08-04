package utilities;

/**
 * Central place to read runtime system properties passed via {@code -DPROP=value}.
 * <p>
 * All simulations pull config from here so a single simulation class can be re-run
 * as smoke / load / stress / spike just by changing CLI flags — no code change.
 */
public final class RunnerHelper {

    private RunnerHelper() {}

    /** Base URL under test — e.g. {@code -DBASE_URL=https://api.example.com}. */
    public static final String baseUrl = System.getProperty("BASE_URL", "https://httpbin.org");

    /** Optional auth token, read from env var (never pass secrets on the CLI). */
    public static final String authToken = System.getenv("LOAD_TEST_AUTH_TOKEN");

    /** Simple overrides that skip the SEQUENCE parser entirely. */
    public static final int    userCount    = parseInt("USERS", 0);
    public static final long   maxDuration  = parseLong("MAX_DURATION", 0);

    /** Global throttling: cap RPS across the whole simulation. 0 = no cap. */
    public static final int throttleRps         = parseInt("THROTTLE_RPS", 0);
    public static final long throttleHoldFor    = parseLong("THROTTLE_HOLD_FOR", 0);

    /** Fail the build if p95 exceeds this many ms. 0 = disabled. */
    public static final long p95FailUnderMs = parseLong("P95_FAIL_UNDER_MS", 0);

    /** Fail the build if success rate falls below this percentage. 0 = disabled. */
    public static final double successRateFailUnder = parseDouble("SUCCESS_RATE_FAIL_UNDER", 0);

    private static int parseInt(String key, int def) {
        String v = System.getProperty(key);
        return (v == null || v.isBlank()) ? def : Integer.parseInt(v.trim());
    }

    private static long parseLong(String key, long def) {
        String v = System.getProperty(key);
        return (v == null || v.isBlank()) ? def : Long.parseLong(v.trim());
    }

    private static double parseDouble(String key, double def) {
        String v = System.getProperty(key);
        return (v == null || v.isBlank()) ? def : Double.parseDouble(v.trim());
    }
}
