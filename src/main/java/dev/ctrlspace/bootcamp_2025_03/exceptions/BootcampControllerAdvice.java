package dev.ctrlspace.bootcamp_2025_03.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class BootcampControllerAdvice {

    Logger logger = LoggerFactory.getLogger(this.getClass());


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleConflict(Exception e) {
        // Log the exception
        logger.error("Bootcamp App error: ", e);

        BootcampErrorEntity errorEntity = new BootcampErrorEntity();
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        if (e instanceof BootcampException) {
            httpStatus = ((BootcampException) e).getHttpStatus();
        }

        errorEntity.setErrorCode(httpStatus.value());
        errorEntity.setErrorDescription(httpStatus.name());
        errorEntity.setMessage(e.getMessage());


        return ResponseEntity.status(httpStatus).body(errorEntity);

    }
}
