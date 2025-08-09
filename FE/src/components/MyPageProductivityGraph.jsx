import React, { useState, useMemo, useEffect } from 'react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
// ChevronLeft : 왼쪽 화살표
// ChevronRight : 오른쪽 화살표
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { fetchProductivityGraph } from '../apis/mypageProductivityGraph';

import './MyPageProductivityGraph.css';

const ProductivityChart = () => {
  // 대한민국 기존 '오늘' 생성
  // 마이페이지 들어갔을 때, 보여줄 처음 월/주 정보 기준
  const getSeoulToday = () => {
    const seoulNow = new Intl.DateTimeFormat('en-CA',
      {
        timeZone: 'Asia/Seoul',
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
      });
    const [y, m, d] = seoulNow.format(new Date()).split('-').map(Number);
    return new Date(y, m - 1, d);
  };

  // 월별/주별 모드 선택
  const [viewMode, setViewMode] = useState('monthly');
  // 현재 선택된 월/주 정보 기준
  const [currentDate, setCurrentDate] = useState(() => getSeoulToday());

  // 주별 모드 시작일 계산
  const getNowWeekStartMonday = (date) => {
    const d = new Date(date);
    const day = d.getDay(); // 0=일, 1=월, ...
    const diff = (day + 6) % 7; // 월요일 기준 보정
    d.setDate(d.getDate() - diff);
    d.setHours(0, 0, 0, 0); // 비교 오차 방지 -> 오늘 0시 0분 0초로 설정
    return d;
  }

  const generateMonthlyData = (year, month) => {
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    return Array.from({ length: daysInMonth }, (_, day) => ({
      day: day + 1,
      commits: Math.floor(Math.random() * 15) + 1,
      date: `${year}-${String(month + 1).padStart(2, '0')}-${String(day + 1).padStart(2, '0')}`
    }));
  };

  // 날짜를 YYYY-MM-DD 형식으로 변환하는 헬퍼 함수 (타임존 이슈 방지)
  const formatDate = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  const generateWeeklyData = (startDate) => {
    const monday = getNowWeekStartMonday(startDate);
    const weekDays = ['월', '화', '수', '목', '금', '토', '일'];
    return weekDays.map((day, i) => {
      const date = new Date(monday);
      date.setDate(monday.getDate() + i);
      return {
        day,
        commits: Math.floor(Math.random() * 20) + 1,
        // date: date.toISOString().split('T')[0],
        date: formatDate(date),
        fullDay: day
      };
    });
  };

  const today = useMemo(() => getSeoulToday(), []);

  // 오늘과 같은 달/같은 주인지 비교
  const isSameMonth = (a, b) => {
    return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth();
  };

  const isSameWeek = (a, b) => {
    return getNowWeekStartMonday(a).getTime() === getNowWeekStartMonday(b).getTime();
  };

  // 다음 버튼 비활성화 여부 -> 오늘이 속한 주/월 다음 주/월이면 비홀성화
  const isNextDisabled = viewMode === 'monthly'
    ? isSameMonth(currentDate, today)
    : isSameWeek(currentDate, today);

  // 이전 버튼 비활성화는 아직 사용자의 전체 프로젝트 커밋 기록을 가져오기 어려움으로 생략

  const chartData = useMemo(() => {
    if (viewMode === 'monthly') {
      return generateMonthlyData(currentDate.getFullYear(), currentDate.getMonth());
    } else {
      return generateWeeklyData(getNowWeekStartMonday(currentDate));
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
      // 한 번 더 방어 (오늘이 속한 주/월 다음 주/월이면 비활성화)
      const blocked = viewMode === 'monthly'
        ? isSameMonth(prev, today)
        : isSameWeek(prev, today);
      if (blocked) return prev;

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
      const monday = getNowWeekStartMonday(currentDate);
      const sunday = new Date(monday);
      sunday.setDate(monday.getDate() + 6);
      return `${monday.getFullYear()}년 ${monday.getMonth() + 1}월 ${monday.getDate()}일 - ${sunday.getMonth() + 1}월 ${sunday.getDate()}일`;
      // +1을 해주는 이유 : 월 표기 때문에 0월, 1월, ... 이렇게 표기되기 때문
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

  // 현재 viewMode와 currentDate를 기반으로 startDate와 endDate를 계산하는 함수
  const getDateRange = () => {
    if (viewMode === 'monthly') {
      // 월별 모드: 해당 월의 1일부터 말일까지
      const year = currentDate.getFullYear();
      const month = currentDate.getMonth();
      const startDate = new Date(year, month, 1);
      const endDate = new Date(year, month + 1, 0); // 해당 월의 마지막 날

      return {
        startDate: formatDate(startDate),
        endDate: formatDate(endDate)
      };
    } else {
      // 주별 모드: 해당 주의 월요일부터 일요일까지
      const monday = getNowWeekStartMonday(currentDate);
      const sunday = new Date(monday);
      sunday.setDate(monday.getDate() + 6);

      return {
        startDate: formatDate(monday),
        endDate: formatDate(sunday)
      };
    }
  };

  const fetchProductivityGraphData = async () => {
    try {
      const { startDate, endDate } = getDateRange();
      console.log(`API 호출 - 모드: ${viewMode}, 시작일: ${startDate}, 종료일: ${endDate}`);

      const response = await fetchProductivityGraph(viewMode, startDate, endDate);
      console.log('생산성 그래프 데이터 조회 성공:', response);
    } catch (error) {
      console.error('생산성 그래프 데이터 조회 실패:', error);
    }
  };

  useEffect(() => {
    fetchProductivityGraphData();
  }, [viewMode, currentDate]);

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
          <button
            onClick={goToNext}
            className={`productivity-graph-nav-button ${isNextDisabled ? 'disabled' : ''}`}
            disabled={isNextDisabled}
          >
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
                {/* <stop offset="5%" stopColor="#A3A1E6" stopOpacity={0.8} /> */}
                <stop offset="5%" stopColor="#B4B3DC" stopOpacity={0.8} />
                {/* <stop offset="95%" stopColor="#8b5cf6" stopOpacity={0.1}/> */}
                {/* <stop offset="95%" stopColor="#A5A3F7" stopOpacity={0.1} /> */}
                {/* <stop offset="95%" stopColor="#A3A1E6" stopOpacity={0} /> */}
                <stop offset="95%" stopColor="#B4B3DC" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis dataKey="day" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#6b7280' }} />
            <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#6b7280' }} />
            {/* <Tooltip /> */}
            <Tooltip content={<CustomTooltip />} />
            {/* 그래프 외곽선 색 */}
            {/* <Area type="monotone" dataKey="commits" stroke="#A3A1E6" strokeWidth={2} fillOpacity={1} fill="url(#colorCommits)" /> */}
            <Area type="monotone" dataKey="commits" stroke="#B4B3DC" strokeWidth={2} fillOpacity={1} fill="url(#colorCommits)" />
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
