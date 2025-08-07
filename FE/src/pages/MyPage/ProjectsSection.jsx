// src/pages/MyPage/ProjectsSection.jsx
import React, { useState } from 'react';
import styles from './ProjectsSection.module.css';
import ProjectEditModal from '../../components/ProjectEditModal';
import { useNavigate } from 'react-router-dom';
import useModalStore from "../../store/modalStore";
import { useProjectStore } from '../../stores/mypageProjectStore';

const ProjectsSection = () => {
  const navigate = useNavigate();
  const [activeFilter, setActiveFilter] = useState('전체'); // 필터 상태를 컴포넌트 내부에서 관리
  // 삭제 
  // const [localProjects, setLocalProjects] = useState(projects);
  // 설정 모달 관련 상태 관리
  const [selectedProject, setSelectedProject] = useState(null);
  // const [showModal, setShowModal] = useState(false);
  const { isOpen, modalType, closeModal, openModal } = useModalStore()

  const { projects, deleteProject, updateProject } = useProjectStore();

  // 삭제 핸들러
  const handleDelete = (id) => {
    // setLocalProjects(prev => prev.filter(project => project.id !== id));
    deleteProject(id);
  };

  // 설정 모달 관련 핸들러
  const handleSettings = (project) => {
    setSelectedProject(project);
    // setShowModal(true);
    openModal('PROJECT_EDIT')
  };

  // 프로젝트 생성 핸들러
  const handleCreateProject = () => {
    navigate('/project/create');
  };

  // 프로젝트 보드 페이지 이동 핸들러 -> 향후 백엔드와 연결해 프로젝트 id를 기반으로 각 프로젝트 보드 페이지로 이동
  const handleProjectBoard = (id) => {
    navigate(`/project/${id}`);
  };


  // const filteredProjects = localProjects.filter((project) => {
  const filteredProjects = projects.filter((project) => {
    if (activeFilter === '전체') return true;
    if (activeFilter === '진행 중') return !project.disabled;
    if (activeFilter === '완료') return project.disabled;
    return true;
  });

  return (
    <section className={styles.projectsSection}>
      <div className={styles.projectsHeader}>
        <h2>Projects</h2>
        <div className={styles.projectFilters}>
          {['전체', '진행 중', '완료'].map((label) => (
            <button
              key={label}
              className={`${styles.filterBtn} ${activeFilter === label ? styles.active : ''}`}
              onClick={() => setActiveFilter(label)}
            >
              {label}
            </button>
          ))}
        </div>
      </div>

      {filteredProjects.length === 0 ? (
        <div className={styles.noProjectsContainer}>
          <div className={styles.noProjectsContent}>
            <p className={styles.noProjectsTitle}>생성한 프로젝트가 아직 없어요.</p>
            <p className={styles.noProjectsSubtitle}>지금 바로 프로젝트를 생성하고 관리해 보세요.</p>
            <button className={styles.createProjectBtn} onClick={() => handleCreateProject()}>프로젝트 생성하기</button>
          </div>
        </div>
      ) : (
        <div className={styles.projectList}>
          {filteredProjects.map((project) => (
            <div
              key={project.id}
              className={`${styles.projectCard} ${project.disabled ? styles.disabled : ''}`}
              onClick={() => handleProjectBoard(project.id)}
            >
              {/* 우측 상단 삭제, 설정 버튼 */}
              <div className={styles.cardActions}>
                <button 
                  className={`${styles.iconBtn} ${styles.deleteBtn}`} 
                  title="삭제" 
                  // onClick={() => handleDelete(project.id)}
                  onClick={(e) => {
                    e.stopPropagation(); // 카드 클릭 이벤트 버블링 방지
                    // 삭제 버튼을 클릭했을 때 상세 페이지로 이동하지 않기 위함
                    handleDelete(project.id)
                  }}
                >
                </button>
                <button 
                  className={`${styles.iconBtn} ${styles.settingsBtn}`} 
                  title="설정" 
                  onClick={(e) => {
                    e.stopPropagation(); // 카드 클릭 이벤트 버블링 방지
                    // 설정 버튼을 클릭했을 때 상세 페이지로 이동하지 않기 위함
                    handleSettings(project)
                  }}
                >
                </button>
              </div>
              <h3>{project.title}</h3>
              <p className={styles.projectDuration}>{project.duration}</p>
              <div className={styles.progressBarContainer}>
                <span>진행률</span>
                <span>{project.progress}%</span>
                <div className={styles.progressBar}>
                  <div
                    className={styles.progress}
                    style={{
                      width: `${project.progress}%`,
                      backgroundColor: project.disabled ? '#CCCBE4' : undefined
                    }}
                  ></div>
                </div>
              </div>
              {/* 기술 스택 */}
              <div className={styles.projectStack}>
                {project.stack.map((tech, index) => (
                  <span
                    key={index}
                    className={project.disabled ? styles.techTagDisabled : styles.techTagInProcess}
                  >
                    {tech}
                  </span>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
      {isOpen && (modalType === 'PROJECT_EDIT') &&  (
        <ProjectEditModal
          project={selectedProject}
          // onClose={() => setShowModal(false)}
          onClose={() => closeModal()}
          onSave={(updatedProject) => {
            console.log('🧩 저장된 프로젝트:', updatedProject);
            // setLocalProjects(prev =>
            //   prev.map(p => p.id === updatedProject.id ? updatedProject : p)
            // );
            updateProject(updatedProject); // Zustand 스토어에 업데이트된 프로젝트 저장
            // setShowModal(false);
            closeModal();
          }}
        />
      )}
    </section>
  );
};

export default ProjectsSection;
