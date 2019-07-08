package io.localmotion.adminjob.commands.cognitoimportfile;

import com.google.gson.Gson;
import io.localmotion.adminjob.domain.AdminCommand;
import io.localmotion.adminjob.domain.AdminJobCommandRecord;
import io.localmotion.adminjob.domain.JobResult;
import io.localmotion.adminjob.domain.JobResultCode;
import io.localmotion.storage.file.FileAccessor;
import io.localmotion.user.projection.Profile;
import io.localmotion.user.projection.ProfileProjection;
import io.micronaut.context.annotation.Value;

import javax.inject.Inject;

public class CognitoImportFileCommand implements AdminCommand {

    private static final String COMMAND_IDENTIFIER = "GenerateCognitoInputFile";

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
        StringBuilder sb = new StringBuilder();
        for (Profile i : profileProjection.getAllProfiles()) {
            String record = i.getId() + "," + i.getUsername() + "," + i.getEmailAddress() + "\n\r";
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
