import { useState, useRef } from 'react';
import ConsolidacaoToolbar from '../../components/Consolidacao/ConsolidacaoToolbar';
import ConsolidacaoGrid from '../../components/Consolidacao/ConsolidacaoGrid';

export default function ConsolidacaoPage() {
  const scrollContainerRef = useRef<HTMLDivElement>(null);

  const [collapsed, setCollapsed] = useState<{
    receitas: boolean;
    despesas: boolean;
    categoriasVal: boolean;
    categoriasPct: boolean;
    resumo: boolean;
  }>({
    receitas: false,
    despesas: false,
    categoriasVal: false,
    categoriasPct: false,
    resumo: false,
  });

  const isAllCollapsed = Object.values(collapsed).every(Boolean);

  const handleToggleAll = () => {
    const next = !isAllCollapsed;
    setCollapsed({
      receitas: next,
      despesas: next,
      categoriasVal: next,
      categoriasPct: next,
      resumo: next,
    });
  };

  const handleToggleSection = (section: keyof typeof collapsed) => {
    setCollapsed(prev => ({ ...prev, [section]: !prev[section] }));
  };

  const handleResetScroll = () => {
    const container = scrollContainerRef.current;
    if (container) {
      if (typeof container.scrollTo === 'function') {
        container.scrollTo({ left: 0, behavior: 'smooth' });
      } else {
        container.scrollLeft = 0;
      }
    }
  };

  return (
    <div className="px-4 pb-4 pt-4 w-full max-w-full h-full flex flex-col">
      <div className="shrink-0">
        <ConsolidacaoToolbar
          onResetScroll={handleResetScroll}
          isAllCollapsed={isAllCollapsed}
          onToggleAll={handleToggleAll}
        />
      </div>
      <div className="flex-1 min-h-0">
        <ConsolidacaoGrid
          scrollContainerRef={scrollContainerRef}
          collapsed={collapsed}
          onToggleSection={handleToggleSection}
        />
      </div>
    </div>
  );
}
