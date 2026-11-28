# M7 — Fare Negotiation

inDrive-style fare negotiation on top of the Uber-style tiered estimates: the rider can propose their own price for a requested ride, and the driver can accept, decline or counter-offer, until both sides agree.

- **Offer flow:** `POST /api/rides/{rideId}/offers` creates an offer (rider or driver side); `GET` lists the negotiation thread for the ride; `PUT /{offerId}/accept` and `PUT /{offerId}/decline` close an offer. Only the two parties on the ride can see or act on its offers.
- **State machine:** offers move PENDING -> ACCEPTED / DECLINED / SUPERSEDED; a new counter-offer automatically supersedes the previous pending one, so there is always exactly one offer on the table.
- **Acceptance sets the fare:** accepting an offer locks the negotiated amount onto the ride, and payment + receipt then use the agreed price instead of the metered estimate.
- **Proposed fare up front:** `PUT /api/rides/{rideId}/proposed-fare` lets the rider attach their price at request time, so the driver sees the offer before accepting the ride at all.
- **Frontend:** negotiation card in the trip view — offer input, accept/decline/counter actions, and the agreed fare reflected in the estimate sheet, payment and receipt.

**Tests:** `FareNegotiationServiceTest` covers offer creation, counter superseding, accept locking the fare, decline, and ownership checks.
