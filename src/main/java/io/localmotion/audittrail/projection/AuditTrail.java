package io.localmotion.audittrail.projection;

import java.util.LinkedList;
import java.util.List;

public class AuditTrail {

    private final int maxRecords;

    private int recordsProcessed = 0;
    private final List<AuditTrailRecord> records;

    AuditTrail(int maxRecords) {
        this.maxRecords = maxRecords;
        this.records = new LinkedList<>();
    }

    AuditTrail(List<AuditTrailRecord> records) {
        this.maxRecords = records.size();
        this.records = records;
    }

    void addRecord(AuditTrailRecord record) {
        records.add(record);
        if (++recordsProcessed > maxRecords)
            records.remove(0);
    }

    List<AuditTrailRecord> getRecords() {
        return records;
    }

    int getTotalRecords() {
        return recordsProcessed;
    }

}
