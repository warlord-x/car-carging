package com.example.demo.exceptions;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ChargerPointExceptionHandler extends ResponseEntityExceptionHandler {


    private static final String INTERNAL_ERROR_CODE = "internal-error-code";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String ERROR_CODE = "errorCode";
    public static final String ERROR_CODE_INVALID_CHARGING_POINT = "invalid-charging-point";

    @ExceptionHandler(value
            = { InvalidCharingPointException.class })
    protected ResponseEntity<Object> handleInvalidChargingPointException(
            InvalidCharingPointException ex, WebRequest request) {

        return getErrorJson(ex,request,HttpStatus.BAD_REQUEST, ERROR_CODE_INVALID_CHARGING_POINT);
    }

    @ExceptionHandler(value
            = { Exception.class})
    protected ResponseEntity<Object> handleInternalError(
            RuntimeException ex, WebRequest request) {
        return getErrorJson(ex,request,HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_CODE);
    }



    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return getErrorJson(ex, request,HttpStatus.BAD_REQUEST, INTERNAL_ERROR_CODE);
    }

    private ResponseEntity<Object> getErrorJson(Exception ex, WebRequest request, HttpStatus errorStatus, String errorCode) {
        String bodyOfResponse = "{\n" +
                "  \"" + ERROR_MESSAGE + "\": \"" +ex.getMessage()+"\",\n" +
                "  \"" + ERROR_CODE + "\" : \"" +errorCode+"\"\n" +
                "}";
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), errorStatus , request);
    }



}
