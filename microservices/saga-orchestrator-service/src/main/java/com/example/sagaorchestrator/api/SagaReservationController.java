package com.example.sagaorchestrator.api;

import com.example.sagaorchestrator.dto.SagaReserveRequest;
import com.example.sagaorchestrator.dto.SagaReserveResponse;
import com.example.sagaorchestrator.service.SagaReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saga")
@RequiredArgsConstructor
public class SagaReservationController {

    private final SagaReservationService sagaReservationService;

    @PostMapping("/reserve")
    public ResponseEntity<SagaReserveResponse> reserve(@Valid @RequestBody SagaReserveRequest request) {
        SagaReserveResponse response = sagaReservationService.reserve(request);
        if ("COMPLETED".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
