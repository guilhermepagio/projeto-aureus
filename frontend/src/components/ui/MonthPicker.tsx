import React, { useState, useRef, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { Calendar, ChevronDown, ChevronLeft, ChevronRight } from 'lucide-react';

export const MONTHS = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

export interface MonthPickerProps {
  id?: string;
  value: string; // 'YYYY-MM' ou ''
  onChange: (value: string) => void;
  theme?: 'red' | 'green' | 'blue';
  disabled?: boolean;
  hasError?: boolean;
  placeholder?: string;
  className?: string;
}

export default function MonthPicker({
  id,
  value,
  onChange,
  theme = 'blue',
  disabled = false,
  hasError = false,
  placeholder = 'MM/AAAA',
  className = '',
}: MonthPickerProps) {
  const [isOpen, setIsOpen] = useState(false);
  const buttonRef = useRef<HTMLButtonElement>(null);
  const popoverRef = useRef<HTMLDivElement>(null);
  const [popoverCoords, setPopoverCoords] = useState<{ top: number; left: number } | null>(null);

  // Determina ano e mês base a partir do value (YYYY-MM) ou da data atual
  const parsed = React.useMemo(() => {
    if (value && value.includes('-')) {
      const [y, m] = value.split('-');
      const yearNum = parseInt(y, 10);
      const monthIdx = parseInt(m, 10) - 1;
      if (!isNaN(yearNum) && !isNaN(monthIdx) && monthIdx >= 0 && monthIdx <= 11) {
        return { year: yearNum, monthIndex: monthIdx, isValid: true };
      }
    }
    const now = new Date();
    return { year: now.getFullYear(), monthIndex: now.getMonth(), isValid: false };
  }, [value]);

  const [pickerYear, setPickerYear] = useState(parsed.year);

  // Atualiza pickerYear quando parsed.year mudar
  useEffect(() => {
    setPickerYear(parsed.year);
  }, [parsed.year]);

  // Calcula posicionamento flutuante na tela (fora do overflow do modal)
  const updateCoords = () => {
    if (!buttonRef.current) return;
    const rect = buttonRef.current.getBoundingClientRect();
    const popoverHeight = 280;
    const popoverWidth = 256;

    const spaceBelow = window.innerHeight - rect.bottom;
    const openUpwards = spaceBelow < popoverHeight && rect.top > popoverHeight;

    let left = rect.left;
    if (left + popoverWidth > window.innerWidth - 16) {
      left = window.innerWidth - popoverWidth - 16;
    }
    if (left < 16) left = 16;

    const top = openUpwards ? Math.max(8, rect.top - popoverHeight - 6) : rect.bottom + 6;

    setPopoverCoords({ top, left });
  };

  useEffect(() => {
    if (!isOpen) return;

    updateCoords();

    const handleScrollOrResize = () => {
      updateCoords();
    };

    window.addEventListener('resize', handleScrollOrResize);
    window.addEventListener('scroll', handleScrollOrResize, true);

    return () => {
      window.removeEventListener('resize', handleScrollOrResize);
      window.removeEventListener('scroll', handleScrollOrResize, true);
    };
  }, [isOpen]);

  // Click outside e tecla Escape
  useEffect(() => {
    if (!isOpen) return;

    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as Node;
      if (
        popoverRef.current &&
        !popoverRef.current.contains(target) &&
        buttonRef.current &&
        !buttonRef.current.contains(target)
      ) {
        setIsOpen(false);
      }
    };

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('keydown', handleKeyDown);

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen]);

  const handleSelect = (y: number, mIdx: number) => {
    const formatted = `${y}-${String(mIdx + 1).padStart(2, '0')}`;
    onChange(formatted);
    setIsOpen(false);
  };

  const handleCurrentMonth = () => {
    const now = new Date();
    const formatted = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    onChange(formatted);
    setIsOpen(false);
  };

  // Definições de estilo de acordo com a aba/tema
  const themeStyles = {
    red: {
      inputFocus: 'focus-within:ring-1 focus-within:ring-red-500 focus-within:border-red-500',
      icon: 'text-red-600',
      arrowHover: 'hover:bg-red-50 hover:text-red-600 focus-visible:ring-red-500',
      selectedMonth: 'bg-red-600 text-white shadow-sm',
      currentCalendarMonth: 'bg-red-50 text-red-600 border border-red-200 font-bold hover:bg-red-100',
      unselectedMonthHover: 'hover:bg-red-50 hover:text-red-600 focus-visible:ring-red-500',
      shortcut: 'text-red-600 hover:underline',
    },
    green: {
      inputFocus: 'focus-within:ring-1 focus-within:ring-emerald-600 focus-within:border-emerald-600',
      icon: 'text-emerald-600',
      arrowHover: 'hover:bg-emerald-50 hover:text-emerald-700 focus-visible:ring-emerald-600',
      selectedMonth: 'bg-emerald-600 text-white shadow-sm',
      currentCalendarMonth: 'bg-emerald-50 text-emerald-700 border border-emerald-200 font-bold hover:bg-emerald-100',
      unselectedMonthHover: 'hover:bg-emerald-50 hover:text-emerald-700 focus-visible:ring-emerald-600',
      shortcut: 'text-emerald-700 hover:underline',
    },
    blue: {
      inputFocus: 'focus-within:ring-1 focus-within:ring-blue-500 focus-within:border-blue-500',
      icon: 'text-blue-600',
      arrowHover: 'hover:bg-blue-50 hover:text-blue-600 focus-visible:ring-blue-500',
      selectedMonth: 'bg-blue-600 text-white shadow-sm',
      currentCalendarMonth: 'bg-blue-50 text-blue-600 border border-blue-200 font-bold hover:bg-blue-100',
      unselectedMonthHover: 'hover:bg-blue-50 hover:text-blue-600 focus-visible:ring-blue-500',
      shortcut: 'text-blue-600 hover:underline',
    },
  }[theme];

  const hasValidValue = parsed.isValid;
  const monthNumFormatted = String(parsed.monthIndex + 1).padStart(2, '0');
  const displayLabel = hasValidValue
    ? `${MONTHS[parsed.monthIndex]} ${parsed.year} (${monthNumFormatted}/${parsed.year})`
    : '';

  return (
    <div className={`relative ${className}`}>
      <button
        ref={buttonRef}
        type="button"
        id={id}
        disabled={disabled}
        onClick={() => {
          if (!disabled) {
            setPickerYear(parsed.year);
            setIsOpen(prev => !prev);
          }
        }}
        aria-haspopup="dialog"
        aria-expanded={isOpen}
        aria-label={displayLabel || placeholder}
        className={`mt-1 relative w-full flex items-center justify-between rounded-md shadow-sm sm:text-sm px-3 py-2 border text-left cursor-pointer transition-colors min-h-[38px] disabled:opacity-50 disabled:bg-gray-100 disabled:cursor-not-allowed ${
          hasError ? 'border-red-500' : 'border-gray-300'
        } ${themeStyles.inputFocus} ${disabled ? 'bg-gray-100' : 'bg-white'}`}
      >
        <div className="flex items-center gap-2">
          <Calendar className={`w-4 h-4 shrink-0 ${themeStyles.icon}`} />
          <span className={hasValidValue ? 'text-gray-900 font-medium' : 'text-gray-400'}>
            {displayLabel || placeholder}
          </span>
        </div>
        <ChevronDown
          className={`w-4 h-4 text-gray-400 shrink-0 transition-transform duration-200 ${
            isOpen ? 'rotate-180' : ''
          }`}
        />
      </button>

      {isOpen && popoverCoords && createPortal(
        <div
          ref={popoverRef}
          style={{
            position: 'fixed',
            top: `${popoverCoords.top}px`,
            left: `${popoverCoords.left}px`,
          }}
          className="z-[10000] bg-white border border-gray-200 rounded-xl shadow-2xl p-3 w-64 animate-in fade-in zoom-in-95 duration-100"
          role="dialog"
          aria-label="Seletor de Mês e Ano"
        >
          {/* Navegação de Ano */}
          <div className="flex items-center justify-between mb-3 px-1">
            <button
              type="button"
              onClick={() => setPickerYear(prev => prev - 1)}
              className={`w-7 h-7 flex items-center justify-center rounded text-gray-600 transition-colors cursor-pointer focus:outline-none focus-visible:ring-2 ${themeStyles.arrowHover}`}
              aria-label="Ano anterior"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="font-bold text-sm text-gray-900 tracking-tight">{pickerYear}</span>
            <button
              type="button"
              onClick={() => setPickerYear(prev => prev + 1)}
              className={`w-7 h-7 flex items-center justify-center rounded text-gray-600 transition-colors cursor-pointer focus:outline-none focus-visible:ring-2 ${themeStyles.arrowHover}`}
              aria-label="Próximo ano"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>

          {/* Grid de 12 Meses */}
          <div className="grid grid-cols-3 gap-1.5 mb-2">
            {MONTHS.map((m, idx) => {
              const isSelected = hasValidValue && pickerYear === parsed.year && idx === parsed.monthIndex;
              const now = new Date();
              const isCurrentCalendarMonth = now.getFullYear() === pickerYear && now.getMonth() === idx;

              return (
                <button
                  key={m}
                  type="button"
                  onClick={() => handleSelect(pickerYear, idx)}
                  className={`py-2 px-1 text-xs font-semibold rounded-lg transition-all cursor-pointer text-center focus:outline-none focus-visible:ring-2 ${
                    isSelected
                      ? themeStyles.selectedMonth
                      : isCurrentCalendarMonth
                      ? themeStyles.currentCalendarMonth
                      : `text-gray-700 hover:bg-gray-100 ${themeStyles.unselectedMonthHover}`
                  }`}
                >
                  {m}
                </button>
              );
            })}
          </div>

          {/* Rodapé de atalhos */}
          <div className="pt-2 border-t border-gray-100 flex justify-between items-center text-xs">
            <button
              type="button"
              onClick={handleCurrentMonth}
              className={`font-semibold cursor-pointer ${themeStyles.shortcut}`}
            >
              Ir para Mês Atual
            </button>
            <button
              type="button"
              onClick={() => setIsOpen(false)}
              className="text-gray-500 hover:text-gray-700 cursor-pointer"
            >
              Fechar
            </button>
          </div>
        </div>,
        document.body
      )}
    </div>
  );
}
