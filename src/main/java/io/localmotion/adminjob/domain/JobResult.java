package io.localmotion.adminjob.domain;

import lombok.Value;

@Value
public class JobResult {
    private JobResultCode resultCode;
    private String message;
    private String result;                  // Json object

    public JobResult(JobResultCode resultCode, String message, String result) {
        this.resultCode = resultCode;
        this.message = message;
        this.result = result;
    }

    public JobResult(JobResultCode resultCode, String message) {
        this.resultCode = resultCode;
        this.message = message;
        this.result = null;
    }

}
