import React from 'react';
import FormField from './FormField';
import { formatCurrency } from '../../utils/currencyFormat';

export interface ValorInputProps {
  id?: string;
  label?: string;
  value: string;
  onChange: (value: string) => void;
  error?: string;
  disabled?: boolean;
  required?: boolean;
  placeholder?: string;
  className?: string;
  autoFocus?: boolean;
}

export const ValorInput: React.FC<ValorInputProps> = ({
  id = 'valor',
  label = 'Valor',
  value,
  onChange,
  error,
  disabled = false,
  required = true,
  placeholder = 'R$ 0,00',
  className = '',
  autoFocus = false,
}) => {
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange(formatCurrency(e.target.value));
  };

  return (
    <FormField id={id} label={label} required={required} error={error} className={className}>
      {({ id: inputId, hasError, errorId }) => (
        <input
          type="text"
          id={inputId}
          autoFocus={autoFocus}
          value={value}
          onChange={handleChange}
          disabled={disabled}
          placeholder={placeholder}
          aria-invalid={hasError}
          aria-describedby={errorId}
          className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 ${
            hasError
              ? 'border-red-500 focus:border-red-500 focus:ring-red-500'
              : 'border-gray-300 focus:border-primary focus:ring-primary'
          }`}
        />
      )}
    </FormField>
  );
};

export default ValorInput;
