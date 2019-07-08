package io.localmotion.adminjob.commands.cognitoimportfile;

import com.google.gson.Gson;
import io.localmotion.adminjob.commands.AdminJobRegistry;
import io.localmotion.adminjob.domain.AdminCommand;
import io.localmotion.adminjob.domain.AdminJobCommandRecord;
import io.localmotion.adminjob.domain.JobResult;
import io.localmotion.adminjob.domain.JobResultCode;
import io.localmotion.storage.file.FileAccessor;
import io.localmotion.user.projection.Profile;
import io.localmotion.user.projection.ProfileProjection;
import io.micronaut.context.annotation.Value;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CognitoImportFileCommand implements AdminCommand {

    private static final String COMMAND_IDENTIFIER = "GenerateCognitoInputFile";
    private static final String FILE_HEADERS = "cognito:username,name,given_name,family_name,middle_name,nickname,preferred_username,profile,picture,website,email,email_verified,gender,birthdate,zoneinfo,locale,phone_number,phone_number_verified,address,updated_at,cognito:mfa_enabled";

    @Value("${localmotion.adminjob.location}")
    private String fileLocation;

    @Value("${localmotion.adminjob.cognitoimportfile.filename}")
    private String fileName;


    @Inject
    private ProfileProjection profileProjection;

    @Inject
    private FileAccessor fileAccessor;


    @Override
    public String getIdentifier() {
        return COMMAND_IDENTIFIER;
    }

    @Override
    public JobResult run(AdminJobCommandRecord adminJobCommandRecord) {
        int recordsWritten = 0;
        int hashcode = 0;
        StringBuilder sb = new StringBuilder(FILE_HEADERS + "\r\n");
        for (Profile i : profileProjection.getAllProfiles()) {
            String record = i.getUsername() + ",,,,,,,,,," + i.getEmailAddress() + ",FALSE,,,,,,,,,FALSE\r\n";
            sb.append(record);
            recordsWritten++;
            hashcode += record.hashCode();
        }

        if (fileAccessor.fileExists(fileLocation, fileName))
            fileAccessor.deleteFile(fileLocation, fileName);

        fileAccessor.writeFile(fileLocation, fileName, sb.toString());
        CognitoImportFileResult result = new CognitoImportFileResult(recordsWritten, hashcode);

        return new JobResult(JobResultCode.SUCCESS, "Export successful", new Gson().toJson(result));
    }
}
