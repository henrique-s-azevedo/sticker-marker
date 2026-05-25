import './TabBar.css';

const TABS = [
  { key: 'ALL', label: 'Todos' },
  { key: 'MISSING', label: 'Em Falta' },
  { key: 'OWNED', label: 'Colecionados' },
  { key: 'DUPLICATE', label: 'Duplicados' },
];

export default function TabBar({ activeTab, onTabChange }) {
  return (
    <div className="tab-bar">
      {TABS.map(tab => (
        <button
          key={tab.key}
          className={`tab-bar__tab${activeTab === tab.key ? ' tab-bar__tab--active' : ''}`}
          onClick={() => onTabChange(tab.key)}
        >
          {tab.label}
        </button>
      ))}
    </div>
  );
}
