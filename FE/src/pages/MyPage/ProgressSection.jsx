import React, { useState } from 'react';
import { memo } from 'react'; /* 메모이제이션 : 불필요한 렌더링에서 보호 */
/* 프로젝트 필터링 선택할 때마다 재렌더링 되는 것 방지 */
import './ProgressSection.css';

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
    <section className="progress-section">
      <div className="progress-header">
        <h2>Progress</h2>
        <div className="year-navigator">
          <button onClick={handlePrevYear} className="nav-button">&lt;</button>
          <span>{year}</span>
          <button 
            onClick={handleNextYear} 
            className="nav-button"
            disabled={year >= currentYear}
          >
            &gt;
          </button>
        </div>
      </div>

      <div className="heatmap-container">
        {/* Month Labels */}
        <div className="heatmap-months">
          {months.map(month => (
            <div key={month} className="heatmap-month">{month}</div>
          ))}
        </div>
        
        <div className="heatmap-body">
            {/* Weekday Labels */}
            <div className="heatmap-weekdays">
                <span>M</span>
                <span>W</span>
                <span>F</span>
            </div>
            
            {/* Heatmap Grid - 기존 로직을 이 안에 배치 */}
            <div className="heatmap-grid">
              {/* 앞쪽 기간 외 셀 */}
              {[...Array(leadingEmptyDays)].map((_, i) => (
                <div key={`start-pad-${i}`} className="heatmap-cell placeholder-cell" />
              ))}
              {/* 실제 데이터 셀 */}
              {heatmapData.map((data, index) => (
                  <div
                      key={index}
                      className={`heatmap-cell level-${data.level}`}
                      title={`Date: ${data.date.toDateString()}, Level: ${data.level}`}
                  ></div>
              ))}
              {/* 뒤쪽 기간 외 셀 */}
              {[...Array(trailingEmptyDays)].map((_, i) => (
                <div key={`end-pad-${i}`} className="heatmap-cell placeholder-cell" />
              ))}
            </div>
        </div>

        {/* Footer */}
        <div className="heatmap-footer">
          <div>
            {/* 현재 연도일 때만 주 정보 표시 */}
            {/* {year === currentYear && (
              <span className="week-info">Today</span>
            )} */}
            <div className="footer-date">2025 Monday first</div>
          </div>
          <div className="heatmap-legend">
            <span className="legend-label">Less</span>
            <div className="heatmap-cell level-1"></div>
            <div className="heatmap-cell level-2"></div>
            <div className="heatmap-cell level-3"></div>
            <span className="legend-label">More</span>
          </div>
        </div>
      </div>
    </section>
  );
};

export default memo(ProgressSection);