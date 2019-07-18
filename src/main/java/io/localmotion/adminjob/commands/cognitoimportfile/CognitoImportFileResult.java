package io.localmotion.adminjob.commands.cognitoimportfile;

import lombok.Value;

@Value
public class CognitoImportFileResult {
    private int recordsWritten;
    private int hashcode;
}
