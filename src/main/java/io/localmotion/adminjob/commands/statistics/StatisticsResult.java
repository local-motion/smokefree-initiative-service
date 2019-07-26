package io.localmotion.adminjob.commands.statistics;

import lombok.Value;

@Value
public class StatisticsResult {
    private int chatMessageCount;
    private int eventCount;
    private int snapshotCount;
    private int userDataCount;
    private int personalDataRecordCount;
    private int profileCount;
    private int deletedProfileCount;
    private int initiativeCount;
}
