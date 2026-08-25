package com.guilhermepagio.aureus.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorContaDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ConsolidacaoPorCategoriaDTO;
import com.guilhermepagio.aureus.backend.service.ConsolidacaoService;

import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/consolidacao")
@RequiredArgsConstructor
@Validated
public class ConsolidacaoController {

    private final ConsolidacaoService consolidacaoService;

    @GetMapping("/por-conta")
    public ResponseEntity<ConsolidacaoPorContaDTO> getPorConta(
            @RequestParam @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Formato de data inválido. Use YYYY-MM") String mesAno,
            @AuthenticationPrincipal String usuarioId) {
        
        if (usuarioId == null) {
            return ResponseEntity.status(401).build();
        }

        ConsolidacaoPorContaDTO dto = consolidacaoService.calcularConsolidacaoPorConta(usuarioId, mesAno);
        return ResponseEntity.ok(dto);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(jakarta.validation.ConstraintViolationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @GetMapping("/por-categoria")
    public ResponseEntity<ConsolidacaoPorCategoriaDTO> getPorCategoria(
            @AuthenticationPrincipal org.springframework.security.oauth2.core.user.OAuth2User principal,
            @RequestParam("mesAno") String mesAno) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String usuarioId = principal.getAttribute("sub");
        
        ConsolidacaoPorCategoriaDTO dto = consolidacaoService.calcularConsolidacaoPorCategoria(usuarioId, mesAno);
        return ResponseEntity.ok(dto);
    }
}
