package io.localmotion.email.template.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public interface EmailTemplate {
    String getName();
    String getSubjectLine();
    String getTextPart();
    String getHtmlPart();

    default String readEmailContentFromTemplate(InputStream inputStream) {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }catch(Exception e) {

        }
        return resultStringBuilder.toString();
    }

}
