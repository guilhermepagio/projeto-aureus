import React, { useMemo } from 'react';
import FormField from './FormField';
import ValorInput from './ValorInput';
import MonthPicker from '../ui/MonthPicker';
import { calculateValorTotal, calculateUltimaParcela } from './parcelamentoUtils';

export interface ParcelamentoFieldsProps {
  valorParcela: string;
  onChangeValorParcela: (value: string) => void;
  quantidadeParcelas: number | '';
  onChangeQuantidadeParcelas: (value: number | '') => void;
  dataInicio: string;
  onChangeDataInicio: (value: string) => void;
  errors?: {
    valorParcela?: string;
    quantidadeParcelas?: string;
    dataInicio?: string;
  };
  disabled?: boolean;
  theme?: 'red' | 'green';
  idPrefix?: string;
}

export const ParcelamentoFields: React.FC<ParcelamentoFieldsProps> = ({
  valorParcela,
  onChangeValorParcela,
  quantidadeParcelas,
  onChangeQuantidadeParcelas,
  dataInicio,
  onChangeDataInicio,
  errors,
  disabled = false,
  theme = 'red',
  idPrefix,
}) => {
  const valorTotalPreview = useMemo(
    () => calculateValorTotal(valorParcela, quantidadeParcelas),
    [valorParcela, quantidadeParcelas]
  );

  const ultimaParcelaPreview = useMemo(
    () => calculateUltimaParcela(dataInicio, quantidadeParcelas),
    [dataInicio, quantidadeParcelas]
  );

  const valorParcelaId = idPrefix ? `${idPrefix}-valorParcela` : 'valorParcela';
  const qtdParcelasId = idPrefix ? `${idPrefix}-quantidadeParcelas` : 'quantidadeParcelas';
  const dataInicioId = idPrefix ? `${idPrefix}-dataInicio` : 'dataInicio';

  const isRed = theme === 'red';

  return (
    <>
      <div className="grid grid-cols-2 gap-4">
        <ValorInput
          id={valorParcelaId}
          label="Valor Parcela"
          value={valorParcela}
          onChange={onChangeValorParcela}
          error={errors?.valorParcela}
          disabled={disabled}
          placeholder="R$ 0,00"
        />

        <FormField
          id={qtdParcelasId}
          label="Qtd. Parcelas"
          required
          error={errors?.quantidadeParcelas}
        >
          {({ id, hasError, errorId, ariaRequired }) => (
            <input
              type="number"
              step="1"
              id={id}
              value={quantidadeParcelas}
              onChange={(e) =>
                onChangeQuantidadeParcelas(e.target.value ? Number(e.target.value) : '')
              }
              disabled={disabled}
              min={1}
              aria-required={ariaRequired}
              aria-invalid={hasError}
              aria-describedby={errorId}
              className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 ${
                hasError
                  ? 'border-red-500 focus:border-red-500'
                  : 'border-gray-300 focus:border-primary'
              }`}
            />
          )}
        </FormField>
      </div>

      <div
        className={`rounded-md p-3 border ${
          isRed ? 'bg-red-50 border-red-200' : 'bg-teal-50 border-teal-200'
        }`}
      >
        <p
          role="status"
          aria-live="polite"
          className={`text-sm font-medium ${isRed ? 'text-red-800' : 'text-teal-800'}`}
        >
          Valor Total: {valorTotalPreview}
        </p>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <FormField
          id={dataInicioId}
          label="Primeira Parcela"
          required
          error={errors?.dataInicio}
        >
          {({ id, hasError, errorId }) => (
            <MonthPicker
              id={id}
              value={dataInicio}
              onChange={onChangeDataInicio}
              disabled={disabled}
              hasError={hasError}
              ariaDescribedBy={errorId}
              theme={isRed ? 'red' : 'green'}
              placeholder="Selecione o mês"
            />
          )}
        </FormField>

        <div>
          <label
            id={`${idPrefix ? idPrefix + '-' : ''}ultima-parcela-label`}
            className="block text-sm font-medium text-gray-700"
          >
            Última Parcela
          </label>
          <div
            role="status"
            aria-live="polite"
            aria-labelledby={`${idPrefix ? idPrefix + '-' : ''}ultima-parcela-label`}
            className={`mt-1 flex items-center w-full rounded-md shadow-sm sm:text-sm px-3 py-2 border border-gray-300 bg-gray-50 min-h-[38px] ${
              ultimaParcelaPreview !== '-' ? 'text-gray-900 font-medium' : 'text-gray-400'
            }`}
          >
            {ultimaParcelaPreview}
          </div>
        </div>
      </div>
    </>
  );
};

export default ParcelamentoFields;
