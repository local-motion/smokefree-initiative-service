package io.localmotion.adminjob.commands;

import io.localmotion.adminjob.commands.chatmigration_v1_0_18_v1_0_19.ChatMigrationCommand;
import io.localmotion.adminjob.commands.chatverification.ChatVerificationCommand;
import io.localmotion.adminjob.commands.deletepersonaldata.DeletePersonalDataJobCommand;
import io.localmotion.adminjob.commands.statistics.StatisticsJobCommand;
import io.localmotion.adminjob.domain.AdminCommand;
import io.localmotion.adminjob.commands.cognitoimportfile.CognitoImportFileJobCommand;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class AdminJobRegistry {

    @Inject private CognitoImportFileJobCommand cognitoImportFileJobCommand;
    @Inject private DeletePersonalDataJobCommand deletePersonalDataJobCommand;
    @Inject private StatisticsJobCommand statisticsJobCommand;
    @Inject private ChatVerificationCommand chatVerificationCommand;
    @Inject private ChatMigrationCommand chatMigrationCommand;

    private final Map<String, AdminCommand> adminJobs = new HashMap<>();

    private boolean registryBuilt = false;
    private void buildRegistry() {
        if (!registryBuilt) {
            registerAdminJob(cognitoImportFileJobCommand);
            registerAdminJob(deletePersonalDataJobCommand);
            registerAdminJob(statisticsJobCommand);
            registerAdminJob(chatVerificationCommand);
            registerAdminJob(chatMigrationCommand);
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
