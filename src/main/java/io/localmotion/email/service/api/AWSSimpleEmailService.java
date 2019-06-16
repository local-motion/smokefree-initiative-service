package io.localmotion.email.service.api;

import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.ResponseMetadata;
import io.localmotion.email.template.api.EmailTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

public interface AWSSimpleEmailService {

	AmazonWebServiceResult<ResponseMetadata> sendEmail(String toAddress, EmailTemplate template, Map<String, String> templateData);

	AmazonWebServiceResult<ResponseMetadata> sendTemplateEmail(String templateName, String toAddress, Map<String, String> data);

	AmazonWebServiceResult<ResponseMetadata> verifyEmailAddress(String emailAddress);

	AmazonWebServiceResult<ResponseMetadata> getAllEmailAddresses();

	AmazonWebServiceResult<ResponseMetadata> deleteEmailIdentity(String emailAddress);

	//Verification EmailTemplate
	AmazonWebServiceResult<ResponseMetadata> createCustomVerificationTemplate();

	AmazonWebServiceResult<ResponseMetadata> confirmEmailAddress(String confirmAddress);

	AmazonWebServiceResult<ResponseMetadata> createEmailTemplate(String templateName);

	AmazonWebServiceResult<ResponseMetadata> createEmailTemplate(String templateName, String body);

	AmazonWebServiceResult<ResponseMetadata> updateEmailTemplate(String templateName, String body);

	String readTemplateContent() throws URISyntaxException, IOException;

}
