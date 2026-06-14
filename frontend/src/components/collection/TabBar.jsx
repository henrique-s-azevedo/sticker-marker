import { useTranslation } from 'react-i18next';
import './TabBar.css';

const TAB_KEYS = [
  { key: 'ALL',       tKey: 'tabs.all' },
  { key: 'MISSING',   tKey: 'tabs.missing' },
  { key: 'OWNED',     tKey: 'tabs.owned' },
  { key: 'DUPLICATE', tKey: 'tabs.duplicates' },
];

export default function TabBar({ activeTab, onTabChange }) {
  const { t } = useTranslation();

  return (
    <div className="tab-bar">
      {TAB_KEYS.map(tab => (
        <button
          key={tab.key}
          className={`tab-bar__tab${activeTab === tab.key ? ' tab-bar__tab--active' : ''}`}
          onClick={() => onTabChange(tab.key)}
        >
          {t(tab.tKey)}
        </button>
      ))}
    </div>
  );
}
