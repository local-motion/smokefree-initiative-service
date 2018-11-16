package smokefree.domain;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateInitiativeCommandTest {

    private ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    @Test
    void when_html_input_should_fail() {
        CreateInitiativeCommand cmd = new CreateInitiativeCommand(
                "initiative-1",
                "<script>alert('hello');</script>",
                Type.smokefree,
                Status.not_started,
                null
        );

        Set<ConstraintViolation<CreateInitiativeCommand>> violations = validatorFactory.getValidator().validate(cmd);
        assertEquals(1, violations.size());
        assertEquals("may have unsafe html content", violations.iterator().next().getMessage());
    }
}