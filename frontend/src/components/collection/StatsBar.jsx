import { useTranslation } from 'react-i18next';
import './StatsBar.css';

export default function StatsBar({ total, owned, missing, duplicates }) {
  const { t } = useTranslation();
  const progress = total > 0 ? Math.round((owned / total) * 100) : 0;

  return (
    <div className="stats-bar">
      <div className="stats-bar__item stats-bar__item--owned">
        <span className="stats-bar__value">{owned}</span>
        <span className="stats-bar__label">{t('stats.owned')}</span>
      </div>
      <div className="stats-bar__item stats-bar__item--missing">
        <span className="stats-bar__value">{missing}</span>
        <span className="stats-bar__label">{t('stats.missing')}</span>
      </div>
      <div className="stats-bar__item stats-bar__item--duplicate">
        <span className="stats-bar__value">{duplicates}</span>
        <span className="stats-bar__label">{t('stats.duplicates')}</span>
      </div>
      <div className="stats-bar__item stats-bar__item--progress">
        <span className="stats-bar__value">{progress}%</span>
        <span className="stats-bar__label">{t('stats.progress')}</span>
      </div>
    </div>
  );
}
