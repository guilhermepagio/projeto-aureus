import React from 'react';

export interface FormFieldProps {
  id: string;
  label: string;
  required?: boolean;
  error?: string;
  className?: string;
  children:
    | React.ReactNode
    | ((helpers: {
        id: string;
        errorId?: string;
        hasError: boolean;
        required: boolean;
        ariaRequired?: boolean;
        'aria-required'?: boolean;
        'aria-invalid'?: boolean;
        'aria-describedby'?: string;
      }) => React.ReactNode);
}

export const FormField: React.FC<FormFieldProps> = ({
  id,
  label,
  required = false,
  error,
  className = '',
  children,
}) => {
  const errorId = error ? `${id}-error` : undefined;
  const hasError = Boolean(error);

  return (
    <div className={className}>
      <label htmlFor={id} className="block text-sm font-medium text-gray-700">
        {label} {required && '*'}
      </label>
      {typeof children === 'function'
        ? children({
            id,
            errorId,
            hasError,
            required,
            ariaRequired: required ? true : undefined,
            'aria-required': required ? true : undefined,
            'aria-invalid': hasError,
            'aria-describedby': errorId,
          })
        : children}
      {error && (
        <p id={errorId} className="mt-1 text-sm text-red-600" role="alert">
          {error}
        </p>
      )}
    </div>
  );
};

export default FormField;
