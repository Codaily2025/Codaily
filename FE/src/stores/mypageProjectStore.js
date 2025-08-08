// src/stores/mypageProjectStore.js
import { create } from 'zustand';
import { authInstance, defaultInstance } from '../apis/axios';

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
    duration: '2025/04/05 ~ 2025/05/20',
    progress: 100,
    stack: ['HTML', 'CSS', 'JavaScript'],
    disabled: true,
    repoUrl: 'https://github.com/sample3.git'
  }
];

export const useProjectStore = create((set) => ({
  projects: [], // 초기 상태는 빈 배열
  setProjects: (projects) => set({ projects }),
  deleteProject: (projectId) =>
    set((state) => ({
      projects: state.projects.filter((p) => p.id !== projectId),
    })),
  updateProject: (updatedProject) =>
    set((state) => ({
      projects: state.projects.map((p) =>
        p.id === updatedProject.id ? updatedProject : p
      ),
    })),
  // 참고: 프로젝트 추가용 addProject 함수도 구현해야 함(우리 프로젝트에서는 현재 필요 없음음)
  // addProject: (newProject) => set((state) => ({ projects: [...state.projects, newProject] })),
}));

// React Query가 사용할 모의(fetch) 함수
// 네트워크 요청을 시뮬레이션합니다.
const fetchProjects = async () => {
  console.log('프로젝트를 조회합니다 (모의 API 호출)...');
  await new Promise(resolve => setTimeout(resolve, 500)); // 네트워크 지연을 시뮬레이션합니다
  return initialProjects;
};

export { fetchProjects };

// 백엔드 API에서 프로젝트 목록 조회하는 함수
export const fetchProjectsByUserId = async (userId) => {
  // console.log(`Fetching projects for userId: ${userId} using authInstance...`);
  // console.log(`Fetching projects for userId: ${userId} using authInstance...`);
  
  try {
    //  authInstance.get을 사용
    //  defaultInstance.get을 사용
    // const response = await defaultInstance.get(`/users/${userId}/`);
    const response = await authInstance.get(`/users/${userId}`);
    // console.log('projects response:', response);
    const projectsFromApi = response.data;

    // 백엔드 데이터를 프론트엔드 형식으로 변환
    const formattedProjects = projectsFromApi.map(project => ({
      id: project.projectId,
      title: project.title,
      duration: `${project.startDate} ~ ${project.endDate}`,
      progress: project.progressRate,
      stack: project.techStacks,
      disabled: project.status === '완료',
      repoUrl: `https://github.com/sample${project.projectId}.git`
    }));

    return formattedProjects;
  } catch (error) {
    console.error('Error fetching projects from API:', error);
    // console.log('Using dummy data instead...');
    
    // 에러 발생 시 더미 데이터 반환
    return initialProjects;
  }
};

/**
 * 헬퍼 함수: 한글 요일명을 영문으로 변환
 * @param {string} koreanDay - 한글 요일명 (월, 화, 수, 목, 금, 토, 일)
 * @returns {string} - 영문 요일명 (MONDAY, TUESDAY, ...)
 */
const convertKoreanDayToEnglish = (koreanDay) => {
  const dayMap = {
    '월': 'MONDAY',
    '화': 'TUESDAY', 
    '수': 'WEDNESDAY',
    '목': 'THURSDAY',
    '금': 'FRIDAY',
    '토': 'SATURDAY',
    '일': 'SUNDAY'
  };
  return dayMap[koreanDay] || koreanDay;
};

/**
 * 헬퍼 함수: 시작일, 종료일, 활성화된 요일을 기반으로 실제 작업 날짜 목록을 생성합니다.
 * @param {string} startDate - 'YYYY-MM-DD' 형식의 시작일
 * @param {string} endDate - 'YYYY-MM-DD' 형식의 종료일
 * @param {object} timeByDay - { 월: 8, 화: 0, ... } 형식의 요일별 작업 시간 객체
 * @returns {string[]} - ['YYYY-MM-DD', ...] 형식의 날짜 배열
 */
const generateScheduledDates = (startDate, endDate, timeByDay) => {
  // console.log('generateScheduledDates inputs:', { startDate, endDate, timeByDay });
  // console.log('timeByDay type:', typeof timeByDay);
  // console.log('timeByDay keys:', Object.keys(timeByDay));
  // console.log('timeByDay entries:', Object.entries(timeByDay));
  
  const scheduledDates = [];
  // {startDate: '2025-08-01', endDate: '2025-11-30', timeByDay: {금: 0, 목: 0, 수: 0, 월: 0, 일: 0, 토: 0, 화: 0}}
  // const currentDate = new Date(startDate);
  // const lastDate = new Date(endDate);
  const dayMap = ['일', '월', '화', '수', '목', '금', '토'];
    // 'YYYY-MM-DD' 문자열을 분리하여 숫자로 변환
    const startParts = startDate.split('-').map(Number);
    // new Date(year, monthIndex, day) 형식으로 날짜를 생성 month는 0부터 시작하므로 -1 해주기기
    const currentDate = new Date(startParts[0], startParts[1] - 1, startParts[2]);
  
    const endParts = endDate.split('-').map(Number);
    const lastDate = new Date(endParts[0], endParts[1] - 1, endParts[2])
  // 활성화된 요일 (작업 시간이 0보다 큰 요일) 목록
  const activeDays = Object.keys(timeByDay).filter(day => {
    const dayStr = String(day);
    const hours = timeByDay[day];
    console.log('Checking day:', dayStr, 'hours:', hours, 'type:', typeof day);
    return hours > 0;
  });
  console.log('activeDays:', activeDays);
  
  // 디버깅을 위한 추가 로그
  console.log('timeByDay object:', timeByDay);
  console.log('activeDays length:', activeDays.length);
  
  // 모든 요일이 0일 때 기본값 설정 (임시 해결책)
  if (activeDays.length === 0) {
    console.log('모든 요일이 0시간으로 설정되어 있습니다. 기본값을 사용합니다.');
    // 월요일을 기본으로 설정
    activeDays.push('월');
    console.log('기본값 설정 후 activeDays:', activeDays);
  }

  while (currentDate <= lastDate) {
    const dayName = dayMap[currentDate.getDay()];
    if (activeDays.includes(dayName)) {
      scheduledDates.push(currentDate.toISOString().split('T')[0]);
    }
    currentDate.setDate(currentDate.getDate() + 1);
  }
  // console.log('Generated scheduledDates:', scheduledDates); // []
  return scheduledDates;
};


/**
 * 백엔드 API로 프로젝트 정보를 수정(PATCH)하는 함수
 * @param {object} params - 필요한 파라미터 객체
 * @param {number} params.userId - 사용자 ID
 * @param {number} params.projectId - 프로젝트 ID
 * @param {object} params.projectData - 프론트엔드 폼 데이터
 */
export const updateProjectAPI = async ({ userId : tempUserId, projectId, projectData }) => {
  const userId = 1; // 테스트를 위해 임시로 1로 설정
  console.log('Updating project via API...', { userId, projectId, projectData });

  // timeByDay 객체를 안전하게 처리
  const safeTimeByDay = {};
  Object.entries(projectData.timeByDay).forEach(([key, value]) => {
    safeTimeByDay[String(key)] = value;
  });
  console.log('Safe timeByDay:', safeTimeByDay);

  // 1. 백엔드가 요구하는 형식으로 데이터 변환
  const transformedData = {
    title: projectData.title,
    startDate: projectData.startDate.replace(/\./g, '-'), // YYYY.MM.DD -> YYYY-MM-DD
    endDate: projectData.endDate.replace(/\./g, '-'),
    
    // timeByDay 객체를 { dateName, hours } 형태의 배열로 변환 (한글 요일명을 영문으로 변환)
    daysOfWeek: Object.entries(safeTimeByDay).map(([dateName, hours]) => {
      console.log('Processing day:', dateName, 'type:', typeof dateName, 'hours:', hours);
      // 키를 강제로 문자열로 변환
      const dayNameStr = String(dateName);
      return {
        dateName: convertKoreanDayToEnglish(dayNameStr),
        hours,
      };
    }),
    
    // scheduledDates 배열 생성
    scheduledDates: generateScheduledDates(
      projectData.startDate.replace(/\./g, '-'),
      projectData.endDate.replace(/\./g, '-'),
      safeTimeByDay
    ),
  };
  
  console.log('transformedData:', transformedData);
  try {
    // 올바른 형식
    // {
    //   "title": "string",
    //   "startDate": "2025-08-08",
    //   "endDate": "2025-08-08",
    //   "scheduledDates": [
    //     "2025-08-08"
    //   ],
    //   "daysOfWeek": [
    //     {
    //       "dateName": "string",
    //       "hours": 0
    //     }
    //   ]
    // }
  
    // transformedData.scheduledDates = ["2025-08-08"];

    console.log('projectId:', projectId);
    console.log('userId:', userId);
    console.log('transformedData:', transformedData);
    const response = await authInstance.patch(`/users/${userId}/${projectId}`, transformedData, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    console.log('프로젝트 업데이트 성공', response.data);
    return response.data;
    
  } catch (error) {
    console.log('transformedData2:', transformedData);
    console.error('Error updating project:', error);
    // 에러를 다시 throw하여 useMutation의 onError에서 처리할 수 있도록 함
    throw error;
  }
};