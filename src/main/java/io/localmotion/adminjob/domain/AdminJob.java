package io.localmotion.adminjob.domain;

/**
 * All specific admin jobs must implement this interface so they can be controlled by the admin framework
 */
public interface AdminJob {

    public String getJobIdentifier();
    public String run(AdminJobCommandRecord adminJobCommandRecord);
}
