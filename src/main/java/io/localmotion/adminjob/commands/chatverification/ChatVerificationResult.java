package io.localmotion.adminjob.commands.chatverification;

import lombok.Value;

import java.util.List;

@Value
public class ChatVerificationResult {
    private List<String> deviations;
}
