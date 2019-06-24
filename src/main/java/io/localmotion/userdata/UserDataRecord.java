package io.localmotion.userdata;

import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class UserDataRecord {

    @NotNull
    @Id
    private String userId;

    @NotNull
    private Date lastUpdate;

    @NotNull
    @Size(max = 20000, message = "User data size exceeded")
    private String text;
}
