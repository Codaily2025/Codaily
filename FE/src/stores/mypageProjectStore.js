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

export const useProjectStore = create((set) => ({
  projects: [], // 프로젝트 목록 상태
  
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
}));