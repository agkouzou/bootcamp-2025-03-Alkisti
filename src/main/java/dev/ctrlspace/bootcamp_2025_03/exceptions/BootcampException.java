package dev.ctrlspace.bootcamp_2025_03.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BootcampException extends Exception {

    private final HttpStatus httpStatus;

    public BootcampException(String message) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public BootcampException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public BootcampException(String message, Throwable cause) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }

    public BootcampException(HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public BootcampException(Throwable cause) {
        super(cause);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
