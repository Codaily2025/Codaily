import React from 'react';
import CalendarHeatmap from 'react-calendar-heatmap';
import 'react-calendar-heatmap/dist/styles.css'; // 기본 스타일

const today = new Date();

const ProgressSection = () => {
  const startDate = new Date(today.getFullYear(), 0, 1); // 1월 1일
  const endDate = new Date(today.getFullYear(), 11, 31); /*1년치 */

  // 더미 데이터 생성 (1년치)
  const values = Array.from({ length: 365 }, (_, i) => {
    const date = new Date(today.getFullYear(), 0, i + 1);
    const count = Math.floor(Math.random() * 4); // 0~3 사이 level
    return { date: date.toISOString().slice(0, 10), count };
  });

  return (
    <div>
      <h2>Heatmap (잔디 그래프)</h2>
      <CalendarHeatmap
        startDate={startDate}
        endDate={endDate}
        values={values}
        classForValue={(value) => {
          if (!value || value.count === 0) return 'color-empty';
          return `color-github-${value.count}`; // 1~4 수준 표현
        }}
        showWeekdayLabels={true}
        tooltipDataAttrs={(value) => {
          return {
            'data-tip': `${value.date}: 활동량 ${value.count}`,
          };
        }}
      />
    </div>
  );
};

export default ProgressSection;
