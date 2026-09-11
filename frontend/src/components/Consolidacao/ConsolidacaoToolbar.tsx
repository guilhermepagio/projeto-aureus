import { ChevronLeft, ChevronRight } from 'lucide-react';
import { useMonthStore } from '../../store/monthStore';

const MONTHS = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

export default function ConsolidacaoToolbar() {
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
  };

  return (
    <div className="flex items-center mb-4">
      <div className="flex items-center gap-3 flex-wrap">
        <h1 className="text-2xl font-bold tracking-tight text-[#1A1A2E]">Consolidação</h1>

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
          className="px-4 py-1.5 text-[13px] font-semibold text-white bg-[#D4A843] hover:bg-[#C09538] rounded-lg shadow-sm transition-all hover:-translate-y-0.5 cursor-pointer"
        >
          Mês Atual
        </button>
      </div>
    </div>
  );
}
