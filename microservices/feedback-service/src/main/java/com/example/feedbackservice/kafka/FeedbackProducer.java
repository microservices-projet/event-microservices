package com.example.feedbackservice.kafka;

import com.example.eventcontract.EventEnvelope;
import com.example.eventcontract.EventEnvelopeContract;
import com.example.feedbackservice.entity.Feedback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackProducer {

    private static final String TOPIC = "feedback-events";
    private static final String AGGREGATE_TYPE = "Feedback";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendFeedbackCreated(Feedback feedback) {
        sendMessage(feedback, "CREATED");
    }

    public void sendFeedbackModerated(Feedback feedback) {
        sendMessage(feedback, "MODERATED");
    }

    public void sendFeedbackDeleted(Long feedbackId) {
        EventEnvelope envelope = EventEnvelope.v1(
                "DELETED",
                AGGREGATE_TYPE,
                String.valueOf(feedbackId),
                feedbackId,
                null,
                null,
                null
        );
        Message<EventEnvelope> message = MessageBuilder
                .withPayload(envelope)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .setHeader(EventEnvelopeContract.EVENT_TYPE_HEADER, "DELETED")
                .build();
        kafkaTemplate.send(message);
        log.info("Kafka feedback DELETED id={}", feedbackId);
    }

    private void sendMessage(Feedback feedback, String eventType) {
        EventEnvelope envelope = EventEnvelope.v1(
                eventType,
                AGGREGATE_TYPE,
                String.valueOf(feedback.getId()),
                feedback,
                null,
                null,
                null
        );
        Message<EventEnvelope> message = MessageBuilder
                .withPayload(envelope)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .setHeader(EventEnvelopeContract.EVENT_TYPE_HEADER, eventType)
                .build();
        kafkaTemplate.send(message);
        log.info("Kafka feedback {} id={}", eventType, feedback.getId());
    }
}
