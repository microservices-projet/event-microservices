package com.example.paymentservice.controller;

import com.example.paymentservice.dto.PaymentCallbackRequest;
import com.example.paymentservice.dto.PaymentSessionResponse;
import com.example.paymentservice.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<PaymentSessionResponse> getByReservation(@PathVariable String reservationId) {
        return paymentService.getByReservationId(reservationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{paymentId}/callback")
    public ResponseEntity<PaymentSessionResponse> callback(
            @PathVariable String paymentId,
            @RequestBody PaymentCallbackRequest request,
            @RequestHeader(value = "x-correlation-id", required = false) String correlationId
    ) {
        if (request.isSuccess()) {
            return ResponseEntity.ok(paymentService.markCompleted(paymentId, correlationId));
        }
        return ResponseEntity.ok(paymentService.markFailed(paymentId, request.getFailureReason(), correlationId));
    }

    @PostMapping("/{paymentId}/retry")
    public ResponseEntity<PaymentSessionResponse> retry(
            @PathVariable String paymentId,
            @RequestHeader(value = "x-correlation-id", required = false) String correlationId
    ) {
        return ResponseEntity.ok(paymentService.retryPayment(paymentId, correlationId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
