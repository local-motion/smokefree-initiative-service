package io.localmotion.userdata;

import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserData {

    private LocalDateTime lastAuditTrailView;

}
