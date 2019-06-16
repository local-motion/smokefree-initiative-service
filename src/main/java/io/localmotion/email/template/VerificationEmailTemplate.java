package io.localmotion.email.template;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.UpdateCustomVerificationEmailTemplateRequest;
import io.localmotion.email.template.api.AbstractEmailTemplate;
import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Singleton
@Slf4j
public class VerificationEmailTemplate extends AbstractEmailTemplate {

    @Inject
    AmazonSimpleEmailService amazonSimpleEmailServiceClient;

    @Value("${email.templates.verification.name}")
    private String name;

    @Value("${email.templates.verification.subjectline}")
    private String subjectLine;

    @Value("${email.templates.verification.textpart}")
    private String textPart;

    @Value("${email.templates.verification.htmlpart}")
    private String htmlPart;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSubjectLine() {
        return subjectLine;
    }

    @Override
    public String getTextPart() {
        return textPart;
    }


    public String getHtmlPart(Map<String, String> data) {
        log.info(this.getClass().getClassLoader().getResourceAsStream("templates/" + this.name + ".html").toString());
        String template =  readEmailContentFromTemplate(this.getClass().getClassLoader().getResourceAsStream("templates/" + this.name + ".html"));
        if(data != null) {
            template=data.entrySet().stream()
                    .map( placeholder-> (Function<String,String>) s->s.replaceAll("# "+ placeholder.getKey() + " #", placeholder.getValue()))
                    .reduce(Function.identity(), Function::andThen)
                    .apply(template);
        }
        log.info(template);
        return template;
    }

    public void update(String username) {
        Map<String, String> data = new HashMap<>();
        data.put("username", username);
        UpdateCustomVerificationEmailTemplateRequest request = new UpdateCustomVerificationEmailTemplateRequest()
                .withTemplateSubject(this.getSubjectLine())
                .withTemplateName(this.getName())
                .withFromEmailAddress("verification@javaasayudha.tk")
                .withSuccessRedirectionURL("https://techoverflow-ta.aws.abnamro.org/")
                .withFailureRedirectionURL("https://www.google.com")
                .withTemplateContent(this.getHtmlPart(data));

        amazonSimpleEmailServiceClient.updateCustomVerificationEmailTemplate(request);

    }
}
