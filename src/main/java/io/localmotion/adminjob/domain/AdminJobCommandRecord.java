package io.localmotion.adminjob.domain;

import lombok.Value;

//public interface AdminJobCommandRecord {
    /**
     *
     * @return the e-mail address of an authenticated user that may trigger this jobs
     */
//    public String getOperatorEmail();
//}

@Value
public class AdminJobCommandRecord {
    String jobIdentifier;
    String description;
    String operatorEmail;
    String inputParameters;
}