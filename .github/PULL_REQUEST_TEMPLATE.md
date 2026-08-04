## Summary
<!-- What changed and why. Keep it short. -->

## Type of change
- [ ] Bug fix
- [ ] New feature (new simulation / chain / injection token)
- [ ] Docs / examples
- [ ] Refactor / cleanup
- [ ] CI / tooling

## Checklist
- [ ] `./scripts/run.sh smoke` passes locally
- [ ] New endpoints added to `RestApiChains` (or a per-service module)
- [ ] New injection tokens follow the `NAME_N` + `_DURATION` / `_TO` naming pattern
- [ ] README "Supported tokens" table updated if new tokens added
- [ ] Assertions use `RunnerHelper.p95FailUnderMs` / `.successRateFailUnder` where relevant
- [ ] Commit message explains the *why*
- [ ] Branch rebased on `main`
