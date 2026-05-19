package hexa.template.ai.email.adapter.rest;

import hexa.template.ai.email.adapter.persistence.audit.Slf4jAuditLogger;
import hexa.template.ai.email.adapter.persistence.repository.InMemoryEmailRepository;
import hexa.template.ai.email.application.dto.User;
import hexa.template.ai.email.application.ports.IAuditLogger;
import hexa.template.ai.email.application.ports.IAuthProvider;
import hexa.template.ai.email.application.ports.IEmailRepository;
import hexa.template.ai.email.application.usecases.CreateEmailRecordUseCase;
import hexa.template.ai.email.domain.entities.Email;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectFactory;

@Configuration
public class EmailRestConfig {
    @Bean
    public IEmailRepository emailRepository() {
        return new InMemoryEmailRepository();
    }

    @Bean
    public IAuditLogger auditLogger() {
        return new Slf4jAuditLogger();
    }

    @Bean
    public Map<String, User> apiKeyUserMap() {
        // Example: hardcoded API key for E2E and dev
        Map<String, User> map = new HashMap<>();
        map.put("test-api-key", new User("alice"));
        return Collections.unmodifiableMap(map);
    }

    @Bean
    public IAuthProvider authProvider(Map<String, User> apiKeyUserMap, ObjectFactory<HttpServletRequest> requestFactory) {
        // Use ObjectFactory to get the current request for each call (request scope)
        return new hexa.template.ai.email.adapter.rest.authentication.ApiKeyAuthProvider(apiKeyUserMap, requestFactory.getObject());
    }

    @Bean
    public CreateEmailRecordUseCase createEmailRecordUseCase(IAuthProvider authProvider, IEmailRepository emailRepository, IAuditLogger auditLogger) {
        return new CreateEmailRecordUseCase(authProvider, emailRepository, auditLogger);
    }
}
