import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Navigation from './components/Navigation/Navigation';

// Placeholders for routes
const Consolidacao = () => <div style={{ padding: '24px' }}><h2>Consolidação</h2><p>Conteúdo da Consolidação</p></div>;
const DespesasVariaveis = () => <div style={{ padding: '24px' }}><h2>Despesas Variáveis</h2><p>Conteúdo de Despesas Variáveis</p></div>;
const DespesasFixas = () => <div style={{ padding: '24px' }}><h2>Despesas Fixas</h2><p>Conteúdo de Despesas Fixas</p></div>;
const ReceitasVariaveis = () => <div style={{ padding: '24px' }}><h2>Receitas Variáveis</h2><p>Conteúdo de Receitas Variáveis</p></div>;
const ReceitasFixas = () => <div style={{ padding: '24px' }}><h2>Receitas Fixas</h2><p>Conteúdo de Receitas Fixas</p></div>;

function App() {
  return (
    <BrowserRouter>
      <Navigation />
      <main style={{ paddingBottom: '80px' /* Leave space for bottom nav on mobile */ }}>
        <Routes>
          <Route path="/" element={<Consolidacao />} />
          <Route path="/despesas-variaveis" element={<DespesasVariaveis />} />
          <Route path="/despesas-fixas" element={<DespesasFixas />} />
          <Route path="/receitas-variaveis" element={<ReceitasVariaveis />} />
          <Route path="/receitas-fixas" element={<ReceitasFixas />} />
          <Route path="*" element={<div style={{ padding: '24px' }}><h2>404 - Página não encontrada</h2></div>} />
        </Routes>
      </main>
    </BrowserRouter>
  );
}

export default App;
