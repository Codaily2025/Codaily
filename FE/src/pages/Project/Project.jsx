import { React, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import './Project.css'
import './TaskCard.css'
import ProjectTemplate from '@/components/layouts/ProjectTemplate'
import { useUserProjects } from '@/hooks/useProjects'
import useProjectStore from '@/stores/projectStore'

const Project = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const { 
    activeProjects,
    lastWorkedProject,
    currentProject,
    setActiveProjects, 
    updateLastWorkedProject, 
    setCurrentProject 
  } = useProjectStore()

  // id 여부에 따른 currentProject 설정
  useEffect(() => {
    if (id && activeProjects && activeProjects.length > 0) {
      // URL에 ID가 있으면 해당하는 프로젝트를 currentProject로 설정
      const targetProject = activeProjects.find(project => 
        project.projectId === parseInt(id)
      )
      if (targetProject) {
        setCurrentProject(targetProject)
        console.log('프로젝트 ID로 currentProject 설정:', targetProject)
      } else {
        console.log('해당 ID의 프로젝트를 찾을 수 없습니다:', id)
      }
    } else if (!id && lastWorkedProject) {
      // URL에 ID가 없으면 마지막 작업 프로젝트를 currentProject로 설정
      const targetProject = activeProjects?.find(project => 
        project.projectId === lastWorkedProject.id
      )
      if (targetProject) {
        setCurrentProject(targetProject)
        console.log('마지막 작업 프로젝트로 currentProject 설정:', targetProject)
      } else {
        setCurrentProject(lastWorkedProject)
        console.log('lastWorkedProject를 currentProject로 설정:', lastWorkedProject)
      }
    }
  }, [id, activeProjects, lastWorkedProject, setCurrentProject])
  
  // 사용자 프로젝트 목록 가져오기 (사이드바용)
  const { 
    data: projects, 
    isLoading: isProjectsLoading, 
    error: projectsError 
  } = useUserProjects()

  // 프로젝트 리스트 데이터 및 마지막 작업 프로젝트를 전역 상태에 저장
  useEffect(() => {
    if (projects && projects.length > 0) {
      setActiveProjects(projects)
      updateLastWorkedProject()
    }
  }, [projects, setActiveProjects, updateLastWorkedProject])

  console.log('사용자가 진행 중인 프로젝트 리스트: ', projects)
  console.log('현재 렌더링 중인 currentProject: ', currentProject)

  const handleCreateProject = () => {
    navigate('/project/create')
  };

  const isLoading = isProjectsLoading
  const error = projectsError

  if (isLoading) {
    return (
      <div className="project-container">
        <div className="loading-spinner">프로젝트 정보를 불러오는 중...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="project-container">
        <div className="error-message">
          오류가 발생했습니다: {error}
          <button onClick={() => window.location.reload()}>다시 시도</button>
        </div>
      </div>
    );
  }

  return (
    <div className="project-container">
      <div className="project-header">
        <div className="project-info">
          {/* 전역에서 가져온 currentProject 데이터 */}
          <h1>{currentProject?.title || '프로젝트'}</h1>
          <p>{currentProject?.description || `프로젝트 ID: ${currentProject?.projectId}`}</p>
          {currentProject?.lastWorkedDate && (
            <span className="last-worked">
              마지막 작업: {new Date(currentProject?.lastWorkedDate).toLocaleDateString('ko-KR')}
            </span>
          )}
        </div>
        {/* <button className="create-project-button" onClick={handleCreateProject}>
          + 새 프로젝트 만들기
        </button> */}
      </div>
      <ProjectTemplate 
        currentProject={currentProject} 
        projects={projects}
      />
    </div>
  );
};

export default Project
