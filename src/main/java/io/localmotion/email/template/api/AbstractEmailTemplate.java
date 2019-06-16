package io.localmotion.email.template.api;

public abstract class AbstractEmailTemplate implements EmailTemplate{
    @Override
    public String getHtmlPart() {
        return readEmailContentFromTemplate(this.getClass().getClassLoader().getResourceAsStream("templates/" + this.getName() + ".html"));
    }

}
