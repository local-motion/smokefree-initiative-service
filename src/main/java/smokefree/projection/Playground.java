package smokefree.projection;

import lombok.Value;
import lombok.experimental.Wither;
import smokefree.domain.Status;

import java.time.LocalDate;

@Value
@Wither
public class Playground {
    String id;
    String name;
    Double lat;
    Double lng;
    Status status;
    LocalDate smokeFreeDate;
    int volunteerCount;
    int votes;
}
