package io.localmotion.email.service;

import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.localmotion.email.Constants;
import io.localmotion.email.service.api.AWSSimpleEmailService;
import io.localmotion.email.template.VerificationEmailTemplate;
import io.localmotion.email.template.api.EmailTemplate;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@Slf4j
public class AWSSimpleEmailServiceImplementation implements AWSSimpleEmailService {

	@Inject
    AmazonSimpleEmailService amazonSimpleEmailServiceClient;

	@Inject
	VerificationEmailTemplate verificationEmailTemplate;

	@Inject
    ObjectMapper mapper;

	@Override
	public AmazonWebServiceResult<ResponseMetadata> sendEmail(String toAddress, EmailTemplate template, Map<String, String> templateData) {
		SendEmailResult result = null;
		try {

			SendEmailRequest request = new SendEmailRequest()
					.withDestination(
							new Destination().withToAddresses(toAddress))
					.withMessage(new Message()
							.withBody(new Body()
									.withHtml(new Content()
											.withCharset("UTF-8").withData(template.getHtmlPart()))
									.withText(new Content()
											.withCharset("UTF-8").withData(template.getTextPart())))
							.withSubject(new Content()
									.withCharset("UTF-8").withData(template.getSubjectLine())))
					.withSource(Constants.NOTIFICATION_ADDRESS);

			// Comment or remove the next line if you are not using a
			// configuration set
			// .withConfigurationSetName(CONFIGSET);

			result = amazonSimpleEmailServiceClient.sendEmail(request);
			log.info(result.toString());
		} catch(Exception ex) {
			System.out.println("The email was not sent. Error message: "
					+ ex.getMessage());
		}

		return result;
	}



	@Override
	public AmazonWebServiceResult<ResponseMetadata> sendTemplateEmail(String templateName ,String toAddress, Map<String,String> data) {

		String jsonString = null;
		try {
			jsonString  = mapper.writeValueAsString(data);
			log.info(jsonString);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		SendTemplatedEmailRequest request = new SendTemplatedEmailRequest()
				.withDestination(new Destination().withToAddresses(toAddress))
				.withReplyToAddresses("no-reply@javaasayudha.tk")
				.withTemplate(templateName)
				.withSource(Constants.NOTIFICATION_ADDRESS)
				.withTemplateData(jsonString);

		return amazonSimpleEmailServiceClient.sendTemplatedEmail(request);
	}

	@Override
	public AmazonWebServiceResult<ResponseMetadata> verifyEmailAddress(String emailAddress) {
		VerifyEmailAddressRequest request = new VerifyEmailAddressRequest().withEmailAddress(emailAddress);
		VerifyEmailAddressResult response = amazonSimpleEmailServiceClient.verifyEmailAddress(request);
		return response;

	}

	@Override
	public AmazonWebServiceResult<ResponseMetadata> getAllEmailAddresses() {
		ListIdentitiesResult identities = amazonSimpleEmailServiceClient.listIdentities();
		return identities;
	}

	@Override
	public AmazonWebServiceResult<ResponseMetadata> deleteEmailIdentity(String emailAddress) {
		DeleteIdentityRequest request = new DeleteIdentityRequest().withIdentity(emailAddress);
		return amazonSimpleEmailServiceClient.deleteIdentity(request);
	}

	@Override
	public AmazonWebServiceResult<ResponseMetadata> createCustomVerificationTemplate() {

		/*if(assertIsEmailVerificationTemplateExist("verification-template") == 400) {
			CreateCustomVerificationEmailTemplateRequest request = new CreateCustomVerificationEmailTemplateRequest()

					.withTemplateSubject(verificationEmailTemplate.getSubjectLine())
					.withTemplateName(verificationEmailTemplate.getName())
					.withFromEmailAddress("verification@javaasayudha.tk")
					.withSuccessRedirectionURL("https://techoverflow-ta.aws.abnamro.org/")
					.withFailureRedirectionURL("https://www.google.com")
					.withTemplateContent(verificationEmailTemplate.getHtmlPart());
			return amazonSimpleEmailServiceClient.createCustomVerificationEmailTemplate(request);
		} else {
			UpdateCustomVerificationEmailTemplateRequest request = new UpdateCustomVerificationEmailTemplateRequest()
					.withTemplateSubject(verificationEmailTemplate.getSubjectLine())
					.withTemplateName(verificationEmailTemplate.getName())
					.withFromEmailAddress("verification@javaasayudha.tk")
					.withSuccessRedirectionURL("https://techoverflow-ta.aws.abnamro.org/")
					.withFailureRedirectionURL("https://www.google.com")
					.withTemplateContent(verificationEmailTemplate.getHtmlPart());

			return amazonSimpleEmailServiceClient.updateCustomVerificationEmailTemplate(request);
		}*/

		return null;


	}

	@Override
	public AmazonWebServiceResult<ResponseMetadata> confirmEmailAddress(String confirmAddress) {
		verificationEmailTemplate.update("Anand");
		SendCustomVerificationEmailRequest request = new SendCustomVerificationEmailRequest().withEmailAddress(confirmAddress)
				.withTemplateName(verificationEmailTemplate.getName());
		return amazonSimpleEmailServiceClient.sendCustomVerificationEmail(request);
	}

	@Override
	public AmazonWebServiceResult<ResponseMetadata> createEmailTemplate(String templateName) {
		Template template = new Template().withTemplateName(templateName).withSubjectPart("Notification: Missed Conversations")
				.withHtmlPart("<html>\n" +
						"                      <head></head>\n" +
						"                      <body style=\"font-family:sans-serif;\">\n" +
						"                        <h1 style=\"text-align:center\">it seems you are not logged in</h1>\n" +
						"                        <p>Below are the Missied conversations from the Playground {{ name }}</p>\n" +
						"                        <p> {{ messages }} </p>\n "+
						"                      </body>\n" +
						"                      </html>")
				.withTextPart("Text Part");
		CreateTemplateRequest request = new CreateTemplateRequest().withTemplate(template);
		return amazonSimpleEmailServiceClient.createTemplate(request);
	}
	@Override
	public AmazonWebServiceResult<ResponseMetadata> createEmailTemplate(String templateName, String body) {
		Template template = new Template().withTemplateName(templateName).withSubjectPart(" LocalMotion Campaign")
				.withHtmlPart(body)
				.withTextPart("Text Part");
		CreateTemplateRequest request = new CreateTemplateRequest().withTemplate(template);
		return amazonSimpleEmailServiceClient.createTemplate(request);
	}

	@Override
	public AmazonWebServiceResult<ResponseMetadata> updateEmailTemplate(String templateName, String htmlPart) {
		Template template = new Template().withTemplateName(templateName).withSubjectPart("Notification: Missed Conversations")
				.withHtmlPart(htmlPart)
				.withTextPart("Text Part");
		UpdateTemplateRequest request = new UpdateTemplateRequest().withTemplate(template);
		VerifyDomainIdentityRequest request1 = new VerifyDomainIdentityRequest().withDomain("amazonSimpleEmailServiceClient");

		VerifyDomainIdentityResult response = amazonSimpleEmailServiceClient.verifyDomainIdentity(request1);

		VerifyDomainDkimRequest request2 = new VerifyDomainDkimRequest().withDomain("dsa");
		VerifyDomainDkimResult response2 =  amazonSimpleEmailServiceClient.verifyDomainDkim(request2);


		return amazonSimpleEmailServiceClient.updateTemplate(request);
	}

	public  int assertIsEmailVerificationTemplateExist(String templateName) {
		return amazonSimpleEmailServiceClient.getCustomVerificationEmailTemplate(
				new GetCustomVerificationEmailTemplateRequest().withTemplateName(templateName)
		).getSdkHttpMetadata().getHttpStatusCode();
	}

	public  String readTemplateContent() throws URISyntaxException, IOException {
		ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class).get();
		Optional<URL> resource = loader.getResource("classpath:templates/verification-template" + ".html");
		Path path = Paths.get(resource.get().toURI());
		System.out.println(resource.toString());
		Stream<String> lines = Files.lines(path);
		String data = lines.collect(Collectors.joining("\n"));
		lines.close();
		System.out.println(data);
		return data;
	}

	private String readFromInputStream(InputStream inputStream)
			throws IOException {
		StringBuilder resultStringBuilder = new StringBuilder();
		try (BufferedReader br
					 = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = br.readLine()) != null) {
				resultStringBuilder.append(line).append("\n");
			}
		}
		return resultStringBuilder.toString();
	}






}
