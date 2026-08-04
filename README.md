# load-testing-blueprint

> An open-source starter template **built on top of [Gatling](https://gatling.io)** for load testing in **Java 17**.
> One codebase, four canonical profiles (smoke, load, stress, spike), and a dynamic injection DSL you drive from the CLI.
>
> *Not affiliated with, endorsed by, or a product of Gatling Corp — see [NOTICE](NOTICE).*

[![Load Test](https://github.com/surya098-pixel/load-testing-blueprint/actions/workflows/load-test.yml/badge.svg)](https://github.com/surya098-pixel/load-testing-blueprint/actions/workflows/load-test.yml)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org)
[![Gatling](https://img.shields.io/badge/Gatling-3.11-FF6B00)](https://gatling.io)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

---

## Why this template exists

Most Gatling starter examples give you one simulation class hard-coded to one shape of load. Real teams need to run **the same simulation as smoke / load / stress / spike** with a single flag change, and want the load pattern itself to be **data-driven from the command line** so pipelines can shape a run without editing code.

This template captures that pattern. It uses Gatling's open-source edition (`gatling-charts-highcharts`, Apache 2.0) as a Maven dependency — no Gatling code is bundled or redistributed here.

| Feature | Included |
|---|---|
| Gatling 3.11 + Java 17 DSL | ✅ |
| Four canonical simulations — **smoke, load, stress, spike** | ✅ |
| Dynamic **injection DSL** driven by system properties (`-DSEQUENCE=...`) | ✅ |
| Reusable HTTP chains module (the "page object" of load tests) | ✅ |
| Base simulation with shared protocol / auth / headers | ✅ |
| Runtime config: `-DBASE_URL`, `-DUSERS`, `-DMAX_DURATION`, `-DP95_FAIL_UNDER_MS`, `-DSUCCESS_RATE_FAIL_UNDER` | ✅ |
| Auth token read from env var (never committed) | ✅ |
| Per-profile assertions (smoke gates on p95, stress lets it break) | ✅ |
| One-line runner scripts + GitHub Actions CI with profile picker | ✅ |
| Gatling HTML report uploaded as artifact on every run | ✅ |

---

## Quick start

```bash
git clone https://github.com/surya098-pixel/load-testing-blueprint.git
cd load-testing-blueprint

./scripts/run.sh smoke              # 1 user for 30s — proves the wiring works
./scripts/run.sh load               # ramp 1→20 rps, hold for 5 min
./scripts/run.sh stress             # climbing steps to 200 rps for 10+ min
./scripts/run.sh spike              # normal → 500-user peak → recover

BASE_URL=https://your-api.example.com ./scripts/run.sh load
```

Open the report — path prints at the end, or:

```bash
open target/gatling/*/index.html
```

---

## The dynamic injection DSL

The killer feature. Instead of editing Java to change a load shape, **you describe the funnel on the command line** as a comma-separated sequence of tokens. Each token names its own set of properties.

```bash
-DSEQUENCE="RAMP_USERS_PER_SEC_1,CONSTANT_1,STRESS_PEAK_1,CONSTANT_2" \
-DRAMP_USERS_PER_SEC_1=1 -DRAMP_USERS_PER_SEC_1_TO=50  -DRAMP_USERS_PER_SEC_1_DURATION=60 \
-DCONSTANT_1=50            -DCONSTANT_1_DURATION=180 \
-DSTRESS_PEAK_1=500        -DSTRESS_PEAK_1_DURATION=30 \
-DCONSTANT_2=50            -DCONSTANT_2_DURATION=120
```

That builds this Gatling funnel at runtime:

```java
rampUsersPerSec(1).to(50).during(60s),
constantUsersPerSec(50).during(180s),
stressPeakUsers(500).during(30s),
constantUsersPerSec(50).during(120s)
```

### Supported tokens

| Token | Additional properties | Maps to |
|---|---|---|
| `NOTHING_*` | *(value is seconds)* | `nothingFor(N)` |
| `AT_ONCE_*` | *(value is users)* | `atOnceUsers(N)` |
| `RAMP_USERS_*` | `_DURATION` | `rampUsers(N).during(D)` |
| `RAMP_USERS_PER_SEC_*` | `_TO`, `_DURATION` | `rampUsersPerSec(F).to(T).during(D)` |
| `CONSTANT_*` | `_DURATION` | `constantUsersPerSec(N).during(D)` |
| `STRESS_PEAK_*` | `_DURATION` | `stressPeakUsers(N).during(D)` |

The `_1` / `_2` / `_N` suffix lets the same token appear multiple times in one funnel (warmup constant + sustained constant, etc.).

### Simple override

If you just want "N users for D seconds", skip SEQUENCE and pass `-DUSERS=N -DMAX_DURATION=D`.

---

## Architecture

```
src/test/
├── java/
│   ├── utilities/
│   │   ├── InjectionHelper.java     # The SEQUENCE parser (see above)
│   │   └── RunnerHelper.java        # Central system-property reader
│   ├── modules/
│   │   └── RestApiChains.java       # Reusable HTTP call chains
│   └── simulations/
│       ├── BaseSimulation.java      # Shared protocol / auth / headers
│       ├── SmokeSimulation.java     # Fixed tiny funnel — 1 user for 30s
│       ├── LoadSimulation.java      # SEQUENCE-driven, sustained expected traffic
│       ├── StressSimulation.java    # SEQUENCE-driven, climbing steps until failure
│       └── SpikeSimulation.java     # SEQUENCE-driven, sudden burst + recovery
└── resources/
    ├── logback-test.xml
    └── data/users.csv               # Sample feeder
```

### The four profiles, and when to use each

| Profile | What it does | Use it for |
|---|---|---|
| **smoke** | 1 user, 30 s | Every PR. Catches broken scripts / dead endpoints. Should ALWAYS pass. |
| **load** | Ramp to expected steady-state, hold | Nightly. Verifies the system handles normal production volume. |
| **stress** | Climb past capacity until it breaks | Weekly / before major releases. Finds the breaking point. |
| **spike** | Normal → sudden peak → back to normal | Before campaigns / launches. Verifies elasticity + recovery. |

---

## CI

`.github/workflows/load-test.yml` runs the **smoke** profile automatically on every push. Manual runs via `workflow_dispatch` accept:

- **profile** — `smoke`, `load`, `stress`, or `spike`
- **base_url** — override the target

Gatling's full HTML report is uploaded as an artifact on every run.

Set an auth token as the `LOAD_TEST_AUTH_TOKEN` repo secret — it's read from the env, never from a file.

---

## Runtime overrides

| Property | Default | Effect |
|---|---|---|
| `-DBASE_URL=...` | `https://httpbin.org` | Target base URL |
| `-DUSERS=N -DMAX_DURATION=D` | *(unset)* | Skip SEQUENCE, run `constantUsersPerSec(N).during(D)` |
| `-DP95_FAIL_UNDER_MS=2000` | 2000 (load) | Fail the build if p95 exceeds this |
| `-DSUCCESS_RATE_FAIL_UNDER=99` | 99 (load), 90 (spike) | Fail the build if success rate falls below this % |
| `LOAD_TEST_AUTH_TOKEN` *(env var)* | *(unset)* | Adds `Authorization: Bearer <token>` to every request |

---

## Adapting to your service

1. **Point at your API** — set `BASE_URL` in the CI workflow or export it locally.
2. **Add chains** — put your endpoints in `modules/RestApiChains.java` (or split into per-service modules: `OrdersChains.java`, `PaymentsChains.java`).
3. **Compose scenarios** — a simulation is just `scenario("name").exec(chain1).pause(1,3).exec(chain2)`.
4. **Set realistic assertions** — smoke should be strict (99 %+, 2 s p95). Stress should have none.
5. **Wire secrets** — auth token as env var / GitHub Actions secret.

## Part of the QA Blueprint series

Three companion templates that cover the QA automation stack end-to-end:

| | Repo | Stack | For |
|---|---|---|---|
| 🎭 | [qa-automation-blueprint](https://github.com/surya098-pixel/qa-automation-blueprint) | Playwright + TypeScript | UI end-to-end tests |
| 🥒 | [api-automation-blueprint](https://github.com/surya098-pixel/api-automation-blueprint) | RestAssured + Cucumber + Java | API contract & regression |
| 🚀 | **load-testing-blueprint** *(you are here)* | Gatling + Java | Load, stress & spike |

## License & attribution

This project (the template itself) is released under the **MIT License** — see [LICENSE](LICENSE).

It depends on third-party open-source software, most notably **Gatling** (Apache License 2.0) and its associated Maven plugin. Full attribution and trademark disclaimers are in [NOTICE](NOTICE). Dependencies are resolved from Maven Central at build time — none of their code is redistributed in this repository.

*"Gatling" is a trademark of Gatling Corp. This project is not affiliated with, endorsed by, or a product of Gatling Corp — the name is used descriptively to identify the runtime this template targets.*

© 2026 [Surya Reddy](https://github.com/surya098-pixel)

---

<sub>💡 If this template saved you time, [give it a star](../../stargazers) — it helps others find it. Track growth on [star-history](https://star-history.com/#surya098-pixel/load-testing-blueprint&Date).</sub>
