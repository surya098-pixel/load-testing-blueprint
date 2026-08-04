# Contributing

Thanks for your interest in improving **load-testing-blueprint**! Issues, discussions, and pull requests are all welcome.

## Ways to help

- 🐛 [Report a bug](../../issues/new?template=bug_report.md)
- 💡 [Request a feature](../../issues/new?template=feature_request.md)
- 📖 Improve the README, examples, or Javadoc
- 🧪 Add a new simulation, chain, or injection token
- ⚡ Refactor for clarity or better DX

## Local development

```bash
git clone https://github.com/surya098-pixel/load-testing-blueprint.git
cd load-testing-blueprint

./scripts/run.sh smoke              # 1 user for 30s
./scripts/run.sh load               # ramp 1→20 rps, hold 5 min
./scripts/run.sh stress             # steps to 200 rps
./scripts/run.sh spike              # normal → 500-user peak

BASE_URL=https://your-api.example.com ./scripts/run.sh load
```

Requires **JDK 17+** and **Maven 3.9+**.

## Pull request checklist

- [ ] `./scripts/run.sh smoke` passes locally
- [ ] New endpoints added to `RestApiChains` (or a per-service module), not hardcoded in simulations
- [ ] New injection tokens follow the `NAME_N` naming pattern with matching `_DURATION` / `_TO` sub-properties
- [ ] `README.md` "Supported tokens" table updated if new tokens added
- [ ] Assertions on new simulations use `RunnerHelper.p95FailUnderMs` and `.successRateFailUnder` where relevant
- [ ] Commit messages describe the *why*, not just the *what*
- [ ] Branch is rebased on `main`

## Layer responsibilities

| Layer | Does | Never does |
|---|---|---|
| simulations | Compose chains, define injection profile, set assertions | Direct HTTP calls |
| modules | Reusable HTTP chains, one per service | Own injection or assertions |
| utilities | SEQUENCE parser, config reader | Know about specific endpoints |

## Code of Conduct

Be kind. Assume good intent. If something is wrong, [open an issue](../../issues/new).

## Questions?

[Start a discussion](../../discussions) — happy to help.
