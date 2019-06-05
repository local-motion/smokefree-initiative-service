package io.localmotion.initiative.controller;

import io.localmotion.smokefreeplaygrounds.domain.Status;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public
class CreateInitiativeInput {

    @NotNull(message = "The initiativeId must have a value")
    @Size(min = 1, message = "The initiativeId must not be blank")
    String initiativeId;

    @NotBlank
    @Length(min = 3, message = "The name must be at least 3 characters")
    @Length(max = 40, message = "The name must be less than 40 characters")
//    @Pattern(regexp="^[a-zA-Z0-9&'!\\-][ a-zA-Z0-9&'!\\-]{0,38}[a-zA-Z0-9&'!\\-]$") // only the middle characters may be a space
    @Pattern(regexp="^([a-zA-Z0-9&'!\\-] ?)*[a-zA-Z0-9&'!\\-]$") // only the middle characters may be a space and no consecutive spaces
    String name;

    @NotNull(message = "The lat must not be blank")
    Double lat;

    @NotNull(message = "The lng must not be blank")
    Double lng;
}
