package io.localmotion.email.template;

import io.localmotion.email.template.api.AbstractEmailTemplate;
import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Slf4j
@Singleton
public class ChatNotificationEmailTemplate extends AbstractEmailTemplate {

    @Value("${email.templates.notify.name}")
    private String name;

    @Value("${email.templates.notify.subjectline}")
    private String subjectLine;

    @Value("${email.templates.notify.textpart}")
    private String textPart;

    @Value("${email.templates.notify.htmlpart}")
    private String htmlPart;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSubjectLine() {
        return this.subjectLine;
    }

    @Override
    public String getTextPart() {
        return this.textPart;
    }
}
