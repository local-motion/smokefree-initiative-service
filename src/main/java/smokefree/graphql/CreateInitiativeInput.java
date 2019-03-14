package smokefree.graphql;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import smokefree.domain.Status;
import smokefree.domain.Type;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public
class CreateInitiativeInput {

    @NotNull(message = "The initiativeId must have a value")
    @Size(min = 1, message = "The initiativeId must not be blank")
    String initiativeId;

    Type type;

    @NotNull(message = "The status must not be blank")
    Status status;

    @Length(min = 3, message = "The name must be at least 3 characters")
    @Length(max = 40, message = "The name must be less than 40 characters")
    @NotNull
    String name;

    @NotNull(message = "The lat must not be blank")
    Double lat;

    @NotNull(message = "The lng must not be blank")
    Double lng;
}
