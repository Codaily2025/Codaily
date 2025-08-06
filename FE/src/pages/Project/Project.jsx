import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import './Project.css';
import './TaskCard.css';
import './KanbanBoard.css';
import './Sidebar.css';
import ProjectTemplate from '../../components/layouts/ProjectTemplate';
import { useUserProjects, useLastWorkedProject, useProjectDetail } from '../../hooks/useProjects';

const Project = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  
  // 사용자 프로젝트 목록 가져오기 (사이드바용)
  const { 
    data: projects, 
    isLoading: isProjectsLoading, 
    error: projectsError 
  } = useUserProjects();

  // 프로젝트 상세 정보 가져오기
  const { 
    data: currentProject, 
    isLoading: isProjectLoading, 
    error: projectError 
  } = id ? 
    useProjectDetail(parseInt(id)) : // URL에 ID가 있으면 해당 프로젝트
    useLastWorkedProject(); // 없으면 마지막 작업 프로젝트

  const handleCreateProject = () => {
    navigate('/project/create');
  };

  const isLoading = isProjectsLoading || isProjectLoading;
  const error = projectsError || projectError;

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
          <h1>{currentProject?.title || '프로젝트'}</h1>
          <p>{currentProject?.description || `프로젝트 ID: ${id}`}</p>
          {currentProject?.lastWorkedAt && (
            <span className="last-worked">
              마지막 작업: {new Date(currentProject.lastWorkedAt).toLocaleDateString('ko-KR')}
            </span>
          )}
        </div>
        <button className="create-project-button" onClick={handleCreateProject}>
          + 새 프로젝트 만들기
        </button>
      </div>
      <ProjectTemplate currentProject={currentProject} projects={projects} />
    </div>
  );
};

export default Project;