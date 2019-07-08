package io.localmotion.adminjob.controller;

import com.google.gson.Gson;
import io.localmotion.adminjob.commands.AdminJobRegistry;
import io.localmotion.adminjob.domain.*;
import io.localmotion.storage.file.FileAccessor;
import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.SimpleDateFormat;
import java.time.Instant;

@Slf4j
@Singleton
public class AdminJobController {

    private static final int EXECUTION_MARKER_EXPIRY = 5*60;   // time until an non-deleted execution marker expires (in seconds)

    @Value("${localmotion.adminjob.location}")
    private String fileLocation;

    @Value("${localmotion.adminjob.historylocation}")
    private String historyLogLocation;

    @Value("${localmotion.adminjob.commandfilename}")
    private String commandFileName;

    @Value("${localmotion.adminjob.executionmarkername}")
    private String executionMarkerName;

    @Value("${localmotion.adminjob.resultfilename}")
    private String resultFileName;

    @Inject
    private FileAccessor fileAccessor;

    @Inject
    private AdminJobRegistry adminJobRegistry;


    public AdminJobCommandRecord readAdminJobCommandRecord() {
        if (!fileAccessor.fileExists(fileLocation, commandFileName))
            return null;
        else {
            Gson gson = new Gson();
            String commandRecordString = fileAccessor.readFileToString(fileLocation, commandFileName);
            return gson.fromJson(commandRecordString, AdminJobCommandRecord.class);
        }
    }

//    public void cancelCurrentJob() {
//        // delete the command record
//        if (fileAccessor.fileExists(fileLocation, executionMarkerName)) {
//            fileAccessor.deleteFile(fileLocation, executionMarkerName);
//        }
//    }

    public JobResult runCurrentJob() {
        final Instant jobDateTime = Instant.now();
        final long executionMarkerTimestamp = jobDateTime.toEpochMilli();
        final String executionMarker = executionMarkerTimestamp + "";

        try {
            // Run the job and write the result
            AdminJobCommandRecord adminJobCommandRecord = readAdminJobCommandRecord();
            AdminCommand adminCommand = adminJobRegistry.lookupAdminJob(adminJobCommandRecord.getCommandIdentifier());
            if (adminCommand == null) {
                log.warn("Job with command identifier '" + adminJobCommandRecord.getCommandIdentifier() + "' not found, cancelling");
                return new JobResult(JobResultCode.FAIL, "Admin command '" + adminJobCommandRecord.getCommandIdentifier() + "' does not exist", null);
            }

            // Check if an execution is already in progress
            if (fileAccessor.fileExists(fileLocation, executionMarkerName)) {
                String existingMarker = fileAccessor.readFileToString(fileLocation, executionMarkerName);
                try {
                    long existingMarkerTimestamp = Long.valueOf(existingMarker);
                    if (existingMarkerTimestamp + EXECUTION_MARKER_EXPIRY < executionMarkerTimestamp)
                        return new JobResult(JobResultCode.FAIL, "Job is already running", null);
                    else
                        fileAccessor.deleteFile(fileLocation, executionMarkerName);
                } catch (NumberFormatException e) {
                    // Invalid file contents, just remove the marker file and continue
                    log.warn("Illegal contents of execution marker file [" + existingMarker + "], deleting and continuing to run job");
                    fileAccessor.deleteFile(fileLocation, executionMarkerName);
                }
            }

            // Create an execution marker to signal that the job is running
            fileAccessor.writeFile(fileLocation, executionMarkerName, executionMarker);

            // Validate that the marker is indeed our marker
            if (!executionMarker.equals(fileAccessor.readFileToString(fileLocation, executionMarkerName)))
                throw new IllegalStateException("Job is already running");

            try {
                // Run the job
                log.info("Running job of " + adminJobCommandRecord);
                JobResult jobResult = adminCommand.run(adminJobCommandRecord);

                // Write the job info to the history bucket
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String historyFileName = "job_" + simpleDateFormat.format(jobDateTime);
                JobHistoryRecord historyRecord = new JobHistoryRecord(jobDateTime, adminJobCommandRecord.getOperatorEmail(), adminJobCommandRecord, jobResult);
                String content = new Gson().toJson(historyRecord);
                fileAccessor.writeFile(historyLogLocation, historyFileName, content);

                // Write the job info to the result file (same as history record). Remove the previous result file first.
                if (!fileAccessor.fileExists(fileLocation, resultFileName))
                    fileAccessor.deleteFile(fileLocation, resultFileName);
                fileAccessor.writeFile(fileLocation, resultFileName, content);

                return jobResult;

            } finally {
                // Remove the execution marker
                fileAccessor.deleteFile(fileLocation, executionMarkerName);
            }
        } catch (Exception e) {
            log.warn("Caught exception while running job:");
            e.printStackTrace();
            return new JobResult(JobResultCode.FAIL, "Job failed with exception: " + e.getMessage(), null);
        }
    }
}
