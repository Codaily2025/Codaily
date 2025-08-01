import React, { useState, useMemo } from 'react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
// ChevronLeft : 왼쪽 화살표
// ChevronRight : 오른쪽 화살표
import { ChevronLeft, ChevronRight } from 'lucide-react';

import './MyPageProductivityGraph.css';

const ProductivityChart = () => {
  const [viewMode, setViewMode] = useState('monthly');
  const [currentDate, setCurrentDate] = useState(new Date(2025, 0, 1));

  const generateMonthlyData = (year, month) => {
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    return Array.from({ length: daysInMonth }, (_, day) => ({
      day: day + 1,
      commits: Math.floor(Math.random() * 15) + 1,
      date: `${year}-${String(month + 1).padStart(2, '0')}-${String(day + 1).padStart(2, '0')}`
    }));
  };

  const generateWeeklyData = (startDate) => {
    const weekDays = ['월', '화', '수', '목', '금', '토', '일'];
    return weekDays.map((day, i) => {
      const date = new Date(startDate);
      date.setDate(startDate.getDate() + i);
      return {
        day,
        commits: Math.floor(Math.random() * 20) + 1,
        date: date.toISOString().split('T')[0],
        fullDay: day
      };
    });
  };

  const chartData = useMemo(() => {
    if (viewMode === 'monthly') {
      return generateMonthlyData(currentDate.getFullYear(), currentDate.getMonth());
    } else {
      const monday = new Date(currentDate);
      const dayOfWeek = monday.getDay();
      const diff = monday.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1);
      monday.setDate(diff);
      return generateWeeklyData(monday);
    }
  }, [viewMode, currentDate]);

  const goToPrevious = () => {
    setCurrentDate((prev) => {
      const newDate = new Date(prev);
      viewMode === 'monthly'
        ? newDate.setMonth(prev.getMonth() - 1)
        : newDate.setDate(prev.getDate() - 7);
      return newDate;
    });
  };

  const goToNext = () => {
    setCurrentDate((prev) => {
      const newDate = new Date(prev);
      viewMode === 'monthly'
        ? newDate.setMonth(prev.getMonth() + 1)
        : newDate.setDate(prev.getDate() + 7);
      return newDate;
    });
  };

  const getCurrentPeriodText = () => {
    if (viewMode === 'monthly') {
      const monthNames = ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'];
      return `${currentDate.getFullYear()}년 ${monthNames[currentDate.getMonth()]}`;
    } else {
      const monday = new Date(currentDate);
      const dayOfWeek = monday.getDay();
      const diff = monday.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1);
      monday.setDate(diff);
      const sunday = new Date(monday);
      sunday.setDate(monday.getDate() + 6);
      return `${monday.getFullYear()}년 ${monday.getMonth() + 1}월 ${monday.getDate()}일 - ${sunday.getMonth() + 1}월 ${sunday.getDate()}일`;
    }
  };

  // 그래프 툴팁 (날짜, 커밋 수)
  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload;
      return (
        <div className="tooltip">
          <p className="tooltip-title">
            {viewMode === 'monthly' ? `${currentDate.getFullYear()}년 ${currentDate.getMonth() + 1}월 ${label}일` : data.fullDay}
          </p>
          <p className="tooltip-value">커밋 수: {payload[0].value}</p>
        </div>
      );
    }
    return null;
  };

  return (
    <section className="productivity-graph-section">
      <div className="productivity-graph-header">
        <h2>Productivity</h2>
        <div className="productivity-graph-filters">
          <button
            onClick={() => setViewMode('monthly')}
            className={`productivity-graph-filter-btn ${viewMode === 'monthly' ? 'active' : ''}`}
          >
            월별
          </button>
          <button
            onClick={() => setViewMode('weekly')}
            className={`productivity-graph-filter-btn ${viewMode === 'weekly' ? 'active' : ''}`}
          >
            주별
          </button>
        </div>
        <div className="graph-navigation">
          <button onClick={goToPrevious} className="productivity-graph-nav-button">
            <ChevronLeft className="icon" />
          </button>
          <h3 className="graph-period-text">{getCurrentPeriodText()}</h3>
          <button onClick={goToNext} className="productivity-graph-nav-button">
            <ChevronRight className="icon" />
          </button>
        </div>
      </div>
      {/* 차트 영역 */}
      <div className="chart-area">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
            <defs>
              {/* 그래프 채우기 색 */}
              <linearGradient id="colorCommits" x1="0" y1="0" x2="0" y2="1">
                {/* <stop offset="5%" stopColor="#8b5cf6" stopOpacity={0.8} /> */}
                {/* <stop offset="5%" stopColor="#DBCFFD" stopOpacity={0.8} /> */}
                <stop offset="5%" stopColor="#A3A1E6" stopOpacity={0.8} />
                {/* <stop offset="95%" stopColor="#8b5cf6" stopOpacity={0.1}/> */}
                {/* <stop offset="95%" stopColor="#A5A3F7" stopOpacity={0.1} /> */}
                <stop offset="95%" stopColor="#A3A1E6" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis dataKey="day" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#6b7280' }} />
            <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#6b7280' }} />
            {/* <Tooltip /> */}
            <Tooltip content={<CustomTooltip />} />
            {/* 그래프 외곽선 색 */}
            <Area type="monotone" dataKey="commits" stroke="#A3A1E6" strokeWidth={2} fillOpacity={1} fill="url(#colorCommits)" />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      <div className="chart-summary">
        <div className="summary-box purple">
          <p>총 커밋</p>
          <p className="summary-value">{chartData.reduce((sum, item) => sum + item.commits, 0)}</p>
        </div>
        <div className="summary-box blue">
          <p>평균 커밋</p>
          <p className="summary-value">
            {Math.round(chartData.reduce((sum, item) => sum + item.commits, 0) / chartData.length)}
          </p>
        </div>
        <div className="summary-box green">
          <p>최대 커밋</p>
          <p className="summary-value">{Math.max(...chartData.map(item => item.commits))}</p>
        </div>
      </div>
    </section>
  );
};

export default ProductivityChart;
