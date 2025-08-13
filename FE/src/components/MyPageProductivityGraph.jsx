import React, { useMemo } from 'react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
// ChevronLeft : 왼쪽 화살표
// ChevronRight : 오른쪽 화살표
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { useProductivityGraphData } from '../queries/usemypageProductivityGraph';
import useProductivityGraphStore from '../stores/mypageProductivityGraphStore';

import './MyPageProductivityGraph.css';

const ProductivityChart = () => {
  // Zustand 스토어에서 상태와 액션 가져오기
  const {
    viewMode,
    currentDate,
    setViewMode,
    goToPrevious,
    goToNext,
    getCurrentPeriodText,
    getIsNextDisabled,
  } = useProductivityGraphStore();

  // React Query로 API 데이터 가져오기
  const { data: apiData, isLoading, error } = useProductivityGraphData(viewMode, currentDate);

  // 주별 모드 시작일 계산 (fallback 데이터 생성용)
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
      // commits: Math.floor(Math.random() * 15) + 1,
      productivityScore: Math.floor(Math.random() * 100) + 1,
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
        // commits: Math.floor(Math.random() * 20) + 1,
        productivityScore: Math.floor(Math.random() * 100) + 1,
        // date: date.toISOString().split('T')[0],
        date: formatDate(date),
        fullDay: day
      };
    });
  };

  // 다음 버튼 비활성화 여부는 스토어에서 계산
  const isNextDisabled = getIsNextDisabled();

  // API 데이터를 차트 형식으로 변환하는 함수
  const transformApiDataToChartData = (data) => {
    // console.log(`[${viewMode}] 변환 시작 - 원본 데이터:`, data);

    if (!data) {
      // console.log(`[${viewMode}] 데이터가 null/undefined`);
      return [];
    }

    if (viewMode === 'monthly') {
      // 월별 데이터 변환: API response가 객체 형태 {chartData: [...]}인 경우
      if (data.chartData && Array.isArray(data.chartData)) {
        // console.log(`[${viewMode}] 월별 데이터 변환 중:`, data.chartData);
        return data.chartData.map(item => ({
          day: parseInt(item.date), // "1", "2" -> 1, 2
          commits: item.commits ? item.commits : 0,
          productivityScore: item.productivityScore || 0,
          date: `${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}-${String(item.date).padStart(2, '0')}`
        }));
      } else {
        // console.log(`[${viewMode}] 월별 데이터 구조가 예상과 다름:`, data);
      }
    } else {
      // 주별 데이터 변환: API response가 배열 형태인 경우
      // console.log(`[${viewMode}] 주별 데이터 확인 - 배열인가?:`, Array.isArray(data));

      if (Array.isArray(data)) {
        // console.log(`[${viewMode}] 주별 데이터(배열) 변환 중:`, data);
        return data.map(item => ({
          day: item.day,
          commits: item.commits ? item.commits : 0,
          productivityScore: item.productivityScore || 0,
          date: item.date,
          fullDay: item.fullDay || item.day
        }));
      } else if (data.chartData && Array.isArray(data.chartData)) {
        // 주별도 월별과 같은 구조일 수 있음
        // console.log(`[${viewMode}] 주별 데이터(객체) 변환 중:`, data.chartData);
        return data.chartData.map(item => ({
          day: item.day || item.date,
          commits: item.commits ? item.commits : 0,
          productivityScore: item.productivityScore || 0,
          date: item.date,
          fullDay: item.fullDay || item.day
        }));
      } else {
        // console.log(`[${viewMode}] 주별 데이터 구조가 예상과 다름:`, data);
      }
    }

    // console.log(`[${viewMode}] 변환 실패 - 빈 배열 반환`);
    return [];
  };

  const chartData = useMemo(() => {
    // API 데이터가 있으면 변환해서 사용, 없으면 fallback 데이터 사용
    if (apiData) {
      const transformedData = transformApiDataToChartData(apiData);
      // console.log(`[${viewMode}] API 변환된 chartData (길이: ${transformedData.length}):`, transformedData);
      return transformedData;
    }
    // } else {
    //   // API 데이터가 없을 때 fallback (기존 로직)
    //   if (viewMode === 'monthly') {
    //     const fallbackData = generateMonthlyData(currentDate.getFullYear(), currentDate.getMonth());
    //     // console.log(`[${viewMode}] fallback chartData (길이: ${fallbackData.length}):`, fallbackData);
    //     return fallbackData;
    //   } else {
    //     const fallbackData = generateWeeklyData(getNowWeekStartMonday(currentDate));
    //     // console.log(`[${viewMode}] fallback chartData (길이: ${fallbackData.length}):`, fallbackData);
    //     return fallbackData;
    //   }
    // }
    // API 없거나 빈 배열이면 '랜덤' 대신 0으로 채운 스켈레톤
    if (viewMode === 'monthly') {
      const y = currentDate.getFullYear();
      const m = currentDate.getMonth();
      const daysInMonth = new Date(y, m + 1, 0).getDate();
      return Array.from({ length: daysInMonth }, (_, i) => ({
        day: i + 1,
        productivityScore: 0,
        date: `${y}-${String(m + 1).padStart(2, '0')}-${String(i + 1).padStart(2, '0')}`,
      }));
    } else {
      const monday = getNowWeekStartMonday(currentDate);
      const weekDays = ['월', '화', '수', '목', '금', '토', '일'];
      return weekDays.map((d, i) => {
        const dt = new Date(monday);
        dt.setDate(monday.getDate() + i);
        return {
          day: d,
          fullDay: d,
          productivityScore: 0,
          date: formatDate(dt),
        };
      });
    }
  }, [viewMode, currentDate, apiData]);
  // 월별 예시
  // [
  // {
  // commits: 6,
  // date: '2025-08-01',
  // day: 1
  // }, {
  // commits: 10,
  // date: '2025-08-02',
  // day: 2
  // }, ...]
  // 주별 예시
  // [
  // {
  // commits: 3,
  // date: '2025-08-04',
  // day: "월",
  // fullDay: "월",
  // }, {
  // commits: 2,
  // date: '2025-08-05',
  // day: "화",
  // fullDay: "화",
  // }, ...]


  // 네비게이션 함수들은 스토어에서 가져옴

  // 그래프 툴팁 (날짜, 커밋 수)
  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload;
      // console.log(`[Tooltip] viewMode: ${viewMode}, data:`, data, 'label:', label);

      let tooltipTitle = '';
      if (viewMode === 'monthly') {
        // 월별: "2025년 1월 15일"
        tooltipTitle = `${currentDate.getFullYear()}년 ${currentDate.getMonth() + 1}월 ${label}일`;
      } else {
        // 주별: "2025년 1월 15일 (월)"
        // console.log(`[Tooltip] 주별 - data:`, data);

        // currentDate를 기준으로 해당 주의 시작일(월요일) 계산
        const weekStart = getNowWeekStartMonday(currentDate);
        const weekDays = ['월', '화', '수', '목', '금', '토', '일'];
        const dayIndex = weekDays.indexOf(data.day);

        // console.log(`[Tooltip] 주별 계산 - weekStart:`, weekStart, `data.day: ${data.day}, dayIndex: ${dayIndex}`);

        if (dayIndex !== -1) {
          // 해당 요일의 정확한 날짜 계산
          const targetDate = new Date(weekStart);
          targetDate.setDate(weekStart.getDate() + dayIndex);

          const year = targetDate.getFullYear();
          const month = targetDate.getMonth() + 1;
          const day = targetDate.getDate();
          const fullDay = data.fullDay || data.day;

          // console.log(`[Tooltip] 계산된 날짜 - year: ${year}, month: ${month}, day: ${day}, fullDay: ${fullDay}`);
          tooltipTitle = `${year}년 ${month}월 ${day}일 ${fullDay}요일`;
        } else {
          // 요일을 찾을 수 없는 경우 fallback
          // console.log(`[Tooltip] 요일을 찾을 수 없음: ${data.day}`);
          tooltipTitle = data.fullDay || data.day || '날짜 정보 없음';
        }
      }

      return (
        <div className="tooltip">
          {/* <p className="tooltip-title"> */}
          {/* {viewMode === 'monthly' ? `${currentDate.getFullYear()}년 ${currentDate.getMonth() + 1}월 ${label}일` : data.fullDay} */}
          {/* </p> */}
          <p className="tooltip-title">{tooltipTitle}</p>

          {/* 생산성 점수와 커밋 수 보여주기 */}
          <p className="tooltip-value">생산성 점수: {data.productivityScore}</p>
          {typeof data.commits === 'number' && (
            <p className="tooltip-value">커밋 수: {data.commits}</p>
          )}
        </div>
      );
    }
    return null;
  };

  // API 호출 로직은 React Query로 이동됨

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
        {isLoading ? (
          <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
            <p>데이터를 불러오는 중...</p>
          </div>
        ) : error ? (
          <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
            <p>데이터를 불러오는데 실패했습니다.</p>
          </div>
        ) : (
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
              {/* <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#6b7280' }} /> */}
              {/* y축은 생산성 점수 범위 0-100 사이로 설정 */}
              <YAxis 
                domain={[0, 100]}
                axisLine={false}
                tickLine={false}
                tick={{ fontSize: 12, fill: '#6b7280' }}
              />
              {/* <Tooltip /> */}
              <Tooltip content={<CustomTooltip />} />
              {/* 그래프 외곽선 색 */}
              {/* <Area type="monotone" dataKey="commits" stroke="#A3A1E6" strokeWidth={2} fillOpacity={1} fill="url(#colorCommits)" /> */}
              {/* commits 필드 데이터(커밋 수)를 기반으로 그래프 채우기 */}
              <Area type="monotone" dataKey="productivityScore" stroke="#B4B3DC" strokeWidth={2} fillOpacity={1} fill="url(#colorCommits)" />
            </AreaChart>
          </ResponsiveContainer>
        )}
      </div>

      <div className="chart-summary">
        <div className="summary-box purple">
          <p>총 커밋</p>
          <p className="summary-value">
            {/* {(apiData?.summary?.totalCommits !== undefined) 
              ? apiData.summary.totalCommits 
              : chartData.reduce((sum, item) => sum + item.commits, 0)
            } */}
            {apiData?.summary?.totalCommits ?? 0}
          </p>
        </div>
        <div className="summary-box blue">
          <p>평균 커밋</p>
          <p className="summary-value">
          {Math.round(apiData?.summary?.averageCommits ?? 0)}
          </p>
        </div>
        <div className="summary-box green">
          <p>최대 커밋</p>
          <p className="summary-value">
            {/* {(apiData?.summary?.maxCommits !== undefined) 
              ? apiData.summary.maxCommits
              : Math.max(...chartData.map(item => item.commits))
            } */}
            {apiData?.summary?.maxCommits ?? 0}
          </p>
        </div>
      </div>
    </section>
  );
};

export default ProductivityChart;
