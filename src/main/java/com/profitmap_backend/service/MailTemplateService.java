package com.profitmap_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailTemplateService {

    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("hr");

    private final SpringTemplateEngine templateEngine;

    public String render(String templateName, Map<String, Object> variables) {
        Context context = new Context(DEFAULT_LOCALE);
        if (variables != null && !variables.isEmpty()) {
            context.setVariables(variables);
        }
        return templateEngine.process(templateName, context);
    }
}

