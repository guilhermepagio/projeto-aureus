import React, { useState, useRef, useEffect, useMemo } from 'react';
import { createPortal } from 'react-dom';
import { Calendar, ChevronDown, ChevronLeft, ChevronRight, X } from 'lucide-react';
import { MONTHS } from './MonthPicker';

const WEEKDAYS = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];

export interface DatePickerProps {
  id?: string;
  value: string; // 'YYYY-MM-DD' ou ''
  onChange: (value: string) => void;
  theme?: 'red' | 'green' | 'blue';
  disabled?: boolean;
  hasError?: boolean;
  placeholder?: string;
  className?: string;
}

export default function DatePicker({
  id,
  value,
  onChange,
  theme = 'red',
  disabled = false,
  hasError = false,
  placeholder = 'DD/MM/AAAA',
  className = '',
}: DatePickerProps) {
  const [isOpen, setIsOpen] = useState(false);
  const buttonRef = useRef<HTMLButtonElement>(null);
  const popoverRef = useRef<HTMLDivElement>(null);
  const [popoverCoords, setPopoverCoords] = useState<{ top: number; left: number } | null>(null);

  // Parse do valor YYYY-MM-DD ou fallback para a data de hoje
  const parsedValue = useMemo(() => {
    if (value && value.includes('-')) {
      const parts = value.split('-');
      if (parts.length === 3) {
        const y = parseInt(parts[0], 10);
        const m = parseInt(parts[1], 10) - 1;
        const d = parseInt(parts[2], 10);
        if (!isNaN(y) && !isNaN(m) && !isNaN(d) && m >= 0 && m <= 11 && d >= 1 && d <= 31) {
          return { year: y, month: m, day: d, isValid: true };
        }
      }
    }
    const now = new Date();
    return { year: now.getFullYear(), month: now.getMonth(), day: now.getDate(), isValid: false };
  }, [value]);

  const [viewYear, setViewYear] = useState(parsedValue.year);
  const [viewMonth, setViewMonth] = useState(parsedValue.month);

  // Sincroniza o mês e ano visualizados quando o valor mudar ou o popover for aberto
  useEffect(() => {
    setViewYear(parsedValue.year);
    setViewMonth(parsedValue.month);
  }, [parsedValue.year, parsedValue.month]);

  // Calcula posicionamento flutuante na tela (fora do overflow do modal)
  const updateCoords = () => {
    if (!buttonRef.current) return;
    const rect = buttonRef.current.getBoundingClientRect();
    const popoverHeight = 330;
    const popoverWidth = 280;

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

  const handlePrevMonth = () => {
    if (viewMonth === 0) {
      setViewMonth(11);
      setViewYear(y => y - 1);
    } else {
      setViewMonth(m => m - 1);
    }
  };

  const handleNextMonth = () => {
    if (viewMonth === 11) {
      setViewMonth(0);
      setViewYear(y => y + 1);
    } else {
      setViewMonth(m => m + 1);
    }
  };

  const handleSelectDay = (day: number) => {
    const formatted = `${viewYear}-${String(viewMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    onChange(formatted);
    setIsOpen(false);
  };

  const handleToday = () => {
    const now = new Date();
    const formatted = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
    onChange(formatted);
    setIsOpen(false);
  };

  const handleClear = (e?: React.MouseEvent) => {
    if (e) e.stopPropagation();
    onChange('');
    setIsOpen(false);
  };

  // Cálculo da grade de dias
  const calendarDays = useMemo(() => {
    const daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate();
    const startDayOfWeek = new Date(viewYear, viewMonth, 1).getDay(); // 0 = Domingo
    return {
      blanks: Array.from({ length: startDayOfWeek }),
      days: Array.from({ length: daysInMonth }, (_, i) => i + 1),
    };
  }, [viewYear, viewMonth]);

  const themeStyles = {
    red: {
      inputFocus: 'focus-within:ring-1 focus-within:ring-red-500 focus-within:border-red-500',
      icon: 'text-red-600',
      arrowHover: 'hover:bg-red-50 hover:text-red-600 focus-visible:ring-red-500',
      selectedDay: 'bg-red-600 text-white shadow-sm font-bold',
      todayBadge: 'bg-red-50 text-red-600 border border-red-200 font-bold hover:bg-red-100',
      dayHover: 'hover:bg-red-50 hover:text-red-600 focus-visible:ring-red-500',
      shortcut: 'text-red-600 hover:underline',
    },
    green: {
      inputFocus: 'focus-within:ring-1 focus-within:ring-emerald-600 focus-within:border-emerald-600',
      icon: 'text-emerald-600',
      arrowHover: 'hover:bg-emerald-50 hover:text-emerald-700 focus-visible:ring-emerald-600',
      selectedDay: 'bg-emerald-600 text-white shadow-sm font-bold',
      todayBadge: 'bg-emerald-50 text-emerald-700 border border-emerald-200 font-bold hover:bg-emerald-100',
      dayHover: 'hover:bg-emerald-50 hover:text-emerald-700 focus-visible:ring-emerald-600',
      shortcut: 'text-emerald-700 hover:underline',
    },
    blue: {
      inputFocus: 'focus-within:ring-1 focus-within:ring-blue-500 focus-within:border-blue-500',
      icon: 'text-blue-600',
      arrowHover: 'hover:bg-blue-50 hover:text-blue-600 focus-visible:ring-blue-500',
      selectedDay: 'bg-blue-600 text-white shadow-sm font-bold',
      todayBadge: 'bg-blue-50 text-blue-600 border border-blue-200 font-bold hover:bg-blue-100',
      dayHover: 'hover:bg-blue-50 hover:text-blue-600 focus-visible:ring-blue-500',
      shortcut: 'text-blue-600 hover:underline',
    },
  }[theme];

  const hasValidValue = parsedValue.isValid;
  const displayLabel = hasValidValue
    ? `${String(parsedValue.day).padStart(2, '0')}/${String(parsedValue.month + 1).padStart(2, '0')}/${parsedValue.year}`
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
            setViewYear(parsedValue.year);
            setViewMonth(parsedValue.month);
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
        <div className="flex items-center gap-2 min-w-0">
          <Calendar className={`w-4 h-4 shrink-0 ${themeStyles.icon}`} />
          <span className={`truncate ${hasValidValue ? 'text-gray-900 font-medium' : 'text-gray-400'}`}>
            {displayLabel || placeholder}
          </span>
        </div>
        <div className="flex items-center gap-1 shrink-0 ml-1">
          {hasValidValue && !disabled && (
            <span
              role="button"
              tabIndex={0}
              onClick={handleClear}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  handleClear();
                }
              }}
              title="Limpar data"
              aria-label="Limpar data"
              className="p-0.5 rounded-full hover:bg-gray-200 text-gray-400 hover:text-gray-600 transition-colors cursor-pointer"
            >
              <X className="w-3.5 h-3.5" />
            </span>
          )}
          <ChevronDown
            className={`w-4 h-4 text-gray-400 shrink-0 transition-transform duration-200 ${
              isOpen ? 'rotate-180' : ''
            }`}
          />
        </div>
      </button>

      {isOpen && popoverCoords && createPortal(
        <div
          ref={popoverRef}
          style={{
            position: 'fixed',
            top: `${popoverCoords.top}px`,
            left: `${popoverCoords.left}px`,
          }}
          className="z-[10000] bg-white border border-gray-200 rounded-xl shadow-2xl p-3 w-72 animate-in fade-in zoom-in-95 duration-100"
          role="dialog"
          aria-label="Seletor de Data"
        >
          {/* Navegação de Mês e Ano */}
          <div className="flex items-center justify-between mb-2.5 px-1">
            <button
              type="button"
              onClick={handlePrevMonth}
              className={`w-7 h-7 flex items-center justify-center rounded text-gray-600 transition-colors cursor-pointer focus:outline-none focus-visible:ring-2 ${themeStyles.arrowHover}`}
              aria-label="Mês anterior"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <div className="flex items-center gap-1">
              <span className="font-bold text-sm text-gray-900 tracking-tight">
                {MONTHS[viewMonth]} {viewYear}
              </span>
            </div>
            <button
              type="button"
              onClick={handleNextMonth}
              className={`w-7 h-7 flex items-center justify-center rounded text-gray-600 transition-colors cursor-pointer focus:outline-none focus-visible:ring-2 ${themeStyles.arrowHover}`}
              aria-label="Próximo mês"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>

          {/* Dias da Semana */}
          <div className="grid grid-cols-7 gap-1 mb-1 text-center">
            {WEEKDAYS.map(w => (
              <span key={w} className="text-[11px] font-semibold text-gray-400 py-0.5">
                {w}
              </span>
            ))}
          </div>

          {/* Grid de Dias */}
          <div className="grid grid-cols-7 gap-1 mb-2.5">
            {calendarDays.blanks.map((_, i) => (
              <span key={`blank-${i}`} className="w-8 h-8" />
            ))}
            {calendarDays.days.map(day => {
              const formattedCurrent = `${viewYear}-${String(viewMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
              const isSelected = value === formattedCurrent;
              const now = new Date();
              const isToday = now.getFullYear() === viewYear && now.getMonth() === viewMonth && now.getDate() === day;

              return (
                <button
                  key={day}
                  type="button"
                  onClick={() => handleSelectDay(day)}
                  className={`w-8 h-8 text-xs rounded-lg flex items-center justify-center transition-all cursor-pointer focus:outline-none focus-visible:ring-2 ${
                    isSelected
                      ? themeStyles.selectedDay
                      : isToday
                      ? themeStyles.todayBadge
                      : `text-gray-700 hover:bg-gray-100 ${themeStyles.dayHover}`
                  }`}
                >
                  {day}
                </button>
              );
            })}
          </div>

          {/* Rodapé de atalhos */}
          <div className="pt-2 border-t border-gray-100 flex justify-between items-center text-xs">
            <button
              type="button"
              onClick={handleToday}
              className={`font-semibold cursor-pointer ${themeStyles.shortcut}`}
            >
              Hoje
            </button>
            <div className="flex items-center gap-3">
              {hasValidValue && (
                <button
                  type="button"
                  onClick={() => handleClear()}
                  className="text-gray-500 hover:text-red-600 font-medium cursor-pointer transition-colors"
                >
                  Limpar
                </button>
              )}
              <button
                type="button"
                onClick={() => setIsOpen(false)}
                className="text-gray-500 hover:text-gray-700 cursor-pointer"
              >
                Fechar
              </button>
            </div>
          </div>
        </div>,
        document.body
      )}
    </div>
  );
}
