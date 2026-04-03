package com.example.reservationservice.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "processed_payment_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedPaymentEvent {
    @Id
    private String eventId;
    private LocalDateTime processedAt;
}
