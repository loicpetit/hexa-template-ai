package hexa.template.ai.email.adapter.rest;

import hexa.template.ai.email.application.usecases.CreateEmailRecordCommand;
import hexa.template.ai.email.application.usecases.CreateEmailRecordResponse;
import hexa.template.ai.email.application.usecases.CreateEmailRecordUseCase;
import hexa.template.ai.email.adapter.rest.dto.EmailRequestDto;
import hexa.template.ai.email.adapter.rest.dto.EmailResponseDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@RestController
@RequestMapping("/api/emails")
public class EmailController {
    private final CreateEmailRecordUseCase createEmailRecordUseCase;

    public EmailController(CreateEmailRecordUseCase createEmailRecordUseCase) {
        this.createEmailRecordUseCase = createEmailRecordUseCase;
    }

    @PostMapping
    public ResponseEntity<EmailResponseDto> createEmail(@RequestBody EmailRequestDto request) {
        CreateEmailRecordResponse response = createEmailRecordUseCase.execute(new CreateEmailRecordCommand(request.value()));
        EmailResponseDto dto = new EmailResponseDto(response.id(), response.value());
        String lastModified = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC).format(response.updated());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.LAST_MODIFIED, lastModified)
                .body(dto);
    }
}
