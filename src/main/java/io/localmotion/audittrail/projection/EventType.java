package io.localmotion.audittrail.projection;

public enum EventType {

    // Initiative events
    INITIATIVE_JOINED,
    CHECKLIST_UPDATE,

    // User events
    USER_CREATED,
    USER_DELETED,
    PERSONAL_DATA_DELETED,
    USER_REVIVED,
    USER_RENAMED,
    NOTIFICATION_SETTINGS_UPDATED,

    // Playground events
    PLAYGROUND_CREATED,
    SMOKEFREE_DECISION,
    SMOKEFREE_DATE_COMMITTED,
    MANAGER_JOINED_INITIATIVE,
    PLAYGROUND_OBSERVATION

    }
