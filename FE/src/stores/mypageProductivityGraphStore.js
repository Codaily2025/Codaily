import { create } from 'zustand';

// 대한민국 현재 날짜 생성
const getSeoulToday = () => {
  const seoulNow = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Seoul',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
  const [y, m, d] = seoulNow.format(new Date()).split('-').map(Number);
  return new Date(y, m - 1, d);
};

// 주별 모드 시작일 계산
const getNowWeekStartMonday = (date) => {
  const d = new Date(date);
  const day = d.getDay(); // 0=일, 1=월, ...
  const diff = (day + 6) % 7; // 월요일 기준 보정
  d.setDate(d.getDate() - diff);
  d.setHours(0, 0, 0, 0);
  return d;
};

const useProductivityGraphStore = create((set, get) => ({
  // 상태
  viewMode: 'monthly', // 'monthly' | 'weekly'
  currentDate: getSeoulToday(),
  today: getSeoulToday(),

  // 액션
  setViewMode: (mode) => set({ viewMode: mode }),
  
  setCurrentDate: (date) => set({ currentDate: date }),

  // 이전 기간으로 이동
  goToPrevious: () => set((state) => {
    const newDate = new Date(state.currentDate);
    if (state.viewMode === 'monthly') {
      newDate.setMonth(state.currentDate.getMonth() - 1);
    } else {
      newDate.setDate(state.currentDate.getDate() - 7);
    }
    return { currentDate: newDate };
  }),

  // 다음 기간으로 이동 (오늘 이후로는 이동 불가)
  goToNext: () => set((state) => {
    const { viewMode, currentDate, today } = state;
    
    // 다음 버튼 비활성화 체크
    const isSameMonth = (a, b) => {
      return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth();
    };
    
    const isSameWeek = (a, b) => {
      return getNowWeekStartMonday(a).getTime() === getNowWeekStartMonday(b).getTime();
    };

    const blocked = viewMode === 'monthly'
      ? isSameMonth(currentDate, today)
      : isSameWeek(currentDate, today);
    
    if (blocked) return state;

    const newDate = new Date(currentDate);
    if (viewMode === 'monthly') {
      newDate.setMonth(currentDate.getMonth() + 1);
    } else {
      newDate.setDate(currentDate.getDate() + 7);
    }
    
    return { currentDate: newDate };
  }),

  // 계산된 값들 (getter 함수들)
  getCurrentPeriodText: () => {
    const { viewMode, currentDate } = get();
    
    if (viewMode === 'monthly') {
      const monthNames = ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'];
      return `${currentDate.getFullYear()}년 ${monthNames[currentDate.getMonth()]}`;
    } else {
      const monday = getNowWeekStartMonday(currentDate);
      const sunday = new Date(monday);
      sunday.setDate(monday.getDate() + 6);
      return `${monday.getFullYear()}년 ${monday.getMonth() + 1}월 ${monday.getDate()}일 - ${sunday.getMonth() + 1}월 ${sunday.getDate()}일`;
    }
  },

  getIsNextDisabled: () => {
    const { viewMode, currentDate, today } = get();
    
    const isSameMonth = (a, b) => {
      return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth();
    };
    
    const isSameWeek = (a, b) => {
      return getNowWeekStartMonday(a).getTime() === getNowWeekStartMonday(b).getTime();
    };

    return viewMode === 'monthly'
      ? isSameMonth(currentDate, today)
      : isSameWeek(currentDate, today);
  },
}));

export default useProductivityGraphStore;
