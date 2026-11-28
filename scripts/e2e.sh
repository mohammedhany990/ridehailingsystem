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

req() {
  local attempt
  for attempt in $(seq 1 12); do
    if curl -fsS "$@"; then return 0; fi
    sleep 5
  done
  return 1
}

register_and_verify() {
  local name="$1" email="$2" role="$3"
  say "register $role $email"
  local attempt
  for attempt in $(seq 1 12); do
    if curl -fsS -X POST "$GW/api/auth/register" -H 'Content-Type: application/json' \
      -d "{\"name\":\"$name\",\"email\":\"$email\",\"phone\":\"01000000000\",\"password\":\"$PASS\",\"role\":\"$role\"}" > /dev/null; then
      break
    fi
    [ "$attempt" = "12" ] && fail "register $role"
    sleep 5
  done
  local otp=""
  for i in $(seq 1 30); do
    local msg body cte
    msg=$(curl -fsS "$MAILHOG/api/v2/messages?limit=50" \
      | jq -c --arg to "$email" '[.items[] | select(.To[0].Mailbox + "@" + .To[0].Domain == $to)][0] // empty' || true)
    if [ -n "$msg" ]; then
      cte=$(echo "$msg" | jq -r '.Content.Headers["Content-Transfer-Encoding"][0] // ""' | tr '[:upper:]' '[:lower:]')
      body=$(echo "$msg" | jq -r '.Content.Body // ""')
      if [ "$cte" = "base64" ]; then
        body=$(echo "$body" | tr -d '\r\n' | base64 -d 2>/dev/null || true)
      else
        body=$(echo "$body" | sed 's/=\r\n//g; s/=\n//g')
      fi
      otp=$(echo "$body" | grep -oE '<h1>[0-9]{6}</h1>' | grep -oE '[0-9]{6}' | head -1 || true)
      [ -z "$otp" ] && otp=$(echo "$body" | grep -oE '[0-9]{6}' | head -1 || true)
    fi
    [ -n "$otp" ] && break
    sleep 2
  done
  [ -n "$otp" ] || fail "no OTP mail for $email"
  if ! curl -fsS -X POST "$GW/api/auth/verify-email" -H 'Content-Type: application/json' \
    -d "{\"email\":\"$email\",\"otp\":\"$otp\"}" > /dev/null; then
    echo "DEBUG cte=$cte otp=$otp"
    echo "DEBUG body head: $(echo "$body" | head -c 300)"
    fail "verify-email $role"
  fi
}

login() {
  curl -fsS -X POST "$GW/api/auth/login" -H 'Content-Type: application/json' \
    -d "{\"email\":\"$1\",\"password\":\"$PASS\"}" | jq -r '.data.accessToken' || fail "login $1"
}

register_and_verify "E2E Rider" "$RIDER_EMAIL" RIDER
register_and_verify "E2E Driver" "$DRIVER_EMAIL" DRIVER

RIDER_TOKEN=$(login "$RIDER_EMAIL"); [ -n "$RIDER_TOKEN" ] || fail "rider login"
DRIVER_TOKEN=$(login "$DRIVER_EMAIL"); [ -n "$DRIVER_TOKEN" ] || fail "driver login"
say "rider profile: read, update, avatar"
PROFILE_NAME=$(curl -fsS "$GW/api/auth/profile/me" -H "Authorization: Bearer $RIDER_TOKEN" | jq -r '.data.name')
[ -n "$PROFILE_NAME" ] && [ "$PROFILE_NAME" != "null" ] || fail "profile read"

NEW_NAME="E2E Rider Updated"
UPDATED=$(curl -fsS -X PUT "$GW/api/auth/profile/me" -H "Authorization: Bearer $RIDER_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"name\":\"$NEW_NAME\",\"phone\":\"+201099040294\"}" | jq -r '.data.name')
[ "$UPDATED" = "$NEW_NAME" ] || fail "profile update"

printf 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==' | base64 -d > /tmp/e2e-avatar.png
AVATAR_URL=$(curl -fsS -X POST "$GW/api/auth/profile/me/avatar" -H "Authorization: Bearer $RIDER_TOKEN" \
  -F "file=@/tmp/e2e-avatar.png;type=image/png" | jq -r '.data.avatarUrl')
[ -n "$AVATAR_URL" ] && [ "$AVATAR_URL" != "null" ] || fail "avatar upload"

RIDER_ID=$(curl -fsS "$GW/api/auth/profile/me" -H "Authorization: Bearer $RIDER_TOKEN" | jq -r '.data.id')
AVATAR_CT=$(curl -fsS -o /dev/null -w '%{content_type}' "$GW/api/auth/profile/avatar/$RIDER_ID" -H "Authorization: Bearer $RIDER_TOKEN")
[ "$AVATAR_CT" = "image/png" ] || fail "avatar fetch content-type $AVATAR_CT"

BAD_CT=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$GW/api/auth/profile/me/avatar" -H "Authorization: Bearer $RIDER_TOKEN" \
  -F "file=@scripts/e2e.sh;type=image/png")
[ "$BAD_CT" = "400" ] || fail "invalid avatar rejected with $BAD_CT, expected 400"

DRIVER_ID=$(echo "$DRIVER_TOKEN" | cut -d. -f2 | tr '_-' '/+' | { read p; pad=$(( (4 - ${#p} % 4) % 4 )); echo "$p$(printf '=%.0s' $(seq 1 $pad))"; } | base64 -d 2>/dev/null | jq -r .userId)
[ -n "$DRIVER_ID" ] || fail "driver id from JWT"

say "driver profile + online"
req -X POST "$GW/api/drivers" -H "Authorization: Bearer $DRIVER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"licenseNumber":"E2E-LIC-1"}' > /dev/null || fail "create driver profile"
req -X PUT "$GW/api/drivers/me/online" -H "Authorization: Bearer $DRIVER_TOKEN" > /dev/null || fail "driver online"

say "driver location update"
req -X POST "$GW/api/location/update" -H "Authorization: Bearer $DRIVER_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"driverId\":\"$DRIVER_ID\",\"latitude\":30.0444,\"longitude\":31.2357}" > /dev/null || fail "location update"
sleep 2

say "promo code validates"
PROMO_VALID=$(req -X POST "$GW/api/pricing/promo/validate" -H "Authorization: Bearer $RIDER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"code":"RIDE20"}' | jq -r '.data.valid')
[ "$PROMO_VALID" = "true" ] || fail "promo RIDE20 not valid"

say "fare estimate with vehicle type + promo"
EST=$(req -X POST "$GW/api/pricing/estimate" -H "Authorization: Bearer $RIDER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"pickupLat":30.0444,"pickupLng":31.2357,"dropoffLat":30.0626,"dropoffLng":31.2497,"vehicleType":"COMFORT","promoCode":"RIDE20"}' \
  | jq -r '.data.estimatedMinFare')
[ -n "$EST" ] && [ "$EST" != "null" ] || fail "fare estimate"

say "tier estimates differ across vehicle types"
TIER_MINS=$(for VT in MOTO STANDARD COMFORT XL PREMIUM; do
  curl -fsS -X POST "$GW/api/pricing/estimate" -H "Authorization: Bearer $RIDER_TOKEN" -H 'Content-Type: application/json' \
    -d "{\"pickupLat\":30.0444,\"pickupLng\":31.2357,\"dropoffLat\":30.0626,\"dropoffLng\":31.2497,\"vehicleType\":\"$VT\"}" \
    | jq -r '.data.estimatedMinFare'
done)
DISTINCT=$(echo "$TIER_MINS" | sort -u | wc -l | tr -d ' ')
[ "$DISTINCT" -ge 4 ] || fail "tier estimates not distinct (got $DISTINCT distinct of 5): $(echo $TIER_MINS | tr '\n' ' ')"
MOTO_MIN=$(echo "$TIER_MINS" | sed -n 1p)
PREMIUM_MIN=$(echo "$TIER_MINS" | sed -n 5p)
awk "BEGIN{exit !($PREMIUM_MIN > $MOTO_MIN)}" || fail "PREMIUM min ($PREMIUM_MIN) should exceed MOTO min ($MOTO_MIN)"

say "rider requests ride (COMFORT tier)"
RIDE_ID=$(req -X POST "$GW/api/rides/request" -H "Authorization: Bearer $RIDER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"pickupLat":30.0444,"pickupLng":31.2357,"dropoffLat":30.0626,"dropoffLng":31.2497,"searchRadiusKm":10,"vehicleType":"COMFORT"}' \
  | jq -r '.data.id')
[ -n "$RIDE_ID" ] && [ "$RIDE_ID" != "null" ] || fail "ride request"

say "driver accepts"
req -X PUT "$GW/api/rides/$RIDE_ID/accept" -H "Authorization: Bearer $DRIVER_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"driverId\":\"$DRIVER_ID\"}" > /dev/null || fail "accept"

say "start + breadcrumbs + complete"
req -X PUT "$GW/api/rides/$RIDE_ID/start" -H "Authorization: Bearer $DRIVER_TOKEN" > /dev/null || fail "start"

req -X POST "$GW/api/rides/$RIDE_ID/breadcrumbs" -H "Authorization: Bearer $DRIVER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"lat":30.0450,"lng":31.2360}' > /dev/null || fail "breadcrumb 1"
sleep 1
req -X POST "$GW/api/rides/$RIDE_ID/breadcrumbs" -H "Authorization: Bearer $DRIVER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"lat":30.0500,"lng":31.2400}' > /dev/null || fail "breadcrumb 2"

TRAIL_COUNT=$(req "$GW/api/rides/$RIDE_ID/trail" -H "Authorization: Bearer $RIDER_TOKEN" | jq -r '.data.pointCount')
[ "$TRAIL_COUNT" = "2" ] || fail "trail has $TRAIL_COUNT points, expected 2"

req -X PUT "$GW/api/rides/$RIDE_ID/complete" -H "Authorization: Bearer $DRIVER_TOKEN" > /dev/null || fail "complete"

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
req -X POST "$GW/api/payments/$PAYMENT_ID/charge" -H "Authorization: Bearer $RIDER_TOKEN" > /dev/null || fail "charge"
req -X POST "$GW/api/payments/$PAYMENT_ID/confirm-simulated" -H "Authorization: Bearer $RIDER_TOKEN" > /dev/null || fail "confirm"

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

say "driver earnings endpoint"
curl -fsS "$GW/api/payments/earnings/me/daily" -H "Authorization: Bearer $DRIVER_TOKEN" \
  | jq -e '.success == true' > /dev/null || fail "driver earnings endpoint"

echo "E2E PASS: register -> verify -> login -> driver online -> promo -> estimate -> ride(COMFORT) -> trail($TRAIL_COUNT) -> payment -> receipt($RECEIPT) -> notifications (unread=$UNREAD) -> earnings"
