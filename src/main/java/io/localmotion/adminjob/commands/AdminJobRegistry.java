package io.localmotion.adminjob.commands;

import io.localmotion.adminjob.domain.AdminCommand;
import io.localmotion.adminjob.commands.cognitoimportfile.CognitoImportFileCommand;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class AdminJobRegistry {

    private final Map<String, AdminCommand> adminJobs = new HashMap<>();

    {
        registerAdminJob(new CognitoImportFileCommand());
    }

    public void registerAdminJob(AdminCommand adminCommand) {
        adminJobs.put(adminCommand.getIdentifier(), adminCommand);
    }

    public Collection<AdminCommand> getAdminJobs() {
        return adminJobs.values();
    }

    public AdminCommand lookupAdminJob(String jobIdentifier) {
        return adminJobs.get(jobIdentifier);
    }
}
