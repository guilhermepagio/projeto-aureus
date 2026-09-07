import ConsolidacaoToolbar from '../../components/Consolidacao/ConsolidacaoToolbar';
import ConsolidacaoGrid from '../../components/Consolidacao/ConsolidacaoGrid';

export default function ConsolidacaoPage() {
  return (
    <div className="px-4 pb-4 pt-4 w-full max-w-full h-full flex flex-col">
      <div className="shrink-0">
        <ConsolidacaoToolbar />
      </div>
      <div className="flex-1 min-h-0">
        <ConsolidacaoGrid />
      </div>
    </div>
  );
}
