package io.localmotion.adminjob.commands.chatmigration_v1_0_18_v1_0_19;

import lombok.Value;

import java.util.List;

@Value
public class ChatMigrationResult {
    private List<String> conversions;
}
