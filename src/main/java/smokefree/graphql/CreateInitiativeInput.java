package smokefree.graphql;

import io.micronaut.validation.Validated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import smokefree.domain.Status;
import smokefree.domain.Type;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public
class CreateInitiativeInput {
    String initiativeId;
    Type type;
    Status status;

    @Length(min = 3, message = "The name must be at least 3 characters")
    @Length(max = 40, message = "The name must be less than 40 characters")
    @NotNull
    String name;

    Double lat;
    Double lng;
}
