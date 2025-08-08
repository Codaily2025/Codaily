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
  console.log(`Fetching projects for userId: ${userId} using authInstance...`);
  
  try {
    //  authInstance.get을 사용
    //  defaultInstance.get을 사용
    // const response = await defaultInstance.get(`/users/${userId}/`);
    const response = await authInstance.get(`/users/${userId}`);
    console.log('projects response:', response);
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
    console.log('Using dummy data instead...');
    
    // 에러 발생 시 더미 데이터 반환
    return initialProjects;
  }
};
