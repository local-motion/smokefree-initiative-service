package io.localmotion.email.template;

import io.localmotion.email.template.api.AbstractEmailTemplate;
import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;


@Singleton
@Slf4j
public class PlaygroundActivityEmailTemplate extends AbstractEmailTemplate {

    @Value("${email.templates.playgroundactivity.name}")
    private String name;

    @Value("${email.templates.playgroundactivity.subjectline}")
    private String subjectLine;

    @Value("${email.templates.playgroundactivity.textpart}")
    private String textPart;

    @Value("${email.templates.playgroundactivity.htmlpart}")
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
