package hexa.template.ai.email.application.usecases;

import java.time.Instant;

public record CreateEmailRecordResponse(String id, String value, Instant created) {
}