package io.localmotion.adminjob.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RunAdminJobInput {
    private int validationCode;
    private Boolean retainCommandFile;
}
