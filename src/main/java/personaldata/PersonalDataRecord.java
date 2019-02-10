package personaldata;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.util.Date;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//@ToString
@Entity
public class PersonalDataRecord {

    public static final int MAX_PERSON_ID_LENGTH = 255;
    public static final int MAX_DATA_LENGTH = 10240;

    public PersonalDataRecord(String personId, String data) {
        if (personId.length() > MAX_PERSON_ID_LENGTH)
            throw new IllegalArgumentException("personId cannot be longer than " + MAX_PERSON_ID_LENGTH + " characters: (length: " + personId.length() +  "): " + personId);
        if (data.length() > MAX_DATA_LENGTH)
            throw new IllegalArgumentException("data cannot be longer than " + MAX_DATA_LENGTH + " characters: (length: " + data.length() + "): " + data);
        this.personId = personId;
        this.data = data;
    }

    @Id @GeneratedValue
    private Long recordId;

    @NotBlank
    @Column(length=MAX_PERSON_ID_LENGTH)
    private String personId;

    @NotBlank
    @Column(length=MAX_DATA_LENGTH)
    private String data;

}
