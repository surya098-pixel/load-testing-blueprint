package modules;

import io.gatling.javaapi.core.ChainBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * Reusable HTTP chains — the "module" layer of the framework.
 * <p>
 * Simulations compose these; they never call {@code http(...)} directly.
 * When an endpoint changes, you fix it in one place.
 */
public final class RestApiChains {

    private RestApiChains() {}

    // ---------- httpbin (public demo API) ----------

    public static final ChainBuilder getRoot =
            exec(http("GET /get")
                    .get("/get")
                    .check(status().is(200))
                    .check(jsonPath("$.url").exists()));

    public static final ChainBuilder postJson =
            exec(http("POST /post")
                    .post("/post")
                    .header("Content-Type", "application/json")
                    .body(StringBody("{\"loadTest\":true,\"actor\":\"#{actor}\"}"))
                    .check(status().is(200))
                    .check(jsonPath("$.json.loadTest").is("true")));

    public static final ChainBuilder delay500ms =
            exec(http("GET /delay/0.5")
                    .get("/delay/0.5")
                    .check(status().is(200)));

    public static final ChainBuilder statusOk =
            exec(http("GET /status/200")
                    .get("/status/200")
                    .check(status().is(200)));

    /** Composite journey — chain of calls a single user makes. */
    public static final ChainBuilder browseAndPost =
            exec(getRoot)
                    .pause(1, 3)
                    .exec(statusOk)
                    .pause(1, 2)
                    .exec(postJson);

    // ---------- Stateful chain — extract from response, reuse in next request ----------

    /**
     * Real-world correlation pattern: call /uuid, extract the returned UUID,
     * then POST it to /anything and verify httpbin echoed it back.
     * <p>
     * Session variables (set via {@code saveAs}) persist across requests
     * within a single virtual user's session and are interpolated with {@code #{name}}.
     */
    public static final ChainBuilder statefulChain =
            exec(http("GET /uuid — obtain a token")
                    .get("/uuid")
                    .check(status().is(200))
                    .check(jsonPath("$.uuid").saveAs("obtainedUuid")))
                    .pause(1)
                    .exec(http("POST /anything — use the token")
                            .post("/anything")
                            .header("X-Correlation-Id", "#{obtainedUuid}")
                            .body(StringBody("{\"token\":\"#{obtainedUuid}\",\"actor\":\"#{actor}\"}"))
                            .check(status().is(200))
                            // httpbin echoes the body back — assert the token round-tripped correctly.
                            .check(jsonPath("$.json.token").isEL("#{obtainedUuid}"))
                            .check(jsonPath("$.headers.X-Correlation-Id").isEL("#{obtainedUuid}")));
}
