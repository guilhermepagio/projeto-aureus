package com.guilhermepagio.aureus.backend.exception;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        List<ApiErrorResponse.ValidationErrorItem> errors = new ArrayList<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
            errors.add(new ApiErrorResponse.ValidationErrorItem(
                fieldError.getField(),
                fieldError.getDefaultMessage(),
                fieldError.getDefaultMessage()
            ));
        }

        for (ObjectError globalError : ex.getBindingResult().getGlobalErrors()) {
            String objectName = globalError.getObjectName();
            fieldErrors.putIfAbsent(objectName, globalError.getDefaultMessage());
            errors.add(new ApiErrorResponse.ValidationErrorItem(
                objectName,
                globalError.getDefaultMessage(),
                globalError.getDefaultMessage()
            ));
        }

        String path = request != null ? request.getRequestURI() : "";
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Erro de validação nos campos informados",
            path,
            fieldErrors,
            errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiErrorResponse> handleTypeMismatchAndMissingParam(Exception ex, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        String message = "Parâmetro de requisição inválido ou ausente.";
        if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            message = String.format("O parâmetro '%s' possui formato ou tipo inválido.", mismatch.getName());
        } else if (ex instanceof MissingServletRequestParameterException missing) {
            message = String.format("O parâmetro obrigatório '%s' não foi informado.", missing.getParameterName());
        }
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            message,
            path,
            null,
            null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        List<ApiErrorResponse.ValidationErrorItem> errors = new ArrayList<>();

        if (ex.getConstraintViolations() != null) {
            for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
                String propertyPath = "param";
                if (violation.getPropertyPath() != null) {
                    String fullPath = violation.getPropertyPath().toString();
                    if (fullPath != null && !fullPath.isBlank()) {
                        int lastDot = fullPath.lastIndexOf('.');
                        propertyPath = lastDot >= 0 ? fullPath.substring(lastDot + 1) : fullPath;
                    }
                }
                fieldErrors.putIfAbsent(propertyPath, violation.getMessage());
                errors.add(new ApiErrorResponse.ValidationErrorItem(
                    propertyPath,
                    violation.getMessage(),
                    violation.getMessage()
                ));
            }
        }

        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Erro de validação nos parâmetros informados",
            path,
            fieldErrors,
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        List<ApiErrorResponse.ValidationErrorItem> errors = new ArrayList<>();

        if (ex.getParameterValidationResults() != null) {
            for (var parameterValidationResult : ex.getParameterValidationResults()) {
                String paramName = parameterValidationResult.getMethodParameter().getParameterName();
                for (var resolvableError : parameterValidationResult.getResolvableErrors()) {
                    String defaultMessage = resolvableError.getDefaultMessage();
                    String fieldKey = paramName != null ? paramName : "param";
                    fieldErrors.putIfAbsent(fieldKey, defaultMessage);
                    errors.add(new ApiErrorResponse.ValidationErrorItem(
                        fieldKey,
                        defaultMessage,
                        defaultMessage
                    ));
                }
            }
        }

        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Erro de validação nos parâmetros informados",
            path,
            fieldErrors,
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violação de integridade relacional: {}", ex.getMessage());
        String path = request != null ? request.getRequestURI() : "";
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Não é possível realizar a operação devido a vínculos ativos ou restrições de integridade.",
            path,
            null,
            null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            path,
            null,
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.FORBIDDEN.value(),
            HttpStatus.FORBIDDEN.getReasonPhrase(),
            ex.getMessage(),
            path,
            null,
            null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler({
        IllegalArgumentException.class,
        DateTimeParseException.class
    })
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(Exception ex, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        String message = ex.getMessage() != null && !ex.getMessage().isBlank()
            ? ex.getMessage()
            : "Argumento ou parâmetro inválido.";
        if (ex instanceof DateTimeParseException) {
            message = "Formato de data inválido: " + ex.getMessage();
        }
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            message,
            path,
            null,
            null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Corpo da requisição inválido ou mal formatado.",
            path,
            null,
            null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            "Recurso não encontrado: " + path,
            path,
            null,
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
            "Método HTTP não suportado para este endpoint.",
            path,
            null,
            null
        );
        HttpHeaders headers = new HttpHeaders();
        if (ex.getSupportedHttpMethods() != null) {
            headers.setAllow(ex.getSupportedHttpMethods());
        }
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).body(response);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        String mediaTypeInfo = ex.getContentType() != null ? ex.getContentType().toString() : (ex.getMessage() != null ? ex.getMessage() : "");
        String message = !mediaTypeInfo.isBlank() ? "Tipo de mídia não suportado: " + mediaTypeInfo : "Tipo de mídia não suportado.";
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(),
            message,
            path,
            null,
            null
        );
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiErrorResponse> handleMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.NOT_ACCEPTABLE.value(),
            HttpStatus.NOT_ACCEPTABLE.getReasonPhrase(),
            "Tipo de mídia requisitado não é suportado pelo servidor.",
            path,
            null,
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Erro interno inesperado no servidor: ", ex);
        String path = request != null ? request.getRequestURI() : "";
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "Ocorreu um erro interno inesperado no servidor.",
            path,
            null,
            null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
