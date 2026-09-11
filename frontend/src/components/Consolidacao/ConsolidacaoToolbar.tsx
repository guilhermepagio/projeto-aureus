import { ChevronLeft, ChevronRight, RotateCcw } from 'lucide-react';
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

  if (!selectedMonth || !selectedMonth.includes('-')) return null;

  const [yearStr, monthStr] = selectedMonth.split('-');
  const year = parseInt(yearStr, 10);
  const monthIndex = parseInt(monthStr, 10) - 1;

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
        <div className="flex items-center gap-1 bg-white border border-[#E5E7EB] rounded-lg p-1 shadow-sm">
          <button
            type="button"
            onClick={handlePrevMonth}
            className="w-7 h-7 flex items-center justify-center rounded text-[#6B7280] hover:text-[#0D7377] hover:bg-[#E8F4F4] transition-colors"
            aria-label="Mês anterior"
          >
            <ChevronLeft className="w-4 h-4" />
          </button>
          <span aria-live="polite" className="text-sm font-semibold text-[#1A1A2E] px-2 min-w-[100px] text-center">
            {MONTHS[monthIndex]} {year}
          </span>
          <button
            type="button"
            onClick={handleNextMonth}
            className="w-7 h-7 flex items-center justify-center rounded text-[#6B7280] hover:text-[#0D7377] hover:bg-[#E8F4F4] transition-colors"
            aria-label="Próximo mês"
          >
            <ChevronRight className="w-4 h-4" />
          </button>
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
              className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-1 ${
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
