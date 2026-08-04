#!/usr/bin/env bash
# Convenience wrapper — pick a profile, override BASE_URL if you want.
#
#   ./scripts/run.sh smoke
#   ./scripts/run.sh load
#   ./scripts/run.sh stress
#   ./scripts/run.sh spike
#   BASE_URL=https://your-api.example.com ./scripts/run.sh load

set -euo pipefail

PROFILE="${1:-smoke}"
BASE_URL="${BASE_URL:-https://httpbin.org}"

echo "▶ Profile   : $PROFILE"
echo "▶ Target    : $BASE_URL"
echo

case "$PROFILE" in
  smoke)
    mvn -q gatling:test \
      -Dgatling.simulationClass=simulations.SmokeSimulation \
      -DBASE_URL="$BASE_URL" \
      -DUSERS=1 -DMAX_DURATION=30
    ;;

  load)
    mvn -q gatling:test \
      -Dgatling.simulationClass=simulations.LoadSimulation \
      -DBASE_URL="$BASE_URL" \
      -DSEQUENCE="RAMP_USERS_PER_SEC_1,CONSTANT_1" \
      -DRAMP_USERS_PER_SEC_1=1 -DRAMP_USERS_PER_SEC_1_TO=20 -DRAMP_USERS_PER_SEC_1_DURATION=60 \
      -DCONSTANT_1=20 -DCONSTANT_1_DURATION=300
    ;;

  stress)
    mvn -q gatling:test \
      -Dgatling.simulationClass=simulations.StressSimulation \
      -DBASE_URL="$BASE_URL" \
      -DSEQUENCE="RAMP_USERS_PER_SEC_1,CONSTANT_1,RAMP_USERS_PER_SEC_2,CONSTANT_2" \
      -DRAMP_USERS_PER_SEC_1=1 -DRAMP_USERS_PER_SEC_1_TO=50 -DRAMP_USERS_PER_SEC_1_DURATION=60 \
      -DCONSTANT_1=50 -DCONSTANT_1_DURATION=180 \
      -DRAMP_USERS_PER_SEC_2=50 -DRAMP_USERS_PER_SEC_2_TO=200 -DRAMP_USERS_PER_SEC_2_DURATION=120 \
      -DCONSTANT_2=200 -DCONSTANT_2_DURATION=180
    ;;

  spike)
    mvn -q gatling:test \
      -Dgatling.simulationClass=simulations.SpikeSimulation \
      -DBASE_URL="$BASE_URL" \
      -DSEQUENCE="CONSTANT_1,STRESS_PEAK_1,CONSTANT_2" \
      -DCONSTANT_1=10 -DCONSTANT_1_DURATION=60 \
      -DSTRESS_PEAK_1=500 -DSTRESS_PEAK_1_DURATION=30 \
      -DCONSTANT_2=10 -DCONSTANT_2_DURATION=60
    ;;

  stateful)
    mvn -q gatling:test \
      -Dgatling.simulationClass=simulations.StatefulChainSimulation \
      -DBASE_URL="$BASE_URL" \
      -DSEQUENCE="RAMP_USERS_1,CONSTANT_1" \
      -DRAMP_USERS_1=5 -DRAMP_USERS_1_DURATION=10 \
      -DCONSTANT_1=5 -DCONSTANT_1_DURATION=60
    ;;

  *)
    echo "Unknown profile: $PROFILE" >&2
    echo "Usage: $0 {smoke|load|stress|spike|stateful}" >&2
    exit 1
    ;;
esac

echo
echo "✓ Done. Open the HTML report in target/gatling/*/index.html"
