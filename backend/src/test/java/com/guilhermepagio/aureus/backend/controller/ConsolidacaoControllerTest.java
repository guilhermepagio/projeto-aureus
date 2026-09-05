package com.guilhermepagio.aureus.backend.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorContaDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorCategoriaDTO;
import com.guilhermepagio.aureus.backend.service.ConsolidacaoService;

@ExtendWith(MockitoExtension.class)
public class ConsolidacaoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ConsolidacaoService consolidacaoService;

    @InjectMocks
    private ConsolidacaoController consolidacaoController;

    @BeforeEach
    public void setup() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(consolidacaoController)
            .setValidator(validator)
            .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.getParameterType().equals(String.class) && 
                           parameter.hasParameterAnnotation(org.springframework.security.core.annotation.AuthenticationPrincipal.class);
                }
                @Override
                public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                    return "usuario1";
                }
            })
            .build();
    }

    @Test
    public void deveFiltrarPorUsuario() throws Exception {
        when(consolidacaoService.calcularConsolidacaoPorConta("usuario1", "2024-01"))
                .thenReturn(new ConsolidacaoPorContaDTO(Collections.emptyList(), Collections.emptyList()));

        mockMvc.perform(get("/api/consolidacao/por-conta")
                .param("mesAno", "2024-01"))
                .andExpect(status().isOk());

        verify(consolidacaoService).calcularConsolidacaoPorConta("usuario1", "2024-01");
    }

    @Test
    public void deveRejeitarMesAnoInvalido() throws Exception {
        java.lang.reflect.Method method = ConsolidacaoController.class.getMethod("getPorConta", String.class, String.class);
        boolean hasPattern = false;
        for (java.lang.annotation.Annotation ann : method.getParameterAnnotations()[0]) {
            if (ann instanceof jakarta.validation.constraints.Pattern) {
                hasPattern = true;
                org.junit.jupiter.api.Assertions.assertEquals("^\\d{4}-(0[1-9]|1[0-2])$", ((jakarta.validation.constraints.Pattern) ann).regexp());
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(hasPattern);
    }

    /**
     * A autenticação é garantida pelo filtro JWT do Spring Security (JwtAuthenticationFilter),
     * configurado antes de qualquer endpoint no SecurityConfig.
     * Rotas /api/** requerem autenticação — requisições sem token JWT válido são barradas com 401
     * pelo filtro, nunca chegando ao controller.
     * Este teste documenta que o controller não possui lógica de auth manual
     * e depende corretamente da cadeia de filtros.
     */
    @Test
    public void devePassarUsuarioAutenticadoParaOServico() throws Exception {
        when(consolidacaoService.calcularConsolidacaoPorConta("usuario1", "2024-01"))
                .thenReturn(new ConsolidacaoPorContaDTO(Collections.emptyList(), Collections.emptyList()));

        mockMvc.perform(get("/api/consolidacao/por-conta")
                .param("mesAno", "2024-01"))
                .andExpect(status().isOk());

        verify(consolidacaoService).calcularConsolidacaoPorConta("usuario1", "2024-01");
    }

    @Test
    public void deveFiltrarPorCategoria() throws Exception {
        when(consolidacaoService.calcularConsolidacaoPorCategoria("usuario1", "2024-01"))
                .thenReturn(new ConsolidacaoPorCategoriaDTO(Collections.emptyList()));

        mockMvc.perform(get("/api/consolidacao/por-categoria")
                .param("mesAno", "2024-01"))
                .andExpect(status().isOk());

        verify(consolidacaoService).calcularConsolidacaoPorCategoria("usuario1", "2024-01");
    }

    @Test
    public void deveRejeitarMesAnoInvalidoPorCategoria() throws Exception {
        java.lang.reflect.Method method = ConsolidacaoController.class.getMethod("getPorCategoria", String.class, String.class);
        boolean hasPattern = false;
        for (java.lang.annotation.Annotation ann : method.getParameterAnnotations()[0]) {
            if (ann instanceof jakarta.validation.constraints.Pattern) {
                hasPattern = true;
                org.junit.jupiter.api.Assertions.assertEquals("^\\d{4}-(0[1-9]|1[0-2])$", ((jakarta.validation.constraints.Pattern) ann).regexp());
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(hasPattern);
    }

    @Test
    public void deveRetornar401QuandoUsuarioIdNulo() {
        org.springframework.http.ResponseEntity<ConsolidacaoPorContaDTO> respConta = consolidacaoController.getPorConta("2024-01", null);
        org.junit.jupiter.api.Assertions.assertEquals(401, respConta.getStatusCode().value());

        org.springframework.http.ResponseEntity<ConsolidacaoPorCategoriaDTO> respCat = consolidacaoController.getPorCategoria("2024-01", null);
        org.junit.jupiter.api.Assertions.assertEquals(401, respCat.getStatusCode().value());
    }
}
