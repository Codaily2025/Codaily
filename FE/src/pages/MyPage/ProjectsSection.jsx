// src/pages/MyPage/ProjectsSection.jsx
import React, { useState, useEffect, useCallback, useRef } from 'react';
import styles from './ProjectsSection.module.css';
import { useNavigate } from 'react-router-dom';
import useModalStore from "../../store/modalStore";
import { useProjectStore } from '../../stores/mypageProjectStore';
import { useAuthStore } from '../../stores/authStore';
import { useProjectsQuery } from '../../queries/useProjectsQuery';
import { useDeleteProjectMutation } from '../../queries/useProjectMutation';
import { useDeleteGithubRepoMutation } from '../../queries/useGitHub';
import { getProjectDetailAPI } from '../../apis/mypageProject';

const ProjectsSection = () => {

  // const { user } = useAuthStore();
  // const userId = user?.userId;
  const userId = 1;
  const navigate = useNavigate();
  const [activeFilter, setActiveFilter] = useState('전체'); // 필터 상태를 컴포넌트 내부에서 관리
  // 설정 모달 관련 상태 관리
  const [selectedProject, setSelectedProject] = useState(null);
  const { isOpen, modalType, closeModal, openModal } = useModalStore()
  
  // 삭제 진행 상태 관리
  const [isDeleting, setIsDeleting] = useState(false);

  // 더보기 기능을 위한 상태 (초기값 10개)
  const [visibleCount, setVisibleCount] = useState(10);

  const { projects, setProjects } = useProjectStore();
  
  // 스크롤 위치 유지를 위한 ref
  const projectsSectionRef = useRef(null);
  
  // React Query를 사용하여 프로젝트 데이터 조회
  const { data: projectsFromServer, isLoading, error } = useProjectsQuery();
  
  // 프로젝트 삭제 뮤테이션
  const deleteProjectMutation = useDeleteProjectMutation();
  
  // 레포지토리 삭제 뮤테이션
  const deleteGithubRepoMutation = useDeleteGithubRepoMutation();

  // 서버에서 데이터를 가져왔을 때 로컬 스토어에 설정
  useEffect(() => {
    if (projectsFromServer) {
      setProjects(projectsFromServer);
    }
  }, [projectsFromServer, setProjects]);

  // 삭제 핸들러
  const handleDelete = async (id) => {
    // 삭제 확인
    if (!window.confirm('정말로 이 프로젝트를 삭제하시겠습니까?\n연결된 레포지토리도 함께 해제됩니다.')) {
      return;
    }
    
    setIsDeleting(true);
    
    try {
      // 프로젝트 상세 정보를 가져와서 연결된 레포지토리 확인
      const projectDetail = await getProjectDetailAPI(id);
      
      // 연결된 레포지토리들이 있다면 먼저 삭제
      if (projectDetail.repositories && projectDetail.repositories.length > 0) {
        console.log('연결된 레포지토리들을 먼저 삭제합니다:', projectDetail.repositories);
        
        // 모든 레포지토리를 병렬로 삭제
        const deletePromises = projectDetail.repositories.map(repo => 
          deleteGithubRepoMutation.mutateAsync({ repoId: repo.repoId })
        );
        
        await Promise.all(deletePromises);
        console.log('모든 레포지토리가 성공적으로 삭제되었습니다.');
      }
      
      // 레포지토리 삭제 완료 후 프로젝트 삭제
      deleteProjectMutation.mutate(
        { projectId: id },
        {
          onSuccess: () => {
            // 삭제 성공 시 로컬 스토어에서도 즉시 제거
            setProjects(projects.filter(project => project.id !== id));
            console.log('프로젝트가 성공적으로 삭제되었습니다.');
            alert('프로젝트가 성공적으로 삭제되었습니다.');
            
            // 다음 렌더링 후 스크롤 위치 복원
            setTimeout(() => {
              if (projectsSectionRef.current) {
                projectsSectionRef.current.scrollTop = currentScrollTop;
              }
            }, 0);
          },
          onError: (error) => {
            console.error('프로젝트 삭제에 실패했습니다:', error);
            alert('프로젝트 삭제에 실패했습니다. 다시 시도해주세요.');
          }
        }
      );
      
    } catch (error) {
      console.error('프로젝트 삭제 중 오류가 발생했습니다:', error);
      alert('프로젝트 삭제 중 오류가 발생했습니다. 다시 시도해주세요.');
    } finally {
      setIsDeleting(false);
    }
  };
 

  // 설정 모달 관련 핸들러
  const handleSettings = (project) => {
    setSelectedProject(project);
    // if (selectedProject) {
      openModal('PROJECT_EDIT', project);
    // }
  };

  // 프로젝트 생성 핸들러
  const handleCreateProject = () => {
    navigate('/project/create');
  };

  // 프로젝트 보드 페이지 이동 핸들러 -> 향후 백엔드와 연결해 프로젝트 id를 기반으로 각 프로젝트 보드 페이지로 이동
  const handleProjectBoard = (id) => {
    navigate(`/project/${id}`);
  };

  // 더보기 버튼 클릭 핸들러
  const handleLoadMore = () => {
    setVisibleCount(prev => prev + 10);
  };

  // const filteredProjects = localProjects.filter((project) => {
  const filteredProjects = projects.filter((project) => {
    if (activeFilter === '전체') return true;
    if (activeFilter === '진행 중') return !project.disabled;
    if (activeFilter === '완료') return project.disabled;
    return true;
  });

  // 필터가 변경될 때 visibleCount 초기화
  useEffect(() => {
    setVisibleCount(10);
  }, [activeFilter]);

  const handleModalClose = useCallback(() => {
    closeModal();
    setSelectedProject(null);
  }, [closeModal]);

  // const handleModalSave = useCallback((updatedProject) => {
  //   console.log('저장된 프로젝트:', updatedProject);
  //   updateProject(updatedProject);
  //   closeModal();
  //   setSelectedProject(null);
  // }, [closeModal, updateProject]);

  return (
    <section className={styles.projectsSection} ref={projectsSectionRef}>
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

      {isLoading ? (
        <div className={styles.noProjectsContainer}>
          <div className={styles.noProjectsContent}>
            <p className={styles.noProjectsTitle}>프로젝트를 불러오는 중...</p>
          </div>
        </div>
      ) : error ? (
        <div className={styles.noProjectsContainer}>
          <div className={styles.noProjectsContent}>
            <p className={styles.noProjectsTitle}>프로젝트를 불러오는데 실패했습니다.</p>
            <p className={styles.noProjectsSubtitle}>잠시 후 다시 시도해 주세요.</p>
          </div>
        </div>
      ) : filteredProjects.length === 0 ? (
        <div className={styles.noProjectsContainer}>
          <div className={styles.noProjectsContent}>
            <p className={styles.noProjectsTitle}>생성한 프로젝트가 아직 없어요.</p>
            <p className={styles.noProjectsSubtitle}>지금 바로 프로젝트를 생성하고 관리해 보세요.</p>
            <button className={styles.createProjectBtn} onClick={() => handleCreateProject()}>프로젝트 생성하기</button>
          </div>
        </div>
      ) : (
        <div className={styles.projectList}>
          {filteredProjects.slice(0, visibleCount).map((project) => (
            <div
              key={project.id}
              className={`${styles.projectCard} ${project.disabled ? styles.disabled : ''}`}
              onClick={() => handleProjectBoard(project.id)}
            >
              {/* 우측 상단 삭제, 설정 버튼 */}
              <div className={styles.cardActions}>
                <button 
                  className={`${styles.iconBtn} ${styles.deleteBtn}`} 
                  title={isDeleting ? "삭제 중..." : "삭제"} 
                  disabled={isDeleting}
                  onClick={(e) => {
                    e.stopPropagation(); // 카드 클릭 이벤트 버블링 방지
                    // 삭제 버튼을 클릭했을 때 상세 페이지로 이동하지 않기 위함
                    handleDelete(project.id)
                  }}
                >
                  {isDeleting && <div className={styles.loadingSpinner}></div>}
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
                {/* {project.stack.map((tech, index) => (
                  <span
                    key={index}
                    className={project.disabled ? styles.techTagDisabled : styles.techTagInProcess}
                  >
                    {tech}
                  </span>
                ))} */}
              </div>
            </div>
          ))}
          
          {/* 더보기 버튼 */}
          {visibleCount < filteredProjects.length && (
            <div className={styles.loadMoreContainer}>
              <button className={styles.loadMoreButton} onClick={handleLoadMore}>
                더보기
              </button>
            </div>
          )}
        </div>
      )}
    </section>
  );
};

export default ProjectsSection;
