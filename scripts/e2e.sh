#!/usr/bin/env bash
set -euo pipefail

GW="${GW:-http://localhost:8888}"
MAILHOG="${MAILHOG:-http://localhost:8025}"
TS=$(date +%s)
RIDER_EMAIL="rider_${TS}@e2e.test"
DRIVER_EMAIL="driver_${TS}@e2e.test"
PASS="Password123!"

say() { echo "== $*"; }
fail() { echo "E2E FAIL: $*" >&2; exit 1; }

register_and_verify() {
  local name="$1" email="$2" role="$3"
  say "register $role $email"
  curl -fsS -X POST "$GW/api/auth/register" -H 'Content-Type: application/json' \
    -d "{\"name\":\"$name\",\"email\":\"$email\",\"phone\":\"01000000000\",\"password\":\"$PASS\",\"role\":\"$role\"}" > /dev/null \
    || fail "register $role"
  local otp=""
  for i in $(seq 1 30); do
    otp=$(curl -fsS "$MAILHOG/api/v2/messages?limit=50" \
      | jq -r --arg to "$email" '[.items[] | select(.To[0].Mailbox + "@" + .To[0].Domain == $to)][0].Content.Body // empty' \
      | grep -oE '[0-9]{6}' | head -1)
    [ -n "$otp" ] && break
    sleep 2
  done
  [ -n "$otp" ] || fail "no OTP mail for $email"
  curl -fsS -X POST "$GW/api/auth/verify-email" -H 'Content-Type: application/json' \
    -d "{\"email\":\"$email\",\"otp\":\"$otp\"}" > /dev/null || fail "verify-email $role"
}

login() {
  curl -fsS -X POST "$GW/api/auth/login" -H 'Content-Type: application/json' \
    -d "{\"email\":\"$1\",\"password\":\"$PASS\"}" | jq -r '.data.accessToken'
}

register_and_verify "E2E Rider" "$RIDER_EMAIL" RIDER
register_and_verify "E2E Driver" "$DRIVER_EMAIL" DRIVER

RIDER_TOKEN=$(login "$RIDER_EMAIL"); [ -n "$RIDER_TOKEN" ] || fail "rider login"
DRIVER_TOKEN=$(login "$DRIVER_EMAIL"); [ -n "$DRIVER_TOKEN" ] || fail "driver login"
DRIVER_ID=$(echo "$DRIVER_TOKEN" | cut -d. -f2 | tr '_-' '/+' | { read p; pad=$(( (4 - ${#p} % 4) % 4 )); echo "$p$(printf '=%.0s' $(seq 1 $pad))"; } | base64 -d 2>/dev/null | jq -r .userId)
[ -n "$DRIVER_ID" ] || fail "driver id from JWT"

say "driver profile + online"
curl -fsS -X POST "$GW/api/drivers" -H "Authorization: Bearer $DRIVER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"licenseNumber":"E2E-LIC-1"}' > /dev/null || fail "create driver profile"
curl -fsS -X PUT "$GW/api/drivers/me/online" -H "Authorization: Bearer $DRIVER_TOKEN" > /dev/null || fail "driver online"

say "driver location update"
curl -fsS -X POST "$GW/api/location/update" -H "Authorization: Bearer $DRIVER_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"driverId\":\"$DRIVER_ID\",\"latitude\":30.0444,\"longitude\":31.2357}" > /dev/null || fail "location update"
sleep 2

say "promo code validates"
PROMO_VALID=$(curl -fsS -X POST "$GW/api/pricing/promo/validate" -H "Authorization: Bearer $RIDER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"code":"RIDE20"}' | jq -r '.data.valid')
[ "$PROMO_VALID" = "true" ] || fail "promo RIDE20 not valid"

say "fare estimate with vehicle type + promo"
EST=$(curl -fsS -X POST "$GW/api/pricing/estimate" -H "Authorization: Bearer $RIDER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"pickupLat":30.0444,"pickupLng":31.2357,"dropoffLat":30.0626,"dropoffLng":31.2497,"vehicleType":"COMFORT","promoCode":"RIDE20"}' \
  | jq -r '.data.estimatedMinFare')
[ -n "$EST" ] && [ "$EST" != "null" ] || fail "fare estimate"

say "rider requests ride (COMFORT tier)"
RIDE_ID=$(curl -fsS -X POST "$GW/api/rides/request" -H "Authorization: Bearer $RIDER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"pickupLat":30.0444,"pickupLng":31.2357,"dropoffLat":30.0626,"dropoffLng":31.2497,"searchRadiusKm":10,"vehicleType":"COMFORT"}' \
  | jq -r '.data.id')
[ -n "$RIDE_ID" ] && [ "$RIDE_ID" != "null" ] || fail "ride request"

say "driver accepts"
curl -fsS -X PUT "$GW/api/rides/$RIDE_ID/accept" -H "Authorization: Bearer $DRIVER_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"driverId\":\"$DRIVER_ID\"}" > /dev/null || fail "accept"

say "start + breadcrumbs + complete"
curl -fsS -X PUT "$GW/api/rides/$RIDE_ID/start" -H "Authorization: Bearer $DRIVER_TOKEN" > /dev/null || fail "start"

curl -fsS -X POST "$GW/api/rides/$RIDE_ID/breadcrumbs" -H "Authorization: Bearer $DRIVER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"lat":30.0450,"lng":31.2360}' > /dev/null || fail "breadcrumb 1"
sleep 1
curl -fsS -X POST "$GW/api/rides/$RIDE_ID/breadcrumbs" -H "Authorization: Bearer $DRIVER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"lat":30.0500,"lng":31.2400}' > /dev/null || fail "breadcrumb 2"

TRAIL_COUNT=$(curl -fsS "$GW/api/rides/$RIDE_ID/trail" -H "Authorization: Bearer $RIDER_TOKEN" | jq -r '.data.pointCount')
[ "$TRAIL_COUNT" = "2" ] || fail "trail has $TRAIL_COUNT points, expected 2"

curl -fsS -X PUT "$GW/api/rides/$RIDE_ID/complete" -H "Authorization: Bearer $DRIVER_TOKEN" > /dev/null || fail "complete"

say "payment created from Kafka event"
PAYMENT_ID=""
for i in $(seq 1 30); do
  PAYMENT_ID=$(curl -fsS "$GW/api/payments/me" -H "Authorization: Bearer $RIDER_TOKEN" \
    | jq -r --arg r "$RIDE_ID" '[.data.content[] | select(.rideId == $r)][0].id // empty')
  [ -n "$PAYMENT_ID" ] && break
  sleep 2
done
[ -n "$PAYMENT_ID" ] || fail "no payment for ride"

say "charge + confirm (simulated)"
curl -fsS -X POST "$GW/api/payments/$PAYMENT_ID/charge" -H "Authorization: Bearer $RIDER_TOKEN" > /dev/null || fail "charge"
curl -fsS -X POST "$GW/api/payments/$PAYMENT_ID/confirm-simulated" -H "Authorization: Bearer $RIDER_TOKEN" > /dev/null || fail "confirm"

say "trip receipt"
RECEIPT=$(curl -fsS "$GW/api/payments/ride/$RIDE_ID/receipt" -H "Authorization: Bearer $RIDER_TOKEN" | jq -r '.data.receiptNumber')
echo "$RECEIPT" | grep -q "^RCT-" || fail "receipt missing, got $RECEIPT"

say "notifications delivered"
UNREAD=""
for i in $(seq 1 30); do
  UNREAD=$(curl -fsS "$GW/api/notifications/me/unread-count" -H "Authorization: Bearer $RIDER_TOKEN" | jq -r '.data')
  [ "$UNREAD" != "0" ] && [ -n "$UNREAD" ] && break
  sleep 2
done
[ "$UNREAD" != "0" ] || fail "rider has no notifications"

echo "E2E PASS: register -> verify -> login -> driver online -> promo -> estimate -> ride(COMFORT) -> trail($TRAIL_COUNT) -> payment -> receipt($RECEIPT) -> notifications (unread=$UNREAD)"
