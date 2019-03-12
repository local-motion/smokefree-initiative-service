package smokefree.domain;

import org.junit.jupiter.api.Test;
import smokefree.graphql.CreateInitiativeInput;

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

    @Test
    void should_haveConstraintViolation_when_playgroundNameHasMoreThan40Characters() {
        CreateInitiativeInput cmd = new CreateInitiativeInput(
                "initiative-1",
                Type.smokefree,
                Status.not_started,
                "Happy DiemenHappy DiemenHappy DiemenHappy DiemenHappy Diemen",
                22223.32,
                32.322
        );
        Set<ConstraintViolation<CreateInitiativeInput>> violations = validatorFactory.getValidator().validate(cmd);
        assertEquals(1, violations.size());
        assertEquals("The name must be less than 40 characters", violations.iterator().next().getMessage());
    }

    @Test
    void should_NotHaveConstraintViolation_when_playgroundNameHas3To40Characters() {
        CreateInitiativeInput cmd = new CreateInitiativeInput(
                "initiative-1",
                Type.smokefree,
                Status.not_started,
                "Happy Diemen",
                22223.32,
                32.322
        );
        Set<ConstraintViolation<CreateInitiativeInput>> violations = validatorFactory.getValidator().validate(cmd);
        assertEquals(0, violations.size());
    }

    @Test
    void should_haveConstraintViolation_when_playgroundInitiativeIdIsNull() {
        CreateInitiativeInput cmd = new CreateInitiativeInput(
                null,
                Type.smokefree,
                Status.not_started,
                "Happy Diemen",
                22223.32,
                32.322
        );
        Set<ConstraintViolation<CreateInitiativeInput>> violations = validatorFactory.getValidator().validate(cmd);
        assertEquals(1, violations.size());
        assertEquals("The initiativeId must have a value", violations.iterator().next().getMessage());
    }
    @Test
    void should_haveConstraintViolation_when_playgroundInitiativeIdIsBlank() {
        CreateInitiativeInput cmd = new CreateInitiativeInput(
                "",
                Type.smokefree,
                Status.not_started,
                "Happy Diemen",
                22223.32,
                32.322
        );
        Set<ConstraintViolation<CreateInitiativeInput>> violations = validatorFactory.getValidator().validate(cmd);
        assertEquals(1, violations.size());
        assertEquals("The initiativeId must not be blank", violations.iterator().next().getMessage());
    }

    @Test
    void should_haveConstraintViolation_when_playgroundStatusIsNull() {
        CreateInitiativeInput cmd = new CreateInitiativeInput(
                "Name",
                Type.smokefree,
                null,
                "Happy Diemen",
                22223.32,
                32.322
        );
        Set<ConstraintViolation<CreateInitiativeInput>> violations = validatorFactory.getValidator().validate(cmd);
        assertEquals(1, violations.size());
        assertEquals("The status must not be blank", violations.iterator().next().getMessage());
    }

    @Test
    void should_haveConstraintViolation_whenPlaygroundLongitudeIsNull() {
        CreateInitiativeInput cmd = new CreateInitiativeInput(
                "Name",
                Type.smokefree,
                Status.not_started,
                "Happy Diemen",
                32.234566,
                null
        );
        Set<ConstraintViolation<CreateInitiativeInput>> violations = validatorFactory.getValidator().validate(cmd);
        assertEquals(1, violations.size());
        assertEquals("The lng must not be blank", violations.iterator().next().getMessage());

    }

    @Test
    void should_haveConstraintViolation_whenPlaygroundLatitudeIsNull() {
        CreateInitiativeInput cmd = new CreateInitiativeInput(
                "Name",
                Type.smokefree,
                Status.not_started,
                "Happy Diemen",
                null,
                32.234566
        );
        Set<ConstraintViolation<CreateInitiativeInput>> violations = validatorFactory.getValidator().validate(cmd);
        assertEquals(1, violations.size());
        assertEquals("The lat must not be blank", violations.iterator().next().getMessage());

    }

}