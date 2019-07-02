package io.localmotion.adminjob.domain;

import lombok.Value;

import java.time.Instant;


@Value
public class JobHistoryRecord {
    Instant runDateTime;
    String operatorEmail;
    AdminJobCommandRecord adminJobCommandRecord;
    String result;
}