const NEGATIVE_REGEX = /^\s*[-−–—]\s*R\$|^\s*R\$\s*[-−–—]|^\s*[-−–—]/;
const BRL_FORMATTER = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
});

export const formatCurrency = (value: string | number): string => {
  if (value === undefined || value === null || value === '') return '';
  
  let isNegative = false;
  let stringValue: string;
  if (typeof value === 'number') {
    if (!Number.isFinite(value)) return '';
    isNegative = value < 0;
    stringValue = Math.abs(value).toFixed(2).replace(/\D/g, '');
  } else if (typeof value === 'string') {
    isNegative = NEGATIVE_REGEX.test(value);
    stringValue = value.replace(/\D/g, '');
  } else {
    return '';
  }
  
  if (!stringValue) return '';
  
  let numberValue = (Number(stringValue) / 100) * (isNegative ? -1 : 1);
  if (numberValue === 0 || Object.is(numberValue, -0)) {
    numberValue = 0;
  }
  return BRL_FORMATTER.format(numberValue);
};

export const parseCurrency = (value: string): number => {
  if (!value) return 0;
  if (typeof value !== 'string') {
    return typeof value === 'number' && Number.isFinite(value) ? value : 0;
  }
  const isNegative = NEGATIVE_REGEX.test(value);
  const stringValue = value.replace(/\D/g, '');
  let numberValue = (Number(stringValue) / 100) * (isNegative ? -1 : 1);
  if (numberValue === 0 || Object.is(numberValue, -0)) {
    numberValue = 0;
  }
  return numberValue;
};
