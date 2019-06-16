package io.localmotion.email.template;

import io.localmotion.email.template.api.AbstractEmailTemplate;
import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.util.Map;
import java.util.function.Function;

@Singleton
@Slf4j
public class PlaygroundInviteTemplate extends AbstractEmailTemplate {


    @Value("${email.templates.playgroundinvite.name}")
    private String name;

    @Value("${email.templates.playgroundinvite.subjectline}")
    private String subjectLine;

    @Value("${email.templates.playgroundinvite.textpart}")
    private String textPart;

    @Value("${email.templates.playgroundinvite.htmlpart}")
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

    public String getHtmlPart(Map<String, String> data) {
        String template =  readEmailContentFromTemplate(this.getClass().getClassLoader().getResourceAsStream("templates/" + this.name + ".html"));
        if(data != null) {
            template=data.entrySet().stream()
                    .map( placeholder-> (Function<String,String>) s->s.replaceAll("# "+ placeholder.getKey() + " #", placeholder.getValue()))
                    .reduce(Function.identity(), Function::andThen)
                    .apply(template);
        }
        log.info("Aftre replaceALl -----> " +template);
        return template;

    }

    @Override
    public String toString() {
        return "PlaygroundInviteTemplate{" +
                "name='" + name + '\'' +
                ", subjectLine='" + subjectLine + '\'' +
                ", textPart='" + textPart + '\'' +
                ", htmlPart='" + getHtmlPart(null) + '\'' +
                '}';
    }
}
