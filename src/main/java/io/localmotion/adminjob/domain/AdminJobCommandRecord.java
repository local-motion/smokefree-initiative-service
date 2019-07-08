package io.localmotion.adminjob.domain;

import lombok.Value;

//public interface AdminJobCommandRecord {
    /**
     *
     * @return the e-mail address of an authenticated user that may trigger this commands
     */
//    public String getOperatorEmail();
//}

@Value
public class AdminJobCommandRecord {
    String commandIdentifier;
    String comment;
    String operatorEmail;
    String inputParameters;
}