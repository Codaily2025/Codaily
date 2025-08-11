import React, { useState } from 'react'
import { X } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import ProjectItem from '@/components/molecules/ProjectItem'
import styles from './Sidebar.module.css'

const Sidebar = ({ isOpen, onClose, projects = [], currentProjectId, isLoading = false }) => {
  const navigate = useNavigate()
  const [expandedProject, setExpandedProject] = useState(null)

  const handleProjectToggle = (projectName) => {
    setExpandedProject(expandedProject === projectName ? null : projectName)
  }

  const handleProjectClick = (projectId) => {
    console.log(`Project 관리 페이지로 이동: /project/${projectId}`)
    navigate(`/project/${projectId}`)
    onClose() // 사이드바 닫기
  }

  return (
    <>
      {/* Overlay */}
      {isOpen && <div className={styles.sidebarOverlay} onClick={onClose} />}
      
      {/* Sidebar */}
      <div className={`${styles.sidebar} ${isOpen ? styles.sidebarOpen : styles.sidebarClosed}`}>
        <div className={styles.sidebarHeader}>
          <h2 className={styles.sidebarTitle}>My Projects</h2>
          <button className={styles.sidebarCloseBtn} onClick={onClose}>
            <X size={20} />
          </button>
        </div>
        
        <div className={styles.sidebarContent}>
          {isLoading ? (
            <div className={styles.sidebarLoading}>프로젝트 목록을 불러오는 중...</div>
          ) : projects.length > 0 ? (
            projects.map((project) => (
              <ProjectItem
                key={project.projectId}
                project={{ ...project, name: project.title }}
                state={{
                  isActive: project.projectId === currentProjectId,
                  isExpanded: expandedProject === project.title
                }}
                onToggle={() => handleProjectToggle(project.title)}
                onClick={() => handleProjectClick(project.projectId)}
              />
            ))
          ) : (
            <div className={styles.noProjects}>진행 중인 프로젝트가 없습니다.</div>
          )}
        </div>
      </div>
    </>
  )
}

export default Sidebar