import React, { useState } from 'react';
import './ProjectsSection.css';
import ProjectEditModal from '../../components/ProjectEditModal';
import { useNavigate } from 'react-router-dom';

const ProjectsSection = ({ projects, activeFilter, setActiveFilter }) => {
  const navigate = useNavigate();
  // 삭제 
  const [localProjects, setLocalProjects] = useState(projects);
  // 설정 모달 관련 상태 관리
  const [selectedProject, setSelectedProject] = useState(null);
  const [showModal, setShowModal] = useState(false);

  // 삭제 핸들러
  const handleDelete = (id) => {
    setLocalProjects(prev => prev.filter(project => project.id !== id));
  };

  // 설정 모달 관련 핸들러
  const handleSettings = (project) => {
    setSelectedProject(project);
    setShowModal(true);
  };

  // 프로젝트 생성 핸들러
  const handleCreateProject = () => {
    navigate('/project/create');
  };

  const filteredProjects = localProjects.filter((project) => {
    if (activeFilter === '전체') return true;
    if (activeFilter === '진행 중') return !project.disabled;
    if (activeFilter === '완료') return project.disabled;
    return true;
  });

  return (
    <section className="projects-section">
      <div className="projects-header">
        <h2>Projects</h2>
        <div className="project-filters">
          {['전체', '진행 중', '완료'].map((label) => (
            <button
              key={label}
              className={`filter-btn ${activeFilter === label ? 'active' : ''}`}
              onClick={() => setActiveFilter(label)}
            >
              {label}
            </button>
          ))}
        </div>
      </div>

      {filteredProjects.length === 0 ? (
        <div className="no-projects-container">
          <div className="no-projects-content">
            <p className="no-projects-title">생성한 프로젝트가 아직 없어요.</p>
            <p className="no-projects-subtitle">지금 바로 프로젝트를 생성하고 관리해 보세요.</p>
            <button className="create-project-btn" onClick={() => handleCreateProject()}>프로젝트 생성하기</button>
          </div>
        </div>
      ) : (
        <div className="project-list">
          {filteredProjects.map((project) => (
            <div
              key={project.id}
              className={`project-card ${project.disabled ? 'disabled' : ''}`}
            >
              {/* 우측 상단 삭제, 설정 버튼 */}
              <div className="card-actions">
                <button className="icon-btn delete-btn" title="삭제" onClick={() => handleDelete(project.id)}>
                </button>
                <button className="icon-btn settings-btn" title="설정" onClick={() => handleSettings(project)}>
                </button>
              </div>
              <h3>{project.title}</h3>
              <p className="project-duration">{project.duration}</p>
              <div className="progress-bar-container">
                <span>진행률</span>
                <span>{project.progress}%</span>
                <div className="progress-bar">
                  <div
                    className="progress"
                    style={{
                      width: `${project.progress}%`,
                      backgroundColor: project.disabled ? '#CCCBE4' : undefined
                    }}
                  ></div>
                </div>
              </div>
              {/* 기술 스택 */}
              <div className="project-stack">
                {project.stack.map((tech, index) => (
                  <span
                    key={index}
                    className={project.disabled ? 'tech-tag-disabled' : 'tech-tag-in-process'}
                  >
                    {tech}
                  </span>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
      {showModal && (
        <ProjectEditModal
          project={selectedProject}
          onClose={() => setShowModal(false)}
          onSave={(updatedProject) => {
            setLocalProjects(prev =>
              prev.map(p => p.id === updatedProject.id ? updatedProject : p)
            );
            setShowModal(false);
          }}
        />
      )}
    </section>
  );
};

export default ProjectsSection;
