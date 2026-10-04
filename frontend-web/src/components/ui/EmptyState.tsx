import React from 'react';

interface EmptyStateProps {
  title: string;
  description: string;
  icon?: React.ReactNode;
  action?: {
    label: string;
    onClick: () => void;
    color?: 'primary' | 'danger' | 'blue';
  };
}

const EmptyState: React.FC<EmptyStateProps> = ({ title, description, icon, action }) => {
  const buttonColorClass = action?.color === 'danger' 
    ? 'bg-red-600 hover:bg-red-700 focus-visible:outline-red-600'
    : action?.color === 'blue'
    ? 'bg-blue-600 hover:bg-blue-700 focus-visible:outline-blue-600'
    : 'bg-primary hover:bg-primary-light focus-visible:outline-primary';

  return (
    <div className="max-w-lg mx-auto w-full mt-4">
      <div className="text-center py-12">
      {icon ? (
        <div className="mx-auto h-12 w-12 text-gray-400 flex items-center justify-center">
          {icon}
        </div>
      ) : (
        <svg
          className="mx-auto h-12 w-12 text-gray-400"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          aria-hidden="true"
        >
          <path
            vectorEffect="non-scaling-stroke"
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"
          />
        </svg>
      )}
      <h3 className="mt-2 text-sm font-semibold text-gray-900">{title}</h3>
      <p className="mt-1 text-sm text-gray-500">{description}</p>
      {action && (
        <div className="mt-6">
          <button
            onClick={action.onClick}
            type="button"
            className={`cursor-pointer h-8 px-4 inline-flex items-center justify-center rounded-md text-xs font-semibold text-white shadow-sm focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 ${buttonColorClass}`}
          >
            {action.label}
          </button>
        </div>
      )}
    </div>
    </div>
  );
};

export default EmptyState;
