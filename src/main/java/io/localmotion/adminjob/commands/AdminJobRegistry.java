package io.localmotion.adminjob.commands;

import io.localmotion.adminjob.commands.deletepersonaldata.DeletePersonalDataCommand;
import io.localmotion.adminjob.domain.AdminCommand;
import io.localmotion.adminjob.commands.cognitoimportfile.CognitoImportFileCommand;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class AdminJobRegistry {

    @Inject private CognitoImportFileCommand cognitoImportFileCommand;
    @Inject private DeletePersonalDataCommand deletePersonalDataCommand;

    private final Map<String, AdminCommand> adminJobs = new HashMap<>();

    private boolean registryBuilt = false;
    private void buildRegistry() {
        if (!registryBuilt) {
            registerAdminJob(cognitoImportFileCommand);
            registerAdminJob(deletePersonalDataCommand);
            registryBuilt = true;
        }
    }

    public void registerAdminJob(AdminCommand adminCommand) {
        adminJobs.put(adminCommand.getIdentifier(), adminCommand);
    }

    public Collection<AdminCommand> getAdminJobs() {
        buildRegistry();
        return adminJobs.values();
    }

    public AdminCommand lookupAdminJob(String jobIdentifier) {
        buildRegistry();
        return adminJobs.get(jobIdentifier);
    }
}
