import { useQuery } from '@tanstack/react-query';
import { fetchProductivityGraph } from '../apis/mypageProductivityGraph';

// 날짜를 YYYY-MM-DD 형식으로 변환하는 헬퍼 함수
const formatDate = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

// 주별 모드 시작일 계산
const getNowWeekStartMonday = (date) => {
  const d = new Date(date);
  const day = d.getDay(); // 0=일, 1=월, ...
  const diff = (day + 6) % 7; // 월요일 기준 보정
  d.setDate(d.getDate() - diff);
  d.setHours(0, 0, 0, 0); // 비교 오차 방지
  return d;
};

// 날짜 범위 계산 함수
const getDateRange = (viewMode, currentDate) => {
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

// React Query 훅
export const useProductivityGraphData = (viewMode, currentDate) => {
  const { startDate, endDate } = getDateRange(viewMode, currentDate);
  
  return useQuery({
    queryKey: ['productivityGraph', viewMode, startDate, endDate],
    queryFn: async () => {
      console.log(`API 호출 - 모드: ${viewMode}, 시작일: ${startDate}, 종료일: ${endDate}`);
      const response = await fetchProductivityGraph(viewMode, startDate, endDate);
      console.log('생산성 그래프 데이터 조회 성공:', response.data);
      return response.data;
    },
    staleTime: 5 * 60 * 1000, // 5분간 fresh 상태 유지
    cacheTime: 10 * 60 * 1000, // 10분간 캐시 유지
    retry: 1, // 실패시 1번만 재시도
    onError: (error) => {
      console.error('생산성 그래프 데이터 조회 실패:', error);
    }
  });
};