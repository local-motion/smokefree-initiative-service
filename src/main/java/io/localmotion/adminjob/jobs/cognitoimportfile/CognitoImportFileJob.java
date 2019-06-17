package io.localmotion.adminjob.jobs.cognitoimportfile;

import io.localmotion.adminjob.domain.AdminJob;

public class CognitoImportFileJob implements AdminJob {

    private static final String JOB_IDENTIFIER = "CognitoInputFile";

    @Override
    public String getJobIdentifier() {
        return JOB_IDENTIFIER;
    }

    @Override
    public void run() {

    }
}
