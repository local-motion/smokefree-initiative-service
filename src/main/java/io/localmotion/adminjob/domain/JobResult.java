package io.localmotion.adminjob.domain;

import lombok.Value;

@Value
public class JobResult {
    private JobResultCode resultCode;
    private String message;
    private String result;                  // Json object
}
