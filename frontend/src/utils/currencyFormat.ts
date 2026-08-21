export const formatCurrency = (value: string | number): string => {
  if (value === undefined || value === null || value === '') return '';
  
  let stringValue;
  if (typeof value === 'number') {
    stringValue = value.toFixed(2).replace(/\D/g, '');
  } else {
    stringValue = value.replace(/\D/g, '');
  }
  
  if (!stringValue) return '';
  
  const numberValue = Number(stringValue) / 100;
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(numberValue);
};

export const parseCurrency = (value: string): number => {
  if (!value) return 0;
  const stringValue = value.replace(/\D/g, '');
  return Number(stringValue) / 100;
};
