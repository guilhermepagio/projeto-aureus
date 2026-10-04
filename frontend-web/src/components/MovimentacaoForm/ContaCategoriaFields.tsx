import React from 'react';
import FormField from './FormField';
import { useContas } from '../../hooks/useContas';
import { useCategorias } from '../../hooks/useCategorias';

export interface ContaCategoriaFieldsProps {
  contaId: number | '';
  categoriaId: number | '';
  onChangeConta: (id: number | '') => void;
  onChangeCategoria: (id: number | '') => void;
  errors?: {
    contaId?: string;
    categoriaId?: string;
  };
  disabled?: boolean;
  layout?: 'grid' | 'stacked';
  idPrefix?: string;
}

export const ContaCategoriaFields: React.FC<ContaCategoriaFieldsProps> = ({
  contaId,
  categoriaId,
  onChangeConta,
  onChangeCategoria,
  errors,
  disabled = false,
  layout = 'stacked',
  idPrefix,
}) => {
  const { data: contas } = useContas();
  const { data: categorias } = useCategorias();

  const categoriaInputId = idPrefix ? `${idPrefix}-categoriaId` : 'categoriaId';
  const contaInputId = idPrefix ? `${idPrefix}-contaId` : 'contaId';

  const categoriaField = (
    <FormField
      id={categoriaInputId}
      label="Categoria"
      required
      error={errors?.categoriaId}
    >
      {({ id, hasError, errorId }) => (
        <select
          id={id}
          value={categoriaId}
          onChange={(e) => onChangeCategoria(e.target.value ? Number(e.target.value) : '')}
          disabled={disabled}
          aria-required="true"
          aria-invalid={hasError}
          aria-describedby={errorId}
          className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 ${
            hasError
              ? 'border-red-500 focus:border-red-500 focus:ring-red-500'
              : 'border-gray-300 focus:border-primary focus:ring-primary'
          }`}
        >
          <option value="">Selecione...</option>
          {categorias?.map((c) => (
            <option key={c.id} value={c.id}>
              {c.descricao}
            </option>
          ))}
        </select>
      )}
    </FormField>
  );

  const contaField = (
    <FormField
      id={contaInputId}
      label="Conta"
      required
      error={errors?.contaId}
    >
      {({ id, hasError, errorId }) => (
        <select
          id={id}
          value={contaId}
          onChange={(e) => onChangeConta(e.target.value ? Number(e.target.value) : '')}
          disabled={disabled}
          aria-required="true"
          aria-invalid={hasError}
          aria-describedby={errorId}
          className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 ${
            hasError
              ? 'border-red-500 focus:border-red-500 focus:ring-red-500'
              : 'border-gray-300 focus:border-primary focus:ring-primary'
          }`}
        >
          <option value="">Selecione...</option>
          {contas?.map((c) => (
            <option key={c.id} value={c.id}>
              {c.descricao}
            </option>
          ))}
        </select>
      )}
    </FormField>
  );

  if (layout === 'grid') {
    return (
      <div className="grid grid-cols-2 gap-4">
        {categoriaField}
        {contaField}
      </div>
    );
  }

  return (
    <>
      {categoriaField}
      {contaField}
    </>
  );
};

export default ContaCategoriaFields;
