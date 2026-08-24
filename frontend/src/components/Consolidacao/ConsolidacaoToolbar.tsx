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
    <div className="flex items-center justify-between mb-4">
      <div className="flex items-center space-x-4">
        <button
          onClick={handlePrevMonth}
          className="p-2 border rounded-md hover:bg-gray-100 transition-colors bg-white"
          aria-label="Mês anterior"
        >
          <ChevronLeft className="w-5 h-5 text-gray-700" />
        </button>
        <span aria-live="polite" className="text-lg font-medium text-gray-800 capitalize w-24 text-center">
          {MONTHS[monthIndex]} {year}
        </span>
        <button
          onClick={handleNextMonth}
          className="p-2 border rounded-md hover:bg-gray-100 transition-colors bg-white"
          aria-label="Próximo mês"
        >
          <ChevronRight className="w-5 h-5 text-gray-700" />
        </button>
      </div>
      <button
        onClick={handleCurrentMonth}
        className="px-4 py-2 text-sm font-medium text-blue-600 bg-blue-50 hover:bg-blue-100 rounded-md transition-colors"
      >
        Mês Atual
      </button>
    </div>
  );
}
