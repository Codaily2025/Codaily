import React, { useState } from 'react';
import { memo } from 'react'; /* 메모이제이션 : 불필요한 렌더링에서 보호 */
/* 프로젝트 필터링 선택할 때마다 재렌더링 되는 것 방지 */
import styles from './ProgressSection.module.css';

// 예시를 위해 임시 데이터를 사용합니다.
const getDummyHeatmapDataForYear = (year) => {
  console.log(`Fetching data for ${year}...`); // 데이터 새로고침 확인용
  const data = [];
  for (let i = 0; i < 365; i++) {
    data.push({
      date: new Date(year, 0, i + 1),
      level: Math.floor(Math.random() * 4), // 0 to 3
    });
  }
  return data;
};

const ProgressSection = () => {
  const currentYear = new Date().getFullYear();
  const [year, setYear] = useState(currentYear);

  // 연도가 변경될 때마다 heatmap 데이터를 새로 가져옵니다.
  // 실제 애플리케이션에서는 이 부분에 API 호출 등을 사용할 수 있습니다.
  const heatmapData = getDummyHeatmapDataForYear(year);
  
  const firstDayOfYear = new Date(year, 0, 1).getDay(); // 1월 1일
  const lastDayOfYear = new Date(year, 11, 31).getDay(); // 12월 31일
  const leadingEmptyDays = firstDayOfYear; // 앞쪽 placeholder 셀 개수
  const trailingEmptyDays = 6 - lastDayOfYear; // 뒤쪽 placeholder 셀 개수

  const handlePrevYear = () => {
    setYear(prevYear => prevYear - 1);
  };

  const handleNextYear = () => {
    // 현재 연도보다 미래로는 이동하지 못하게 방지
    if (year < currentYear) {
      setYear(prevYear => prevYear + 1);
    }
  };

  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

  return (
    <section className={styles.progressSection}>
      <div className={styles.progressHeader}>
        <h2>Progress</h2>
        <div className={styles.yearNavigator}>
          <button onClick={handlePrevYear} className={styles.navButton}>&lt;</button>
          <span>{year}</span>
          <button 
            onClick={handleNextYear} 
            className={styles.navButton}
            disabled={year >= currentYear}
          >
            &gt;
          </button>
        </div>
      </div>

      <div className={styles.heatmapContainer}>
        {/* Month Labels */}
        <div className={styles.heatmapMonths}>
          {months.map(month => (
            <div key={month} className={styles.heatmapMonth}>{month}</div>
          ))}
        </div>
        
        <div className={styles.heatmapBody}>
            {/* Weekday Labels */}
            <div className={styles.heatmapWeekdays}>
                <span>M</span>
                <span>W</span>
                <span>F</span>
            </div>
            
            {/* Heatmap Grid - 기존 로직을 이 안에 배치 */}
            <div className={styles.heatmapGrid}>
              {/* 앞쪽 기간 외 셀 */}
              {[...Array(leadingEmptyDays)].map((_, i) => (
                <div key={`start-pad-${i}`} className={styles.heatmapCellPlaceholderCell} />
              ))}
              {/* 실제 데이터 셀 */}
              {heatmapData.map((data, index) => (
                  <div
                      key={index}
                      className={`${styles.heatmapCell} ${styles[`level-${data.level}`]}`}
                      title={`Date: ${data.date.toDateString()}, Level: ${data.level}`}
                  ></div>
              ))}
              {/* 뒤쪽 기간 외 셀 */}
              {[...Array(trailingEmptyDays)].map((_, i) => (
                <div key={`end-pad-${i}`} className={styles.heatmapCellPlaceholderCell} />
              ))}
            </div>
        </div>

        {/* Footer */}
        <div className={styles.heatmapFooter}>
          <div>
            {/* 현재 연도일 때만 주 정보 표시 */}
            {/* {year === currentYear && (
              <span className="week-info">Today</span>
            )} */}
              <div className={styles.footerDate}>2025 Monday first</div>
          </div>
          <div className={styles.heatmapLegend}>
            <span className={styles.legendLabel}>Less</span>
            <div className={`${styles.heatmapCell} ${styles['level-1']}`}></div>
            <div className={`${styles.heatmapCell} ${styles['level-2']}`}></div>
            <div className={`${styles.heatmapCell} ${styles['level-3']}`}></div>
            <span className={styles.legendLabel}>More</span>
          </div>
        </div>
      </div>
    </section>
  );
};

export default memo(ProgressSection);