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
}
