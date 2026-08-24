import { useMonthStore } from '../../store/monthStore';


const getNext24Months = (startMonthYYYYMM: string) => {
  const [yearStr, monthStr] = startMonthYYYYMM.split('-');
  let year = parseInt(yearStr, 10);
  let monthIndex = parseInt(monthStr, 10) - 1;

  const result = [];
  for (let i = 0; i < 24; i++) {
    result.push({
      label: `${String(monthIndex + 1).padStart(2, '0')}/${year}`,
      value: `${year}-${String(monthIndex + 1).padStart(2, '0')}`
    });
    
    monthIndex++;
    if (monthIndex > 11) {
      monthIndex = 0;
      year++;
    }
  }
  return result;
};

export default function ConsolidacaoGrid() {
  const { selectedMonth } = useMonthStore();
  
  if (!selectedMonth || !selectedMonth.includes('-')) return null;
  
  const months = getNext24Months(selectedMonth);

  return (
    <div className="w-full border rounded-lg bg-white shadow-sm h-full flex flex-col">
      <div className="overflow-auto h-[calc(100vh-250px)] md:h-[calc(100vh-180px)]">
        <table className="w-full border-separate border-spacing-0 min-w-max relative">
          <thead>
            <tr className="bg-gray-50">
              <th aria-hidden="true" className="sticky left-0 top-0 z-20 bg-gray-50 p-4 text-left font-medium text-gray-700 min-w-[200px] border-b border-r border-gray-200">
                {/* Empty cell for row labels */}
              </th>
              {months.map(m => (
                <th scope="col" key={m.value} className="sticky top-0 z-10 bg-gray-50 p-4 text-center font-medium text-gray-700 min-w-[120px] border-b border-r border-gray-200">
                  {m.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {/* Fake rows to validate layout */}
            {[...Array(15)].map((_, rowIndex) => (
              <tr key={rowIndex} className="group hover:bg-gray-50 transition-colors">
                <th scope="row" className="sticky left-0 z-10 bg-white group-hover:bg-gray-50 p-4 text-left font-medium text-gray-800 border-b border-r border-gray-200">
                  Categoria {rowIndex + 1}
                </th>
                {months.map((m, colIndex) => (
                  <td key={m.value} className="p-4 text-center text-gray-600 border-b border-r border-gray-200">
                    R$ {(rowIndex * 100 + colIndex * 50).toFixed(2)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
