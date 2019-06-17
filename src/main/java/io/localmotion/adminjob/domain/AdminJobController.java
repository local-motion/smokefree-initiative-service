package io.localmotion.adminjob.domain;

import io.localmotion.adminjob.jobs.AdminJobRegistry;
import io.micronaut.context.annotation.Value;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AdminJobController {

    @Value("${localmotion.adminjob.commandfileurl}")
    private String commandFileUrl;

    @Inject
    private AdminJobRegistry adminJobRegistry;

    public AdminJobCommandRecord readAdminJobCommandRecord() {
        return null;
    }

    public void cancelCurrentJob() {
        // delete the command record
    }

    public void runCurrentJob() {
        AdminJobCommandRecord adminJobCommandRecord = readAdminJobCommandRecord();
        AdminJob adminJob = adminJobRegistry.lookupAdminJob(adminJobCommandRecord.getJobIdentifier());
        adminJob.run();
    }
}
