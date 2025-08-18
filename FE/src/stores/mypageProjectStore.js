// src/stores/mypageProjectStore.js
import { create } from 'zustand';
import { sortProjects } from '../apis/mypageProject';

// 더미 데이터는 여기에서 정의되지만 React Query로 초기 조회(fetch)할 예정입니다.
const initialProjects = [
  {
    id: 1,
    title: '팀 협업 칸반보드 제작',
    duration: '2025/06/10 ~ 2025/09/30',
    progress: 75,
    stack: ['React', 'WebSocket', 'Express'],
    disabled: false,
    repoUrl: 'https://github.com/sample1.git'
  },
  {
    id: 2,
    title: 'Next.js 기반 기술 블로그',
    duration: '2025/07/01 ~ 2025/08/01',
    progress: 20,
    stack: ['Next.js', 'TailwindCSS'],
    disabled: false,
    repoUrl: 'https://github.com/sample2.git'
  },
  {
    id: 3,
    title: '개인 포트폴리오 사이트',
    duration: '2025/02/05 ~ 2025/05/20',
    progress: 100,
    stack: ['HTML', 'CSS', 'JavaScript'],
    disabled: true,
    repoUrl: 'https://github.com/sample3.git'
  },
  {
    id: 4,
    title: 'React Native 모바일 앱',
    duration: '2025/03/01 ~ 2025/05/31',
    progress: 45,
    stack: ['React Native', 'TypeScript'],
    disabled: false,
    repoUrl: 'https://github.com/sample4.git'
  },
  {
    id: 5,
    title: 'Angular 대시보드',
    duration: '2025/02/05 ~ 2025/04/30',
    progress: 60,
    stack: ['Angular', 'TypeScript', 'Chart.js'],
    disabled: false,
    repoUrl: 'https://github.com/sample5.git'
  }
];

export const useProjectStore = create((set, get) => ({
  projects: [], // 프로젝트 목록 상태
  projectDetail: null, // 프로젝트 상세 정보 상태

  // 프로젝트 목록 설정 (정렬하여 저장)
  setProjects: (projects) => set({ projects: sortProjects(projects) }),
  
  // 프로젝트 삭제 (로컬 상태에서만)
  deleteProject: (projectId) =>
    set((state) => ({
      projects: state.projects.filter((p) => p.id !== projectId),
    })),
    
  // 프로젝트 업데이트 (로컬 상태에서만, 정렬 유지)
  updateProject: (updatedProject) =>
    set((state) => {
      const updatedProjects = state.projects.map((p) =>
        p.id === updatedProject.id ? updatedProject : p
      );
      return { projects: sortProjects(updatedProjects) };
    }),
    
  // 프로젝트 추가 (로컬 상태에서만, 정렬 유지)
  addProject: (newProject) => 
    set((state) => {
      const newProjects = [...state.projects, newProject];
      return { projects: sortProjects(newProjects) };
    }),

  // 프로젝트 상세 정보 설정 (API 응답을 프론트엔드 형식으로 변환)
  setProjectDetail: (apiResponse) => {
    if (!apiResponse) {
      set({ projectDetail: null });
      return;
    }

    // API 응답을 프론트엔드 형식으로 변환
    const projectDetail = {
      id: apiResponse.projectId,
      title: apiResponse.title,
      description: apiResponse.description,
      startDate: apiResponse.startDate,
      endDate: apiResponse.endDate,
      status: apiResponse.status,
      // daysOfWeeks를 timeByDay 형식으로 변환
      timeByDay: convertDaysOfWeeksToTimeByDay(apiResponse.daysOfWeeks),
      // repositories 정보
      repositories: apiResponse.repositories || [],
      // 작업 가능한 날짜 (기존 DB 데이터 그대로 사용)
      schedules: apiResponse.schedules || []
    };

    set({ projectDetail });
  },

  // 프로젝트 상세 정보 가져오기
  getProjectDetail: () => get().projectDetail,
  
  // 레포지토리 삭제 (프로젝트 상세 정보에서)
  removeRepository: (projectId, repoId) => {
    set((state) => {
      if (!state.projectDetail || state.projectDetail.id !== projectId) {
        return state;
      }
      
      const updatedRepositories = state.projectDetail.repositories.filter(
        repo => repo.repoId !== repoId
      );
      
      return {
        projectDetail: {
          ...state.projectDetail,
          repositories: updatedRepositories
        }
      };
    });
  },

  // 기간 변경에 따른 schedules 자동 조정
  adjustSchedulesForDateRange: (newStartDate, newEndDate) => {
    set((state) => {
      if (!state.projectDetail) {
        return state;
      }

      const currentSchedules = state.projectDetail.schedules || [];
      const adjustedSchedules = adjustSchedulesBasedOnDateRange(
        currentSchedules,
        newStartDate,
        newEndDate
      );

      return {
        projectDetail: {
          ...state.projectDetail,
          schedules: adjustedSchedules
        }
      };
    });
  },
}));

// 헬퍼 함수: 영문 요일명을 한글로 변환
const convertEnglishDayToKorean = (englishDay) => {
  const dayMap = {
    'MONDAY': '월',
    'TUESDAY': '화', 
    'WEDNESDAY': '수',
    'THURSDAY': '목',
    'FRIDAY': '금',
    'SATURDAY': '토',
    'SUNDAY': '일'
  };
  return dayMap[englishDay] || englishDay;
};

// 헬퍼 함수: API 응답의 daysOfWeeks를 프론트엔드 timeByDay 형식으로 변환
const convertDaysOfWeeksToTimeByDay = (daysOfWeeks) => {
  const timeByDay = {
    '월': 0, '화': 0, '수': 0, '목': 0, '금': 0, '토': 0, '일': 0
  };
  
  if (daysOfWeeks && Array.isArray(daysOfWeeks)) {
    daysOfWeeks.forEach(day => {
      const koreanDay = convertEnglishDayToKorean(day.dateName);
      timeByDay[koreanDay] = day.hours || 0;
    });
  }
  
  return timeByDay;
};

// 헬퍼 함수: 기간 변경에 따른 schedules 자동 조정
const adjustSchedulesBasedOnDateRange = (currentSchedules, newStartDate, newEndDate) => {
  if (!currentSchedules || !Array.isArray(currentSchedules)) {
    return [];
  }

  // 날짜 문자열을 Date 객체로 변환하는 함수
  const parseDate = (dateStr) => {
    if (!dateStr) return null;
    // "2025.08.03" 형식을 "2025-08-03" 형식으로 변환
    const normalizedDate = dateStr.replace(/\./g, '-');
    return new Date(normalizedDate);
  };

  const startDate = parseDate(newStartDate);
  const endDate = parseDate(newEndDate);

  if (!startDate || !endDate) {
    return currentSchedules;
  }

  // 기존 schedules에서 새로운 기간 범위 내의 날짜만 필터링
  const filteredSchedules = currentSchedules.filter(schedule => {
    const scheduleDate = new Date(schedule.scheduledDate);
    return scheduleDate >= startDate && scheduleDate <= endDate;
  });

  // 기간이 확장된 경우 새로운 날짜들을 자동으로 추가
  const existingDates = new Set(filteredSchedules.map(s => s.scheduledDate));
  const newSchedules = [...filteredSchedules];

  // 시작일 이전의 새로운 날짜들 추가
  const originalStartDate = currentSchedules.length > 0 
    ? new Date(Math.min(...currentSchedules.map(s => new Date(s.scheduledDate))))
    : startDate;
  
  if (startDate < originalStartDate) {
    const currentDate = new Date(startDate);
    while (currentDate < originalStartDate) {
      const dateStr = currentDate.toISOString().split('T')[0]; // YYYY-MM-DD 형식
      if (!existingDates.has(dateStr)) {
        newSchedules.push({
          scheduleId: newSchedules.length + 1,
          scheduledDate: dateStr
        });
        existingDates.add(dateStr);
      }
      currentDate.setDate(currentDate.getDate() + 1);
    }
  }

  // 종료일 이후의 새로운 날짜들 추가
  const originalEndDate = currentSchedules.length > 0
    ? new Date(Math.max(...currentSchedules.map(s => new Date(s.scheduledDate))))
    : endDate;

  if (endDate > originalEndDate) {
    const currentDate = new Date(originalEndDate);
    currentDate.setDate(currentDate.getDate() + 1); // 다음 날부터 시작
    while (currentDate <= endDate) {
      const dateStr = currentDate.toISOString().split('T')[0]; // YYYY-MM-DD 형식
      if (!existingDates.has(dateStr)) {
        newSchedules.push({
          scheduleId: newSchedules.length + 1,
          scheduledDate: dateStr
        });
        existingDates.add(dateStr);
      }
      currentDate.setDate(currentDate.getDate() + 1);
    }
  }

  // scheduleId 재정렬
  return newSchedules.map((schedule, index) => ({
    ...schedule,
    scheduleId: index + 1
  }));
};