package dev.ctrlspace.bootcamp_2025_03.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BootcampErrorEntity {

    private String message;
    private Integer errorCode;
    private String errorDescription;

}
