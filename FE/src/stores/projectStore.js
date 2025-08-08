import { create } from 'zustand'

const useProjectStore = create((set, get) => ({
  // 사용자가 진행 중인 프로젝트 리스트
  activeProjects: [],
  
  // 마지막으로 작업했던 프로젝트 (id, title, lastWorkedDate)
  lastWorkedProject: null,
  
  // 현재 화면에 렌더링 중인 프로젝트 (상세 정보 포함 여부는 아직 미정)
  currentProject: null,
  
  // activeProjects 설정
  setActiveProjects: (projects) => set({ activeProjects: projects }),
  
  // lastWorkedProject 설정
  setLastWorkedProject: (project) => set({ lastWorkedProject: project }),
  
  // currentProject 설정
  setCurrentProject: (project) => set({ currentProject: project }),
      
  // activeProjects에서 가장 최근에 작업한 프로젝트를 lastWorkedProject로 설정
  updateLastWorkedProject: () => {
    const { activeProjects } = get()
    
    if (!activeProjects || activeProjects.length === 0) {
      set({ lastWorkedProject: null })
      return
    }
    
    // lastWorkedDate 기준으로 정렬하여 가장 최근 프로젝트 선택
    const sortedProjects = [...activeProjects].sort(
      (a, b) => new Date(b.lastWorkedDate || b.lastWorkedAt) - new Date(a.lastWorkedDate || a.lastWorkedAt)
    )
    
    const mostRecentProject = sortedProjects[0]
    const lastWorkedProject = {
      id: mostRecentProject.id || mostRecentProject.projectId,
      title: mostRecentProject.title,
      lastWorkedDate: mostRecentProject.lastWorkedDate || mostRecentProject.lastWorkedAt
    }
    
    set({ lastWorkedProject })
  },
      
  // 프로젝트 추가 (activeProjects에 새 프로젝트 추가하고 lastWorked 업데이트)
  addProject: (project) => {
    const { activeProjects } = get()
    const updatedProjects = [...activeProjects, project]
    
    set({ activeProjects: updatedProjects })
    
    // lastWorked 프로젝트도 업데이트
    get().updateLastWorkedProject()
  },
      
  // 스토어 초기화
  resetStore: () => set({
    activeProjects: [],
    lastWorkedProject: null,
    currentProject: null
  })
}))

export default useProjectStore