import React from 'react';

export interface ObservacoesFieldProps {
  id?: string;
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
  maxLength?: number;
  className?: string;
  error?: string;
}

export const ObservacoesField: React.FC<ObservacoesFieldProps> = ({
  id = 'observacoes',
  value,
  onChange,
  disabled = false,
  maxLength = 300,
  className = '',
  error,
}) => {
  const errorId = error ? `${id}-error` : undefined;

  return (
    <div className={`flex flex-col min-h-0 ${className}`}>
      <label htmlFor={id} className="block text-sm font-medium text-gray-700">
        Observações
      </label>
      <textarea
        id={id}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        disabled={disabled}
        maxLength={maxLength}
        aria-invalid={Boolean(error)}
        aria-describedby={errorId}
        className="mt-1 block w-full flex-1 min-h-[160px] rounded-md border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm p-2 border disabled:opacity-50 disabled:bg-gray-100 resize-none overflow-y-auto"
      />
      {error && (
        <p id={errorId} className="mt-1 text-sm text-red-600" role="alert">
          {error}
        </p>
      )}
    </div>
  );
};

export default ObservacoesField;
