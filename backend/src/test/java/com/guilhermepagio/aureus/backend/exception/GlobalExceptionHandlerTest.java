package com.guilhermepagio.aureus.backend.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    public void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
    }

    @Test
    public void deveTratarMethodArgumentNotValidExceptionComStatus400EItensDeValidacao() throws NoSuchMethodException {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "testObject");
        bindingResult.addError(new FieldError("testObject", "descricao", "A descrição é obrigatória"));
        bindingResult.addError(new FieldError("testObject", "valor", "O valor deve ser maior que zero"));
        bindingResult.addError(new org.springframework.validation.ObjectError("testObject", "Constraint global de objeto violada"));

        MethodParameter parameter = new MethodParameter(
            this.getClass().getDeclaredMethod("setUp"), -1
        );
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleMethodArgumentNotValid(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
        assertEquals("/api/test", body.path());
        assertNotNull(body.timestamp());

        // Field errors map
        assertNotNull(body.fieldErrors());
        assertEquals(3, body.fieldErrors().size());
        assertEquals("A descrição é obrigatória", body.fieldErrors().get("descricao"));
        assertEquals("O valor deve ser maior que zero", body.fieldErrors().get("valor"));
        assertEquals("Constraint global de objeto violada", body.fieldErrors().get("testObject"));

        // Errors list (compatible with frontend errorData.errors[0].defaultMessage)
        assertNotNull(body.errors());
        assertEquals(3, body.errors().size());
        assertEquals("descricao", body.errors().get(0).field());
        assertEquals("A descrição é obrigatória", body.errors().get(0).defaultMessage());
        assertEquals("A descrição é obrigatória", body.errors().get(0).message());
        assertEquals("testObject", body.errors().get(2).field());
        assertEquals("Constraint global de objeto violada", body.errors().get(2).defaultMessage());
    }

    @Test
    public void deveTratarHttpMessageNotReadableExceptionComStatus400() {
        org.springframework.http.converter.HttpMessageNotReadableException ex =
            new org.springframework.http.converter.HttpMessageNotReadableException("JSON parse error", (org.springframework.http.HttpInputMessage) null);

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleHttpMessageNotReadable(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
        assertEquals("Corpo da requisição inválido ou mal formatado.", body.message());
        assertEquals("/api/test", body.path());
    }

    @Test
    public void deveTratarMethodArgumentTypeMismatchExceptionComStatus400() {
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex =
            new org.springframework.web.method.annotation.MethodArgumentTypeMismatchException("abc", Long.class, "id", null, null);

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleTypeMismatchAndMissingParam(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
        assertTrue(body.message().contains("Parâmetro de requisição inválido ou ausente"));
        assertEquals("/api/test", body.path());
    }

    @Test
    public void deveTratarMissingServletRequestParameterExceptionComStatus400() {
        org.springframework.web.bind.MissingServletRequestParameterException ex =
            new org.springframework.web.bind.MissingServletRequestParameterException("mesAno", "String");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleTypeMismatchAndMissingParam(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
        assertTrue(body.message().contains("Parâmetro de requisição inválido ou ausente"));
        assertEquals("/api/test", body.path());
    }

    @Test
    public void deveTratarDataIntegrityViolationExceptionComStatus400ESemVazarDetalhesInternos() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
            "ERROR: update or delete on table \"contas\" violates foreign key constraint \"fk_despesas_conta\" on table \"despesas_fixas\""
        );

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
        assertEquals("/api/test", body.path());
        assertFalse(body.message().contains("violates foreign key constraint"));
        assertFalse(body.message().contains("fk_despesas_conta"));
        assertTrue(body.message().contains("vínculos ativos") || body.message().contains("integridade"));
    }

    @Test
    public void deveTratarResourceNotFoundExceptionComStatus404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Conta não encontrada: 999");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleResourceNotFound(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.status());
        assertEquals("Not Found", body.error());
        assertEquals("Conta não encontrada: 999", body.message());
        assertEquals("/api/test", body.path());
    }

    @Test
    public void deveTratarAccessDeniedExceptionComStatus403() {
        AccessDeniedException ex = new AccessDeniedException("Acesso negado: a conta informada não pertence ao usuário autenticado");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleAccessDenied(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(403, body.status());
        assertEquals("Forbidden", body.error());
        assertEquals("Acesso negado: a conta informada não pertence ao usuário autenticado", body.message());
        assertEquals("/api/test", body.path());
    }

    @Test
    public void deveTratarIllegalArgumentExceptionComStatus400() {
        IllegalArgumentException ex = new IllegalArgumentException("usuarioId não pode ser nulo");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleIllegalArgument(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
        assertEquals("usuarioId não pode ser nulo", body.message());
        assertEquals("/api/test", body.path());
    }

    @Test
    public void deveTratarExceptionGenericaComStatus500ESemExporStacktrace() {
        NullPointerException ex = new NullPointerException("Null reference at internal layer");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.status());
        assertEquals("Internal Server Error", body.error());
        assertFalse(body.message().contains("Null reference"));
        assertFalse(body.message().contains("NullPointerException"));
        assertEquals("Ocorreu um erro interno inesperado no servidor.", body.message());
        assertEquals("/api/test", body.path());
    }
}
