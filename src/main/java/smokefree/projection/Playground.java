package smokefree.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import smokefree.domain.Status;

import java.time.LocalDate;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Data
@AllArgsConstructor
public class Playground {
    String id;
    String name;
    Double lat;
    Double lng;
    Status status;
    LocalDate smokeFreeDate;
    int volunteerCount;
    int votes;
    final List<Manager> managers = newArrayList();

    Playground addManager(Manager manager) {
        managers.add(manager);
        return this;
    }

    @Value
    static class Manager {
        String id;
        String username;
    }
}
