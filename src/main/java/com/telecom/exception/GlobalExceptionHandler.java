package com.telecom.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Every error is an RFC 9457 ProblemDetail: one shape to document (see errors.adoc).
 * Extending ResponseEntityExceptionHandler makes Spring MVC's own exceptions (405, 415, 400 ...) ProblemDetails
 * too, and gives exactly one handler for validation errors.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail notFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail duplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Duplicate Resource", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidStateException.class)
    public ProblemDetail invalidState(InvalidStateException ex, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Invalid State Transition", ex.getMessage(), request.getRequestURI());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getFieldErrors().forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        String path = request instanceof ServletWebRequest swr ? swr.getRequest().getRequestURI() : null;
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Invalid Request", "Validation failed", path);
        pd.setProperty("errors", errors);
        return handleExceptionInternal(ex, pd, headers, status, request);
    }

    static ProblemDetail problem(HttpStatus status, String title, String detail, String path) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        if (path != null) {
            pd.setInstance(URI.create(path));
        }
        return pd;
    }
}
