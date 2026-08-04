# Changelog

All notable changes to this project will be documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/); versioning follows [SemVer](https://semver.org/).

## [Unreleased]

## [0.2.0] - 2026-08-05

### Added
- **`StatefulChainSimulation`** — 5th profile demonstrating real-world correlation: extract from response, reuse in next request, verify round-trip
- **CSV feeder wired** — `data/users.csv` now drives per-VU `#{actor}` values in the stateful scenario (previously the file existed but was unused)
- `statefulChain` chain in `RestApiChains` — httpbin `/uuid` → `/anything` with `X-Correlation-Id` propagation
- `scripts/run.sh stateful` profile
- README documents the correlation pattern with an inline snippet
- **Live sample report** auto-deployed to GitHub Pages on every push to `main`

[0.2.0]: https://github.com/surya098-pixel/load-testing-blueprint/releases/tag/v0.2.0

## [0.1.0] - 2026-08-05

### Added
- Initial public release
- Gatling 3.11 + Java 17 load testing framework
- Four canonical simulations: **smoke**, **load**, **stress**, **spike**
- Dynamic injection DSL — build funnels from CLI system properties (`-DSEQUENCE=…`)
- Supported injection tokens: `NOTHING`, `AT_ONCE`, `RAMP_USERS`, `RAMP_USERS_PER_SEC`, `CONSTANT`, `STRESS_PEAK`
- Simple `-DUSERS=N -DMAX_DURATION=D` override that skips the SEQUENCE parser
- `RestApiChains` module — reusable HTTP call chains
- `BaseSimulation` — shared protocol / auth / headers
- `RunnerHelper` — central system-property reader with p95 + success-rate gates
- Sample CSV feeder for parameterized data
- Auth token via `LOAD_TEST_AUTH_TOKEN` env var (never committed)
- One-line `scripts/run.sh` wrapper for all four profiles
- GitHub Actions CI with profile picker (`workflow_dispatch`)
- Gatling HTML report uploaded as CI artifact
- NOTICE file with Apache 2.0 attribution for Gatling
- Explicit disclaimer that this project is not affiliated with Gatling Corp

[Unreleased]: https://github.com/surya098-pixel/load-testing-blueprint/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/surya098-pixel/load-testing-blueprint/releases/tag/v0.1.0
