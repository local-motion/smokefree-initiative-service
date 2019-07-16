package io.localmotion.adminjob.commands.deletepersonaldata;

import com.google.gson.Gson;
import io.localmotion.adminjob.controller.AdminJobController;
import io.localmotion.adminjob.domain.AdminCommand;
import io.localmotion.adminjob.domain.AdminJobCommandRecord;
import io.localmotion.adminjob.domain.JobResult;
import io.localmotion.adminjob.domain.JobResultCode;
import io.localmotion.security.user.SecurityContext;
import io.localmotion.personaldata.PersonalDataRepository;
import io.localmotion.user.command.RetrieveUserCommand;
import io.localmotion.user.projection.Profile;
import io.localmotion.user.projection.ProfileProjection;
import org.axonframework.commandhandling.gateway.CommandGateway;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DeletePersonalDataCommand implements AdminCommand {

    private static final String COMMAND_IDENTIFIER = "DeletePersonalData";

    @Inject
    private CommandGateway gateway;

    @Inject
    private ProfileProjection profileProjection;

    @Inject
    private PersonalDataRepository personalDataRepository;

    @Override
    public String getIdentifier() {
        return COMMAND_IDENTIFIER;
    }

    @Override
    public JobResult run(AdminJobCommandRecord adminJobCommandRecord, SecurityContext securityContext) {
        DeletePersonalDataInput input = new Gson().fromJson(adminJobCommandRecord.getInputParameters(), DeletePersonalDataInput.class);

        Profile profile = profileProjection.getDeletedProfile(input.getUserId());
        if (profile == null)
            if (profileProjection.profile(input.getUserId()) == null)
            return new JobResult(JobResultCode.FAIL, "User does not exist", "");
        else
            return new JobResult(JobResultCode.FAIL, "User is still active. Offboard the user first.", "");

        int deletedCount = gateway.sendAndWait(AdminJobController.decorateWithMetaData(new io.localmotion.user.command.DeletePersonalDataCommand(securityContext.requireUserId()), securityContext));

        DeletePersonalDataResult result = new DeletePersonalDataResult(deletedCount);

        return new JobResult(JobResultCode.SUCCESS, "Deleted " + deletedCount + " personal data records", new Gson().toJson(result));
    }



}
