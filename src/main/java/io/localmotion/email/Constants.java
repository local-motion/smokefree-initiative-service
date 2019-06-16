package io.localmotion.email;

public class Constants {

	public static final String  NOTIFICATION_ADDRESS = "notification@javaasayudha.tk";

	static final String SUBJECT = "Amazon SES test (AWS SDK for Java)";

	static final String HTMLBODY = "<h1>Amazon SES test (AWS SDK for Java)</h1>"
			+ "<p>This email was sent with <a href='https://aws.amazon.com/ses/'>"
			+ "Amazon SES</a> using the <a href='https://aws.amazon.com/sdk-for-java/'>"
			+ "AWS SDK for Java</a>";

	static final String TEXTBODY = "This email was sent through Amazon SES "
			+ "using the AWS SDK for Java.";
}
