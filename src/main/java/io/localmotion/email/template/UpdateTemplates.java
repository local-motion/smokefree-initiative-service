package io.localmotion.email.template;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.discovery.event.ServiceStartedEvent;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Requires(notEnv = {Environment.TEST})
@Slf4j
public class UpdateTemplates implements ApplicationEventListener<ServiceStartedEvent> {

    @Inject
    AmazonSimpleEmailService amazonSimpleEmailServiceClient;

    @Inject
    VerificationEmailTemplate verificationEmailTemplate;

    @Inject
    PlaygroundInviteTemplate playgroundInviteTemplate;

    @Inject
    PlaygroundActivityEmailTemplate playgroundActivityEmailTemplate;

    @Inject
    ChatNotificationEmailTemplate chatNotifyEmailTemplate;


    @Override
    public void onApplicationEvent(ServiceStartedEvent event) {
        //updateEmailVerificationTemplate();
        updatePlaygroundInviteTemplate();
        updatePlaygroundActivityTemplate();
        updateChatNotifyTemplate();
    }

    /*private void updateEmailVerificationTemplate() {
        log.info("update Custom verirification Email Template");
        log.info(verificationEmailTemplate.toString());
        log.info("----------> Html Part" + verificationEmailTemplate.getHtmlPart());
        if(assertIsEmailVerificationTemplateExist(verificationEmailTemplate.getName())) {


            log.info("Update Custom verification EmailTemplate");
            UpdateCustomVerificationEmailTemplateRequest request = new UpdateCustomVerificationEmailTemplateRequest()
                    .withTemplateSubject(verificationEmailTemplate.getSubjectLine())
                    .withTemplateName(verificationEmailTemplate.getName())
                    .withFromEmailAddress("verification@javaasayudha.tk")
                    .withSuccessRedirectionURL("https://techoverflow-ta.aws.abnamro.org/")
                    .withFailureRedirectionURL("https://www.google.com")
                    .withTemplateContent(verificationEmailTemplate.getHtmlPart());

            amazonSimpleEmailServiceClient.updateCustomVerificationEmailTemplate(request);
            log.info("Complete Update  custom verification EmailTemplate");

        } else {

            log.info("Create custom verification EmailTemplate");
            CreateCustomVerificationEmailTemplateRequest request = new CreateCustomVerificationEmailTemplateRequest()

                    .withTemplateSubject(verificationEmailTemplate.getSubjectLine())
                    .withTemplateName(verificationEmailTemplate.getName())
                    .withFromEmailAddress("verification@javaasayudha.tk")
                    .withSuccessRedirectionURL("https://techoverflow-ta.aws.abnamro.org/")
                    .withFailureRedirectionURL("https://www.google.com")
                    .withTemplateContent(verificationEmailTemplate.getHtmlPart());
            amazonSimpleEmailServiceClient.createCustomVerificationEmailTemplate(request);
            log.info("Complete custom verification Create EmailTemplate");

        }

        log.info("Complete Update custom verification EmailTemplate");
    }*/

    private void updatePlaygroundInviteTemplate() {
        log.info("Sync template {}", playgroundInviteTemplate.getName());
        if(assertIsTemplateExist(playgroundInviteTemplate.getName())) {
            //update template
            Template template = new Template().withTemplateName(playgroundInviteTemplate.getName())
                    .withSubjectPart(playgroundInviteTemplate.getSubjectLine())
                    .withTextPart(playgroundInviteTemplate.getTextPart())
                    .withHtmlPart(playgroundInviteTemplate.getHtmlPart());
            UpdateTemplateRequest request = new UpdateTemplateRequest().withTemplate(template);
            amazonSimpleEmailServiceClient.updateTemplate(request);
        } else {
            //create Template
            Template template = new Template().withTemplateName(playgroundInviteTemplate.getName())
                    .withSubjectPart(playgroundInviteTemplate.getSubjectLine())
                    .withTextPart(playgroundInviteTemplate.getTextPart())
                    .withHtmlPart(playgroundInviteTemplate.getHtmlPart());
            CreateTemplateRequest request = new CreateTemplateRequest().withTemplate(template);
            amazonSimpleEmailServiceClient.createTemplate(request);
        }
        log.info("Sync successfully template {}", playgroundInviteTemplate.getName() );
    }

    private void updatePlaygroundActivityTemplate() {
        if(assertIsTemplateExist(playgroundActivityEmailTemplate.getName())) {
            //update Template
            Template template = new Template().withTemplateName(playgroundActivityEmailTemplate.getName())
                    .withSubjectPart(playgroundActivityEmailTemplate.getSubjectLine())
                    .withTextPart(playgroundActivityEmailTemplate.getTextPart())
                    .withHtmlPart(playgroundActivityEmailTemplate.getHtmlPart());
            UpdateTemplateRequest request = new UpdateTemplateRequest().withTemplate(template);
            amazonSimpleEmailServiceClient.updateTemplate(request);
        } else {
            //cretae Template
            Template template = new Template().withTemplateName(playgroundActivityEmailTemplate.getName())
                    .withSubjectPart(playgroundActivityEmailTemplate.getSubjectLine())
                    .withTextPart(playgroundActivityEmailTemplate.getTextPart())
                    .withHtmlPart(playgroundActivityEmailTemplate.getHtmlPart());
            CreateTemplateRequest request = new CreateTemplateRequest().withTemplate(template);
            amazonSimpleEmailServiceClient.createTemplate(request);
        }
    }

    private void updateChatNotifyTemplate() {
        if(assertIsTemplateExist(chatNotifyEmailTemplate.getName())) {
            // update Template
            Template template = new Template().withTemplateName(chatNotifyEmailTemplate.getName())
                    .withSubjectPart(chatNotifyEmailTemplate.getSubjectLine())
                    .withTextPart(chatNotifyEmailTemplate.getTextPart())
                    .withHtmlPart(chatNotifyEmailTemplate.getHtmlPart());
            UpdateTemplateRequest request = new UpdateTemplateRequest().withTemplate(template);
            amazonSimpleEmailServiceClient.updateTemplate(request);
        } else {
            // create Template
            Template template = new Template().withTemplateName(chatNotifyEmailTemplate.getName())
                    .withSubjectPart(chatNotifyEmailTemplate.getSubjectLine())
                    .withTextPart(chatNotifyEmailTemplate.getTextPart())
                    .withHtmlPart(chatNotifyEmailTemplate.getHtmlPart());
            CreateTemplateRequest request = new CreateTemplateRequest().withTemplate(template);
            amazonSimpleEmailServiceClient.createTemplate(request);
        }
    }

    @Override
    public boolean supports(ServiceStartedEvent event) {
        return true;
    }

    public  boolean assertIsEmailVerificationTemplateExist(String templateName) {
        GetCustomVerificationEmailTemplateResult response;
        try {
            response = amazonSimpleEmailServiceClient.getCustomVerificationEmailTemplate(
                    new GetCustomVerificationEmailTemplateRequest().withTemplateName(templateName));
            log.info("Custom Email verification Template {} ", response.getTemplateName());
        }catch(CustomVerificationEmailTemplateDoesNotExistException ex) {
            return false;
        }
        return response.getSdkHttpMetadata().getHttpStatusCode() == 200;
    }

    public boolean assertIsTemplateExist(String templateName) {
        GetTemplateRequest request = new GetTemplateRequest().withTemplateName(templateName);
        GetTemplateResult response;
        try {
            response = amazonSimpleEmailServiceClient.getTemplate(request);
        }catch(TemplateDoesNotExistException ex) {
            log.info("Template {} does not exist", templateName);
            return false;
        }
        return response.getSdkHttpMetadata().getHttpStatusCode() == 200 ;

    }
}
