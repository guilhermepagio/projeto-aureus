import ConsolidacaoToolbar from '../../components/Consolidacao/ConsolidacaoToolbar';
import ConsolidacaoGrid from '../../components/Consolidacao/ConsolidacaoGrid';

export default function ConsolidacaoPage() {
  return (
    <div className="px-4 pb-4 w-full max-w-full">
      <div className="mb-4 mt-2 pl-2 border-l-4 border-blue-600">
        <h1 className="text-2xl font-bold text-gray-800">Consolidação</h1>
      </div>
      <ConsolidacaoToolbar />
      <ConsolidacaoGrid />
    </div>
  );
}
