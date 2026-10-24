package dev.ctrlspace.bootcamp_2025_03.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class BootcampControllerAdvice {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(BootcampException.class)
    public ResponseEntity<BootcampErrorEntity> handleBootcampException(BootcampException e) {
        HttpStatus status = e.getHttpStatus() != null ? e.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
        logger.error("Bootcamp App error [{}]: {}", status, e.getMessage(), e);
        return buildResponse(status, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BootcampErrorEntity> handleUnexpected(Exception e) {
        logger.error("Unhandled error: ", e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    private ResponseEntity<BootcampErrorEntity> buildResponse(HttpStatus status, String message) {
        BootcampErrorEntity errorEntity = new BootcampErrorEntity();
        errorEntity.setErrorCode(status.value());
        errorEntity.setErrorDescription(status.name());
        errorEntity.setMessage(message);
        return ResponseEntity.status(status).body(errorEntity);
    }
}
