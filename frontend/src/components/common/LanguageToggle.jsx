import { useTranslation } from 'react-i18next';
import './LanguageToggle.css';

export default function LanguageToggle() {
  const { i18n } = useTranslation();
  const lang = i18n.language;

  return (
    <div className="lang-toggle">
      <button
        className={`lang-toggle__btn${lang === 'en' ? ' lang-toggle__btn--active' : ''}`}
        onClick={() => i18n.changeLanguage('en')}
        aria-pressed={lang === 'en'}
      >
        🇬🇧 EN
      </button>
      <button
        className={`lang-toggle__btn${lang === 'pt' ? ' lang-toggle__btn--active' : ''}`}
        onClick={() => i18n.changeLanguage('pt')}
        aria-pressed={lang === 'pt'}
      >
        🇵🇹 PT
      </button>
    </div>
  );
}
