import ConsolidacaoToolbar from '../../components/Consolidacao/ConsolidacaoToolbar';
import ConsolidacaoGrid from '../../components/Consolidacao/ConsolidacaoGrid';

export default function ConsolidacaoPage() {
  return (
    <div className="px-4 pb-4 w-full max-w-full h-full flex flex-col">
      <div className="mb-4 mt-2 pl-2 border-l-4 border-blue-600 shrink-0">
        <h1 className="text-2xl font-bold text-gray-800">Consolidação</h1>
      </div>
      <div className="shrink-0 mb-4">
        <ConsolidacaoToolbar />
      </div>
      <div className="flex-1 min-h-0">
        <ConsolidacaoGrid />
      </div>
    </div>
  );
}
