package com.ridehailing.paymentservice.controller;

import com.ridehailing.paymentservice.common.ApiResponse;
import com.ridehailing.paymentservice.dto.PaymentResponse;
import com.ridehailing.paymentservice.dto.ReceiptResponse;
import com.ridehailing.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final com.ridehailing.paymentservice.repository.DriverEarningsDailyRepository earningsDailyRepository;

    @GetMapping("/earnings/me/daily")
    public ResponseEntity<ApiResponse<java.util.List<com.ridehailing.paymentservice.entity.DriverEarningsDaily>>> myDailyEarnings(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID driverId = UUID.fromString(jwt.getClaim("userId"));
        return ResponseEntity.ok(ApiResponse.success("Daily earnings fetched",
                earningsDailyRepository.findByDriverIdOrderByEarningDateDesc(driverId)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> myPayments(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        return ResponseEntity.ok(ApiResponse.success(
                "Payments retrieved",
                paymentService.historyForRider(userId, PageRequest.of(page, size))));
    }

    @GetMapping("/driver/me")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> driverPayments(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        return ResponseEntity.ok(ApiResponse.success(
                "Driver payments retrieved",
                paymentService.historyForDriver(userId, PageRequest.of(page, size))));
    }

    @GetMapping("/ride/{rideId}/receipt")
    public ResponseEntity<ApiResponse<ReceiptResponse>> rideReceipt(
            @PathVariable UUID rideId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        return ResponseEntity.ok(ApiResponse.success(
                "Receipt retrieved",
                paymentService.receiptForRide(rideId, userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved", paymentService.getOwned(id, userId)));
    }

    @PostMapping("/{id}/charge")
    public ResponseEntity<ApiResponse<PaymentResponse>> charge(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        return ResponseEntity.ok(ApiResponse.success(
                "Charge initiated",
                PaymentResponse.from(paymentService.initiateCharge(id, userId))));
    }

    @PostMapping("/{id}/confirm-simulated")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmSimulated(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        return ResponseEntity.ok(ApiResponse.success(
                "Payment confirmed (simulated)",
                PaymentResponse.from(paymentService.confirmSimulated(id, userId))));
    }
}
