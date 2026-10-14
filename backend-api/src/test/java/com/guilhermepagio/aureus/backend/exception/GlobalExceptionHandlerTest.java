package com.guilhermepagio.aureus.backend.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
        bindingResult.addError(new ObjectError("testObject", "Constraint global de objeto violada"));

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
        HttpMessageNotReadableException ex =
            new HttpMessageNotReadableException("JSON parse error", (HttpInputMessage) null);

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
        MethodArgumentTypeMismatchException ex =
            new MethodArgumentTypeMismatchException("abc", Long.class, "id", null, null);

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleTypeMismatchAndMissingParam(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
        assertEquals("O parâmetro 'id' possui formato ou tipo inválido.", body.message());
        assertFalse(body.message().contains("java.lang"));
        assertEquals("/api/test", body.path());
    }

    @Test
    public void deveTratarMissingServletRequestParameterExceptionComStatus400() {
        MissingServletRequestParameterException ex =
            new MissingServletRequestParameterException("mesAno", "String");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleTypeMismatchAndMissingParam(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
        assertEquals("O parâmetro obrigatório 'mesAno' não foi informado.", body.message());
        assertEquals("/api/test", body.path());
    }

    @Test
    public void deveTratarConstraintViolationExceptionComStatus400() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("mesAno");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("Formato de data inválido. Use YYYY-MM");

        ConstraintViolationException ex =
            new ConstraintViolationException("Erro de validação", Set.of(violation));

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleConstraintViolation(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
        assertEquals("Erro de validação nos parâmetros informados", body.message());
        assertEquals("/api/test", body.path());
        assertNotNull(body.fieldErrors());
        assertEquals("Formato de data inválido. Use YYYY-MM", body.fieldErrors().get("mesAno"));
        assertNotNull(body.errors());
        assertEquals(1, body.errors().size());
        assertEquals("mesAno", body.errors().get(0).field());
    }

    @Test
    public void deveTratarNoResourceFoundExceptionComStatus404() {
        NoResourceFoundException ex =
            new NoResourceFoundException(HttpMethod.GET, "/api/contas/99999", "No static resource");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleNoResourceFound(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.status());
        assertEquals("Not Found", body.error());
        assertEquals("Recurso não encontrado: /api/test", body.message());
        assertEquals("/api/test", body.path());
    }

    @Test
    public void deveTratarHttpRequestMethodNotSupportedExceptionComStatus405EHeaderAllow() {
        HttpRequestMethodNotSupportedException ex =
            new HttpRequestMethodNotSupportedException("POST", List.of("GET", "OPTIONS"));

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleMethodNotSupported(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getHeaders().getAllow());
        assertTrue(response.getHeaders().getAllow().contains(HttpMethod.GET));
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(405, body.status());
        assertEquals("Method Not Allowed", body.error());
        assertEquals("Método HTTP não suportado para este endpoint.", body.message());
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
    public void deveTratarIllegalArgumentExceptionComMensagemNulaUsandoFallback() {
        IllegalArgumentException ex = new IllegalArgumentException();

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleIllegalArgument(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
        assertEquals("Argumento ou parâmetro inválido.", body.message());
        assertEquals("/api/test", body.path());
    }

    @Test
    public void deveExtrairNomeFolhaEmConstraintViolationComPrefixoDeMetodo() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("getPorConta.mesAno");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("Formato de data inválido. Use YYYY-MM");

        ConstraintViolationException ex =
            new ConstraintViolationException("Erro de validação", Set.of(violation));

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleConstraintViolation(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.fieldErrors());
        assertEquals("Formato de data inválido. Use YYYY-MM", body.fieldErrors().get("mesAno"));
        assertFalse(body.fieldErrors().containsKey("getPorConta.mesAno"));
        assertEquals("mesAno", body.errors().get(0).field());
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

    @Test
    public void deveTratarConstraintViolationExceptionComListaNulaSemLancarNpe() {
        ConstraintViolationException ex =
            new ConstraintViolationException("Violação", null);

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleConstraintViolation(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void deveTratarHandlerMethodValidationExceptionComStatus400EItensDeValidacao() {
        HandlerMethodValidationException ex =
            mock(HandlerMethodValidationException.class);
        ParameterValidationResult paramResult =
            mock(ParameterValidationResult.class);
        MethodParameter methodParam = mock(MethodParameter.class);
        when(methodParam.getParameterName()).thenReturn("mesAno");
        when(paramResult.getMethodParameter()).thenReturn(methodParam);

        MessageSourceResolvable resolvable = mock(MessageSourceResolvable.class);
        when(resolvable.getDefaultMessage()).thenReturn("Formato de data inválido. Use YYYY-MM");
        when(paramResult.getResolvableErrors()).thenReturn(List.of(resolvable));
        when(ex.getParameterValidationResults()).thenReturn(List.of(paramResult));

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleHandlerMethodValidation(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("mesAno", body.errors().get(0).field());
        assertEquals("Formato de data inválido. Use YYYY-MM", body.errors().get(0).defaultMessage());
        assertEquals("Formato de data inválido. Use YYYY-MM", body.fieldErrors().get("mesAno"));
    }

    @Test
    public void deveTratarDateTimeParseExceptionComStatus400() {
        DateTimeParseException ex =
            new DateTimeParseException("Text '2024-13' could not be parsed", "2024-13", 5);

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleIllegalArgument(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertTrue(body.message().contains("Formato de data inválido"));
    }

    @Test
    public void deveTratarHttpMediaTypeNotSupportedExceptionComStatus415() {
        HttpMediaTypeNotSupportedException ex =
            new HttpMediaTypeNotSupportedException(
                MediaType.TEXT_PLAIN,
                List.of(MediaType.APPLICATION_JSON)
            );

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleMediaTypeNotSupported(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(415, body.status());
        assertEquals("Unsupported Media Type", body.error());
        assertTrue(body.message().contains("text/plain"));
    }

    @Test
    public void deveTratarHttpMediaTypeNotAcceptableExceptionComStatus406() {
        HttpMediaTypeNotAcceptableException ex =
            new HttpMediaTypeNotAcceptableException("No acceptable representation");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleMediaTypeNotAcceptable(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(406, body.status());
        assertEquals("Not Acceptable", body.error());
    }
}
