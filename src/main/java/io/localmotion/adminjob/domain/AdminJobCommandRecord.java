package io.localmotion.adminjob.domain;

import lombok.Value;

@Value
public class AdminJobCommandRecord {
    String commandIdentifier;
    String comment;
    String operatorEmail;
    String inputParameters;

    public int getValidationCode() {
        return commandIdentifier.hashCode() + comment.hashCode() + operatorEmail.hashCode() + inputParameters.hashCode();
    }
}