package com.guilhermepagio.aureus.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.DespesaFixa;
import com.guilhermepagio.aureus.backend.domain.DespesaVariavel;
import com.guilhermepagio.aureus.backend.domain.ReceitaFixa;
import com.guilhermepagio.aureus.backend.domain.ReceitaVariavel;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.IdReferenceDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaFixaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaVariavelRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaFixaRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaVariavelRepository;
import com.guilhermepagio.aureus.backend.security.TenantContext;

@ExtendWith(MockitoExtension.class)
public class TenantValidationTest {

    private static final String TENANT_CORRENTE = "usuario-autenticado-123";
    private static final String OUTRO_TENANT = "outro-usuario-456";

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private DespesaFixaRepository despesaFixaRepository;

    @Mock
    private ReceitaFixaRepository receitaFixaRepository;

    @Mock
    private DespesaVariavelRepository despesaVariavelRepository;

    @Mock
    private ReceitaVariavelRepository receitaVariavelRepository;

    @InjectMocks
    private DespesaFixaService despesaFixaService;

    @InjectMocks
    private ReceitaFixaService receitaFixaService;

    @InjectMocks
    private DespesaVariavelService despesaVariavelService;

    @InjectMocks
    private ReceitaVariavelService receitaVariavelService;

    @BeforeEach
    public void setUp() {
        TenantContext.setTenantId(TENANT_CORRENTE);
    }

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
    }

    // --- DespesaFixa Tenant Validation ---

    @Test
    public void despesaFixaDeveNegarAcessoQuandoContaPertenceAOutroTenant() {
        DespesaFixaRequestDTO dto = new DespesaFixaRequestDTO(
            "Aluguel", new BigDecimal("1200.00"),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> despesaFixaService.criar(dto));
        assertEquals("Acesso negado: a conta informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void despesaFixaDeveNegarAcessoQuandoCategoriaPertenceAOutroTenant() {
        DespesaFixaRequestDTO dto = new DespesaFixaRequestDTO(
            "Aluguel", new BigDecimal("1200.00"),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        Conta conta = new Conta(10L, "Nubank", "Obs");
        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(TENANT_CORRENTE));
        when(contaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findOwnerUsuarioId(20L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> despesaFixaService.criar(dto));
        assertEquals("Acesso negado: a categoria informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void despesaFixaDeveLancar404QuandoContaNaoExiste() {
        DespesaFixaRequestDTO dto = new DespesaFixaRequestDTO(
            "Aluguel", new BigDecimal("1200.00"),
            new IdReferenceDTO(999L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        when(contaRepository.findOwnerUsuarioId(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> despesaFixaService.criar(dto));
        assertEquals("Conta não encontrada: 999", ex.getMessage());
    }

    @Test
    public void despesaFixaDeveLancar404AoAtualizarIdInexistente() {
        DespesaFixaRequestDTO dto = new DespesaFixaRequestDTO(
            "Aluguel", new BigDecimal("1200.00"),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        when(despesaFixaRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> despesaFixaService.atualizar(999L, dto));
        assertEquals("Despesa fixa não encontrada: 999", ex.getMessage());
    }

    // --- ReceitaFixa Tenant Validation ---

    @Test
    public void receitaFixaDeveNegarAcessoQuandoContaPertenceAOutroTenant() {
        ReceitaFixaRequestDTO dto = new ReceitaFixaRequestDTO(
            "Salário", new BigDecimal("5000.00"),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> receitaFixaService.criar(dto));
        assertEquals("Acesso negado: a conta informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void receitaFixaDeveNegarAcessoQuandoCategoriaPertenceAOutroTenant() {
        ReceitaFixaRequestDTO dto = new ReceitaFixaRequestDTO(
            "Salário", new BigDecimal("5000.00"),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        Conta conta = new Conta(10L, "Nubank", "Obs");
        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(TENANT_CORRENTE));
        when(contaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findOwnerUsuarioId(20L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> receitaFixaService.criar(dto));
        assertEquals("Acesso negado: a categoria informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void receitaFixaDeveLancar404AoAtualizarIdInexistente() {
        ReceitaFixaRequestDTO dto = new ReceitaFixaRequestDTO(
            "Salário", new BigDecimal("5000.00"),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        when(receitaFixaRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> receitaFixaService.atualizar(999L, dto));
        assertEquals("Receita fixa não encontrada: 999", ex.getMessage());
    }

    // --- DespesaVariavel Tenant Validation ---

    @Test
    public void despesaVariavelDeveNegarAcessoQuandoContaPertenceAOutroTenant() {
        DespesaVariavelRequestDTO dto = new DespesaVariavelRequestDTO(
            "Celular", "Loja X", LocalDate.of(2024, 1, 1),
            new BigDecimal("200.00"), 10, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs"
        );

        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> despesaVariavelService.criar(dto));
        assertEquals("Acesso negado: a conta informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void despesaVariavelDeveNegarAcessoQuandoCategoriaPertenceAOutroTenant() {
        DespesaVariavelRequestDTO dto = new DespesaVariavelRequestDTO(
            "Celular", "Loja X", LocalDate.of(2024, 1, 1),
            new BigDecimal("200.00"), 10, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs"
        );

        Conta conta = new Conta(10L, "Nubank", "Obs");
        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(TENANT_CORRENTE));
        when(contaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findOwnerUsuarioId(20L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> despesaVariavelService.criar(dto));
        assertEquals("Acesso negado: a categoria informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void despesaVariavelDeveLancar404AoAtualizarIdInexistente() {
        DespesaVariavelRequestDTO dto = new DespesaVariavelRequestDTO(
            "Celular", "Loja X", LocalDate.of(2024, 1, 1),
            new BigDecimal("200.00"), 10, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs"
        );

        when(despesaVariavelRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> despesaVariavelService.atualizar(999L, dto));
        assertEquals("Despesa variável não encontrada: 999", ex.getMessage());
    }

    // --- ReceitaVariavel Tenant Validation ---

    @Test
    public void receitaVariavelDeveNegarAcessoQuandoContaPertenceAOutroTenant() {
        ReceitaVariavelRequestDTO dto = new ReceitaVariavelRequestDTO(
            "Freelance", new BigDecimal("1000.00"), 2, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs"
        );

        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> receitaVariavelService.criar(dto));
        assertEquals("Acesso negado: a conta informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void receitaVariavelDeveNegarAcessoQuandoCategoriaPertenceAOutroTenant() {
        ReceitaVariavelRequestDTO dto = new ReceitaVariavelRequestDTO(
            "Freelance", new BigDecimal("1000.00"), 2, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs"
        );

        Conta conta = new Conta(10L, "Nubank", "Obs");
        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(TENANT_CORRENTE));
        when(contaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findOwnerUsuarioId(20L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> receitaVariavelService.criar(dto));
        assertEquals("Acesso negado: a categoria informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void despesaFixaDeveNegarAcessoQuandoContaPertenceAOutroTenantAoAtualizar() {
        DespesaFixa existente = new DespesaFixa();
        existente.setId(100L);
        when(despesaFixaRepository.findById(100L)).thenReturn(Optional.of(existente));

        DespesaFixaRequestDTO dto = new DespesaFixaRequestDTO(
            "Aluguel", new BigDecimal("1200.00"),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> despesaFixaService.atualizar(100L, dto));
        assertEquals("Acesso negado: a conta informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void despesaFixaDeveNegarAcessoQuandoCategoriaPertenceAOutroTenantAoAtualizar() {
        DespesaFixa existente = new DespesaFixa();
        existente.setId(100L);
        when(despesaFixaRepository.findById(100L)).thenReturn(Optional.of(existente));

        DespesaFixaRequestDTO dto = new DespesaFixaRequestDTO(
            "Aluguel", new BigDecimal("1200.00"),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        Conta conta = new Conta(10L, "Nubank", "Obs");
        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(TENANT_CORRENTE));
        when(contaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findOwnerUsuarioId(20L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> despesaFixaService.atualizar(100L, dto));
        assertEquals("Acesso negado: a categoria informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void despesaFixaDeveFalharFechadoQuandoTenantContextForNulo() {
        TenantContext.clear();

        DespesaFixaRequestDTO dto = new DespesaFixaRequestDTO(
            "Aluguel", new BigDecimal("1200.00"),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(TENANT_CORRENTE));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> despesaFixaService.criar(dto));
        assertEquals("Acesso negado: a conta informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void receitaFixaDeveNegarAcessoQuandoContaPertenceAOutroTenantAoAtualizar() {
        ReceitaFixa existente = new ReceitaFixa();
        existente.setId(100L);
        when(receitaFixaRepository.findById(100L)).thenReturn(Optional.of(existente));

        ReceitaFixaRequestDTO dto = new ReceitaFixaRequestDTO(
            "Salário", new BigDecimal("5000.00"),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> receitaFixaService.atualizar(100L, dto));
        assertEquals("Acesso negado: a conta informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void receitaFixaDeveNegarAcessoQuandoCategoriaPertenceAOutroTenantAoAtualizar() {
        ReceitaFixa existente = new ReceitaFixa();
        existente.setId(100L);
        when(receitaFixaRepository.findById(100L)).thenReturn(Optional.of(existente));

        ReceitaFixaRequestDTO dto = new ReceitaFixaRequestDTO(
            "Salário", new BigDecimal("5000.00"),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L),
            "Obs", LocalDate.of(2024, 1, 1)
        );

        Conta conta = new Conta(10L, "Nubank", "Obs");
        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(TENANT_CORRENTE));
        when(contaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findOwnerUsuarioId(20L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> receitaFixaService.atualizar(100L, dto));
        assertEquals("Acesso negado: a categoria informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void despesaVariavelDeveNegarAcessoQuandoContaPertenceAOutroTenantAoAtualizar() {
        DespesaVariavel existente = new DespesaVariavel();
        existente.setId(100L);
        when(despesaVariavelRepository.findById(100L)).thenReturn(Optional.of(existente));

        DespesaVariavelRequestDTO dto = new DespesaVariavelRequestDTO(
            "Celular", "Loja X", LocalDate.of(2024, 1, 1),
            new BigDecimal("200.00"), 10, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs"
        );

        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> despesaVariavelService.atualizar(100L, dto));
        assertEquals("Acesso negado: a conta informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void despesaVariavelDeveNegarAcessoQuandoCategoriaPertenceAOutroTenantAoAtualizar() {
        DespesaVariavel existente = new DespesaVariavel();
        existente.setId(100L);
        when(despesaVariavelRepository.findById(100L)).thenReturn(Optional.of(existente));

        DespesaVariavelRequestDTO dto = new DespesaVariavelRequestDTO(
            "Celular", "Loja X", LocalDate.of(2024, 1, 1),
            new BigDecimal("200.00"), 10, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs"
        );

        Conta conta = new Conta(10L, "Nubank", "Obs");
        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(TENANT_CORRENTE));
        when(contaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findOwnerUsuarioId(20L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> despesaVariavelService.atualizar(100L, dto));
        assertEquals("Acesso negado: a categoria informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void receitaVariavelDeveNegarAcessoQuandoContaPertenceAOutroTenantAoAtualizar() {
        ReceitaVariavel existente = new ReceitaVariavel();
        existente.setId(100L);
        when(receitaVariavelRepository.findById(100L)).thenReturn(Optional.of(existente));

        ReceitaVariavelRequestDTO dto = new ReceitaVariavelRequestDTO(
            "Freelance", new BigDecimal("1000.00"), 2, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs"
        );

        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> receitaVariavelService.atualizar(100L, dto));
        assertEquals("Acesso negado: a conta informada não pertence ao usuário autenticado", ex.getMessage());
    }

    @Test
    public void receitaVariavelDeveNegarAcessoQuandoCategoriaPertenceAOutroTenantAoAtualizar() {
        ReceitaVariavel existente = new ReceitaVariavel();
        existente.setId(100L);
        when(receitaVariavelRepository.findById(100L)).thenReturn(Optional.of(existente));

        ReceitaVariavelRequestDTO dto = new ReceitaVariavelRequestDTO(
            "Freelance", new BigDecimal("1000.00"), 2, LocalDate.of(2024, 1, 1),
            new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs"
        );

        Conta conta = new Conta(10L, "Nubank", "Obs");
        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of(TENANT_CORRENTE));
        when(contaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findOwnerUsuarioId(20L)).thenReturn(Optional.of(OUTRO_TENANT));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> receitaVariavelService.atualizar(100L, dto));
        assertEquals("Acesso negado: a categoria informada não pertence ao usuário autenticado", ex.getMessage());
    }
}
