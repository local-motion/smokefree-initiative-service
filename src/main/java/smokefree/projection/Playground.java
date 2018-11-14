package smokefree.projection;

import lombok.Value;
import lombok.experimental.Wither;
import smokefree.domain.Status;

@Value
@Wither
public class Playground {
    String id;
    String name;
    Double lat;
    Double lng;
    Status status;
    int volunteerCount;
    int votes;
}
