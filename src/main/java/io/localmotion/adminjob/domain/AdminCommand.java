package io.localmotion.adminjob.domain;

import io.localmotion.security.user.SecurityContext;

/**
 * All specific admin commands must implement this interface so they can be controlled by the admin framework
 */
public interface AdminCommand {

    public String getIdentifier();
    public JobResult run(AdminJobCommandRecord adminJobCommandRecord, SecurityContext securityContext);
}
