import React, { useState } from 'react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import FormField from './FormField';
import ValorInput from './ValorInput';
import ContaCategoriaFields from './ContaCategoriaFields';
import ObservacoesField from './ObservacoesField';
import ParcelamentoFields from './ParcelamentoFields';
import {
  calculateValorTotal,
  calculateUltimaParcela,
} from './parcelamentoUtils';
import MovimentacaoFixaFormModal from './MovimentacaoFixaFormModal';
import MovimentacaoVariavelFormModal from './MovimentacaoVariavelFormModal';

import DespesaFixaFormModal from '../../pages/DespesasFixas/components/DespesaFixaFormModal';
import ReceitaFixaFormModal from '../../pages/ReceitasFixas/components/ReceitaFixaFormModal';
import DespesaVariavelFormModal from '../../pages/DespesasVariaveis/components/DespesaVariavelFormModal';
import ReceitaVariavelFormModal from '../../pages/ReceitasVariaveis/components/ReceitaVariavelFormModal';

vi.mock('../../hooks/useContas', () => ({
  useContas: () => ({
    data: [
      { id: 1, descricao: 'Nubank' },
      { id: 2, descricao: 'Itaú' },
    ],
    isLoading: false,
  }),
}));

vi.mock('../../hooks/useCategorias', () => ({
  useCategorias: () => ({
    data: [
      { id: 10, descricao: 'Alimentação' },
      { id: 20, descricao: 'Moradia' },
    ],
    isLoading: false,
  }),
}));

const mockMutateDespesaFixaCreate = vi.fn();
const mockMutateDespesaFixaUpdate = vi.fn();
vi.mock('../../hooks/useDespesasFixas', () => ({
  useCreateDespesaFixa: () => ({ mutate: mockMutateDespesaFixaCreate, isPending: false }),
  useUpdateDespesaFixa: () => ({ mutate: mockMutateDespesaFixaUpdate, isPending: false }),
}));

const mockMutateReceitaFixaCreate = vi.fn();
const mockMutateReceitaFixaUpdate = vi.fn();
vi.mock('../../hooks/useReceitasFixas', () => ({
  useCreateReceitaFixa: () => ({ mutate: mockMutateReceitaFixaCreate, isPending: false }),
  useUpdateReceitaFixa: () => ({ mutate: mockMutateReceitaFixaUpdate, isPending: false }),
}));

const mockMutateDespesaVariavelCreate = vi.fn();
const mockMutateDespesaVariavelUpdate = vi.fn();
vi.mock('../../hooks/useDespesasVariaveis', () => ({
  useCreateDespesaVariavel: () => ({ mutate: mockMutateDespesaVariavelCreate, isPending: false }),
  useUpdateDespesaVariavel: () => ({ mutate: mockMutateDespesaVariavelUpdate, isPending: false }),
}));

const mockMutateReceitaVariavelCreate = vi.fn();
const mockMutateReceitaVariavelUpdate = vi.fn();
vi.mock('../../hooks/useReceitasVariaveis', () => ({
  useCreateReceitaVariavel: () => ({ mutate: mockMutateReceitaVariavelCreate, isPending: false }),
  useUpdateReceitaVariavel: () => ({ mutate: mockMutateReceitaVariavelUpdate, isPending: false }),
}));

describe('MovimentacaoForm - Componentes Compartilhados', () => {
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  describe('FormField', () => {
    it('renderiza label com asterisco quando required=true', () => {
      render(
        <FormField id="test-field" label="Descrição" required>
          <input id="test-field" />
        </FormField>
      );

      const label = screen.getByText(/Descrição/);
      expect(label).toBeDefined();
      expect(label.textContent).toContain('*');
    });

    it('exibe mensagem de erro vinculada e acessível com role="alert"', () => {
      render(
        <FormField id="test-field" label="Nome" error="Campo obrigatório">
          {({ id, errorId, hasError }) => (
            <input
              id={id}
              aria-invalid={hasError}
              aria-describedby={errorId}
              data-testid="input-element"
            />
          )}
        </FormField>
      );

      const input = screen.getByTestId('input-element');
      expect(input.getAttribute('aria-invalid')).toBe('true');
      expect(input.getAttribute('aria-describedby')).toBe('test-field-error');

      const errorMsg = screen.getByRole('alert');
      expect(errorMsg.textContent).toBe('Campo obrigatório');
      expect(errorMsg.id).toBe('test-field-error');
    });
  });

  describe('ValorInput', () => {
    it('aplica formatação de moeda brasileira ao alterar valor', () => {
      const TestWrapper = () => {
        const [val, setVal] = useState('');
        return (
          <ValorInput
            id="valor-teste"
            label="Valor"
            value={val}
            onChange={setVal}
          />
        );
      };

      render(<TestWrapper />);

      const input = screen.getByRole('textbox', { name: /Valor/ }) as HTMLInputElement;
      fireEvent.change(input, { target: { value: '15000' } });

      expect(input.value).toContain('150,00');
    });

    it('exibe erro com aria-invalid e aria-describedby quando fornecido', () => {
      render(
        <ValorInput
          id="valor-erro"
          label="Valor"
          value=""
          onChange={vi.fn()}
          error="O valor deve ser maior que zero"
        />
      );

      const input = screen.getByRole('textbox', { name: /Valor/ });
      expect(input.getAttribute('aria-invalid')).toBe('true');
      expect(input.getAttribute('aria-describedby')).toBe('valor-erro-error');
      expect(screen.getByText('O valor deve ser maior que zero')).toBeDefined();
    });
  });

  describe('ContaCategoriaFields', () => {
    it('renderiza os seletores de Categoria e Conta carregados via hooks', () => {
      const onChangeConta = vi.fn();
      const onChangeCategoria = vi.fn();

      render(
        <ContaCategoriaFields
          contaId=""
          categoriaId=""
          onChangeConta={onChangeConta}
          onChangeCategoria={onChangeCategoria}
          layout="grid"
        />
      );

      const selectCategoria = screen.getByRole('combobox', { name: /Categoria/ });
      const selectConta = screen.getByRole('combobox', { name: /Conta/ });

      expect(selectCategoria).toBeDefined();
      expect(selectConta).toBeDefined();

      expect(screen.getByText('Alimentação')).toBeDefined();
      expect(screen.getByText('Nubank')).toBeDefined();

      fireEvent.change(selectCategoria, { target: { value: '10' } });
      expect(onChangeCategoria).toHaveBeenCalledWith(10);

      fireEvent.change(selectConta, { target: { value: '2' } });
      expect(onChangeConta).toHaveBeenCalledWith(2);
    });

    it('exibe erros para Categoria e Conta quando presentes', () => {
      render(
        <ContaCategoriaFields
          contaId=""
          categoriaId=""
          onChangeConta={vi.fn()}
          onChangeCategoria={vi.fn()}
          errors={{
            categoriaId: 'Selecione uma categoria',
            contaId: 'Selecione uma conta',
          }}
        />
      );

      expect(screen.getByText('Selecione uma categoria')).toBeDefined();
      expect(screen.getByText('Selecione uma conta')).toBeDefined();
    });
  });

  describe('ObservacoesField', () => {
    it('permite digitar texto e respeita maxLength padrão de 300 caracteres', () => {
      const onChange = vi.fn();
      render(
        <ObservacoesField
          id="obs"
          value="Nota fiscal recebida"
          onChange={onChange}
        />
      );

      const textarea = screen.getByRole('textbox', { name: /Observações/ }) as HTMLTextAreaElement;
      expect(textarea.value).toBe('Nota fiscal recebida');
      expect(textarea.maxLength).toBe(300);

      fireEvent.change(textarea, { target: { value: 'Nova anotação' } });
      expect(onChange).toHaveBeenCalledWith('Nova anotação');
    });
  });

  describe('ParcelamentoFields e Funções de Cálculo', () => {
    it('calcula valor total preview corretamente', () => {
      expect(calculateValorTotal('100,00', 3)).toContain('300,00');
      expect(calculateValorTotal('49,99', 2)).toContain('99,98');
      expect(calculateValorTotal('', 3)).toBe('-');
      expect(calculateValorTotal('100,00', '')).toBe('-');
      expect(calculateValorTotal('0,00', 5)).toBe('-');
    });

    it('calcula última parcela através de fronteiras de ano', () => {
      // Maio 2026 + 3 parcelas -> Maio(1), Junho(2), Julho(3) 2026
      expect(calculateUltimaParcela('2026-05', 3)).toBe('Jul 2026 (07/2026)');

      // Novembro 2026 + 4 parcelas -> Nov(1), Dez(2) 2026, Jan(3), Fev(4) 2027
      expect(calculateUltimaParcela('2026-11', 4)).toBe('Fev 2027 (02/2027)');

      expect(calculateUltimaParcela('', 3)).toBe('-');
      expect(calculateUltimaParcela('2026-01', '')).toBe('-');
    });

    it('validação defensiva de dataInicio em calculateUltimaParcela', () => {
      expect(calculateUltimaParcela('2026', 3)).toBe('-');
      expect(calculateUltimaParcela('invalid-date', 3)).toBe('-');
      expect(calculateUltimaParcela('2026-13', 3)).toBe('-');
      expect(calculateUltimaParcela('2026-00', 3)).toBe('-');
      expect(calculateUltimaParcela('2026-05', 0)).toBe('-');
      expect(calculateUltimaParcela('2026-05', -1)).toBe('-');
    });

    it('renderiza os campos de parcelamento com preview dinâmico', () => {
      render(
        <ParcelamentoFields
          valorParcela="150,00"
          onChangeValorParcela={vi.fn()}
          quantidadeParcelas={4}
          onChangeQuantidadeParcelas={vi.fn()}
          dataInicio="2026-05"
          onChangeDataInicio={vi.fn()}
          theme="red"
          idPrefix="despesa"
        />
      );

      expect(screen.getByText(/Valor Total:.*600,00/)).toBeDefined();
      expect(screen.getByText('Ago 2026 (08/2026)')).toBeDefined();
    });
  });

  describe('MovimentacaoFixaFormModal', () => {
    it('valida campos obrigatórios vazios ou zero ao submeter', () => {
      const handleSubmit = vi.fn();
      render(
        <MovimentacaoFixaFormModal
          isOpen={true}
          onClose={vi.fn()}
          tipo="despesa"
          onSubmit={handleSubmit}
          isPending={false}
        />
      );

      // Submete formulário vazio
      fireEvent.click(screen.getByRole('button', { name: 'Salvar' }));

      expect(handleSubmit).not.toHaveBeenCalled();
      expect(screen.getByText('A descrição é obrigatória')).toBeDefined();
      expect(screen.getByText('O valor deve ser maior que zero')).toBeDefined();
      expect(screen.getByText('Selecione uma conta')).toBeDefined();
      expect(screen.getByText('Selecione uma categoria')).toBeDefined();
    });

    it('limpa erro do campo individualmente ao alterar o valor', () => {
      render(
        <MovimentacaoFixaFormModal
          isOpen={true}
          onClose={vi.fn()}
          tipo="despesa"
          onSubmit={vi.fn()}
          isPending={false}
        />
      );

      fireEvent.click(screen.getByRole('button', { name: 'Salvar' }));
      expect(screen.getByText('A descrição é obrigatória')).toBeDefined();

      const input = screen.getByRole('textbox', { name: /Descrição/ });
      fireEvent.change(input, { target: { value: 'Internet' } });

      expect(screen.queryByText('A descrição é obrigatória')).toBeNull();
    });

    it('preenche campos existentes ao receber itemToEdit e submete com payload correto', () => {
      const handleSubmit = vi.fn();
      const handleClose = vi.fn();

      render(
        <MovimentacaoFixaFormModal
          isOpen={true}
          onClose={handleClose}
          tipo="receita"
          itemToEdit={{
            id: 42,
            descricao: 'Salário Mensal',
            valor: 5000,
            conta: { id: 1, descricao: 'Nubank' },
            categoria: { id: 10, descricao: 'Alimentação' },
            observacoes: 'Recebido todo dia 5',
          }}
          onSubmit={handleSubmit}
          isPending={false}
        />
      );

      expect(screen.getByRole('heading', { name: 'Editar Receita Fixa' })).toBeDefined();
      expect(screen.getByDisplayValue('Salário Mensal')).toBeDefined();
      expect(screen.getByDisplayValue(/5\.000,00/)).toBeDefined();
      expect(screen.getByDisplayValue('Recebido todo dia 5')).toBeDefined();

      // Submete formulário populado
      fireEvent.click(screen.getByRole('button', { name: 'Salvar' }));

      expect(handleSubmit).toHaveBeenCalledTimes(1);
      expect(handleSubmit.mock.calls[0][0]).toEqual({
        descricao: 'Salário Mensal',
        valor: 5000,
        conta: { id: 1 },
        categoria: { id: 10 },
        observacoes: 'Recebido todo dia 5',
      });
    });
  });

  describe('MovimentacaoVariavelFormModal', () => {
    it('renderiza campos condicionais de Local e Data da Compra para despesas', () => {
      render(
        <MovimentacaoVariavelFormModal
          isOpen={true}
          onClose={vi.fn()}
          tipo="despesa"
          hasLocalEDataCompra={true}
          onSubmit={vi.fn()}
          isPending={false}
        />
      );

      expect(screen.getByLabelText('Local da Compra')).toBeDefined();
      expect(screen.getByLabelText('Data da Compra')).toBeDefined();
    });

    it('não renderiza campos de Local e Data da Compra para receitas', () => {
      render(
        <MovimentacaoVariavelFormModal
          isOpen={true}
          onClose={vi.fn()}
          tipo="receita"
          hasLocalEDataCompra={false}
          onSubmit={vi.fn()}
          isPending={false}
        />
      );

      expect(screen.queryByLabelText('Local da Compra')).toBeNull();
      expect(screen.queryByLabelText('Data da Compra')).toBeNull();
    });

    it('valida campos obrigatórios da movimentação variável ao submeter', () => {
      const handleSubmit = vi.fn();
      render(
        <MovimentacaoVariavelFormModal
          isOpen={true}
          onClose={vi.fn()}
          tipo="despesa"
          onSubmit={handleSubmit}
          isPending={false}
        />
      );

      fireEvent.click(screen.getByRole('button', { name: 'Salvar' }));

      expect(handleSubmit).not.toHaveBeenCalled();
      expect(screen.getByText('A descrição é obrigatória')).toBeDefined();
      expect(screen.getByText('O valor da parcela deve ser maior que zero')).toBeDefined();
      expect(screen.getByText('Mínimo de 1 parcela')).toBeDefined();
      expect(screen.getByText('Data da 1ª parcela é obrigatória')).toBeDefined();
      expect(screen.getByText('Selecione uma conta')).toBeDefined();
      expect(screen.getByText('Selecione uma categoria')).toBeDefined();
    });

    it('limpa erro do campo individualmente ao alterar valor em movimentação variável', () => {
      render(
        <MovimentacaoVariavelFormModal
          isOpen={true}
          onClose={vi.fn()}
          tipo="despesa"
          onSubmit={vi.fn()}
          isPending={false}
        />
      );

      fireEvent.click(screen.getByRole('button', { name: 'Salvar' }));
      expect(screen.getByText('A descrição é obrigatória')).toBeDefined();

      const input = screen.getByRole('textbox', { name: /Descrição/ });
      fireEvent.change(input, { target: { value: 'Celular' } });

      expect(screen.queryByText('A descrição é obrigatória')).toBeNull();
    });

    it('submete dados de movimentação variável com parcelamento e datas formatadas', () => {
      const handleSubmit = vi.fn();

      render(
        <MovimentacaoVariavelFormModal
          isOpen={true}
          onClose={vi.fn()}
          tipo="despesa"
          itemToEdit={{
            id: 99,
            descricao: 'Notebook Novo',
            localCompra: 'Amazon',
            dataCompra: '2026-05-10',
            valorParcela: 400,
            quantidadeParcelas: 10,
            dataInicio: '2026-05-01',
            conta: { id: 2, descricao: 'Itaú' },
            categoria: { id: 20, descricao: 'Moradia' },
            observacoes: 'Parcelado sem juros',
          }}
          onSubmit={handleSubmit}
          isPending={false}
        />
      );

      fireEvent.click(screen.getByRole('button', { name: 'Salvar' }));

      expect(handleSubmit).toHaveBeenCalledTimes(1);
      expect(handleSubmit.mock.calls[0][0]).toEqual({
        descricao: 'Notebook Novo',
        localCompra: 'Amazon',
        dataCompra: '2026-05-10',
        valorParcela: 400,
        quantidadeParcelas: 10,
        dataInicio: '2026-05-01',
        conta: { id: 2 },
        categoria: { id: 20 },
        observacoes: 'Parcelado sem juros',
      });
    });
  });

  describe('Page Modal Wrappers', () => {
    it('renderiza DespesaFixaFormModal e delega criação com sucesso', () => {
      render(
        <DespesaFixaFormModal
          isOpen={true}
          onClose={vi.fn()}
          despesaToEdit={null}
        />
      );

      expect(screen.getByRole('heading', { name: 'Nova Despesa Fixa' })).toBeDefined();

      fireEvent.change(screen.getByRole('textbox', { name: /Descrição/ }), { target: { value: 'Condomínio' } });
      fireEvent.change(screen.getByRole('textbox', { name: /Valor/ }), { target: { value: '80000' } });
      fireEvent.change(screen.getByRole('combobox', { name: /Categoria/ }), { target: { value: '20' } });
      fireEvent.change(screen.getByRole('combobox', { name: /Conta/ }), { target: { value: '1' } });

      fireEvent.click(screen.getByRole('button', { name: 'Salvar' }));

      expect(mockMutateDespesaFixaCreate).toHaveBeenCalledWith(
        expect.objectContaining({
          descricao: 'Condomínio',
          valor: 800,
          categoria: { id: 20 },
          conta: { id: 1 },
        }),
        expect.any(Object)
      );
    });

    it('renderiza ReceitaFixaFormModal e delega edição com sucesso', () => {
      render(
        <ReceitaFixaFormModal
          isOpen={true}
          onClose={vi.fn()}
          receitaToEdit={{
            id: 15,
            descricao: 'Consultoria',
            valor: 3500,
            conta: { id: 1, descricao: 'Nubank' },
            categoria: { id: 10, descricao: 'Alimentação' },
            observacoes: 'Projeto X',
          }}
        />
      );

      expect(screen.getByRole('heading', { name: 'Editar Receita Fixa' })).toBeDefined();

      fireEvent.click(screen.getByRole('button', { name: 'Salvar' }));

      expect(mockMutateReceitaFixaUpdate).toHaveBeenCalledWith(
        expect.objectContaining({
          id: 15,
          descricao: 'Consultoria',
          valor: 3500,
        }),
        expect.any(Object)
      );
    });

    it('renderiza DespesaVariavelFormModal e delega criação com campos de compra', () => {
      render(
        <DespesaVariavelFormModal
          isOpen={true}
          onClose={vi.fn()}
          despesaToEdit={null}
        />
      );

      expect(screen.getByRole('heading', { name: 'Nova Despesa Variável' })).toBeDefined();
      expect(screen.getByLabelText('Local da Compra')).toBeDefined();
      expect(screen.getByLabelText('Data da Compra')).toBeDefined();
    });

    it('renderiza ReceitaVariavelFormModal sem campos de compra', () => {
      render(
        <ReceitaVariavelFormModal
          isOpen={true}
          onClose={vi.fn()}
          receitaToEdit={null}
        />
      );

      expect(screen.getByRole('heading', { name: 'Nova Receita Variável' })).toBeDefined();
      expect(screen.queryByLabelText('Local da Compra')).toBeNull();
      expect(screen.queryByLabelText('Data da Compra')).toBeNull();
    });
  });
});
