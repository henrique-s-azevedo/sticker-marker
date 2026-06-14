/**
 * Summary bar displayed at the top of the collection view.
 * Shows owned/missing/duplicate counts and a completion percentage.
 * Percentage is computed client-side from the received props.
 *
 * @param {number} total - total stickers in the album
 * @param {number} owned - stickers the user owns
 * @param {number} missing - stickers still needed
 * @param {number} duplicates - total extra copies across all stickers
 */
import './StatsBar.css';

export default function StatsBar({ total, owned, missing, duplicates }) {
  const progress = total > 0 ? Math.round((owned / total) * 100) : 0;

  return (
    <div className="stats-bar">
      <div className="stats-bar__item stats-bar__item--owned">
        <span className="stats-bar__value">{owned}</span>
        <span className="stats-bar__label">Owned</span>
      </div>
      <div className="stats-bar__item stats-bar__item--missing">
        <span className="stats-bar__value">{missing}</span>
        <span className="stats-bar__label">Missing</span>
      </div>
      <div className="stats-bar__item stats-bar__item--duplicate">
        <span className="stats-bar__value">{duplicates}</span>
        <span className="stats-bar__label">Duplicates</span>
      </div>
      <div className="stats-bar__item stats-bar__item--progress">
        <span className="stats-bar__value">{progress}%</span>
        <span className="stats-bar__label">Progress</span>
      </div>
    </div>
  );
}
