import { useState, useRef, useEffect } from 'react';
import { ChevronLeft, ChevronRight, RotateCcw, Calendar, ChevronDown } from 'lucide-react';
import { useMonthStore } from '../../store/monthStore';

const MONTHS = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

export interface ConsolidacaoToolbarProps {
  onResetScroll?: () => void;
  isAllCollapsed?: boolean;
  onToggleAll?: () => void;
}

export default function ConsolidacaoToolbar({
  onResetScroll,
  isAllCollapsed = false,
  onToggleAll,
}: ConsolidacaoToolbarProps = {}) {
  const { selectedMonth, setSelectedMonth } = useMonthStore();
  const [isPickerOpen, setIsPickerOpen] = useState(false);
  const popoverRef = useRef<HTMLDivElement>(null);

  if (!selectedMonth || !selectedMonth.includes('-')) return null;

  const [yearStr, monthStr] = selectedMonth.split('-');
  const year = parseInt(yearStr, 10);
  const monthIndex = parseInt(monthStr, 10) - 1;

  const [pickerYear, setPickerYear] = useState(year);

  // Sincroniza o ano interno do picker quando o ano selecionado mudar
  useEffect(() => {
    setPickerYear(year);
  }, [year]);

  // Fecha o popover ao clicar fora ou apertar Escape
  useEffect(() => {
    if (!isPickerOpen) return;

    const handleClickOutside = (event: MouseEvent) => {
      if (popoverRef.current && !popoverRef.current.contains(event.target as Node)) {
        setIsPickerOpen(false);
      }
    };

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setIsPickerOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('keydown', handleKeyDown);

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isPickerOpen]);

  const handlePrevMonth = () => {
    const newMonthIndex = monthIndex - 1;
    let newYear = year;
    let newMonth = newMonthIndex;
    if (newMonthIndex < 0) {
      newMonth = 11;
      newYear -= 1;
    }
    setSelectedMonth(`${newYear}-${String(newMonth + 1).padStart(2, '0')}`);
  };

  const handleNextMonth = () => {
    const newMonthIndex = monthIndex + 1;
    let newYear = year;
    let newMonth = newMonthIndex;
    if (newMonthIndex > 11) {
      newMonth = 0;
      newYear += 1;
    }
    setSelectedMonth(`${newYear}-${String(newMonth + 1).padStart(2, '0')}`);
  };

  const handleCurrentMonth = () => {
    const now = new Date();
    setSelectedMonth(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`);
    if (onResetScroll) {
      onResetScroll();
    } else {
      const container = document.getElementById('consolidacao-scroll-container');
      if (container) {
        if (typeof container.scrollTo === 'function') {
          container.scrollTo({ left: 0, behavior: 'smooth' });
        } else {
          container.scrollLeft = 0;
        }
      }
    }
  };

  return (
    <div className="flex items-center mb-4">
      <div className="w-[221px] shrink-0">
        <h1 className="text-2xl font-bold tracking-tight text-[#1A1A2E]">Consolidação</h1>
      </div>

      <div className="flex items-center gap-3 flex-wrap">
        {/* Controles de navegação de mês com Popover customizado */}
        <div className="relative flex items-center gap-1 bg-white border border-[#E5E7EB] rounded-lg p-1 shadow-sm">
          <button
            type="button"
            onClick={handlePrevMonth}
            className="w-7 h-7 flex items-center justify-center rounded text-[#6B7280] hover:text-blue-600 hover:bg-blue-50 transition-colors cursor-pointer"
            aria-label="Mês anterior"
          >
            <ChevronLeft className="w-4 h-4" />
          </button>

          {/* Botão para abrir o seletor personalizado */}
          <button
            type="button"
            onClick={() => {
              setPickerYear(year);
              setIsPickerOpen(prev => !prev);
            }}
            className="flex items-center gap-1.5 text-sm font-semibold text-[#1A1A2E] px-2 py-1 rounded hover:bg-blue-50 hover:text-blue-600 transition-colors cursor-pointer min-w-[105px] justify-center focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
            aria-label="Selecionar mês e ano"
            aria-expanded={isPickerOpen}
            aria-haspopup="dialog"
          >
            <Calendar className="w-3.5 h-3.5 text-blue-600 shrink-0" />
            <span aria-live="polite">
              {MONTHS[monthIndex]} {year}
            </span>
            <ChevronDown
              className={`w-3.5 h-3.5 text-[#6B7280] shrink-0 transition-transform duration-200 ${
                isPickerOpen ? 'rotate-180 text-blue-600' : ''
              }`}
            />
          </button>

          <button
            type="button"
            onClick={handleNextMonth}
            className="w-7 h-7 flex items-center justify-center rounded text-[#6B7280] hover:text-blue-600 hover:bg-blue-50 transition-colors cursor-pointer"
            aria-label="Próximo mês"
          >
            <ChevronRight className="w-4 h-4" />
          </button>

          {/* Popover flutuante do Seletor Personalizado */}
          {isPickerOpen && (
            <div
              ref={popoverRef}
              className="absolute top-full left-0 mt-1.5 z-50 bg-white border border-[#E5E7EB] rounded-xl shadow-xl p-3 w-64 animate-in fade-in zoom-in-95 duration-100"
              role="dialog"
              aria-label="Seletor de Mês e Ano"
            >
              {/* Navegação de Ano */}
              <div className="flex items-center justify-between mb-3 px-1">
                <button
                  type="button"
                  onClick={() => setPickerYear(prev => prev - 1)}
                  className="w-7 h-7 flex items-center justify-center rounded hover:bg-blue-50 hover:text-blue-600 text-[#4B5563] transition-colors cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
                  aria-label="Ano anterior"
                >
                  <ChevronLeft className="w-4 h-4" />
                </button>
                <span className="font-bold text-sm text-[#1A1A2E] tracking-tight">{pickerYear}</span>
                <button
                  type="button"
                  onClick={() => setPickerYear(prev => prev + 1)}
                  className="w-7 h-7 flex items-center justify-center rounded hover:bg-blue-50 hover:text-blue-600 text-[#4B5563] transition-colors cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
                  aria-label="Próximo ano"
                >
                  <ChevronRight className="w-4 h-4" />
                </button>
              </div>

              {/* Grid de 12 Meses */}
              <div className="grid grid-cols-3 gap-1.5 mb-2">
                {MONTHS.map((m, idx) => {
                  const isSelected = pickerYear === year && idx === monthIndex;
                  const now = new Date();
                  const isCurrentCalendarMonth = now.getFullYear() === pickerYear && now.getMonth() === idx;

                  return (
                    <button
                      key={m}
                      type="button"
                      onClick={() => {
                        const newMonthFormatted = `${pickerYear}-${String(idx + 1).padStart(2, '0')}`;
                        setSelectedMonth(newMonthFormatted);
                        setIsPickerOpen(false);
                      }}
                      className={`py-2 px-1 text-xs font-semibold rounded-lg transition-all cursor-pointer text-center focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 ${
                        isSelected
                          ? 'bg-blue-600 text-white shadow-sm'
                          : isCurrentCalendarMonth
                          ? 'bg-blue-50 text-blue-600 border border-blue-200 font-bold hover:bg-blue-100'
                          : 'text-[#374151] hover:bg-gray-100 hover:text-blue-600'
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
                  onClick={() => {
                    handleCurrentMonth();
                    setIsPickerOpen(false);
                  }}
                  className="text-blue-600 hover:underline font-semibold cursor-pointer"
                >
                  Ir para Mês Atual
                </button>
                <button
                  type="button"
                  onClick={() => setIsPickerOpen(false)}
                  className="text-gray-500 hover:text-gray-700 cursor-pointer"
                >
                  Fechar
                </button>
              </div>
            </div>
          )}
        </div>

        <button
          type="button"
          onClick={handleCurrentMonth}
          className="flex items-center gap-1.5 px-3.5 py-1.5 text-[13px] font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm transition-colors cursor-pointer"
        >
          <RotateCcw className="w-3.5 h-3.5" />
          <span>Mês Atual</span>
        </button>

        {onToggleAll && (
          <div className="flex items-center gap-2 pl-3 border-l border-slate-200">
            <button
              type="button"
              role="switch"
              aria-checked={isAllCollapsed}
              onClick={onToggleAll}
              aria-label="Ocultar tudo"
              className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-1 ${
                isAllCollapsed ? 'bg-blue-600' : 'bg-slate-300'
              }`}
            >
              <span className="sr-only">Ocultar tudo</span>
              <span
                aria-hidden="true"
                className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow-md ring-0 transition duration-200 ease-in-out ${
                  isAllCollapsed ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
            <span
              onClick={onToggleAll}
              className="text-[13px] font-medium text-slate-700 cursor-pointer select-none"
            >
              Ocultar tudo
            </span>
          </div>
        )}
      </div>
    </div>
  );
}
