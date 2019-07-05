package io.localmotion.adminjob.domain;

import com.google.gson.Gson;
import io.localmotion.adminjob.jobs.AdminJobRegistry;
import io.localmotion.storage.file.FileAccessor;
import io.micronaut.context.annotation.Value;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.SimpleDateFormat;
import java.time.Instant;

@Singleton
public class AdminJobController {

    @Value("${localmotion.adminjob.commandfileurl}")
    private String commandFileUrl;

    @Value("${localmotion.adminjob.bucketname}")
    private String bucketName;

    @Value("${localmotion.adminjob.historybucketname}")
    private String historyBucketName;

    @Value("${localmotion.adminjob.commandfilekey}")
    private String commandFileKey;

    @Value("${localmotion.adminjob.resultfilekey}")
    private String resultFileKey;

    @Value("${localmotion.adminjob.jobhistorykey}")
    private String jobHistoryKey;

    @Inject
    private FileAccessor fileAccessor;


    @Inject
    private AdminJobRegistry adminJobRegistry;

    public AdminJobCommandRecord readAdminJobCommandRecord() {
        Gson gson = new Gson();
        String commandRecordString = fileAccessor.readFileToString(bucketName, commandFileKey);
        return gson.fromJson(commandRecordString, AdminJobCommandRecord.class);

    }

    public void cancelCurrentJob() {
        // delete the command record
    }

    public void runCurrentJob() {
        Instant jobDateTime = Instant.now();

        // Run the job and write the result
        AdminJobCommandRecord adminJobCommandRecord = readAdminJobCommandRecord();
        AdminJob adminJob = adminJobRegistry.lookupAdminJob(adminJobCommandRecord.getJobIdentifier());
        String resultString = adminJob.run(adminJobCommandRecord);
        fileAccessor.writeFile(bucketName, resultFileKey, resultString);

        // Write the job info to the history bucket
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String historyKey = "run_" + simpleDateFormat.format(jobDateTime);
        JobHistoryRecord historyRecord = new JobHistoryRecord(jobDateTime, adminJobCommandRecord.getOperatorEmail(), adminJobCommandRecord, resultString);
        String content = new Gson().toJson(historyRecord);
        fileAccessor.writeFile(historyBucketName, historyKey, content);
    }
}
