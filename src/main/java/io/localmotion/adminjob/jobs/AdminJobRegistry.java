package io.localmotion.adminjob.jobs;

import io.localmotion.adminjob.domain.AdminJob;
import io.localmotion.adminjob.jobs.cognitoimportfile.CognitoImportFileJob;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class AdminJobRegistry {

    private final Map<String, AdminJob> adminJobs = new HashMap<>();

    {
        registerAdminJob(new CognitoImportFileJob());
    }

    public void registerAdminJob(AdminJob adminJob) {
        adminJobs.put(adminJob.getJobIdentifier(), adminJob);
    }

    public Collection<AdminJob> getAdminJobs() {
        return adminJobs.values();
    }

    public AdminJob lookupAdminJob(String jobIdentifier) {
        return adminJobs.get(jobIdentifier);
    }
}
