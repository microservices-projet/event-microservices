package com.example.reservationservice.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEntry {
    private String action;
    private Long performedBy;
    private LocalDateTime timestamp;
    private String details;
}
