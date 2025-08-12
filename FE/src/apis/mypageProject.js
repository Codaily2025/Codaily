// src/apis/mypageProject.js
// 마이페이지 프로젝트 관련 API
import { authInstance } from './axios';

/**
 * 프로젝트 목록을 정렬하는 함수
 * 1차: 시작일 기준 오름차순 (빠른 순)
 * 2차: 제목 기준 오름차순 (가나다순, abc순)
 * @param {Array} projects - 정렬할 프로젝트 배열
 * @returns {Array} - 정렬된 프로젝트 배열
 */
export const sortProjects = (projects) => {
  return [...projects].sort((a, b) => {
    // duration에서 시작일 추출 (예: "2025/06/10 ~ 2025/09/30" -> "2025/06/10")
    const getStartDate = (duration) => {
      const startDateStr = duration.split(' ~ ')[0];
      // "2025/06/10" 또는 "2025-06-10" 형식을 Date로 변환
      return new Date(startDateStr.replace(/\//g, '-'));
    };
    
    // 1차 정렬: 시작일 기준 오름차순 (빠른 순)
    const startDateA = getStartDate(a.duration);
    const startDateB = getStartDate(b.duration);
    
    if (startDateA.getTime() !== startDateB.getTime()) {
      return startDateA.getTime() - startDateB.getTime(); // 빠른 순
    }
    
    // 2차 정렬: 시작일이 같으면 제목 기준 오름차순 (가나다순)
    return a.title.localeCompare(b.title, 'ko', { 
      numeric: true, 
      sensitivity: 'base' 
    });
  });
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
  const scheduledDates = [];
  const dayMap = ['일', '월', '화', '수', '목', '금', '토'];
  
  // 'YYYY-MM-DD' 문자열을 분리하여 숫자로 변환
  const startParts = startDate.split('-').map(Number);
  // new Date(year, monthIndex, day) 형식으로 날짜를 생성 month는 0부터 시작하므로 -1 해주기
  const currentDate = new Date(startParts[0], startParts[1] - 1, startParts[2]);

  const endParts = endDate.split('-').map(Number);
  const lastDate = new Date(endParts[0], endParts[1] - 1, endParts[2])
  
  // 활성화된 요일 (작업 시간이 0보다 큰 요일) 목록
  const activeDays = Object.keys(timeByDay).filter(day => {
    const dayStr = String(day);
    const hours = timeByDay[day];
    return hours > 0;
  });
  
  // 모든 요일이 0일 때 기본값 설정 (임시 해결책)
  if (activeDays.length === 0) {
    console.log('모든 요일이 0시간으로 설정되어 있습니다. 기본값을 사용합니다.');
    // 월요일을 기본으로 설정
    activeDays.push('월');
  }

  while (currentDate <= lastDate) {
    const dayName = dayMap[currentDate.getDay()];
    if (activeDays.includes(dayName)) {
      scheduledDates.push(currentDate.toISOString().split('T')[0]);
    }
    currentDate.setDate(currentDate.getDate() + 1);
  }
  
  return scheduledDates;
};

// 더미 데이터 (API 실패시 폴백용)
const dummyProjects = [
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

/**
 * 사용자의 프로젝트 목록을 조회하는 API
 * @returns {Promise<Array>} - 프로젝트 목록
 */
export const fetchProjectsByUserId = async () => {
  try {
    const response = await authInstance.get(`projects`);
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

    return sortProjects(formattedProjects);
  } catch (error) {
    console.error('Error fetching projects from API:', error);
    console.log('Using dummy data instead...');
    
    // 에러 발생 시 더미 데이터 반환
    return sortProjects(dummyProjects);
  }
};

/**
 * 프로젝트 정보를 수정하는 API
 * @param {object} params - 필요한 파라미터 객체
 * @param {number} params.projectId - 프로젝트 ID
 * @param {object} params.projectData - 프론트엔드 폼 데이터
 * @returns {Promise<object>} - 수정된 프로젝트 데이터
 */
export const updateProjectAPI = async ({ projectId, projectData }) => {
  // console.log('Updating project via API...', { projectId, projectData });

  // timeByDay 객체를 안전하게 처리
  const safeTimeByDay = {};
  Object.entries(projectData.timeByDay).forEach(([key, value]) => {
    safeTimeByDay[String(key)] = value;
  });

  // 백엔드가 요구하는 형식으로 데이터 변환
  const transformedData = {
    title: projectData.title,
    startDate: projectData.startDate.replace(/\./g, '-'), // YYYY.MM.DD -> YYYY-MM-DD
    endDate: projectData.endDate.replace(/\./g, '-'),
    
    // timeByDay 객체를 { dateName, hours } 형태의 배열로 변환 (한글 요일명을 영문으로 변환)
    daysOfWeek: Object.entries(safeTimeByDay).map(([dateName, hours]) => {
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
  
  try {
    console.log('수정 요청 데이터:', transformedData)
    const response = await authInstance.patch(`projects/${projectId}`, transformedData, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    console.log('프로젝트 업데이트 성공', response.data);
    return response.data;
    
  } catch (error) {
    console.error('Error updating project:', error);
    throw error;
  }
};

/**
 * 프로젝트를 삭제하는 API
 * @param {number} projectId - 프로젝트 ID
 * @returns {Promise<void>}
 */

/* http://localhost:8081/projects/{projectId} */
export const deleteProjectAPI = async (projectId) => {
  try {
    const response = await authInstance.delete(`projects/${projectId}`);
    console.log('프로젝트 삭제 성공', response.data);
    return response.data;
  } catch (error) {
    console.error('Error deleting project:', error);
    throw error;
  }
};
